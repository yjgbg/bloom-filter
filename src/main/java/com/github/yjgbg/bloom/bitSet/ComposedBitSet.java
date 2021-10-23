package com.github.yjgbg.bloom.bitSet;

import com.github.yjgbg.bloom.core.BitSet;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public class ComposedBitSet<A extends BitSet,B extends BitSet> implements BitSet {
	private final A bitSet0;
	private final B bitSet1;

	@Override
	public boolean test(long[] indexes) {
		final var bitSet0Size = bitSet0.size();
		final var test0 = bitSet0.test(Arrays.stream(indexes).filter(it -> it < bitSet0Size).toArray());
		if (!test0)return false;
		return bitSet1.test(Arrays.stream(indexes).filter(it -> it >= bitSet0Size).map(it -> it - bitSet0Size).toArray());
	}

	@Override
	public void set(long[] indexes) {
		final var bitSet0Size = bitSet0.size();
		bitSet0.set(Arrays.stream(indexes).filter(it -> it < bitSet0Size).toArray());
		bitSet1.set(Arrays.stream(indexes).filter(it -> it >= bitSet0Size).map(it -> it - bitSet0Size).toArray());
	}

	@Override
	public void clear() {
		bitSet0.clear();
		bitSet1.clear();
	}

	@Override
	public long size() {
		return bitSet0.size() + bitSet1.size();
	}
}
