package com.hypers.has.bf.redis;

import com.hypers.has.bf.BloomFilter;
import io.lettuce.core.BitFieldArgs;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.Iterator;
import java.util.List;

class LettuceRedisBitMap implements BloomFilter.BitMap {

    /**
     * redis key
     */
    final String identifier;

    final RedisCommands<String, ?> commands;
    /**
     * pre-defined size
     */
    final long size;

    /**
     * 512 Kb
     */
    int batch = 2 << 19;


    LettuceRedisBitMap(String identifier, RedisCommands<String, ?> commands, long size) {
        this.identifier = identifier;
        this.commands = commands;
        this.size = size;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void clear() {
        commands.del(identifier);
    }

    @Override
    public void set(long index) {
        commands.setbit(identifier, index, 1);
    }

    @Override
    public boolean get(long index) {
        return commands.getbit(identifier, index) > 0;
    }

    public void setByte(long i, byte b) {
        long index = i << 3;
        for (int j = 0; j < 8; j++) {
            commands.setbit(identifier, index + j, ((b >>> (7 - j)) & 0x1));
        }
    }

    public byte getByte(long i) {
        long index = i << 3;
        byte res = 0;

        for (int j = 0; j < 8; j++) {
            if (commands.getbit(identifier, index + j) > 0) {
                res = (byte) (res | (0x1 << (7 - j)));
            }
        }
        return res;
    }

    BitFieldArgs.BitFieldType oneBitType = BitFieldArgs.unsigned(1);
    BitFieldArgs.BitFieldType eightBitType = BitFieldArgs.signed(8);

    @Override
    public void set(long[] indexes) {
        BitFieldArgs args = new BitFieldArgs();
        for (long index : indexes) {
            args.set(oneBitType, Math.toIntExact(index), 1);
        }
        commands.bitfield(identifier, args);
    }

    @Override
    public boolean allMatch(long[] indexes) {
        BitFieldArgs args = new BitFieldArgs();
        for (long index : indexes) {
            args.get(oneBitType, Math.toIntExact(index));
        }
        return commands.bitfield(identifier, args).stream().allMatch(a -> a > 0);
    }


    /**
     * @param i     first index of byte
     * @param bytes data
     */
    @Override
    public void write(long i, byte[] bytes) {
        BitFieldArgs args = new BitFieldArgs();
        i = i << 3;
        for (int j = 0; j < bytes.length; j++) {
            byte b = bytes[j];
            if (b != 0) {
                args.set(eightBitType, Math.toIntExact(i), b);
            }
            i += 8;
            if (i % (1 << 20) == 0) {
                commands.bitfield(identifier, args);
                args = new BitFieldArgs();
            }
        }
        commands.bitfield(identifier, args);
    }

    @Override
    public void read(long i, byte[] bytes) {
        BitFieldArgs args = new BitFieldArgs();
        i = i << 3;
        for (int j = 0; j < bytes.length; j++) {
            args.get(eightBitType, Math.toIntExact(i));
            i += 8;
        }
        List<Long> res = commands.bitfield(identifier, args);
        int j = 0;
        for (long value : res) {
            bytes[j] = (byte) value;
        }
    }

    @Override
    public Iterator<byte[]> iterator() {
        return new Itr(batch);
    }

    private class Itr implements Iterator<byte[]> {
        int cursor = 0;       // index of next bytes to return
        int batch;            // batch size in bits

        Itr(int batch) {
            this.batch = batch;
        }

        @Override
        public boolean hasNext() {
            return size != cursor;
        }

        @Override
        public byte[] next() {
            BitFieldArgs args = new BitFieldArgs();
            int offset = batch >> 3; // in bits
            long bitCursor = (long) cursor << 3;
            for (int i = 0; i < batch; i += 8) {
                args.get(eightBitType, Math.toIntExact(bitCursor + i));
            }
            List<Long> values = commands.bitfield(identifier, args);
            byte[] res = new byte[offset];
            int i = 0;
            for (long v : values) {
                res[i++] = (byte) v;
            }
            cursor += offset;
            return res;
        }
    }
}
