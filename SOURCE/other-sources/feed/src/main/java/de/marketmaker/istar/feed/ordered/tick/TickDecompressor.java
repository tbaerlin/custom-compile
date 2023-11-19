/*
 * TickDecompressor.java
 *
 * Created on 16.11.12 13:36
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import org.iq80.snappy.Snappy;
import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldDataConverter;
import de.marketmaker.istar.feed.ordered.OrderedSnapRecord;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.RawTick;
import de.marketmaker.istar.feed.tick.TickCoder;
import de.marketmaker.istar.feed.tick.TickRecordImpl;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.common.util.ByteBufferUtils.duplicate;
import static de.marketmaker.istar.feed.FeedUpdateFlags.FLAGS_TICK_CORRECTION;
import static de.marketmaker.istar.feed.FeedUpdateFlags.FLAG_TICK_CORRECTION_DELETE;

/**
 * @author oflege
 */
public class TickDecompressor implements Iterable<TickDecompressor.Element> {

    private static final EnumSet<AbstractTickRecord.TickItem.Encoding> VALID_ENCODINGS = EnumSet.of(
            AbstractTickRecord.TickItem.Encoding.TICK3,
            AbstractTickRecord.TickItem.Encoding.TICKZ
    );

    private final byte[] data;

    private final AbstractTickRecord.TickItem.Encoding encoding;

    private final int length;

    public static final class Element {
        private final ByteBuffer bb;

        private final ByteBuffer bb2;

        private final BufferFieldData data = new BufferFieldData();

        private int flags;

        public Element(ByteBuffer bb, ByteBuffer bb2) {
            this.bb = bb;
            this.bb2 = bb2;
        }

        public String toString() {
            return new TickCli.LineBuilder().build(getFlags(), new BufferFieldData(this.data.asBuffer()));
        }

        private void reset(int position, int limit) {
            reset(this.bb, position, limit);
        }

        private void reset2(int position, int limit) {
            reset(this.bb2, position, limit);
        }

        private void reset(final ByteBuffer buffer, int position, int limit) {
            buffer.limit(limit).position(position);
            this.flags = buffer.get() & 0xFF;
            this.data.reset(buffer);
        }

        public BufferFieldData getData() {
            return this.data;
        }

        public int getFlags() {
            return this.flags;
        }

        public boolean hasFlag(int test) {
            return (this.flags & test) != 0;
        }
    }

    public static boolean canDecompress(TickRecordImpl.TickItem item) {
        return VALID_ENCODINGS.contains(item.getEncoding());
    }

    public TickDecompressor(TickRecordImpl.TickItem item) {
        this(item.getData(), item.getLength(), item.getEncoding());
    }

    public TickDecompressor(byte[] data, AbstractTickRecord.TickItem.Encoding encoding) {
        this(data, data.length, encoding);
    }

    public TickDecompressor(byte[] data, int length,
            AbstractTickRecord.TickItem.Encoding encoding) {
        if (!VALID_ENCODINGS.contains(encoding)) {
            throw new IllegalArgumentException(String.valueOf(encoding));
        }
        this.data = data;
        this.length = length;
        this.encoding = encoding;
    }

