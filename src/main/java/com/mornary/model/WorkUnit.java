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
     * The index of this work unit in the overarching operation. Work units are processed asynchronously, the index is needed to
     * reassemble the outputs in the right order.
     */
    int index;

    /**
     * Bit reader for the work unit.
     */
    BitReader bitReader;

    /**
     * Constructs a new work unit.
     *
     * @param data   An array of data to be processed by the work unit.
     * @param length The length of the data in the array, regardless of the size of the array.
     * @param index  The index of this work unit in the overarching operation. Work units are processed asynchronously, the index
     *               is needed to reassemble the outputs in the right order.
     */
    public WorkUnit(byte[] data, int length, int index) {
        this.index = index;
        this.bitReader = new BitReader(Arrays.copyOf(data, length), length);
    }

}
