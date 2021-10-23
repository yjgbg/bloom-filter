package com.github.yjgbg.bloom.core;

import java.util.function.Predicate;

public interface BloomFilter<A> extends Predicate<A> {
	boolean test(A a);

	void put(A a);

	void clear();

	long bitSize();
}
