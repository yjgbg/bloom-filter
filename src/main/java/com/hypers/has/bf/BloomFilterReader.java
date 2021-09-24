package com.hypers.has.bf;

import java.util.function.Predicate;

public interface BloomFilterReader<T> extends Predicate<T> {

    boolean contains(T element);

    boolean contains(byte[] raw);

    @Override
    default boolean test(T t) { return contains(t); }
}
