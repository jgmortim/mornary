package com.mornary.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test class for {@link MornaryService}.
 *
 * @author John Mortimore
 */
public class MornaryServiceTest {

    MornaryService service;

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    public MornaryServiceTest() throws IOException {
        service = new MornaryService(1024, 10);
    }

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    public void encodeText_noOutputFile_validOutputPrintedToConsole() throws IOException {
        final String input = "Hello World!";

        // Output is not deterministic, but with the letter and word breaks removed, it must match the following.
        final String expectedWithoutBreaks =
                ".-..-....--..-.-.--.--...--.--...--.----..-......-.-.---.--.----.---..-..--.--...--..-....-....-";

        this.service.encode(input, null);

        final String actual = outputStreamCaptor.toString();

        assertTrue(actual.matches("^[.\\- /]+$")); // Only dots, dashes, spaces, and slashes.

        final String actualWithoutBreaks = actual.replace(" ", "").replace("/", "");

        assertEquals(expectedWithoutBreaks, actualWithoutBreaks);
    }

    @Test
    public void encodeText_outputFile_validOutputWrittenToFile() throws IOException {
        final String input = "Hello World!";

        // Output is not deterministic, but with the letter and word breaks removed, it must match the following.
        final String expectedWithoutBreaks =
                ".-..-....--..-.-.--.--...--.--...--.----..-......-.-.---.--.----.---..-..--.--...--..-....-....-";

        final File output = new File("testOut.txt");

        this.service.encode(input, output);

        final String outputFileContents = new String(Files.readAllBytes(output.toPath()));

        assertTrue(outputFileContents.matches("^[.\\- /]+$")); // Only dots, dashes, spaces, and slashes.

        final String actualWithoutBreaks = outputFileContents.replace(" ", "").replace("/", "");

        assertEquals(expectedWithoutBreaks, actualWithoutBreaks);

        if (!output.delete()) { //delete the output file after test runs
            fail("Output file " + output.getName() + " could not be deleted after test completion");
        }
    }

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
}
