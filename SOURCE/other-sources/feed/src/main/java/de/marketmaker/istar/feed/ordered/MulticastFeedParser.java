/*
 * MulticastFeedParser.java
 *
 * Created on 28.08.12 09:06
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;

import com.lmax.disruptor.EventHandler;
import com.netflix.servo.annotations.Monitor;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRegistry;
import de.marketmaker.istar.feed.FeedDataRegistryImpl;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.ParserErrorHandler;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VolatileFeedDataRegistry;
import de.marketmaker.istar.feed.connect.FeedStats;
import de.marketmaker.istar.feed.mux.MuxOutput;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;

/**
 * Parses the new multicast feed and forwards updates to {@link OrderedUpdateBuilder}s.
 * If the multicast feed is received in the same process, this object will be used as a
 * delegate {@link com.lmax.disruptor.EventHandler} of the
 * {@link de.marketmaker.istar.feed.multicast.FeedMulticastReceiver}.
 * <p>
 * In a different setup,
 * the feed may be received by a separate process on the same machine and then be offered
 * locally by tcp; in that case, this object can be used as a
 * {@link de.marketmaker.istar.feed.mux.MuxOutput} delegate of the
 * {@link de.marketmaker.istar.feed.mux.MuxIn} that connects to the local tcp feed source.
 * @author oflege
 */
