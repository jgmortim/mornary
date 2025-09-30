package com.github.jgmortim.mornary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jgmortim.mornary.exception.InvalidBinaryException;
import com.github.jgmortim.mornary.model.BinaryTree;
import com.github.jgmortim.mornary.model.Encoding;
import com.github.jgmortim.mornary.model.Node;

import java.io.IOException;
import java.net.URL;
import java.util.StringJoiner;

/**
 * Service class for Mornary.
 *
 * @author John Mortimore
 */
public class MornaryService {

    private final BinaryTree tree;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Constructs the MornaryService.
     */
    public MornaryService() throws IOException {
        URL url = getClass().getResource("/morsecode.json");

        Encoding[] encodings = OBJECT_MAPPER.readValue(url, Encoding[].class);

        this.tree = new BinaryTree(encodings);
    }

    /**
     * Converts a binary string to valid Morse code.
     *
     * @param binary The binary string to convert.
     * @return A valid string of Morse code.
     */
    public String binaryToMorseCode(String binary) {
        if (!binary.matches("^[0-1]+$")) {
            throw new InvalidBinaryException(binary);
        }

        String morseWithoutSpaces = binary
                .replace('0', '.')
                .replace('1', '-');

        int index = 0;
        StringJoiner output = new StringJoiner(" ");
        while (index < morseWithoutSpaces.length()) {
            Node node = null;
            int length = 0;
            int maxLength = Math.min(morseWithoutSpaces.length() - index, this.tree.getMaxDepth());

            // Loop until a valid morse encoding is randomly selected
            while (node == null || node.getEncoding() == null) {

                // Randomly select a character length for a morse encoded character.
                length = Math.min(maxLength, 1 + (int) (Math.random() * this.tree.getMaxDepth()));

                // Starting at the current index, Check if the substring with that length is a valid encoding.
                node = this.tree.get(morseWithoutSpaces.substring(index, index + length));
            }

            output.add(node.getEncoding().getCode());
            index += length;
        }

        return output.toString();
    }

    public String morseCodeToBinary(String morse) {
        return morse
                .replace('.', '0')
                .replace('-', '1')
                .replace(" ", "");
    }

}
