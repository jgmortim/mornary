package com.mornary.model;

public class MorseTrie {

    private final MorseTrieNode root = new MorseTrieNode();

    /**
     * Insert a MorseDictionaryEntry into the trie.
     *
     * @param entry Morse word with bitPattern and length.
     */
    public void insert(MorseDictionaryEntry entry) {
        MorseTrieNode node = root;
        long bits = entry.getBitPattern();
        int len = entry.getBitLength();

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

        node.addEntry(entry);
    }

    public MorseTrieNode root() {
        return root;
    }
}
