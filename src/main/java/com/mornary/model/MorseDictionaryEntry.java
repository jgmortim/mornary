package com.mornary.model;

/**
 * Represents an English word encoded in Morse.
 *
 * @param english       The English word.
 * @param morse         The word in Morse code with spaces for letter breaks.
 * @param morseNoBreaks The word in Morse code with letter and word breaks removed.
 * @author John Mortimore
 */
public record MorseDictionaryEntry(String english, String morse, String morseNoBreaks) {
}
