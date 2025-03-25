package notification

import (
	"immortalcrabcorp.com/fiscal-engine/internal/notification/eloop"
	"immortalcrabcorp.com/fiscal-engine/pkg/log"
)

type (
	Conductor struct {
		eventLoop *eloop.SlackEventLoop
	}

	TransactionalLookupSlot struct {
		Title                string
		TransactionalHandler interface{}
	}
)

// Constructor for Conductor that accepts transactionalLookUp as an argument
func NewConductor(logger log.ILogger, transactionalLookUp []TransactionalLookupSlot) *Conductor {

	conductor := new(Conductor)
	conductor.eventLoop = eloop.NewEventLoop(logger)
	for _, it := range transactionalLookUp {
		conductor.eventLoop.Subscribe(it.Title, it.TransactionalHandler)
	}
	conductor.eventLoop.TurnOn()

	return conductor
}

func (self *Conductor) GetInterceptor() func(title string, data ...interface{}) {

	return func(title string, data ...interface{}) {

		eventInfo := eloop.Occurrence{title, data}

		if accepted := self.eventLoop.TryPublish(&eventInfo); !accepted {
			panic("Event enqueuing has not been possible during latter attempt")
		}
	}
}

// Cleanup code for the resource, e.g., releasing memory or closing connections
func (self *Conductor) Close() {

	self.eventLoop.TurnOff()
}
