package com.github.yjgbg.bloom.strategy;

import com.github.yjgbg.bloom.core.HashingStrategy;
import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class Murmur128HashingStrategy implements HashingStrategy {
	private final long m; // Number of bits in the filter
	private final int k; // Number of hash functions

	@Override
	public long[] apply(byte[] raw) {
		@SuppressWarnings("UnstableApiUsage")
		byte[] bytes = Hashing.murmur3_128().hashBytes(raw).asBytes();
		long hash1 = (bytes[0] & 0xFFL) << 56
				| (bytes[1] & 0xFFL) << 48
				| (bytes[2] & 0xFFL) << 40
				| (bytes[3] & 0xFFL) << 32
				| (bytes[4] & 0xFFL) << 24
				| (bytes[5] & 0xFFL) << 16
				| (bytes[6] & 0xFFL) << 8
				| (bytes[7] & 0xFFL);
		long hash2 = (bytes[8] & 0xFFL) << 56
				| (bytes[9]  & 0xFFL) << 48
				| (bytes[10] & 0xFFL) << 40
				| (bytes[11] & 0xFFL) << 32
				| (bytes[12] & 0xFFL) << 24
				| (bytes[13] & 0xFFL) << 16
				| (bytes[14] & 0xFFL) << 8
				| (bytes[15] & 0xFFL);
		long[] indexes = new long[k];
		long combinedHash = hash1;
		for (int i = 0; i < k; i++) {
			// Make the combined hash positive and indexable
			indexes[i] = Math.floorMod(combinedHash,m);
			combinedHash += hash2;
		}
		return indexes;
	}
}
