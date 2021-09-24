# Bloom Filter


## TODO

1. `FileBloomFilter` 并发问题.
2. 实现异步的基于 Redis 的 `BloomFilter` 实现(`AsyncRedisBloomFilter`).
3. 优化导入 `LettuceRedisBloomFilter` 速率.
4. ？ 是否用一个接口规定 `BloomFilter` 的元素类型，目前直接 `toString().getBytes()`.

