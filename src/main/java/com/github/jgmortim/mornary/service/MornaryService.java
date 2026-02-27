package com.github.jgmortim.mornary.service;

import com.epic.morse.service.MorseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jgmortim.mornary.exception.InvalidBinaryException;
import com.github.jgmortim.mornary.model.BinaryTree;
import com.github.jgmortim.mornary.model.Encoding;
import com.github.jgmortim.mornary.model.IndexedResult;
import com.github.jgmortim.mornary.model.MorseDictionaryEntry;
import com.github.jgmortim.mornary.model.Node;
import com.github.jgmortim.mornary.utility.AsciiUtility;
import com.github.jgmortim.mornary.utility.BinaryUtilities;
import com.github.jgmortim.mornary.utility.OutputUtility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service class for Mornary.
 *
 * @author John Mortimore
 */
public class MornaryService {

    private static final String MORSE_CODE_WORD_DELIMITER = " / ";
    private static final String CANT_PRINT_OUTPUT_ERROR_MESSAGE =
            "No output file specified, but decoded data is not ASCII text and cannot be printed to the console."
                    + "\nPlease specify an output file and try again.";

    private final BinaryTree tree;

    private final List<MorseDictionaryEntry> morseDictionary;

    private final int workUnitSize;
    private final int threadPoolSize;
    private final int queueCapacity;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Constructs the MornaryService.
     *
     * @param workUnitSize   The number of bytes of input to be processed per thread task.
     * @param threadPoolSize The number of threads to use for encoding.
     */
    public MornaryService(int workUnitSize, int threadPoolSize) throws IOException {
        this.workUnitSize = workUnitSize;
        this.threadPoolSize = threadPoolSize;
        this.queueCapacity = threadPoolSize + 10;

        // Load in the binary tree.
        URL morseUrl = getClass().getResource("/morsecode.json");
        Encoding[] encodings = OBJECT_MAPPER.readValue(morseUrl, Encoding[].class);
        this.tree = new BinaryTree(encodings);

        // Load in a dictionary file.
        try (InputStream input = getClass().getResourceAsStream("/English5000.txt")) {

            if (input == null) {
                throw new RuntimeException("Dictionary not found");
            }

            this.morseDictionary = new BufferedReader(new InputStreamReader(input))
                    .lines()
                    .map(MorseCode::convertToMorseCode)
                    .map(word -> new MorseDictionaryEntry(word, word.replace(" ", "")))
                    .distinct()
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Failed to load dictionary", e);
        }
    }


    /**
     * Encodes the given input text as morse code and prints the output to the console.
     *
     * @param input The text to encode.
     */
    public void encode(String input) {
        final String binary = AsciiUtility.toAsciiBinary(input);
        if (!binary.matches("^[0-1]+$")) {
            throw new InvalidBinaryException(binary);
        }

        String unprocessedMorse = binary
                .replace('0', '.')
                .replace('1', '-');

        StringJoiner morseWords = new StringJoiner(MORSE_CODE_WORD_DELIMITER);

        // Loop until the entire input has been consumed.
        while (!unprocessedMorse.isEmpty()) {
            String word = findWord(unprocessedMorse, 10);
            morseWords.add(word);
            unprocessedMorse = unprocessedMorse.substring(word.replace(" ", "").length());
        }

        System.out.println(morseWords);
    }

    /**
     * Decodes the given input text and prints the output to the console.
     *
     * @param input The Morse code to decode.
     */
    public void decode(String input) {
        final String binary = this.morseCodeToBinaryString(input);
        final String output = AsciiUtility.toAsciiText(binary);
        System.out.println(output);
    }

