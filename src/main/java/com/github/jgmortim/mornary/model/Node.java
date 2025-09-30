package com.github.jgmortim.mornary.model;

import lombok.Getter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents a Morse code node in a morse code binary tree.
 *
 * @author John Mortimore
 */
@Getter
public class Node {
    Encoding encoding;
    Node left;
    Node right;


    Node(Encoding encoding) {
        this.encoding = encoding;
        right = null;
        left = null;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(50);
        print(buffer, "", "");
        return buffer.toString();
    }

    private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(encoding.getCharacter());
        buffer.append('\n');

        Set<Node> children = new HashSet<>();
        if (left != null) {
            children.add(left);
        }
        if (right != null) {
            children.add(right);
        }

        for (Iterator<Node> it = children.iterator(); it.hasNext();) {
            Node next = it.next();
            if (it.hasNext()) {
                next.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                next.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }


}
