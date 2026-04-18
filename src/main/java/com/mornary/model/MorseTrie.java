package com.mornary.model;

import lombok.Getter;

/**
 * A binary trie implementation based on Morse code. Where the branches are dots and dashes.
 *
 * @author John Mortimore
 */
@Getter
public class MorseTrie {

    /**
     * The root node of the trie.
     */
    private final MorseTrieNode root = new MorseTrieNode();

    /**
     * Insert a text segment into the trie.
     *
     * @param textSegment Text segment with bit pattern and length.
     */
    public void insert(TextSegment textSegment) {
        MorseTrieNode node = root;
        long bits = textSegment.getBitPattern();
        int len = textSegment.getBitLength();

        for (int i = len - 1; i >= 0; i--) { // MSB first
            long bit = (bits >> i) & 1;
            if (bit == 0) {
                if (node.dot == null) {
                    node.dot = new MorseTrieNode();
                }
                node = node.dot;
            } else {
                if (node.dash == null) {
                    node.dash = new MorseTrieNode();
                }
                node = node.dash;
            }
        }

        node.addTextSegment(textSegment);
    }
}
