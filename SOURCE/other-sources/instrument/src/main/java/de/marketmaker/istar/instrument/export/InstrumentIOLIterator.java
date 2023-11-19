/*
 * InstrumentIndexInterator.java
 *
 * Created on 19.04.2010 08:51:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.IoUtils;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator of {@link IOL}s for instrument index, offset and length files. <b>Note that the
 * creator of this iterator has also to close it.</b>
 *
 * @author zzhao
 * @since 1.2
 */
class InstrumentIOLIterator implements Iterator<IOL>, AutoCloseable {

    private DataFile iolFile;

    private int remainingBytes;

    private final int numEntries;

    private int index;

    private ByteBuffer buffer;

    private boolean offsetUnsignedInt = false;

    InstrumentIOLIterator(File iolFile) throws IOException {
        this(iolFile, false);
    }

    InstrumentIOLIterator(File iolFile, boolean offsetUnsignedInt) throws IOException {
        this.iolFile = new DataFile(iolFile, true);
        this.remainingBytes = (int) this.iolFile.size();
        this.offsetUnsignedInt = offsetUnsignedInt;
        final int iolSize = offsetUnsignedInt ? 16 : IOL.SIZE;
        this.numEntries = this.remainingBytes / iolSize;
        this.index = 0;
        this.buffer = ByteBuffer.allocate(1024 * iolSize);
        this.buffer.flip();
    }

    int getNumEntries() {
        return this.numEntries;
    }

    public void close() throws IOException {
        IoUtils.close(this.iolFile);
    }

    public boolean hasNext() {
        return this.index < this.numEntries;
    }

    public IOL next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        try {
            if (!this.buffer.hasRemaining()) {
                fillBuffer();
            }

            ++this.index;

            final long iid = this.buffer.getLong();
            final long offset = this.offsetUnsignedInt
                ? this.buffer.getInt() & 0xFFFFFFFFL
                : this.buffer.getLong();
            final int length = this.buffer.getInt();

            return new IOL(iid, offset, length);
        } catch (IOException e) {
            throw new IllegalStateException("failed reading index, offset and length file", e);
        }
    }

    private void fillBuffer() throws IOException {
        this.buffer.clear();
        if (this.remainingBytes > 0) {
            if (this.remainingBytes < this.buffer.capacity()) {
                this.buffer.limit(this.remainingBytes);
            }

            this.iolFile.read(this.buffer);
            this.buffer.flip();
            this.remainingBytes -= this.buffer.remaining();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("not allowed");
    }
}
