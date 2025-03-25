package notification_test

import (
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	kitlog "immortalcrabcorp.com/fiscal-engine/internal/kit/log"
	"immortalcrabcorp.com/fiscal-engine/internal/notification"
)

func TestGetInterceptor(t *testing.T) {

	type Dummy struct {
		Anumber int
	}

	// Handler for TestEventX
	handler0Arg0 := ""
	handler0Arg1 := &Dummy{0}
	handler0 := func(arg0 string, arg1 *Dummy) error {
		handler0Arg0 = arg0
		handler0Arg1.Anumber = arg1.Anumber
		return nil
	}

	// Handler for TestEventY
	handler1Arg0 := 0
	handler1 := func(arg0 int) error {
		handler1Arg0 = arg0
		return nil
	}

	// Handler for TestEventZ
	handler2Arg0 := ""
	handler2Arg1 := 0.0
	handler2 := func(arg0 string, arg1 float64) error {
		handler2Arg0 = arg0
		handler2Arg1 = arg1
		return nil
	}

	// Setting up the transactional lookup with multiple handlers
	transactionalLookUp := []notification.TransactionalLookupSlot{
		{Title: "TestEventX", TransactionalHandler: handler0},
		{Title: "TestEventY", TransactionalHandler: handler1},
		{Title: "TestEventZ", TransactionalHandler: handler2},
	}

	conductor := notification.NewConductor(kitlog.NewMarshmallowLogger(true), transactionalLookUp)
	defer conductor.Close()

	emit := conductor.GetInterceptor()

	// Allow some time for asynchronous event handling
	time.Sleep(300 * time.Millisecond)

	// Trigger each event and check handlers' arguments
	emit("TestEventX", "first_arg_test_gotten", &Dummy{666})
	time.Sleep(300 * time.Millisecond) // Adjust time if needed

	assert.Equal(t, handler0Arg0, "first_arg_test_gotten")
	assert.Equal(t, handler0Arg1.Anumber, 666)

	emit("TestEventY", 42)
	time.Sleep(300 * time.Millisecond)

	assert.Equal(t, handler1Arg0, 42)

	emit("TestEventZ", "test_string", 3.14)
	time.Sleep(300 * time.Millisecond)

	assert.Equal(t, handler2Arg0, "test_string")
	assert.Equal(t, handler2Arg1, 3.14)
}

func TestClose(t *testing.T) {

	type Dummy struct {
		Anumber int
	}

	handler0Arg0 := ""
	handler0Arg1 := &Dummy{0}
	handler0 := func(arg0 string, arg1 *Dummy) error {
		handler0Arg0 = arg0
		handler0Arg1.Anumber = arg1.Anumber
		return nil
	}

	transactionalLookUp := []notification.TransactionalLookupSlot{
		{Title: "TestEvent", TransactionalHandler: handler0},
	}

	conductor := notification.NewConductor(kitlog.NewMarshmallowLogger(true), transactionalLookUp)
	conductor.Close()

	emit := conductor.GetInterceptor()

	// Allow some time for asynchronous event handling
	time.Sleep(300 * time.Millisecond) // Adjust time if needed

	// Use interceptor to trigger the event
	emit("TestEvent", "first_arg_test_gotten", &Dummy{666})

	time.Sleep(300 * time.Millisecond) // Adjust time if needed

	assert.NotEqual(t, handler0Arg0, "first_arg_test_gotten")
	assert.NotEqual(t, handler0Arg1.Anumber, 666)
}
