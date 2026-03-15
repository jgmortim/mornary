package com.mornary.utility;

/**
 * Utility class for operations involving binary.
 *
 * @author John Mortimore
 */
public class BinaryUtilities {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private BinaryUtilities() {
    }

    /**
     * Converts a string of ones and zeros into the equivalent byte array.
     *
     * @param s The binary string.
     * @return The byte array.
     */
    public static byte[] binaryStringToByteArray(String s) {
        if (s.length() % 8 != 0) throw new IllegalArgumentException("Binary data length must be multiple of 8");
        byte[] data = new byte[s.length() / 8];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '1') {
                data[i >> 3] |= 0x80 >> (i & 0x7);
            } else if (c != '0') {
                throw new IllegalArgumentException("Invalid char in binary string: " + c);
            }
        }
        return data;
    }

}
