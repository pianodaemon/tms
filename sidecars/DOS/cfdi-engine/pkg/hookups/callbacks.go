package hookups

import (
	"fmt"
	"time"

	"immortalcrabcorp.com/fiscal-engine/internal/dal"
)

func isMinimalArgsPresence(atLeast int, args ...interface{}) error {
	if len(args) < atLeast {
		return fmt.Errorf("expected at least %d arguments, but got %d", atLeast, len(args))
	}
	return nil
}

func (self *FiscalEngine) callbackHandOverToDozer(args ...interface{}) error {

	if err := isMinimalArgsPresence(1, args...); err != nil {
		return err
	}

	// Assume the first argument is receiptID as a string
	receiptID, ok := args[0].(string)
	if !ok {
		return fmt.Errorf("expected receiptID as a string, but got %T", args[0])
	}

	db := self.mcli.Database(self.dbID)
	recp, err := dal.GetReceiptByID(db, receiptID)
	if err != nil {
		return fmt.Errorf("callback cannot retrieve data from receipt %s: %v", receiptID, err)
	}

	formattedTime := recp.Chronology.DeferAt.Format("2006-01-02 15:04:05")
	self.logger.Info("Receipt billing has been scheduled", "receipt_id", receiptID, "schedule_at", formattedTime)
	return nil
}

func (self *FiscalEngine) callbackHandOverToProcessor(args ...interface{}) error {
	// Ensure there are at least 2 arguments (receiptID and timestamp)
	if err := isMinimalArgsPresence(2, args...); err != nil {
		return err
	}

	// Extract receiptID and timestamp with type assertions
	receiptID, ok := args[0].(string)
	if !ok {
		return fmt.Errorf("expected receiptID as a string, but got %T", args[0])
	}

	timestamp, ok := args[1].(time.Time)
	if !ok {
		return fmt.Errorf("expected timestamp as time.Time, but got %T", args[1])
	}

	db := self.mcli.Database(self.dbID)
	err := dal.UpdateStartedAt(db, receiptID, timestamp)
	if err != nil {
		return fmt.Errorf("callback cannot update the start time of %s: %v", receiptID, err)
	}

	self.logger.Info("Receipt billing has been started", "receipt_id", receiptID)
	return nil
}

func (self *FiscalEngine) callbackGoodOutcomeFromProcessor(args ...interface{}) error {
	// Ensure there are at least 3 arguments (receiptID and timestamp and doc_uuid)
	if err := isMinimalArgsPresence(3, args...); err != nil {
		return err
	}

	// Extract receiptID and timestamp with type assertions
	receiptID, ok := args[0].(string)
	if !ok {
		return fmt.Errorf("expected receiptID as a string, but got %T", args[0])
	}

	timestamp, ok := args[1].(time.Time)
	if !ok {
		return fmt.Errorf("expected timestamp as time.Time, but got %T", args[1])
	}

	docUUID, ok := args[2].(string)
	if !ok {
		return fmt.Errorf("expected docUUID as a string, but got %T", args[2])
	}

	db := self.mcli.Database(self.dbID)
	err := dal.UpdateCompletedAt(db, receiptID, timestamp, docUUID)
	if err != nil {
		return fmt.Errorf("callback cannot update the complete time of %s: %v", receiptID, err)
	}

	self.logger.Info("Receipt billing has been completed", "receipt_id", receiptID)
	return nil
}

func (self *FiscalEngine) callbackBadOutcomeFromProcessor(args ...interface{}) error {
	// Ensure there are at least 2 arguments (receiptID and timestamp)
	if err := isMinimalArgsPresence(2, args...); err != nil {
		return err
	}

	// Extract receiptID and timestamp with type assertions
	receiptID, ok := args[0].(string)
	if !ok {
		return fmt.Errorf("expected receiptID as a string, but got %T", args[0])
	}

	timestamp, ok := args[1].(time.Time)
	if !ok {
		return fmt.Errorf("expected timestamp as time.Time, but got %T", args[1])
	}

	db := self.mcli.Database(self.dbID)
	err := dal.UpdateNonCompletedAt(db, receiptID, timestamp)
	if err != nil {
		return fmt.Errorf("callback cannot update the complete time of %s: %v", receiptID, err)
	}

	self.logger.Info("Receipt billing has failed", "receipt_id", receiptID)
	return nil
}

func (self *FiscalEngine) callbackGoodOutcomeFromInvoiceCancelation(args ...interface{}) error {
	// Ensure there are at least 2 arguments (receiptID and timestamp)
	if err := isMinimalArgsPresence(2, args...); err != nil {
		return err
	}

	// Extract receiptID and timestamp with type assertions
	receiptID, ok := args[0].(string)
	if !ok {
		return fmt.Errorf("expected receiptID as a string, but got %T", args[0])
	}

	timestamp, ok := args[1].(time.Time)
	if !ok {
		return fmt.Errorf("expected timestamp as time.Time, but got %T", args[1])
	}

	db := self.mcli.Database(self.dbID)
	err := dal.UpdateCanceledAt(db, receiptID, timestamp)
	if err != nil {
		return fmt.Errorf("callback cannot update the cancelation time of %s: %v", receiptID, err)
	}

	self.logger.Info("Receipt cancelation has been completed", "receipt_id", receiptID)
	return nil
}

func (self *FiscalEngine) callbackBadOutcomeFromInvoiceCancelation(args ...interface{}) error {
	// Ensure there are at least 2 arguments (receiptID and timestamp)
	if err := isMinimalArgsPresence(2, args...); err != nil {
		return err
	}

	// Extract receiptID and timestamp with type assertions
	receiptID, ok := args[0].(string)
	if !ok {
		return fmt.Errorf("expected receiptID as a string, but got %T", args[0])
	}

	timestamp, ok := args[1].(time.Time)
	if !ok {
		return fmt.Errorf("expected timestamp as time.Time, but got %T", args[1])
	}

	db := self.mcli.Database(self.dbID)
	err := dal.UpdateCancelFailedAttempt(db, receiptID, timestamp)
	if err != nil {
		return fmt.Errorf("callback cannot update the cancelation failure time of %s: %v", receiptID, err)
	}

	self.logger.Info("Receipt cancelation has failed", "receipt_id", receiptID)
	return nil
}

func (self *FiscalEngine) callbackHandOverToInvoiceCancelation(args ...interface{}) error {
	// Ensure there are at least 2 arguments (receiptID and timestamp)
	if err := isMinimalArgsPresence(2, args...); err != nil {
		return err
	}

	// Extract receiptID and timestamp with type assertions
	receiptID, ok := args[0].(string)
	if !ok {
		return fmt.Errorf("expected receiptID as a string, but got %T", args[0])
	}

	timestamp, ok := args[1].(time.Time)
	if !ok {
		return fmt.Errorf("expected timestamp as time.Time, but got %T", args[1])
	}

	db := self.mcli.Database(self.dbID)
	err := dal.UpdateCancelAttemptStartedAt(db, receiptID, timestamp)
	if err != nil {
		return fmt.Errorf("callback cannot update the cancel start time of %s: %v", receiptID, err)
	}

	self.logger.Info("Receipt cancelation has been started", "receipt_id", receiptID)
	return nil
}
