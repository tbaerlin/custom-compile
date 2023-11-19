package de.marketmaker.istar.feed.dp.web;


import java.io.PrintWriter;
import java.util.BitSet;
import java.util.Iterator;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.marketmaker.istar.common.util.PriceFormatter;
import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.common.util.XmlUtil;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.vwd.EntitlementProvider;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

public class DpPricesFormatter {
    public static final BitSet DEFAULT_ESCAPE_NULLS = new BitSet();

    static {
        DEFAULT_ESCAPE_NULLS.set(VwdFieldDescription.ADF_Brief.id());
        DEFAULT_ESCAPE_NULLS.set(VwdFieldDescription.ADF_Geld.id());
    }

    private final DateTimeFormatter timestampFormat = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");

    private PriceFormatter pf = new PriceFormatter(5, 8);

    private EntitlementProvider entitlementProvider;

    public DpPricesFormatter() {
        pf.setShowTrailingZeros(false);
    }

    public void setEntitlementProvider(EntitlementProvider entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public void write(PrintWriter pw, FeedData data, Iterator<SnapField> it, boolean withTimeOfArrival, boolean withDateOfArrival, boolean useFieldIds,
            int timestamp) {
        final StringBuilder sb = new StringBuilder(80);
        sb.append("<element vkey=\"").append(XmlUtil.encode(getSymbolVwdfeed(data)))
                .append("\" entitlements=\"");
        appendEntitlements(data, sb);
        sb.append("\">");
        pw.println(sb.toString());
        if (timestamp != 0 && withTimeOfArrival) {
            appendFieldTag(pw, VwdFieldDescription.ADF_TIMEOFARR.id(), useFieldIds, false);
            pw.print(getFormattedValueTime(DateTimeProvider.Timestamp.decodeTime(timestamp)));
            appendFieldTag(pw, VwdFieldDescription.ADF_TIMEOFARR.id(), useFieldIds, true);
        }
        if (timestamp != 0 && withDateOfArrival) {
            appendFieldTag(pw, VwdFieldDescription.ADF_DATEOFARR.id(), useFieldIds, false);
            pw.print(getFormattedValueDate(DateTimeProvider.Timestamp.decodeDate(timestamp)));
            appendFieldTag(pw, VwdFieldDescription.ADF_DATEOFARR.id(), useFieldIds, true);
        }
        SnapField sf;
        while (it.hasNext()) {
            sf = it.next();
            appendFieldTag(pw, sf.getId(), useFieldIds, false);
            pw.print(getFormattedValue(sf));
            appendFieldTag(pw, sf.getId(), useFieldIds, true);
        }
        pw.println("</element>");
    }

    private String getSymbolVwdfeed(FeedData data) {
        // TODO: remove old type mapping as soon as mdp does not need it any more
        return ((VendorkeyVwd)data.getVendorkey()).toByteString(true).toString();
    }

    private void appendEntitlements(FeedData data, StringBuilder sb) {
        if (this.entitlementProvider != null) {
            final int[] entitlements = this.entitlementProvider.getEntitlements(data);
            for (int i = 0; i < entitlements.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(entitlements[i]);
            }
        }
    }

    private void appendFieldTag(PrintWriter pw, final int fid, boolean useFieldIds, boolean close) {
        pw.print(close ? "</" : "<");
        if (useFieldIds) {
            pw.append("ADF_").print(fid);
        }
        else {
            pw.print(VwdFieldDescription.getField(fid).name());
        }
        pw.print(">");
        if (close) {
            pw.println();
        }
    }

    protected String getFormattedValueTime(int timeValue) {
        return TimeFormatter.formatSecondsInDay(timeValue);
    }

    private String getFormattedValueDate(int dateValue) {
        return (dateValue % 100) + "." + ((dateValue % 10000) / 100) + "." + (dateValue / 10000);
    }

    /**
     * format given snap field.
     */
    protected String getFormattedValue(SnapField field) {
        VwdFieldDescription.Field f = VwdFieldDescription.getField(field.getId());
        switch (f.type()) {
            case PRICE:
                return pf.formatPrice(field.getPrice());
            case DATE:
                return getFormattedValueDate((Integer) field.getValue());
            case TIME:
                return getFormattedValueTime((Integer) field.getValue());
            case TIMESTAMP:
                final long timestamp = (Long) field.getValue();
                if (timestamp == 0) {
                    return "n/a";
                }
                return this.timestampFormat.print(timestamp);
            case UINT:
                final Number result = (Number) field.getValue();
                final long value = result.longValue() & 0xFFFFFFFFL;
                return Long.toString(value);
            default:
                return field.getValue() != null ? XmlUtil.encode(field.getValue().toString()) : "n/a";
        }
    }
}
