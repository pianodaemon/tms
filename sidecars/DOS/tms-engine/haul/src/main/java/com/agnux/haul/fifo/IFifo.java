package com.agnux.haul.fifo;

/**
 * The IFifo interface defines the contract for a First-In-First-Out (FIFO)
 * messaging or data handling system. This interface provides a method for
 * pushing messages or data into a FIFO queue, with the option for additional
 * parameters.
 * <p>
 * Implementing classes should define the specifics of how messages are handled
 * in the FIFO queue, including managing the order and storing the data.
 */
public interface IFifo {

    /**
     * Pushes a message into the FIFO queue with optional additional parameters.
     * <p>
     * This method takes a message and any number of extra parameters, pushing
     * the message into the queue while potentially using the extra parameters
     * for custom processing. The method returns a response indicating the
     * success or result of the operation.
     *
     * @param msg The message to be pushed into the FIFO queue.
     * @param extraParams Optional extra parameters for customizing the message
     * handling. These can be used for additional metadata, configurations, or
     * other purposes.
     * @return A response indicating the result of the operation, such as a
     * success message, queue status, or other relevant information.
     */
    public String push(String msg, Object... extraParams);
}
