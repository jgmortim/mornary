package com.github.jgmortim.mornary.model;

/**
 * Represents a completed work unit and were it fits in the overall sequence of work units.
 *
 * @param index Index of this work unit.
 * @param value The result of the work unit.
 * @author John Mortimore
 */
public record IndexedResult(int index, String value) {
}
