package com.mornary.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test class for {@link DecodeService}.
 *
 * @author John Mortimore
 */
public class DecodeServiceUnitTest {

    DecodeService service;

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    public DecodeServiceUnitTest() throws IOException {
        service = new DecodeService(1024);
    }

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    /* Text Input Methods */

    @Test
    public void decodeText_noOutputFile_correctOutputPrintedToConsole() throws IOException {
        final String input = ".-. . -.. . . -- . .-. / - . -- .--. . .- -. / --. .. --. --- - / . .-.. . ...- . -. /" +
                             " - -- . -- .- / --- .-- -. . -.. / -- .- -.. .-- . . -.. / .. -. ... -";
        final String expected = "Hello World!";

        this.service.decode(input, null);

        assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void decodeText_outputFile_correctOutputWrittenToFile() throws IOException {
        final String input = ".-. . -.. . . -- . .-. / - . -- .--. . .- -. / --. .. --. --- - / . .-.. . ...- . -. /" +
                             " - -- . -- .- / --- .-- -. . -.. / -- .- -.. .-- . . -.. / .. -. ... -";
        final String expected = "Hello World!";
        final File output = new File("testOut.txt");

        this.service.decode(input, output);

        String outputFileContents = new String(Files.readAllBytes(output.toPath()));

        assertEquals(expected, outputFileContents);

        if (!output.delete()) { //delete the output file after test runs
            fail("Output file " + output.getName() + " could not be deleted after test completion");
        }
    }

    /* File Input Methods - Small Text Payload */

    @Test
    public void decodeTxtFile_noOutputFile_correctOutputPrintedToConsole() throws IOException, URISyntaxException {
        final File input =  new File(getClass().getResource("/stego/SmallTextFileEncoded.txt").toURI());

        final String expected = "The quick brown fox jumps over the lazy dog.";

        this.service.decode(input, null);

        assertEquals(expected, outputStreamCaptor.toString());
    }

    @Test
    public void decodeTxtFile_outputFile_correctOutputWrittenToFile() throws IOException, URISyntaxException {
        final File input =  new File(getClass().getResource("/stego/SmallTextFileEncoded.txt").toURI());

        final String expected = "The quick brown fox jumps over the lazy dog.";

        final File output = new File("testOut.txt");

        this.service.decode(input, output);

        String outputFileContents = new String(Files.readAllBytes(output.toPath()));

        assertEquals(expected, outputFileContents);

        if (!output.delete()) { //delete the output file after test runs
            fail("Output file " + output.getName() + " could not be deleted after test completion");
        }
    }
}
