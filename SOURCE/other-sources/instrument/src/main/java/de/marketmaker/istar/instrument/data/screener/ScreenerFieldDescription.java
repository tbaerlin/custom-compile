/*
 * ScreenerFieldDescription.java
 *
 * Created on 26.04.2005 09:19:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.data.screener;

/**
 * Generated class. Use gen_VwdFieldDescription.pl to generate. DO NOT EDIT.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ScreenerFieldDescription {

    final static String[] TYPE_NAMES = new String[4];

    public final static int[] TYPES = new int[53];
    final static String[] NAMES = new String[TYPES.length];
    final static int[] LENGTHS = new int[TYPES.length];
    final static double[] PRICE_FACTORS = new double[TYPES.length];
    public final static int[] FORMATTING_HINTS = new int[TYPES.length];

    public final static int TYPE_DATE = 0;
    public final static int TYPE_PRICE = 1;
    public final static int TYPE_UCHAR = 2;
    public final static int TYPE_UNUM4 = 3;

    public final static int FH_DATE = TYPE_DATE;
    public final static int FH_FLOAT = TYPE_PRICE;
    public final static int FH_TEXT = TYPE_UCHAR;
    public final static int FH_NUMBER = TYPE_UNUM4;
    public final static int FH_PRICE = 11;

    public final static int MMF_ANALYSISDATE = 3;
    public final static int MMF_BETA = 31;
    public final static int MMF_BMF = 27;
    public final static int MMF_BMFRISK = 41;
    public final static int MMF_BNF = 28;
    public final static int MMF_BNFRISK = 42;
    public final static int MMF_CCY = 6;
    public final static int MMF_CORR = 32;
    public final static int MMF_COUNTRY = 7;
    public final static int MMF_DIVIDEND = 2;
    public final static int MMF_EREV = 4;
    public final static int MMF_ERT = 35;
    public final static int MMF_ERTDATE = 37;
    public final static int MMF_ERTPRICE = 38;
    public final static int MMF_FIRSTDATE = 50;
    public final static int MMF_GLDATE = 51;
    public final static int MMF_GLEVAL = 52;
    public final static int MMF_GPE = 8;
    public final static int MMF_GROUP = 9;
    public final static int MMF_IDX = 40;
    public final static int MMF_IRST = 19;
    public final static int MMF_ISIN = 1;
    public final static int MMF_LTG = 11;
    public final static int MMF_LTGYEAR = 12;
    public final static int MMF_LTPE = 13;
    public final static int MMF_MC = 14;
    public final static int MMF_NAME = 10;
    public final static int MMF_NBRANL = 26;
    public final static int MMF_PAYOUT = 43;
    public final static int MMF_PDF = 47;
    public final static int MMF_PREMIUM = 46;
    public final static int MMF_PRICE = 15;
    public final static int MMF_RISKDATE = 39;
    public final static int MMF_RISKZONE = 33;
    public final static int MMF_RP = 17;
    public final static int MMF_SECTOR = 18;
    public final static int MMF_STARTDATE = 23;
    public final static int MMF_STARTPRICE = 24;
    public final static int MMF_STOPPRICE = 20;
    public final static int MMF_TICKER = 5;
    public final static int MMF_TIMING = 22;
    public final static int MMF_TR = 21;
    public final static int MMF_TRADETYPE = 25;
    public final static int MMF_TR_MAX = 45;
    public final static int MMF_TR_MIN = 44;
    public final static int MMF_TT = 34;
    public final static int MMF_TTDATE = 36;
    public final static int MMF_VARPCT = 49;
    public final static int MMF_VARVAL = 48;
    public final static int MMF_VOL12M = 30;
    public final static int MMF_VOL1M = 29;
    public final static int MMF_VR = 16;

    static {
        TYPE_NAMES[TYPE_DATE] = "DATE";
        TYPE_NAMES[TYPE_PRICE] = "PRICE";
        TYPE_NAMES[TYPE_UCHAR] = "UCHAR";
        TYPE_NAMES[TYPE_UNUM4] = "UNUM4";

        TYPES[MMF_ANALYSISDATE] = TYPE_DATE;
        TYPES[MMF_BETA] = TYPE_PRICE;
        TYPES[MMF_BMF] = TYPE_PRICE;
        TYPES[MMF_BMFRISK] = TYPE_UNUM4;
        TYPES[MMF_BNF] = TYPE_PRICE;
        TYPES[MMF_BNFRISK] = TYPE_UNUM4;
        TYPES[MMF_CCY] = TYPE_UCHAR;
        TYPES[MMF_CORR] = TYPE_PRICE;
        TYPES[MMF_COUNTRY] = TYPE_UCHAR;
        TYPES[MMF_DIVIDEND] = TYPE_PRICE;
        TYPES[MMF_EREV] = TYPE_PRICE;
        TYPES[MMF_ERT] = TYPE_PRICE;
        TYPES[MMF_ERTDATE] = TYPE_DATE;
        TYPES[MMF_ERTPRICE] = TYPE_PRICE;
        TYPES[MMF_FIRSTDATE] = TYPE_DATE;
        TYPES[MMF_GLDATE] = TYPE_DATE;
        TYPES[MMF_GLEVAL] = TYPE_UNUM4;
        TYPES[MMF_GPE] = TYPE_PRICE;
        TYPES[MMF_GROUP] = TYPE_UCHAR;
        TYPES[MMF_IDX] = TYPE_UCHAR;
        TYPES[MMF_IRST] = TYPE_UNUM4;
        TYPES[MMF_ISIN] = TYPE_UCHAR;
        TYPES[MMF_LTG] = TYPE_PRICE;
        TYPES[MMF_LTGYEAR] = TYPE_UNUM4;
        TYPES[MMF_LTPE] = TYPE_PRICE;
        TYPES[MMF_MC] = TYPE_PRICE;
        TYPES[MMF_NAME] = TYPE_UCHAR;
        TYPES[MMF_NBRANL] = TYPE_UNUM4;
        TYPES[MMF_PAYOUT] = TYPE_PRICE;
        TYPES[MMF_PDF] = TYPE_UCHAR;
        TYPES[MMF_PREMIUM] = TYPE_PRICE;
        TYPES[MMF_PRICE] = TYPE_PRICE;
        TYPES[MMF_RISKDATE] = TYPE_DATE;
        TYPES[MMF_RISKZONE] = TYPE_UNUM4;
        TYPES[MMF_RP] = TYPE_PRICE;
        TYPES[MMF_SECTOR] = TYPE_UCHAR;
        TYPES[MMF_STARTDATE] = TYPE_DATE;
        TYPES[MMF_STARTPRICE] = TYPE_PRICE;
        TYPES[MMF_STOPPRICE] = TYPE_PRICE;
        TYPES[MMF_TICKER] = TYPE_UCHAR;
        TYPES[MMF_TIMING] = TYPE_UNUM4;
        TYPES[MMF_TR] = TYPE_PRICE;
        TYPES[MMF_TRADETYPE] = TYPE_UNUM4;
        TYPES[MMF_TR_MAX] = TYPE_PRICE;
        TYPES[MMF_TR_MIN] = TYPE_PRICE;
        TYPES[MMF_TT] = TYPE_PRICE;
        TYPES[MMF_TTDATE] = TYPE_DATE;
        TYPES[MMF_VARPCT] = TYPE_PRICE;
        TYPES[MMF_VARVAL] = TYPE_PRICE;
        TYPES[MMF_VOL12M] = TYPE_PRICE;
        TYPES[MMF_VOL1M] = TYPE_PRICE;
        TYPES[MMF_VR] = TYPE_UNUM4;

        NAMES[MMF_ANALYSISDATE] = "MMF_ANALYSISDATE";
        NAMES[MMF_BETA] = "MMF_BETA";
        NAMES[MMF_BMF] = "MMF_BMF";
        NAMES[MMF_BMFRISK] = "MMF_BMFRISK";
        NAMES[MMF_BNF] = "MMF_BNF";
        NAMES[MMF_BNFRISK] = "MMF_BNFRISK";
        NAMES[MMF_CCY] = "MMF_CCY";
        NAMES[MMF_CORR] = "MMF_CORR";
        NAMES[MMF_COUNTRY] = "MMF_COUNTRY";
        NAMES[MMF_DIVIDEND] = "MMF_DIVIDEND";
        NAMES[MMF_EREV] = "MMF_EREV";
        NAMES[MMF_ERT] = "MMF_ERT";
        NAMES[MMF_ERTDATE] = "MMF_ERTDATE";
        NAMES[MMF_ERTPRICE] = "MMF_ERTPRICE";
        NAMES[MMF_FIRSTDATE] = "MMF_FIRSTDATE";
        NAMES[MMF_GLDATE] = "MMF_GLDATE";
        NAMES[MMF_GLEVAL] = "MMF_GLEVAL";
        NAMES[MMF_GPE] = "MMF_GPE";
        NAMES[MMF_GROUP] = "MMF_GROUP";
        NAMES[MMF_IDX] = "MMF_IDX";
        NAMES[MMF_IRST] = "MMF_IRST";
        NAMES[MMF_ISIN] = "MMF_ISIN";
        NAMES[MMF_LTG] = "MMF_LTG";
        NAMES[MMF_LTGYEAR] = "MMF_LTGYEAR";
        NAMES[MMF_LTPE] = "MMF_LTPE";
        NAMES[MMF_MC] = "MMF_MC";
        NAMES[MMF_NAME] = "MMF_NAME";
        NAMES[MMF_NBRANL] = "MMF_NBRANL";
        NAMES[MMF_PAYOUT] = "MMF_PAYOUT";
        NAMES[MMF_PDF] = "MMF_PDF";
        NAMES[MMF_PREMIUM] = "MMF_PREMIUM";
        NAMES[MMF_PRICE] = "MMF_PRICE";
        NAMES[MMF_RISKDATE] = "MMF_RISKDATE";
        NAMES[MMF_RISKZONE] = "MMF_RISKZONE";
        NAMES[MMF_RP] = "MMF_RP";
        NAMES[MMF_SECTOR] = "MMF_SECTOR";
        NAMES[MMF_STARTDATE] = "MMF_STARTDATE";
        NAMES[MMF_STARTPRICE] = "MMF_STARTPRICE";
        NAMES[MMF_STOPPRICE] = "MMF_STOPPRICE";
        NAMES[MMF_TICKER] = "MMF_TICKER";
        NAMES[MMF_TIMING] = "MMF_TIMING";
        NAMES[MMF_TR] = "MMF_TR";
        NAMES[MMF_TRADETYPE] = "MMF_TRADETYPE";
        NAMES[MMF_TR_MAX] = "MMF_TR_MAX";
        NAMES[MMF_TR_MIN] = "MMF_TR_MIN";
        NAMES[MMF_TT] = "MMF_TT";
        NAMES[MMF_TTDATE] = "MMF_TTDATE";
        NAMES[MMF_VARPCT] = "MMF_VARPCT";
        NAMES[MMF_VARVAL] = "MMF_VARVAL";
        NAMES[MMF_VOL12M] = "MMF_VOL12M";
        NAMES[MMF_VOL1M] = "MMF_VOL1M";
        NAMES[MMF_VR] = "MMF_VR";

        LENGTHS[MMF_CCY] = 5;
        LENGTHS[MMF_COUNTRY] = 5;
        LENGTHS[MMF_ERTPRICE] = 10;
        LENGTHS[MMF_GROUP] = 50;
        LENGTHS[MMF_IDX] = 50;
        LENGTHS[MMF_ISIN] = 15;
        LENGTHS[MMF_NAME] = 50;
        LENGTHS[MMF_PDF] = 7;
        LENGTHS[MMF_SECTOR] = 50;
        LENGTHS[MMF_TICKER] = 10;

        for (int i = 0; i < PRICE_FACTORS.length; i++) {
            PRICE_FACTORS[i] = 1d;
        }
        PRICE_FACTORS[MMF_BMF] = 0.01d;
        PRICE_FACTORS[MMF_BNF] = 0.01d;
        PRICE_FACTORS[MMF_BETA] = 0.01d;
        PRICE_FACTORS[MMF_CORR] = 100d;

        System.arraycopy(TYPES, 0, FORMATTING_HINTS, 0, FORMATTING_HINTS.length);
        FORMATTING_HINTS[MMF_DIVIDEND] = FH_PRICE;
        FORMATTING_HINTS[MMF_ERTPRICE] = FH_PRICE;
        FORMATTING_HINTS[MMF_PRICE] = FH_PRICE;
        FORMATTING_HINTS[MMF_STARTPRICE] = FH_PRICE;
        FORMATTING_HINTS[MMF_STOPPRICE] = FH_PRICE;
        FORMATTING_HINTS[MMF_TR] = FH_PRICE;
        FORMATTING_HINTS[MMF_TR_MAX] = FH_PRICE;
        FORMATTING_HINTS[MMF_TR_MIN] = FH_PRICE;
        FORMATTING_HINTS[MMF_VARVAL] = FH_PRICE;
    }

    /**
     * Returns the number of the field with the given name
     * name identifies field
     * field number or -1 if no such field exists.
     */
    public static int getFieldByName(String name) {
        for (int i = 0; i < NAMES.length; i++) {
            if (name.equals(NAMES[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the mapped field for any non_null_trade field
     * name identifies fieldId
     * field number or -1 if no such field exists.
     */
    public static int getNonNullTradeMapping(int fieldId) {
        switch (fieldId) {
            default:
                return -1;
        }
    }

    /**
     * Returns the mapped field for any save_if_different field
     * name identifies fieldId
     * field number or -1 if no such field exists.
     */
    public static int getSaveIfDifferentMapping(int fieldId) {
        switch (fieldId) {
            default:
                return -1;
        }
    }

}
