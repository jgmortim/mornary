package com.mornary.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class WorkUnit {

    byte[] data;
    int length;
    int index;

    public WorkUnit(byte[] data, int length, int index) {
        this.data = Arrays.copyOf(data, length);
        this.index = index; // Keeps track of where this work unit fits in the sequence.
        this.length = length;
    }

}
