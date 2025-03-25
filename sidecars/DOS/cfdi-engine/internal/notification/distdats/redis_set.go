package distdats

import (
	"context"
	"os"
	"strconv"

	"github.com/go-redis/redis/v8"
)

type RedisIncSet struct {
	Name string
}

// connectToRedis establishes a connection to Redis
func (s *RedisIncSet) connectToRedis() (*redis.Client, error) {
	redisHost := os.Getenv("REDIS_HOST")
	redisPort := os.Getenv("REDIS_PORT")

	return connectToRedis(redisHost, redisPort)
}

// isPresent checks if the Redis set exists
func (s *RedisIncSet) IsPresent() (bool, error) {
	client, err := s.connectToRedis()
	if err != nil {
		return false, err
	}
	defer client.Close()

	exists, err := client.Exists(context.Background(), s.Name).Result()
	return exists > 0, err
}

// ping checks the connection to Redis
func (s *RedisIncSet) Ping() error {
	client, err := s.connectToRedis()
	if err != nil {
		return err
	}
	defer client.Close()

	_, err = client.Ping(context.Background()).Result()
	return err
}

// Add the string to the sorted set in Redis
func (s *RedisIncSet) Add(str string, timestamp int64) error {
	client, err := s.connectToRedis()
	if err != nil {
		return err
	}
	defer client.Close()

	if err := client.ZAdd(context.Background(), s.Name, &redis.Z{Score: float64(timestamp), Member: str}).Err(); err != nil {
		return err
	}

	return nil
}

func (s *RedisIncSet) FetchTheOnesAreDue(timestamp int64) ([]string, error) {
	client, err := s.connectToRedis()
	if err != nil {
		return nil, err
	}
	defer client.Close()

	// Convert timestamp to string representation
	maxScore := strconv.FormatInt(timestamp, 10)

	// Fetch items whose scheduled time is due
	items, err := client.ZRangeByScore(context.Background(), s.Name, &redis.ZRangeBy{
		Min: "0",
		Max: maxScore,
	}).Result()
	return items, err
}
