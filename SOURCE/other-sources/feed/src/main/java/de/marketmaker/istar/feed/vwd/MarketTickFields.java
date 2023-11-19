/*
 * MarketTickFields.java
 *
 * Created on 25.02.14 17:02
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import de.marketmaker.istar.common.util.ByteString;

/**
 * @author oflege
 */
public abstract class MarketTickFields {

    private static final Set<String> CEF_MARKETS = new HashSet<>(Arrays.asList(
            "ETR", "FFM", "BLN", "MCH", "BUL", "DDF", "DTB", "DTBAGR", "FFMST", "HBG", "HNV", "TRADE", "XETF"
    ));

    private static final Set<String> EURONEXT_MARKETS = new HashSet<>(Arrays.asList(
            "BL", "FR", "NL", "PT", "XCOMD", "XEQD", "XIRD", "XLIF"
    ));

    // any fields that are not part of trade, bid, ask, suspend but should be recorded
    static final BitSet DEFAULT_TICK_FIELDS = toBitSet(
            /* The following commented fields are persisted implicitely when available

            VwdFieldDescription.ADF_Brief,
            VwdFieldDescription.ADF_Brief_Umsatz,
            VwdFieldDescription.ADF_Brief_Kurszusatz,
            VwdFieldDescription.ADF_Geld_Umsatz,
            VwdFieldDescription.ADF_Geld_Kurszusatz,
            VwdFieldDescription.ADF_Bezahlt,
            VwdFieldDescription.ADF_Bezahlt_Umsatz,
            // VwdFieldDescription.ADF_Bezahlt_Kurszusatz, // Handled in CAT but seems to be not persisted
             */

            VwdFieldDescription.ADF_Anfang,
            VwdFieldDescription.ADF_Anzahl_Handel,
            VwdFieldDescription.ADF_Auktion,
            VwdFieldDescription.ADF_Auktion_Umsatz,
            VwdFieldDescription.ADF_Ausgabe,
            VwdFieldDescription.ADF_Indicative_Price,
            VwdFieldDescription.ADF_Indicative_Qty,
            VwdFieldDescription.ADF_Kassa,
            VwdFieldDescription.ADF_Kassa_Kurszusatz,
            VwdFieldDescription.ADF_Mittelkurs,
            VwdFieldDescription.ADF_NAV,
            VwdFieldDescription.ADF_Notierungsart,
            VwdFieldDescription.ADF_Open_Interest,
            VwdFieldDescription.ADF_Rendite,
            VwdFieldDescription.ADF_Rendite_Brief,
            VwdFieldDescription.ADF_Rendite_Geld,
            VwdFieldDescription.ADF_Rendite_ISMA,
            VwdFieldDescription.ADF_Ruecknahme,
            VwdFieldDescription.ADF_Schluss,
            VwdFieldDescription.ADF_Settlement,
            VwdFieldDescription.ADF_Tageshoch,
            VwdFieldDescription.ADF_Tagestief,
            VwdFieldDescription.ADF_Umsatz_gesamt,
            VwdFieldDescription.ADF_Volatility
    );

    private static final BitSet ADDITIONAL_TICK_FIELDS_EURONEXT = toBitSet(
            VwdFieldDescription.ADF_Trade_Type,
            VwdFieldDescription.ADF_Bezahlt_Forerunner,
            VwdFieldDescription.ADF_EDSP,
            VwdFieldDescription.ADF_Trade_Condition_3
    );

    private static final BitSet ADDITIONAL_TICK_FIELDS_LME = toBitSet(
            VwdFieldDescription.ADF_Benchmark,
            VwdFieldDescription.ADF_Interpo_Closing,
            VwdFieldDescription.ADF_Official_Ask,
            VwdFieldDescription.ADF_Official_Bid,
            VwdFieldDescription.ADF_Prov_Evaluation,
            VwdFieldDescription.ADF_Unofficial_Ask,
            VwdFieldDescription.ADF_Unofficial_Bid,
            VwdFieldDescription.ADF_VWAP
    );

