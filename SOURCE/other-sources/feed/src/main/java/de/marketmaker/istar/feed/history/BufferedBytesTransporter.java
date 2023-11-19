/*
 * DataFileTransporter.java
 *
 * Created on 22.08.12 11:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.jcip.annotations.NotThreadSafe;

import de.marketmaker.istar.common.io.DataFile;

/**
 * A buffered bytes transporter reading an underlying data file.
 *
 * @author zzhao
 */
@NotThreadSafe
public class BufferedBytesTransporter {
    private final DataFile dataFile;

    private final ByteBuffer buffer;

    private final ByteBuffer bufferReadOnly;

    /** Position inside file where the buffer's current content starts */
    private long dataFilePosition;

    /**
     * Initialize this buffer with the first <code>bufSize</code> bytes from <code>dataFile</code>.
     * The given buffer size <b>must</b> at least be the largest item's size.
     * @param dataFile Source of data
     * @param bufSize Buffer size
     * @throws IOException If data cannot be read
     */
    public BufferedBytesTransporter(DataFile dataFile, int bufSize) throws IOException {
        this.dataFile = dataFile;
        this.dataFilePosition = 0L;
        this.buffer = ByteBuffer.allocate((int) Math.min(dataFile.size(), bufSize));
        this.bufferReadOnly = this.buffer.asReadOnlyBuffer();
        this.dataFile.seek(this.dataFilePosition);
        this.dataFile.read(this.buffer);
        this.buffer.flip();
    }

    /**
     * Transfer an item at the given offset with the given length to the given target.
     * @param itemOffsetInFile Item's offset inside the file
     * @param itemLength Items length
     * @param target Target where to tranfer to
     * @return Number of transferred bytes
     * @throws IOException If new data is required from the source file and cannot be read
     */
    public int transferTo(long itemOffsetInFile, int itemLength, TransferTarget target) throws IOException {
        if (itemOffsetInFile < this.dataFilePosition) {
            // used in merge-sort style merge operation, hence this behavior
            throw new IllegalArgumentException("can only transfer forward");
        }
        if (exceedsCurrentBuffer(itemOffsetInFile, itemLength)) {
            fetchNewDataFromFile(itemOffsetInFile);
        }
        return transfer(itemOffsetInFile, itemLength, target);
    }

    /**
     * Reads missing bytes of the current history item. This only needs the offset
     * of the item because by definition no item will be larger than the buffer's capacity.
     * @param itemOffsetInFile Item's offset inside the file
     * @throws IOException If data cannot be read
     */
    private void fetchNewDataFromFile(long itemOffsetInFile) throws IOException {
        final long itemOffsetInBuffer = (itemOffsetInFile - this.dataFilePosition);
        if (itemOffsetInBuffer > this.buffer.limit()) {
            clearRead(itemOffsetInFile);
        }
        else {
            compactRead(itemOffsetInFile, (int) itemOffsetInBuffer);
        }
    }

    /**
     * Checks if the new history item is larger than the content of the buffer.
     * @param itemOffsetInFile Item's offset inside the file
     * @param itemLength Items length
     * @return <code>true</code> if the end of this item falls outside of the current buffer, <code>false</code> else
     */
    private boolean exceedsCurrentBuffer(long itemOffsetInFile, int itemLength) {
        return itemOffsetInFile + itemLength > this.dataFilePosition + this.buffer.limit();
    }

    /**
     * This will be used if the new position is beyond the current buffers data. It will then
     * discard all data from the buffer by overwriting it with data starting at the new items
     * start position
     * @param itemOffsetInFile New items start position
     * @throws IOException If data cannot be read
     */
    private void clearRead(long itemOffsetInFile) throws IOException {
        this.buffer.clear();
        this.dataFilePosition = itemOffsetInFile;
        this.dataFile.seek(this.dataFilePosition);
        this.dataFile.read(this.buffer);
        this.buffer.flip();
    }

    /**
     * This will be used if the requested item starts with data that is already partially inside the buffer.
     * It will then append required data from the file to the buffer.
     * @param itemOffsetInFile New items offset inside the file
     * @param itemOffsetInBuffer New items offset inside the current buffer
     * @throws IOException If data cannot be read
     */
    private void compactRead(long itemOffsetInFile, int itemOffsetInBuffer) throws IOException {
        this.buffer.position(itemOffsetInBuffer);
        this.buffer.compact();
        // seek to the end of last read, critical if another thread read from the data file
        // the whole transferTo operation MUST always synchronized
        this.dataFile.seek(this.dataFilePosition + this.buffer.capacity());
        this.dataFile.read(this.buffer);
        this.buffer.flip();
        this.dataFilePosition = itemOffsetInFile; // set the new from position
    }

    /**
     * Transfer data via the buffer to the given target.
     * @param itemOffsetInFile Item's offset inside the file
     * @param itemLength Items length
     * @param target Target where to tranfer to
     * @return Number of transferred bytes
     * @throws IOException If cannot be written
     */
    private int transfer(long itemOffsetInFile, int itemLength, TransferTarget target) throws IOException {
        try {
            this.bufferReadOnly.position((int) (itemOffsetInFile - this.dataFilePosition));
            this.bufferReadOnly.limit(this.bufferReadOnly.position() + itemLength);
            return target.transfer(this.bufferReadOnly);
        } finally {
            // Reset position and limit
            this.bufferReadOnly.clear();
        }
    }
}
