package com.github.jgmortim.mornary;

import com.epic.morse.service.MorseCode;
import com.github.jgmortim.mornary.service.MornaryService;
import com.github.jgmortim.mornary.utility.AsciiUtility;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 *
 * @author John Mortimore
 */
@CommandLine.Command(name = "mornary", mixinStandardHelpOptions = true, version = "mornary 1.0.0",
        description = "Disguises ASCII text as Morse code")
public class Main implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The text to encode")
    private String input;

    @CommandLine.Option(names = "/e")
    boolean encoding;

    @CommandLine.Option(names = "/d")
    boolean decoding;

    @CommandLine.Option(names = "-n")
    int numMatchesBeforeSelection = 10;

    @Override
    public Integer call() throws Exception {

        MornaryService service = new MornaryService();

        if (encoding) {
            String binary = AsciiUtility.toAsciiBinary(this.input);
            String output = service.binaryToMorseCode(binary, numMatchesBeforeSelection);
            System.out.println(output);
            System.out.println(MorseCode.convertToText(output.replace("/", " ")));
        }
        if (decoding) {
            String binary = service.morseCodeToBinary(this.input);
            String output = AsciiUtility.toAsciiText(binary);
            System.out.println(output);
        }

        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}