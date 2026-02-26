package com.github.jgmortim.mornary.service;

import com.epic.morse.service.MorseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jgmortim.mornary.exception.InvalidBinaryException;
import com.github.jgmortim.mornary.model.BinaryTree;
import com.github.jgmortim.mornary.model.Encoding;
import com.github.jgmortim.mornary.model.IndexedResult;
import com.github.jgmortim.mornary.model.MorseDictionaryEntry;
import com.github.jgmortim.mornary.model.Node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

    private final BinaryTree tree;

    private final List<MorseDictionaryEntry> morseDictionary;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Constructs the MornaryService.
     */
    public MornaryService() throws IOException, URISyntaxException {
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
     *
     * @param dataUrl
     * @param output
     * @return
     */
    public void toMorseCode7(URL dataUrl, URL output) throws IOException, URISyntaxException {

        final int workUnitSize = 1024; // Number of bytes of input to be processed per thread task.
        final int threadPoolSize = 10;
        final int queueCapacity = threadPoolSize + 10;

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                threadPoolSize,
                threadPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        CompletionService<IndexedResult> completionService = new ExecutorCompletionService<>(executor);

        try (
                InputStream is = dataUrl.openStream();
                BufferedWriter writer = Files.newBufferedWriter(
                        Paths.get(output.toURI()),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                )
        ) {

            byte[] buffer = new byte[workUnitSize];
            int readLength;
            int numWorkUnitsSubmitted = 0;

            Map<Integer, String> completedWorkUnits = new HashMap<>(); // Holds completed work units until they can be written (removed when written).
            
            int writeIndex = 0;

            // Submit a task for each "workUnitSize" bytes read-in from the file.
            while ((readLength = is.read(buffer, 0, workUnitSize)) > 0) {

                final byte[] workUnit = Arrays.copyOf(buffer, readLength);
                final int workUnitIndex = numWorkUnitsSubmitted++; // Keeps track of where this work unit fits in the sequence.
                final int workUnitLength = readLength;

                completionService.submit(() -> {

                    StringJoiner morseWords = new StringJoiner(MORSE_CODE_WORD_DELIMITER);

                    // Convert the work unit to a binary string and find an appropriate morse code mapping.
                    String input = byteArrayToBinaryString(workUnit, workUnitLength).toString();
                    while (!input.isEmpty()) {
                        String word = findWord(input, 10);
                        morseWords.add(word);
                        input = input.substring(
                                word.replace(" ", "").length()
                        );
                    }

                    return new IndexedResult(workUnitIndex, morseWords.toString());
                });

                // Drain one completed work unit, if available.
                Future<IndexedResult> future = completionService.poll();
                if (future != null) {
                    IndexedResult completedWorkUnit = future.get();
                    completedWorkUnits.put(completedWorkUnit.getIndex(), completedWorkUnit.getValue());

                    // Write any available contiguous work units.
                    while (completedWorkUnits.containsKey(writeIndex)) {
                        writer.write(completedWorkUnits.remove(writeIndex++));
                        writer.write(MORSE_CODE_WORD_DELIMITER);
                    }
                }
            }

            // Now loop until all remaining work units complete and have been written.
            while ( writeIndex < numWorkUnitsSubmitted) {
                IndexedResult completedWorkUnit = completionService.take().get();
                completedWorkUnits.put(completedWorkUnit.getIndex(), completedWorkUnit.getValue());

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
     * Converts the first "actualSize" byte of a byte array into a binary string.
     *
     * @param data       The byte array.
     * @param actualSize The actual size of the data in the array, regardless of the capacity of the array.
     * @return The binary string.
     */
    private static StringBuilder byteArrayToBinaryString(byte[] data, int actualSize) {
        StringBuilder binary = new StringBuilder(actualSize * 8);
        for (int i = 0; i < actualSize; i++) {
            byte b = data[i];
            // Convert byte to int and mask with 0xFF to handle negative values correctly
            binary.append(
                    String.format("%8s", Integer.toBinaryString(b & 0xFF))
                            .replace(' ', '0')
                            .replace('0', '.')
                            .replace('1', '-')
            );
        }
        return binary;
    }

    /**
     * Converts a binary string to valid Morse code.
     *
     * @param binary                    The binary string to convert.
     * @param numMatchesBeforeSelection The number of matching words to find before selecting one. A larger value will
     *                                  result in a better selection, but will result in worse performance.
     * @return A valid string of Morse code.
     */
    public String binaryToMorseCode(String binary, int numMatchesBeforeSelection) {
        if (!binary.matches("^[0-1]+$")) {
            throw new InvalidBinaryException(binary);
        }

        String input = binary
                .replace('0', '.')
                .replace('1', '-');

        StringJoiner morseWords = new StringJoiner(MORSE_CODE_WORD_DELIMITER);

        // Loop until the entire input has been consumed.
        while (!input.isEmpty()) {
            String word = findWord(input, numMatchesBeforeSelection);
            morseWords.add(word);
            input = input.substring(word.replace(" ", "").length());
        }

        return morseWords.toString();
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
     * Converts a string of morse code into binary.
     * Dots become zeros, dashes become ones, spaces and forward slashes are removed.
     *
     * @param morse The morse code string.
     * @return The matching binary string.
     */
    public String morseCodeToBinary(String morse) {
        return morse
                .replace('.', '0')
                .replace('-', '1')
                .replace(" ", "")
                .replace("/", "");
    }

}
