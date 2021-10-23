package com.github.yjgbg.bloom;

import com.github.yjgbg.bloom.bitSet.MemoryBitSet;
import com.github.yjgbg.bloom.core.GenericBloomFilter;
import com.github.yjgbg.bloom.strategy.Murmur128HashingStrategy;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.function.Function;

import static java.lang.Math.*;

public class BloomFilters {

	/**
	 * <a href="https://hur.st/bloomfilter">布隆过滤器计算器</a>
	 * @param p        允许的错误率
	 * @param n        Expected number of items in th，误判率越低，因为n和m会用于计算hash函数的个数
	 * @param toBytes a -> bytes[]
	 * @param <A> 目标类型
	 * @return
	 */
	static <A> GenericBloomFilter<A> murmur128ComposedLettuceHashBloomFilter(
			String redisKey, RedisCommands<String, ?> commands,long n,double p, Function<A, byte[]> toBytes) {
		final double m0 = (n * log(p)) / log(1 / pow(2, log(2)));
		final double k0 = (m0/ n) * log(2);
		final double k0s = Math.floor(k0);// 向下取整
		final double k0b = Math.ceil(k0); // 向上取整
		final double m0s = k0s / log(2) * n;
		final double m0b = k0b / log(2) * n;
		final long m = (long)ceil(min(m0s,m0b));
		final var k = (int)((m/n) * log(2));
		final var fpp =  pow(1 - exp(-k / ((double)m / n)), k);
		System.out.println("fpp="+fpp);
		return GenericBloomFilter.of(
				BitSets.composedLettuceBitSet(m,redisKey,commands),
				Murmur128HashingStrategy.of(m, Math.max(k,1)),
				toBytes);
	}

	static <A> GenericBloomFilter<A> murmur128HashMemoryBloomFilter(int m,long n, Function<A, byte[]> toBytes) {
		final var k = (int) round(((double) m / n) * log(2));
		return GenericBloomFilter.of(new MemoryBitSet(m), Murmur128HashingStrategy.of(m,k),toBytes);
	}
}
// 有公式： p = pow(1 - exp(-k / (m / n)), k)
// 当n,p确定时，可以由该公式推导出一个k(自变量)和m(因变量)的函数，并画出对应的函数图像。求：在图像上方(在n为给定的值的前提下，在图像上方意味着p小于给定的值)，
// 且m,k为整数的所有点中的最低点的坐标（横纵坐标应该都是一个由n和p组成的式子）