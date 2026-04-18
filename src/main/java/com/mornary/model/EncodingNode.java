package com.mornary.model;

import lombok.Getter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents a Morse code node in a {@link EncodingBinaryTree}.
 *
 * @author John Mortimore
 */
@Getter
public class EncodingNode {
    Encoding encoding;
    EncodingNode left;
    EncodingNode right;


    EncodingNode(Encoding encoding) {
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

        Set<EncodingNode> children = new HashSet<>();
        if (left != null) {
            children.add(left);
        }
        if (right != null) {
            children.add(right);
        }

        for (Iterator<EncodingNode> it = children.iterator(); it.hasNext();) {
            EncodingNode next = it.next();
            if (it.hasNext()) {
                next.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                next.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }


}
