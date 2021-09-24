package com.hypers.has.bf;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * BloomFilter
 *
 * <a href="https://en.wikipedia.org/wiki/Bloom_filter">Bloom Filter</a>
 * <br><br>
 * n Number of items in the filter <br>
 * p Probability of false positives, fraction between 0 and 1 or a number indicating 1-in-p <br>
 * m Number of bits in the filter <br>
 * k Number of hash functions <br>
 * <br>
 * n = ceil(m / (-k / log(1 - exp(log(p) / k))))<br>
 * p = pow(1 - exp(-k / (m / n)), k)<br>
 * m = ceil((n * log(p)) / log(1 / pow(2, log(2))))<br>
 * k = round((m / n) * log(2)) <br>
 * <br><br>
 *
 * @author Jasper Xu
 */
public interface BloomFilter<T> extends BloomFilterReader<T>, Serializable {

    /**
     * clear this BloomFilter.
     * all bits would be set to 0.
     */
    void clear();

    /**
     * return actual false positive probability of this BloomFilter.
     */
    double fpp();

    /**
     * add a element
     */
    void put(byte[] raw);

    /**
     * add a element
     */
    default void put(T element) {
        put(toBytes(element));
    }

    /**
     * transfer a element to bytes
     */
    default byte[] toBytes(T element) {
        return element.toString().getBytes(StandardCharsets.UTF_8);
    }

    default boolean contains(T element) { return contains(toBytes(element)); }


    interface BitMap extends Iterable<byte[]> {
        /**
         * return size of this BitMap in bits.
         * @return size
         */
        long size();

        /**
         * clear this BitMap.
         */
        void clear();

        /**
         * set the bit to 1 at certain index.
         * @param index index
         */
        void set(long index);

        /**
         * get the bit at certain index.
         * @param index index
         * @return true if the bit is 1, or false if 0.
         */
        boolean get(long index);

        /**
         * write the certain bytes with data.
         * @param i first index of byte
         * @param bytes data
         */
        void write(long i, byte[] bytes);
        /**
         * @param i first index of byte
         * @param bytes data
         */
        void read(long i, byte[] bytes);

        // default empty
        default void set(long[] indexes) {
            throw new RuntimeException("operation not supported!");
        }
        default boolean allMatch(long[] indexes) {
            throw new RuntimeException("operation not supported!");
        }
    }
    interface Strategy {
        long[] indexes(byte[] raw, Param param);
    }

    /**
     * <a href="https://en.wikipedia.org/wiki/Bloom_filter">Bloom Filter</a>
     * <br><br>
     * n Number of items in the filter <br>
     * p Probability of false positives, fraction between 0 and 1 or a number indicating 1-in-p <br>
     * m Number of bits in the filter <br>
     * k Number of hash functions <br>
     * <br>
     * n = ceil(m / (-k / log(1 - exp(log(p) / k))))<br>
     * p = pow(1 - exp(-k / (m / n)), k)<br>
     * m = ceil((n * log(p)) / log(1 / pow(2, log(2))))<br>
     * k = round((m / n) * log(2)) <br>
     * <br><br>
     *
     */
    class Param {

        /**
         * false positive probability
         */
        final double falsePositiveProbability;
        /**
         * num of bits
         */
        final long bitSize;
        /**
         * num of item
         */
        final long numOfItems;
        /**
         * num of Hash Functions
         */
        final int numOfHashFunctions;

        private Param(long numOfItems, double falsePositiveProbability) {
            this.falsePositiveProbability = falsePositiveProbability;
            this.numOfItems = numOfItems;
            this.bitSize = BloomFilterCalculator.roundBitSize(numOfItems, falsePositiveProbability);
            this.numOfHashFunctions = BloomFilterCalculator.numOfHashFunctions(this.bitSize, this.numOfItems);
        }

        private Param(long numOfItems, double falsePositiveProbability, long maxBitSize) {
            this.falsePositiveProbability = falsePositiveProbability;
            this.numOfItems = numOfItems;
            this.bitSize = java.lang.Math.min(BloomFilterCalculator.roundBitSize(numOfItems, falsePositiveProbability), maxBitSize);
            this.numOfHashFunctions = BloomFilterCalculator.numOfHashFunctions(this.bitSize, this.numOfItems);
        }

        public static Param of(long numOfItems, double falsePositiveProbability) {
            return new Param(numOfItems, falsePositiveProbability);
        }

        public static Param of(long numOfItems, double falsePositiveProbability, long maxBitSize) {
            return new Param(numOfItems, falsePositiveProbability, maxBitSize);
        }

        public double getFalsePositiveProbability() {
            return falsePositiveProbability;
        }

        public long getBitSize() {
            return bitSize;
        }

        public long getNumOfItems() {
            return numOfItems;
        }

        public int getNumOfHashFunctions() {
            return numOfHashFunctions;
        }
    }
}
