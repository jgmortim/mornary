package com.mornary.model;

/**
 * A dictionary and the weight multiplier to apply to the score of matching words found in the dictionary.
 *
 * @param filename                The name of the file containing the raw dictionary.
 * @param maxMatchesForDictionary Max number of matches to find while searching the dictionary
 * @param scoreMultiplier         The multiplier to apply to the score of words found in this dictionary.
 *
 * @author John Mortimore
 */
public record WeightedDictionary(String filename, int maxMatchesForDictionary, double scoreMultiplier) {
}
