package com.github.yjgbg.bloom;

import com.github.yjgbg.bloom.core.BloomFilter;

import java.nio.charset.StandardCharsets;

public class main {
	public static void main(String[] args) {
		final var bloom = BloomFilters.murmur128HashMemoryBloomFilter(
				1000, 100,(String string) -> string.getBytes(StandardCharsets.UTF_8));
	}

	public static void test(BloomFilter<String> bloom, String value) {
		final var before =bloom.test(value);
		bloom.put(value);
		final var after =bloom.test(value);
		if (!after || before) System.out.println(value);
	}
}
