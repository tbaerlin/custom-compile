/*
 * SnapFieldVwdFormatter.java
 *
 * Created on 28.04.2005 14:23:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.joda.time.LocalTime;

import de.marketmaker.istar.common.util.DateFormatter;
import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.feed.AsciiBytes;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnapFieldVwdFormatter {

    private final DecimalFormat DF = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SnapFieldVwdFormatter() {
        DF.applyLocalizedPattern("0.00######");
    }

    public String formatValue(SnapField sf) {
        if (!sf.isDefined()) {
            return "n/a";
        }
        final VwdFieldDescription.Field f = VwdFieldDescription.getField(sf.getId());
        switch(f.type()) {
            case DATE:
                return DateFormatter.formatYyyymmdd(((Number)sf.getValue()).intValue());
            case TIME:
                if (sf.getValue() instanceof LocalTime) {
                    return String.valueOf(sf.getValue());
                }
                return TimeFormatter.formatSecondsInDay(((Number)sf.getValue()).intValue());
            case UINT:
                if (f == VwdFieldDescription.BIG_SOURCE_ID) {
                    // hack for hack in staticfeed, value for BIG_SOURCE_ID is stored as string
                    return String.valueOf(sf.getValue());
                }
                // intentional fall-through
            case USHORT:
                return Long.toString(Integer.toUnsignedLong(((Number) sf.getValue()).intValue()));
            case STRING:
                // Counter-hack because this is actually a uint64_t in a FLSTRING(8)
                if (f == VwdFieldDescription.ADF_Total_Volume || f == VwdFieldDescription.ADF_Total_Volume_prev) {
                    ByteBuffer bb = ByteBuffer.wrap(((String)sf.getValue()).getBytes(AsciiBytes.CP_1252)).order(ByteOrder.LITTLE_ENDIAN);
                    // This is a simplication until we exceed (2^63)-1 - if that ever happens we need to switch to BigInteger instead
                    return Long.toString(bb.getLong());
                }
                return sf.getValue().toString();
            case PRICE:
                return formatPrice(sf.getPrice());
            case TIMESTAMP:
                final long ts = ((Number)sf.getValue()).longValue();
                return (ts != 0) ? sdf.format(new Date(ts)) : "n/a";
            default:
                throw new IllegalArgumentException("unknown field type: " + f);
        }
    }

    public String formatPrice(final BigDecimal price) {
        return DF.format(price);
    }
}