@ManagedResource
public final class MulticastFeedParser implements InitializingBean,
        EventHandler<ByteBuffer>, MuxOutput, FeedStats.MessageSink {

    @Monitor(type = COUNTER)
    private final AtomicLong numRecordsParsed = new AtomicLong();

    @Monitor(type = COUNTER)
    private final AtomicLong numParseProblems = new AtomicLong();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeedDataRegistry registry;

    private ParserErrorHandler errorHandler;

    private OrderedUpdateBuilder[] builders = new OrderedUpdateBuilder[0];

    private VendorkeyFilter vendorkeyFilter;

    private boolean useFilterInUpdate = false;

    private OrderedUpdate update;

    private int hashCodeMask;   // this seems to be deprecated

    private int acceptableHashCode;

    private boolean canGetFeedData;

    /**
     * {@link #ackUpdate(FeedData)} modifies the state of a FeedData object according to the
     * current message. Whenever a FeedData object is shared for realtime and delayed data, the
     * delayed parser should <em>not</em> update the state, so this flag should be set to false.
     */
    private boolean ackUpdates = true;

    private boolean neverRegisterKey = false;

    private volatile boolean disabled = false;

    public void setAckUpdates(boolean ackUpdates) {
        this.ackUpdates = ackUpdates;
    }

    void setDisabled(boolean disabled) {
        this.disabled = disabled;
        this.logger.info("<setDisabled> " + disabled);
    }

    public void setNeverRegisterKey(boolean neverRegisterKey) {
        this.neverRegisterKey = neverRegisterKey;
    }

    public void setErrorHandler(ParserErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void setRegistry(FeedDataRegistry registry) {
        this.registry = registry;
    }

    public void setBuilders(OrderedUpdateBuilder... builders) {
        this.builders = Arrays.copyOf(builders, builders.length);
    }

    public void setVendorkeyFilter(VendorkeyFilter vendorkeyFilter) {
        this.vendorkeyFilter = vendorkeyFilter;
    }

    public void setHashCodeMask(int hashCodeMask) {
        this.hashCodeMask = hashCodeMask;
    }

    public void setAcceptableHashCode(int acceptableHashCode) {
        this.acceptableHashCode = acceptableHashCode;
    }

    public void setUseFilterInUpdate(boolean useFilterInUpdate) {
        this.useFilterInUpdate = useFilterInUpdate;
    }

    @ManagedAttribute
    public long getNumRecordsParsed() {
        return this.numRecordsParsed.get();
    }

    @Override
    public long numMessagesReceived() {
        return this.numRecordsParsed.get();
    }

    @ManagedAttribute
    public long getNumParseProblems() {
        return this.numParseProblems.get();
    }

    @Override
    public void afterPropertiesSet() {
        this.update = new OrderedUpdate(this.useFilterInUpdate ? this.vendorkeyFilter : null,
                this.hashCodeMask, this.acceptableHashCode);
        // FeedDataRepository and FeedDataRegistryImpl can retrieve snap data from peer chicagos
        this.canGetFeedData = this.registry instanceof FeedDataRepository
                || this.registry instanceof FeedDataRegistryImpl;
        if (!this.canGetFeedData && this.neverRegisterKey) {
            throw new IllegalStateException("cannot get or register keys");
        }
    }

    // ----------------------------------------
    // MuxInputReceiver
    // ----------------------------------------

    @Override
    public boolean isAppendOnlyCompleteRecords() {
        return false;
    }

    @Override
    public void append(ByteBuffer buffer) throws IOException {
        if (!parseRecords(buffer)) {
            logBufferStatus(buffer);
            throw new IOException("invalid length in " + buffer + ": "
                    + (buffer.getShort(buffer.position()) & 0xFFFF));
        }
    }

    void logBufferStatus(ByteBuffer buffer) {
        final String bytesEncoded = Base64.getEncoder().encodeToString(buffer.array());
        final ByteBuffer duplicate = buffer.duplicate();
        this.logger.info(bytesEncoded);
        final boolean hasData = IntStream.range(0, duplicate.remaining())
            .map(i -> duplicate.get()).anyMatch(i -> i != 0);
        this.logger.info("{} has data: {}", buffer, hasData);
    }

    // ----------------------------------------
    // EventHandler
    // ----------------------------------------

    @Override
    public void onEvent(ByteBuffer event, long sequence, boolean endOfBatch) throws Exception {
        if (this.disabled) { // may happen for push
            return;
        }
        try {
            onEvent(event);
        } catch (Throwable e) {
            // we are running in a disruptor worker thread, which should never throw an exception
            this.logger.error("<onEvent> failed", e);
        }
    }

    // ----------------------------------------

    public void onEvent(ByteBuffer buffer) {
        if (!parseRecords(buffer)) {
            // no need to throw exception, caller is expected to clear buffer
            this.logger.error("<onEvent> " + "invalid length in " + buffer + ": "
                    + (buffer.getShort(buffer.position()) & 0xFFFF));
        }
    }

    private void onError(ByteBuffer buffer, Exception e) {
        if (this.errorHandler == null) {
            this.logger.error("<parseRecords> failed", e);
        }
        else {
            this.errorHandler.handle(buffer, e);
        }
        this.numParseProblems.incrementAndGet();
    }

    private boolean parseRecords(ByteBuffer buffer) {
        final int bufferEnd = buffer.limit();
        while (buffer.remaining() > 1) {
            buffer.mark();
            final int recordEnd = buffer.position() + (buffer.getShort() & 0xFFFF);
            if (recordEnd <= buffer.position()) {
                buffer.reset();
                return false;
            }
            if (recordEnd > buffer.limit()) { // incomplete message
                buffer.reset();
                return true;
            }
            buffer.limit(recordEnd);
            try {
                doParseRecord(buffer);
            } catch (Exception e) {
                try {
                    // There have been cases that this command fails but we need to recover!
                    onError((ByteBuffer) buffer.duplicate().reset().limit(recordEnd), e);
                } catch (Exception e2) {
                    onError(null, e);
                    this.logger.error("<parseRecord> Could not copy buffer for error logging", e2);
                }
            }
            buffer.limit(bufferEnd).position(recordEnd);
        }
        return true;
    }

    // this is running on a SelectorThread
    private void doParseRecord(ByteBuffer buffer) {
        if (!this.update.reset(buffer)) {
            return; // filtered
        }

        final OrderedFeedData feedData = getFeedData();
        if (feedData == null) {
            return;
        }

        this.numRecordsParsed.incrementAndGet();

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (feedData) {
            for (OrderedUpdateBuilder builder : this.builders) {
                builder.process(feedData, this.update);
            }
            ackUpdate(feedData);
        }
    }

    private void ackUpdate(FeedData feedData) {
        if (!this.ackUpdates) {
            return;
        }
        if (this.update.isDelete()) {
            this.registry.unregister(feedData.getVendorkey());
        }
        else {
            feedData.setState(FeedData.STATE_UPDATED);
        }
    }

    private OrderedFeedData getFeedData() {
        if (this.canGetFeedData) {
            // we no longer create a ByteString instance here with byte[] copy
            // now we reuse the byte[] in an AsciiString instance to do the registry lookup
            final OrderedFeedData feedData = (OrderedFeedData) this.registry.get(this.update.getVwdCode());
            if (feedData != null || this.neverRegisterKey) {
                return feedData;
            }
        }
        final VendorkeyVwd key = this.update.getVendorkey();
        if (this.vendorkeyFilter != null && !this.useFilterInUpdate &&
                !this.vendorkeyFilter.test(key)) {
            return null;
        }

        final OrderedFeedData result = (OrderedFeedData) this.registry.create(key);
        if (this.update.isDelete() || result == null) {
            return result;
        }

        result.setCreatedTimestamp(this.update.getTimestamp());
        this.registry.register(result);
        return result;
    }

    public static void main(String[] args) throws Exception {
        MulticastFeedParser p = new MulticastFeedParser();
        p.setRegistry(new VolatileFeedDataRegistry());
        p.afterPropertiesSet();
        byte[] bytes = FileCopyUtils.copyToByteArray(new File("/Users/oflege/tmp/rt.bin"));
        p.append(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN));
        System.out.println(p.numRecordsParsed.get());
    }
}
