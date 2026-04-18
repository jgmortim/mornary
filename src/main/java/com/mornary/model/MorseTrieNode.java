package com.mornary.model;

import java.util.Arrays;

/**
 * Node object for {@link MorseTrie}.
 *
 * @author John Mortimore
 */
public class MorseTrieNode {

    public MorseTrieNode dot;
    public MorseTrieNode dash;

    /**
     * The text segments. Because the trie can have hundreds of thousands of nodes, it important to be as lazy as possible when
     * creating this collection. We start it as null. And then change it to a single {@link TextSegment} when the first entry is
     * encountered. If a second entry is encountered, then we convert it to an array. Most nodes are going to either have 0 or 1
     * entry. So we only want to create arrays when absolutely necessary.
     */
    private Object data = null;

    /**
     * Constructs a new node.
     */
    public MorseTrieNode() {
    }

    /**
     * Adds a text segment to the node.
     *
     * @param textSegment The text segment to add.
     */
    public void addTextSegment(TextSegment textSegment) {
        if (data == null) {
            // If node has no data, just set the data to be the text segment.
            data = textSegment;
        } else if (data instanceof TextSegment original) {
            // If the data is a single text segment, convert it to an array and add the new text segment.
            data = new TextSegment[]{original, textSegment};
        } else {
            // If the data is already an array, grow the array and add the new text segment.
            TextSegment[] originalArray = (TextSegment[]) data;
            TextSegment[] newArray = Arrays.copyOf(originalArray, originalArray.length + 1);
            newArray[originalArray.length] = textSegment;
            data = newArray;
        }
    }

    /**
     * Retrieves an array of text segments at the node.
     *
     * @return The array of text segments.
     */
    public TextSegment[] getTextSegments() {
        if (data instanceof TextSegment entry) {
            return new TextSegment[]{entry};
        } else if (data instanceof TextSegment[] array) {
            return array;
        } else {
            return new TextSegment[0];
        }
    }
}
