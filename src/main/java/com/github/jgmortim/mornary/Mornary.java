package com.github.jgmortim.mornary;

import com.github.jgmortim.mornary.service.MornaryService;
import com.github.jgmortim.mornary.utility.AsciiUtility;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Mornary
 *
 * @author John Mortimore
 */
@CommandLine.Command(name = "mornary", mixinStandardHelpOptions = true, version = "mornary 1.0.0-SNAPSHOT",
        description = "Disguises text and binary data as Morse code")
public class Mornary implements Callable<Integer> {

    @CommandLine.ArgGroup(multiplicity = "1")
    Mode mode;

    static class Mode {
        @CommandLine.Option(names = "/e", description = "Encode text", paramLabel = "<text>")
        String encodeText;

        @CommandLine.Option(names = "/E", description = "Encode file", paramLabel = "<file>")
        File encodeFile;

        @CommandLine.Option(names = "/d", description = "Decode text", paramLabel = "<text>")
        String decodeText;

        @CommandLine.Option(names = "/D", description = "Decode file", paramLabel = "<file>")
        File decodeFile;
    }

    @CommandLine.Option(names = "-n", hidden = true)
    int numMatchesBeforeSelection = 10;

    @Override
    public Integer call() throws Exception {

        MornaryService service = new MornaryService();

        if (mode.encodeText != null) {
            String binary = AsciiUtility.toAsciiBinary(this.mode.encodeText);
            String output = service.binaryToMorseCode(binary, numMatchesBeforeSelection);
            System.out.println(output);
        } else if (mode.encodeFile != null) {

            String morseCode = service.toMorseCode3(this.mode.encodeFile.toURI().toURL());

            //TODO a lot of this is just here for testing because I haven't make any unit tests yet
            System.out.println(morseCode);
//            System.out.println("Decoded as Morse:");
//            System.out.println(MorseCode.convertToText(morseCode.replace("/", " ")));
//            System.out.println("Decoded:");
//            String binary = service.morseCodeToBinary(morseCode);
//            String ascii = AsciiUtility.toAsciiText(binary);
//            System.out.println(ascii);
//            System.out.println("Matches: " + original.equals(ascii));
        } else if (mode.decodeText != null) {
            final String binary = service.morseCodeToBinary(this.mode.decodeText);
            final String output = AsciiUtility.toAsciiText(binary);
            System.out.println(output);
        } else if (mode.decodeFile != null) {
            final String morseCode = Files.readString(Path.of(this.mode.decodeFile.toURI()));
            final String binary = service.morseCodeToBinary(morseCode);
            final String output = AsciiUtility.toAsciiText(binary);
            System.out.println(output);
        }

        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Mornary()).execute(args);
        System.exit(exitCode);
    }
}