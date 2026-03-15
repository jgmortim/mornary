package com.mornary.model;

/**
 * Bit reader.
 *
 * @author John Mortimore
 */
public final class BitReader {

    /**
     * The data in the reader.
     */
    private final byte[] data;

    /**
     * The total number of bits of data.
     */
    private final int totalBits;

    /**
     * Current position of the reader.
     */
    private int bitPos;

    /**
     * Constructs a new bit reader.
     *
     * @param data   An array data to be read
     * @param length The length of the data in the array, regardless of the size of the array.
     */
    public BitReader(byte[] data, int length) {
        this.data = data;
        this.totalBits = length * 8;
        this.bitPos = 0;
    }

    /**
     * Returns true if the reader has not advanced to the end of the data.
     *
     * @return True if there is data in the reader that has not been consumed.
     */
    public boolean hasRemaining() {
        return bitPos < totalBits;
    }

    /**
     * Returns the number of bits of unconsumed data in the reader.
     *
     * @return The number of bits of data in the reader that has not been consumed.
     */
    public int remainingBits() {
        return totalBits - bitPos;
    }

    /**
     * Returns the value of the bit at the specified offset from the current cursor position.
     * <p>
     * The offset is measured in bits relative to the current {@link #bitPos} and does not modify the cursor state. This allows
     * callers to "peek" ahead in the underlying byte array without advancing the read position.
     * <p>
     * Bits are read in big-endian order within each byte (most significant bit first). For example, an offset of {@code 0} returns
     * the bit at the current cursor position, {@code 1} returns the next bit, and so on.
     *
     * @param offset The number of bits ahead of the current cursor position to read.
     * @return {@code 0} if the bit is unset, or {@code 1} if the bit is set.
     * @throws ArrayIndexOutOfBoundsException if the calculated bit index exceeds the bounds of the underlying byte array.
     */
    public int getBit(int offset) {
        int index = bitPos + offset;
        int byteIndex = index >> 3;
        int bitOffset = 7 - (index & 7);
        return (data[byteIndex] >> bitOffset) & 1;
    }

    /**
     * Advances the reader by the given number of bits.
     *
     * @param bits The number of bits to advance the cursor by.
     */
    public void advance(int bits) {
        bitPos += bits;
    }
}
