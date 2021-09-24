package com.hypers.has.bf;

public interface TransferableBloomFilter<T> extends BloomFilter<T>, Iterable<byte[]> {
    default void transferFrom(TransferableBloomFilter<T> src) {
        long i = 0;
        for (byte[] b : src) {
            putBytes(i, b);
            i += b.length;
        }
    }

    /**
     * @param i first index of byte of bitmap
     */
    void putBytes(long i, byte[] b);

}
