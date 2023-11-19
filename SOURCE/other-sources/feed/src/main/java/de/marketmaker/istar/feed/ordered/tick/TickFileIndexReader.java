/*
 * TickFileIndexReader.java
 *
 * Created on 22.11.12 10:56
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import de.marketmaker.istar.common.util.ByteString;

import static de.marketmaker.istar.feed.ordered.tick.TickFileIndexWriter.*;

/**
 * @author oflege
 */
public class TickFileIndexReader {

    public interface IndexHandler {
        void handle(ByteString key, long position, int length);
    }

    public static long readEntries(FileChannel fc, IndexHandler h) throws IOException {
        return new TickFileIndexReader(fc).readEntries(h);
    }

    private final FileChannel fc;

    private final ByteBuffer root;

    private final ByteBuffer bb;

    final long indexStart;

    TickFileIndexReader(FileChannel fc) throws IOException {
        this(fc, TickFileIndexWriter.NODE_SIZE);
    }

    TickFileIndexReader(FileChannel fc, int nodeSize) throws IOException {
        this.fc = fc;
        final long size = this.fc.size();

        this.bb = ByteBuffer.allocate(nodeSize).order(ByteOrder.LITTLE_ENDIAN);

        final ByteBuffer tmp = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        if (size - tmp.remaining() < bb.remaining()) {
            bb.limit((int) size - tmp.remaining());
        }

        fc.position(size - bb.remaining() - tmp.remaining());
        fc.read(bb);
        fc.read(tmp);
        tmp.flip();
        final long last = tmp.getLong();
        this.indexStart = TickFileIndexWriter.decodeIndexStart(last);
        assert indexStart < size : indexStart + ">=" + size;

        int finalNodeLength = TickFileIndexWriter.decodeRootNodeLength(last);
        assert finalNodeLength <= this.bb.capacity() : finalNodeLength + ">" + this.bb.capacity();

        this.bb.flip();
        this.bb.position(this.bb.remaining() - finalNodeLength);

        this.root = (ByteBuffer) ByteBuffer.allocate(bb.remaining())
                .order(ByteOrder.LITTLE_ENDIAN).put(this.bb).flip();
        this.bb.clear();
    }

    long readEntries(IndexHandler h) throws IOException {
        this.fc.position(this.indexStart);
        int head;
        do {
            head = loadBuffer();
            if (isLeafNode(head)) {
                while (this.bb.hasRemaining()) {
                    h.handle(ByteString.readFrom(this.bb, ByteString.LENGTH_ENCODING_BYTE),
                            this.bb.getLong(), this.bb.getInt());
                }
            }
        } while (isFullSizeNode(head));
        return this.indexStart;
    }

    /**
     * Compares key with bs. We cannot use <code>key.compareTo(bs)</code>, because the index is
     * created based on sorted keys WITH market, but contains keys WITHOUT market. As an example,
     * <code>"NZ50.NZX".compareTo("NZ50-C.NZX")</code> is &gt; 0, but
     * <code>"NZ50".compareTo("NZ50-C")</code> is &lt; 0. The important aspect is that we have to
     * check whether a char is less than <code>'.'</code> (the char that separates symbol and market)
     * and modify the result of this method accordingly. In other words, this method has to return
     * the same result as <em>key_with_market.compareTo(bs_with_market)</em> would.
     * @param key lookup key
     * @param bs key from index
     * @return 0 if key equals bs, -1/+1 if key can be found left/right of bs.
     */
    static int compareKeys(ByteString key, ByteString bs) {
        final int p = key.indexOfDifference(bs);
        if (p < 0) {
            return 0;
        }
        if (p == key.length() || key.charAt(p) == '.') {
            return (p == bs.length() || bs.charAt(p) < '.') ? 1 : -1;
        }
        if (p == bs.length() || bs.charAt(p) == '.') {
            return (key.charAt(p) < '.') ? -1 : 1;
        }
        return key.charAt(p) < bs.charAt(p) ? -1 : 1;
    }

    private boolean isFullSizeNode(int head) {
        return (head & FULL_SIZE_FLAG) != 0;
    }

    private boolean isLeafNode(int head) {
        return (head & INDEX_NODE_FLAG) == 0;
    }

    long[] find(ByteString key) throws IOException {
        this.bb.clear();
        this.bb.put(root.duplicate()).flip();
        int head = limitBuffer();
        while (true) {
            final boolean isLeaf = isLeafNode(head);
            if (isLeaf) {
                while (bb.hasRemaining()) {
                    final ByteString bs = ByteString.readFrom(bb, ByteString.LENGTH_ENCODING_BYTE);
                    final int cmp = compareKeys(key, bs);
                    if (cmp > 0) {
                        bb.position(bb.position() + 12);
                    }
                    else if (cmp == 0) {
                        return new long[] { bb.getLong(), bb.getInt() };
                    }
                    else {
                        return null;
                    }
                }
                return null;
            }
            else {
                head = loadBuffer(getNodeOffset(key));
            }
        }
    }

    int depth() throws IOException {
        int result = 1;
        this.bb.clear();
        this.bb.put(root.duplicate()).flip();
        int head = limitBuffer();
        while (!isLeafNode(head)) {
            result++;
            head = loadBuffer(bb.getInt());
        }
        return result;
    }

    private int loadBuffer(int nodeOffset) throws IOException {
        this.fc.position(this.indexStart + nodeOffset);
        return loadBuffer();
    }

    private int loadBuffer() throws IOException {
        this.bb.clear();
        this.fc.read(this.bb);
        this.bb.flip();
        return limitBuffer();
    }

    private int limitBuffer() {
        final int result = bb.getShort() & 0xFFFF;
        this.bb.limit(result & LIMIT_MASK);
        return result;
    }

    private int getNodeOffset(ByteString key) {
        int offset = bb.getInt();
        while (bb.hasRemaining()) {
            ByteString bs = ByteString.readFrom(bb, ByteString.LENGTH_ENCODING_BYTE);
            if (compareKeys(key, bs) < 0) {
                return offset;
            }
            offset = bb.getInt();
        }
        return offset;
    }
}
