package com.github.yjgbg.bloom.core;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.function.Function;

@RequiredArgsConstructor(staticName = "of")
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class GenericBloomFilter<A> implements BloomFilter<A> {
	BitSet bitSet;
	HashingStrategy hashingStrategy;
	Function<A, byte[]> toBytes;

	@Override
	public boolean test(A value) {
		return bitSet.test(hashingStrategy.apply(toBytes.apply(value)));
	}

	@Override
	public void put(A value) {
		bitSet.set(hashingStrategy.apply(toBytes.apply(value)));
	}

	@Override
	public void clear() {
		bitSet.clear();
	}

	@Override
	public long bitSize() {
		return bitSet.size();
	}
}
