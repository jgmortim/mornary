package com.github.jgmortim.mornary.model;

import lombok.Getter;

import java.util.Arrays;

/**
 * A binary tree implementation based on morse code. Dots branch left and dashes branch right.
 *
 * @author John Mortimore
 */
public class BinaryTree {

    /**
     * The root node of the tree.
     */
    private Node root;

    /**
     * The depth of the deepest node in the tree.
     */
    @Getter
    private int maxDepth = 0;

    /**
     * Constructs a new BinaryTree from the given array of encodings.
     *
     * @param encodings The array of encodings to populate the tree with.
     */
    public BinaryTree(Encoding[] encodings) {
        this.addAll(encodings);
    }

    /**
     * Adds a given encoding to the tree.
     *
     * @param encoding The encoding to add.
     */
    public void add(Encoding encoding) {
        if (encoding.getCode().length() > maxDepth) {
            maxDepth = encoding.getCode().length();
        }
        root = addRecursive(encoding, root, encoding.getCode());
    }

    /**
     * Adds all encodings from an array to the tree.
     *
     * @param encodings The array of encodings to add.
     */
    public void addAll(Encoding[] encodings) {
        Arrays.stream(encodings).forEach(this::add);
    }

    /**
     * @param encoding      The encoding to add.
     * @param current       The current node.
     * @param remainingCode The reminder of the code to process beyond the current node.
     * @return The node that was added.
     */
    private Node addRecursive(Encoding encoding, Node current, String remainingCode) {
        if (remainingCode.isEmpty()) {
            if (current == null) {
                return new Node(encoding);
            } else {
                current.encoding = encoding;
                return current;
            }
        }

        if (current == null) {
            current = new Node(null);
        }

        if ('.' == remainingCode.charAt(0)) {
            current.left = addRecursive(encoding, current.left, remainingCode.substring(1));
        } else if ('-' == remainingCode.charAt(0)) {
            current.right = addRecursive(encoding, current.right, remainingCode.substring(1));
        }

        return current;
    }

    /**
     * Retrieves the node represented by the given code from the root.
     *
     * @param code A string of dots and dashes.
     * @return The node in the tree represented by the given code.
     */
    public Node get(String code) {
        return this.get(root, code);
    }

    /**
     * Retrieves the node represented by the given code from the StartingNode node.
     *
     * @param StartingNode The node to start the traversal from.
     * @param code         A string of dots and dashes.
     * @return The node in the tree represented by the given code as traversed from the StartingNode.
     */
    private Node get(Node StartingNode, String code) {
        if (code.isEmpty() || StartingNode == null) {
            return StartingNode;
        }

        if ('.' == code.charAt(0)) {
            return get(StartingNode.left, code.substring(1));
        } else if ('-' == code.charAt(0)) {
            return get(StartingNode.right, code.substring(1));
        } else {
            throw new RuntimeException(code.charAt(0) + " is neither a dot or a dash");
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
