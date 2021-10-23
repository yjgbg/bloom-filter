package com.github.yjgbg.bloom.core;

import java.util.function.Function;

public interface HashingStrategy extends Function<byte[],long[]> {
	long[] apply(byte[] raw);
}
