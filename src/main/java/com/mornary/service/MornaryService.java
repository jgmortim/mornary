package com.mornary.service;

import com.epic.morse.service.MorseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mornary.exception.InvalidBinaryException;
import com.mornary.exception.NotTextException;
import com.mornary.model.BinaryTree;
import com.mornary.model.Encoding;
import com.mornary.model.IndexedResult;
import com.mornary.model.MorseDictionaryEntry;
import com.mornary.model.Node;
import com.mornary.utility.AsciiUtility;
import com.mornary.utility.BinaryUtilities;
import com.mornary.utility.OutputUtility;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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

    private final BinaryTree tree;

    private final List<MorseDictionaryEntry> commonWordsDictionary;
    private final List<MorseDictionaryEntry> rareWordsDictionary;

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
        assert morseUrl != null;
        try (InputStream in = morseUrl.openStream()) {
            Encoding[] encodings = OBJECT_MAPPER.readValue(in, Encoding[].class);
            this.tree = new BinaryTree(encodings);
        }

        // Load in a dictionary file.
        try (
                InputStream commonWords = getClass().getResourceAsStream("/English5000.txt");
                InputStream rareWords = getClass().getResourceAsStream("/EnglishHugeAlpha.txt")
        ) {

            if (commonWords == null || rareWords == null) {
                throw new RuntimeException("Dictionary not found");
            }

            this.commonWordsDictionary = new BufferedReader(new InputStreamReader(commonWords))
                    .lines()
                    .map(MorseCode::convertToMorseCode)
                    .map(word -> new MorseDictionaryEntry(word, word.replace(" ", "")))
                    .distinct()
                    .collect(Collectors.toList());

            this.rareWordsDictionary = new BufferedReader(new InputStreamReader(rareWords))
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
     * @param input  The text to encode.
     * @param output The file to write the Morse code output to. If the file exists, it will be truncated; if it does not exist,
     *               it will be created. If null, then encoded data will be printed to the console.
     * @implNote This method is designed for small text inputs where there are no memory concerns with reading the entire input into
     *           memory and with writing the entire output in a single operation. For large inputs where memory use and processing
     *           speed need to be considered, {@link #encode(File, File)} should be used.
     */
    public void encode(String input, File output) throws IOException {
        final String binary = AsciiUtility.toAsciiBinary(input);
        if (!binary.matches("^[0-1]+$")) {
            throw new InvalidBinaryException(binary);
        }

        String unprocessedMorse = binary
                .replace('0', '.')
                .replace('1', '-');

        StringJoiner morseWords = new StringJoiner(MORSE_CODE_WORD_DELIMITER);

        // Loop until the entire input has been consumed.
        CircularFifoQueue<String> previousWords = new CircularFifoQueue<>(3);
        while (!unprocessedMorse.isEmpty()) {
            String word = findWord(unprocessedMorse, previousWords, 10);
            morseWords.add(word);
            unprocessedMorse = unprocessedMorse.substring(word.replace(" ", "").length());
            previousWords.add(word);
        }

        try (BufferedWriter writer = OutputUtility.createWriter(output)) {
            writer.write(morseWords.toString());
        }
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
     * Encodes the given input file into Morse code and writes the result to the specified output file,
     * or to the console if output is null.
     * <p>
     * This method implements a bounded parallel streaming pipeline to process the input file efficiently:
     * <ul>
     *     <li>Data is read from the input file in chunks (work units) of {@link #workUnitSize} bytes.
     *     <li>Each work unit is submitted to a fixed-size thread pool for parallel processing.
     *     <li>Each thread converts its work unit into Morse code.
     *     <li>Completed work units are stored temporarily in a small buffer until they can be written in the correct sequence to
     *         preserve the order of the input (work units are buffered until all preceding work units have been written).
     *     <li>The method writes output incrementally as work units complete, avoiding storing the entire output in memory.
     * </ul>
     * <p>
     * Concurrency and memory usage:
     * <ul>
     *     <li>The thread pool size and task queue capacity are bounded by {@link #threadPoolSize} and {@link #queueCapacity},
     *         respectively, preventing unbounded memory growth even for very large input files.
     *     <li>The reading thread may execute tasks directly if the queue is full, providing backpressure and ensuring memory
     *         remains bounded.
     * </ul>
     *
     * @param input  The file to encode as Morse code.
     * @param output The file to write the Morse code output to. If the file exists, it will be truncated; if it does not exist,
     *               it will be created. If null, then encoded data will be printed to the console.
     * @implNote This method is designed for large files where reading the entire content into memory is impractical. It combines
     *           incremental reading, parallel processing, and ordered streaming output for efficient memory usage. When these
     *           concerns do not exist, {@link #encode(String, File)} may be used instead as it has less overhead.
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

        CompletionService<IndexedResult<String>> completionService = new ExecutorCompletionService<>(executor);

        final long fileSize = input.length();
        final long totalWorkUnits = (long) Math.ceil((double) fileSize / this.workUnitSize);

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
                    String binaryString = BinaryUtilities.byteArrayToMorseBinaryString(workUnit, workUnitLength);
                    CircularFifoQueue<String> previousWords = new CircularFifoQueue<>(3);
                    while (!binaryString.isEmpty()) {
                        String word = findWord(binaryString, previousWords, 10);
                        morseWords.add(word);
                        binaryString = binaryString.substring(
                                word.replace(" ", "").length()
                        );
                        previousWords.add(word);
                    }

                    return new IndexedResult<>(workUnitIndex, morseWords.toString());
                });

                // Drain one completed work unit, if available.
                Future<IndexedResult<String>> future = completionService.poll();
                if (future != null) {
                    IndexedResult<String> completedWorkUnit = future.get();
                    completedWorkUnits.put(completedWorkUnit.index(), completedWorkUnit.value());

                    // Write any available contiguous work units.
                    while (completedWorkUnits.containsKey(writeIndex)) {
                        writer.write(completedWorkUnits.remove(writeIndex++));
                        writer.write(MORSE_CODE_WORD_DELIMITER);
                        if (output != null) { // If using a file output, print progress to console.
                            this.printProgress(writeIndex, totalWorkUnits);
                        }
                    }
                }
            }

            // Now loop until all remaining work units complete and have been written.
            while (writeIndex < numWorkUnitsSubmitted) {
                IndexedResult<String> completedWorkUnit = completionService.take().get();
                completedWorkUnits.put(completedWorkUnit.index(), completedWorkUnit.value());

                // Write any available contiguous work units.
                while (completedWorkUnits.containsKey(writeIndex)) {
                    writer.write(completedWorkUnits.remove(writeIndex++));
                    if (writeIndex < numWorkUnitsSubmitted) { // Avoid extra delimiter after final work unit
                        writer.write(MORSE_CODE_WORD_DELIMITER);
                    }
                    if (output != null) { // If using a file output, print progress to console.
                        this.printProgress(writeIndex, totalWorkUnits);
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
     * Finds a word in Morse code that matches the start of (or the entire) input.
     * <p>
     * For example, if the input started with ".--", that could match the word "at" (which is ".- -" in morse).
     * In which case, the response would be ".- -".
     *
     * @param input                     A string consisting of only dots and dashes.
     * @param previousWords             The N previously selected words. This should not be an exhaustive list.
     *                                  Used in determining a word's score.
     * @param numMatchesBeforeSelection The number of matching words to find before selecting one. A large value will
     *                                  result in a better selection, but will result in worse performance.
     * @return A word that matches the start of the input, but with spaces at letter breaks.
     */
    private String findWord(String input, CircularFifoQueue<String> previousWords, int numMatchesBeforeSelection) {
        if (numMatchesBeforeSelection <= 0) {
            throw new IllegalArgumentException(
                    "numMatchesBeforeSelection [" + numMatchesBeforeSelection + "]  must be greater than 0"
            );
        }
        if (input == null || input.isEmpty()) {
            return "";
        }

        final Set<String> matchingWords = new HashSet<>();

        // Loop through the common words dictionary looking for matching words.
        searchDictionary(this.commonWordsDictionary, input, matchingWords, numMatchesBeforeSelection);

        // If a no matching words were found in the common words dictionary, check the rare words dictionary
        if (matchingWords.isEmpty()) {
            searchDictionary(this.rareWordsDictionary, input, matchingWords, numMatchesBeforeSelection);
        }

        return matchingWords.stream()
                .max(Comparator.comparingInt(word -> scoreWord((String) word, previousWords))
                        .thenComparing(x -> ThreadLocalRandom.current().nextInt())
                )
                .orElse(findLetter(input)); // Find a matching letter if there were no matching words.
    }

    /**
     * Helper method for {@link #findWord(String, CircularFifoQueue, int)}.
     *
     * @param dictionary       The dictionary to search for words.
     * @param input            A string consisting of only dots and dashes.
     * @param matches          The list that matching words should be added to.
     * @param numMatchesToFind The max number of matching words to find before returning.
     */
    private void searchDictionary(List<MorseDictionaryEntry> dictionary, String input, Set<String> matches, int numMatchesToFind) {
        // Pick a random index in the dictionary to start looking for words that match.
        Random randomGen = new Random();
        int random = randomGen.nextInt(dictionary.size());

        // Loop through the dictionary looking for matching words.
        for (int i = 0; i < dictionary.size(); i++) {
            MorseDictionaryEntry entry = dictionary.get(random);

            if (input.startsWith(entry.getWordWithoutLetterBreaks())) {
                matches.add(entry.getWord());
                // Break early if the requisite number of matches has been found.
                if (matches.size() >= numMatchesToFind) {
                    break;
                }
            }
            random = (random + 1) % dictionary.size();
        }
    }

    /**
     * Score a given word so that it can be compared to other matching words.
     *
     * @param word          The word to score.
     * @param previousWords The N previously selected words. This should not be an exhaustive list. If word appears in the
     *                     previousWords, it will have a negative impact on the score.
     * @return The word score.
     */
    private static int scoreWord(String word, CircularFifoQueue<String> previousWords) {
        // Some file formates produce long sections of repeating bit patterns, this can result in the exact same word being
        // selected many times in a row. To reduce the likelihood of repeated words, we apply a penalty on word repeats.
        int previousWordPenalty = 0;
        for (int i = 0; i < previousWords.size(); i++) {
            previousWordPenalty += word.equals(previousWords.get(i)) ? 1 : 0;
        }
        return word.length() - previousWordPenalty;
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
