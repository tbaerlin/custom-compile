/*
 * MdpsDelayer.java
 *
 * Created on 25.08.2006 08:02:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.nio.ByteBuffer;

import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.feed.FeedBuilder;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.delay.Delayer;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;

/**
 * A Delayer subclass that acts as a FeedBuilder and understands different mdps protocol versions
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class MdpsDelayer extends Delayer implements FeedBuilder {

    private VendorkeyFilter statsFilter;

    public void setStatsFilter(VendorkeyFilter statsFilter) {
        this.statsFilter = statsFilter;
    }

    public byte[] getApplicableMessageTypes() {
        return VwdFeedConstants.getXfeedDynamicAndStaticAndRatios();
    }

    public void process(FeedData data, ParsedRecord pr) {
        process(pr, data, pr.getRecord().getAsByteBuffer());
    }

    public void setProtocolVersion(int protocolVersion) {
        if (protocolVersion != 1 && protocolVersion != 3) {
            throw new IllegalArgumentException("unsupported " + protocolVersion);
        }
        this.byteOrder = MdpsFeedUtils.getByteOrder(protocolVersion);
        this.logger.info("<setProtocolVersion> = " + protocolVersion);
    }

    protected void limitBufferToNextRecord(ByteBuffer bb) {
        // buffer is supposed to start with mdps record header which starts with length:
        final int pos = bb.position();
        bb.limit(pos + MdpsFeedUtils.getUnsignedShort(bb, pos));
    }
}
