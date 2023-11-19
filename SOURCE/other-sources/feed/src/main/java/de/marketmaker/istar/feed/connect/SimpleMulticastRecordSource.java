/*
 * SimpleMulticastRecordSource.java
 *
 * Created on 10.02.2005 14:32:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.RecordSource;

/**
 * Multicast based RecordSource without the possibility to re-request missing packets or deal
 * with reordered packets. It something like that occurs, it indicates that a switch or
 * network card is not working correctly, and instead of adding lots of code to correct that
 * it might be easier to just replace the component.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class SimpleMulticastRecordSource extends OrderedMulticastReceiver
        implements RecordSource, InitializingBean {

    private final FeedRecord feedRecord = new FeedRecord();

    private AtomicBoolean addSyncRecord = new AtomicBoolean(false);

    private ByteBuffer current;

    private boolean lengthIsInt = true;

    public SimpleMulticastRecordSource() {
    }

    public SimpleMulticastRecordSource(ByteBuffer current) {
        this.current = current;
        this.feedRecord.withOrder(current.order());
    }

    public void setLengthIsInt(boolean lengthIsInt) {
        this.lengthIsInt = lengthIsInt;
    }

    @ManagedOperation
    public void addSyncRecord() {
        this.addSyncRecord.set(true);
    }

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        clearSourceAddress();
        this.current = null;
    }

    public FeedRecord getFeedRecord() throws InterruptedException {
        // handle most common case first
        if (this.current != null && this.current.hasRemaining()) {
            final FeedRecord result = nextFromBuffer();
            if (result != null) {
                return result;
            }
        }

        if (this.addSyncRecord.compareAndSet(true, false)) {
            return FeedRecord.SYNC;
        }

        this.current = null;
        do {
            try {
                this.current = receive();
            } catch (SocketTimeoutException e) {
                return null;
            } catch (IOException e) {
                this.logger.warn("<fillBuffer> failed", e);
            }
        } while (this.current == null);

        return nextFromBuffer();
    }

    private FeedRecord nextFromBuffer() {
        final int length = getLength();
        if (length <= 0 || this.current.remaining() < length) {
            this.logger.warn("<nextFromBuffer> invalid length: " + length + ", ignoring");
            return null;
        }

        try {
            this.feedRecord.reset(this.current.array(), this.current.position(), length);
            this.current.position(this.current.position() + length);
        } catch (Exception e) {
            this.logger.error("<nextFromBuffer> failed pos()=" + this.current.position()
                    + ", length=" + length + ", in packet " + this.current.getLong(0));
            return null;
        }

        return this.feedRecord;
    }

    private int getLength() {
        if (this.lengthIsInt) {
            return this.current.getInt() - 4;
        }
        else {
            return (this.current.getShort() & 0xFFFF) - 2;
        }
    }
}