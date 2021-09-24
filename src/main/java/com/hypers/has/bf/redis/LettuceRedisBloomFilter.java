package com.hypers.has.bf.redis;

import com.hypers.has.bf.BloomFilter;
import com.hypers.has.bf.TransferableBloomFilter;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.Iterator;


/**
 *
 * @param <T>
 */
public class LettuceRedisBloomFilter<T> implements BloomFilter<T>, TransferableBloomFilter<T> {


    /**
     * create a instance of lettuce redis bloom filter.
     * @param commands redis commands instance.
     * @param identifier Key of bitmap.
     * @param param param for bloom filter.
     * @param strategy strategies of generating the k * log(M) bits required for an element
     *                 to be mapped to a BloomFilter of M bits and k hash functions.
     * @return Bloom Filter instance implemented by redis bitmap.
     */
    public static <T> LettuceRedisBloomFilter<T> create(RedisCommands<String, String> commands,
                                                        String identifier,
                                                        Param param,
                                                        BloomFilter.Strategy strategy) {

        return new LettuceRedisBloomFilter<>(commands, identifier, param, strategy);
    }


    /**
     * strategies of generating the k * log(M) bits required for an element
     * to be mapped to a BloomFilter of M bits and k hash functions.
     */
    final BloomFilter.Strategy strategy;
    /**
     * param of bloom filter
     */
    final Param param;

//    /**
//     * identifier for redis bitmap
//     */
//    String bitmapIdentifier;

    /**
     * lettuce redis client
     */
    BloomFilter.BitMap bits;



    private LettuceRedisBloomFilter(RedisCommands<String, ?> commands,
                                    String bitmapIdentifier,
                                    BloomFilter.Param param,
                                    BloomFilter.Strategy strategy) {
        this.bits = new LettuceRedisBitMap(bitmapIdentifier, commands, param.getBitSize());
        this.param = param;
        this.strategy = strategy;
    }


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
        return bits.allMatch(indexes);
    }

    @Override
    public Iterator<byte[]> iterator() {
        return bits.iterator();
    }

    @Override
    public void putBytes(long i, byte[] b) {
        bits.write(i, b);
    }


}
