package com.github.jgmortim.mornary;

import com.github.jgmortim.mornary.service.MornaryService;
import com.github.jgmortim.mornary.utility.AsciiUtility;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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

        String output = "";

        if (this.mode.encodeText != null) {
            String binary = AsciiUtility.toAsciiBinary(mode.encodeText);
            output = service.binaryToMorseCode(binary, numMatchesBeforeSelection);
        } else if (this.mode.decodeText != null) {
            final String binary = service.morseCodeToBinary(mode.decodeText);
            output = AsciiUtility.toAsciiText(binary);
        } else if (this.mode.encodeFile != null) {
            output = service.toMorseCode3(this.mode.encodeFile.toURI().toURL());
        } else if (this.mode.decodeFile != null) {
            final String morseCode = Files.readString(Path.of(this.mode.decodeFile.toURI()));
            final String binary = service.morseCodeToBinary(morseCode);
            output = AsciiUtility.toAsciiText(binary);
        }

        if (this.outputFile != null) {
            Path outputPath = this.outputFile.toPath();
            Files.writeString(outputPath, output);
        } else {
            System.out.println(output);
        }

        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Mornary()).execute(args);
        System.exit(exitCode);
    }
}