package com.github.jgmortim.mornary.utility;

/**
 * Service class for Mornary.
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

}
