package hookups

import (
	"errors"
	"time"

	"github.com/shopspring/decimal"
	"immortalcrabcorp.com/fiscal-engine/internal/dal"
	"immortalcrabcorp.com/fiscal-engine/internal/dal/models"
	"immortalcrabcorp.com/fiscal-engine/internal/misc"
	notif "immortalcrabcorp.com/fiscal-engine/internal/notification"
	"immortalcrabcorp.com/fiscal-engine/internal/sagas"
	"immortalcrabcorp.com/fiscal-engine/pkg/log"

	"go.mongodb.org/mongo-driver/mongo"
)

type (
	FiscalEngine struct {
		dbID      string
		mcli      *mongo.Client
		conductor *notif.Conductor
		sched     *notif.SchedGateway
		logger    log.ILogger
	}
)

const (
	HandOverToDozer                   = "HAND_OVER_TO_DOZER"
	HandOverToProcessor               = "HAND_OVER_TO_PROCESSOR"
	GoodOutcomeFromProcessor          = "GOOD_OUTCOME_FROM_PROCESSOR"
	BadOutcomeFromProcessor           = "BAD_OUTCOME_FROM_PROCESSOR"
	HandOverToInvoiceCancelation      = "HAND_OVER_TO_INVOICE_CANCELATION"
	GoodOutcomeFromInvoiceCancelation = "GOOD_OUTCOME_FROM_INVOICE_CANCELATION"
	BadOutcomeFromInvoiceCancelation  = "BAD_OUTCOME_FROM_INVOICE_CANCELATION"
	firstSecondUnixEpoch              = 1
)

func NewFiscalEngine(logger log.ILogger, mongoURI string) *FiscalEngine {

	feng := &FiscalEngine{}
	feng.logger = logger
	feng.dbID = "receipts_db"
	err := dal.SetUpConnMongoDB(&(feng.mcli), mongoURI)
	if err != nil {
		panic(err.Error())
	}

	// Wire each interception id to its respective callbacks
	{
		feng.conductor = notif.NewConductor(
			feng.logger,
			[]notif.TransactionalLookupSlot{
				{HandOverToDozer, feng.callbackHandOverToDozer},
				{HandOverToProcessor, feng.callbackHandOverToProcessor},
				{GoodOutcomeFromProcessor, feng.callbackGoodOutcomeFromProcessor},
				{BadOutcomeFromProcessor, feng.callbackBadOutcomeFromProcessor},
				{HandOverToInvoiceCancelation, feng.callbackHandOverToInvoiceCancelation},
				{GoodOutcomeFromInvoiceCancelation, feng.callbackGoodOutcomeFromInvoiceCancelation},
				{BadOutcomeFromInvoiceCancelation, feng.callbackBadOutcomeFromInvoiceCancelation},
			})
	}

	// Wire specific interception ids for the incomming notifications
	{
		handlers := make(map[string]func([]byte) error)
		{
			makeDigestHandler := func(kind string) func([]byte) error {

				emit := feng.conductor.GetInterceptor()
				return func(rawMsg []byte) error {

					// Handler function to parse the basic message and call emit
					return digestNotification(rawMsg, func(args ...interface{}) {
						emit(kind, args...)
					})
				}
			}

			// For kinds sharing the same digest the handlers are dynamically created by a factory function
			supportedKinds := []string{
				HandOverToProcessor,
				BadOutcomeFromProcessor,
				HandOverToInvoiceCancelation,
				GoodOutcomeFromInvoiceCancelation,
				BadOutcomeFromInvoiceCancelation,
			}
			for _, kind := range supportedKinds {
				handlers[kind] = makeDigestHandler(kind)
			}
		}

		handlers[GoodOutcomeFromProcessor] = func(rawMsg []byte) error {
			emit := feng.conductor.GetInterceptor()
			presetEmit := func(args ...interface{}) {
				emit(GoodOutcomeFromProcessor, args...)
			}
			return digestGoodOutcomeFromProcessor(rawMsg, presetEmit)
		}

		// Use the handlers map for arrivalHandler
		arrivalHandler := func(kind string, rawMsg []byte) error {
			if handler, exists := handlers[kind]; exists {
				feng.logger.Info("Digest an incomming notifaction", "type", kind)
				return handler(rawMsg)
			}
			return errors.New("unsupported message type")
		}

		timeTable := misc.GetEnvOrDefault("TIMETABLE_SET_NAME", "timeTable")
		processorInput := misc.GetEnvOrDefault("PROCESSOR_INPUT_QUEUE_NAME", "processorInput")
		processorCancelInput := misc.GetEnvOrDefault("PROCESSOR_CANCEL_INPUT_QUEUE_NAME", "processorCancelInput")
		notificationsInput := misc.GetEnvOrDefault("NOTIFICATIONS_QUEUE_NAME", "notifications")

		// Energize the Gateway (it will start catching incomming notifications)
		feng.sched = notif.NewSchedGateway(feng.logger, timeTable, processorInput,
			processorCancelInput, notificationsInput, arrivalHandler)
	}

	return feng
}

