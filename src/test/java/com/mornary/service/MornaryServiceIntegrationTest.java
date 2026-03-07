package com.mornary.service;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration test class for {@link MornaryService}.
 * <p>
 * These tests pipe large inputs through both the encode and decode methods to ensure that the two methods
 * are perfect inverses of each other.
 *
 * @author John Mortimore
 */
public class MornaryServiceIntegrationTest {

    MornaryService service;

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    public MornaryServiceIntegrationTest() throws IOException {
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


    @ParameterizedTest
    @ValueSource(strings = {
            "/payloads/SmallTextFile.txt",
            "/payloads/5kb.txt",
            "/payloads/512kb.txt"
    })
    public void encodeAndDecode_outputFile_successful(String inputFile) throws IOException, URISyntaxException {

        final File input =  new File(getClass().getResource(inputFile).toURI());
        final File outputEncode = new File("testEncode.txt");
        final File outputDecode = new File("testDecode.txt");

        this.service.encode(input, outputEncode);
        this.service.decode(outputEncode, outputDecode);

        assertTrue(FileUtils.contentEquals(input, outputDecode));

        if (!outputEncode.delete()) { //delete the output file after test runs
            fail("Output file " + outputEncode.getName() + " could not be deleted after test completion");
        }
        if (!outputDecode.delete()) { //delete the output file after test runs
            fail("Output file " + outputDecode.getName() + " could not be deleted after test completion");
        }
    }

}
