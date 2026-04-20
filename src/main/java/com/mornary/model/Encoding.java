package com.mornary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A representation of a Morse character encoding.
 *
 * @author John Mortimore
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Encoding {

    /**
     * The character. For example, "A".
     */
    private char character;

    /**
     * The code. For example, ".-".
     */
    private String code;
}
