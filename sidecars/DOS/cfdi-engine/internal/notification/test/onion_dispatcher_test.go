package notification_test

import (
	"encoding/json"
	"errors"
	"fmt"
	"strings"
	"testing"

	notif "immortalcrabcorp.com/fiscal-engine/internal/notification"
)

// mockDispatcher simulates message dispatching for testing.
func mockDispatcher(kind string, rawMsg []byte) error {
	// Define the expected structure for the message
	type Message struct {
		Kind      string `json:"type"`
		ReceiptID string `json:"receipt_id"`
		Timestamp int64  `json:"timestamp"`
	}

	// Valid event types
	const (
		HandOverToDozer                   = "HAND_OVER_TO_DOZER"
		HandOverToProcessor               = "HAND_OVER_TO_PROCESSOR"
		GoodOutcomeFromProcessor          = "GOOD_OUTCOME_FROM_PROCESSOR"
		BadOutcomeFromProcessor           = "BAD_OUTCOME_FROM_PROCESSOR"
		GoodOutcomeFromInvoiceCancelation = "GOOD_OUTCOME_FROM_INVOICE_CANCELATION"
		BadOutcomeFromInvoiceCancelation  = "BAD_OUTCOME_FROM_INVOICE_CANCELATION"
	)

	// Check if kind is a valid event type
	switch kind {
	case HandOverToDozer, HandOverToProcessor, GoodOutcomeFromProcessor, BadOutcomeFromProcessor,
		GoodOutcomeFromInvoiceCancelation, BadOutcomeFromInvoiceCancelation:
		// Unmarshal the message to check for receipt_id and timestamp fields
		var msg Message
		if err := json.Unmarshal(rawMsg, &msg); err != nil {
			return fmt.Errorf("error unmarshaling message: %v", err)
		}

		if msg.Kind != kind {
			return fmt.Errorf("found a weird type after unmarshaling message")
		}

		// Validate required fields
		if msg.ReceiptID != "12345" {
			if msg.ReceiptID == "" {
				return errors.New("missing receipt_id in message")
			}
			return errors.New("found a weird receipt_id in message")
		}
		if msg.Timestamp != 1634020500 {
			return errors.New("missing or invalid timestamp in message")
		}

		// If both fields are present, return no error
		return nil

	case "errorType":
		return errors.New("simulated dispatch error")
	default:
		return fmt.Errorf("unsupported message type: %s", kind)
	}
}

func TestOnionDispatcher(t *testing.T) {
	tests := []struct {
		name          string
		rawMsg        []byte
		expectedError string
	}{
		{
			name: "Valid message with HandOverToDozer type",
			rawMsg: []byte(`{
				"type": "HAND_OVER_TO_DOZER",
				"receipt_id": "12345",
				"timestamp": 1634020500
			}`),
			expectedError: "", // No error expected
		},
		{
			name: "Valid message with HandOverToProcessor type",
			rawMsg: []byte(`{
				"type": "HAND_OVER_TO_PROCESSOR",
				"receipt_id": "12345",
				"timestamp": 1634020500
			}`),
			expectedError: "", // No error expected
		},
		{
			name: "Valid message with GoodOutcomeFromProcessor type",
			rawMsg: []byte(`{
				"type": "GOOD_OUTCOME_FROM_PROCESSOR",
				"receipt_id": "12345",
				"timestamp": 1634020500
			}`),
			expectedError: "", // No error expected
		},
		{
			name: "Valid message with BadOutcomeFromProcessor type",
			rawMsg: []byte(`{
				"type": "BAD_OUTCOME_FROM_PROCESSOR",
				"receipt_id": "12345",
				"timestamp": 1634020500
			}`),
			expectedError: "", // No error expected
		},
		{
			name: "Valid message with GoodOutcomeFromInvoiceCancelation type",
			rawMsg: []byte(`{
				"type": "GOOD_OUTCOME_FROM_INVOICE_CANCELATION",
				"receipt_id": "12345",
				"timestamp": 1634020500
			}`),
			expectedError: "", // No error expected
		},
		{
			name: "Valid message with BadOutcomeFromInvoiceCancelation type",
			rawMsg: []byte(`{
				"type": "BAD_OUTCOME_FROM_INVOICE_CANCELATION",
				"receipt_id": "12345",
				"timestamp": 1634020500
			}`),
			expectedError: "", // No error expected
		},
		{
			name:          "Invalid JSON format",
			rawMsg:        []byte(`{"type":`), // Malformed JSON
			expectedError: "error handling message of unknown kind",
		},
		{
			name: "Unsupported message type",
			rawMsg: []byte(`{
				"type": "UNSUPPORTED_TYPE",
				"receipt_id": "12345",
				"timestamp": 1634020500
			}`),
			expectedError: "unsupported message type: UNSUPPORTED_TYPE",
		},
		{
			name: "Missing receipt_id in message",
			rawMsg: []byte(`{
				"type": "HAND_OVER_TO_DOZER",
				"timestamp": 1634020500
			}`),
			expectedError: "missing receipt_id in message",
		},
		{
			name: "Dispatch error for supported type",
			rawMsg: []byte(`{
				"type": "errorType",
				"receipt_id": "12345",
				"timestamp": 1634020500
			}`),
			expectedError: "simulated dispatch error",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			err := notif.OnionDispatcher(tt.rawMsg, mockDispatcher)
			if err != nil {
				if tt.expectedError == "" {
					t.Errorf("expected no error, got: %v", err)
				} else if !strings.Contains(err.Error(), tt.expectedError) {
					t.Errorf("expected error to contain: %v, got: %v", tt.expectedError, err)
				}
			} else if tt.expectedError != "" {
				t.Errorf("expected error: %v, got: nil", tt.expectedError)
			}
		})
	}
}
