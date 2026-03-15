package com.mornary.model;

/**
 * Represents a matching word from a Morse dictionary and the score of the word.
 *
 * @param entry The matching entry from a dictionary.
 * @param score The score of the match.
 *
 * @author John Mortimore
 */
public record Match(MorseDictionaryEntry entry, double score) {
}
