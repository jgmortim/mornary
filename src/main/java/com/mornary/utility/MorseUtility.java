package com.mornary.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mornary.model.Encoding;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Utility class for dealing with Morse Code.
 *
 * @author John Mortimore
 */
public class MorseUtility {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String MORSE_CODE_LETTER_DELIMITER = " ";
    private static final String MORSE_CODE_WORD_DELIMITER = "/";

    private static final Map<Character, String> ENCODING_MAP = new HashMap<>();

    static {
        URL morseUrl = MorseUtility.class.getResource("/morsecode.json");
        assert morseUrl != null;
        try (InputStream in = morseUrl.openStream()) {
            Arrays.stream(OBJECT_MAPPER.readValue(in, Encoding[].class)).
                forEach(encoding -> ENCODING_MAP.put(encoding.getCharacter(), encoding.getCode()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the given text to Morse code.
     *
     * @param text The text to convert.
     * @return The encoded text.
     */
    public static String toMorseCode(String text) {
        StringJoiner morseCode = new StringJoiner(MORSE_CODE_LETTER_DELIMITER);
        final String upperCase = text.toUpperCase(Locale.US);
        for (int i = 0; i < upperCase.length(); i++) {
            char c = upperCase.charAt(i);
            if (ENCODING_MAP.containsKey(c)) {
                morseCode.add(ENCODING_MAP.get(c));
            } else if (' ' == c) {
                morseCode.add(MORSE_CODE_WORD_DELIMITER);
            } else {
                morseCode.add(String.valueOf(c));
            }
        }

        return morseCode.toString();
    }
}
