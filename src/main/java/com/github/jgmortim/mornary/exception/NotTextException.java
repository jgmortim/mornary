package com.github.jgmortim.mornary.exception;

/**
 * Exception to throw when an output file has not been specified, but the output data is not text and can't be
 * printed to the console.
 *
 * @author John Mortimore
 */
public class NotTextException extends RuntimeException {

    /**
     * Constructs a new NotTextException with the specified string included in the error message.
     * The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
     */
    public NotTextException() {
        super("No output file specified, but decoded data is not ASCII text and cannot be printed to the console."
                + "\nPlease specify an output file and try again.");
    }
}
