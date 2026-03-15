package com.mornary.model;

import lombok.Getter;

import java.util.Arrays;

/**
 * A single work unit.
 *
 * @author John Mortimore
 */
@Getter
public class WorkUnit {

    /**
     * An array of data. Regardless of the size fo the array, the data will be of length {@link #length}.
     */
    byte[] data;

    /**
     * The length of the data in {@link #data}
     */
    int length;

    /**
     * The index of this work unit in the overarching operation. Work units are processed asynchronously, the index is needed to
     * reassemble the outputs in the right order.
     */
    int index;

    /**
     * Constructs a new work unit.
     *
     * @param data   An array of data to be processed by the work unit.
     * @param length The length of the data in the array, regardless of teh size of the array.
     * @param index  The index of this work unit in the overarching operation. Work units are processed asynchronously, the index
     *               is needed to reassemble the outputs in the right order.
     */
    public WorkUnit(byte[] data, int length, int index) {
        this.data = Arrays.copyOf(data, length);
        this.index = index; // Keeps track of where this work unit fits in the sequence.
        this.length = length;
    }

}
