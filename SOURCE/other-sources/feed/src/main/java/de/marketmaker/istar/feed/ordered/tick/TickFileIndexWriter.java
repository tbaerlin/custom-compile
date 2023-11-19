/*
 * TickFileIndex.java
 *
 * Created on 22.11.12 07:52
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;

/**
 * Creates a B*-Structure and stores it at the end of a tick file. Each node in the index is
 * {@value #NODE_SIZE} bytes long, except for the last leaf node and the following last
 * index node(s), which may be smaller.<br>
 * Each node starts with a short that encodes two flags (index node or leaf, full-size or not) and
 * the actual length of data in the node.<br>
 * Since all keys in the file belong to the same market, the market name (including the leading dot)
 * will be stripped from keys that appear in the index
 * (i.e., vwdcode <code>710000.ETR</code> is indexed as <code>710000</code>).
 *
 * @author oflege
 */
class TickFileIndexWriter {
    static final int NODE_SIZE = 0x2000;

    static final int INDEX_NODE_FLAG = 0x8000;

    static final int FULL_SIZE_FLAG = 0x4000;

    static final int LIMIT_MASK = 0x3fff;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int nodeSize;

    private final ByteBuffer[] bbs = new ByteBuffer[8];

    private final int[] numNodes = new int[8];

    private final long indexStart;

    private int pos = 0;

    private final WritableByteChannel channel;

    private ByteString marketName;

    private ByteString previousVwdcode = ByteString.EMPTY;

    private static final ByteString DOT = new ByteString(".");

    TickFileIndexWriter(WritableByteChannel channel, long indexStart) {
        this(NODE_SIZE, channel, indexStart);
    }

    TickFileIndexWriter(int nodeSize, WritableByteChannel channel, long indexStart) {
        if (!channel.isOpen()) {
            throw new IllegalStateException();
        }
        this.nodeSize = nodeSize;
        this.indexStart = indexStart;
        this.channel = channel;
    }

    void append(List<FeedData> elements, int day) throws IOException {
        marketName = elements.get(0).getMarket().getName().prepend(DOT);

        this.bbs[0] = createBuffer();

        for (FeedData fd : elements) {
            int length;
            long offset;
            synchronized (fd) {
                final OrderedTickData td = ((OrderedFeedData) fd).getOrderedTickData();
                length = td.getLength(day);
                if (length == 0) {
                    continue;
                } else if (length >= 0x6FFF_FFFF) { // 256 MiB or less before end of range (roughly the range: 1.75 - 2GiB)
                    this.logger.warn("<append> " + fd + " close to max size of signed int: " + length
                            + ". Headroom: " + (Integer.MAX_VALUE - length) + "bytes");
                } else if (length < 0) {
                    this.logger.error("<append> " + fd + " exceeds signed int by "
                            + (Integer.toUnsignedLong(length) - Integer.MAX_VALUE) + " bytes. Skipping.");
                    continue;
                }
                offset = td.getStoreAddress(day);
            }

            if (MemoryTickStore.isMemoryAddress(offset)) {
                this.logger.error("<append> " + fd + " with memory address " + Long.toHexString(offset));
                continue;
            }

            final ByteString vwdcode = fd.getVwdcode();
            assert previousVwdcode.compareTo(vwdcode) < 0
                    : ("keys not sorted: " + previousVwdcode + " > " + vwdcode);
            previousVwdcode = vwdcode;

            final ByteString key = removeMarket(vwdcode);

            int keyLength = key.length();
            if (bbs[0].remaining() < (keyLength + 1 + 8 + 4)) {
                final int leafPos = appendNode(0, true);
                addIndex(1, leafPos, key);
            }
            writeTo(0, key);
            bbs[0].putLong(offset);
            bbs[0].putInt(length);
        }

        int nodePos = pos;
        for (int i = 0; i < bbs.length; i++) {
            if (bbs[i] == null) {
                break;
            }
            if (bbs[i].position() == 2) {
                continue;
            }
            if (i > 0) {
                bbs[i].putInt(nodePos);
            }
            nodePos = appendNode(i, false);
        }
        bbs[0].clear();
        bbs[0].putLong(encodeLast(nodePos)).flip();
        this.channel.write(bbs[0]);
    }

    private long encodeLast(int nodePos) {
        return indexStart << 16 | (this.pos - nodePos);
    }

    static long decodeIndexStart(long last) {
        return last >> 16;
    }

    static int decodeRootNodeLength(long last) {
        return (int)(last & 0xFFFF);
    }

    private ByteString removeMarket(ByteString vwdcode) {
        if (vwdcode.endsWith(this.marketName)) {
            return vwdcode.substring(0, vwdcode.length() - this.marketName.length());
        }
        final int i = vwdcode.indexOf('.');
        return vwdcode.replace(i, i + this.marketName.length(), ByteString.EMPTY);
    }

    private void writeTo(int i, ByteString key) {
        key.writeTo(bbs[i], ByteString.LENGTH_ENCODING_BYTE);
    }

    private ByteBuffer createBuffer() {
        return (ByteBuffer) ByteBuffer.allocate(nodeSize).order(ByteOrder.LITTLE_ENDIAN).position(2);
    }

    private void addIndex(int i, int leftPos, ByteString vwdcode) throws IOException {
        if (this.bbs[i] == null) {
            this.bbs[i] = createBuffer();
        }
        if (this.bbs[i].remaining() < (4 + 1 + vwdcode.length() + 4)) {
            bbs[i].putInt(leftPos);
            final int indexPos = appendNode(i, true);
            addIndex(i + 1, indexPos, vwdcode);
        }
        else {
            this.bbs[i].putInt(leftPos);
            writeTo(i, vwdcode);
        }
    }

    int depth() {
        for (int i = 1; i < bbs.length; i++) {
            if (bbs[i] == null) {
                return i;
            }
        }
        throw new IllegalStateException();
    }

    int[] getNumNodes() {
        return Arrays.copyOfRange(this.numNodes, 0, depth());
    }

    private int appendNode(final int i, boolean fullSize) throws IOException {
        this.numNodes[i]++;

        final ByteBuffer bb = bbs[i];
        bb.flip();

/*
        if (i > 0) {
            final ByteBuffer tmp = bb.duplicate().order(ByteOrder.LITTLE_ENDIAN);
            tmp.position(2);
            System.out.printf("%1d %4d ", i, pos);
            System.out.print("|" + tmp.getInt());
            while (tmp.hasRemaining()) {
                System.out.print("|" + ByteString.readFrom(tmp, ByteString.LENGTH_ENCODING_BYTE));
                System.out.print("|" + tmp.getInt());
            }
            System.out.println("|");
        }
*/

        int flags = (i > 0 ? INDEX_NODE_FLAG : 0) + (fullSize ? FULL_SIZE_FLAG : 0);
        bb.putShort(0, (short) (flags + bb.remaining()));
        if (fullSize) {
            bb.limit(bb.capacity());
        }
        int result = pos;
        this.pos += bb.remaining();
        this.channel.write(bb);
        bb.clear().position(2);
        return result;
    }
}
