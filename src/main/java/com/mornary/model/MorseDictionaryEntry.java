package com.mornary.model;

import lombok.Getter;

/**
 * Represents an English word, its Morse encoding, and the corresponding Mornary bit pattern.
 *
 * @author John Mortimore
 */
@Getter
public final class MorseDictionaryEntry {

    /**
     * The word in English.
     */
    private final String english;

    /**
     * The word in standard Morse code.
     */
    private final String morse;

    /**
     * The word in standard Morse code with letter and word breaks removed.
     */
    private final String morsePattern;

    /**
     * The bit pattern equivalent to {@link #morsePattern}. 0s for dots, 1s for dashes. Stored as a long.
     */
    private final long bitPattern;

    /**
     * The length of the {@link #bitPattern}.
     */
    private final int bitLength;

    /**
     * The number of letters in the {@link #english} word.
     */
    private final int numberOfLetters;

    private final double scoreMultiplier;

    /**
     * Constructs a new Morse dictionary entry.
     *
     * @param english         The English word.
     * @param morse           The word in Morse code with spaces for letter breaks.
     * @param scoreMultiplier Score multiplier for this word
     */
    public MorseDictionaryEntry(String english, String morse, double scoreMultiplier) {
        this.english = english;
        this.morse = morse;
        this.scoreMultiplier = scoreMultiplier;

        this.numberOfLetters = english.replaceAll(" ", "").length();

        this.morsePattern = morse.replace(" ", "").replace("/", "");

        long bits = 0;
        int length = 0;

        for (char c : morsePattern.toCharArray()) {
            bits <<= 1;
            if (c == '-') bits |= 1;
            length++;
        }

        this.bitPattern = bits;
        this.bitLength = length;
    }

}
