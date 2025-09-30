package com.github.jgmortim.mornary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jgmortim.mornary.model.BinaryTree;
import com.github.jgmortim.mornary.model.Encoding;
import com.github.jgmortim.mornary.utility.MornaryUtility;

import java.io.File;
import java.io.IOException;

public class Main {

    private static final String PATH = "\\resources\\morsecode.json";


    private static BinaryTree loadTree() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Encoding[] encodings = mapper.readValue(new File(PATH), Encoding[].class);

        //        EnumSet.allOf(Morse.class)
        //           .forEach(i -> System.out.println(tree.get(i.getCode()).character + " : " + i.getCode()));

        return new BinaryTree(encodings);
    }

    public static void main(String[] args) throws IOException {
        String input = "Hello World";


        BinaryTree tree = loadTree();

        String binary = MornaryUtility.toAsciiBinary(input);
        String output = MornaryUtility.binaryToDotsAndDashes(binary, tree);


        System.out.println(output);

    }
}