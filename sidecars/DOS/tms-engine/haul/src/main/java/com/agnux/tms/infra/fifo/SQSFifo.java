package com.agnux.tms.infra.fifo;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

/**
 * SQSFifo is an implementation of the {@link IFifo} interface that interacts
 * with Amazon SQS FIFO queues. It provides a mechanism for sending messages to
 * a FIFO queue with support for message grouping and deduplication.
 */
@Log4j2
@AllArgsConstructor
public class SQSFifo implements IFifo {

    // The SQS client used to interact with the SQS service
    private final SqsClient sqsClient;

    // The optional target queue URL (if not provided, an exception will be thrown)
    private final Optional<String> target;

    /**
     * Constructs an SQSFifo instance with a provided SQS client and target
     * queue URL.
     *
     * @param sqsClient The Amazon SQS client for communication with the
     * service.
     * @param target The target queue URL for the FIFO queue.
     */
    public SQSFifo(SqsClient sqsClient, final String target) {
        this(sqsClient, Optional.ofNullable(target));
    }

    /**
     * Pushes a message to the FIFO queue. This is the main entry point for
     * sending messages. The method supports optional additional parameters for
     * specifying the message group ID and deduplication ID.
     *
     * @param msg The message to be sent to the FIFO queue.
     * @param extraParams Optional additional parameters for message group and
     * deduplication IDs.
     * @return The message ID returned by SQS after the message is successfully
     * sent.
     * @throws IllegalArgumentException If the message body is null or empty.
     */
    @Override
    public String push(String msg, Object... extraParams) {
        // Ensure the message is not null or empty
        if (msg == null || msg.isEmpty()) {
            throw new IllegalArgumentException("Message body cannot be null or empty");
        }

        // Default group ID and deduplication ID if not provided
        String messageGroupId = "defaultGroupId"; // Customizable logic for group ID
        String messageDeduplicationId = String.valueOf(System.nanoTime()); // Deduplication ID logic

        // Check if additional parameters (messageGroupId, messageDeduplicationId) are provided
        if (extraParams.length > 0 && extraParams[0] instanceof String) {
            messageGroupId = (String) extraParams[0];
        }
        if (extraParams.length > 1 && extraParams[1] instanceof String) {
            messageDeduplicationId = (String) extraParams[1];
        }

        // Send the message and get the response
        SendMessageResponse response = pushMessage(msg, messageGroupId, messageDeduplicationId);

        // Return the message ID as the response
        return response.messageId();
    }

    /**
     * Pushes the message to the FIFO queue with a specified message group ID
     * and deduplication ID. This method handles the construction of the message
     * request and interacts with the SQS client.
     *
     * @param messageBody The message content to be sent to the FIFO queue.
     * @param messageGroupId The group ID used for FIFO message ordering.
     * @param messageDeduplicationId The deduplication ID used for ensuring
     * message uniqueness.
     * @return The response from SQS after sending the message.
     * @throws RuntimeException If an error occurs while sending the message to
     * the SQS queue.
     */
    private SendMessageResponse pushMessage(String messageBody, String messageGroupId, String messageDeduplicationId) {
        try {
            // Build the SendMessageRequest with the required parameters
            SendMessageRequest sendRequest = SendMessageRequest.builder()
                    .queueUrl(target.orElseThrow(() -> new IllegalStateException("Queue URL is missing.")))
                    .messageBody(messageBody)
                    .messageDeduplicationId(messageDeduplicationId)
                    .messageGroupId(messageGroupId)
                    .build();

            // Send the message using the SQS client
            return sqsClient.sendMessage(sendRequest);
        } catch (AwsServiceException | SdkClientException e) {
            // Log the error and throw a runtime exception
            log.error("Failed to send message to SQS FIFO queue", e);
            throw new RuntimeException("Error sending message to SQS FIFO queue", e);
        }
    }
}
