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
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import org.iq80.snappy.Snappy;
import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickRecordImpl;

import static de.marketmaker.istar.common.util.ByteBufferUtils.duplicate;

/**
 * @author oflege
 */
public class DumpDecompressor implements Iterable<DumpDecompressor.Element> {

    private static final EnumSet<AbstractTickRecord.TickItem.Encoding> VALID_ENCODINGS = EnumSet.of(
            AbstractTickRecord.TickItem.Encoding.DUMP3,
            AbstractTickRecord.TickItem.Encoding.DUMPZ
    );

    private final byte[] data;

    private final AbstractTickRecord.TickItem.Encoding encoding;

    private final int length;

    public static final class Element {
        private final BufferFieldData data = new BufferFieldData();

        private int flags;

        private int timestamp;

        private int mdpsMsgType;

        private int mdpsKeyType;

        public String toString() {
            return ""; // TODO new TickCli.LineBuilder().build(getFlags(), new BufferFieldData(this.data.asBuffer()));
        }

        private void reset(final ByteBuffer buffer) {
            this.timestamp = buffer.getInt();
            this.mdpsMsgType = buffer.get() & 0xFF;
            this.mdpsKeyType = buffer.get() & 0xFF;
            this.flags = buffer.getShort() & 0xFFFF;
            this.data.reset(buffer);
        }

        public int getTimestamp() {
            return timestamp;
        }

        public int getMdpsMsgType() {
            return mdpsMsgType;
        }

        public int getMdpsKeyType() {
            return mdpsKeyType;
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

    public DumpDecompressor(TickRecordImpl.TickItem item) {
        this(item.getData(), item.getLength(), item.getEncoding());
    }

    public DumpDecompressor(byte[] data, AbstractTickRecord.TickItem.Encoding encoding) {
        this(data, data.length, encoding);
    }

    public DumpDecompressor(byte[] data, int length, AbstractTickRecord.TickItem.Encoding encoding) {
        if (!VALID_ENCODINGS.contains(encoding)) {
            throw new IllegalArgumentException(String.valueOf(encoding));
        }
        this.data = data;
        this.length = length;
        this.encoding = encoding;
    }

    /**
     * @return iterator over feed records represented as Element objects.
     */
    @Override
    public Iterator<Element> iterator() {
        return new Iterator<Element>() {
            private final Iterator<ByteBuffer> it = recordIterator(false);

            private final Element element = new Element();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Element next() {
                if (!it.hasNext()) {
                    throw new NoSuchElementException();
                }
                this.element.reset(it.next());
                return this.element;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Iterator<ByteBuffer> recordIterator(final boolean withLength) {
        return new Iterator<ByteBuffer>() {
            private final ByteBuffer bb = BufferFieldData.asBuffer(data, length);

            private final ByteBuffer bb2 = (ByteBuffer) BufferFieldData.asBuffer(new byte[
                    /* TODO TickDeflater.BUFFER_SIZE*/ 65536]).flip();

            private final ByteBuffer bbDup = duplicate(bb);

            private final ByteBuffer bb2Dup = duplicate(bb2);

            private ByteBuffer next = null;

            private final int bbLimit = bb.limit();

            private boolean hasAdvanced;

            {
                prepareNextChunk();
            }

            private void advance() {
                this.hasAdvanced = true;
                if (this.bb2.hasRemaining() || this.bb.position() < this.bbLimit) {
                    advanceNext();
                    return;
                }
                this.next = null;
            }

            private void advanceNext() {
                if (this.bb2.hasRemaining()) {
                    nextFrom(this.bb2, this.bb2Dup);
                }
                else if (bb.hasRemaining()) {
                    nextFrom(this.bb, this.bbDup);
                }
                else {
                    prepareNextChunk();
                    advanceNext();
                }
            }

            @Override
            public boolean hasNext() {
                if (!this.hasAdvanced) {
                    advance();
                }
                return this.next != null;
            }

            @Override
            public ByteBuffer next() {
                if (!this.hasAdvanced) {
                    advance();
                }
                if (this.next == null) {
                    throw new NoSuchElementException();
                }
                this.hasAdvanced = false;
                return this.next;
            }

            private void nextFrom(ByteBuffer src, ByteBuffer dup) {
                this.next = dup;
                this.next.clear();
                int length;
                if (withLength) {
                    this.next.position(src.position());
                    length = length(src);
                }
                else {
                    length = length(src);
                    this.next.position(src.position());
                }
                final int end = length + src.position();
                this.next.limit(end);
                src.position(end);
            }

            public int length(ByteBuffer bb) {
                final int n = bb.get();
                if (n < 0) {
                    return n & 0x7F;
                }
                return (n << 7) | (bb.get() & 0x7F);
            }

            private void prepareNextChunk() {
                bb.limit(this.bbLimit);
                final int chunkLength = bb.getInt();

                if (chunkLength < 0) { // compressed
                    final int count = uncompress(-chunkLength);
                    this.bb2.position(0).limit(count);
                    this.bb.limit(this.bb.position() - chunkLength).position(this.bb.limit());
                }
                else {
                    assert chunkLength > 0 : "Encountered chunkLength " + chunkLength + " " + bb.toString();
                    this.bb.limit(this.bb.position() + chunkLength);
                }
            }

            private int uncompress(int num) {
                if (encoding == AbstractTickRecord.TickItem.Encoding.DUMP3) {
                    return Snappy.uncompress(bb.array(), bb.position(), num, bb2.array(), 0);
                }
                else if (encoding == AbstractTickRecord.TickItem.Encoding.DUMPZ) {
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

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    public static void main(String[] args) throws IOException {
        DumpDecompressor dd = new DumpDecompressor(FileCopyUtils.copyToByteArray(new File("/Users/oflege/tmp/A0NEKF.HNV")), AbstractTickRecord.TickItem.Encoding.DUMPZ);
        for (Element e : dd) {
            System.out.println(TimeFormatter.formatSecondsInDay(DateTimeProvider.Timestamp.decodeTime(e.getTimestamp())));
        }
    }
}