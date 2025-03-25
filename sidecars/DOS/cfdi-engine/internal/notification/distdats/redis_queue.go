package distdats

import (
	"context"
	"os"

	"github.com/go-redis/redis/v8"
)

type RedisIncQueue struct {
	QueueName string
}

// connectToRedis establishes a connection to Redis
func (q *RedisIncQueue) connectToRedis() (*redis.Client, error) {
	redisHost := os.Getenv("REDIS_HOST")
	redisPort := os.Getenv("REDIS_PORT")

	return connectToRedis(redisHost, redisPort)
}

// isPresent checks if the Redis queue exists
func (q *RedisIncQueue) IsPresent() (bool, error) {
	client, err := q.connectToRedis()
	if err != nil {
		return false, err
	}
	defer client.Close()

	exists, err := client.Exists(context.Background(), q.QueueName).Result()
	return exists > 0, err
}

// ping checks the connection to Redis
func (q *RedisIncQueue) Ping() error {
	client, err := q.connectToRedis()
	if err != nil {
		return err
	}
	defer client.Close()

	_, err = client.Ping(context.Background()).Result()
	return err
}

// push adds a message to the tail of the queue
func (q *RedisIncQueue) Push(message string) error {
	client, err := q.connectToRedis()
	if err != nil {
		return err
	}
	defer client.Close()

	_, err = client.LPush(context.Background(), q.QueueName, message).Result()
	return err
}

// pop removes a message from the head of the queue (blocking)
func (q *RedisIncQueue) Pop() (string, error) {
	client, err := q.connectToRedis()
	if err != nil {
		return "", err
	}
	defer client.Close()

	result, err := client.BRPop(context.Background(), 0, q.QueueName).Result()
	if err != nil {
		return "", err
	}
	return result[1], nil
}
