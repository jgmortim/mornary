package com.mornary.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 *
 * @author John Mortimore
 */
@Getter
public class WeightedDictionary {

   public WeightedDictionary(String filename, int maxMatchesForDictionary, double scoreMultiplier) {
      this.filename = filename;
      this.maxMatchesForDictionary = maxMatchesForDictionary;
      this.scoreMultiplier = scoreMultiplier;
   }

   private final String filename;
   private final double scoreMultiplier;
   private final int maxMatchesForDictionary;

   @Setter
   private List<MorseDictionaryEntry> dictionary;
}
