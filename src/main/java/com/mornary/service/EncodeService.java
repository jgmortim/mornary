package com.mornary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mornary.model.EncodingBinaryTree;
import com.mornary.model.BitReader;
import com.mornary.model.MorseTrie;
import com.mornary.model.MorseTrieNode;
import com.mornary.model.OperationSize;
import com.mornary.model.WeightedDictionary;
import com.mornary.model.Encoding;
import com.mornary.model.IndexedResult;
import com.mornary.model.TextSegment;
import com.mornary.model.EncodingNode;
import com.mornary.model.Match;
import com.mornary.model.WorkUnit;
import com.mornary.utility.MorseUtility;
import com.mornary.utility.OutputUtility;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

/**
 * Mornary encoding service.
 *
 * @author John Mortimore
 */
public class EncodeService {

    private static final String MORSE_CODE_WORD_DELIMITER = " / ";

    private final EncodingBinaryTree singleCharacterTree;

    private static final WeightedDictionary DICT_FIVE_GRAM = new WeightedDictionary("/5grams_english.txt", 1.5);
    private static final WeightedDictionary DICT_FOUR_GRAM = new WeightedDictionary("/4grams_english.txt", 1.1);
    private static final WeightedDictionary DICT_COMMON = new WeightedDictionary("/English5000.txt", 1.0);    // Top 5000 common English words
    private static final WeightedDictionary DICT_THREE_GRAM = new WeightedDictionary("/3grams_english.txt", 1.0);
    private static final WeightedDictionary DICT_TWO_GRAM = new WeightedDictionary("/2grams_english.txt", .9);  // 5000 English 2grams
    private static final WeightedDictionary DICT_RARE = new WeightedDictionary("/EnglishHugeAlpha.txt", .7); // Hugh English dictionary

    static final List<WeightedDictionary> DICTIONARIES = List.of(
        DICT_FIVE_GRAM,
        DICT_FOUR_GRAM,
        DICT_COMMON,
        DICT_THREE_GRAM,
        DICT_TWO_GRAM,
        DICT_RARE
    );

    static final List<WeightedDictionary> DICTIONARIES_REDUCED_SET = List.of(
        DICT_COMMON
    );

    private final MorseTrie morseTrie = new MorseTrie();

    private final PrintService printService;

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
    public EncodeService(int workUnitSize, int threadPoolSize, boolean lowMemory) throws IOException {
        this.workUnitSize = workUnitSize;
        this.threadPoolSize = threadPoolSize;
        this.queueCapacity = threadPoolSize + 10;

        // Load in the binary tree.
        URL morseUrl = getClass().getResource("/morsecode.json");
        assert morseUrl != null;
        try (InputStream in = morseUrl.openStream()) {
            Encoding[] encodings = OBJECT_MAPPER.readValue(in, Encoding[].class);
            this.singleCharacterTree = new EncodingBinaryTree(encodings);
        }

        // Load in dictionary files.
        List<WeightedDictionary> dictionaries = lowMemory ? DICTIONARIES_REDUCED_SET :  DICTIONARIES;

        for (WeightedDictionary weightedDictionary : dictionaries) {
            try (
                InputStream is = getClass().getResourceAsStream(weightedDictionary.filename())
            ) {
                if (is == null) {
                    throw new RuntimeException("Dictionary not found: " +  weightedDictionary.filename());
                }

                new BufferedReader(new InputStreamReader(is))
                    .lines()
                    .map(englishText -> {
                        String morse = MorseUtility.toMorseCode(englishText);
                        return new TextSegment(englishText, morse, weightedDictionary.scoreMultiplier());
                    })
                    .distinct()
                    .forEach(this.morseTrie::insert);

            } catch (IOException e) {
                throw new RuntimeException("Failed to load dictionary", e);
            }
        }

        this.printService = new PrintService();
    }

    /**
     * Encodes the given input text as Morse code and prints the output to the console.
     *
     * @param input  The text to encode.
     * @param output The file to write the Morse code output to. If the file exists, it will be truncated; if it does not exist,
     *               it will be created. If null, then encoded data will be printed to the console.
     * @implNote This method is designed for small text inputs where there are no memory concerns with reading the entire input into
     *           memory and with writing the entire output in a single operation. For large inputs where memory use and processing
     *           speed need to be considered, {@link #encode(File, File)} should be used.
     */
    public void encode(String input, File output) throws IOException {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);

        WorkUnit workUnit = new WorkUnit(data, data.length, 0);

        String encodedWorkUnit = this.encodeWorkUnit(workUnit, OperationSize.SMALL);

