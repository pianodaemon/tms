package log

type (
	ILogger interface {
		Info(msg string, fields ...interface{})
		Debug(msg string, fields ...interface{})
		Error(msg string, fields ...interface{})
		Fatal(msg string, fields ...interface{})
		WithContext(fields ...interface{}) ILogger
		Sync()
	}
)
