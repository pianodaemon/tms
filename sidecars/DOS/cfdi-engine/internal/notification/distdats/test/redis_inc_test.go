package verification

import (
	"context"
	"os"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/wait"

	"immortalcrabcorp.com/fiscal-engine/internal/notification/distdats"
)

// setupTestRedisContainer starts a Redis container and sets the environment variables needed for testing
func setupTestRedisContainer(t *testing.T) (testcontainers.Container, string, string) {
	ctx := context.Background()

	// Start Redis container
	req := testcontainers.ContainerRequest{
		Image:        "redis:alpine",
		ExposedPorts: []string{"6379/tcp"},
		WaitingFor:   wait.ForListeningPort("6379/tcp"),
	}
	redisContainer, err := testcontainers.GenericContainer(ctx, testcontainers.GenericContainerRequest{
		ContainerRequest: req,
		Started:          true,
	})
	if err != nil {
		t.Fatalf("failed to start redis container: %v", err)
	}

	// Get the mapped port
	redisHost, err := redisContainer.Host(ctx)
	if err != nil {
		t.Fatalf("failed to get redis container host: %v", err)
	}
	redisPort, err := redisContainer.MappedPort(ctx, "6379")
	if err != nil {
		t.Fatalf("failed to get redis container port: %v", err)
	}

	// Set environment variables for Redis connection
	os.Setenv("REDIS_HOST", redisHost)
	os.Setenv("REDIS_PORT", redisPort.Port())

	return redisContainer, redisHost, redisPort.Port()
}

func TestClusterQueue(t *testing.T) {
	// Set up Redis container for testing
	redisContainer, _, _ := setupTestRedisContainer(t)
	defer redisContainer.Terminate(context.Background())

	// Define Redis queue for testing
	var queue distdats.NetDistQueue = &distdats.RedisIncQueue{QueueName: "test_queue"}

	// Wait a moment for the Redis container to be ready
	time.Sleep(2 * time.Second)

	// Test isPresent function
	t.Run("isPresent", func(t *testing.T) {

		err := queue.Ping()
		assert.NoError(t, err)

		// Push a message to create the queue
		err = queue.Push("test_message")
		assert.NoError(t, err)

		// Now check if the queue exists
		exists, err := queue.IsPresent()
		assert.NoError(t, err)
		assert.True(t, exists)

		// Check pop functionality
		result, err := queue.Pop()
		assert.NoError(t, err)
		assert.Equal(t, "test_message", result)
	})
}

func TestNetDistSet(t *testing.T) {
	// Set up Redis container for testing
	redisContainer, _, _ := setupTestRedisContainer(t)
	defer redisContainer.Terminate(context.Background())

	// Define Redis set for testing
	var dset distdats.NetDistSet = &distdats.RedisIncSet{Name: "test_set"}

	// Wait a moment for the Redis container to be ready
	time.Sleep(2 * time.Second)

	// Test isPresent function
	t.Run("Fetch the ones are due", func(t *testing.T) {

		err := dset.Ping()
		assert.NoError(t, err)

		// Add items with scores (timestamps)
		err = dset.Add("item1", 1620000000)
		assert.NoError(t, err)
		err = dset.Add("item2", 1620000010)
		assert.NoError(t, err)

		// Now check if the set exists
		exists, err := dset.IsPresent()
		assert.NoError(t, err)
		assert.True(t, exists)

		// Fetch items that are due up to a specific timestamp
		items, err := dset.FetchTheOnesAreDue(1620000005)
		assert.NoError(t, err)
		assert.Equal(t, []string{"item1"}, items)

		// Fetch items that are due up to a later timestamp
		items, err = dset.FetchTheOnesAreDue(1620000015)
		assert.NoError(t, err)
		assert.Equal(t, []string{"item1", "item2"}, items)
	})
}