    public Iterator<RawTick> rawTickIterator(final boolean withAdditionalFields) {
        return new Iterator<RawTick>() {
            final Iterator<Element> it = iterator();

            final RawTick t = new RawTick();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public RawTick next() {
                final Element e = it.next();
                t.reset();
                this.t.setType(toRawTickType(e.flags));
                parseFields(e.getData());
                return this.t;
            }

            private void parseFields(BufferFieldData fd) {
                t.setMdpsTime(fd.getInt());
                List<SnapField> fields = null;
                for (int id = fd.readNext(); id != 0; id = fd.readNext()) {
                    SnapField sf = null;
                    switch (id) {
                        case VwdFieldOrder.ORDER_ADF_BEZAHLT:
                            long price = encodePrice(fd);
                            if (price != Long.MIN_VALUE) {
                                t.setPrice(price);
                                t.setPricePresent(true);
                            }
                            break;
                        case VwdFieldOrder.ORDER_ADF_GELD:
                            long bidPrice = encodePrice(fd);
                            if (bidPrice != Long.MIN_VALUE) {
                                t.setBidPrice(bidPrice);
                                t.setBidPresent(true);
                            }
                            break;
                        case VwdFieldOrder.ORDER_ADF_GELD_UMSATZ:
                            t.setBidVolume(fd.getInt());
                            break;
                        case VwdFieldOrder.ORDER_ADF_BRIEF:
                            long askPrice = encodePrice(fd);
                            if (askPrice != Long.MIN_VALUE) {
                                t.setAskPrice(askPrice);
                                t.setAskPresent(true);
                            }
                            break;
                        case VwdFieldOrder.ORDER_ADF_BRIEF_UMSATZ:
                            t.setAskVolume(fd.getInt());
                            break;
                        case VwdFieldOrder.ORDER_ADF_BEZAHLT_UMSATZ:
                            t.setVolume(fd.getInt());
                            break;
                        case VwdFieldOrder.ORDER_ADF_BEZAHLT_KURSZUSATZ:
                            t.setSupplement(OrderedSnapRecord.toString(fd.getBytes()));
                            break;
                        case VwdFieldOrder.ORDER_ADF_NOTIERUNGSART:
                            t.setNotierungsart(OrderedSnapRecord.toString(fd.getBytes()));
                            break;
                        case VwdFieldOrder.ORDER_ADF_RENDITE:
                            sf = toSnapField(fd);
                            if (sf != null) {
                                final BigDecimal bd = sf.getPrice();
                                long val = encodePrice(bd.unscaledValue().longValue(), -bd.scale());
                                if (val != Long.MIN_VALUE) {
                                    t.setYield(val);
                                }
                            }
                            break;
                        case VwdFieldOrder.ORDER_ADF_KASSA:
                            sf = toSnapField(fd);
                            t.setWithKassa(true);
                            break;
                        default:
                            if (withAdditionalFields) {
                                sf = toSnapField(fd);
                            }
                            else {
                                fd.skipCurrent();
                            }
                    }
                    if (sf != null && withAdditionalFields) {
                        if (fields == null) {
                            fields = new ArrayList<>(4);
                        }
                        fields.add(sf);
                    }
                }
                if (fields != null) {
                    t.setAdditionalFields(fields);
                }
            }

            private long encodePrice(BufferFieldData fd) {
                return encodePrice(fd.getInt(), fd.getByte());
            }

            private long encodePrice(long base, int scale) {
                try {
                    return PriceCoder.encode(base, scale);
                } catch (IllegalArgumentException e) {
                    return Long.MIN_VALUE;
                }
            }

            private SnapField toSnapField(BufferFieldData fd) {
                return FieldDataConverter.RAW.convert(fd);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private int toRawTickType(int flags) {
        int result = 0;
        if ((flags & FeedUpdateFlags.FLAG_WITH_TRADE) != 0) {
            result |= TickCoder.TYPE_TRADE;
        }
        if ((flags & FeedUpdateFlags.FLAG_WITH_ASK) != 0) {
            result |= TickCoder.TYPE_ASK;
        }
        if ((flags & FeedUpdateFlags.FLAG_WITH_BID) != 0) {
            result |= TickCoder.TYPE_BID;
        }
        return result;
    }

    private int uncompress(int num, ByteBuffer bb, ByteBuffer bb2) {
        final int result = doUncompress(num, bb, bb2);
//        System.out.println("uncompress " + num + " => " + result);
        return result;
    }

    private int doUncompress(int num, ByteBuffer bb, ByteBuffer bb2) {
        if (encoding == AbstractTickRecord.TickItem.Encoding.TICK3) {
            return Snappy.uncompress(bb.array(), bb.position(), num, bb2.array(), 0);
        }
        else if (encoding == AbstractTickRecord.TickItem.Encoding.TICKZ) {
            bb2.clear();
            try (GZIPInputStream gzis =
                         new GZIPInputStream(new ByteArrayInputStream(bb.array(), bb.position(), num))) {
                return FileCopyUtils.copy(gzis, new OutputStream() {
                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        bb2.put(b, off, len);
                    }

                    @Override
                    public void write(int b) throws IOException {
                        bb2.put((byte) b);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // cannot happen...
        throw new IllegalArgumentException(String.valueOf(encoding));
    }

    /**
     * @return iterator over valid ticks, i.e., ticks that have not been corrected and ticks
     * that were added by means of a correction. Ticks marked as deleted by correction will not
     * be returned.
     */
    @Override
    public Iterator<Element> iterator() {
        return iterator(false);
    }

    public Iterator<Element> iterator(final boolean withDeleted) {
        return new Iterator<Element>() {
            private final ByteBuffer bb = BufferFieldData.asBuffer(data, length);

            private final ByteBuffer bb2 = (ByteBuffer) BufferFieldData.asBuffer(new byte[
                    TickDeflater.BUFFER_SIZE]).flip();

            private final Element element = new Element(duplicate(bb), duplicate(bb2));

            private final int bbLimit = bb.limit();

            private boolean hasNext = true;

            private boolean hasAdvanced;

            {
                prepareNextChunk();
            }

            private void advance() {
                this.hasAdvanced = true;
                while (advanceNext()) {
                    if ((this.element.getFlags() & FLAGS_TICK_CORRECTION) == 0
                            || withDeleted || !this.element.hasFlag(FLAG_TICK_CORRECTION_DELETE)) {
                        return;
                    }
                }
                this.hasNext = false;
            }

            private boolean advanceNext() {
                if (this.bb2.hasRemaining()) {
                    nextFromBb2();
                    return true;
                }
                if (bb.hasRemaining()) {
                    nextFromBb();
                    return true;
                }
                else if (this.bb.position() < this.bbLimit) {
                    prepareNextChunk();
                    return advanceNext();
                }
                return false;
            }

            @Override
            public boolean hasNext() {
                if (!this.hasAdvanced) {
                    advance();
                }
                return this.hasNext;
            }

            @Override
            public Element next() {
                if (!this.hasAdvanced) {
                    advance();
                }
                if (!this.hasNext) {
                    throw new NoSuchElementException();
                }
                this.hasAdvanced = false;
                return this.element;
            }

            private Element nextFromBb() {
                final int end = bb.position() + (bb.get() & 0xFF);
                this.element.reset(bb.position(), end);
                this.bb.position(end);
                return this.element;
            }

            private Element nextFromBb2() {
                final int end = bb2.position() + (bb2.get() & 0xFF);
                this.element.reset2(bb2.position(), end);
                this.bb2.position(end);
                return this.element;
            }

            private void prepareNextChunk() {
                bb.limit(this.bbLimit);
                final int chunkLength = bb.getInt();

                if (chunkLength < 0) { // compressed
                    final int count = uncompress(-chunkLength, this.bb, this.bb2);
                    this.bb2.position(0).limit(count);
                    this.bb.limit(this.bb.position() - chunkLength).position(this.bb.limit());
                }
                else {
                    assert chunkLength > 0 : "Encountered chunkLength " + chunkLength + " " + bb.toString();
                    this.bb.limit(this.bb.position() + chunkLength);
                }
            }
        };
    }

    /**
     * @return returns ticks as (see {@link #iterator()}) does, but in reverse order, i.e.,
     * latest ticks first.
     */
    public Iterator<Element> reverseIterator() {
        return reverseIterator(false);
    }

    public Iterator<Element> reverseIterator(final boolean withDeleted) {
        return new Iterator<Element>() {
            private final ByteBuffer bb = BufferFieldData.asBuffer(data, length);

            private final ByteBuffer bb2 = (ByteBuffer) BufferFieldData.asBuffer(new byte[
                    TickDeflater.BUFFER_SIZE]).flip();

            private final Element element = new Element(duplicate(bb), duplicate(bb2));

            private boolean hasNext = true;

            private boolean hasAdvanced;

            /**
             * one element for each data chunk in bb, used as a stack
             */
            private final int[] chunkLengths;

            /**
             * pointer to top of {@link #chunkLengths} stack
             */
            private int chunkLengthIdx;

            /**
             * offset of the next chunk to be processed in bb
             */
            private int chunkOffset;

            /**
             * one element for each tick in the current chunk, used as a stack
             */
            private byte[] tickLengths = new byte[1024];

            /**
             * pointer to top of {@link #tickLengths} stack.
             */
            private int tickLengthIdx;

            {
                this.chunkLengths = initChunkLengths();
                this.chunkOffset = this.bb.limit();
                prepareNextChunk();
            }

            protected int[] initChunkLengths() {
                int[] result = new int[256];
                int i = 0;
                while (i < bb.limit()) {
                    final int len = bb.getInt(i);
                    if (this.chunkLengthIdx == result.length) {
                        result = Arrays.copyOf(result, result.length + 256);
                    }
                    result[this.chunkLengthIdx++] = len;
                    i += (4 + Math.abs(len));
                }
                return result;
            }

            private void advance() {
                this.hasAdvanced = true;
                while (advanceNext()) {
                    if ((this.element.getFlags() & FLAGS_TICK_CORRECTION) == 0
                            || withDeleted || !this.element.hasFlag(FLAG_TICK_CORRECTION_DELETE)) {
                        return;
                    }
                }
                this.hasNext = false;
            }

            private boolean advanceNext() {
                if (this.bb2.hasRemaining()) {
                    nextFromBb2();
                    return true;
                }
                if (this.bb.hasRemaining()) {
                    nextFromBb();
                    return true;
                }
                if (this.chunkLengthIdx > 0) {
                    prepareNextChunk();
                    return advanceNext();
                }
                return false;
            }

            @Override
            public boolean hasNext() {
                if (!this.hasAdvanced) {
                    advance();
                }
                return this.hasNext;
            }

            @Override
            public Element next() {
                if (!this.hasAdvanced) {
                    advance();
                }
                if (!this.hasNext) {
                    throw new NoSuchElementException();
                }
                this.hasAdvanced = false;
                return this.element;
            }

            private Element nextFromBb2() {
                int p = this.bb2.limit();
                final int len = this.tickLengths[--this.tickLengthIdx] & 0xFF;
                this.element.reset2(p - len + 1, p);
                this.bb2.limit(p - len);
                return this.element;
            }

            private Element nextFromBb() {
                int p = this.bb.limit();
                final int len = this.tickLengths[--this.tickLengthIdx] & 0xFF;
                this.element.reset(p - len + 1, p);
                this.bb.limit(p - len);
                return this.element;
            }

            private void prepareNextChunk() {
                int chunkLength = this.chunkLengths[--this.chunkLengthIdx];
                bb.limit(this.chunkOffset);
                this.chunkOffset -= Math.abs(chunkLength);
                bb.position(this.chunkOffset);
                this.chunkOffset -= 4;

                if (chunkLength < 0) { // compressed
                    final int count = uncompress(-chunkLength, this.bb, this.bb2);
                    this.bb2.position(0).limit(count);
                    this.bb.limit(this.bb.position());
                    initTickLengths(this.bb2);
                }
                else {
                    assert chunkLength > 0 : "Encountered chunkLength " + chunkLength + " " + bb.toString();
                    this.bb.limit(this.bb.position() + chunkLength);
                    initTickLengths(this.bb);
                }
            }

            private void initTickLengths(ByteBuffer buffer) {
                int p = buffer.position();
                this.tickLengthIdx = 0;
                while (p < buffer.limit()) {
                    final int len = buffer.get(p) & 0xFF;
                    if (this.tickLengthIdx == this.tickLengths.length) {
                        this.tickLengths = Arrays.copyOf(this.tickLengths, this.tickLengthIdx + 1024);
                    }
                    this.tickLengths[this.tickLengthIdx++] = (byte) len;
                    p += len;
                }
            }
        };
    }

    public static void main(String[] args) throws IOException {
        final byte[] bytes = Files.readAllBytes(new File("/Users/oflege/tmp/EURGBP.FXVWD.td3").toPath());
        final TickDecompressor td = new TickDecompressor(bytes, AbstractTickRecord.TickItem.Encoding.TICK3);
        for (int i = 0; i < 2; i++) {
            final long then = System.currentTimeMillis();
            int n = 0;

            final Iterator<Element> it = (i % 2 == 0) ? td.iterator(false) : td.reverseIterator(false);
            while (it.hasNext()) {
                final Element e = it.next();
                n++;
            }
            System.out.println(n + " " + (System.currentTimeMillis() - then) + "ms " + (i % 2 == 0 ? "I" : "R"));
        }
    }
}