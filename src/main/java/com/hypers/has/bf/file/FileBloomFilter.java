package com.hypers.has.bf.file;

import com.hypers.has.bf.BloomFilter;
import com.hypers.has.bf.TransferableBloomFilter;

import java.io.IOException;

import java.nio.file.Path;
import java.util.Iterator;

public class FileBloomFilter<T> implements BloomFilter<T>, TransferableBloomFilter<T> {


    public static <T> FileBloomFilter<T> create(Path path, BloomFilter.Param param, BloomFilter.Strategy strategy) {
        return new FileBloomFilter<>(path, param, strategy);
    }

    FileBloomFilter(Path path, BloomFilter.Param param, BloomFilter.Strategy strategy) {
        try {
            this.bits = new FileBitMap(path, param.getBitSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.param = param;
        this.strategy = strategy;
    }

    BloomFilter.BitMap   bits;
    BloomFilter.Param    param;
    BloomFilter.Strategy strategy;

    @Override
    public void clear() {
        bits.clear();
    }

    @Override
    public double fpp() {
        return param.getFalsePositiveProbability();
    }

    @Override
    public void put(byte[] raw) {
        long[] indexes = strategy.indexes(raw, param);
        for (long index : indexes) {
            bits.set(index);
        }
    }


    @Override
    public boolean contains(byte[] raw) {
        long[] indexes = strategy.indexes(raw, param);
        for (long index : indexes) {
            if (! bits.get(index)) return false;
        }
        return true;
    }


    @Override
    public Iterator<byte[]> iterator() {
        return bits.iterator();
    }

    @Override
    public void putBytes(long i, byte[] b) {
        bits.read(i, b);
    }

}
