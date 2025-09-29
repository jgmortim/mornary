package com.github.jgmortim;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.StringJoiner;


public class Main {

    private static final String PATH = "\\resources\\morsecode.json";


    public static String asciiToBinary(String asciiString) {

        byte[] bytes = asciiString.getBytes();
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

    public static String binaryToDotsAndDashes(String binary, BinaryTree tree) {
        String dotsAndDashes = binary
                .replace('0', '.')
                .replace('1', '-');

        int index = 0;
        StringJoiner output = new StringJoiner(" ");
        while (index < dotsAndDashes.length()) {
            Node node = null;
            int length = 0;
            int maxLength = Math.min(dotsAndDashes.length() - index, BinaryTree.MAX_DEPTH);
            while (node == null || node.characterEncoding == null) {

                // Randomly select a character length for a morse encoded character
                length = Math.min(maxLength, 1 + (int) (Math.random() * BinaryTree.MAX_DEPTH));

                // Check if the sequence of that length, starting at the current index, is a valid character
                node = tree.get(dotsAndDashes.substring(index, index + length));
            }
            output.add(node.characterEncoding.encoding);
            index += length;
        }

        return output.toString();
    }


    private static BinaryTree loadTree() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CharacterEncoding[] morse = mapper.readValue(new File(PATH), CharacterEncoding[].class);

        //        EnumSet.allOf(Morse.class)
        //           .forEach(i -> System.out.println(tree.get(i.getCode()).character + " : " + i.getCode()));

        return new BinaryTree(morse);
    }

    public static void main(String[] args) throws IOException {
        String input = "Hello World";

        String binary = asciiToBinary(input);

        BinaryTree tree = loadTree();

        String output = binaryToDotsAndDashes(binary, tree);

        System.out.println(output);

    }
}