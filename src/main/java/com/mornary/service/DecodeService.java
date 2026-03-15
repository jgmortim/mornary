package com.mornary.service;

import com.mornary.exception.NotTextException;
import com.mornary.utility.AsciiUtility;
import com.mornary.utility.BinaryUtilities;
import com.mornary.utility.OutputUtility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Mornary decoding service.
 *
 * @author John Mortimore
 */
public class DecodeService {

    private final int workUnitSize;

    /**
     * Constructs the MornaryService.
     *
     * @param workUnitSize The number of bytes of input to be processed per thread task.
     */
    public DecodeService(int workUnitSize) {
        this.workUnitSize = workUnitSize;
    }

    /**
     * Decodes the given input text and writes the output to the specified output file, or to the console if the file is null.
     *
     * @param input  The Morse code to decode.
     * @param output The file to write the output to. If the file exists, it will be truncated;
     *               if it does not exist, it will be created. If omitted, and the raw data is text, it will be
     *               printed to the console. If it's not text, then an error will be thrown
     */
    public void decode(String input, File output) throws IOException {
        final String binary = this.morseCodeToBinaryString(input);

        byte[] decodedData = BinaryUtilities.binaryStringToByteArray(binary);

        try (OutputStream outputStream = OutputUtility.createOutputStream(output)) {

            if (output != null || AsciiUtility.isAsciiText(decodedData)) {
                outputStream.write(decodedData);
            } else {
                throw new NotTextException();
            }
        }
    }

    /**
     * Decodes the given input file from Morse code into the original binary data and writes the result to the
     * given output file, or to the console if output is null.
     *
     * @param input  The file containing Morse code to be decoded.
     * @param output The file to write the output to. If the file exists, it will be truncated;
     *               if it does not exist, it will be created. If omitted, and the raw data is text, it will be
     *               printed to the console. If it's not text, then an error will be thrown
     */
    public void decode(File input, File output) throws IOException {

        final long fileSize = input.length();
        final long totalWorkUnits = (long) Math.ceil((double) fileSize / this.workUnitSize);

        try (
            InputStream is = input.toURI().toURL().openStream();
            OutputStream outputStream = OutputUtility.createOutputStream(output)
        ) {

            // Read off the first "workUnitSize" bytes to the data buffer (or less if the file is under "workUnitSize" bytes).
            byte[] dataBuffer = new byte[this.workUnitSize];
            int readLength = is.read(dataBuffer, 0, this.workUnitSize);


            StringBuilder binaryStringBuffer = new StringBuilder();
            int writeIndex = 0;

            // Loop until all the data has been read into the buffer.
            while (readLength > 0) {

                // Convert data to ASCII and append to the end of the morseCode string.
                String morseCode = AsciiUtility.toAsciiText(dataBuffer, readLength);

                // Decode the morseCode to a binary string and add to the buffer.
                binaryStringBuffer.append(this.morseCodeToBinaryString(morseCode));

                int numBytes = binaryStringBuffer.length() / 8;
                int numBitsToWrite = 8 * numBytes;

                byte[] decodedData = BinaryUtilities.binaryStringToByteArray(binaryStringBuffer.substring(0, numBitsToWrite));

                if (output != null || AsciiUtility.isAsciiText(decodedData)) {
                    outputStream.write(decodedData);
                } else {
                    throw new NotTextException();
                }
                writeIndex++;

                if (output != null) { // If using a file output, print progress to console.
                    this.printProgress(writeIndex, totalWorkUnits);
                }

                binaryStringBuffer = new StringBuilder(binaryStringBuffer.substring(numBitsToWrite));

                // Read another "workUnitSize" bytes
                readLength = is.read(dataBuffer, 0, this.workUnitSize);
            }

            if (!binaryStringBuffer.isEmpty()) {
                System.out.println("Error: Input file was decoded, but number of bits not divisible by 8. " +
                                   "Remaining bits not written to output: " + binaryStringBuffer);
            }


        } catch (IOException e) {
            System.err.printf("Failed to read from %s: %s%n", input, e.getMessage());
            throw e;
        }
    }

    /**
     * Converts a string of morse code into a binary string.
     * Dots become zeros, dashes become ones, spaces and forward slashes are removed.
     *
     * @param morse The Morse code string.
     * @return The matching binary string.
     */
    private String morseCodeToBinaryString(String morse) {
        return morse
            .replace('.', '0')
            .replace('-', '1')
            .replace(" ", "")
            .replace("/", "");
    }

    /**
     * Prints the current progress percentage to the console.
     *
     * @param workUnitsWritten The number of completed work units that have been written to the output file.
     * @param totalWorkUnits   The total number of work units in the operation.
     */
    private void printProgress(long workUnitsWritten, long totalWorkUnits) {
        double progress = 100 * ((double) workUnitsWritten / totalWorkUnits);
        System.out.printf("\rWork Units Completed: %d of %d (%.2f%%)", workUnitsWritten, totalWorkUnits, progress);
    }

}
