package distdats

type (
	NetDistQueue interface {
		IsPresent() (bool, error)
		Ping() error
		Push(message string) error
		Pop() (string, error)
	}

	NetDistSet interface {
		IsPresent() (bool, error)
		Ping() error
		Add(str string, timestamp int64) error
		FetchTheOnesAreDue(timestamp int64) ([]string, error)
	}
)
