package com.agnux.tms.errors;

/**
 * Custom exception class representing errors within the TMS management system.
 * <p>
 * This class extends the `Exception` class and includes an error code that
 * corresponds to a specific error type in the system. The error code is
 * provided through the `ErrorCodes` enum, which can be used to categorize
 * different types of errors. This class allows for detailed exception handling
 * with the ability to associate specific error codes and messages to the
 * exception.
 * </p>
 */
final public class TmsException extends Exception {

    /**
     * The error code associated with this exception.
     */
    final int errorCode;

    /**
     * Constructor for creating a new `TmsException` with a specified
     * error message and error code.
     * <p>
     * This constructor allows the exception to carry an error message along
     * with an error code from the `ErrorCodes` enum.
     * </p>
     *
     * @param message The detailed error message explaining the reason for the
     * exception.
     * @param errorCode The error code associated with this exception, from the
     * `ErrorCodes` enum.
     */
    public TmsException(String message, ErrorCodes errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
    }

    /**
     * Constructor for creating a new `TmsException` with a specified
     * error message and a cause.
     * <p>
     * This constructor allows the exception to carry an error message and the
     * underlying cause of the exception. The error code is set to
     * `UNKNOWN_ISSUE` by default.
     * </p>
     *
     * @param message The detailed error message explaining the reason for the
     * exception.
     * @param cause The underlying cause of the exception (another throwable).
     */
    public TmsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCodes.UNKNOWN_ISSUE.getCode();
    }

    /**
     * Constructor for creating a new `TmsException` with a specified
     * error message, cause, and error code.
     * <p>
     * This constructor allows the exception to carry an error message, the
     * underlying cause, and an error code that is used to classify the
     * exception.
     * </p>
     *
     * @param message The detailed error message explaining the reason for the
     * exception.
     * @param cause The underlying cause of the exception (another throwable).
     * @param errorCode The error code associated with this exception, from the
     * `ErrorCodes` enum.
     */
    public TmsException(String message, Throwable cause, ErrorCodes errorCode) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }

    /**
     * Retrieves the error code associated with this exception.
     * <p>
     * This method allows access to the specific error code that categorizes the
     * exception and can be used for error handling and logging.
     * </p>
     *
     * @return The error code associated with this exception.
     */
    public int getErrorCode() {
        return errorCode;
    }
}
