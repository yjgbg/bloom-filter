package com.hypers.has.bf.file;

import com.hypers.has.bf.BloomFilter;
import com.hypers.has.bf.util.BitUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

class FileBitMap implements BloomFilter.BitMap {

    Path path;

    FileChannel r;
    FileChannel w;
    // size of bits.
    long size;

    int batch = 1 << 20;

    FileBitMap(Path path) {
        File f = path.toFile();
        if (!f.exists()) {
            throw new RuntimeException(path + " not exists.");
        }
        this.path = path;
        try {
            r = FileChannel.open(path, StandardOpenOption.READ);
            w = FileChannel.open(path, StandardOpenOption.WRITE);
        } catch (IOException ignored) {
        }
        size = f.length() << 3;
    }

    FileBitMap(Path path, long size) throws IOException {
        if ((size & 0x7) > 0) {
            throw new IllegalArgumentException("size must be a multiple of 8.");
        }
        this.size = size;
        File f = path.toFile();
        if (! f.exists()) {
            f.createNewFile();
        }
        this.path = path;
        try {
            r = FileChannel.open(path, StandardOpenOption.READ);
            w = FileChannel.open(path, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void clear() {
        File file = path.toFile();
        if (file.delete()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void set(long index) {
        try {
            byte b;
            long i = index >> 3;

            if (r.size() < i) {
                b = 0;
            } else {
                r.position(i);
                ByteBuffer bf = ByteBuffer.allocate(1);
                r.read(bf);
                b = bf.array()[0];
            }

            long remainder = index & 0x7;
            b = BitUtils.setBit(b, (int) remainder);
            w.position(i);
            w.write(ByteBuffer.wrap(new byte[]{b}));
            if (i >= size) {
                size = r.size();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean get(long index) {
        boolean res;
        try {
            byte b;
            long i = index >> 3;
            long remainder = index & 0x7;

            ByteBuffer bf = ByteBuffer.allocate(1);
            r.position(i);
            r.read(bf);
            b = bf.array()[0];
            res = BitUtils.getBit(b, (int) remainder) > 0;
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(long i, byte[] bytes) {
        try {
            w.position(i);
            w.write(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void read(long i, byte[] bytes) {
        try {
            r.position(i);
            ByteBuffer bf = ByteBuffer.allocate(bytes.length);
            r.read(bf);
            System.arraycopy(bf.array(), 0, bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<byte[]> iterator() {
        return new Itr(batch);
    }

    private class Itr implements Iterator<byte[]> {
        int cursor = 0;       // index of next byte to return
        int batch;            // batch size in bits

        Itr(int batch) {
            this.batch = batch;
        }

        @Override
        public boolean hasNext() {
            return size > cursor;
        }

        @Override
        public byte[] next() {
            try {
                r.position(cursor);
                int offset = batch >> 3; // in bytes
                ByteBuffer buffer = ByteBuffer.allocate(offset);
                r.read(buffer);
                cursor += offset;
                return buffer.array();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
