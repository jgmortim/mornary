package com.mornary.model;

import lombok.Getter;

/**
 * Represents a segment of text in English, its Morse encoding, and its corresponding Mornary bit pattern.
 *
 * @author John Mortimore
 */
@Getter
public final class TextSegment {

    /**
     * The text segment in English.
     */
    private final String english;

    /**
     * The text segment in standard Morse code.
     */
    private final String morse;

    /**
     * The text segment in standard Morse code with letter and word breaks removed.
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
     * The number of letters in the {@link #english} text segment. Spaces are not counted.
     */
    private final int numberOfLetters;

    /**
     * The score multiplier for this word.
     */
    private final double scoreMultiplier;

    /**
     * Constructs a new Text Segment.
     *
     * @param english         The text in English.
     * @param morse           The text in Morse code.
     * @param scoreMultiplier Score multiplier for this text segment.
     */
    public TextSegment(String english, String morse, double scoreMultiplier) {
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
