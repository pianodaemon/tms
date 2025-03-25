package sagas

import (
	"encoding/json"
	"fmt"
	"time"

	"immortalcrabcorp.com/fiscal-engine/internal/dal"
	"immortalcrabcorp.com/fiscal-engine/internal/dal/models"
	"immortalcrabcorp.com/fiscal-engine/internal/misc"
	notif "immortalcrabcorp.com/fiscal-engine/internal/notification"

	"go.mongodb.org/mongo-driver/mongo"
)

const firstSecondUnixEpoch = 1

func CreateReceiptThruSaga(
	receipt *models.Receipt,
	db *mongo.Database,
	sched *notif.SchedGateway,
	emitDeferredSubmition func(args ...interface{}),
	emitRigthAwaySubmition func(args ...interface{})) (string, error) {

	var (
		jobMsgJSON []byte
		timestamp  time.Time
	)

	id, err := dal.CreateReceipt(db, receipt)
	if err != nil {
		goto handleError
	}

	if receipt.Chronology.DeferAt != nil {
		jobMsgJSON, err = setoutTimeTableMsg(receipt)
		if err != nil {
			goto handleError
		}

		if *receipt.Chronology.DeferAt == time.Unix(firstSecondUnixEpoch, 0) {
			timestamp, err = sched.Forward(jobMsgJSON)
			if err != nil {
				goto handleError
			}
			emitRigthAwaySubmition(id.Hex(), timestamp)

		} else {
			err = sched.Submit(jobMsgJSON, (*receipt.Chronology.DeferAt).Unix())
			if err != nil {
				goto handleError
			}
			emitDeferredSubmition(id.Hex())
		}
	}

	return id.Hex(), nil

handleError:
	// Undo receipt creation
	dal.DeleteReceiptByID(db, id.Hex())
	return "", err
}

func setoutTimeTableMsg(receipt *models.Receipt) ([]byte, error) {

	type billingJobMsg struct {
		Receipt models.Receipt `json:"receipt"`
		FKeys   misc.FKeys     `json:"fkeys"`
	}

	var (
		jobMsg     billingJobMsg
		jobMsgJSON []byte
	)

	bucketName := misc.GetEnvOrDefault("BUCKET_NAME", "test-bucket")
	fkeys, err := misc.GetFKeys(bucketName, receipt.Owner)
	if err != nil {
		return nil, fmt.Errorf("failed to obtain the fkeys: %v", err)
	}

	// Prepare the job message
	jobMsg = billingJobMsg{Receipt: *receipt, FKeys: *fkeys}

	// Marshal final job message
	jobMsgJSON, err = json.Marshal(jobMsg)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal billing job: %v", err)
	}

	return jobMsgJSON, nil
}

func EditReceiptThruSaga(
	receipt *models.Receipt,
	receiptBackup *models.Receipt,
	db *mongo.Database,
	sched *notif.SchedGateway,
	emitDeferredSubmition func(args ...interface{}),
	emitRigthAwaySubmition func(args ...interface{})) error {

	// Save the updated receipt back to the database
	err := dal.EditReceipt(db, receipt)
	if err != nil {
		return err
	}

	var (
		jobMsgJSON []byte
		timestamp  time.Time
	)

	if receipt.Chronology.DeferAt != nil {
		jobMsgJSON, err = setoutTimeTableMsg(receipt)
		if err != nil {
			goto handleError
		}

		if *receipt.Chronology.DeferAt == time.Unix(firstSecondUnixEpoch, 0) {
			timestamp, err = sched.Forward(jobMsgJSON)
			if err != nil {
				goto handleError
			}
			emitRigthAwaySubmition(receipt.ID.Hex(), timestamp)

		} else {
			err = sched.Submit(jobMsgJSON, (*receipt.Chronology.DeferAt).Unix())
			if err != nil {
				goto handleError
			}
			emitDeferredSubmition(receipt.ID.Hex())
		}
	}

	return nil

handleError:
	_ = dal.EditReceipt(db, receiptBackup)
	return err
}

func CancelReceiptThruSaga(
	receipt *models.Receipt,
	purpose, replacement string,
	db *mongo.Database,
	sched *notif.SchedGateway,
	emitCancelationSent func(args ...interface{})) error {

	type (
		CancelBody struct {
			ReceiptID   string `json:"receipt_id"`
			DocUUID     string `json:"doc_uuid"`
			Purpose     string `json:"purpose"`
			Replacement string `json:"replacement,omitempty"`
		}

		CancelMsg struct {
			Body  CancelBody `json:"invoice_cancelation"`
			FKeys misc.FKeys `json:"fkeys"`
		}
	)

	var (
		jobMsg     CancelMsg
		jobMsgJSON []byte
	)

	cancelBody := &CancelBody{
		ReceiptID:   receipt.ID.Hex(),
		DocUUID:     receipt.Meta.PacDocUUID,
		Purpose:     purpose,
		Replacement: replacement,
	}

	bucketName := misc.GetEnvOrDefault("BUCKET_NAME", "test-bucket")
	fkeys, err := misc.GetFKeys(bucketName, receipt.Owner)
	if err != nil {
		return fmt.Errorf("failed to obtain the fkeys: %v", err)
	}

	// Prepare the job message
	jobMsg = CancelMsg{Body: *cancelBody, FKeys: *fkeys}

	// Marshal final job message
	jobMsgJSON, err = json.Marshal(jobMsg)
	if err != nil {
		return fmt.Errorf("failed to marshal cancel job: %v", err)
	}

	timestamp, err := sched.AttemptCancelation(jobMsgJSON)
	if err != nil {
		return err
	}
	emitCancelationSent(receipt.ID.Hex(), timestamp)
	return nil
}
