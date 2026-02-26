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
import java.util.List;
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
        @Option(names = "/e", description = "Encode text", paramLabel = "<text>")
        String encodeText;

        @Option(names = "/d", description = "Decode text", paramLabel = "<text>")
        String decodeText;

        @Option(names = "/E", arity = "2", description = "Encode file", paramLabel = "<inputFile> <outputFile>")
        List<File> encodeFile;

        @Option(names = "/D", arity = "2", description = "Decode file", paramLabel = "<inputFile> <outputFile>")
        List<File> decodeFile;
    }

    @Option(names = "-n", hidden = true)
    int numMatchesBeforeSelection = 10;

    @Override
    public Integer call() throws Exception {

        MornaryService service = new MornaryService();


        if (this.mode.encodeText != null) {
            String binary = AsciiUtility.toAsciiBinary(mode.encodeText);
            String output = service.binaryToMorseCode(binary, numMatchesBeforeSelection);
            System.out.println(output);
        } else if (this.mode.decodeText != null) {
            final String binary = service.morseCodeToBinary(mode.decodeText);
            final String output = AsciiUtility.toAsciiText(binary);
            System.out.println(output);
        } else if (this.mode.encodeFile != null) {
            String morseCode = service.toMorseCode3(this.mode.encodeFile.get(0).toURI().toURL());
            Path outputPath = this.mode.encodeFile.get(1).toPath();
            Files.writeString(outputPath, morseCode);
        } else if (this.mode.decodeFile != null) {
            final String morseCode = Files.readString(Path.of(this.mode.decodeFile.get(0).toURI()));
            final String binary = service.morseCodeToBinary(morseCode);
            final String output = AsciiUtility.toAsciiText(binary);
            Path outputPath = this.mode.decodeFile.get(1).toPath();
            Files.writeString(outputPath, output);
        }

        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Mornary()).execute(args);
        System.exit(exitCode);
    }
}