    private static final BitSet ADDITIONAL_TICK_FIELDS_CEF = toBitSet(
            VwdFieldDescription.ADF_Trade_Type,
            VwdFieldDescription.ADF_MMT_CROSSING_TRADE_IND,
            VwdFieldDescription.ADF_MMT_MARKET_MECHANISM,
            VwdFieldDescription.ADF_MMT_MODIFICATION_IND,
            VwdFieldDescription.ADF_MMT_NEGOTIATED_TA_IND,
            VwdFieldDescription.ADF_MMT_PUBLICATION_MODE,
            VwdFieldDescription.ADF_MMT_TRADING_MODE,
            VwdFieldDescription.ADF_MMT_TRANSACTION_CATEGORY
    );

    private static final BitSet LME_TICK_FIELDS;

    private static final BitSet LME_TICK_OIDS;

    private static final BitSet CEF_TICK_FIELDS;

    private static final BitSet CEF_TICK_OIDS;

    private static final BitSet EURONEXT_TICK_FIELDS;

    private static final BitSet EURONEXT_TICK_OIDS;

    static {
        LME_TICK_FIELDS = or(DEFAULT_TICK_FIELDS, ADDITIONAL_TICK_FIELDS_LME);
        LME_TICK_OIDS = toOidSet(ADDITIONAL_TICK_FIELDS_LME);

        CEF_TICK_FIELDS = or(DEFAULT_TICK_FIELDS, ADDITIONAL_TICK_FIELDS_CEF);
        CEF_TICK_OIDS = toOidSet(ADDITIONAL_TICK_FIELDS_CEF);

        EURONEXT_TICK_FIELDS = or(DEFAULT_TICK_FIELDS, ADDITIONAL_TICK_FIELDS_EURONEXT);
        EURONEXT_TICK_OIDS = toOidSet(EURONEXT_TICK_FIELDS);
    }

    private static BitSet or(BitSet bs1, BitSet bs2) {
        final BitSet result = new BitSet(Math.max(bs1.size(), bs2.size()));
        result.or(bs1);
        result.or(bs2);
        return result;
    }

    private static BitSet toOidSet(BitSet fidSet) {
        BitSet result = new BitSet();
        for (int i = fidSet.nextSetBit(0); i >= 0; i = fidSet.nextSetBit(i + 1)) {
            result.set(VwdFieldOrder.getOrder(i));
        }
        return result;
    }

    private static BitSet toBitSet(VwdFieldDescription.Field... fields) {
        Arrays.sort(fields);
        final BitSet result = new BitSet(fields[fields.length - 1].id());
        for (VwdFieldDescription.Field field : fields) {
            result.set(field.id());
        }
        return result;
    }

    static BitSet getDefaultTickFields() {
        return DEFAULT_TICK_FIELDS;
    }

    static BitSet getTickFieldIds(String marketName) {
        if ("LME".equals(marketName)) {
            return LME_TICK_FIELDS;
        }
        if (CEF_MARKETS.contains(marketName)) {
            return CEF_TICK_FIELDS;
        }
        if (EURONEXT_MARKETS.contains(marketName)) {
            return EURONEXT_TICK_FIELDS;
        }
        return DEFAULT_TICK_FIELDS;
    }

    public static BitSet getTickOrderIds(ByteString marketName) {
        final String name = marketName.toString();
        if ("LME".equals(name)) {
            return LME_TICK_OIDS;
        }
        if (CEF_MARKETS.contains(name)) {
            return CEF_TICK_OIDS;
        }
        if (EURONEXT_MARKETS.contains(name)) {
            return EURONEXT_TICK_OIDS;
        }
        return null;
    }

    public static boolean isTickField(int order) {
        return order < VwdFieldOrder.FIRST_NON_TICK || isAdditionalTickField(order);
    }

    private static boolean isAdditionalTickField(int order) {
        return LME_TICK_OIDS.get(order) || CEF_TICK_OIDS.get(order) || EURONEXT_TICK_OIDS.get(order);
    }

    private MarketTickFields() {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        System.out.println(toOidSet(DEFAULT_TICK_FIELDS));
        System.out.println(EURONEXT_TICK_OIDS);
    }
}
