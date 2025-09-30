package com.github.jgmortim.mornary.exception;

/**
 * Exception to throw when attempting to process a binary input that contains characters other than 0 and 1.
 *
 * @author John Mortimore
 */
public class InvalidBinaryException extends RuntimeException {

    /**
     * Constructs a new InvalidBinaryException with the specified string included in the error message.
     * The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
     *
     * @param binary the invalid binary string to include in the detail message. The detail message is saved for
     *               later retrieval by the {@link #getMessage()} method.
     */
    public InvalidBinaryException(String binary) {
        super("The provided string " + binary + " is not valid binary");
    }
}
