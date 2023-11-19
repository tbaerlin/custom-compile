/*
 * MdpsParser.java
 *
 * Created on 01.08.2005 14:16:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.BitSet;

import com.netflix.servo.annotations.Monitor;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.IllegalKeyException;
import de.marketmaker.istar.feed.InvalidFieldsException;
import de.marketmaker.istar.feed.ordered.OrderedFeedUpdateBuilder;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import static com.netflix.servo.annotations.DataSourceLevel.CRITICAL;
import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.*;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ID_ADF_BOERSENZEIT;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ID_SOURCE_ID;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.file.Files.readAllBytes;
import static org.joda.time.DateTimeConstants.SECONDS_PER_DAY;

/**
 * Parser that can parse mdps messages.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class MdpsFeedParser extends MdpsParser {

    /**
     * If false, will only parse messages with keys that do <em>not</em> start with "/D",
     * and vice versa. Thus, an MdpsParser will never be able to process realtime and
     * delayed records.
     */
    protected boolean processDelayedRecords = false;

    private boolean parseFields = true;

    private String metricsPostfix = null;


    MdpsKeyConverter keyConverter;

    private static final BitSet RECAP_HEURISTIC_IDS = new BitSet();

    static {
        RECAP_HEURISTIC_IDS.set(VwdFieldDescription.ADF_Brief_Vortag.id());
        RECAP_HEURISTIC_IDS.set(VwdFieldDescription.ADF_Geld_Vortag.id());
        RECAP_HEURISTIC_IDS.set(VwdFieldDescription.ADF_Schluss_Vortag.id());
        RECAP_HEURISTIC_IDS.set(VwdFieldDescription.ADF_Schluss_Vortagesdatum.id());
        RECAP_HEURISTIC_IDS.set(VwdFieldDescription.ADF_Anfang_Vortag.id());
    }

    public MdpsFeedParser() {
        // too many duplicate fields in mdps feed...
        setLogDuplicateFields(false);
    }

    @Monitor(type = COUNTER, level = CRITICAL)
    public final long numRecordsParsed() {
        return this.numRecordsParsed.get();
    }

    public void setProcessDelayedRecords(boolean processDelayedRecords) {
        this.processDelayedRecords = processDelayedRecords;
        this.logger.info("<setProcessDelayedRecords> " + processDelayedRecords);
    }

    public void setParseFields(boolean parseFields) {
        this.parseFields = parseFields;
        this.logger.info("<setParseFields> " + this.parseFields);
    }

    public void setKeyConverter(MdpsKeyConverter keyConverter) {
        this.keyConverter = keyConverter;
    }

    private static final String GAUGE_RECORDS_PARSED_NAME = "mdps_feed_parser_gauge";
    private static final String GAUGE_RECORDS_FAILED_NAME = "mdps_feed_parser_failures_gauge";
    private static final String PARSE_TIMER_NAME = "mdps_feed_parser_timer";
    protected MeterRegistry meterRegistry;

    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void setMetricsPostfix(String metricsPostfix) {
        this.metricsPostfix = metricsPostfix;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        if (this.keyConverter == null) {
            this.keyConverter = new MdpsKeyConverter(this.processDelayedRecords);
        }

        String postfix = "";
        if (this.metricsPostfix != null) {
            postfix = "_" + this.metricsPostfix;
        }

        if (this.meterRegistry != null) {
            Gauge.builder(GAUGE_RECORDS_PARSED_NAME + postfix, () -> this.numRecordsParsed)
                .tags(Tags.of("t", "num_records_parsed")).register(this.meterRegistry);
            Gauge.builder(GAUGE_RECORDS_FAILED_NAME + postfix, () -> this.numParseErrors)
                .tags(Tags.of("t", "num_records_failed")).register(this.meterRegistry);
            final Timer parseTimer = Timer.builder(PARSE_TIMER_NAME + postfix)
                .tags(Tags.of("t", "duration_feed_parsed")).register(this.meterRegistry);
            this.setParseTimer(parseTimer);
        }
    }

    @Override
    public void parse(FeedRecord feedRecord) {
        final ByteBuffer buffer = feedRecord.getAsByteBuffer();
        final byte mdpsMessageType =
                buffer.get(feedRecord.getOffset() + MdpsMessageConstants.HEADER_MESSAGE_TYPE_OFFSET);

        buffer.position(feedRecord.getOffset() + MdpsMessageConstants.HEADER_LENGTH); // skip header

        final byte messageType = toXfeedMessageType(mdpsMessageType);
        if (!isAnyBuilderInterested(messageType) || !buffer.hasRemaining()) {
            return;
        }

        final short keyFieldid = buffer.getShort();
        if (keyFieldid != MdpsMessageConstants.MDPS_KEY_FID) {
            setParseProblem("Illegal fieldid for mdps key: " + keyFieldid);
            return;
        }

        int mdpsKeyLength = buffer.get() & 0xFF;
        final int typeMapping = MdpsKeyConverter.getMapping(buffer, mdpsKeyLength);
        final ByteString vwdcode = this.keyConverter.convert(buffer, mdpsKeyLength, typeMapping);

        if (vwdcode == null) {
            return;
        }

        this.parsedRecord.reset(feedRecord);
        this.parsedRecord.setMessageType(messageType);
        this.parsedRecord.setMessageTypeMdps(mdpsMessageType);
        this.parsedRecord.setMessageTimestamp(getMessageTimestamp());
        this.parsedRecord.setKeyType(typeMapping);

        final FeedData data = getFeedData(getVendorkey(vwdcode, typeMapping));
        if (data == null) {
            return;
        }

        if (this.parseFields && !parseFields(buffer, data)) {
            handleParseProblem(feedRecord, new InvalidFieldsException(data.getVendorkey().toString(),
                    getFieldException()));
            return;
        }

        build(data);
    }

    protected VendorkeyVwd getVendorkey(ByteString vwdcode, int typeMapping) {
        final VendorkeyVwd result = VendorkeyVwd.getInstance(vwdcode, typeMapping);
        if (result == VendorkeyVwd.ERROR) {
            throw new IllegalKeyException(vwdcode);
        }
        return result;
    }

    protected boolean parseFields(ByteBuffer buffer, FeedData data) {
        buffer.mark();
        while (buffer.hasRemaining()) {
            final int fieldId = buffer.getShort();

            if (fieldId == ID_SOURCE_ID) {
                buffer.get(); // ignore
                continue;
            }

            if (fieldId == ID_ADF_BOERSENZEIT) {
                final int time = getBoersenzeit(data, buffer.getInt());
                this.parsedRecord.setField(ID_ADF_BOERSENZEIT, time);
                continue;
            }

            if (!parseField(fieldId, buffer)) {
                return false;
            }
        }
        return true;
    }

    private int getBoersenzeit(FeedData data, final int mdpsTime) {
        // HACK 1: adjust Boersenzeit so that it is (hopefully) ME(S)Z.
        final int offset = data.getMarket().getBoersenzeitOffset();
        // time == 0 is used to clear the field, although it would be a valid time...
        // to keep the "clear" semantic, do not modify 0
        if (offset == 0 || mdpsTime == 0) {
            return mdpsTime;
        }
        final int adapted = adaptTime(mdpsTime, offset);
        return encodeTime(adapted, decodeTimeMillis(mdpsTime));
    }

    private int adaptTime(int mdpsTime, int offset) {
        int secondOfDay = decodeTime(mdpsTime);
        final int result = secondOfDay + offset;
        if (result >= SECONDS_PER_DAY) {
            return result - SECONDS_PER_DAY;
        }
        else if (result < 0) { //  for negative offsets
            return result + SECONDS_PER_DAY;
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        MdpsFeedParser p = new MdpsFeedParser();
        OrderedFeedUpdateBuilder b = new OrderedFeedUpdateBuilder();
        b.setBuilders(new OrderedUpdateBuilder[]{(data, update)
                -> System.out.println(update.toDebugString())});
        p.setAddToaAndDoa(false);
        p.setFeedBuilders(b);
        p.afterPropertiesSet();

        ByteBuffer bb = ByteBuffer.wrap(readAllBytes(new File("/Users/oflege/tmp/mdpserr.bin").toPath())).order(LITTLE_ENDIAN);

        SimpleMdpsRecordSource rs = new SimpleMdpsRecordSource(bb);

        while (rs.hasRemaining()) {
            FeedRecord fr = rs.getFeedRecord();
            p.parse(fr);
        }
    }
}
