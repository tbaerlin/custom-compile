/*
 * SnapRecordUtils.java
 *
 * Created on 10.02.2005 12:23:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.snap;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.FieldTypeEnum;
import de.marketmaker.istar.domain.data.LiteralSnapField;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.domain.data.SnapRecord.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnapRecordUtils {

    private static final VwdFieldDescription.Field[] TRADE_TIME_FIELDS
            = new VwdFieldDescription.Field[]{
            VwdFieldDescription.MMF_Boersenzeit,
            VwdFieldDescription.ADF_Bezahlt_Zeit
    };

    private static final VwdFieldDescription.Field[] TIME_FIELDS
            = new VwdFieldDescription.Field[]{
            VwdFieldDescription.ADF_Zeit_Quotierung,
            VwdFieldDescription.ADF_Boersenzeit,
            VwdFieldDescription.ADF_Zeit
    };

    private SnapRecordUtils() {
    }

    public static final Comparator<SnapField> BY_ORDER
            = (sf1, sf2) -> Integer.compare(order(sf1), order(sf2));

    public static final Comparator<SnapRecord> COMPARATOR_LAST_TIME
            = (s1, s2) -> -Integer.compare(getTime(s1), getTime(s2));

    public static final Comparator<SnapRecord> COMPARATOR_LAST = (s1, s2) -> {
        int cmp = compare(s1, s2, VwdFieldDescription.MMF_Bezahlt_Datum);
        if (cmp != 0) {
            return cmp;
        }

        cmp = compare(s1, s2, VwdFieldDescription.MMF_Boersenzeit);
        return (cmp != 0) ? cmp : COMPARATOR_LAST_TIME.compare(s1, s2);
    };

    public static final Comparator<SnapRecord> COMPARATOR_VOLUME
            = (s1, s2) -> compare(s1, s2, VwdFieldDescription.ADF_Umsatz_gesamt);

    public static final Comparator<SnapRecord> COMPARATOR_NUM_TRADES =
            (s1, s2) -> compare(s1, s2, VwdFieldDescription.ADF_Anzahl_Handel);

    private static int compare(SnapRecord s1, SnapRecord s2, VwdFieldDescription.Field f) {
        return compareLongDesc(s1.getField(f.id()), s2.getField(f.id()));
    }

    private static int compareLongDesc(SnapField sf1, SnapField sf2) {
        if (sf1.isDefined() && sf2.isDefined()) {
            return -Long.compare(getLong(sf1), getLong(sf2));
        }
        else if (sf1.isDefined()) {
            return -1;
        }
        else if (sf2.isDefined()) {
            return 1;
        }
        return 0;
    }

    /**
     * @return Charset for text fields
     * @see <a href="http://electra2/wiki/doku.php?id=technik:cps:documentation:news">Doku</a>
     */
    public static Charset getCharset(SnapRecord sr) {
        final int i = getInt(sr, VwdFieldDescription.NDB_ContentDescriptor.id());
        return (i != Integer.MIN_VALUE) ? getCharset(i) : DEFAULT_CHARSET;

    }

    public static Charset getCharset(int ndbContentDescriptor) {
        // charset is encoded in lower 8 bits of the field
        switch (ndbContentDescriptor & 0xFF) {
            case 0:
                return ISO_8859_1;
            case 1:
                return UTF_8;
            case 2:
                return CP_1252;
            case 3:
                return US_ASCII;
            default:
                return DEFAULT_CHARSET;
        }
    }


    public static boolean hasTradeToday(SnapRecord sr) {
        final SnapField sfBezahltDatum = sr.getField(VwdFieldDescription.MMF_Bezahlt_Datum.id());
        return !sfBezahltDatum.isDefined()
                || DateUtil.dateToYyyyMmDd(new Date()) == getLong(sfBezahltDatum);
    }


    public static int getTime(SnapRecord sr) {
        return getTime(sr, getTimeOfArrival(sr));
    }

    public static int getTime(SnapRecord sr, int defaultValue) {
        for (VwdFieldDescription.Field f : TIME_FIELDS) {
            final SnapField sf = sr.getField(f.id());
            if (sf.isDefined() && getInt(sf) > 0) {
                return getInt(sf);
            }
        }
        return defaultValue;
    }

    public static int getDate(SnapRecord sr) {
        final SnapField sf = sr.getField(VwdFieldDescription.ADF_Datum.id());
        if (sf.isDefined() && getInt(sf) > 0) {
            return getInt(sf);
        }

        return getDateOfArrival(sr);
    }

    public static int getTimeOfArrival(SnapRecord sr) {
        final SnapField sf = sr.getField(VwdFieldDescription.ADF_TIMEOFARR.id());
        if (sf.isDefined()) {
            return getInt(sf);
        }
        return 0;
    }

    public static int getDateOfArrival(SnapRecord sr) {
        final SnapField sf = sr.getField(VwdFieldDescription.ADF_DATEOFARR.id());
        if (sf.isDefined()) {
            return getInt(sf);
        }
        return 0;
    }

    public static int getTradeTime(SnapRecord sr) {
        return getTradeTime(sr, false);
    }

    public static int getTradeTime(SnapRecord sr, boolean ignoreMmfBoersenzeit) {
        for (int i = ignoreMmfBoersenzeit ? 1 : 0; i < TRADE_TIME_FIELDS.length; i++) {
            final SnapField sf = sr.getField(TRADE_TIME_FIELDS[i].id());
            if (sf.isDefined() && getInt(sf) > 0) {
                return getInt(sf);
            }
        }
        return getTime(sr, 0);
    }

    public static long getLong(SnapRecord sr, int field) {
        return getLong(sr.getField(field));
    }

    public static long getLong(SnapField field) {
        if (field.isDefined()) {
            final Object value = field.getValue();
            if (value instanceof BigDecimal) {
                return getBigDecimalAsLong((BigDecimal) value);
            }
            else if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            else if (field.getType() == FieldTypeEnum.STRING) { // e.g. for fields like ADF_Umsatz_gesamt_in_Whrg
                final String str = getString(field);
                if (StringUtils.hasText(str)) {
                    try {
                        return new BigDecimal(str.trim()).movePointRight(5).longValue();
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
        }
        return Long.MIN_VALUE;
    }

    private static long getBigDecimalAsLong(BigDecimal bd) {
        // encode in same way as does de.marketmaker.istar.feed.mdps.MdpsFeedUtils#getMdpsPrice
        // unscaled base in bits 31..0 and exponent in bits 39..32
        final int unscaled = bd.unscaledValue().intValue();
        return unscaled + (((long) -bd.scale()) << 32 & 0xFF00000000L);
    }

    public static double getPrice(SnapRecord sr, int field) {
        return getPrice(sr.getField(field));
    }

    public static double getPrice(SnapField field) {
        if (field.isDefined() && field.getValue() instanceof Number) {
            return field.getPrice().doubleValue();
        }
        return Double.NaN;
    }

    public static int getInt(SnapRecord sr, int field) {
        return getInt(sr.getField(field));
    }

    public static int getInt(SnapField field) {
        if (field.isDefined() && field.getValue() instanceof Number) {
            return ((Number) field.getValue()).intValue();
        }
        return Integer.MIN_VALUE;
    }

    public static String getString(SnapRecord sr, int field) {
        return getString(sr.getField(field));
    }

    public static String getString(SnapField field) {
        return (field != null && field.isDefined()) ? field.getValue().toString() : null;
    }

    public static ArrayList<SnapField> toOrderedFields(SnapRecord snapRecord) {
        ArrayList<SnapField> result = new ArrayList<>(snapRecord.getSnapFields());
        result.sort(BY_ORDER);
        return result;
    }

    public static Map<Integer, SnapField> getFieldsAsMap(SnapRecord sr) {
        HashMap<Integer, SnapField> result = new HashMap<>();
        for (SnapField sf : sr.getSnapFields()) {
            result.put(sf.getId(), sf);
        }
        return result;
    }

    public static int order(SnapField sf) {
        return VwdFieldOrder.getOrder(sf.getId());
    }


    public static void main(String[] args) {
        final SnapField sf = LiteralSnapField.createString(VwdFieldDescription.ADF_Umsatz_gesamt_in_Whrg.id(), "15.656");
        System.out.println(getLong(sf));

        final SnapField sf2 = LiteralSnapField.createPrice(VwdFieldDescription.ADF_Bezahlt.id(), new BigDecimal("12.5"));
        System.out.println(getLong(sf2));
    }
}
