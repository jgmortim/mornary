package com.github.jgmortim.mornary.utility;

import com.github.jgmortim.mornary.exception.InvalidBinaryException;

import java.util.Arrays;

/**
 * Utility class for Mornary.
 *
 * @author John Mortimore
 */
public final class MornaryUtility {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private MornaryUtility() {
    }

    /**
     * Converts a String to binary ASCII code.
     *
     * @param string The string to convert to binary.
     * @return The ASCII binary representation of the original string.
     */
    public static String toAsciiBinary(String string) {
        byte[] bytes = string.getBytes();
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
     * Converts a binary String to ASCII.
     *
     * @param binary The binary String to convert.
     * @return The equivalent ASCII String.
     */
    public static String toAscii(String binary) {
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

}
