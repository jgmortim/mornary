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
     * The English word.
     */
    String english;

    /**
     * The word in Morse code with spaces for letter breaks.
     */
    String morse;

    /**
     * The word in Morse code with letter and word breaks removed.
     */
    String morseNoBreaks;
}
