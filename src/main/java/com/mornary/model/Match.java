package com.mornary.model;

/**
 * Represents a matching text segment from a Morse dictionary and the score of the text segment.
 *
 * @param entry The matching text segment from a dictionary.
 * @param score The score of the match.
 *
 * @author John Mortimore
 */
public record Match(TextSegment entry, double score) {
}
