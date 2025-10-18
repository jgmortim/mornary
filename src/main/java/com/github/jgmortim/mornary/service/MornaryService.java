package com.github.jgmortim.mornary.service;

import com.epic.morse.service.MorseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jgmortim.mornary.Method;
import com.github.jgmortim.mornary.exception.InvalidBinaryException;
import com.github.jgmortim.mornary.model.BinaryTree;
import com.github.jgmortim.mornary.model.Encoding;
import com.github.jgmortim.mornary.model.Node;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for Mornary.
 *
 * @author John Mortimore
 */
public class MornaryService {

    private final BinaryTree tree;

    private final List<String> morseDictionary;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Constructs the MornaryService.
     */
    public MornaryService() throws IOException, URISyntaxException {
        URL morseUrl = getClass().getResource("/morsecode.json");

        Encoding[] encodings = OBJECT_MAPPER.readValue(morseUrl, Encoding[].class);

        this.tree = new BinaryTree(encodings);

        Set<String> words = Sets.newHashSet(Files.readLines(new File(getClass().getResource("/5000-more-common.txt").toURI().getPath()), Charset.defaultCharset()));

        this.morseDictionary = words.stream()
                .map(MorseCode::convertToMorseCode)
                .collect(Collectors.toList());

    }

    /**
     * Converts a binary string to valid Morse code.
     *
     * @param binary The binary string to convert.
     * @return A valid string of Morse code.
     */
    public String binaryToMorseCode(String binary, Method method) {
        if (!binary.matches("^[0-1]+$")) {
            throw new InvalidBinaryException(binary);
        }

        return switch (method) {
            case smart -> smartEncode(binary);
            case dumb -> dumbEncode(binary);
        };
    }


    private String dumbEncode(String binary) {
        String morseWithoutSpaces = binary
                .replace('0', '.')
                .replace('1', '-');

        int index = 0;
        StringJoiner output = new StringJoiner(" ");
        while (index < morseWithoutSpaces.length()) {
            String letter = this.findLetter(morseWithoutSpaces.substring(index));

            output.add(letter);
            index += letter.length();
        }

        return output.toString();
    }



    private String smartEncode(String binary) {
        String input = binary
                .replace('0', '.')
                .replace('1', '-');

        int index = 0;
        StringJoiner morseWords = new StringJoiner(" / ");

        // Loop until the entire input has been consumed
        while (index < input.length()) {

            String word = findWord(input.substring(index));
            morseWords.add(word);
            index += word.replace(" ", "").length();
        }

        return morseWords.toString();
    }



    /**
     * Finds a word in morse code that matches the start of (or the entire) input.
     * <p>
     * For example, if the input started with ".--", that could match the word "at" (which is ".- -" in morse).
     * In which case, the response would be ".- -".
     *
     * @param input A string consisting of only dots and dashes.
     * @return A word that matches the start of the input, but with spaces added at letter breaks.
     */
    private String findWord(String input) {
        // Pick a random index in the morse dictionary to start looking for words that match.
        Random randomGen = new Random();
        int random = randomGen.nextInt(this.morseDictionary.size());
        int wordsChecked = 0;
        String selectedWord = "";

        List<String> matchingWords = new ArrayList<>();


        // Loop until a match is selected
        while (selectedWord.isEmpty()) {
            // If the dictionary has been exhausted, or we've reached the desired number of matches:
            if (wordsChecked >= this.morseDictionary.size() || matchingWords.size() >= 10) {
                // And no matches were found, find a matching letter and move on.
                if (matchingWords.isEmpty()) {
                    selectedWord = this.findLetter(input);
                } else { // Otherwise, take the longest of matches.
                    selectedWord = matchingWords.stream()
                            .max(Comparator.comparingInt(String::length))
                            .get();
                }
            // Otherwise, keep checking words from the dictionary
            } else {
                String word = this.morseDictionary.get(random);
                String wordWithoutSpaces = word.replace(" ", "");

                if (input.startsWith(wordWithoutSpaces)) {
                    matchingWords.add(word);
                }

                wordsChecked++;
                random = (random + 1) % this.morseDictionary.size();
            }
        }
        return selectedWord;
    }

    /**
     * Randomly finds a single letter in morse code that matches the start of (or the entire) input.
     * <p>
     * For example, if the input was ".--", that could match the letters or E (.), A (.-), or W (.--).
     *
     * @param input A string consisting of only dots and dashes.
     * @return A randomly selected letter that matches the start of the input.
     */
    private String findLetter(String input) {
        Node node = null;
        int maxLength = Math.min(input.length(), this.tree.getMaxDepth());

        // Loop until a valid morse encoding is randomly selected
        while (node == null || node.getEncoding() == null) {

            // Randomly select a character length for a morse encoded character.
            int length = Math.min(maxLength, 1 + (int) (Math.random() * this.tree.getMaxDepth()));

            // Starting at the current index, Check if the substring with that length is a valid encoding.
            node = this.tree.get(input.substring(0, length));
        }

        return node.getEncoding().getCode();
    }

    /**
     * Converts a string of morse code into binary.
     * Dots become zeros, dashes become ones, spaces and forward slashes are removed.
     *
     * @param morse The morse code string.
     * @return The matching binary string.
     */
    public String morseCodeToBinary(String morse) {
        return morse
                .replace('.', '0')
                .replace('-', '1')
                .replace(" ", "")
                .replace("/", "");
    }

}
