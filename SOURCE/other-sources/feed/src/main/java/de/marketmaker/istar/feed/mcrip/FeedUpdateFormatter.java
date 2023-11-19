/*
 * FeedUpdateFormatter.java
 *
 * Created on 26.06.14 07:54
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mcrip;

import java.math.BigDecimal;

import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldData;
import de.marketmaker.istar.feed.ordered.OrderedSnapRecord;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * @author oflege
 */
public class FeedUpdateFormatter {
    private final boolean withSourceId;

    private StringBuilder line = new StringBuilder();

    public FeedUpdateFormatter(boolean withSourceId) {
        this.withSourceId = withSourceId;
    }

    public String format(FeedData data, OrderedUpdate update) {
        // 20130114 02:44:56 11 A E 1 26=02:44:56;82=02:44:56;857=02:44:56;28=11.03;37=02:44:56;29=3000
        this.line.setLength(0);
        if (update.getTimestamp() != 0) {
            final int yyyyMMdd = update.getDate();
            final int secInDay = update.getTime();

            this.line.append(yyyyMMdd).append(' ')    // 20130114
                    .append(TimeFormatter.formatSecondsInDay(secInDay)).append(' ');   // 02:44:56
        }
        this.line.append((int) update.getMdpsMsgType())                // 11
                .append(' ').append((char) update.getMsgType())                   // A
                .append(' ').append(MdpsTypeMappings.getMdpsKeyTypeById(update.getMdpsKeyType())) // E
                .append(' ').append(update.getVwdKeyType());                      // 1
        this.line.append(' ');

        if (this.withSourceId) {
            this.line.append(update.getSourceId());
        }
        else {
            this.line.append(update.hasFlag(FeedUpdateFlags.FLAG_WITH_TRADE) ? 'T' : '-');
            this.line.append(update.hasFlag(FeedUpdateFlags.FLAG_WITH_ASK) ? 'A' : '-');
            this.line.append(update.hasFlag(FeedUpdateFlags.FLAG_WITH_BID) ? 'B' : '-');
            this.line.append(update.hasFlag(FeedUpdateFlags.FLAG_WITH_TICK_FIELD) ? 'X' : '-');
        }

        if (data != null) {
            this.line.append(' ').append(data.getVwdcode());
        }

        char sep = ' ';
        BufferFieldData fd = update.getFieldData();
        while (fd.readNext() != 0) {
            VwdFieldDescription.Field field = VwdFieldOrder.getField(fd.getId());
            if (field == null) {
                fd.skipCurrent();
                continue;
            }
            this.line.append(sep).append(field.id()).append('=');
            sep = ';';
            switch (fd.getType()) {
                case FieldData.TYPE_INT:
                    if (field.type() == VwdFieldDescription.Type.UINT) {
                        this.line.append(fd.getUnsignedInt());
                    } else {
                        this.line.append(fd.getInt());
                    }
                    break;
                case FieldData.TYPE_TIME:
                    this.line.append(formatMdpsTime(fd.getInt()));
                    break;
                case FieldData.TYPE_PRICE:
                    this.line.append(BigDecimal.valueOf(fd.getInt(), -fd.getByte()).toPlainString());
                    break;
                case FieldData.TYPE_STRING:
                    if (fd.getLength() == 0) {
                        this.line.append("");
                    }
                    else {
                        this.line.append('"').append(OrderedSnapRecord.getFieldValue(fd)).append('"');
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown type: " + fd.getType());
            }
        }
        return this.line.toString();
    }

    private static String formatMdpsTime(int time) {
        int hh = time >>> 27;
        int mm = (time >>> 21) & 0x3F;
        int ss = (time >>> 15) & 0x3F;
        int ms = time & 0x7FFF;
        final StringBuilder sb = new StringBuilder(8 + (ms != 0 ? 4 : 0));
        append(sb, hh).append(':');
        append(sb, mm).append(':');
        append(sb, ss);
        if (ms != 0) {
            sb.append('.');
            if (ms < 100) sb.append('0');
            append(sb, ms);
        }
        return sb.toString();
    }

    private static StringBuilder append(StringBuilder sb, int v) {
        if (v < 10) {
            sb.append('0');
        }
        sb.append(v);
        return sb;
    }
}
