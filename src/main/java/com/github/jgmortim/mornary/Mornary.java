package com.github.jgmortim.mornary;

import com.github.jgmortim.mornary.service.MornaryService;
import com.github.jgmortim.mornary.utility.AsciiUtility;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Mornary
 *
 * @author John Mortimore
 */
@Command(name = "mornary", mixinStandardHelpOptions = true, version = "mornary 1.0.0-SNAPSHOT",
        description = "Disguises text and binary data as Morse code")
public class Mornary implements Callable<Integer> {

    @ArgGroup(multiplicity = "1")
    Mode mode;

    static class Mode {
        @Option(names = "-e", description = "Encode text", paramLabel = "<text>")
        String encodeText;

        @Option(names = "-d", description = "Decode text", paramLabel = "<text>")
        String decodeText;

        @Option(names = "-E", description = "Encode a file", paramLabel = "<inputFile>")
        File encodeFile;

        @Option(names = "-D", description = "Decode a file", paramLabel = "<inputFile>")
        File decodeFile;
    }

    @Option(names = "-O", description = "File to write output to. Will write to console if omitted", paramLabel = "<outputFile>")
    File outputFile;

    @Option(names = "-n", hidden = true)
    int numMatchesBeforeSelection = 10;

    @Override
    public Integer call() throws Exception {

        MornaryService service = new MornaryService();

        if (this.mode.encodeText != null) {
            final String binary = AsciiUtility.toAsciiBinary(mode.encodeText);
            final String output = service.binaryToMorseCode(binary, numMatchesBeforeSelection);
            System.out.println(output);
        } else if (this.mode.decodeText != null) {
            final String binary = service.morseCodeToBinary(mode.decodeText);
            final String output = AsciiUtility.toAsciiText(binary);
            System.out.println(output);
        } else if (this.mode.encodeFile != null) {


            service.toMorseCode7(this.mode.encodeFile.toURI().toURL(), this.outputFile.toURI().toURL());


        } else if (this.mode.decodeFile != null) {
            final String morseCode = Files.readString(Path.of(this.mode.decodeFile.toURI()));
            final String binary = service.morseCodeToBinary(morseCode);
            byte[] data = decodeBinary(binary);
            java.nio.file.Files.write(this.outputFile.toPath(), data);
        }



        return 0;
    }

    static byte[] decodeBinary(String s) {
        if (s.length() % 8 != 0) throw new IllegalArgumentException("Binary data length must be multiple of 8");
        byte[] data = new byte[s.length() / 8];
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '1') {
                data[i >> 3] |= 0x80 >> (i & 0x7);
            } else if (c != '0') {
                throw new IllegalArgumentException("Invalid char in binary string");
            }
        }
        return data;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Mornary()).execute(args);
        System.exit(exitCode);
    }
}