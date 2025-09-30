package com.github.jgmortim.mornary;

import com.github.jgmortim.mornary.service.MornaryService;
import com.github.jgmortim.mornary.utility.MornaryUtility;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        String input = "Hello World";

        String binary = MornaryUtility.toAsciiBinary(input);

        MornaryService service = new MornaryService();
        String output = service.binaryToMorseCode(binary);

        System.out.println(output);
    }
}