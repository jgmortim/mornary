package com.mornary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Match {
    MorseDictionaryEntry entry;
    double score;
}
