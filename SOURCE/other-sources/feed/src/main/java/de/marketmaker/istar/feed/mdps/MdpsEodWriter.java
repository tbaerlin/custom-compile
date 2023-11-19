/*
 * MdpsDelayServer.java
 *
 * Created on 11.06.2010 13:09:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.Monitor;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.feed.FeedMarketRepository;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldData;
import de.marketmaker.istar.feed.ordered.FieldDataBuilder;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedFeedDataFactory;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;
import de.marketmaker.istar.mdps.util.OrderedEntitlementProvider;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;

/**
 * Processes ByteBuffers that are supposed to contain mdps feed records and adds them to an
 * internal buffer before forwarding them to a delegate BufferWriter. For all records added,
 * the prefix '/E' is prepended to the key of the message (i.e., 710000.ETR,E becomes /E710000.ETR,E)
 * iff {@link MdpsWriter#usePrefix} is true.
 * <p>
 * The internal buffer is flushed at regular intervals, so that records are not kept for too long
 * even if no new records arrive. If the buffer is empty on flush, a heartbeat record will be sent.
 * @author oflege
 */
@ManagedResource
public class MdpsEodWriter extends MdpsWriter implements InitializingBean, OrderedUpdateBuilder {
    private int sourceId;

    private MdpsMessageAppender appender;

    private OrderedEntitlementProvider entitlementProvider;

    private final OrderedFeedData heartbeatData;

    private final FieldDataBuilder heartBeatBuilder;

    @Monitor(type = COUNTER)
    private final AtomicLong numRecordsPushed = new AtomicLong();

    @Monitor(type = COUNTER)
    private final AtomicLong numRecordsAppended = new AtomicLong();

    public MdpsEodWriter() {
        super(ByteUtil.toBytes("EOD.VWD,E"));
        setProtocolVersion(3);
        this.heartbeatData = OrderedFeedDataFactory.RT.create(VendorkeyVwd.getInstance("1.EOD.VWD"),
                new FeedMarketRepository(true).getMarket(new ByteString("VWD")));
        this.heartBeatBuilder = new FieldDataBuilder(64);
        setUsePrefix(true);
    }

    public void setEntitlementProvider(
            OrderedEntitlementProvider entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.appender = new MdpsMessageAppender(this.sourceId, this.entitlementProvider, this.usePrefix);
    }

    @ManagedAttribute
    public long getNumRecordsPushed() {
        return this.numRecordsPushed.get();
    }

    @ManagedAttribute
    public long getNumRecordsAppended() {
        return this.numRecordsAppended.get();
    }

    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {
        if (data.isReadyForPush() || update.isDelete()) {
            if (append(data, update.getFieldData(), update.getMdpsMsgType())) {
                this.numRecordsPushed.incrementAndGet();
            }
        }
    }

    public boolean append(OrderedFeedData data, FieldData fd, int mdpsMsgType) {
        synchronized (this.bufferMutex) {
            if (this.bb.remaining() < 8192) {
                sendBuffer();
            }
            if (this.appender.append(this.bb, data, fd, mdpsMsgType)) {
                this.numRecordsAppended.incrementAndGet();
                return true;
            }
        }
        return false;
    }

    protected void createHeartbeat() {
        final DateTime dt = new DateTime();
        this.heartBeatBuilder.reset();
        this.heartBeatBuilder.putTimeFid(VwdFieldOrder.ORDER_ADF_ZEIT);
        this.heartBeatBuilder.putInt(MdpsFeedUtils.encodeTime(dt));
        this.heartBeatBuilder.putIntFid(VwdFieldOrder.ORDER_ADF_DATUM);
        this.heartBeatBuilder.putInt(MdpsFeedUtils.encodeDate(dt));
        append(this.heartbeatData,
                new BufferFieldData(this.heartBeatBuilder.asArray()), MdpsMessageTypes.UPDATE);
    }
}
