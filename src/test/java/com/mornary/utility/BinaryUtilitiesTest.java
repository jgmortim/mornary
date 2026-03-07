package com.mornary.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link BinaryUtilities}.
 *
 * @author John Mortimore
 */
public class BinaryUtilitiesTest {

    @Test
    void byteArrayToMorseBinaryString_fullLength_correctStringReturned() {
        final byte[] input = {
                84, 104, 101, 32, 113, 117, 105, 99, 107, 32, 98, 114, 111, 119, 110, 32, 102, 111, 120, 32, 106, 117,
                109, 112, 115, 32, 111, 118, 101, 114, 32, 116, 104, 101, 32, 108, 97, 122, 121, 32, 100, 111, 103, 46
        };
        final String expected =
                ".-.-.-...--.-....--..-.-..-......---...-.---.-.-.--.-..-.--...--.--.-.--..-......--...-..---..-." +
                ".--.----.---.---.--.---...-......--..--..--.----.----.....-......--.-.-..---.-.-.--.--.-.---...." +
                ".---..--..-......--.----.---.--..--..-.-.---..-...-......---.-...--.-....--..-.-..-......--.--.." +
                ".--....-.----.-..----..-..-......--..-...--.----.--..---..-.---.";

        final String actual = BinaryUtilities.byteArrayToMorseBinaryString(input, input.length);

        assertEquals(expected, actual);

    }

    @Test
    void byteArrayToMorseBinaryString_partialLength_correctStringReturned() {
        final byte[] input = {
                84, 104, 101, 32, 113, 117, 105, 99, 107, 32, 98, 114, 111, 119, 110, 32, 102, 111, 120, 32, 106, 117,
                109, 112, 115, 32, 111, 118, 101, 114, 32, 116, 104, 101, 32, 108, 97, 122, 121, 32, 100, 111, 103, 46
        };
        final String expected =
                ".-.-.-...--.-....--..-.-..-......---...-.---.-.-.--.-..-.--...--.--.-.--..-......--...-..---..-." +
                ".--.----.---.---.--.---...-......--..--..--.----.----.....-......--.-.-..---.-.-.--.--.-.---....";

        final String actual = BinaryUtilities.byteArrayToMorseBinaryString(input, 24);

        assertEquals(expected, actual);
    }

    @Test
    void binaryStringToByteArray() {
        final String input =
                "010101000110100001100101001000000111000101110101011010010110001101101011001000000110001001110010" +
                "011011110111011101101110001000000110011001101111011110000010000001101010011101010110110101110000" +
                "011100110010000001101111011101100110010101110010001000000111010001101000011001010010000001101100" +
                "0110000101111010011110010010000001100100011011110110011100101110";

        final byte[] expected = {
                84, 104, 101, 32, 113, 117, 105, 99, 107, 32, 98, 114, 111, 119, 110, 32, 102, 111, 120, 32, 106, 117,
                109, 112, 115, 32, 111, 118, 101, 114, 32, 116, 104, 101, 32, 108, 97, 122, 121, 32, 100, 111, 103, 46
        };

        final byte[] actual = BinaryUtilities.binaryStringToByteArray(input);

        assertArrayEquals(expected, actual);
    }
}
