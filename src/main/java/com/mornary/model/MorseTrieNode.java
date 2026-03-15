package com.mornary.model;

import java.util.ArrayList;
import java.util.List;

public class MorseTrieNode {

    public MorseTrieNode dot;
    public MorseTrieNode dash;

    public List<MorseDictionaryEntry> entries; // Lazy allocation.

    public MorseTrieNode() {
        this.entries = null; // only allocate when needed
    }

    public void addEntry(MorseDictionaryEntry entry) {
        if (entries == null) entries = new ArrayList<>(1);
        entries.add(entry);
    }
}
