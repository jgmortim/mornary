package com.github.jgmortim.mornary.utility;

import com.github.jgmortim.mornary.exception.InvalidBinaryException;

import java.util.Arrays;

/**
 * Utility class for operations involving ASCII.
 *
 * @author John Mortimore
 */
public final class AsciiUtility {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private AsciiUtility() {
    }

    /**
     * Converts a String to binary ASCII code.
     *
     * @param text The string to convert to binary.
     * @return The ASCII binary representation of the original string.
     */
    public static String toAsciiBinary(String text) {
        byte[] bytes = text.getBytes();
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes) {
            int val = b;
            for (int i = 0; i < 8; i++) {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
        }
        return binary.toString();
    }

    /**
     * Converts a binary String to ASCII text.
     *
     * @param binary The binary String to convert.
     * @return The equivalent ASCII String.
     */
    public static String toAsciiText(String binary) {
        if (binary.isEmpty()) {
            return "";
        } else if ((!binary.matches("^[0-1]+$"))) {
            throw new InvalidBinaryException(binary);
        }

        StringBuilder ascii = new StringBuilder();

        Arrays.stream(binary.split("(?<=\\G.{8})")) // Splits the input string into 8-char-sections (Since a char has 8 bits = 1 byte)
                .forEach(s -> ascii.append((char) Integer.parseInt(s, 2)));

        return ascii.toString();
    }

    public static boolean isAsciiText(byte[] data) {
        for (byte b : data) {
            int value = b & 0xFF; // convert to unsigned
            if (value < 0x20 || value > 0x7E) {
                // allow tab, LF, CR
                if (value != 0x09 && value != 0x0A && value != 0x0D) {
                    return false; // non-ASCII byte found
                }
            }
        }
        return true;
    }

}
