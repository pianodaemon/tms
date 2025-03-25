package distdats

import (
	"context"
	"errors"
	"fmt"

	"github.com/go-redis/redis/v8"
)

// connectToRedis establishes a connection to Redis
func connectToRedis(redisHost, redisPort string) (*redis.Client, error) {

	if redisHost == "" || redisPort == "" {
		return nil, errors.New("variables redishost or redisport not set")
	}

	connectionStr := fmt.Sprintf("%s:%s", redisHost, redisPort)
	client := redis.NewClient(&redis.Options{
		Addr: connectionStr,
		DB:   0,
	})

	_, err := client.Ping(context.Background()).Result()
	if err != nil {
		return nil, err
	}

	return client, nil
}
