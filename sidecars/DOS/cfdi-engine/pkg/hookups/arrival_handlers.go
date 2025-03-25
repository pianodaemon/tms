package hookups

import (
	"encoding/json"
	"fmt"
	"time"
)

func digestNotification(
	rawMsg []byte,
	emit func(args ...interface{})) error {

	var msg struct {
		Type      string `json:"type"`
		ReceiptID string `json:"receipt_id"`
		Timestamp int64  `json:"timestamp"`
	}

	if err := json.Unmarshal(rawMsg, &msg); err != nil {
		return fmt.Errorf("failed to parse message: %w", err)
	}

	// Pass receiptID and timestamp as arguments to emit
	emit(msg.ReceiptID, time.Unix(msg.Timestamp, 0))

	return nil
}

func digestGoodOutcomeFromProcessor(
	rawMsg []byte,
	emit func(args ...interface{})) error {

	var msg struct {
		Type      string `json:"type"`
		ReceiptID string `json:"receipt_id"`
		Timestamp int64  `json:"timestamp"`
		DocUUID   string `json:"doc_uuid"`
	}

	if err := json.Unmarshal(rawMsg, &msg); err != nil {
		return fmt.Errorf("failed to parse message: %w", err)
	}

	// Pass receiptID and timestamp as arguments to emit
	emit(msg.ReceiptID, time.Unix(msg.Timestamp, 0), msg.DocUUID)

	return nil
}
