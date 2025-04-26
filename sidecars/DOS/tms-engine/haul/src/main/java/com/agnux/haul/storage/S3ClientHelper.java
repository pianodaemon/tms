package com.agnux.haul.storage;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import com.agnux.haul.errors.TmsException;
import com.agnux.haul.errors.ErrorCodes;

import java.util.Optional;

/**
 * Utility class for setting up an Amazon S3 client. Provides methods to
 * configure and initialize the S3 client based on environment variables or
 * default credentials.
 */
class S3ClientHelper {

    /**
     * Sets up an Amazon S3 client.
     *
     * @param seekout A flag indicating whether to explicitly configure the
     * client using environment variables. If {@code false}, the default AWS
     * credentials provider will be used.
     * @return An instance of {@link S3Client} configured with the appropriate
     * credentials and region.
     * @throws TmsException If required environment variables (AWS_REGION,
     * AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY) are missing or invalid when
     * {@code seekout} is {@code true}.
     */
    public static S3Client setupWithEnv(boolean seekout) throws TmsException {
        if (!seekout) {
            // Use the default credentials provider
            return S3Client.builder()
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }

        // Retrieve credentials and region from environment variables
        Optional<String> region = Optional.ofNullable(System.getenv("AWS_REGION"));
        Optional<String> key = Optional.ofNullable(System.getenv("AWS_ACCESS_KEY_ID"));
        Optional<String> secret = Optional.ofNullable(System.getenv("AWS_SECRET_ACCESS_KEY"));

        // Validate and create AWS credentials
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(key.orElseThrow(() -> new TmsException("AWS key was not provided", ErrorCodes.STORAGE_PROVIDEER_ISSUES)),
                secret.orElseThrow(() -> new TmsException("AWS secret was not provided", ErrorCodes.STORAGE_PROVIDEER_ISSUES))
        );

        // Configure and build the S3 client
        return S3Client.builder()
                .region(Region.of(region.orElseThrow(() -> new TmsException("AWS region was not provided", ErrorCodes.STORAGE_PROVIDEER_ISSUES))))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     * Throws an {@link IllegalStateException} if instantiation is attempted.
     */
    private S3ClientHelper() {
        throw new IllegalStateException("Helper class");
    }
}