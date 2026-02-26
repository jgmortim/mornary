package com.github.jgmortim.mornary;

import com.github.jgmortim.mornary.service.MornaryService;
import com.github.jgmortim.mornary.utility.AsciiUtility;
import picocli.CommandLine;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Mornary
 *
 * @author John Mortimore
 */
@CommandLine.Command(name = "mornary", mixinStandardHelpOptions = true, version = "mornary 1.0.0-SNAPSHOT",
        description = "Disguises ASCII text as Morse code")
public class Mornary implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The text or file to encode or decode")
    String input;

    @CommandLine.ArgGroup(multiplicity = "1")
    Mode mode;

    static class Mode {
        @CommandLine.Option(names = "/e", description = "Encode text", paramLabel = "<text>")
        boolean encoding;

        @CommandLine.Option(names = "/E", description = "Encode file", paramLabel = "<file>")
        boolean encodingFile;

        @CommandLine.Option(names = "/d", description = "Decode text", paramLabel = "<text>")
        boolean decoding;

        @CommandLine.Option(names = "/D", description = "Decode file", paramLabel = "<file>")
        boolean decodingFile;
    }

    @CommandLine.Option(names = "-n", hidden = true)
    int numMatchesBeforeSelection = 10;

    @Override
    public Integer call() throws Exception {

        MornaryService service = new MornaryService();

        if (mode.encoding) {
            String binary = AsciiUtility.toAsciiBinary(this.input);
            String output = service.binaryToMorseCode(binary, numMatchesBeforeSelection);
            System.out.println(output);
//            System.out.println(MorseCode.convertToText(output.replace("/", " ")));
        } else if (mode.encodingFile) {

            URL inputUrl = getClass().getResource(this.input);

            String original = Files.readString(Path.of(inputUrl.toURI()));
            String morseCode = service.toMorseCode3(inputUrl);

            //TODO a lot of this is just here for testing because I haven't make any unit tests yet
//            System.out.println("Encoded:");
            System.out.println(morseCode);
//            System.out.println("Decoded as Morse:");
//            System.out.println(MorseCode.convertToText(morseCode.replace("/", " ")));
//            System.out.println("Decoded:");
//            String binary = service.morseCodeToBinary(morseCode);
//            String ascii = AsciiUtility.toAsciiText(binary);
//            System.out.println(ascii);
//            System.out.println("Matches: " + original.equals(ascii));
        } else if (mode.decoding) {
            final String binary = service.morseCodeToBinary(this.input);
            final String output = AsciiUtility.toAsciiText(binary);
            System.out.println(output);
        } else if (mode.decodingFile) {
            final URL inputUrl = getClass().getResource(this.input);
            final String morseCode = Files.readString(Path.of(inputUrl.toURI()));
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