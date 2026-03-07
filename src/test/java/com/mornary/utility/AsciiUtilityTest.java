package com.mornary.utility;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void toAsciiText_binaryString_empty_success() {
        final String input = "";
        final String expected = "";

        final String actual = AsciiUtility.toAsciiText(input);

        assertEquals(expected, actual);
    }

    @Test
    void toAsciiText_binaryString_letter_success() {
        final String input = "01000001";
        final String expected = "A";

        final String actual = AsciiUtility.toAsciiText(input);

        assertEquals(expected, actual);
    }

    @Test
    void toAsciiText_binaryString_sentence_success() {
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

    @Test
    void toAsciiText_byteArray_empty_success() {
        final byte[] input = {};
        final String expected = "";

        final String actual = AsciiUtility.toAsciiText(input, 0);

        assertEquals(expected, actual);
    }

    @Test
    void toAsciiText_byteArray_letter_success() {
        final byte[] input = {65};
        final String expected = "A";

        final String actual = AsciiUtility.toAsciiText(input, 1);

        assertEquals(expected, actual);
    }

    @Test
    void toAsciiText_byteArray_sentence_success() {
        final byte[] input = {
                84, 104, 101, 32, 113, 117, 105, 99, 107, 32, 98, 114, 111, 119, 110, 32, 102, 111, 120, 32, 106, 117,
                109, 112, 115, 32, 111, 118, 101, 114, 32, 116, 104, 101, 32, 108, 97, 122, 121, 32, 100, 111, 103, 46
        };
        final String expected = "The quick brown fox jumps over the lazy dog.";

        final String actual = AsciiUtility.toAsciiText(input, input.length);

        assertEquals(expected, actual);
    }

    @Test
    void toAsciiText_halfFullByteArray_sentence_success() {
        byte[] input = new byte[24];
        input[0] = 72;
        input[1] = 101;
        input[2] = 108;
        input[3] = 108;
        input[4] = 111;
        input[5] = 32;
        input[6] = 87;
        input[7] = 111;
        input[8] = 114;
        input[9] = 108;
        input[10] = 100;
        input[11] = 33;

        final String expected = "Hello World!";

        final String actual = AsciiUtility.toAsciiText(input, expected.length());

        assertEquals(expected, actual);
    }

    @Test
    void isAsciiText_correctEvaluation() {
        final byte[] helloWorld = "Hello World!".getBytes(StandardCharsets.UTF_8);
        final byte[] controlCharacters = {1, 2, 3, 4};
        final byte[] printingAndNonPrintingMix = {1, 55, 76, 32, 5};


        assertTrue(AsciiUtility.isAsciiText(helloWorld));
        assertFalse(AsciiUtility.isAsciiText(controlCharacters));
        assertFalse(AsciiUtility.isAsciiText(printingAndNonPrintingMix));
    }
}
