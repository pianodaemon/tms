package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type (
	Annotations struct {
		PacDocUUID string `json:"pac_doc_uuid_ref,omitempty" bson:"pac_doc_uuid_ref,omitempty"`
	}

	TimeTrial struct {
		RegistrationAt *time.Time `json:"registration_at,omitempty" bson:"registration_at,omitempty"`
		DeferAt        *time.Time `json:"scheduled_at,omitempty" bson:"scheduled_at,omitempty"`
		StartedAt      *time.Time `json:"started_at,omitempty" bson:"started_at,omitempty"`
		CompletedAt    *time.Time `json:"completed_at,omitempty" bson:"completed_at,omitempty"`
		NonCompletedAt *time.Time `json:"non_completed_at,omitempty" bson:"non_completed_at,omitempty"`
	}

	Tax struct {
		Base         float64 `json:"base" bson:"base,omitempty"`
		Rate         float64 `json:"rate" bson:"rate,omitempty"`
		Amount       float64 `json:"amount" bson:"amount,omitempty"`
		FiscalFactor string  `json:"fiscal_factor" bson:"fiscal_factor,omitempty"`
		FiscalType   string  `json:"fiscal_type" bson:"fiscal_type,omitempty"`
		Transfer     bool    `json:"transfer" bson:"transfer,omitempty"`
	}

	Receipt struct {
		ID               primitive.ObjectID `json:"_id" bson:"_id,omitempty"`
		Owner            string             `json:"owner" bson:"owner,omitempty"`
		ReceptorRFC      string             `json:"receptor_rfc" bson:"receptor_rfc,omitempty"`
		ReceptorDataRef  string             `json:"receptor_data_ref" bson:"receptor_data_ref,omitempty"`
		DocumentCurrency string             `json:"document_currency" bson:"document_currency,omitempty"`
		BaseCurrency     string             `json:"base_currency" bson:"base_currency,omitempty"`
		ExchangeRate     float64            `json:"exchange_rate" bson:"exchange_rate,omitempty"`
		Items            []ReceiptItem      `json:"items" bson:"items,omitempty"`
		Purpose          string             `json:"purpose" bson:"purpose,omitempty"`
		Comments         string             `json:"comments" bson:"comments,omitempty"`
		PaymentWay       string             `json:"payment_way" bson:"payment_way,omitempty"`
		PaymentMethod    string             `json:"payment_method" bson:"payment_method,omitempty"`
		Serie            int                `json:"serie" bson:"serie,omitempty"`
		Chronology       TimeTrial          `json:"chronology" bson:"chronology,omitempty"`
		Meta             Annotations        `json:"meta" bson:"meta,omitempty"`
	}

	ReceiptItem struct {
		ProductID         string  `json:"product_id" bson:"product_id,omitempty"`
		ProductDesc       string  `json:"product_desc" bson:"product_desc,omitempty"`
		ProductQuantity   float64 `json:"product_quantity" bson:"product_quantity,omitempty"`
		ProductUnitPrice  float64 `json:"product_unit_price" bson:"product_unit_price,omitempty"`
		ProductUnit       string  `json:"product_unit" bson:"product_unit,omitempty"`
		ProductAmount     float64 `json:"product_amount" bson:"product_amount,omitempty"`
		ProductTransfers  []Tax   `json:"product_transfers" bson:"product_transfers,omitempty"`
		ProductDeductions []Tax   `json:"product_deductions" bson:"product_deductions,omitempty"`
		FiscalProductID   string  `json:"fiscal_product_id" bson:"fiscal_product_id,omitempty"`
		FiscalProductUnit string  `json:"fiscal_product_unit" bson:"fiscal_product_unit,omitempty"`
	}
)
