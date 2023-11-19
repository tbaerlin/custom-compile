/*
 * FieldDataBuilder.java
 *
 * Created on 30.10.13 10:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import net.jcip.annotations.NotThreadSafe;

import de.marketmaker.istar.common.util.HexDump;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldData;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;
import de.marketmaker.istar.mdps.util.CompactBitSet;
import de.marketmaker.istar.mdps.util.EntitledFieldGroup;
import de.marketmaker.istar.mdps.util.FieldFormatter;
import de.marketmaker.istar.mdps.util.OrderedEntitlementProvider;

import static de.marketmaker.istar.domain.data.SnapRecord.CP_1252;
import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.hasZeroBit;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.Type.DATE;
import static de.marketmaker.istar.mdps.iqs.Constants.FID_PERMISSION;
import static de.marketmaker.istar.mdps.iqs.Constants.GS;

/**
 * Creates a message fragment that starts wih a FID_PERMISSION header field which is followed
 * by a message body that contains envelopes for each entitled field group. Each envelope
 * starts with a single FID_PERMISSION field that specifies a single field group, the envelope's
 * body contains the respective vwd fields that belong to the field group.
 * <p>
 * The fragment can be reused when data is forwarded to multiple subscribed clients as it
 * does not contain any client specific data.
 * @author oflege
 */
@NotThreadSafe
class EntitledFieldsBuilder {

    private static final byte[][] QUALITY_SUFFIXES = new byte[][] {
            null,
            null,
            ":2".getBytes(CP_1252),
            ":3".getBytes(CP_1252)
    };

    static final byte[][] QUALITIES = new byte[][]{
            null,
            new IqsMessageBuilder(8).header(1772, "RT").asBytes(),
            new IqsMessageBuilder(8).header(1772, "DD").asBytes(),
            new IqsMessageBuilder(9).header(1772, "EOD").asBytes(),
    };

    private static final byte[] ZERO_PRICE = new byte[]{'0'};

    private static final int MAX_PERMISSION_LENGTH = 128;

    /**
     * Pre-rendered RS|<em>fid</em>|FS for all vwd fieldIds, indexed by order id. ca 5% faster
     * than rendering each fid field header on demand.
     */
    private static byte[][] FIDS = new byte[VwdFieldOrder.MAX_ORDER + 1][];

    private static final BitSet DATE_FIELD_OIDS = new BitSet(VwdFieldOrder.MAX_ORDER);

    static {
        final IqsMessageBuilder b = new IqsMessageBuilder(6);
        for (int oid = 1; oid <= VwdFieldOrder.MAX_ORDER; oid++) {
            final int fid = VwdFieldOrder.getFieldId(oid);
            if (fid > 0) {
                FIDS[oid] = b.clear().header(fid).asBytes();
                if (VwdFieldDescription.getField(fid).type() == DATE) {
                    DATE_FIELD_OIDS.set(oid);
                }
            }
        }
    }


    private final OrderedEntitlementProvider entitlementProvider;

    private final ByteBuffer bb = ByteBuffer.allocate(1 << 16);

    private final IqsMessageBuilder builder = new IqsMessageBuilder(bb);

    private final ByteBuffer permissions = ByteBuffer.allocate(MAX_PERMISSION_LENGTH);

    private final IqsMessageBuilder pbuilder = new IqsMessageBuilder(permissions);

    private final FieldFormatter formatter;

    /**
     * a BitSet-like structure that captures the order ids of the current message
     */
    private final long[] fields = new long[(VwdFieldOrder.MAX_ORDER >> 6) + 1];

    /**
     * max used index of {@link #fields} that is currently in use
     */
    private int fieldsInUse;

    /**
     * last cached date; dates for the current or previous day occur most frequently and
     * don't change a lot
     */
    private int lastDate = 0;

    /**
     * last cached date representation
     */
    private final byte[] date = new byte[10];

    /**
     * last cached time; updates usually contain a few time fields with the same value
     */
    private int lastTime = -1;

    /**
     * last cached time representation
     */
    private final byte[] time;

    /**
     * buffer for rendering prices
     */
    private final byte[] price = new byte[32];

