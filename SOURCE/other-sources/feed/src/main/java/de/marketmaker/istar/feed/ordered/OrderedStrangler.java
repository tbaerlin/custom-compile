/*
 * Strangler.java
 *
 * Created on 22.06.2007 09:55:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.FeedDataVkeyOnly;
import de.marketmaker.istar.feed.delay.DelayProvider;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import static com.netflix.servo.annotations.DataSourceType.GAUGE;

/**
 * Strangler for ordered feed records, reduces the number of messages by combining several updates
 * before forwarding them to the next component. A typical configuration would be:
 * <pre>
 * MulticastFeedParser
 * |-> Strangler
 *     |-> OrderedUpdateBuilder (e.g. OrderedDelayer)
 *         |-> Multicast-Delayed
 * </pre>
 * The strangler receives records from MulticastFeedParser and decides whether that record can be
 * strangled. If it can, the data will be merged with any previously recorded data
 * for the same vendorkey. Otherwise, any previously recorded data will be sent to another
 * OrderedUpdateBuilder, followed by the currently received record. The
 * action described so far happens in the Parser's thread. Another thread, controlled by
 * the Strangler, is used to forward strangled data to the Delayer.
 * Releasing data can be configured to send all records within a fixed amount of time
 * <p>
 * Updates are merged as long as the total size of the merged fields does not exceed a
 * defined limit.
 * </p>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class OrderedStrangler implements InitializingBean, Lifecycle, OrderedUpdateBuilder {

    public static final int DEFAULT_STRANGLE_FOR_SECONDS = 60;

    private static final int HEADER_LENGTH = 8;

    private static final int DEFAULT_ARRAY_SIZE = 96;

    private static final int DEFAULT_DATA_SIZE = DEFAULT_ARRAY_SIZE - HEADER_LENGTH;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * strangled data keyed by vendorkey representations. The byte array values will be updated
     * by the parser thread and by the releaseThread, access has to be synchronized
     */
    private final Map<ByteString, byte[]> strangledRecords
            = new ConcurrentHashMap<>(400000);

    private final FieldDataMerger merger = new FieldDataMerger();

    private final BufferFieldData data = new BufferFieldData();

    /** to be used by parser thread exclusively */
    private final OrderedUpdate update1 = new OrderedUpdate();

    /** to be used by parser thread exclusively */
    private final ByteBuffer bb1 = BufferFieldData.asBuffer(new byte[2048]);

    /** to be used by release thread exclusively */
    private final OrderedUpdate update2 = new OrderedUpdate();

    /** to be used by release thread exclusively */
    private final ByteBuffer bb2 = BufferFieldData.asBuffer(new byte[2048]);

    private DynamicFieldDataFilter filter;

    /**
     * Destination for records that cannot be strangled.
     */
    private OrderedUpdateBuilder handler;

    /**
     * If set, the strangler will block all updates whose delay would be 0s, as the chicago
     * machines will always return realtime data for all such keys, even if delayed data
     * has been requested.
     */
    private DelayProvider delayProvider;

    private int strangleForSeconds = DEFAULT_STRANGLE_FOR_SECONDS;

    /**
     * Periodically scans strangled data and forwards updates to builder. Also removes
     * entries from strangledRecords that have not received an update within the strangle
     * interval.
     */
    private Thread releaseThread;

    /**
     * True iff the releaseThread should stop
     */
    private volatile boolean stopped = false;

    /**
     * Whether all updates forwarded to the handler should be filtered to include only dynamic fields.
     */
    private boolean filterDynamicFields = true;

    @Monitor(type = COUNTER)
    private final AtomicLong numStrangled = new AtomicLong();

    @Monitor(type = COUNTER)
    private final AtomicLong numBlocked = new AtomicLong();

    public void setFilterDynamicFields(boolean filterDynamicFields) {
        this.filterDynamicFields = filterDynamicFields;
    }

    /**
     * Sets the average number of seconds for which updates for a single vendorkey should be
     * collected before forwarding them to the builder.
     * Default {@value OrderedStrangler#DEFAULT_STRANGLE_FOR_SECONDS}
     * @param strangleForSeconds strangle interval
     */
    public void setStrangleForSeconds(int strangleForSeconds) {
        this.strangleForSeconds = strangleForSeconds;
        this.logger.info("<setStrangleForSeconds> " + this.strangleForSeconds);
    }

    public void setDelayProvider(DelayProvider delayProvider) {
        this.delayProvider = delayProvider;
    }

    public void setHandler(OrderedUpdateBuilder handler) {
        this.handler = handler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.filterDynamicFields) {
            this.filter = new DynamicFieldDataFilter();
        }
    }

    @Override
    public boolean isRunning() {
        return this.releaseThread != null;
    }

    public void start() {
        this.releaseThread = new Thread(OrderedStrangler.this::runRelease, "Strangler-release");
        this.releaseThread.start();
    }

    public void stop() {
        this.stopped = true;
        try {
            this.releaseThread.join(15 * 1000);
        } catch (InterruptedException e) {
            this.logger.error("<stop> interrupted!?");
            Thread.currentThread().interrupt();
        }
        if (this.releaseThread.isAlive()) {
            this.logger.error("<stop> sender thread still alive, returning anyway");
        }
        else {
            this.logger.info("<stop> sender thread stopped");
        }
    }

    private void runRelease() {
        sleep(this.strangleForSeconds * 1000L);
        while (!stopped) {
            try {
                final long millisToSleep = sendStrangled();
                sleep(millisToSleep);
            } catch (Throwable t) {
                logger.error("<runRelease> failed", t);
            }
        }
    }

    private void sleep(long millis) {
        long stillToSleep = millis;
        while (stillToSleep > 0 && !this.stopped) {
            try {
                TimeUnit.MILLISECONDS.sleep(Math.min(1000, stillToSleep));
            } catch (InterruptedException e) {
                this.logger.info("<sleep> interrupted");
                Thread.currentThread().interrupt();
                return;
            }
            stillToSleep -= 1000;
        }
    }

    public byte[] getApplicableMessageTypes() {
        return VwdFeedConstants.getAll();
    }

    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {
        if (delayProvider != null)  {
            final int delayInSeconds = delayProvider.getDelayInSeconds(data);
            if (delayInSeconds <= 0) {
                this.numBlocked.incrementAndGet();
                return;
            }
            update.setDelayInSeconds(delayInSeconds);
        }


        if (this.filterDynamicFields) {
            doProcess(data, this.filter.applyTo(update));
        }
        else {
            doProcess(data, update);
        }
    }

    private void doProcess(OrderedFeedData data, OrderedUpdate update) {
        if (update == null) {
            return;
        }

        if (update.getVwdKeyType() == 6) {  // no strangling for indexes
            this.handler.process(data, update);
            return;
        }

        if (update.getMsgType() != VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE) {
            sendWithoutStrangling(data, update);
            return;
        }

        final byte[] strangled = get(data.getVwdcode());

        if (process(data, update, strangled)) {
            this.numStrangled.incrementAndGet();
        }
    }

    private boolean process(OrderedFeedData data, OrderedUpdate update, byte[] strangled) {
        if (strangled == null) {
            if (update.size() > DEFAULT_DATA_SIZE) {
                sendWithoutStrangling(data, update);
                return false;
            }
            put(data.getVwdcode(), initData(update, new byte[DEFAULT_ARRAY_SIZE]));
            return true;
        }

        return doProcess(data, update, strangled);
    }

    private boolean doProcess(OrderedFeedData data, OrderedUpdate update, byte[] strangled) {
        final byte[] merged;
        synchronized (strangled) {
            if (isEmpty(strangled)) {
                if (update.size() > DEFAULT_DATA_SIZE) {
                    strangled[1] = 1;
                    sendWithoutStrangling(data, update);
                    return false;
                }
                initData(update, strangled);
                return true;
            }

            final ByteBuffer tmp = BufferFieldData.asBuffer(strangled);
            tmp.limit(tmp.get() & 0xFF).position(2);
            tmp.putShort((short) (tmp.getShort(2) | update.getFlags()));
            tmp.putInt(update.getTimestamp());
            this.data.reset(tmp);

            merged = this.merger.merge(this.data, update.getFieldData());
            if (merged == null) { // in-place merge succeeded
                return true;
            }
            if (merged.length <= DEFAULT_DATA_SIZE) {
                System.arraycopy(merged, 0, strangled, HEADER_LENGTH, merged.length);
                strangled[0] = (byte) (HEADER_LENGTH + merged.length);
                return true;
            }
            setEmpty(strangled);
        }
        this.bb1.clear().position(2);
        update.putHeader(this.bb1);
        this.bb1.put(this.bb1.position() - 5, strangled[2]); // replace flags by merged flags
        this.bb1.put(merged).flip();
        this.bb1.putShort((short) this.bb1.remaining());
        this.update1.reset(this.bb1);
        this.handler.process(data, this.update1);
        return false;
    }

    private void setEmpty(byte[] data) {
        data[0] = 0;
    }

    private boolean isEmpty(byte[] data) {
        return data[0] == 0;
    }

    private byte[] initData(OrderedUpdate update, final byte[] data) {
        final ByteBuffer tmp = BufferFieldData.asBuffer(data);
        tmp.position(1);
        tmp.put((byte) update.getVwdKeyType());
        tmp.putShort((short) update.getFlags());
        tmp.putInt(update.getTimestamp());
        update.putFields(tmp);
        tmp.flip();
        tmp.put(0, (byte) tmp.remaining());
        return data;
    }

    private void put(ByteString vwdcode, byte[] bytes) {
        synchronized (this.strangledRecords) {
            this.strangledRecords.put(vwdcode, bytes);
        }
    }

    private byte[] get(ByteString vwdcode) {
        synchronized (this.strangledRecords) {
            return this.strangledRecords.get(vwdcode);
        }
    }


    private void sendWithoutStrangling(OrderedFeedData data, OrderedUpdate update) {
        sendStrangled(data);
        this.handler.process(data, update);
    }

    private void sendStrangled(OrderedFeedData data) {
        final byte[] bytes = get(data.getVwdcode());
        if (bytes == null) {
            return;
        }

        synchronized (bytes) {
            if (!createUpdateMessage(this.bb1, data.getVwdcode(), bytes)) {
                return;
            }
        }

        this.update1.reset(this.bb1);
        this.handler.process(data, this.update1);
    }

    private boolean createUpdateMessage(final ByteBuffer buffer, final ByteString vwdcode,
            final byte[] bytes) {
        if (isEmpty(bytes)) {
            return false;
        }
        buffer.clear().position(2);
        buffer.put((byte) vwdcode.hashCode());
        vwdcode.writeTo(buffer, ByteString.LENGTH_ENCODING_BYTE);
        buffer.put((byte) 0); // mdps msg type
        buffer.put(VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE);
        buffer.putShort(bytes[1]); // vwd key type (mdps key type is 0, not needed)
        buffer.put(bytes[2]); // flags
        buffer.put(bytes, 3, (bytes[0] & 0xFF) - 3);
        buffer.flip();
        buffer.putShort((short) buffer.remaining());
        setEmpty(bytes);
        return true;
    }

    void testSendStrangle() throws InterruptedException {
        sendStrangled();
    }

    private long sendStrangled() throws InterruptedException {
        final TimeTaker tt = new TimeTaker();

        long now = tt.getStartedAt();
        final long endAt = now + this.strangleForSeconds * 1000L;

        int num = 0;
        int totalNum = 0;
        final int numPer10ms = getNumPer10ms();
        long sliceEndAt = now + 10;

        final Set<Map.Entry<ByteString, byte[]>> entries = this.strangledRecords.entrySet();
        for (Iterator<Map.Entry<ByteString, byte[]>> it = entries.iterator(); it.hasNext(); ) {
            final Map.Entry<ByteString, byte[]> entry = it.next();
            final byte[] data = entry.getValue();
            if (!processStrangledData(entry.getKey(), data)) {
                it.remove();
            }
            ++totalNum;
            if (++num == numPer10ms) {
                if (this.stopped) {
                    this.logger.info("<sendStrangled> stopped, returning");
                    return 0;
                }
                num = 0;
                now = System.currentTimeMillis();
                if (now < sliceEndAt) {
                    Thread.sleep(sliceEndAt - now);
                }
                sliceEndAt += 10;
            }
        }

        this.logger.info("<sendStrangled> took " + tt + " for " + totalNum + " records");
        return endAt - System.currentTimeMillis();
    }

    private int getNumPer10ms() {
        final int numBySize = (this.strangleForSeconds == 0)
                ? getSize() : (getSize() / (this.strangleForSeconds * 100));
        return Math.max(10, numBySize);
    }

    /**
     * If data contains any updates, these are assembled and sent to the builder
     * @param vwdcode vendorkey representation
     * @param data strangled data
     * @return true iff data contained any updates
     */
    private boolean processStrangledData(ByteString vwdcode, byte[] data) {
        synchronized (data) {
            if (!createUpdateMessage(this.bb2, vwdcode, data)) {
                // use the type flag as a means to keep the record around for another round
                if (data[1] == 0) {
                    return false;
                }
                data[1] = 0;
                return true;
            }
        }
        this.update2.reset(this.bb2);
        this.handler.process(new FeedDataVkeyOnly(VendorkeyVwd.getInstance(vwdcode, data[1])),
                this.update2);
        return true;
    }


    @ManagedAttribute
    public long getNumStrangled() {
        return numStrangled.get();
    }

    @ManagedAttribute
    public long getNumBlocked() {
        return numBlocked.get();
    }

    @Monitor(name = "size", type = GAUGE)
    public int getSize() {
        return this.strangledRecords.size();
    }
}
