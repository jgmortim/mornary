package com.mornary.model;

import lombok.Getter;

/**
 * Represents an English word, its Morse encoding, and the corresponding Mornary bit pattern.
 *
 * @author John Mortimore
 */
@Getter
public final class MorseDictionaryEntry {
    private final String english;
    private final String morse;
    private final long bitPattern;
    private final int bitLength;
    private final int numberOfLetters;

    /**
     * Constructs a new Morse dictionary entry.
     *
     * @param english The English word.
     * @param morse   The word in Morse code with spaces for letter breaks.
     */
    public MorseDictionaryEntry(String english, String morse) {
        this.english = english;
        this.morse = morse;

        this.numberOfLetters = english.replaceAll(" ", "").length();

        final String morseNoBreaks = morse.replace(" ", "").replace("/", "");

        long bits = 0;
        int length = 0;

        for (char c : morseNoBreaks.toCharArray()) {
            bits <<= 1;
            if (c == '-') bits |= 1;
            length++;
        }

        this.bitPattern = bits;
        this.bitLength = length;
    }

}
