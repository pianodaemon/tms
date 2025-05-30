package com.agnux.tms.errors;

import lombok.Getter;

/**
 * Enum representing the different error codes used within the SPEI system.
 * <p>
 * This enum provides a set of predefined error codes that are used to
 * categorize various types of errors within the system. Each error code is
 * associated with a unique integer value. These error codes help to identify
 * specific issues, making error handling and debugging easier.
 * </p>
 */
@Getter
public enum ErrorCodes {

    /**
     * Success code indicating that the operation completed without issues.
     */
    SUCCESS(0),
    /**
     * Error code indicating an unknown issue. This is a generic error code used
     * when an unexpected or unclassified error occurs.
     */
    UNKNOWN_ISSUE(1000),
    /**
     * Error code indicating that a lack of data integrity has been spotted.
     */
    LACK_OF_DATA_INTEGRITY(1001),
    /**
     * Error code indicating that a required configuration element was not
     * found. This error typically occurs when an expected environment variable
     * or configuration setting is missing.
     */
    CONFIG_ELEMENT_NOT_FOUND(1002),
    /**
     * Error code indicating issues when interacting with the repository provide
     * entity. This error occurs when there are problems with operations such as
     * saving or fetching data.
     */
    REPO_PROVIDER_ISSUES(1003), // Lack of interaction with storage provider entity

    REPO_PROVIDER_NONPRESENT_DATA(1004),

    /**
     * Error code indicating issues when interacting with the storage provider
     * entity. This error occurs when there are problems with storage-related
     * operations, such as uploading or downloading data.
     */
    STORAGE_PROVIDER_ISSUES(1005), // Lack of interaction with storage provider entity

    /**
     * Error code indicating issues when interacting with the FIFO provider
     * entity. This error occurs when there are problems related to pushing or
     * pulling data from the FIFO queue.
     */
    FIFO_PROVIDER_ISSUES(1006), // Lack of interaction with FIFO provider entity

    LACK_OF_PERMISSIONS(1007),

    INVALID_DATA(1008);

    private final int code;

    /**
     * Constructor for creating an error code with a specific integer value.
     *
     * @param code The integer code associated with the error.
     */
    ErrorCodes(final int code) {
        this.code = code;
    }
}
