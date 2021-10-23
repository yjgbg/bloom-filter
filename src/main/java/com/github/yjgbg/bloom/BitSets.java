package com.github.yjgbg.bloom;

import com.github.yjgbg.bloom.bitSet.ComposedBitSet;
import com.github.yjgbg.bloom.bitSet.LettuceBitSet;
import com.github.yjgbg.bloom.core.BitSet;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.stream.Stream;

public class BitSets {
	public static BitSet composedLettuceBitSet(long size, String key, RedisCommands<String, ?> commands) {
		final var mod = (size % LettuceBitSet.SIZE) == 0;
		final var nums0 = size / LettuceBitSet.SIZE;
		final var nums = mod ? nums0 : nums0 + 1;
		return Stream.iterate(0,i -> i + 1)
				.limit(nums).map(i -> key +"_"+i)
				.<BitSet>map(str -> LettuceBitSet.of(str,commands))
				.reduce(ComposedBitSet::new).orElseThrow();
	}
}
