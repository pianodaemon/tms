package com.agnux.tms.storage;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import com.agnux.tms.errors.TmsException;
import com.agnux.tms.errors.ErrorCodes;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

/**
 * S3BucketStorage provides an implementation of the {@link IStorage} interface
 * for interacting with an AWS S3 bucket. It handles file upload, download, and
 * URI generation for objects stored in S3. This class supports handling both
 * success and failure scenarios while interacting with the S3 service.
 */
@Log4j2
@AllArgsConstructor
public class S3BucketStorage implements IStorage {

    // The Amazon S3 client used to interact with the S3 service
    private final S3Client s3Client;

    // The optional S3 bucket name (if not provided, an exception will be thrown when interacting with S3)
    private final Optional<String> target;

    /**
     * Constructs an instance of S3BucketStorage with a provided S3 client and
     * target bucket name.
     *
     * @param s3Client The Amazon S3 client for interacting with the service.
     * @param target The name of the S3 bucket.
     */
    public S3BucketStorage(S3Client s3Client, final String target) {
        this(s3Client, Optional.ofNullable(target));
    }

    /**
     * Uploads a file to the S3 bucket. This method sends the provided input
     * stream as an object in the specified S3 bucket under the provided file
     * name. It includes the content type and length of the file.
     *
     * @param cType The content type of the file being uploaded.
     * @param len The length (size) of the file in bytes.
     * @param fileName The name to assign to the file in S3.
     * @param inputStream The input stream containing the file data.
     * @throws TmsException If an error occurs during the upload process.
     */
    @Override
    public void upload(
            final String cType,
            final long len,
            final String fileName,
            InputStream inputStream) throws TmsException {

        try {
            // Upload the file to the specified S3 bucket
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(getTargetName()) // Target bucket name
                            .key(fileName) // File name in the bucket
                            .contentType(cType) // File content type
                            .contentLength(len) // File size
                            .build(),
                    RequestBody.fromInputStream(inputStream, len) // The file data
            );
        } catch (S3Exception ex) {
            // Log the error and throw an TmsException if the upload fails
            log.error(String.format("File %s cannot be uploaded: %s", fileName, ex.awsErrorDetails().errorMessage()));
            throw new TmsException("A failure occurred when attempting to write to the bucket storage",
                    ex, ErrorCodes.STORAGE_PROVIDEER_ISSUES);
        }
    }

    /**
     * Downloads a file from the S3 bucket. The file is retrieved as a byte
     * array, wrapped in a buffered input stream for further use (e.g., reading
     * the file's contents).
     *
     * @param fileName The name of the file to be downloaded from the S3 bucket.
     * @return A BufferedInputStream containing the file's data.
     * @throws TmsException If an error occurs during the download
     * process.
     */
    @Override
    public BufferedInputStream download(final String fileName) throws TmsException {
        try {
            // Get the file as a byte array from S3
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(getTargetName()) // Target bucket name
                            .key(fileName) // File name in the bucket
                            .build()
            );
            // Return the file data wrapped in a buffered input stream
            return new BufferedInputStream(new ByteArrayInputStream(response.asByteArray()));
        } catch (S3Exception ex) {
            // Log the error and throw an TmsException if the download fails
            log.error(String.format("Failed to download file %s: %s", fileName, ex.awsErrorDetails().errorMessage()));
            throw new TmsException("Failed to download file from bucket storage",
                    ex, ErrorCodes.STORAGE_PROVIDEER_ISSUES);
        }
    }

    /**
     * Retrieves the name of the target S3 bucket. If no bucket name is
     * provided, this method throws an exception.
     *
     * @return The name of the S3 bucket.
     * @throws TmsException If the bucket name is missing.
     */
    @Override
    public String getTargetName() throws TmsException {
        // Ensure the target bucket name is provided
        return target.orElseThrow(() -> new TmsException("AWS bucket was not provided", ErrorCodes.STORAGE_PROVIDEER_ISSUES));
    }

    /**
     * Generates the S3 URI for a file stored in the target bucket. The URI
     * follows the format: "s3://<bucket-name>/<file-name>".
     *
     * @param fileName The name of the file in the S3 bucket.
     * @return The S3 URI for the specified file.
     * @throws TmsException If the file name is null, empty, or invalid.
     */
    @Override
    public String getUri(final String fileName) throws TmsException {
        // Get the bucket name
        final String bucketName = getTargetName();

        // Validate the file name
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new TmsException("File name cannot be null or empty", ErrorCodes.STORAGE_PROVIDEER_ISSUES);
        }

        // Return the S3 URI for the file
        return String.format("s3://%s/%s", bucketName, fileName);
    }
}