package com.github.jgmortim.mornary.utility;

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
     * Converts the first "actualSize" byte of a byte array into a binary string.
     *
     * @param data       The byte array.
     * @param actualSize The actual size of the data in the array, regardless of the capacity of the array.
     * @return The binary string.
     */
    public static String byteArrayToBinaryString(byte[] data, int actualSize) {
        StringBuilder binary = new StringBuilder(actualSize * 8);
        for (int i = 0; i < actualSize; i++) {
            byte b = data[i];
            // Convert byte to int and mask with 0xFF to handle negative values correctly
            binary.append(
                    String.format("%8s", Integer.toBinaryString(b & 0xFF))
                            .replace(' ', '0')
                            .replace('0', '.')
                            .replace('1', '-')
            );
        }
        return binary.toString();
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
                throw new IllegalArgumentException("Invalid char in binary string");
            }
        }
        return data;
    }

}
