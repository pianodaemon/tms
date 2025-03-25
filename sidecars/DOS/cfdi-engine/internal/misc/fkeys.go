package misc

import (
	"context"
	"encoding/json"
	"fmt"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/service/s3"
)

type FKeys struct {
	FPlugin    string `json:"F-PLUGIN"`
	FApiKey    string `json:"F-Api-Key"`
	FSecretKey string `json:"F-Secret-Key"`
}

const (
	basePath = "fiscal-engine/headers"
	fileName = "fkeys.json"
)

func GetFKeys(bucket, owner string) (*FKeys, error) {
	// Load AWS SDK configuration
	cfg, err := config.LoadDefaultConfig(context.TODO())
	if err != nil {
		return nil, fmt.Errorf("unable to load SDK config: %v", err)
	}

	// Initialize S3 client
	svc := s3.NewFromConfig(cfg)

	// Construct the full key
	fullKey := fmt.Sprintf("%s/%s/%s", owner, basePath, fileName)

	// Retrieve the object from S3
	result, err := svc.GetObject(context.TODO(), &s3.GetObjectInput{
		Bucket: aws.String(bucket),
		Key:    aws.String(fullKey),
	})
	if err != nil {
		return nil, fmt.Errorf("failed to retrieve file from S3: %v", err)
	}
	defer result.Body.Close()

	// Decode the JSON data
	var fkeys FKeys
	if err := json.NewDecoder(result.Body).Decode(&fkeys); err != nil {
		return nil, fmt.Errorf("failed to decode JSON: %v", err)
	}

	return &fkeys, nil
}
