/*
 * MdpsFeedRecordQueue.java
 *
 * Created on 25.08.2006 11:13:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.util.BitSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.Monitor;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.Constants;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.ClassUtils;

import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.RecordSource;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import static com.netflix.servo.annotations.DataSourceType.GAUGE;

/**
 * Link between a thread that adds FeedRecords to this object and another thread that retrieves
 * those records. Will buffer records up to a configurable maximum, if that maximum is reached,
 * it will discard the oldest record whenever a new one is to be stored. Can be configured to
 * process only messages whose type is contained in a particular set.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
@ThreadSafe
public class MdpsFeedRecordQueue implements InitializingBean, RecordSource, BeanNameAware {
    private static final int DEFAULT_MAX_QUEUE_SIZE = 10000;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private BitSet filter = null;

    private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;

    /**
     * number of FeedData records that were rejected due to a full queue.
     */
    @Monitor(type = COUNTER)
    private final AtomicLong numRejected = new AtomicLong(0);

    /**
     * number of MultiFeedRecord objects added to this queue
     */
    @Monitor(type = COUNTER)
    private final AtomicLong recordsAdded = new AtomicLong(0);

    /**
     * if &gt; 0, log an error whenever we rejected this many records
     */
    private long logRejectedCount = 0;

    /**
     * synchronization point between threads that add data to this queue
     * and those that retrieve data (by using this object as a
     * {@link de.marketmaker.istar.feed.RecordSource})
     */
    private BlockingQueue<MultiFeedRecord> queue;

    private MultiFeedRecord current = MultiFeedRecord.EMPTY;

    private String name;

    public void setBeanName(String name) {
        this.name = name;
    }

    public void setLogRejectedCount(long logRejectedCount) {
        this.logRejectedCount = logRejectedCount;
    }

    public void afterPropertiesSet() throws Exception {
        this.queue = new ArrayBlockingQueue<>(this.maxQueueSize);
        this.logger.info("<afterPropertiesSet> created queue for up to " + this.maxQueueSize
                + " records in " + this.name);
    }

    public FeedRecord getFeedRecord() throws InterruptedException {
        final FeedRecord result = this.current.getFeedRecord();
        if (result != null) {
            return result;
        }
        this.current = this.queue.take();
        return this.current.getFeedRecord();
    }

    @ManagedAttribute
    public long getNumRejected() {
        return this.numRejected.get();
    }

    @ManagedAttribute
    public long getRecordsAdded() {
        return this.recordsAdded.get();
    }

    @ManagedAttribute
    @Monitor(name = "size", type = GAUGE)
    public int getSize() {
        return this.queue.size();
    }

    public void offer(MultiFeedRecord r, int messageType) {
        if (this.filter == null || this.filter.get(messageType)) {
            if (!this.queue.offer(r)) {
                final long n = this.numRejected.addAndGet(r.getNumRecords());
                if (this.logRejectedCount > 0 && (n % this.logRejectedCount) == 0) {
                    this.logger.error("<offer> rejected " + n + " in " + this.name);
                }
            }
            else {
                this.recordsAdded.incrementAndGet();
            }
        }
    }

    @ManagedOperation
    public void resetNumRejected() {
        this.numRejected.set(0);
    }

    /**
     * Invoke this method to add a {@link de.marketmaker.istar.feed.FeedRecord#SYNC} to the records
     * that will be fetched from this RecordSource.
     * @return true if sync record has been added
     */
    @ManagedOperation
    public boolean addSyncRecord() {
        return this.queue.offer(new MultiFeedRecord(new FeedRecord[] {
                FeedRecord.SYNC
        }, 1));
    }

    /**
     * @deprecated please use {@link #setMaxQueueSize(int)}
     * @param maxBacklog max number of buffered elements
     */
    public void setMaxBacklog(int maxBacklog) {
        setMaxQueueSize(maxBacklog);
    }

    /**
     * Maximum number of FeedRecord objects that will be queued by this object. Whenever
     * the queue is full, any additional records offered will be discarded. Has to be called
     * before {@link #afterPropertiesSet()} will be invoked.
     * <p><b>Important:</b> Setting this parameter to an inappropriate value will cause random
     * data loss. In order to determine the appropriate value, you have to consider the rate
     * at which producers add to this queue and the rate at which records are consumed.
     * <p>Default is {@value #DEFAULT_MAX_QUEUE_SIZE}.
     * @param size maximum queue size.
     */
    public void setMaxQueueSize(int size) {
        this.maxQueueSize = size;
    }

    /**
     * Only messages whose message type is given in the names array will be enqueued; If this
     * method is not called, all messages will be enqueued. Valid names must match constants
     * in {@link MdpsMessageTypes}.
     * @param names message type names of messages that should be processed.
     * @deprecated use {@link MdpsRecordProvider#setMessageTypeNames(String[])}
     */
    public void setMessageTypeNames(String[] names) {
        final Constants constants = new Constants(MdpsMessageTypes.class);

        this.filter = new BitSet();
        for (String name : names) {
            this.filter.set(constants.asNumber(name).intValue());
        }
    }

    public String toString() {
        return ClassUtils.getShortName(getClass())
                + this.name + ", size=" + getSize() + ", rejected=" + getNumRejected() + "]";
    }

    boolean hasFilter() {
        return this.filter != null;
    }
}
