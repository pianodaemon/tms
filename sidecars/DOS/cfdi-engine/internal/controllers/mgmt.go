package controllers

import (
	"net/http"

	"github.com/gin-gonic/gin"

	hups "immortalcrabcorp.com/fiscal-engine/pkg/hookups"
	"immortalcrabcorp.com/fiscal-engine/pkg/log"
)

type (
	FiscalEngineInterface interface {
		DoCreateReceipt(dto *hups.ReceiptDTO) (string, error)
		DoEditReceipt(receiptID string, dto *hups.ReceiptDTO) error
		DoRetryReceiptOnError(receiptID string) error
		DoCancelReceipt(receiptID, purpose, replacement string) error
		DoDeleteReceipt(receiptID string) (bool, error)
	}
)

func CreateReceipt(logger log.ILogger, fiscalEngineImplt FiscalEngineInterface) func(c *gin.Context) {

	return func(c *gin.Context) {

		dto := &hups.ReceiptDTO{}

		if errP := c.ShouldBind(dto); errP != nil {
			c.String(http.StatusBadRequest, "the body should be form of recipe type")
			return
		}

		name, err := fiscalEngineImplt.DoCreateReceipt(dto)
		if err != nil {
			logger.Error(err.Error())
			c.JSON(http.StatusInternalServerError, gin.H{
				"result": err.Error(),
			})
			return
		}

		logger.Info("ID created: " + name)
		c.JSON(http.StatusOK, gin.H{
			"results": name,
		})
	}
}

func EditReceipt(logger log.ILogger, fiscalEngineImplt FiscalEngineInterface) func(c *gin.Context) {

	return func(c *gin.Context) {

		dto := &hups.ReceiptDTO{}

		// Bind the JSON payload to the dto object
		if errP := c.ShouldBind(dto); errP != nil {
			c.String(http.StatusBadRequest, "the body should be in the form of receipt type")
			return
		}

		// Extract the receipt ID from the URL or request parameters
		receiptID := c.Param("id") // assumes the receipt ID is passed in the URL path as /receipts/:id
		if receiptID == "" {
			c.String(http.StatusBadRequest, "missing receipt ID")
			return
		}

		// Call the edit method in the fiscal engine
		err := fiscalEngineImplt.DoEditReceipt(receiptID, dto)
		if err != nil {
			logger.Error(err.Error())
			c.String(http.StatusInternalServerError, err.Error())
			return
		}

		// Send a success response
		c.JSON(http.StatusOK, gin.H{
			"result": "receipt updated successfully",
		})
	}
}

func RetryReceiptOnError(logger log.ILogger, fiscalEngineImplt FiscalEngineInterface) func(c *gin.Context) {

	return func(c *gin.Context) {

		// Extract the receipt ID from the URL or request parameters
		receiptID := c.Param("id") // assumes the receipt ID is passed in the URL path as /receipts/:id
		if receiptID == "" {
			c.String(http.StatusBadRequest, "missing receipt ID")
			return
		}

		// Call the edit method in the fiscal engine
		err := fiscalEngineImplt.DoRetryReceiptOnError(receiptID)
		if err != nil {
			logger.Error(err.Error())
			c.String(http.StatusBadRequest, err.Error())
			return
		}

		// Send a success response
		c.JSON(http.StatusOK, gin.H{
			"result": "receipt retried successfully",
		})
	}
}

func CancelReceipt(logger log.ILogger, fiscalEngineImplt FiscalEngineInterface, hasReplacement bool) func(c *gin.Context) {

	return func(c *gin.Context) {

		var replacementUuid string

		receiptID := c.Param("receipt_id")
		purpose := c.Param("purpose")
		if hasReplacement {
			replacementUuid = c.Param("replacement_uuid")
		}

		err := fiscalEngineImplt.DoCancelReceipt(receiptID, purpose, replacementUuid)
		if err != nil {
			logger.Error(err.Error())
			c.JSON(http.StatusBadRequest, gin.H{
				"result": err.Error(),
			})
			return
		}

		c.JSON(http.StatusOK, gin.H{
			"result": "cancelation request succesfully in process",
		})
	}
}

func DeleteReceipt(logger log.ILogger, fiscalEngineImplt FiscalEngineInterface) func(c *gin.Context) {
	return func(c *gin.Context) {

		receiptID := c.Param("id")
		if receiptID == "" {
			c.String(http.StatusBadRequest, "missing receipt ID")
			return
		}

		result, err := fiscalEngineImplt.DoDeleteReceipt(receiptID)

		if !result {
			c.JSON(http.StatusBadRequest, gin.H{
				"type":      "failure",
				"receiptID": receiptID,
				"details":   "Verify the receipt Id, couldn't be deleted",
			})
			return
		} else if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}

		c.JSON(http.StatusOK, gin.H{
			"type":      "success",
			"receiptID": receiptID,
		})
	}
}
