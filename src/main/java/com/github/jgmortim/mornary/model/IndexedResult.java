package com.github.jgmortim.mornary.model;

import lombok.Getter;

@Getter
public class IndexedResult {
    final int index;
    final String value;

    public IndexedResult(int index, String value) {
        this.index = index;
        this.value = value;
    }
}
