/*
 * TickDeflater.java
 *
 * Created on 07.02.13 14:19
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;
import java.util.zip.GZIPOutputStream;

import de.marketmaker.istar.common.io.ByteBufferOutputStream;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldDataBuilder;

/**
 * Encapsulates how ticks are compressed using gzip.
 * @author oflege
 */
class TickDeflater {
    // A chunk in a file cannot be larger than this; address (40bit) and chunk length (24bit)
    // need to fit into a long. ticks for a single symbol may span several chunks, though
    private static final int MAX_LENGTH = 1 << 24; // 16MB

    private static final int MIN_SIZE_FOR_COMPRESSION = 64;

    // to optimize decompression, compressed data is stored in data chunks that, when uncompressed,
    // always fit into a buffer of this size.
    static final int BUFFER_SIZE = 65536;

    // per symbol
    private int numTickBytes = 0;

    // per symbol
    private int numChunks = 0;

    // per symbol and chunk
    private int chunkLength = 0;

    // current position in the output channel
    private long address;

    private long numBytesIn = 0;

    private long numBytesOut = 0;

    private final long baseAddress;

    // uncompressed ticks will be added to the buffer before they are compressed
    private final ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);

    private final ByteBufferOutputStream bbos = new ByteBufferOutputStream(BUFFER_SIZE);

    private final ByteBuffer writeBuffer
            = ByteBuffer.allocateDirect(MAX_LENGTH).order(ByteOrder.LITTLE_ENDIAN);

    private final WritableByteChannel out;

    TickDeflater(WritableByteChannel out, long address) {
        this.out = out;
        this.baseAddress = address;
        this.address = this.baseAddress;
    }

    long getNumBytesIn() {
        return numBytesIn;
    }

    long getNumBytesOut() {
        return numBytesOut;
    }

    int getNumTickBytes() {
        return numTickBytes;
    }

    long getFileAddress() {
        return TickWriter.toFileAddress(this.address, this.chunkLength);
    }

    /**
     * to be called before ticks for the next symbol are deflated
     */
    void reset() {
        this.bb.clear();
        this.numChunks = 0;
        this.numTickBytes = 0;
        this.chunkLength = 8; // pointer to previous address
    }

    void add(ByteBuffer src) throws IOException {
        if (bb.remaining() < src.remaining()) {
            flushCompressedTicks();
        }
        bb.put(src);
    }

    void add(byte[] tickFieldsWithFlags) throws IOException {
        int num = 1 + tickFieldsWithFlags.length;
        if (bb.remaining() < num) {
            flushCompressedTicks();
        }
        bb.put((byte) num);
        bb.put(tickFieldsWithFlags);
    }

    void add(TickDecompressor.Element e) throws IOException {
        add(e.getFlags(), e.getData());
    }

    void add(int flags, BufferFieldData data) throws IOException {
        final int num = 2 + data.size();
        incNumBytesIn(num);

        if (bb.remaining() < num) {
            flushCompressedTicks();
        }
        bb.put((byte) num);
        bb.put((byte) flags);
        bb.put(data.asBuffer());
    }

    void add(int flags, FieldDataBuilder builder) throws IOException {
        final int num = 2 + builder.length();
        // do not call incNumBytesIn(num) here, the real in bytes have been counted already

        if (bb.remaining() < num) {
            flushCompressedTicks();
        }
        this.bb.put((byte) num);
        this.bb.put((byte) flags);
        builder.getFrom0(this.bb);
    }

    void incNumBytesIn(int num) {
        this.numBytesIn += num;
    }

    /**
     * to be called after ticks for a symbol have been added
     */
    void flushCompressedTicks() throws IOException {
        this.bb.flip();
        try {
            write(compress());
        } finally {
            this.bb.clear();
        }
    }

    private ByteBuffer compress() throws IOException {
        if (this.bb.remaining() < MIN_SIZE_FOR_COMPRESSION) {
            return this.bb;
        }
        return doCompress();
    }

    private ByteBuffer doCompress() throws IOException {
        this.bbos.reset();
        try (OutputStream os = new GZIPOutputStream(this.bbos)) {
            os.write(this.bb.array(), 0, this.bb.limit());
        }
        return this.bbos.toBuffer();
    }

    private void write(ByteBuffer src) throws IOException {
        if (this.writeBuffer.remaining() < (12 + src.remaining())) {
            flushWriteBuffer();
        }
        if (this.numChunks == 0L) {
            this.address = this.baseAddress + this.numBytesOut;
            this.writeBuffer.putLong(0L);
            this.numBytesOut += 8;
            this.numChunks++;
        }
        if (this.chunkLength + src.remaining() > MAX_LENGTH) {
            this.writeBuffer.putLong(getFileAddress());
            this.address = this.baseAddress + this.numBytesOut;
            this.numBytesOut += 8;
            this.chunkLength = 8;
            this.numChunks++;
        }
        int n = 4 + src.remaining();
        this.numBytesOut += n;
        this.chunkLength += n;
        this.numTickBytes += n;
        this.writeBuffer.putInt((this.bb == src) ? src.remaining() : -src.remaining());
        this.writeBuffer.put(src);
        src.clear();
    }

    void flushWriteBuffer() throws IOException {
        this.writeBuffer.flip();
        if (this.writeBuffer.hasRemaining()
                && this.out != null /* null for TickCli#import dry-run */) {
            this.out.write(this.writeBuffer);
        }
        this.writeBuffer.clear();
    }
}