func (self *FiscalEngine) DoCancelReceipt(receiptID, purpose, replacement string) error {

	if (purpose == "01" || purpose == "04") && replacement == "" {
		return errors.New("the cancelation purpose must provide a replacement")
	}

	db := self.mcli.Database(self.dbID)
	existingReceipt, err := dal.GetReceiptByID(db, receiptID)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return errors.New("receipt not found")
		}
		return err
	}

	return sagas.CancelReceiptThruSaga(
		existingReceipt,
		purpose, replacement,
		self.mcli.Database(self.dbID),
		self.sched,
		func(args ...interface{}) {

			emit := self.conductor.GetInterceptor()
			emit(HandOverToInvoiceCancelation, args...)
		})
}

func (self *FiscalEngine) DoCreateReceipt(dto *ReceiptDTO) (string, error) {

	if len(dto.Items) == 0 {
		return "", errors.New("receipt must have at least one item")
	}
	var zeroNonValidFlag bool

	// Convert ReceiptItemDTO to ReceiptItem and calculate totals
	items := make([]models.ReceiptItem, len(dto.Items))
	for i, itemDTO := range dto.Items {
		productQuantityDecimal := decimal.NewFromFloat(itemDTO.ProductQuantity)
		productUnitPriceDecimal := decimal.NewFromFloat(itemDTO.ProductUnitPrice)
		productAmountDecimal := productQuantityDecimal.Mul(productUnitPriceDecimal)
		productAmount, _ := productAmountDecimal.Float64()

		zeroNonValidFlag = productQuantityDecimal.Cmp(decimal.Zero) == 0 || productUnitPriceDecimal.Cmp(decimal.Zero) == 0

		// Convert product transfers and deductions, and calculate tax amounts
		transfers := convertTaxes(itemDTO.ProductTransfers, productAmount)
		deductions := convertTaxes(itemDTO.ProductDeductions, productAmount)
		items[i] = models.ReceiptItem{
			ProductID:         itemDTO.ProductID,
			ProductDesc:       itemDTO.ProductDesc,
			ProductQuantity:   itemDTO.ProductQuantity,
			ProductUnitPrice:  itemDTO.ProductUnitPrice,
			ProductUnit:       itemDTO.ProductUnit,
			ProductAmount:     productAmount,
			ProductTransfers:  transfers,
			ProductDeductions: deductions,
			FiscalProductID:   itemDTO.FiscalProductID,
			FiscalProductUnit: itemDTO.FiscalProductUnit,
		}
	}

	if zeroNonValidFlag {
		return "", errors.New("verify product(s) quantity and price, zero value not allowed")
	}

	var scheduleAt *time.Time = nil
	if dto.Chronology.DeferAt > 0 {
		var equivalent time.Time = time.Unix(dto.Chronology.DeferAt, 0)
		scheduleAt = &equivalent
	}

	now := time.Now()

	// Create the model
	receipt := &models.Receipt{
		Owner:            dto.Owner,
		ReceptorRFC:      dto.ReceptorRFC,
		ReceptorDataRef:  dto.ReceptorDataRef,
		DocumentCurrency: dto.DocumentCurrency,
		BaseCurrency:     dto.BaseCurrency,
		ExchangeRate:     dto.ExchangeRate,
		Items:            items,
		Purpose:          dto.Purpose,
		PaymentWay:       dto.PaymentWay,
		PaymentMethod:    dto.PaymentMethod,
		Serie:            dto.Serie,
		Comments:         dto.Comments,
		Chronology:       models.TimeTrial{RegistrationAt: &now, DeferAt: scheduleAt, StartedAt: nil, CompletedAt: nil},
	}

	return sagas.CreateReceiptThruSaga(
		receipt,
		self.mcli.Database(self.dbID),
		self.sched,
		func(args ...interface{}) {

			emit := self.conductor.GetInterceptor()
			emit(HandOverToDozer, args...)
		},
		func(args ...interface{}) {

			emit := self.conductor.GetInterceptor()
			emit(HandOverToProcessor, args...)
		})
}

