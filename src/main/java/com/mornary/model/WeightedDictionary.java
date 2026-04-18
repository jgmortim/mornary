package com.mornary.model;

import lombok.Getter;

/**
 * A dictionary and the weight multiplier to apply to the score of matching words found in the dictionary.
 *
 * @author John Mortimore
 */
@Getter
public class WeightedDictionary {


    private final String filename;
    private final double scoreMultiplier;
    private final int maxMatchesForDictionary;

    /**
     * Constructs a new weighted dictionary.
     *
     * @param filename                The name of the file containing the raw dictionary.
     * @param maxMatchesForDictionary Max number of matches to find while searching the dictionary
     * @param scoreMultiplier         The multiplier to apply to the score of words found in this dictionary.
     */
    public WeightedDictionary(String filename, int maxMatchesForDictionary, double scoreMultiplier) {
        this.filename = filename;
        this.maxMatchesForDictionary = maxMatchesForDictionary;
        this.scoreMultiplier = scoreMultiplier;
    }
}
