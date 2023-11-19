/*
 * MdpsMessageAppender.java
 *
 * Created on 10.04.15 13:36
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.ordered.FieldData;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;
import de.marketmaker.istar.mdps.util.OrderedEntitlementProvider;

import static de.marketmaker.istar.feed.mdps.MdpsTypeMappings.getMdpsKeyTypeSuffixById;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ADF_Status;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ADF_Tick;

/**
 * Appends mdps messages (protocol version 3) to a given buffer.
 * @author oflege
 */
class MdpsMessageAppender {

    private final int sourceId;

    private final OrderedEntitlementProvider entitlementProvider;

    private final boolean usePrefix;

    private BitSet filter;

    MdpsMessageAppender(int sourceId, OrderedEntitlementProvider entitlementProvider,
            boolean usePrefix) {
        this.sourceId = sourceId;
        this.entitlementProvider = entitlementProvider;
        this.usePrefix = usePrefix;
    }

    boolean append(ByteBuffer bb, OrderedFeedData data, FieldData fd, final int msgType) {
        this.filter = getFilter(data);
        if (this.filter == null) {
            return false;
        }

        final int p = bb.position();

        bb.position(p + 2);
        bb.put((byte) msgType);
        bb.put((byte) 0); // TODO version
        bb.putShort((short) 0); // TODO target pid

        bb.putShort((short) MdpsMessageConstants.MDPS_KEY_FID);

        ByteString suffix = getMdpsKeyTypeSuffixById(data.getVendorkey().getMdpsType());
        ByteString vwdcode = data.getVendorkey().toVwdcode();
        bb.put((byte) ((this.usePrefix ? 2 : 0) + vwdcode.length() + suffix.length()));
        if (this.usePrefix) {
            bb.put(MdpsMessageConstants.MDPS_EOD_KEY_PREFIX1);
            bb.put(MdpsMessageConstants.MDPS_EOD_KEY_PREFIX2);
        }
        vwdcode.writeTo(bb, ByteString.LENGTH_ENCODING_NONE);
        suffix.writeTo(bb, ByteString.LENGTH_ENCODING_NONE);

        if (this.sourceId != 0) {
            bb.putShort((short) VwdFieldDescription.ID_BIG_SOURCE_ID);
            bb.putShort((short) this.sourceId);
        }

        if (!appendFields(bb, fd)) {
            bb.position(p);
            return false;
        }

        bb.putShort(p, (short) (bb.position() - p));
        return true;
    }

    protected BitSet getFilter(OrderedFeedData data) {
        return this.entitlementProvider.getFields(data);
    }

    private boolean appendFields(ByteBuffer bb, FieldData fd) {
        final int p = bb.position();
        for (int oid = fd.readNext(); oid > 0; oid = fd.readNext()) {
            if (!this.filter.get(oid)) {
                fd.skipCurrent();
                continue;
            }
            VwdFieldDescription.Field f = getField(oid);
            if (f == null) {
                fd.skipCurrent();
                continue;
            }
            bb.putShort((short) f.id());
            switch (f.mdpsType()) {
                case USHORT:
                    bb.putShort((short) fd.getInt());
                    break;
                case SIZE:
                case TIME:
                    bb.putInt(fd.getInt());
                    break;
                case DATE:
                    int date = fd.getInt();
                    if (date != 0 && date < 100000000) {
                        // date is in yyyymmdd encoding, recode as mdps date
                        MdpsFeedUtils.putMdpsDate(bb, date);
                    }
                    else {
                        bb.putInt(date);
                    }
                    break;
                case PRICE:
                    bb.putInt(fd.getInt());
                    bb.put(fd.getByte());
                    break;
                case FLSTRING:
                    if (f == ADF_Status || f == ADF_Tick) {
                        bb.putShort((short) fd.getInt());
                    }
                    else {
                        bb.put(getBytesWithLength(fd, f.length()));
                    }
                    break;
                case VLSHSTRING:
                    appendString(bb, fd.getBytes(), false);
                    break;
                case VLLGSTRING:
                    appendString(bb, fd.getBytes(), true);
                    break;
                default:
                    fd.skipCurrent();
                    break;
            }
        }
        return p != bb.position();
    }

    private VwdFieldDescription.Field getField(int oid) {
        return VwdFieldOrder.getField(oid);
    }

    private void appendString(ByteBuffer bb, byte[] s, boolean large) {
        final int len = getLength(s);
        if (large) {
            bb.putShort((short) len);
        }
        else {
            bb.put((byte) len);
        }
        bb.put(s, 0, len);
    }

    private int getLength(byte[] s) {
        for (int i = 0; i < s.length; i++) {
            if (s[i] == 0) {
                return i;
            }
        }
        return s.length;
    }

    private byte[] getBytesWithLength(FieldData fd, int n) {
        final byte[] result = fd.getBytes();
        return (result.length == n) ? result : Arrays.copyOf(result, n);
    }
}
