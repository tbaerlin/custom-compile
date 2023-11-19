/*
 * DataChunkFactory.java
 *
 * Created on 15.11.2004 11:41:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.delay;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.netflix.servo.annotations.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.netflix.servo.annotations.DataSourceType.GAUGE;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DataChunkPool {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final long capacity;

    private final int chunkSize;

    private final int num;

    private BlockingQueue<DataChunk> pool;

    private final AtomicBoolean errorEncountered = new AtomicBoolean(false);

    DataChunkPool(long capacity, int chunkSize, ByteOrder order, boolean direct) {
        this.capacity = capacity;
        this.chunkSize = chunkSize;
        this.num = (int) (this.capacity / chunkSize);
        this.pool = new ArrayBlockingQueue<>(num);
        if (num < 1000 || capacity < (1 << 30)) {
            for (int i = 0; i < num; i++) {
                ByteBuffer bb = createBuffer(direct, this.chunkSize);
                this.pool.add(new DataChunk(bb.order(order)));
            }
        }
        else {
            ByteBuffer base = null;
            int offset = 0;
            for (int i = 0; i < num; i++) {
                if (base == null || base.capacity() - offset < chunkSize) {
                    // 500mb or just enough to fit remaining:
                    final int size = (int) Math.min(1L << 29, ((long) (num - i) * chunkSize));
                    base = createBuffer(direct, size);
                    offset = 0;
                }
                base.clear().position(offset).limit(offset + chunkSize);
                this.pool.add(new DataChunk(base.slice().order(order)));
                offset += chunkSize;
            }
        }
    }

    private ByteBuffer createBuffer(boolean direct, final int capacity) {
        return direct ? ByteBuffer.allocateDirect(capacity) : ByteBuffer.allocate(capacity);
    }

    public long getCapacity() {
        return capacity;
    }

    public int size() {
        return this.pool.size();
    }

    public int getChunkSize() {
        return chunkSize;
    }

    @Monitor(name = "usedChunksPct", type = GAUGE)
    public int getUsedChunksPct() {
        return (this.num - this.pool.size()) * 100 / this.num;
    }

    public DataChunk getChunk() {
        final DataChunk chunk = this.pool.poll();
        if (chunk == null && this.errorEncountered.compareAndSet(false, true)) {
            this.logger.error("<getChunk> no more chunks, capacity exceeded!");
        }
        if (chunk != null && chunk.getLength() > 0) {
            // should never happen, helps to identify illegal app state
            this.logger.error("<getChunk> chunk was modified while in pool");
        }
        return chunk;
    }

    public void returnChunk(DataChunk tc) {
        tc.reset();
        this.pool.add(tc);
        if (this.errorEncountered.get()) {
            // if usage is around 100% and we unset the flag each time a chunk is returned,
            // we would get a lot of error messages, so only unset the flag if usage drops below 99%
            this.errorEncountered.set(getUsedChunksPct() >= 99);
        }
    }

    /**
     * Returns all chunks used by from bucket.
     */
    public void returnChunks(DelayBucket bucket) {
        returnChunks(bucket.getLastChunk());
    }

    public void returnChunks(DataChunk c) {
        DataChunk current = c;
        while (current != null) {
            // returnChunk clears previous, so remember it
            final DataChunk previous = current.getPrevious();
            returnChunk(current);
            current = previous;
        }
    }
}
