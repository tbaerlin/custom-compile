/*
 * EodData.java
 *
 * Created on 22.01.13 15:52
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.read;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.feed.history.BufferedBytesTransporter;
import de.marketmaker.istar.feed.history.TransferTarget;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

/**
 * @author zzhao
 */
class EodData {

    private static final int BUCK_SIZE = Integer.parseInt(System.getProperty("BUCK_SIZE",
            String.valueOf(64 * 1024 * 1024)));

    private final LongArrayList quotes;

    private final LongArrayList offsetsAndLengths;

    private final LongArrayList dataOffsets;

    private final IntArrayList dataLengths;

    private final List<ByteBuffer> dataList;

    private final MyTarget target;

    private int dataListIndex;

    private int currentLength;

    EodData(int size) {
        this.quotes = new LongArrayList(size);
        this.offsetsAndLengths = new LongArrayList(size);
        this.dataOffsets = new LongArrayList();
        this.dataLengths = new IntArrayList();
        this.dataList = new ArrayList<>();
        this.dataListIndex = -1;
        this.currentLength = 0;
        this.target = new MyTarget();
        alloc();
    }

    void clear() {
        this.quotes.clear();
        this.offsetsAndLengths.clear();
        this.dataOffsets.clear();
        this.dataLengths.clear();
        this.dataListIndex = -1;
        this.currentLength = 0;
        alloc();
    }

    private void alloc() {
        this.dataListIndex++;
        this.currentLength = 0;
        if (this.dataList.size() > this.dataListIndex) {
            this.dataList.get(this.dataListIndex).clear();
        }
        else {
            this.dataList.add(ByteBuffer.allocate(BUCK_SIZE));
        }
    }

    int size() {
        return this.quotes.size();
    }

    void loadQuoteData(OffsetLengthCoder olCoder, long quote, long oal,
            BufferedBytesTransporter tran) throws IOException {
        this.quotes.add(quote);
        this.offsetsAndLengths.add(oal);
        final long offset = olCoder.decodeOffset(oal);
        final int len = olCoder.decodeLength(oal);

        if (this.currentLength + len > BUCK_SIZE) {
            this.dataOffsets.add(offset);
            this.dataLengths.add(this.currentLength);
            alloc();
        }
        tran.transferTo(offset, len, this.target.withBuffer(this.dataList.get(this.dataListIndex)));
        this.currentLength += len;
    }

    ByteBuffer loadData(OffsetLengthCoder olCoder, long quote) {
        final int idx = search(quote, this.quotes);
        if (idx < 0) {
            return EodUtil.EMPTY_BB;
        }
        else {
            return fromData(olCoder, idx);
        }
    }

    private ByteBuffer fromData(OffsetLengthCoder olCoder, int idx) {
        final long oal = this.offsetsAndLengths.getLong(idx);
        final long offset = olCoder.decodeOffset(oal);
        final int len = olCoder.decodeLength(oal);

        int index = search(offset, this.dataOffsets);
        if (index < 0) {
            index = -index - 1;
        }
        else {
            index += 1;
        }

        final ByteBuffer bb = this.dataList.get(index).asReadOnlyBuffer();
        bb.position(calcOffset(offset, index));
        bb.limit(bb.position() + len);
        return bb;
    }

    private int calcOffset(long offset, int index) {
        for (int i = 0; i < index; i++) {
            offset -= this.dataLengths.getInt(i);
        }
        return (int) offset;
    }

    private long offsets() {
        long ret = 0;
        final IntListIterator it = this.dataLengths.iterator();
        while (it.hasNext()) {
            ret += it.nextInt();
        }
        return ret;
    }

    private int search(long quote, LongArrayList ll) {
        int low = 0;
        int high = ll.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            final long midVal = ll.getLong(mid);
            if (midVal < quote)
                low = mid + 1;
            else if (midVal > quote)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    private static final class MyTarget implements TransferTarget {

        private ByteBuffer buf;

        private MyTarget withBuffer(ByteBuffer bb) {
            this.buf = bb;
            return this;
        }

        @Override
        public int transfer(ByteBuffer bb) throws IOException {
            final int remaining = bb.remaining();
            this.buf.put(bb);
            return remaining;
        }
    }

    void dumpIndex(PrintStream ps) {
        for (int i = 0; i < quotes.size(); i++) {
            Long qid = quotes.get(i);
            Long oal = this.offsetsAndLengths.get(i);
            ps.println(qid + ";" + oal);
        }
    }
}
