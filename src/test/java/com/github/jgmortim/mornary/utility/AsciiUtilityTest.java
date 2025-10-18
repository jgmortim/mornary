package com.github.jgmortim.mornary.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link AsciiUtility}.
 *
 * @author John Mortimore
 */
public class AsciiUtilityTest {

    @Test
    void toAsciiBinary_empty_success() {
        final String input = "";
        final String expected = "";

        final String actual = AsciiUtility.toAsciiBinary(input);

        assertEquals(expected, actual);
    }

    @Test
    void toAsciiBinary_letter_success() {
        final String input = "A";
        final String expected = "01000001";

        final String actual = AsciiUtility.toAsciiBinary(input);

        assertEquals(expected, actual);
    }

    @Test
    void toAsciiBinary_sentence_success() {
        final String input = "The quick brown fox jumps over the lazy dog.";
        final String expected = """
                01010100011010000110010100100000011100010111010101101001011000110110101100100000\
                01100010011100100110111101110111011011100010000001100110011011110111100000100000\
                01101010011101010110110101110000011100110010000001101111011101100110010101110010\
                00100000011101000110100001100101001000000110110001100001011110100111100100100000\
                01100100011011110110011100101110""";

        final String actual = AsciiUtility.toAsciiBinary(input);

        assertEquals(expected, actual);
    }

    @Test
    void toAsciiText_empty_success() {
        final String input = "";
        final String expected = "";

        final String actual = AsciiUtility.toAsciiText(input);

        assertEquals(expected, actual);
    }

    @Test
    void toAsciiText_letter_success() {
        final String input = "01000001";
        final String expected = "A";

        final String actual = AsciiUtility.toAsciiText(input);

        assertEquals(expected, actual);
    }

    @Test
    void toAsciiText_sentence_success() {
        final String input = """
                01010100011010000110010100100000011100010111010101101001011000110110101100100000\
                01100010011100100110111101110111011011100010000001100110011011110111100000100000\
                01101010011101010110110101110000011100110010000001101111011101100110010101110010\
                00100000011101000110100001100101001000000110110001100001011110100111100100100000\
                01100100011011110110011100101110""";
        final String expected = "The quick brown fox jumps over the lazy dog.";

        final String actual = AsciiUtility.toAsciiText(input);

        assertEquals(expected, actual);
    }


}
