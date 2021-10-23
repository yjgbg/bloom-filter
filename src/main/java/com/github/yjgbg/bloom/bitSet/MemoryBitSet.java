package com.github.yjgbg.bloom.bitSet;

import com.github.yjgbg.bloom.utils.BitUtils;
import com.github.yjgbg.bloom.core.BitSet;

import java.util.Arrays;

public class MemoryBitSet implements BitSet {
	private final byte[] values;
	private final long length;
	public MemoryBitSet(int m) {
		final var arrayLength0 = m /8;
		final var arrayLength = m %8 == 0 ? arrayLength0 : arrayLength0 + 1;
		values = new byte[arrayLength];
		Arrays.fill(values,Byte.MIN_VALUE);
		this.length = m;
	}

	@Override
	public boolean test(long[] indexes) {
		for (long index : indexes) {
			final var i = index >> 3;
			final var offset = index % 8;
			if (BitUtils.getBit(values[(int)i],(int)offset) <= 0)return false;
		}
		return true;
	}

	@Override
	public void set(long[] indexes) {
		for (long index : indexes) {
			final var i = index >> 3;
			final var offset = Math.floorMod(index, 8);
			values[(int)i] = BitUtils.setBit(values[(int)i], offset);
		}
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long size() {
		return length;
	}
}
