/*
 * StaticFeedUpdateBuilder.java
 *
 * Created on 16.07.14 13:35
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.staticfeed;

import java.util.Arrays;

import net.jcip.annotations.NotThreadSafe;

import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;
import de.marketmaker.istar.feed.ordered.OrderedFeedUpdateBuilder;
import de.marketmaker.istar.feed.ordered.OrderedSnapData;
import de.marketmaker.istar.feed.snap.SnapData;

import static de.marketmaker.istar.feed.mdps.MdpsTypeMappings.getMappingForMdpsType;
import static de.marketmaker.istar.feed.vwd.VwdFieldOrder.ORDER_BIG_SOURCE_ID;

/**
 * In static updates, the message's sourceId and the key's type are of special importance. To be
 * able to figure out which sources use which type for which symbol, a string field is used
 * to store, for each sourceId, the latest secType (the field's content looks like
 * <tt>srcId=sectype(,srcId=sectype)*</tt>). Changes to that field have to be propagated to
 * mdp update files. As each mdps message contains only a single sourceId and a single type,
 * the contents of that field have to be computed explicitly (i.e., cannot be copied from the
 * mdps message). If the new content of that field is different from the current one, this
 * class adds the new content to the update as the first field.
 * <p>
 * An instance must only be used by a single thread.
 *
 * @author oflege
 */
@NotThreadSafe
public class StaticFeedUpdateBuilder extends OrderedFeedUpdateBuilder {
    private final StringBuilder sb = new StringBuilder(32);

    // to be able to deal with sourceId/sectype typles, we encode them as ints
    // each element is (source id << 8) + vwd sectype
    // so that sorting the encoded numbers sorts the entries in the string field by sourceId.
    private final int[] sids = new int[64];

    // byte position in the snap data array of the current feed data
    private int ix;

    @Override
    protected void setFields(FeedData data, ParsedRecord pr) {
        final SnapData sd = data.getSnapData(true);
        if (sd != null && sd instanceof OrderedSnapData) {
            setSourceIdAndType(pr, (OrderedSnapData) sd);
        }
        super.setFields(data, pr);
    }

    private void setSourceIdAndType(ParsedRecord pr, OrderedSnapData sd) {
        final byte[] changedSourceIdAndType = ackSourceIdAndType(pr, sd.getData(false));
        if (changedSourceIdAndType != null) {
            setString(ORDER_BIG_SOURCE_ID, changedSourceIdAndType, 0, changedSourceIdAndType.length);
        }
    }

    private byte[] ackSourceIdAndType(ParsedRecord pr, byte[] data) {
        int numSids = parseSourceIds(data);
        int sourceId = pr.getSourceId();
        int sid = encodeTerm(sourceId, pr.getKeyType());
        if (numSids == 0) { // first
            this.sids[0] = sid;
            return formatSids(1);
        }
        for (int i = 0; i < numSids; i++) {
            if (this.sids[i] == sid) {
                // src sent same sectype as before
                return null;
            }
            if (decodeSourceId(this.sids[i]) == sourceId) {
                // src sent different sectype
                this.sids[i] = sid;
                return formatSids(numSids);
            }
        }
        // first time src sent anything for this key
        this.sids[numSids] = sid;
        Arrays.sort(this.sids, 0, numSids + 1);
        return formatSids(numSids + 1);
    }

    private byte[] formatSids(int n) {
        this.sb.setLength(0);
        for (int i = 0; i < n; i++) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(decodeSourceId(sids[i])).append('=').append(decodeMdpsType(decodeType(sids[i])));
        }
        return this.sb.toString().getBytes(SnapRecord.US_ASCII);
    }

    /**
     * Parses the current sourceId/Type tuples from data
     * @param data ordered snap record data
     * @return number of tuples stored in <tt>this.sids</tt>
     */
    private int parseSourceIds(byte[] data) {
        if (data == null) {
            return 0;
        }
        // fields stored in ordered snap record format. Since we only need the first field,
        // we avoid using a BufferFieldData object to read the field and thus avoid the
        // creation of a new ByteBuffer for each new message
        this.ix = 0;
        int i = data[this.ix++];
        int oid = i & 0x1F;
        if (i >= 0) {
            oid = (oid << 8) + (data[this.ix++] & 0xFF);
        }
        if (oid != ORDER_BIG_SOURCE_ID) {
            return 0;
        }
        int length = getStopBitEncodedLength(data);
        return parseSids(data, length);
    }

    /**
     * Parses the current sourceId/Type tuples from data
     * @param data ordered snap record data
     * @param length formatted string is stored at ix..ix+length in data
     * @return number of tuples stored in <tt>this.sids</tt>
     */
    private int parseSids(byte[] data, int length) {
        final int to = this.ix + length;
        int n = 0;
        while (this.ix < to && data[this.ix] != 0) {
            int srcId = 0;
            do {
                srcId = (10 * srcId) + (data[this.ix++] - '0');
            } while (data[this.ix] != '=');
            this.ix++; // skip '='
            final int mapping;
            if (this.ix + 1 == to || data[this.ix + 1] == ',' || data[this.ix + 1] == 0) {
                mapping = getMappingForMdpsType(0, data[this.ix++]);
            }
            else {
                mapping = getMappingForMdpsType(data[this.ix++], data[this.ix++]);
            }
            this.sids[n++] = encodeTerm(srcId, mapping);
            if (this.ix == to || data[this.ix] == 0) {
                return n;
            }
            this.ix++;
        }
        return n;
    }

    private static int encodeTerm(int srcId, int mapping) {
        return (srcId << 8) + (mapping & 0xFF);
    }

    private static int decodeSourceId(int encoded) {
        return encoded >> 8;
    }

    private static int decodeType(int encoded) {
        return encoded & 0xFF;
    }

    private static String decodeMdpsType(int encoded) {
        return encoded > 0 ? MdpsTypeMappings.getMdpsKeyTypeByVwdType(encoded) : "OT";
    }

    private int getStopBitEncodedLength(byte[] data) {
        byte b1 = data[this.ix++];
        if (b1 < 0) {
            return b1 & 0x7F;
        }
        return ((int) b1 << 7) | (data[this.ix++] & 0x7F);
    }

    @Override
    public void setTime(int orderId, int value) {
        if (isValidOrderId(orderId)) {
            super.setTime(orderId, value);
        }
    }

    @Override
    public void setInt(int orderId, int value) {
        if (isValidOrderId(orderId)) {
            super.setInt(orderId, value);
        }
    }

    @Override
    public void setPrice(int orderId, long value) {
        if (isValidOrderId(orderId)) {
            super.setPrice(orderId, value);
        }
    }

    @Override
    public void setString(int orderId, byte[] value, int start, int length) {
        if (isValidOrderId(orderId)) {
            super.setString(orderId, value, start, length);
        }
    }

    boolean isValidOrderId(int orderId) {
        return orderId >= ORDER_BIG_SOURCE_ID;
    }
}
