package com.mornary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a matching word from a Morse dictionary and the score of the word.
 *
 * @author John Mortimore
 */
@AllArgsConstructor
@Getter
public class Match {
    MorseDictionaryEntry entry;
    double score;
}
