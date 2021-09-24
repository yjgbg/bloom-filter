package com.hypers.has.bf;

import com.hypers.has.bf.file.FileBloomFilter;
import com.hypers.has.bf.redis.LettuceRedisBloomFilter;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.UUID;

public class BloomFilterTest {

    TransferableBloomFilter<Object> fileBF;
    TransferableBloomFilter<Object> redisBF;


    @Before
    public void before() {
        BloomFilter.Param param = BloomFilter.Param.of(10_000_000L, 1e-4, 1L << 32);

        fileBF = FileBloomFilter.create(Path.of("/Users/jsprx/workspace/test/testbmp"), param, BloomFilterStrategies.MURMUR128_MITZ_64);
//        fileBF.clear();

        RedisClient client = RedisClient.create("redis://localhost");
        StatefulRedisConnection<String, String> conn =  client.connect();
        RedisCommands<String, String> commands = conn.sync();
        redisBF = LettuceRedisBloomFilter.create(commands, "testbmp", param, BloomFilterStrategies.MURMUR128_MITZ_64);
    }

    @Test
    public void test() {

        fileBF.put("1");
        fileBF.put("2");
        fileBF.put("3");
        fileBF.put("123");
        fileBF.put("321");
        fileBF.put("456");
        fileBF.put("654");
        fileBF.put("789");
        fileBF.put("987");

        assert fileBF.contains("1");
        assert fileBF.contains("2");
        assert fileBF.contains("3");
        long start = System.currentTimeMillis();
        redisBF.transferFrom(fileBF);
        System.out.println("transferring spend " + (System.currentTimeMillis() - start) + " ms");

        assert redisBF.contains("1");
        assert redisBF.contains("2");
        assert redisBF.contains("3");
        assert ! redisBF.contains("135");
        assert redisBF.contains("789");
        assert redisBF.contains("987");
    }


    @Test
    public void test1() {
        String o = "redistest";

        redisBF.put(o);

        assert redisBF.contains(o);

    }


    @Test
    public void test2() {

        int fpCount = 0;

        for (int i = 0; i < 1_000; i++) {
            fileBF.put("" + i);
        }

        for (int i = 0; i < 1_000; i++) {
            assert fileBF.contains("" + i);
        }

        for (int i = 0; i < 100_000; i++) {
            if (fileBF.contains(UUID.randomUUID())) {
                fpCount ++;
            }
        }

        System.out.println(fpCount);
    }



    @Test
    public void test3() {
        double fpp = 1e-4;
        long bitSize = Long.MAX_VALUE;
        System.out.println(BloomFilterCalculator.numOfItems(bitSize, fpp, 13));

    }



    @Test
    public void test4() {
//        fileBF.clear();
        fileBF.put("qwer");
        fileBF.put("asdf");
        fileBF.put("zxcv");

        assert fileBF.contains("qwer");
        assert ! fileBF.contains(UUID.randomUUID());

    }
}