        try (BufferedWriter writer = OutputUtility.createWriter(output)) {
            writer.write(encodedWorkUnit);
            if (output == null) { // Write an extra line separator for console output.
                writer.newLine();
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
        final long fileSize = input.length();
        final long totalWorkUnits = (long) Math.ceil((double) fileSize / this.workUnitSize);
        final OperationSize operationSize = OperationSize.getOperationSize(totalWorkUnits);
        final boolean printingProgress = output != null; // Progress updates are printed to the console only when output is written to a file.

        final int actualNumberOfThreads = Math.toIntExact(Math.min(totalWorkUnits, this.threadPoolSize));

        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            actualNumberOfThreads, actualNumberOfThreads,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(this.queueCapacity),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        final CompletionService<IndexedResult<String>> completionService = new ExecutorCompletionService<>(executor);

        try (
                InputStream is = input.toURI().toURL().openStream();
                BufferedWriter writer = OutputUtility.createWriter(output)
        ) {

            int readIndex = 0;
            int writeIndex = 0;
            int readLength;
            byte[] readBuffer = new byte[this.workUnitSize];
            final Map<Integer, String> writeBuffer = new HashMap<>(); // Holds completed work units until they can be written (removed when written).

            // Submit a task for each "workUnitSize" bytes read-in from the file.
            while ((readLength = is.read(readBuffer, 0, this.workUnitSize)) > 0) {

                WorkUnit workUnit = new WorkUnit(readBuffer, readLength, readIndex++);

                completionService.submit(() -> { // Submit the work unit.
                    String encodedWorkUnit = encodeWorkUnit(workUnit, operationSize);
                    return new IndexedResult<>(workUnit.getIndex(), encodedWorkUnit);
                });

                // Drain one completed work unit, if available.
                Future<IndexedResult<String>> future = completionService.poll();
                if (future != null) {
                    IndexedResult<String> completedWorkUnit = future.get();
                    writeBuffer.put(completedWorkUnit.index(), completedWorkUnit.value());

                    // Write any available contiguous work units.
                    writeIndex = writeCompletedWorkUnits(writeBuffer, writeIndex, writer, totalWorkUnits, printingProgress);
                }
            }

            // Now loop until all remaining work units complete and have been written.
            while (writeIndex < readIndex) {
                IndexedResult<String> completedWorkUnit = completionService.take().get();
                writeBuffer.put(completedWorkUnit.index(), completedWorkUnit.value());

                writeIndex = writeCompletedWorkUnits(writeBuffer, writeIndex, writer, totalWorkUnits, printingProgress);
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
     * Encodes a single work unit into Morse code.
     *
     * @param workUnit      A single work unit to be encoded into Morse.
     * @param operationSize The size of the overarching operation. Determines the algorithm used to encode the work unit.
     * @return The encoded work unit.
     */
    private String encodeWorkUnit(WorkUnit workUnit, OperationSize operationSize) {
        StringJoiner morseTextSegments = new StringJoiner(MORSE_CODE_WORD_DELIMITER);

        BitReader bitReader = workUnit.getBitReader();

        // Track the last 3 selected text segments for scoring purposes.
        CircularFifoQueue<String> previousTextSegments = new CircularFifoQueue<>(3);
        while (bitReader.hasRemaining()) {
            TextSegment text = findText(workUnit, previousTextSegments, operationSize);
            morseTextSegments.add(text.getMorse());
            bitReader.advance(text.getBitLength());
            previousTextSegments.add(text.getEnglish());
        }
        return morseTextSegments.toString();
    }

    /**
     * Finds a text segment that matches the start of (or the entire) bit pattern at the current index in the work unit.
     * In the event that multiple matches are found, the one with the highest score will be returned.
     * <p>
     * For example, if the input started with <code>011</code>, the morse pattern would be <code>.--</code>.
     * And that could match the word "at" (which is <code>.- -</code> in Morse).
     * <p>
     * Helper method for {@link #encodeWorkUnit(WorkUnit, OperationSize)}.
     *
     * @param workUnit             Work unit containing the input data and a bit reader.
     * @param previousTextSegments The N previously selected text segments in English. This should not be an exhaustive list.
     *                             Used in determining a text segment's score.
     * @param operationSize        The size of the overarching operation.
     * @return A text segment that matches the start of the input.
     */
    private TextSegment findText(WorkUnit workUnit, CircularFifoQueue<String> previousTextSegments, OperationSize operationSize) {

        final Set<Match> matchingTextSegments = searchTrie(workUnit, previousTextSegments, operationSize);

        return matchingTextSegments.stream()
            .max(Comparator.comparingDouble(Match::score)
                .thenComparing(x -> ThreadLocalRandom.current().nextInt())
            )
            .orElse(findLetter(workUnit)) // Find a matching letter if there were no matching text segments.
            .entry();
    }

    /**
     * Searches the trie for text segments that match the start of (or the entire) bit pattern at the current index in the work unit.
     * <p>
     * Helper method for {@link #findText(WorkUnit, CircularFifoQueue, OperationSize)}.
     *
     * @param workUnit             Work unit containing the input data and a bit reader.
     * @param previousTextSegments The N previously selected text segments. This should not be an exhaustive list.
     *                             Used in determining a text segment's score.
     * @param operationSize        The size of the over arching operation.
     * @return The set of matches.
     */
    private Set<Match> searchTrie(WorkUnit workUnit, CircularFifoQueue<String> previousTextSegments, OperationSize operationSize) {
        final Set<Match> matchingTextSegments = new HashSet<>();

        MorseTrieNode node = this.morseTrie.getRoot();
        int maxDepth = workUnit.getBitReader().remainingBits();
        for (int i = 0; i < maxDepth; i++) {
            // Break early if we reach a leaf or the requisite number of matches has been found.
            if (node == null || matchingTextSegments.size() >= operationSize.matchTarget) {
                break;
            }
            for (TextSegment textSegment : node.getTextSegments()) {
                final double score = scoreTextSegment(textSegment, previousTextSegments);
                matchingTextSegments.add(new Match(textSegment, score));
            }

            int bit = workUnit.getBitReader().getBit(i);
            node = (bit == 0) ? node.dot : node.dash;
        }
        return matchingTextSegments;
    }

    /**
     * Randomly finds a single letter in morse code that matches the start of (or the entire) input.
     * <p>
     * For example, if the input was ".--", that could match the letters or E (.), A (.-), or W (.--).
     *
     * @param workUnit Work unit containing the input data and a bit reader.
     * @return A randomly selected letter that matches the start of the input.
     */
    private Match findLetter(WorkUnit workUnit) {
        EncodingNode node = null;
        int maxLength = Math.min(workUnit.getBitReader().remainingBits(), this.singleCharacterTree.getMaxDepth());

        // Loop until a valid morse encoding is randomly selected
        while (node == null || node.getEncoding() == null) {

            // Randomly select a character length for a morse encoded character.
            int length = Math.min(maxLength, 1 + (int) (Math.random() * this.singleCharacterTree.getMaxDepth()));

            StringBuilder morseBuilder = new StringBuilder(length);

            for (int i = 0; i < length; i++) {
                int bit = workUnit.getBitReader().getBit(i);
                morseBuilder.append(bit == 0 ? '.' : '-');
            }

            String morsePrefix = morseBuilder.toString();

            node = this.singleCharacterTree.get(morsePrefix);
        }
        String morse = node.getEncoding().getCode();
        TextSegment entry = new TextSegment("", morse, 1.0);

        return new Match(entry, 0);
    }

    /**
     * Score a given text segment so that it can be compared to other matching text segments.
     *
     * @param textSegment          The text segment to score.
     * @param previousTextSegments The N previously selected text segments. This should not be an exhaustive list.
     *                             If text segment appears in the previousTextSegments, it will have a negative impact on the score.
     * @return The text segment's score.
     */
    private static double scoreTextSegment(TextSegment textSegment, CircularFifoQueue<String> previousTextSegments) {
        // Some file formats produce long sections of repeating bit patterns, this can result in the exact same word being
        // selected many times in a row. To reduce the likelihood of repeated words, we apply a penalty on word repeats.
        double previousTextMultiplier = 1.0;
        for (int i = 0; i < previousTextSegments.size(); i++) {
            previousTextMultiplier -= textSegment.getEnglish().equals(previousTextSegments.get(i)) ? 0.2 : 0.0;
        }

        double acronymMultiplier = textSegment.getEnglish().toLowerCase().matches(".*[aeiou].*") ? 1.0 : 0.5;

        return textSegment.getNumberOfLetters() * previousTextMultiplier * textSegment.getScoreMultiplier() * acronymMultiplier;
    }

    /**
     * Write any available contiguous work units from the write buffer to the writer. Checks for a completed work unit in the write
     * buffer with the current write index as its key. If the work unit is in the buffer, it is removed from the buffer and written
     * to the writer. The write index is incremented and this processes loops until it runs out of sequential work units to write.
     *
     * @param writeBuffer      Buffer containing completed work units.
     * @param writeIndex       The current write index.
     * @param writer           The writer to use.
     * @param totalWorkUnits   Total number of work units in the operation.
     * @param printingProgress True if progress percentage should be printed to the console.
     * @return The new write index.
     */
    private int writeCompletedWorkUnits(Map<Integer, String> writeBuffer, int writeIndex,
                                        BufferedWriter writer, long totalWorkUnits, boolean printingProgress) throws IOException {
        while (writeBuffer.containsKey(writeIndex)) {
            writer.write(writeBuffer.remove(writeIndex++));
            if (writeIndex < totalWorkUnits) { // Avoid extra delimiter after final work unit.
                writer.write(MORSE_CODE_WORD_DELIMITER);
            }
            if (printingProgress) {
                this.printService.printProgress(writeIndex, totalWorkUnits);
            }
        }
        return writeIndex;
    }

}
