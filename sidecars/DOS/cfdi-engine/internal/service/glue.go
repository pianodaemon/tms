package service

import (
	"github.com/gin-gonic/gin"

	co "immortalcrabcorp.com/fiscal-engine/internal/controllers"
	"immortalcrabcorp.com/fiscal-engine/internal/misc"
	hups "immortalcrabcorp.com/fiscal-engine/pkg/hookups"
	"immortalcrabcorp.com/fiscal-engine/pkg/log"
)

// Engages the RESTful API
func Engage(logger log.ILogger) (merr error) {

	defer func() {

		if r := recover(); r != nil {
			merr = r.(error)
		}
	}()

	fiscalEngineImplt := hups.NewFiscalEngine(logger, misc.GetEnvOrDefault("MONGO_URI", "mongodb://127.0.0.1:27017"))

	r := gin.Default()
	setUpHandlers(logger, r, fiscalEngineImplt)
	r.Run() // listen and serve on 0.0.0.0:8080

	return nil
}

func setUpHandlers(logger log.ILogger, r *gin.Engine, pm co.FiscalEngineInterface) {

	logger.Debug("Running controllers setup")
	r.GET("/ping", co.Health)
	r.POST("/receipts", co.CreateReceipt(logger, pm))
	r.PUT("/receipts/:id", co.EditReceipt(logger, pm))
	r.DELETE("/receipts/:id", co.DeleteReceipt(logger, pm))
	r.PUT("/receipts/:id/retry", co.RetryReceiptOnError(logger, pm))
	r.POST("/receipts/cancel/:receipt_id/:purpose", co.CancelReceipt(logger, pm, false))
	r.POST("/receipts/cancel/:receipt_id/:purpose/:replacement_uuid", co.CancelReceipt(logger, pm, true))
}
