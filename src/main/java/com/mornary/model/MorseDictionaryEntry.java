package com.mornary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents an English word encoded in Morse.
 *
 * @author John Mortimore
 */
@Getter
@AllArgsConstructor
public class MorseDictionaryEntry {
    /**
     * The word in Morse code with spaces for letter breaks.
     */
    String word;

    /**
     * The word in Morse code with letter breaks removed.
     */
    String wordWithoutLetterBreaks;
}
