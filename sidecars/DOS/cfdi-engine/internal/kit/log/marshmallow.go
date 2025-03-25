package log

import (
	"bytes"

	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"

	"immortalcrabcorp.com/fiscal-engine/pkg/log"
)

type MarshmallowLogger struct {
	logger *zap.SugaredLogger
}

// NewMarshmallowLogger initializes the logger for production or test use
func NewMarshmallowLogger(testing bool) log.ILogger {
	var zapLogger *zap.Logger
	var err error

	if testing {
		// Test configuration for an in-memory logger
		buffer := new(bytes.Buffer)
		core := zapcore.NewCore(
			zapcore.NewConsoleEncoder(zap.NewDevelopmentEncoderConfig()),
			zapcore.AddSync(buffer),
			zap.DebugLevel,
		)
		zapLogger = zap.New(core, zap.AddCaller(), zap.AddCallerSkip(1)) // Set AddCallerSkip for test logger
	} else {
		// Production logger
		zapLogger, err = zap.NewProduction()
		if err != nil {
			panic("Failed to initialize Zap logger")
		}
		zapLogger = zapLogger.WithOptions(zap.AddCaller(), zap.AddCallerSkip(1)) // Adjust AddCallerSkip for production
	}

	return &MarshmallowLogger{
		logger: zapLogger.Sugar(),
	}
}

// Ensure logs are flushed on exit
func (l *MarshmallowLogger) Sync() {
	l.logger.Sync()
}

// Info logs an informational message
func (l *MarshmallowLogger) Info(msg string, fields ...interface{}) {
	l.logger.Infow(msg, fields...)
}

// Debug logs an error message
func (l *MarshmallowLogger) Debug(msg string, fields ...interface{}) {
	l.logger.Debugw(msg, fields...)
}

// Error logs an error message
func (l *MarshmallowLogger) Error(msg string, fields ...interface{}) {
	l.logger.Errorw(msg, fields...)
}

// Fatal logs a fatal error and exits
func (l *MarshmallowLogger) Fatal(msg string, fields ...interface{}) {
	l.logger.Fatalw(msg, fields...)
}

// Adds by default file:line to the logger's context
func (l *MarshmallowLogger) WithContext(contextFields ...interface{}) log.ILogger {
	return &MarshmallowLogger{logger: l.logger.With(contextFields...)}
}