    /**
     * Encodes the given input file into Morse code and writes the result to the specified output file.
     * <p>
     * This method implements a bounded parallel streaming pipeline to process the input file efficiently:
     * <ul>
     *     <li>Data is read from the input file in chunks (work units) of {@link #workUnitSize} bytes.
     *     <li>Each work unit is submitted to a fixed-size thread pool for parallel processing.
     *     <li>Each thread converts its work unit into Morse code.
     *     <li>Completed work units are stored temporarily in a small buffer until they can be written in
     *         the correct sequence to preserve the order of the input (work units are buffered until all preceding
     *         work units have been written).
     *     <li>The method writes output incrementally as work units complete, avoiding storing the entire
     *         output in memory.
     * </ul>
     * <p>
     * Concurrency and memory usage:
     * <ul>
     *     <li>The thread pool size and task queue capacity are bounded by {@link #threadPoolSize} and
     *         {@link #queueCapacity}, respectively, preventing unbounded memory growth even for very large input files.
     *     <li>The reading thread may execute tasks directly if the queue is full, providing backpressure and ensuring
     *         memory remains bounded.
     * </ul>
     *
     * @param input  The file to encode as Morse code.
     * @param output The file to write the Morse code output to. If the file exists, it will be truncated;
     *               if it does not exist, it will be created.
     * @implNote This method is designed for large files where reading the entire content into memory is impractical.
     *           It combines incremental reading, parallel processing, and ordered streaming output for
     *           efficient memory usage.
     */
    public void encode(File input, File output) throws IOException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                this.threadPoolSize,
                this.threadPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(this.queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        CompletionService<IndexedResult> completionService = new ExecutorCompletionService<>(executor);

        try (
                InputStream is = input.toURI().toURL().openStream();
                BufferedWriter writer = OutputUtility.createWriter(output)
        ) {

            byte[] buffer = new byte[this.workUnitSize];
            int readLength;
            int numWorkUnitsSubmitted = 0;

            Map<Integer, String> completedWorkUnits = new HashMap<>(); // Holds completed work units until they can be written (removed when written).

            int writeIndex = 0;

            // Submit a task for each "workUnitSize" bytes read-in from the file.
            while ((readLength = is.read(buffer, 0, this.workUnitSize)) > 0) {

                final byte[] workUnit = Arrays.copyOf(buffer, readLength);
                final int workUnitIndex = numWorkUnitsSubmitted++; // Keeps track of where this work unit fits in the sequence.
                final int workUnitLength = readLength;

                completionService.submit(() -> {

                    StringJoiner morseWords = new StringJoiner(MORSE_CODE_WORD_DELIMITER);

                    // Convert the work unit to a binary string and find an appropriate morse code mapping.
                    String binaryString = BinaryUtilities.byteArrayToBinaryString(workUnit, workUnitLength);
                    while (!binaryString.isEmpty()) {
                        String word = findWord(binaryString, 10);
                        morseWords.add(word);
                        binaryString = binaryString.substring(
                                word.replace(" ", "").length()
                        );
                    }

                    return new IndexedResult(workUnitIndex, morseWords.toString());
                });

                // Drain one completed work unit, if available.
                Future<IndexedResult> future = completionService.poll();
                if (future != null) {
                    IndexedResult completedWorkUnit = future.get();
                    completedWorkUnits.put(completedWorkUnit.index(), completedWorkUnit.value());

                    // Write any available contiguous work units.
                    while (completedWorkUnits.containsKey(writeIndex)) {
                        writer.write(completedWorkUnits.remove(writeIndex++));
                        writer.write(MORSE_CODE_WORD_DELIMITER);
                    }
                }
            }

            // Now loop until all remaining work units complete and have been written.
            while (writeIndex < numWorkUnitsSubmitted) {
                IndexedResult completedWorkUnit = completionService.take().get();
                completedWorkUnits.put(completedWorkUnit.index(), completedWorkUnit.value());

                // Write any available contiguous work units.
                while (completedWorkUnits.containsKey(writeIndex)) {
                    writer.write(completedWorkUnits.remove(writeIndex++));
                    if (writeIndex < numWorkUnitsSubmitted) { // Avoid extra delimiter after final work unit
                        writer.write(MORSE_CODE_WORD_DELIMITER);
                    }
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Decodes the given input file from Morse code into the original binary data and write the result to the
     * given output file.
     *
     * @param input  The file containing Morse code to be decoded.
     * @param output The file to write the output to. If the file exists, it will be truncated;
     *               if it does not exist, it will be created.
     */
    public void decode(File input, File output) throws IOException {
        final String morseCode = Files.readString(input.toPath());
        final String binary = this.morseCodeToBinaryString(morseCode);
        byte[] data = BinaryUtilities.binaryStringToByteArray(binary);

        if (output != null) {
            java.nio.file.Files.write(output.toPath(), data);
        } else if (AsciiUtility.isAsciiText(data)) {
            System.out.println(new String(data, StandardCharsets.UTF_8));
        } else {
            System.out.println(CANT_PRINT_OUTPUT_ERROR_MESSAGE);
        }
    }

    /**
     * Finds a word in Morse code that matches the start of (or the entire) input.
     * <p>
     * For example, if the input started with ".--", that could match the word "at" (which is ".- -" in morse).
     * In which case, the response would be ".- -".
     *
     * @param input                     A string consisting of only dots and dashes.
     * @param numMatchesBeforeSelection The number of matching words to find before selecting one. A large value will
     *                                  result in a better selection, but will result in worse performance.
     * @return A word that matches the start of the input, but with spaces at letter breaks.
     */
    private String findWord(String input, int numMatchesBeforeSelection) {
        if (numMatchesBeforeSelection <= 0) {
            throw new IllegalArgumentException(
                    "numMatchesBeforeSelection [" + numMatchesBeforeSelection + "]  must be greater than 0"
            );
        }
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Pick a random index in the morse dictionary to start looking for words that match.
        Random randomGen = new Random();
        int random = randomGen.nextInt(this.morseDictionary.size());

        final List<String> matchingWords = new ArrayList<>();

        // Loop through the dictionary looking for matching words.
        for (int i = 0; i < this.morseDictionary.size(); i++) {
            MorseDictionaryEntry entry = this.morseDictionary.get(random);

            if (input.startsWith(entry.getWordWithoutLetterBreaks())) {
                matchingWords.add(entry.getWord());
                // Break early if the requisite number of matches has been found.
                if (matchingWords.size() >= numMatchesBeforeSelection) {
                    break;
                }
            }
            random = (random + 1) % this.morseDictionary.size();
        }

        // Prefer the longest match (ties are broken randomly for variation)
        return matchingWords.stream()
                .min(Comparator.comparingInt(String::length)
                        .reversed()
                        .thenComparing(x -> ThreadLocalRandom.current().nextInt())
                )
                .orElse(findLetter(input)); // Find a matching letter if there were no matching words.
    }

    /**
     * Randomly finds a single letter in morse code that matches the start of (or the entire) input.
     * <p>
     * For example, if the input was ".--", that could match the letters or E (.), A (.-), or W (.--).
     *
     * @param input A string consisting of only dots and dashes.
     * @return A randomly selected letter that matches the start of the input.
     */
    private String findLetter(String input) {
        Node node = null;
        int maxLength = Math.min(input.length(), this.tree.getMaxDepth());

        // Loop until a valid morse encoding is randomly selected
        while (node == null || node.getEncoding() == null) {

            // Randomly select a character length for a morse encoded character.
            int length = Math.min(maxLength, 1 + (int) (Math.random() * this.tree.getMaxDepth()));

            // Starting at the current index, Check if the substring with that length is a valid encoding.
            node = this.tree.get(input.substring(0, length));
        }

        return node.getEncoding().getCode();
    }

    /**
     * Converts a string of morse code into a binary string.
     * Dots become zeros, dashes become ones, spaces and forward slashes are removed.
     *
     * @param morse The morse code string.
     * @return The matching binary string.
     */
    public String morseCodeToBinaryString(String morse) {
        return morse
                .replace('.', '0')
                .replace('-', '1')
                .replace(" ", "")
                .replace("/", "");
    }

}
