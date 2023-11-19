/*
 * StaticMdpsParser.java
 *
 * Created on 08.05.14 10:38
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.staticfeed;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.Monitor;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.mdps.MdpsFeedParser;
import de.marketmaker.istar.feed.mdps.MdpsMessageConstants;
import de.marketmaker.istar.feed.mdps.MdpsMessageTypes;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ID_SOURCE_ID;

/**
 * An MdpsParser with additional support for blob messages.
 * @author oflege
 */
@ManagedResource
public class StaticMdpsParser extends MdpsFeedParser {

    private BlobBuilder blobBuilder;

    private AtomicBoolean flushBlobs = new AtomicBoolean();

    @Monitor(type = COUNTER)
    private AtomicLong numBlobsParsed = new AtomicLong();

    private final BitSet discardEmptyNewRecordsForSourceIds = new BitSet(537);

    private final Map<Integer, BitSet> emptyRecordOverrideWhitelist = new HashMap<>();

    public StaticMdpsParser() {
        setRegisterCreatedKey(false);
        for (int sid : new int[]{6, 16, 23, 28, 53, 108, 235, 346, 363, 365, 419, 536}) { // ISTAR-747
            this.discardEmptyNewRecordsForSourceIds.set(sid);
        }

        // CORE-17475: Add a override whitelist for MessageType MdpsMessageTypes.UPDATE and BigSourceId 6
        BitSet whitelist = new BitSet(7);
        whitelist.set(6);
        this.emptyRecordOverrideWhitelist.put(MdpsMessageTypes.UPDATE, whitelist);
    }

    @ManagedAttribute
    public long getBlobsParsed() {
        return this.numBlobsParsed.get();
    }

    public void triggerFlushBlobs() {
        this.flushBlobs.set(true);
    }

    public void setBlobBuilder(BlobBuilder blobBuilder) {
        this.blobBuilder = blobBuilder;
    }

    @Override
    protected void build(FeedData data) {
        if (data.getState() == FeedData.STATE_NEW) {
            if (this.parsedRecord.isEmpty()
                    && this.discardEmptyNewRecordsForSourceIds.get(this.parsedRecord.getSourceId())
                    && !isInWhiteList()) {
                return;
            }
            register(data);
        }
        super.build(data);
    }

    private boolean isInWhiteList() {
        BitSet whitelist = this.emptyRecordOverrideWhitelist.get((int) this.parsedRecord.getMessageTypeMdps());
        return whitelist != null && whitelist.get(this.parsedRecord.getSourceId());
    }

    public void parse(FeedRecord feedRecord) {
        if (this.flushBlobs.compareAndSet(true, false)) {
            this.blobBuilder.flush();
        }

        if (feedRecord.getMdpsMessageType() == MdpsMessageTypes.BLOB) {
            parseBlob(feedRecord);
        }
        else {
            super.parse(feedRecord);
        }
    }

    private void parseBlob(FeedRecord feedRecord) {
        final ByteBuffer buffer = feedRecord.getAsByteBuffer();
        buffer.position(feedRecord.getOffset() + MdpsMessageConstants.HEADER_LENGTH); // skip header

        if (this.blobBuilder == null || !buffer.hasRemaining()) {
            return;
        }

        final short keyFieldid = buffer.getShort();
        if (keyFieldid != MdpsMessageConstants.MDPS_KEY_FID) {
            setParseProblem("Illegal fieldid for mdps key: " + keyFieldid);
            return;
        }

        // IGNORED: final ByteString mdpsKey =
        ByteString.readFrom(buffer, ByteString.LENGTH_ENCODING_BYTE);
/*
        if (!MDPS_KEY.equals(mdpsKey)) {
            setParseProblem("Invalid blob mdps key: " + mdpsKey);
            return;
        }
*/
        initParsedRecord(feedRecord);
        parseFields(buffer);

        this.numBlobsParsed.incrementAndGet();
        this.blobBuilder.process(this.parsedRecord);
    }

    private void initParsedRecord(FeedRecord feedRecord) {
        this.parsedRecord.reset(feedRecord);
        this.parsedRecord.setMessageType(VwdFeedConstants.MESSAGE_TYPE_BLOB);
        this.parsedRecord.setMessageTypeMdps((byte) MdpsMessageTypes.BLOB);
        this.parsedRecord.setMessageTimestamp(getMessageTimestamp());
        this.parsedRecord.setKeyType(0x101);
    }

    private void parseFields(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            final int fieldId = buffer.getShort();
            if (fieldId == ID_SOURCE_ID) {
                buffer.get(); // ignore
            }
            else {
                parseField(fieldId, buffer);
            }
        }
    }
}