func (self *FiscalEngine) DoEditReceipt(receiptID string, dto *ReceiptDTO) error {

	if len(dto.Items) == 0 {
		return errors.New("receipt must have at least one item")
	}

	db := self.mcli.Database(self.dbID)
	existingReceipt, err := dal.GetReceiptByID(db, receiptID)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return errors.New("receipt not found")
		}
		return err
	}

	// To preserve data integrity
	if existingReceipt.Chronology.DeferAt != nil {
		return errors.New("receipt is no longer editable")
	}

	// Copy required by saga in case of error
	existingReceiptCopy := *existingReceipt

	// Convert ReceiptItemDTO to ReceiptItem and calculate totals
	items := make([]models.ReceiptItem, len(dto.Items))
	for i, itemDTO := range dto.Items {
		productQuantityDecimal := decimal.NewFromFloat(itemDTO.ProductQuantity)
		productUnitPriceDecimal := decimal.NewFromFloat(itemDTO.ProductUnitPrice)
		productAmountDecimal := productQuantityDecimal.Mul(productUnitPriceDecimal)
		productAmount, _ := productAmountDecimal.Float64()

		// Convert product transfers and deductions, and calculate tax amounts
		transfers := convertTaxes(itemDTO.ProductTransfers, productAmount)
		deductions := convertTaxes(itemDTO.ProductDeductions, productAmount)

		items[i] = models.ReceiptItem{
			ProductID:         itemDTO.ProductID,
			ProductDesc:       itemDTO.ProductDesc,
			ProductQuantity:   itemDTO.ProductQuantity,
			ProductUnitPrice:  itemDTO.ProductUnitPrice,
			ProductUnit:       itemDTO.ProductUnit,
			ProductAmount:     productAmount,
			ProductTransfers:  transfers,
			ProductDeductions: deductions,
			FiscalProductID:   itemDTO.FiscalProductID,
			FiscalProductUnit: itemDTO.FiscalProductUnit,
		}
	}

	var scheduleAt *time.Time = nil
	if dto.Chronology.DeferAt > 0 {
		var equivalent time.Time = time.Unix(dto.Chronology.DeferAt, 0)
		scheduleAt = &equivalent
	}

	// Update the fields of the existing receipt
	existingReceipt.Owner = dto.Owner
	existingReceipt.ReceptorRFC = dto.ReceptorRFC
	existingReceipt.ReceptorDataRef = dto.ReceptorDataRef
	existingReceipt.DocumentCurrency = dto.DocumentCurrency
	existingReceipt.BaseCurrency = dto.BaseCurrency
	existingReceipt.ExchangeRate = dto.ExchangeRate
	existingReceipt.Items = items
	existingReceipt.Purpose = dto.Purpose
	existingReceipt.PaymentWay = dto.PaymentWay
	existingReceipt.PaymentMethod = dto.PaymentMethod
	existingReceipt.Serie = dto.Serie
	existingReceipt.Comments = dto.Comments
	existingReceipt.Chronology.DeferAt = scheduleAt

	return sagas.EditReceiptThruSaga(
		existingReceipt,
		&existingReceiptCopy,
		self.mcli.Database(self.dbID),
		self.sched,
		func(args ...interface{}) {

			emit := self.conductor.GetInterceptor()
			emit(HandOverToDozer, args...)
		},
		func(args ...interface{}) {

			emit := self.conductor.GetInterceptor()
			emit(HandOverToProcessor, args...)
		})
}

func (self *FiscalEngine) DoRetryReceiptOnError(receiptID string) error {

	db := self.mcli.Database(self.dbID)
	existingReceipt, err := dal.GetReceiptByID(db, receiptID)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return errors.New("receipt not found")
		}
		return err
	}

	if existingReceipt.Chronology.NonCompletedAt == nil {
		return errors.New("can't retry a receipt that is not failed")
	}

	// Copy required by saga in case of error
	existingReceiptCopy := *existingReceipt

	immediately := time.Unix(firstSecondUnixEpoch, 0)
	existingReceipt.Chronology.DeferAt = &immediately
	existingReceipt.Chronology.StartedAt = nil
	existingReceipt.Chronology.NonCompletedAt = nil

	return sagas.EditReceiptThruSaga(
		existingReceipt,
		&existingReceiptCopy,
		self.mcli.Database(self.dbID),
		self.sched,
		func(args ...interface{}) {

			emit := self.conductor.GetInterceptor()
			emit(HandOverToDozer, args...)
		},
		func(args ...interface{}) {

			emit := self.conductor.GetInterceptor()
			emit(HandOverToProcessor, args...)
		})
}

func (self *FiscalEngine) DoDeleteReceipt(receiptID string) (bool, error) {
	receiptDeleted, err := dal.DeleteFutileReceiptByID(self.mcli.Database(self.dbID), receiptID)
	return receiptDeleted, err
}

// convertTaxes converts a slice of TaxDTO to a slice of Tax, calculates amounts using Base and Rate
func convertTaxes(taxesDTO []TaxDTO, base float64) []models.Tax {
	taxes := make([]models.Tax, len(taxesDTO))
	for i, taxDTO := range taxesDTO {
		baseDecimal := decimal.NewFromFloat(base)
		rateDecimal := decimal.NewFromFloat(taxDTO.Rate)
		taxAmountDecimal := baseDecimal.Mul(rateDecimal)
		taxAmount, _ := taxAmountDecimal.Float64()

		taxes[i] = models.Tax{
			Base:         base, // Base comes from ProductAmount
			Rate:         taxDTO.Rate,
			FiscalFactor: taxDTO.FiscalFactor,
			FiscalType:   taxDTO.FiscalType,
			Transfer:     taxDTO.Transfer,
			Amount:       taxAmount, // Calculated tax amount
		}
	}
	return taxes
}
