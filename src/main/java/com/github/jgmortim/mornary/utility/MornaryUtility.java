package com.github.jgmortim.mornary.utility;

import com.github.jgmortim.mornary.model.BinaryTree;
import com.github.jgmortim.mornary.model.Node;
import com.github.jgmortim.mornary.exception.InvalidBinaryException;

import java.util.StringJoiner;

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

    /**
     * Converts a binary string to valid Morse code based on the provided binary tree.
     *
     * @param binary The binary string to convert.
     * @param tree The binary tree representing the Morse code character encoding.
     * @return A valid string of Morse code.
     */
    public static String binaryToDotsAndDashes(String binary, BinaryTree tree) {
        if (!binary.matches("^[0-1]+$")) {
            throw new InvalidBinaryException(binary);
        }

        String dotsAndDashes = binary
                .replace('0', '.')
                .replace('1', '-');

        int index = 0;
        StringJoiner output = new StringJoiner(" ");
        while (index < dotsAndDashes.length()) {
            Node node = null;
            int length = 0;
            int maxLength = Math.min(dotsAndDashes.length() - index, tree.getMaxDepth());
            while (node == null || node.getEncoding() == null) {

                // Randomly select a character length for a morse encoded character
                length = Math.min(maxLength, 1 + (int) (Math.random() * tree.getMaxDepth()));

                // Check if the sequence of that length, starting at the current index, is a valid character
                node = tree.get(dotsAndDashes.substring(index, index + length));
            }
            output.add(node.getEncoding().getCode());
            index += length;
        }

        return output.toString();
    }

}
