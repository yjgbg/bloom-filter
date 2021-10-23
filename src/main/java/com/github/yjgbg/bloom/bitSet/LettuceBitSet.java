package com.github.yjgbg.bloom.bitSet;

import com.github.yjgbg.bloom.core.BitSet;
import io.lettuce.core.BitFieldArgs;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class LettuceBitSet implements BitSet {
	public static long SIZE = 512L * 1024 * 1024 * 8;
	final String key;
	final RedisCommands<String, ?> commands;
	private final BitFieldArgs.BitFieldType oneBitType = BitFieldArgs.unsigned(1);

	@Override
	public boolean test(long[] indexes) {
		final var args = new BitFieldArgs();
		for (long index : indexes) {
			args.set(oneBitType, Math.toIntExact(index));
		}
		return commands.bitfield(key, args).stream().allMatch(it -> it > 0);
	}

	@Override
	public void set(long[] indexes) {
		final var args = new BitFieldArgs();
		for (long index : indexes) {
			args.set(oneBitType, Math.toIntExact(index), 1);
		}
		commands.bitfield(key, args);
	}

	@Override
	public void clear() {
		commands.del(key);
	}

	@Override
	public long size() {
		return SIZE;
	}
}
