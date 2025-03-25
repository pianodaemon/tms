package main

import (
	"syscall"

	kitlog "immortalcrabcorp.com/fiscal-engine/internal/kit/log"
	platform "immortalcrabcorp.com/fiscal-engine/internal/service"
)

const name = "FiscalEngine"

func main() {
	logger := kitlog.NewMarshmallowLogger(false)
	defer logger.Sync()

	if err := platform.Engage(logger); err != nil {

		logger.Fatal("Service struggles with error",
			"service", name,
			"error", err,
		)
	}

	syscall.Exit(0)
}
