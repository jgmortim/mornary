package com.github.jgmortim.mornary.service;

import com.epic.morse.service.MorseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jgmortim.mornary.exception.InvalidBinaryException;
import com.github.jgmortim.mornary.model.BinaryTree;
import com.github.jgmortim.mornary.model.Encoding;
import com.github.jgmortim.mornary.model.MorseDictionaryEntry;
import com.github.jgmortim.mornary.model.Node;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
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
        URL dictionaryUrl = getClass().getResource("/English5000.txt");

        if (dictionaryUrl != null) {
            this.morseDictionary = Files.lines(Paths.get(dictionaryUrl.toURI()))
                    .map(MorseCode::convertToMorseCode)
                    .map(word -> new MorseDictionaryEntry(word, word.replace(" ", "")))
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Dictionary not found");
        }
    }


    /**
     * Initial working method. Reads the entire file into memory and then converts it to morse code.
     * Superseded by {@link #toMorseCode2(URL)}.
     *
     * TODO Delete when confident in approach.
     *
     * @param dataUrl URL of the file to encode.
     * @return A string of morse code.
     */
    public String toMorseCode(URL dataUrl) throws IOException {
        try (InputStream is = dataUrl.openStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            is.transferTo(baos);
            byte[] data = baos.toByteArray();
            StringBuilder binary = new StringBuilder(data.length * 8);
            for (byte b : data) {
                // Convert byte to int and mask with 0xFF to handle negative values correctly
                binary.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
            }
            return binaryToMorseCode(binary.toString(), 10);

        } catch (IOException e) {
            System.err.printf("Failed to read from %s: %s%n", dataUrl, e.getMessage());
            throw e;
        }
    }

    /**
     * Second working method; more elaborate. Reads data into a buffer for processing and reads more data as needed.
     * Superseded by {@link #toMorseCode3(URL)}.
     *
     * TODO Delete when confident in approach.
     *
     * @param dataUrl URL of the file to encode.
     * @return A string of morse code.
     */
    public String toMorseCode2(URL dataUrl) throws IOException {
        StringJoiner morseWords = new StringJoiner(MORSE_CODE_WORD_DELIMITER);
        final int bucketSize = 1024; // In Bytes

        try (InputStream is = dataUrl.openStream()) {

            // Read off the first "bucketSize" bytes to the data buffer (or less if the file is under "bucketSize" bytes).
            byte[] dataBuffer = new byte[bucketSize];
            int readLength = is.read(dataBuffer, 0, bucketSize);

            String binaryString = "";

            // Loop until all the data has been read into the buffer and the buffer is empty.
            while (readLength > 0 || !binaryString.isEmpty()) {

                // If data was read in, convert it to a String of 1s and 0s and append to the end of the binaryString
                if (readLength > 0) {
                    StringBuilder binary = byteArrayToBinaryString(dataBuffer, readLength);
                    binaryString += binary.toString();
                }

                // Convert the binaryString to valid Morse code
                while (!binaryString.isEmpty()) {
                    String word = findWord(binaryString, 10);
                    morseWords.add(word);

                    int bitsConsumed = word.replace(" ", "").length();

                    binaryString = binaryString.substring(bitsConsumed);

                    // If there are less than 100 bits in the binary string
                    // AND there is more data to be read in,
                    // break out of loop to read in more data
                    if (binaryString.length() < 100 && readLength > 0) {
                        break;
                    }
                }

                // Read another "bucketSize" bytes
                readLength = is.read(dataBuffer, 0, bucketSize);
            }

            return morseWords.toString();

        } catch (IOException e) {
            System.err.printf("Failed to read from %s: %s%n", dataUrl, e.getMessage());
            throw e;
        }
    }

    /**
     * Third working method; even more elaborate. Reads entire file into a 2D byte array. Each element in the array
     * contains "bucketSize" bytes, and each bucket is processed in parallel and then stitched together at the end
     *
     * @param dataUrl URL of the file to encode.
     * @return A string of morse code.
     */
    public String toMorseCode3(URL dataUrl) throws IOException, URISyntaxException {

        final int bucketSize = 1024; // In Bytes
        final int threadPoolSize = 10;

        long fileSize = Files.size(Paths.get(dataUrl.toURI()));
        int numberOfBuckets = (int) Math.ceil((double) fileSize / bucketSize);

        String[] morseSentences = new String[numberOfBuckets]; // Array to hold the results of each bucket

        try (InputStream is = dataUrl.openStream()) {

            byte[][] data = new byte[numberOfBuckets][bucketSize];

            int lastBucketDataSize = bucketSize; // Will likely be smaller than the size of the bucket.
            for (byte[] datum : data) {
                int readLength = is.read(datum, 0, bucketSize);
                if (readLength > 0 && readLength < bucketSize) {
                    lastBucketDataSize = readLength;
                }
            }
            final int lastBucketDataSizeFinal = lastBucketDataSize; // Needs to be final to use in the lambda below.

            // Create a thread pool with 1 thread for each bucket.
            ExecutorService executor = Executors.newFixedThreadPool(Math.min(numberOfBuckets, threadPoolSize));

            // Synchronize with a countdown latch.
            CountDownLatch latch = new CountDownLatch(data.length);

            System.out.println(numberOfBuckets);

            for (int i = 0; i < data.length; i++) { // For each bucket
                final int index = i;
                executor.execute(() -> {    // Send it to the thread pool for processing
                    final int dataSize = (index == data.length - 1 ? lastBucketDataSizeFinal : bucketSize);
                    // Convert the "bucketSize" bytes to a character string of dots and dashes
                    StringBuilder binary = byteArrayToBinaryString(data[index], dataSize);
                    String input = binary.toString();


                    StringJoiner morseWords = new StringJoiner(MORSE_CODE_WORD_DELIMITER);
                    // Convert the string to valid Morse code
                    while (!input.isEmpty()) {
                        String word = findWord(input, 10);
                        morseWords.add(word);
                        input = input.substring(word.replace(" ", "").length());
                    }
                    morseSentences[index] = morseWords.toString();
                    latch.countDown();
                });
            }

            // Wait for all threads to complete, then shutdown the thread pool.
            latch.await();
            executor.shutdown();

            return String.join(MORSE_CODE_WORD_DELIMITER, morseSentences);


        } catch (IOException e) {
            System.err.printf("Failed to read from %s: %s%n", dataUrl, e.getMessage());
            throw e;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Converts the first "actualSize" byte of a byte array into a binary string.
     *
     * @param data       The byte array
     * @param actualSize The actual size of the data in the array, regardless of the copacity of the array.
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
