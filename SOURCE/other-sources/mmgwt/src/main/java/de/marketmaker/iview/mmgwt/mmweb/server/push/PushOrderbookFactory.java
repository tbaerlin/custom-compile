/*
 * PushPriceFactory.java
 *
 * Created on 10.02.2010 08:52:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Locale;
import java.util.Objects;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushOrderbook;

import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.getFieldByName;

/**
 * @author oflege
 */
class PushOrderbookFactory {
    private static final DecimalFormat priceRenderer
            = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    static {
        priceRenderer.applyLocalizedPattern("0.#####");
    }

    private static final int BEST_COUNT = 20;

    private static final int[] BEST_BID_IDS = new int[BEST_COUNT];

    private static final int[] BEST_BID_SIZE_IDS = new int[BEST_COUNT];

    private static final int[] BEST_ASK_IDS = new int[BEST_COUNT];

    private static final int[] BEST_ASK_SIZE_IDS = new int[BEST_COUNT];

    static {
        for (int i = 0; i < BEST_COUNT; i++) {
            final int j = i + 1;
            BEST_ASK_IDS[i] = getFieldByName("ADF_Best_Ask_" + j).id();
            BEST_ASK_SIZE_IDS[i] = getFieldByName("ADF_Best_Ask_" + j + "_Size").id();
            BEST_BID_IDS[i] = getFieldByName("ADF_Best_Bid_" + j).id();
            BEST_BID_SIZE_IDS[i] = getFieldByName("ADF_Best_Bid_" + j + "_Size").id();
        }
    }

    static final BitSet BEST_FIELDS = new BitSet(VwdFieldDescription.ADF_Best_Ask_20_Size.id());

    static final BitSet ALLOWED_FIELDS = new BitSet(VwdFieldDescription.ADF_Best_Ask_20_Size.id());

    static {
        for (int id : BEST_ASK_IDS) {
            BEST_FIELDS.set(id);
        }
        for (int id : BEST_ASK_SIZE_IDS) {
            BEST_FIELDS.set(id);
        }
        for (int id : BEST_BID_IDS) {
            BEST_FIELDS.set(id);
        }
        for (int id : BEST_BID_SIZE_IDS) {
            BEST_FIELDS.set(id);
        }

        ALLOWED_FIELDS.or(BEST_FIELDS);
        ALLOWED_FIELDS.set(VwdFieldDescription.ADF_DATEOFARR.id());
        ALLOWED_FIELDS.set(VwdFieldDescription.ADF_TIMEOFARR.id());
    }

    private static final String[][] EMPTY = new String[][]{new String[0], new String[0]};

    private PushOrderbookFactory() {
    }

    static PushOrderbook create(SnapRecord sr) {
        final PushOrderbook result = new PushOrderbook();

        final String[][] bidPricesAndVolumes = createArray(sr, true);
        result.setBidPrices(bidPricesAndVolumes[0]);
        result.setBidVolumes(bidPricesAndVolumes[1]);

        final String[][] askPricesAndVolumes = createArray(sr, false);
        result.setAskPrices(askPricesAndVolumes[0]);
        result.setAskVolumes(askPricesAndVolumes[1]);

        final DateTime date = getDate(sr);
        if (date != null) {
            result.setDate(ISODateTimeFormat.dateTimeNoMillis().print(date));
        }
        return result;
    }

    private static DateTime getDate(SnapRecord sr) {
        final int yyyymmdd = getInt(sr, VwdFieldDescription.ADF_DATEOFARR);
        final int seconds = getInt(sr, VwdFieldDescription.ADF_TIMEOFARR);
        if (yyyymmdd >= 0 && seconds >= 0) {
            return DateUtil.toDateTime(yyyymmdd, seconds);
        }
        return null;
    }

    private static int getInt(SnapRecord sr, final VwdFieldDescription.Field field) {
        return SnapRecordUtils.getInt(sr.getField(field.id()));
    }

    private static String[][] createArray(SnapRecord record, boolean bid) {
        String[] prices = new String[BEST_COUNT];
        String[] volumes = new String[BEST_COUNT];

        int[] priceFids = bid ? BEST_BID_IDS : BEST_ASK_IDS;
        int[] volumeFids = bid ? BEST_BID_SIZE_IDS : BEST_ASK_SIZE_IDS;

        int idx = 0;
        while (idx < BEST_COUNT) {
            final SnapField pField = record.getField(priceFids[idx]);
            final SnapField vField = record.getField(volumeFids[idx]);
            if (!pField.isDefined() || !vField.isDefined()) {
                break;
            }

            final BigDecimal bd = pField.getPrice();
            final int volume = SnapRecordUtils.getInt(vField);

            // same condition as in de.marketmaker.istar.merger.provider.OrderbookProvider#buildOrderbookList
            if (bd.signum() == 0 || volume == 0) {
                break;
            }

            prices[idx] = format(bd);
            volumes[idx] = Integer.toString(volume);

            idx++;
        }

        if (idx == 0) {
            return EMPTY;
        }
        if (idx == BEST_COUNT) {
            return new String[][]{prices, volumes};
        }
        return new String[][]{Arrays.copyOf(prices, idx), Arrays.copyOf(volumes, idx)};
    }

    private static String format(BigDecimal bd) {
        synchronized (priceRenderer) {
            return priceRenderer.format(bd);
        }
    }

    public static PushOrderbook createDiff(PushOrderbook current, PushOrderbook previous) {
        if (previous == null) {
            return current;
        }
        final PushOrderbook result = new PushOrderbook();
        result.setVwdCode(current.getVwdCode());
        if (!Objects.equals(previous.getDate(), current.getDate())) {
            result.setDate(current.getDate());
        }
        result.setBidPrices(createDiff(current.getBidPrices(), previous.getBidPrices()));
        result.setBidVolumes(createDiff(current.getBidVolumes(), previous.getBidVolumes()));
        result.setAskPrices(createDiff(current.getAskPrices(), previous.getAskPrices()));
        result.setAskVolumes(createDiff(current.getAskVolumes(), previous.getAskVolumes()));
        return result;
    }

    private static String[] createDiff(String[] current, String[] previous) {
        final String[] result = new String[current.length];
        for (int i = 0; i < result.length; i++) {
            if (i >= previous.length || !Objects.equals(current[i], previous[i])) {
                result[i] = current[i];
            }
        }
        return result;
    }

    /*
    public static  String toString(PushOrderbook o) {
        StringBuilder sb = new StringBuilder(o.getVwdCode()).append(", ")
                .append(Integer.toHexString(System.identityHashCode(o))).append(", ")
                .append(o.getDate()).append(", ").append("[asks[");
        append(sb, o.getAskPrices(), o.getAskVolumes());
        sb.append("], bids=[");
        append(sb, o.getBidPrices(), o.getBidVolumes());
        sb.append("]");
        return sb.toString();
    }

    private static void append(StringBuilder sb, String[] prices, String[] volumes) {
        if (prices == null) {
            sb.append("--");
            return;
        }
        for (int i = 0; i < prices.length; i++) {
            sb.append(" ");
            sb.append(prices[i] == null ? "X" : prices[i]).append("/");
            sb.append(volumes[i] == null ? "X" : volumes[i]);
        }
    }
*/

}