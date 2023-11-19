/*
 * LiveTickCorrections.java
 *
 * Created on 24.06.14 10:36
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.joda.time.DateTimeConstants;

import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.tick.TickProvider;

import static de.marketmaker.istar.feed.ordered.OrderedFeedDataFactory.RT_TICKS;

/**
 * Apply tick corrections to tick data that is currently "live", i.e., additional ticks may be
 * added while the corrections are applied.<p>
 * The basic idea is to use a temporary {@link de.marketmaker.istar.feed.ordered.OrderedFeedData}
 * object to which we add each of the ticks received so far and apply corrections on the fly.
 * Once the corrected ticks have been stored, the storage info in the temporary object is used
 * to replace the respective info in the live object.
 * </p>
 * @author oflege
 */
class LiveTickCorrections extends TickCorrections implements Callable<Boolean> {

    private final ByteBuffer bb = BufferFieldData.asBuffer(new byte[256]);

    private final OrderedFeedData fd;

    private final OrderedTickData td;

    private final OrderedFeedData tmpFd;

    private final OrderedTickData tmpTd;

    private final int day;

    private final MarketDay md;

    private final TickWriter tickWriter;

    private final MemoryTickStore memoryTickStore;

    static LiveTickCorrections create(OrderedFeedData fd, OrderedUpdate update,
            TickWriter tickWriter, MemoryTickStore memoryTickStore) {
        final List<Correction> corrections = decodeCorrections(update);
        if (corrections != null && !corrections.isEmpty()) {
            return new LiveTickCorrections(fd, update.getDate(), tickWriter, memoryTickStore, corrections);
        }
        return null;
    }

    private LiveTickCorrections(OrderedFeedData fd, int day,
            TickWriter tickWriter, MemoryTickStore memoryTickStore, List<Correction> corrections) {
        super(corrections);

        this.fd = fd;
        this.td = fd.getOrderedTickData();

        this.tmpFd = RT_TICKS.create(fd.getVendorkey(), fd.getMarket());
        this.tmpTd = tmpFd.getOrderedTickData();
        this.tmpTd.setTickStats(this.td.getTickStats());
        this.tmpTd.setDate(day);

        this.day = day;
        this.md = MarketDay.create(fd.getMarket().getName(), day);
        this.tickWriter = tickWriter;
        this.memoryTickStore = memoryTickStore;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            return doRun();
        } catch (Throwable t) {
            this.logger.error("<run> failed", t);
            return false;
        }
    }

    private boolean doRun() throws InterruptedException {
        final TickProvider.Result ticks;
        long address;
        synchronized (this.fd) {
            if (this.day != this.td.getDate()) {
                this.logger.warn("<doRun> invalid day " + this.day + " != " + this.td.getDate());
                return false;
            }
            if (!waitForEviction()) {
                return false;
            }
            this.td.setCorrectionPending();
            ticks = this.memoryTickStore.getTicks(this.fd, this.day, null);
            address = this.td.getStoreAddress(this.day);
        }

        long toFree = addTicks(ticks, address);
        setFree(toFree);
        this.logger.info("<applyTo> finished for " + fd.getVendorkey());
        return true;
    }

    private void setFree(long addr) {
        // because we set the correction pending flag for td, all chunks that would normally have
        // been submitted for writing and eventually freed
        // are just kept in memory and we have to free them all, hence the recursion
        if (MemoryTickStore.isMemoryAddress(addr)) {
            final long prev = this.memoryTickStore.getPrevious(addr);
            this.memoryTickStore.setFree(addr);
            setFree(prev);
        }
    }

    private long addTicks(TickProvider.Result ticks, long address) throws InterruptedException {
        synchronized (this.tmpFd) {
            // add all ticks that were available
            addTicks(ticks);

            // now add ticks that were added to fd while we copied ticks to tmpFd until we have
            // finally catched up
            long previousAddress = address;
            while (true) {
                final byte[] toAdd;
                synchronized (this.fd) {
                    final long currentAddress = td.getStoreAddress();
                    if (currentAddress == previousAddress) {
                        td.setStoreAddress(day, tmpTd.getStoreAddress());
                        td.setLength(day, tmpTd.getLength());
                        td.unsetCorrectionPending();
                        return currentAddress;
                    }
                    toAdd = this.memoryTickStore.getData(previousAddress, currentAddress);
                    previousAddress = currentAddress;
                }
                addTicks(toAdd);
            }
        }
    }

    private void addTicks(byte[] toAdd) throws InterruptedException {
        ByteBuffer tmp = ByteBuffer.wrap(toAdd);
        while (tmp.hasRemaining()) {
            tmp.limit(tmp.position() + (tmp.get() & 0xFF));
            this.bb.clear().position(1);
            this.bb.put(tmp);
            addTick();
            tmp.limit(tmp.capacity());
        }
    }

    private void addTicks(TickProvider.Result ticks) throws InterruptedException {
        // make sure to use an iterator that also returns ticks corrected in previous run(s)
        Iterator<TickDecompressor.Element> it
                = new TickDecompressor(ticks.getTicks(), ticks.getEncoding()).iterator(true);
        while (it.hasNext()) {
            TickDecompressor.Element e = it.next();
            Correction c = findMatching(e);
            if (c != null) {
                addTick(c.getFlags(e.getFlags()), e.getData());
                if (c.inserts != null) {
                    for (int i = 0; i < c.inserts.length; i++) {
                        addTick(c.inserts[i]);
                    }
                }
            }
            else {
                addTick(e.getFlags(), e.getData());
            }
        }
    }

    private void addTick(byte[] insert) throws InterruptedException {
        this.bb.clear().position(1);
        this.bb.put(insert);
        addTick();
    }

    private void addTick(int flags, BufferFieldData data) throws InterruptedException {
        this.bb.clear().position(1);
        this.bb.put((byte) flags);
        data.putFields(this.bb);
        addTick();
    }

    private void addTick() throws InterruptedException {
        this.bb.flip();
        this.bb.put(0, (byte) this.bb.remaining());
        if (!this.memoryTickStore.add(this.tmpFd, this.bb)) {
            throw new IllegalStateException("failed to add tick");
        }
        if (this.tmpTd.isWritePending()) {
            waitForWrite();
        }
    }

    private void waitForWrite() throws InterruptedException {
        assert Thread.holdsLock(this.tmpFd);

        // a chunk has been submitted for writing; chunk writing tasks will be submitted
        // periodically by tickWriter, but that may take 2mins, so trigger flush NOW:
        this.tickWriter.flushTask(this.md);

        this.logger.debug("<waitForWrite> flush " + this.md);
        while (this.tmpTd.isWritePending()) {
            // TickWriter#commitWrite will need to synchronize on cfd to update
            // tick related info, so we have to release that lock temporarily:
            this.tmpFd.wait(100);
        }
    }

    private boolean waitForEviction() throws InterruptedException {
        assert Thread.holdsLock(this.fd);

        int n = 0;
        while (this.td.isEvictionPending() || this.td.isWritePending()) {
            if (n == 1) {
                // maybe task had not been flushed previously?
                this.tickWriter.flushTask(this.md);
            }
            if (n++ == DateTimeConstants.SECONDS_PER_MINUTE) {
                this.logger.error("<waitForEviction> timeout while waiting for eviction of " + this.fd.getVendorkey());
                return false;
            }
            this.logger.info("<waitForEviction> waiting for eviction of " + this.fd.getVendorkey());
            // TickWriter#commitWrite will need to synchronize on fd to update
            // tick related info, so we have to release that lock temporarily:
            fd.wait(100);
        }
        return true;
    }
}