    private BufferFieldData bfd = new BufferFieldData();

    /**
     * strings in update messages are always as long as indicated by the field; strings in snap
     * data structures may be shorter and the end of the actual string is marked with a 0 byte.
     * Since the latter are only needed for initial recaps, we use this flag to avoid searching
     * for 0s in the common case (i.e., formatting subscribed data based on an update message).
     */
    private boolean find0StringEnd = false;

    /**
     * position in {@link #permissions} after the FS of the FID_PERMISSION field; the content
     * in that buffer from <tt>0..permissionsOffset</tt> is immutable.
     */
    private final int permissionsOffset;

    private final byte[] qualitySuffix;

    private final byte[] adfQualitaet;

    EntitledFieldsBuilder(OrderedEntitlementProvider entitlementProvider, FieldFormatter formatter, int quality) {
        this.entitlementProvider = entitlementProvider;
        this.formatter = formatter;
        this.time = new byte[formatter.getTimeLength()];

        // put body separator after permissions
        this.bb.put(MAX_PERMISSION_LENGTH, GS);

        this.pbuilder.header(FID_PERMISSION);
        this.permissionsOffset = this.permissions.position();

        this.qualitySuffix = QUALITY_SUFFIXES[quality];
        this.adfQualitaet = QUALITIES[quality];
    }

    /**
     * Creates a buffer containing a header field (FID_PERMISSION) followed by the
     * body with envelopes for all fields contained in data's realtime snap. The buffer's
     * mark will be set to the beginning of the data, so that the buffer can be reset
     * to read it multiple times.
     * @param data
     * @param withAllFields
     * @return buffer with formatted data or null if data does not match any entitlement rule.
     */
    ByteBuffer formatMessage(IqsFeedData data, boolean withAllFields) {
        this.find0StringEnd = true;
        this.bfd.reset(data.getSnapData(true).getData(false));
        ByteBuffer result;
        if (withAllFields) {
            result = formatMessageWithAllFields(data, this.bfd);
        }
        else {
            result = formatMessage(data, this.bfd);
        }
        this.find0StringEnd = false;
        return result;
    }

    /**
     * Creates a buffer containing a header field (FID_PERMISSION) followed by the
     * body with envelopes for the various fields contained in fieldData. The buffer's
     * mark will be set to the beginning of the data, so that the buffer can be reset
     * to read it multiple times.
     * @param data
     * @param fd contains fields to be formatted
     * @return buffer with formatted data or null if data does not match any entitlement rule.
     */
    ByteBuffer formatMessage(IqsFeedData data, final BufferFieldData fd) {
        final EntitledFieldGroup[] fieldGroups = getEntitledFieldGroups(data);
        if (fieldGroups == null) {
            return null;
        }

        return formatMessage(fd, fieldGroups);
    }

    private ByteBuffer formatMessageWithAllFields(IqsFeedData data, final BufferFieldData fd) {
        final EntitledFieldGroup[] tmp = getEntitledFieldGroups(data);
        return formatMessage(fd, appendAllFieldGroupTo(tmp));
    }

    private EntitledFieldGroup[] appendAllFieldGroupTo(EntitledFieldGroup[] tmp) {
        if (tmp == null) {
            return new EntitledFieldGroup[] { EntitledFieldGroup.ALL };
        }
        final EntitledFieldGroup[] result = Arrays.copyOf(tmp, tmp.length + 1);
        result[tmp.length] = EntitledFieldGroup.ALL;
        return result;
    }

    private ByteBuffer formatMessage(BufferFieldData fd, EntitledFieldGroup[] fieldGroups) {
        this.permissions.clear().position(this.permissionsOffset);
        // position buffer so that permissions can be prepended later on
        this.bb.clear().position(MAX_PERMISSION_LENGTH + 1); // +1 to leave GS untouched

        initFields(fd);

        for (EntitledFieldGroup fg : fieldGroups) {
            final CompactBitSet ef = fg.getFieldOrders();

            if (!ef.intersects(this.fields, this.fieldsInUse)) {
                continue;
            }

            openEnvelope(fg);

            final int min = ef.getMin();
            final int max = ef.getMax();

            for (int oid = fd.rewind().readNext(); oid > 0 && oid <= max; oid = fd.readNext()) {
                if (oid < min || !ef.get(oid)) {
                    fd.skipCurrent();
                    continue;
                }
                appendField(fd);
            }
            bb.put(this.adfQualitaet);
            closeEnvelope();
        }

        this.permissions.flip();
        this.bb.flip().position(MAX_PERMISSION_LENGTH - this.permissions.remaining());
        this.bb.mark();
        this.bb.put(this.permissions);
        this.bb.reset();

        return this.bb;
    }

