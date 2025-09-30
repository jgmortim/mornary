package com.github.jgmortim.mornary.exception;

/**
 * Exception to throw when attempting to process a Morse code input that contains characters other than dots, dashes,
 * and spaces.
 *
 * @author John Mortimore
 */
public class InvalidMorseCodeException extends RuntimeException {

    /**
     * Constructs a new InvalidMorseCodeException with the specified string included in the error message.
     * The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
     *
     * @param morse the invalid Morse string to include in the detail message. The detail message is saved for
     *              later retrieval by the {@link #getMessage()} method.
     */
    public InvalidMorseCodeException(String morse) {
        super("The provided string " + morse + " is not valid morse code");
    }
}
