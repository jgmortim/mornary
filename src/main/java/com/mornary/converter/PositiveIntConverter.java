package com.mornary.converter;

import picocli.CommandLine.ITypeConverter;

/**
 * Command line argument parser for arguments that must be a positive non-zero integer.
 *
 * @author John Mortimore
 */
public class PositiveIntConverter implements ITypeConverter<Integer> {
    @Override
    public Integer convert(String value) {

        try {
            int v = Integer.parseInt(value);
            if (v <= 0) {
                throw new IllegalArgumentException("Must be greater than 0");
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Must be a positive integer");
        }
    }
}