    private void initFields(BufferFieldData fd) {
        this.fieldsInUse = 1;
        this.fields[0] = 0;
        for (int oid = fd.readNext(); oid > 0; oid = fd.readNext()) {
            final int index = oid >> 6;
            while (fieldsInUse <= index) {
                fields[fieldsInUse++] = 0;
            }
            this.fields[index] |= (1L << oid);
            fd.skipCurrent();
        }
    }

    private EntitledFieldGroup[] getEntitledFieldGroups(IqsFeedData data) {
        return data.getFieldGroups(this.entitlementProvider);
    }

    private void appendField(BufferFieldData fd) {
        switch (fd.getType()) {
            case FieldData.TYPE_INT:
                appendInt(fd);
                break;
            case FieldData.TYPE_PRICE:
                appendPrice(fd);
                break;
            case FieldData.TYPE_STRING:
                appendString(fd);
                break;
            case FieldData.TYPE_TIME:
                appendTime(fd);
                break;
        }
    }

    private void appendPrice(BufferFieldData fd) {
        final int base = fd.getInt();
        final int exp = fd.getByte();
        if (hasZeroBit(exp)) {
            appendFid(fd).putValue(ZERO_PRICE);
        }
        else {
            final int from = this.formatter.renderPrice(base, exp, this.price);
            appendFid(fd).putValue(this.price, from, this.price.length - from);
        }
    }

    private void appendTime(BufferFieldData fd) {
        formatTime(fd.getInt());
        appendFid(fd).putValue(time);
    }

    private void formatTime(int mdpsTime) {
        if (mdpsTime != lastTime) {
            this.lastTime = mdpsTime;
            this.formatter.renderMdpsTime(mdpsTime, time);
        }
    }

    private void appendInt(BufferFieldData fd) {
        final int oid = fd.getId();
        if (DATE_FIELD_OIDS.get(oid)) {
            formatDate(fd.getInt());
            appendFid(oid).putValue(date);
        }
        else {
            appendFid(oid).putValue(fd.getInt());
        }
    }

    private void formatDate(int yyyymmdd) {
        if (lastDate != yyyymmdd) {
            lastDate = yyyymmdd;
            this.formatter.renderDate(yyyymmdd, this.date);
        }
    }

    private void appendString(BufferFieldData fd) {
        final byte[] stringBytes = fd.getBytes();
        appendFid(fd).putValue(stringBytes, 0, findEnd(stringBytes));
    }

    private IqsMessageBuilder appendFid(BufferFieldData fd) {
        return appendFid(fd.getId());
    }

    private IqsMessageBuilder appendFid(int oid) {
        return this.builder.putValue(FIDS[oid]);
    }

    private int findEnd(byte[] bytes) {
        if (this.find0StringEnd) {
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == 0) {
                    return i;
                }
            }
        }
        return bytes.length;
    }

    private void closeEnvelope() {
        this.builder.closeEnvelope();
    }

    private void openEnvelope(EntitledFieldGroup g) {
        this.builder.openEnvelope().permission(g.getSelectorStr(), this.qualitySuffix).body();
        addPermission(g);
    }

    private void addPermission(EntitledFieldGroup g) {
        if (this.permissions.position() > this.permissionsOffset) {
            this.pbuilder.putValue((byte) ',');
        }
        this.pbuilder.permissionValue(g.getSelectorStr(), this.qualitySuffix);
    }

    public static void main(String[] args) {
        System.out.println(HexDump.toHex(QUALITIES[1]));
    }
}
