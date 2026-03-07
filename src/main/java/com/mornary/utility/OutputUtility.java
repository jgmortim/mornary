package com.mornary.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Utility class for operations involving file and console outputs.
 *
 * @author John Mortimore
 */
public class OutputUtility {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private OutputUtility() {
    }

    /**
     * Creates a BufferedWriter for the specified output file.
     *
     * @param output The output file to use. If null, then System.out will be used.
     * @return The BufferedWriter.
     */
    public static BufferedWriter createWriter(File output) throws IOException {
        if (output != null) {
            return Files.newBufferedWriter(
                    Paths.get(output.toURI()),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        }

        return new BufferedWriter(new OutputStreamWriter(System.out)) {
            @Override
            public void close() throws IOException {
                flush(); // Don't close System.out
            }
        };
    }

    /**
     * Creates an appropriate OutputStream for the specified output file.
     *
     * @param output The output file to use. If null, then System.out will be used.
     * @return The OutputStream.
     */
    public static OutputStream createOutputStream(File output) throws FileNotFoundException {
        if (output != null) {
            return new FileOutputStream(output);
        } else {
            return new FilterOutputStream(System.out) {
                @Override
                public void close() throws IOException {
                    flush(); // Don't close System.out
                }
            };
        }

    }
}
