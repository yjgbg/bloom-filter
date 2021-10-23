package com.github.yjgbg.bloom.core;

import java.util.function.Predicate;

public interface BitSet extends Predicate<long[]> {
	boolean test(long[] indexes);
	void set(long[] indexes);
	void clear();
	long size();
}
