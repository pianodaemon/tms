package notification

import (
	"context"
	"encoding/json"
	"fmt"
	"time"

	"immortalcrabcorp.com/fiscal-engine/internal/notification/distdats"
	"immortalcrabcorp.com/fiscal-engine/pkg/log"
)

type (
	SchedGateway struct {
		cset                 distdats.NetDistSet
		processorInput       distdats.NetDistQueue
		processorCancelInput distdats.NetDistQueue
		notificationsInput   distdats.NetDistQueue
		dispatcher           func(kind string, rawMsg []byte) error
		ctx                  context.Context
		cancel               context.CancelFunc
		done                 chan struct{} // Channel to signal completion
		logger               log.ILogger
	}
)

func NewSchedGateway(logger log.ILogger,
	timeTableName,
	processorInputQueueName, processorCancelQueueName,
	notificationsInputQueueName string,
	dispatcher func(kind string, rawMsg []byte) error) *SchedGateway {

	ctx, cancel := context.WithCancel(context.Background())
	sched := &SchedGateway{
		cset:                 &distdats.RedisIncSet{timeTableName},
		processorInput:       &distdats.RedisIncQueue{processorInputQueueName},
		processorCancelInput: &distdats.RedisIncQueue{processorCancelQueueName},
		notificationsInput:   &distdats.RedisIncQueue{notificationsInputQueueName},
		dispatcher:           dispatcher,
		ctx:                  ctx,
		cancel:               cancel,
		done:                 make(chan struct{}), // Initialize done channel
		logger:               logger,
	}

	go sched.startArrivals()
	return sched
}

func (self *SchedGateway) Submit(jobMsgJSON []byte, deferAt int64) error {

	// Add job to NetworkSet with current timestamp
	err := self.cset.Add(string(jobMsgJSON), deferAt)
	if err != nil {
		return fmt.Errorf("failed to submit billing job into the time table: %v", err)
	}

	return nil
}

func (self *SchedGateway) Forward(jobMsgJSON []byte) (time.Time, error) {

	now := time.Now()
	err := self.processorInput.Push(string(jobMsgJSON))
	if err != nil {
		return now, fmt.Errorf("failed to submit billing job into the processor: %v", err)
	}

	return now, nil
}

func (self *SchedGateway) AttemptCancelation(jobMsgJSON []byte) (time.Time, error) {

	now := time.Now()
	err := self.processorCancelInput.Push(string(jobMsgJSON))
	if err != nil {
		return now, fmt.Errorf("failed to submit cancelation attempt job into the processor: %v", err)
	}

	return now, nil
}

func (self *SchedGateway) startArrivals() {

	defer close(self.done) // Close done channel when startArrivals exits
	for {
		select {
		case <-self.ctx.Done(): // Listen for cancel signal
			return
		default:
			msg, err := self.notificationsInput.Pop()
			if err != nil {
				self.logger.Error("Error reading from notifications input", "error", err)
				time.Sleep(1 * time.Second) // Retry delay on error
				continue
			}
			if err := OnionDispatcher([]byte(msg), self.dispatcher); err != nil {
				self.logger.Error("Error in arrival handler", "error", err)
			}
		}
	}
}

// Close abandons the Arrivals function and cleans up resources
func (self *SchedGateway) Close() {
	self.cancel()
	<-self.done // Wait for startArrivals to finish
}

// Handles the parsing and dispatching of the message.
func OnionDispatcher(rawMsg []byte, dispatcher func(kind string, rawMsg []byte) error) error {

	type baseMessage struct {
		Kind string `json:"type"`
	}

	var baseMsg baseMessage

	// Unmarshal to determine the kind(type)
	if err := json.Unmarshal(rawMsg, &baseMsg); err != nil {
		return fmt.Errorf("error handling message of unknown kind")
	}

	// Dispatch the message based on its type.
	if err := dispatcher(baseMsg.Kind, rawMsg); err != nil {
		return fmt.Errorf("error handling message of type %s: %w", baseMsg.Kind, err)
	}

	return nil
}
