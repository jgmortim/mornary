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

    /*
     * The text segments. Because the trie can have hundreds of thousands of nodes, it important to be as lazy as possible when
     * creating this collection. We start it both fields as null. And then change it to a single TextSegment when the first entry
     * is encountered. If a second entry is encountered, then we convert it to an array. And the first field is coverted back
     * to null. Most nodes are going to either have 0 or 1 entry. So we only want to create arrays when absolutely necessary.
     */
    private TextSegment singleTextSegment = null;
    private TextSegment[] TextSegmentArray = null;

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
        if (singleTextSegment == null && TextSegmentArray == null) {
            // First text segment encountered for this node.
            singleTextSegment = textSegment;
        } else if (TextSegmentArray == null) {
            // If there is a second text segment, convert it to an array and add the new text segment.
            TextSegmentArray = new TextSegment[]{singleTextSegment, textSegment};
            singleTextSegment = null;
        } else {
            // If there is already an array, grow the array and add the new text segment.
            TextSegment[] originalArray = TextSegmentArray;
            TextSegment[] newArray = Arrays.copyOf(originalArray, originalArray.length + 1);
            newArray[originalArray.length] = textSegment;
            TextSegmentArray = newArray;
        }
    }

    /**
     * Retrieves an array of text segments at the node.
     *
     * @return The array of text segments.
     */
    public TextSegment[] getTextSegments() {
        if (singleTextSegment != null) {
            return new TextSegment[]{singleTextSegment};
        } else if (TextSegmentArray != null) {
            return TextSegmentArray;
        } else  {
            return new TextSegment[0];
        }
    }
}
