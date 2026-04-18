package com.mornary.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Node object for {@link MorseTrie}.
 *
 * @author John Mortimore
 */
public class MorseTrieNode {

    public MorseTrieNode dot;
    public MorseTrieNode dash;

    /**
     * The entries. Because the trie can have hundreds of thousands of nodes, it important to be as lazy as possible when creating
     * this collection. We start it as null. And then change it to a single {@link MorseDictionaryEntry} when the first entry is
     * encountered. If a second entry is encountered, then we convert it to an array. Most nodes are going to either have 0 or 1
     * entry. So we only want to create arrays when absolutely necessary.
     */
    private Object entries = null;

    /**
     * Constructs a new node.
     */
    public MorseTrieNode() {
    }

    /**
     * Adds an entry to the node.
     *
     * @param entry The entry to add.
     */
    public void addEntry(MorseDictionaryEntry entry) {
        if (entries == null) {
            // If node has no data, just set the data to be the entry.
            entries = entry;
        } else if (entries instanceof MorseDictionaryEntry original) {
            // If the data is a single entry, convert it to an array and add the new entry.
            entries = new MorseDictionaryEntry[]{original, entry};
        } else {
            // If the data is already an array, grow the array and add the new entry.
            MorseDictionaryEntry[] originalArray = (MorseDictionaryEntry[]) entries;
            MorseDictionaryEntry[] newArray = Arrays.copyOf(originalArray, originalArray.length + 1);
            newArray[originalArray.length] = entry;
            entries = newArray;
        }
    }

    /**
     * Retrieves an array of entries at the node.
     *
     * @return The array of entries.
     */
    public MorseDictionaryEntry[] getEntries() {
        if (entries instanceof MorseDictionaryEntry entry) {
            return new MorseDictionaryEntry[]{entry};
        } else if (entries instanceof MorseDictionaryEntry[] array) {
            return array;
        } else {
            return new MorseDictionaryEntry[0];
        }
    }
}
