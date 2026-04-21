package com.mornary.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test class for {@link EncodeService}.
 *
 * @author John Mortimore
 */
public class EncodeServiceUnitTest {

    private static final EncodeService SERVICE;

    static {
        try {
            SERVICE = new EncodeService(1024, 10, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

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
    public void encodeText_noOutputFile_validOutputPrintedToConsole() throws IOException {
        final String input = "Hello World!";

        // Output is not deterministic, but with the letter and word breaks removed, it must match the following.
        final String expectedWithoutBreaks =
                ".-..-....--..-.-.--.--...--.--...--.----..-......-.-.---.--.----.---..-..--.--...--..-....-....-";

        SERVICE.encode(input, null);

        final String actual = outputStreamCaptor.toString().trim();

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

        SERVICE.encode(input, output);

        final String outputFileContents = new String(Files.readAllBytes(output.toPath()));

        assertTrue(outputFileContents.matches("^[.\\- /]+$")); // Only dots, dashes, spaces, and slashes.

        final String actualWithoutBreaks = outputFileContents.replace(" ", "").replace("/", "");

        assertEquals(expectedWithoutBreaks, actualWithoutBreaks);

        if (!output.delete()) { //delete the output file after test runs
            fail("Output file " + output.getName() + " could not be deleted after test completion");
        }
    }

    /* File Input Methods - Small Text Payload */

    @Test
    public void encodeTxtFile_noOutputFile_validOutputPrintedToConsole() throws IOException, URISyntaxException {
        final File input =  new File(getClass().getResource("/payloads/SmallTextFile.txt").toURI());

        // Output is not deterministic, but with the letter and word breaks removed, it must match the following.
        final String expectedWithoutBreaks =
                ".-.-.-...--.-....--..-.-..-......---...-.---.-.-.--.-..-.--...--.--.-.--..-......--...-..---..-." +
                ".--.----.---.---.--.---...-......--..--..--.----.----.....-......--.-.-..---.-.-.--.--.-.---...." +
                ".---..--..-......--.----.---.--..--..-.-.---..-...-......---.-...--.-....--..-.-..-......--.--.." +
                ".--....-.----.-..----..-..-......--..-...--.----.--..---..-.---.";

        SERVICE.encode(input, null);

        final String actual = outputStreamCaptor.toString(StandardCharsets.UTF_8);

        assertTrue(actual.matches("^[.\\- /]+$")); // Only dots, dashes, spaces, and slashes.

        final String actualWithoutBreaks = actual.replace(" ", "").replace("/", "");

        assertEquals(expectedWithoutBreaks, actualWithoutBreaks);
    }

    @Test
    public void encodeTxtText_outputFile_validOutputWrittenToFile() throws IOException, URISyntaxException {
        final File input =  new File(getClass().getResource("/payloads/SmallTextFile.txt").toURI());

        // Output is not deterministic, but with the letter and word breaks removed, it must match the following.
        final String expectedWithoutBreaks =
                ".-.-.-...--.-....--..-.-..-......---...-.---.-.-.--.-..-.--...--.--.-.--..-......--...-..---..-." +
                ".--.----.---.---.--.---...-......--..--..--.----.----.....-......--.-.-..---.-.-.--.--.-.---...." +
                ".---..--..-......--.----.---.--..--..-.-.---..-...-......---.-...--.-....--..-.-..-......--.--.." +
                ".--....-.----.-..----..-..-......--..-...--.----.--..---..-.---.";

        final File output = new File("testOut.txt");

        SERVICE.encode(input, output);

        final String outputFileContents = new String(Files.readAllBytes(output.toPath()));

        assertTrue(outputFileContents.matches("^[.\\- /]+$")); // Only dots, dashes, spaces, and slashes.

        final String actualWithoutBreaks = outputFileContents.replace(" ", "").replace("/", "");

        assertEquals(expectedWithoutBreaks, actualWithoutBreaks);

        if (!output.delete()) { //delete the output file after test runs
            fail("Output file " + output.getName() + " could not be deleted after test completion");
        }
    }

}
