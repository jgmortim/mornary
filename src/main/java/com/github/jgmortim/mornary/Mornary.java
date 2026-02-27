package com.github.jgmortim.mornary;

import com.github.jgmortim.mornary.service.MornaryService;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * Mornary
 *
 * @author John Mortimore
 */
@Command(name = "mornary", mixinStandardHelpOptions = true, version = "mornary 1.0.0-alpha.1",
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

    @Option(names = "-t", description = "number of threads", defaultValue = "10")
    int numThreads;

    @Option(names = "-n", hidden = true)
    int numMatchesBeforeSelection = 10;

    @Override
    public Integer call() throws Exception {

        MornaryService service = new MornaryService(1024, this.numThreads);

        if (this.mode.encodeText != null) {
            service.encode(this.mode.encodeText);
        } else if (this.mode.decodeText != null) {
            service.decode(this.mode.decodeText);
        } else if (this.mode.encodeFile != null) {
            service.encode(this.mode.encodeFile, this.outputFile);


        } else if (this.mode.decodeFile != null) {
            service.decode(this.mode.decodeFile, this.outputFile);
        }


        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Mornary()).execute(args);
        System.exit(exitCode);
    }
}