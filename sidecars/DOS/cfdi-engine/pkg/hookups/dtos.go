package hookups

import (
	"time"
)

type (
	TimeTrialDTO struct {
		DeferAt        int64      `json:"defer_at"` // It stands for a UNIX timestamp in seconds
		StartedAt      *time.Time `json:"started_at"`
		CompletedAt    *time.Time `json:"completed_at"`
		NonCompletedAt *time.Time `json:"non_completed_at"`
	}

	ReceiptItemDTO struct {
		ProductID         string   `json:"product_id" binding:"required"`
		ProductDesc       string   `json:"product_desc" binding:"required"`
		ProductQuantity   float64  `json:"product_quantity" binding:"required"`
		ProductUnitPrice  float64  `json:"product_unit_price" binding:"required"`
		ProductUnit       string   `json:"product_unit" binding:"required"`
		ProductTransfers  []TaxDTO `json:"product_transfers"`
		ProductDeductions []TaxDTO `json:"product_deductions"`
		FiscalProductID   string   `json:"fiscal_product_id" binding:"required"`
		FiscalProductUnit string   `json:"fiscal_product_unit" binding:"required"`
	}

	ReceiptDTO struct {
		Owner            string           `json:"owner" binding:"required"`
		ReceptorRFC      string           `json:"receptor_rfc" binding:"required"`
		ReceptorDataRef  string           `json:"receptor_data_ref" binding:"required"`
		DocumentCurrency string           `json:"document_currency" binding:"required"`
		BaseCurrency     string           `json:"base_currency" binding:"required"`
		ExchangeRate     float64          `json:"exchange_rate" binding:"required"`
		Items            []ReceiptItemDTO `json:"items" binding:"required"`
		Purpose          string           `json:"purpose" binding:"required"`
		PaymentWay       string           `json:"payment_way" binding:"required"`
		PaymentMethod    string           `json:"payment_method" binding:"required"`
		Serie            int              `json:"serie" binding:"required"`
		Chronology       TimeTrialDTO     `json:"chronology"`
		Comments         string           `json:"comments"`
	}

	TaxDTO struct {
		Rate         float64 `json:"rate" binding:"required"`
		FiscalFactor string  `json:"fiscal_factor" binding:"required"`
		FiscalType   string  `json:"fiscal_type" binding:"required"`
		Transfer     bool    `json:"transfer" binding:"required"`
	}
)
