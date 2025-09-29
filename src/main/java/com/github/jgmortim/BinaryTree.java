package com.github.jgmortim;

import java.util.Arrays;

public class BinaryTree {

    Node root;

    final static int MAX_DEPTH = 4;

    public BinaryTree(CharacterEncoding[] characters) {
        this.addAll(characters);
    }

    private Node addRecursive(Node current, CharacterEncoding characterEncoding, String sequence) {
        if (sequence.isEmpty()) {
            if (current == null) {
                return new Node(characterEncoding);
            } else {
                current.characterEncoding = characterEncoding;
                return current;
            }
        }

        if (current == null) {
            current = new Node(null);
        }

        if ('.' == sequence.charAt(0)) {
            current.left = addRecursive(current.left, characterEncoding, sequence.substring(1));
        } else if ('-' == sequence.charAt(0)) {
            current.right = addRecursive(current.right, characterEncoding, sequence.substring(1));
        }


        return current;
    }

    public void add(CharacterEncoding character) {
        root = addRecursive(root, character, character.encoding);
    }

    public void addAll(CharacterEncoding[] characters) {
        Arrays.stream(characters).forEach(this::add);
    }

    public Node get(String morse) {
        return this.get(root, morse);
    }

    private Node get(Node current, String sequence) {
        if (sequence.isEmpty() || current == null) {
            return current;
        }

        if ('.' == sequence.charAt(0)) {
            return get(current.left, sequence.substring(1));
        } else if ('-' == sequence.charAt(0)) {
            return get(current.right, sequence.substring(1));
        } else {
            throw new RuntimeException(sequence.charAt(0) + " is neither a dot or a dash");
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
