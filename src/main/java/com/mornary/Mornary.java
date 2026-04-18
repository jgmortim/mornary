package com.mornary;

import com.mornary.converter.PositiveIntConverter;
import com.mornary.configuration.ShortErrorMessageHandler;
import com.mornary.service.DecodeService;
import com.mornary.service.EncodeService;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * Mornary command line interface.
 *
 * @author John Mortimore
 */
@Command(
    name = "mornary",
    version = "Mornary 1.0.0-beta.1",
    description = "Generative steganography using Morse code.",
    sortOptions = false,
    sortSynopsis = false,
    usageHelpAutoWidth = true,
    mixinStandardHelpOptions = true
)
public class Mornary implements Callable<Integer> {

    @ArgGroup(multiplicity = "1")
    Operation operation;

    static class Operation {
        @Option(
            order = 0,
            names = {"-e", "--encode"}, paramLabel = "<text>",
            description = "Encodes the supplied text."
        )
        String encodeText;

        @Option(
            order = 1,
            names = {"-E", "--Encode"}, paramLabel = "<file>",
            description = "Encodes the supplied file."
        )
        File encodeFile;

        @Option(
            order = 2,
            names = {"-d", "--decode"}, paramLabel = "<text>",
            description = "Decodes the supplied Mornary-encoded text."
        )
        String decodeText;

        @Option(
            order = 3,
            names = {"-D", "--Decode"}, paramLabel = "<file>",
            description = "Decodes the Mornary-encoded contents of the supplied file."
        )
        File decodeFile;
    }

    @Option(
        order = 4,
        names = {"-O", "--Output"}, paramLabel = "<file>",
        description = "Writes the output to the supplied file. If omitted, output will be printed to the console."
    )
    File outputFile;

    @Option(
        order = 5,
        names = {"-t", "--threads"}, paramLabel = "<int>", defaultValue = "10",
        description = "Sets the thread pool size. Only used when encoding files. Defaults to 10.",
        converter = PositiveIntConverter.class
    )
    int numThreads;

    @Option(
        order = 6,
        names = {"-m", "--low-memory"}, paramLabel = "<boolean>", defaultValue = "false",
        description = "Reduces the dictionary size in order to reduce the memory footprint of the app. Only used for encoding."
    )
    boolean lowMemory;

    @Override
    public Integer call() throws Exception {

        if (this.operation.encodeText != null || this.operation.encodeFile != null) { // Encoding.

            EncodeService encodeService = new EncodeService(1024, this.numThreads, this.lowMemory);

            if (this.operation.encodeText != null) {
                encodeService.encode(this.operation.encodeText, this.outputFile);
            } else {
                encodeService.encode(this.operation.encodeFile, this.outputFile);
            }

        } else if (this.operation.decodeText != null || this.operation.decodeFile != null) { // Decoding.

            DecodeService decodeService = new DecodeService(1024);

            if (this.operation.decodeText != null) {
                decodeService.decode(this.operation.decodeText, this.outputFile);
            } else {
                decodeService.decode(this.operation.decodeFile, this.outputFile);
            }
        }

        return 0;
    }

    /**
     * Main method.
     *
     * @param args Application arguments
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Mornary())
            .setParameterExceptionHandler(new ShortErrorMessageHandler())
            .execute(args);
        System.exit(exitCode);
    }
}