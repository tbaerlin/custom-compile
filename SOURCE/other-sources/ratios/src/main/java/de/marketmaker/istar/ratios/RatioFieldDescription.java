/*
 * RatioFieldDescription.java
 *
 * Created on 09.01.2007 11:50:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.jcip.annotations.Immutable;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.UnmodifiableBitSet;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.Selector;

import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.*;
import static de.marketmaker.istar.ratios.RatioFieldDescription.Type.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@SuppressWarnings("unused")
public class RatioFieldDescription {
    public final static Map<InstrumentTypeEnum, Map<RatioDataRecord.Field, Field>> FIELDNAMES = new EnumMap<>(InstrumentTypeEnum.class);

    public final static Map<String, Map<RatioDataRecord.Field, RatioFieldDescription.Field>> FIELDNAMES_BY_PERMISSION = new HashMap<>();

    public enum Type {
        NUMBER,
        DECIMAL,
        STRING,
        DATE,
        TIME,
        TIMESTAMP,
        BOOLEAN,
        ENUMSET
    }

    private static final Locale[] LOCALES_IT = new Locale[]{Locale.ITALIAN};

    private static final Locale[] LOCALES_VWDBENL = new Locale[]{new Locale("nl"), Locale.FRENCH};

    private static final Locale[] LOCALES_DE_EN = new Locale[]{Locale.GERMAN, Locale.ENGLISH};

    private static final Locale[] LOCALES_DE_EN_FR_NL = new Locale[]{Locale.GERMAN, Locale.ENGLISH, Locale.FRENCH, new Locale("nl", "NL")};

    private static final Locale[] LOCALES_DE_EN_IT = new Locale[]{Locale.GERMAN, Locale.ENGLISH, Locale.ITALIAN};

    private static final Locale[] LOCALES_DE_EN_FR_IT = new Locale[]{Locale.GERMAN, Locale.ENGLISH, Locale.FRENCH, Locale.ITALIAN};

    private static final int MAX_ID = 1000;

    private static BitSet IN = new BitSet(MAX_ID);

    private static BitSet QS = new BitSet(MAX_ID);

    private static BitSet QR = new BitSet(MAX_ID);

    private static BitSet ENUMS = new BitSet(MAX_ID);

    private static BitSet PERCENT = new BitSet(MAX_ID);

    private static BitSet STATIC = new BitSet(MAX_ID);

    private static BitSet DEPRECATED = new BitSet(MAX_ID);

    private static final BitSet FIELD_IDS = new BitSet(MAX_ID);

    private static final Map<String, Field> FIELDS_BY_NAME = new HashMap<>();

    private final static Field[] FIELDS = new Field[MAX_ID];

    private static int maxFieldId = 0;

    private RatioFieldDescription() {
    }

    @Immutable
    public final static class Field {
        private final int id;

        private final String name;

        private final String displayName;

        private final Type type;

        private final Set<InstrumentTypeEnum> applicableTypes;

        private final boolean deprecated;

        private Locale[] locales;

        private Boolean nullAsMin;

        private Field(int id, String name, Type type, String displayName, Locale[] locales,
                EnumSet<InstrumentTypeEnum> applicableTypes) {
            this.id = Math.abs(id);
            this.deprecated = id < 0;
            this.name = name;
            this.type = type;
            this.displayName = displayName;
            this.locales = locales;
            this.applicableTypes = applicableTypes;
        }

        public int id() {
            return id;
        }

        public String name() {
            return name;
        }

        public boolean isStatic() {
            return STATIC.get(this.id);
        }

        public boolean isDeprecated() {
            return this.deprecated;
        }

        public String displayName() {
            return displayName;
        }

        public Type type() {
            return type;
        }

        public String toString() {
            return this.name + "(" + this.id + ")";
        }

        public boolean isApplicableFor(InstrumentTypeEnum type) {
            return this.applicableTypes.contains(type);
        }

        public String getRatingSystemName() {
            if (this.name.startsWith("rating")) {
                return this.name.substring("rating".length()).toLowerCase();
            }
            return null;
        }

        public boolean isInstrumentField() {
            return IN.get(this.id);
        }

        public boolean isQuoteStaticField() {
            return QS.get(this.id);
        }

        public boolean isQuoteRatioField() {
            return QR.get(this.id);
        }

        public boolean isPercent() {
            return PERCENT.get(this.id);
        }

        public boolean isEnum() {
            return ENUMS.get(this.id);
        }

        public boolean isEnumSet() {
            return ENUMSET == this.type;
        }

        public boolean isNumeric() {
            return this.type != STRING;
        }

        public boolean isLocalized() {
            return this.locales != null;
        }

        public Locale[] getLocales() {
            return locales;
        }

        public Boolean isNullAsMin() {
            return nullAsMin;
        }

        public void setNullAsMin(Boolean nullAsMin) {
            this.nullAsMin = nullAsMin;
        }
    }

    public static class FieldEditor extends PropertyEditorSupport {
        public String getAsText() {
            final RatioFieldDescription.Field field = (RatioFieldDescription.Field) getValue();
            if (field == null) {
                return null;
            }
            return field.name();
        }

        public void setAsText(String text) throws IllegalArgumentException {
            if (!StringUtils.hasText(text)) {
                return;
            }

            super.setValue(RatioFieldDescription.getFieldByName(text));
        }
    }

    private static Field create(int id, String name, Type type, String displayName,
            BitSet bs, InstrumentTypeEnum... types) {
        return create(id, name, type, displayName, null, bs, types);
    }

    private static Field createStatic(int id, String name, Type type, String displayName,
            BitSet bs, InstrumentTypeEnum... types) {
        final Field f = create(id, name, type, displayName, bs, types);
        STATIC.set(f.id);
        return f;
    }

    private static Field createStatic(int id, String name, Type type, String displayName,
            Locale[] locales, BitSet bs, InstrumentTypeEnum... types) {
        final Field f = create(id, name, type, displayName, locales, bs, types);
        STATIC.set(f.id);
        return f;
    }

    /**
     * Create new Field
     * @param fid unique id, use negative value for deprecated fields, field's id will be the
     * absolute value of fid
     * @param name descriptive name, will be lowercased
     * @param type the field's type
     * @param displayName a descriptive display name
     * @param locales array of locales the data is available in
     * @param bs whether the field belongs to an instrument, static quote data or quote ratio data.
     * @param types applicable types
     * @return new Field
     */
    private static Field create(int fid, String name, Type type, String displayName,
            Locale[] locales, BitSet bs, InstrumentTypeEnum... types) {
        final int id = Math.abs(fid);
        if (bs == null && id != 1) {
            throw new IllegalArgumentException("must have a Ref-type for id > 1");
        }
        if (FIELDS[id] != null) {
            throw new IllegalArgumentException("duplicate field " + id);
        }

        final Field f = new Field(fid, name, type, displayName, locales,
                EnumSet.of(types[0], types));
        FIELD_IDS.set(id);
        FIELDS[id] = f;
        FIELDS_BY_NAME.put(f.name().toLowerCase(), f);

        if (f.deprecated) {
            DEPRECATED.set(f.id);
        }

        if (bs != null) {
            bs.set(id);
        }
        maxFieldId = Math.max(maxFieldId, id);
        return f;
    }

    private static Field createEnum(int id, String name, Type type, String displayName,
            BitSet bs, InstrumentTypeEnum... types) {
        return createEnum(id, name, type, displayName, null, bs, types);
    }

    private static Field createEnumStatic(int id, String name, Type type, String displayName,
            BitSet bs, InstrumentTypeEnum... types) {
        final Field f = createEnum(id, name, type, displayName, null, bs, types);
        STATIC.set(f.id);
        return f;
    }

    private static Field createEnum(int id, String name, Type type, String displayName,
            Locale[] locales, BitSet bs, InstrumentTypeEnum... types) {
        final Field f = create(id, name, type, displayName, locales, bs, types);
        ENUMS.set(f.id);
        return f;
    }

    private static Field createEnumStatic(int id, String name, Type type, String displayName,
            Locale[] locales, BitSet bs, InstrumentTypeEnum... types) {
        final Field f = createEnum(id, name, type, displayName, locales, bs, types);
        STATIC.set(f.id);
        return f;
    }

    private static Field createPercent(int id, String name, Type type, String displayName,
            BitSet bs, InstrumentTypeEnum... types) {
        final Field f = create(id, name, type, displayName, bs, types);
        PERCENT.set(f.id);
        return f;
    }

    private static Field createPercentStatic(int id, String name, Type type, String displayName,
            BitSet bs, InstrumentTypeEnum... types) {
        final Field f = createPercent(id, name, type, displayName, bs, types);
        STATIC.set(f.id);
        return f;
    }

    public static final Field mostRecentUpdateTimestamp = create(1, "mostRecentUpdateTimestamp", TIMESTAMP, "Aktuellster Zeitstempel", QS, BND, GNS, CER, FND, STK, WNT, FUT, OPT, IND, CUR, MER, ZNS);

    public static final Field subscriptionRatio = createStatic(3, "subscriptionRatio", DECIMAL, "Bezugsverhältnis", IN, CER, WNT);

    public static final Field volatility1m = createPercent(4, "volatility1m", DECIMAL, "Vola (1 Monat)", QR, BND, GNS, CER, FND, STK, WNT, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field performance1m = createPercent(5, "performance1m", DECIMAL, "Performance (1 Monat)", QR, BND, GNS, CER, STK, WNT, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field spread = create(6, "spread", DECIMAL, "Spread", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field issuerProductname = createStatic(9, "issuerProductname", STRING, "Produktname (Emittent)", IN, CER);

    public static final Field referenceTimestamp = create(10, "referenceTimestamp", TIMESTAMP, "Datum/Zeit letzte interne Kennzahlenberechnung", QR, BND, GNS, CER, FND, STK, WNT, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field currencyStrike = createEnumStatic(11, "currencyStrike", STRING, "Strike-Währung", IN, CER, WNT, OPT);

    public static final Field cap = createStatic(13, "cap", DECIMAL, "Cap", IN, CER);

    public static final Field expires = createStatic(14, "expires", DATE, "Verfallstag", IN, BND, GNS, CER, FND, WNT, FUT, OPT);

    public static final Field spreadRelative = createPercent(15, "spreadRelative", DECIMAL, "Spread, relativ", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field bid = create(16, "bid", DECIMAL, "Bid", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field ask = create(17, "ask", DECIMAL, "Ask", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field lastPrice = create(18, "lastPrice", DECIMAL, "Letzter", QR, BND, GNS, CER, FND, STK, WNT, ZNS);

    public static final Field previousClose = create(19, "previousClose", DECIMAL, "Schluss Vortag", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field discountPrice = create(22, "discountPrice", DECIMAL, "Discount", QR, CER);

    public static final Field discountPriceRelative = createPercent(23, "discountPriceRelative", DECIMAL, "Discount, relativ", QR, CER);

    public static final Field discountPriceRelativePerYear = createPercent(24, "discountPriceRelativePerYear", DECIMAL, "Discount, relativ p.a.", QR, CER);

    public static final Field unchangedEarning = create(25, "unchangedEarning", DECIMAL, "Seitwärts-Ertrag", QR, CER);

    public static final Field unchangedEarningRelative = createPercent(26, "unchangedEarningRelative", DECIMAL, "Seitwärts-Ertrag, relativ", QR, CER);

    public static final Field unchangedEarningRelativePerYear = createPercent(27, "unchangedEarningRelativePerYear", DECIMAL, "Seitwärts-Ertrag, relativ p.a.", QR, CER);

    public static final Field bidVolume = create(28, "bidVolume", NUMBER, "Volumen (Bid)", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field askVolume = create(29, "askVolume", NUMBER, "Volumen (Ask)", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field capToUnderlying = create(30, "capToUnderlying", DECIMAL, "Notwendige Perf.", QR, CER);

    public static final Field capToUnderlyingRelative = createPercent(31, "capToUnderlyingRelative", DECIMAL, "Notwendige Perf., relativ", QR, CER);

    public static final Field underlyingToCap = create(32, "underlyingToCap", DECIMAL, "Sicherheit", QR, CER);

    public static final Field underlyingToCapRelative = createPercent(33, "underlyingToCapRelative", DECIMAL, "Sicherheit, relativ", QR, CER);

    public static final Field bidAskDate = create(34, "bidAskDate", DATE, "Datum (Bid/Ask)", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field bidAskTime = create(35, "bidAskTime", TIME, "Zeit (Bid/Ask)", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field tradeVolume = create(36, "tradeVolume", NUMBER, "Volumen (Bezahlt)", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field lastDate = create(37, "lastDate", DATE, "Datum (Letzter)", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field lastTime = create(38, "lastTime", TIME, "Zeit (Letzter)", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field high = create(39, "high", DECIMAL, "Tageshoch", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field low = create(40, "low", DECIMAL, "Tagestief", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field highYear = create(41, "highYear", DECIMAL, "Jahreshoch", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field lowYear = create(42, "lowYear", DECIMAL, "Jahrestief", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field totalVolume = create(43, "totalVolume", NUMBER, "Volumen (Tag)", QR, BND, GNS, CER, FND, STK, WNT, FUT, OPT);

    public static final Field previousDate = create(44, "previousDate", DATE, "Datum (Vortag)", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field open = create(45, "open", DECIMAL, "Eröffnung", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field underlyingLastPrice = create(48, "underlyingLastPrice", DECIMAL, "Letzter (Underlying)", QR, CER, WNT);

    public static final Field underlyingPreviousClose = create(49, "underlyingPreviousClose", DECIMAL, "Schluss Vortag (Underlying)", QR, CER, WNT);

    public static final Field underlyingTradeVolume = create(52, "underlyingTradeVolume", NUMBER, "Volumen (Letzter, Underlying)", QR, CER, WNT);

    public static final Field underlyingLastDate = create(53, "underlyingLastDate", DATE, "Datum (Letzter, Underlying)", QR, CER, WNT);

    public static final Field underlyingLastTime = create(54, "underlyingLastTime", TIME, "Zeit (Letzter, Underlying)", QR, CER, WNT);

    public static final Field underlyingTotalVolume = create(57, "underlyingTotalVolume", NUMBER, "Volumen (Tag, Underlying)", QR, CER, WNT);

    public static final Field underlyingPreviousDate = create(58, "underlyingPreviousDate", DATE, "Datum (Vortag, Underlying)", QR, CER, WNT);

    public static final Field issuePrice = create(60, "issuePrice", DECIMAL, "Ausgabepreis", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field interimProfit = create(61, "interimProfit", DECIMAL, "Zwischengewinn", QR, FND);

    public static final Field interimProfitDate = create(62, "interimProfitDate", DATE, "Zwischengewinn (Datum)", QR, FND);

    public static final Field estateProfit = create(63, "estateProfit", DECIMAL, "Immobiliengewinn", QR, FND);

    public static final Field stockProfit = create(64, "stockProfit", DECIMAL, "Aktiengewinn", QR, FND);

    public static final Field stockProfitDate = create(65, "stockProfitDate", DATE, "Aktiengewinn (Datum)", QR, FND);

    public static final Field volatility3m = createPercent(67, "volatility3m", DECIMAL, "Vola. (3 Monate)", QR, BND, GNS, CER, FND, STK, WNT, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field performance3m = createPercent(68, "performance3m", DECIMAL, "Performance (3 Monate)", QR, BND, GNS, CER, STK, WNT, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field performance1y = createPercent(69, "performance1y", DECIMAL, "Performance (1 Jahr)", QR, BND, GNS, CER, STK, WNT, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field capLevel = createPercent(70, "capLevel", DECIMAL, "Cap/Underlyingpreis", QR, CER);

    public static final Field yieldRelativePerYear = create(71, "yieldRelativePerYear", DECIMAL, "rel. Rendite p.a.", QR, CER);

    public static final Field volatility10y = createPercent(72, "volatility10y", DECIMAL, "Vola. (10 Jahre)", QR, BND, GNS, CER, FND, STK, WNT, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field volatility1y = createPercent(73, "volatility1y", DECIMAL, "Vola. (1 Jahr)", QR, BND, GNS, CER, FND, STK, WNT, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field sharperatio1y = create(74, "sharperatio1y", DECIMAL, "Sharpe Ratio (1 Jahr)", QR, CER, FND, IND);

    public static final Field sharperatio3y = create(75, "sharperatio3y", DECIMAL, "Sharpe Ratio (3 Jahre)", QR, CER, FND, IND);

    public static final Field bviperformance1y = createPercent(76, "bviperformance1y", DECIMAL, "BVI-Perf. (1 Jahr)", QR, FND);

    public static final Field bviperformance3y = createPercent(77, "bviperformance3y", DECIMAL, "BVI-Perf. (3 Jahre)", QR, FND);

    public static final Field treynor1y = create(78, "treynor1y", DECIMAL, "Treynor (1 Jahr)", QR, FND);

    public static final Field treynor3y = create(79, "treynor3y", DECIMAL, "Treynor (3 Jahre)", QR, FND);

    public static final Field beta1y = create(80, "beta1y", DECIMAL, "Beta (1 Jahr)", QR, STK, BND, GNS, FND);

    public static final Field wmIssueVolume = createStatic(81, "wmIssueVolume", DECIMAL, "Emissionsvolumen (WM)", IN, STK, GNS);

    public static final Field correlation1y = create(82, "correlation1y", DECIMAL, "Korrelation (1 Jahr)", QR, STK, BND, GNS, FND);

    public static final Field marketCapitalization = create(83, "marketcapitalization", NUMBER, "Marktkapitalisierung", QR, STK, GNS);

    public static final Field dateoflast = create(84, "dateoflast", DATE, "Datum (Letzter)", QR, FND, WNT);

    public static final Field intrinsicvalue = create(85, "intrinsicvalue", DECIMAL, "Innerer Wert", QR, WNT);

    public static final Field intrinsicvaluepercent = createPercent(86, "intrinsicvaluepercent", DECIMAL, "Innerer Wert, relativ", QR, WNT);

    public static final Field extrinsicvalue = create(87, "extrinsicvalue", DECIMAL, "Zeitwert", QR, WNT);

    public static final Field extrinsicvaluepercent = createPercent(88, "extrinsicvaluepercent", DECIMAL, "Zeitwert, relativ", QR, WNT);

    public static final Field optionprice = create(89, "optionprice", DECIMAL, "Prämie", QR, WNT);

    public static final Field optionpriceperyear = create(90, "optionpriceperyear", DECIMAL, "Prämie, p.a.", QR, WNT);

    public static final Field breakeven = create(91, "breakeven", DECIMAL, "Breakeven", QR, WNT);

    public static final Field leverage = create(92, "leverage", DECIMAL, "Hebel", QR, CER, WNT);

    public static final Field fairvalue = create(94, "fairvalue", DECIMAL, "Fairer Preis", QR, WNT);

    public static final Field delta = create(95, "delta", DECIMAL, "Delta", QR, WNT, OPT);

    public static final Field omega = create(96, "omega", DECIMAL, "Omega", QR, WNT, OPT);

    public static final Field gamma = create(97, "gamma", DECIMAL, "Gamma", QR, WNT, OPT);

    public static final Field vega = create(98, "vega", DECIMAL, "Vega", QR, WNT, OPT);

    public static final Field theta = create(99, "theta", DECIMAL, "Theta", QR, WNT, OPT);

    public static final Field rho = create(100, "rho", DECIMAL, "Rho", QR, WNT, OPT);

    public static final Field impliedvolatility = createPercent(101, "impliedvolatility", DECIMAL, "Impl. Vola.", QR, WNT, OPT);

    public static final Field performance10y = createPercent(105, "performance10y", DECIMAL, "Performance (10 Jahre)", QR, BND, GNS, CER, STK, WNT, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field screenerInterest = createStatic(106, "screenerinterest", NUMBER, "Interesse (Screener)", IN, STK);

    public static final Field changePercentAlltimeHigh = createPercent(107, "changepercentalltimehigh", DECIMAL, "Abstand zum Alltime-High, relativ", QR, STK);

    public static final Field volatility1w = createPercent(108, "volatility1w", DECIMAL, "Vola. (1 Woche)", QR, BND, GNS, FND, STK, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field attraxRiskclass = createStatic(111, "attraxriskclass", NUMBER, "Attrax-Risikoklasse", IN, FND);

    public static final Field wmCoupon = createPercentStatic(112, "wmcoupon", DECIMAL, "Kupon/Zinssatz (WM)", IN, BND, GNS);

    public static final Field wmExpirationDate = createStatic(113, "wmexpirationdate", DATE, "Fälligkeitsdatum (WM)", IN, BND, GNS);

    public static final Field last = create(114, "last", DECIMAL, "Letzter", QR, WNT);

    public static final Field mmnetassetvalue = create(115, "mmnetassetvalue", DECIMAL, "Rücknahme", QR, FND);

    public static final Field mmissueprice = create(116, "mmissueprice", DECIMAL, "Ausgabe", QR, FND);

    public static final Field performance1d = createPercent(117, "performance1d", DECIMAL, "Performance (1 Tag)", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field performanceToBenchmark3m = createPercent(118, "performancetobenchmark3m", DECIMAL, "rel. Performance zu Benchmark (3 Monate)", QR, BND, GNS, STK, FND);

    public static final Field performanceToBenchmark1w = createPercent(119, "performancetobenchmark1w", DECIMAL, "rel. Performance zu Benchmark (1 Woche)", QR, BND, GNS, STK, FND);

    public static final Field performanceToBenchmark1m = createPercent(120, "performancetobenchmark1m", DECIMAL, "rel. Performance zu Benchmark (1 Monat)", QR, BND, GNS, STK, FND);

    public static final Field performanceToBenchmark1y = createPercent(121, "performancetobenchmark1y", DECIMAL, "rel. Performance zu Benchmark (1 Jahr)", QR, BND, GNS, STK, FND);

    public static final Field averageVolume1m = create(122, "averagevolume1m", DECIMAL, "Durchschnittsvolumen (1 Monat)", QR, BND, CER, GNS, FND, STK, IND, FUT);

    public static final Field averageVolume1y = create(123, "averagevolume1y", DECIMAL, "Durchschnittsvolumen (1 Jahr)", QR, BND, CER, GNS, FND, STK, IND, FUT);

    public static final Field beta1m = create(124, "beta1m", DECIMAL, "Beta-Faktor (1 Monat)", QR, STK, BND, GNS, FND);

    public static final Field earning = create(125, "earning", DECIMAL, "Rendite", QR, BND, GNS);

    public static final Field performanceToBenchmark6m = createPercent(126, "performancetobenchmark6m", DECIMAL, "rel. Performance zu Benchmark (6 Monate)", QR, BND, GNS, STK, FND);

    public static final Field performance6m = createPercent(127, "performance6m", DECIMAL, "Perf. (6 Monate)", QR, BND, GNS, CER, STK, WNT, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field performance3y = createPercent(128, "performance3y", DECIMAL, "Perf. (3 Jahre)", QR, BND, GNS, CER, STK, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field performance5y = createPercent(129, "performance5y", DECIMAL, "Perf. (5 Jahre)", QR, BND, GNS, CER, STK, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field volatility6m = createPercent(130, "volatility6m", DECIMAL, "Vola. (6 Monate)", QR, BND, GNS, CER, FND, STK, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field volatility5y = createPercent(131, "volatility5y", DECIMAL, "Vola. (5 Jahre)", QR, BND, GNS, CER, FND, STK, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field sharperatio5y = create(132, "sharperatio5y", DECIMAL, "Sharpe Ratio (5 Jahre)", QR, CER, FND, IND);

    public static final Field sharperatio1m = create(133, "sharperatio1m", DECIMAL, "Sharpe Ratio (1 Monat)", QR, CER, FND, IND);

    public static final Field sharperatio3m = create(134, "sharperatio3m", DECIMAL, "Sharpe Ratio (3 Monate)", QR, CER, FND, IND);

    public static final Field sharperatio6m = create(135, "sharperatio6m", DECIMAL, "Sharpe Ratio (6 Monate)", QR, CER, FND, IND);

    public static final Field correlation1m = create(136, "correlation1m", DECIMAL, "Korrelation (1 Monat)", QR, STK, BND, GNS, FND);

    public static final Field underlyinglow1y = create(137, "underlyinglow1y", DECIMAL, "Jahrestief (Underlying)", QR, CER);

    public static final Field underlyinghigh1y = create(138, "underlyinghigh1y", DECIMAL, "Jahreshoch (Underlying)", QR, CER);

    public static final Field wkn = create(139, "wkn", STRING, "WKN", IN, BND, GNS, CER, FND, STK, WNT, FUT, OPT, IND, CUR, MER, ZNS);

    public static final Field isin = create(140, "isin", STRING, "ISIN", IN, BND, GNS, CER, FND, STK, WNT, FUT, OPT, IND, CUR, MER, ZNS);

    public static final Field name = create(141, "name", STRING, "Name", IN, BND, GNS, CER, FND, STK, WNT, FUT, OPT, IND, CUR, MER, ZNS);

    public static final Field strikePrice = createStatic(148, "strikePrice", DECIMAL, "Strike", IN, CER, WNT, OPT);

    public static final Field osType = createEnumStatic(149, "osType", STRING, "Typ", IN, CER, WNT, OPT);

    public static final Field isAmerican = createStatic(150, "isAmerican", BOOLEAN, "american?", IN, WNT, OPT);

    public static final Field issuerName = createEnumStatic(151, "issuerName", STRING, "Emittent", IN, BND, GNS, CER, FND, WNT, ZNS);

    public static final Field underlyingWkn = createEnum(152, "underlyingWkn", STRING, "WKN (Underlying)", IN, CER, WNT, FUT, OPT);

    public static final Field underlyingIsin = createEnum(153, "underlyingIsin", STRING, "ISIN (Underlying)", IN, CER, WNT, FUT, OPT);

    public static final Field underlyingName = createEnum(154, "underlyingName", STRING, "Name (Underlying)", IN, CER, WNT, FUT, OPT);

    public static final Field vwdCode = create(155, "vwdCode", STRING, "vwd-Schlüssel", QS, BND, GNS, CER, FND, STK, WNT, FUT, OPT, IND, CUR, MER, ZNS);

    public static final Field mmwkn = create(156, "mmwkn", STRING, "MMWKN", QS, BND, GNS, CER, FND, STK, WNT, CUR, IND, FUT, OPT, MER, ZNS);

    public static final Field currency = createEnum(157, "currency", STRING, "Währung", QS, BND, GNS, CER, FND, STK, WNT, FUT, OPT, IND, CUR, MER, ZNS);

    public static final Field yieldRelative_mdps = createPercent(158, "yieldRelative_mdps", DECIMAL, "Rendite, relativ (MDPS)", QR, BND, GNS);

    public static final Field duration_mdps = create(159, "duration_mdps", DECIMAL, "Duration (MDPS)", QR, BND, GNS);

    public static final Field interest = createPercentStatic(160, "interest", DECIMAL, "Zinssatz", IN, BND, GNS, CER);

    public static final Field interestType = createEnumStatic(161, "interestType", STRING, "Kupontyp", IN, BND, GNS);

    public static final Field country = createEnumStatic(162, "country", STRING, "Land", LOCALES_DE_EN, IN, BND, GNS, FND, STK, ZNS);

    public static final Field bondType = createEnumStatic(163, "bondType", STRING, "Typ", IN, BND, GNS);

    public static final Field ratingFitchShortTerm = createEnumStatic(164, "ratingFitchShortTerm", STRING, "Fitch short term", IN, BND);

    public static final Field ratingMoodysLongTerm = createEnumStatic(165, "ratingMoodysLongTerm", STRING, "Moodys LT", IN, BND);

    public static final Field underlyingType = createEnumStatic(166, "underlyingType", STRING, "Typ (Underlying)", IN, CER, WNT);

    public static final Field highAlltime = create(167, "highAlltime", DECIMAL, "Allzeit-Hoch", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field lowAlltime = create(168, "lowAlltime", DECIMAL, "Allzeit-Tief", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field highAlltimeDate = create(169, "highAlltimeDate", DATE, "Datum Allzeit-Hoch", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field gatrixxQuanto = createStatic(170, "gatrixxQuanto", BOOLEAN, "Quanto?", IN, CER);

    public static final Field lowAlltimeDate = create(171, "lowAlltimeDate", DATE, "Datum Allzeit-Tief", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field exerciseType = createEnumStatic(172, "exerciseType", STRING, "Ausübungsart", IN, CER, OPT);

    public static final Field changePercentHigh52Weeks = createPercent(175, "changepercenthigh52weeks", DECIMAL, "Abstand zum 52 Wochen-Hoch, relativ", QR, STK);

    public static final Field changePercentLow52Weeks = createPercent(176, "changepercentlow52weeks", DECIMAL, "Abstand zum 52 Wochen-Tief, relativ", QR, STK);

    public static final Field high1yDate = create(177, "high1yDate", DATE, "Datum 52-W-Hoch", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field low1yDate = create(178, "low1yDate", DATE, "Datum 52-W-Tief", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field averagePrice1w = create(179, "averagePrice1w", DECIMAL, "Durchschnittspreis (1 Woche)", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, MER, ZNS);

    public static final Field averagePrice1m = create(180, "averagePrice1m", DECIMAL, "Durchschnittspreis (1 Monat)", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, MER, ZNS);

    public static final Field averagePrice3m = create(181, "averagePrice3m", DECIMAL, "Durchschnittspreis (3 Monate)", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, MER, ZNS);

    public static final Field averagePrice6m = create(182, "averagePrice6m", DECIMAL, "Durchschnittspreis (6 Monate)", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, MER, ZNS);

    public static final Field averagePrice1y = create(183, "averagePrice1y", DECIMAL, "Durchschnittspreis (1 Jahr)", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, MER, ZNS);

    public static final Field averagePrice3y = create(184, "averagePrice3y", DECIMAL, "Durchschnittspreis (3 Jahre)", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, MER, ZNS);

    public static final Field issueVolume = createStatic(185, "issueVolume", NUMBER, "Emissionsvolumen", IN, BND, GNS, CER, FND);

    public static final Field averagePrice5y = create(186, "averagePrice5y", DECIMAL, "Durchschnittspreis (5 Jahre)", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, MER, ZNS);

    public static final Field averagePrice10y = create(187, "averagePrice10y", DECIMAL, "Durchschnittspreis (10 Jahre)", QR, BND, CER, GNS, FND, STK, IND, CUR, FUT, MER, ZNS);

    public static final Field beta1w = create(188, "beta1w", DECIMAL, "Beta-Faktor (1 Monat)", QR, STK, BND, GNS, FND);

    public static final Field beta3m = create(189, "beta3m", DECIMAL, "Beta-Faktor (3 Monate)", QR, STK, BND, GNS, FND);

    public static final Field beta6m = create(190, "beta6m", DECIMAL, "Beta-Faktor (6 Monate)", QR, STK, BND, GNS, FND);

    public static final Field performanceToBenchmark3y = createPercent(191, "performancetobenchmark3y", DECIMAL, "rel. Performance zu Benchmark (3 Jahre)", QR, BND, GNS, STK, FND);

    public static final Field performanceToBenchmark5y = createPercent(192, "performancetobenchmark5y", DECIMAL, "rel. Performance zu Benchmark (5 Jahre)", QR, BND, GNS, STK, FND);

    public static final Field lowerKnock = createStatic(193, "lowerKnock", DECIMAL, "Knock-In", IN, CER);

    public static final Field upperKnock = createStatic(194, "upperKnock", DECIMAL, "Knock-Out", IN, CER);

    public static final Field lowerRange = createStatic(195, "lowerRange", DECIMAL, "Untere Range", IN, CER);

    public static final Field upperRange = createStatic(196, "upperRange", DECIMAL, "Obere Range", IN, CER);

    public static final Field wmRedemptionPrice = createStatic(200, "wmRedemptionPrice", DECIMAL, "Rückzahlungskurs (WM)", IN, BND);

    public static final Field issueDate = createStatic(202, "issueDate", DATE, "Emissionsdatum", IN, BND, GNS, CER, FND, WNT);

    public static final Field sector = createEnumStatic(203, "sector", STRING, "Branche", LOCALES_DE_EN, IN, FND, STK, ZNS);

    public static final Field performanceToBenchmark10y = createPercent(204, "performancetobenchmark10y", DECIMAL, "rel. Performance zu Benchmark (10 Jahre)", QR, BND, GNS, STK, FND);

    public static final Field fwwIssueSurcharge = createPercentStatic(215, "fwwIssueSurcharge", DECIMAL, "Ausgabeaufschlag (FWW)", IN, FND);

    public static final Field fwwManagementFee = createPercentStatic(216, "fwwManagementFee", DECIMAL, "Managementgebühr (FWW)", IN, FND);

    public static final Field fwwRiskclass = createStatic(217, "fwwRiskclass", NUMBER, "Risikoklasse (FWW)", IN, FND);

    public static final Field factsetCurrentPriceSalesRatio1Y = create(232, "factsetCurrentPriceSalesRatio1Y", DECIMAL, "Price/Sales Ratio (Factset und aktueller Kurs, FY1)", QR, STK);

    public static final Field factsetCurrentPriceSalesRatio2Y = create(233, "factsetcurrentpricesalesratio2y", DECIMAL, "Price/Sales Ratio (Factset und aktueller Kurs, FY2)", QR, STK);

    public static final Field performanceToBenchmark1d = createPercent(234, "performancetobenchmark1d", DECIMAL, "rel. Performance zu Benchmark (1 Tag)", QR, STK);

    public static final Field changeNet = createPercent(240, "changeNet", DECIMAL, "Differenz", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field changePercent = createPercent(241, "changePercent", DECIMAL, "relative Differenz", QR, BND, GNS, CER, FND, STK, WNT);

    public static final Field bviperformance1m = createPercent(243, "bviperformance1m", DECIMAL, "BVI-Perf. (1 Monat)", QR, FND);

    public static final Field bviperformance3m = createPercent(244, "bviperformance3m", DECIMAL, "BVI-Perf. (3 Monate)", QR, FND);

    public static final Field fwwAccountFee = createPercentStatic(245, "fwwAccountFee", DECIMAL, "Depotbankvergütung (FWW)", IN, FND);

    public static final Field fwwFundType = createEnumStatic(250, "fwwFundType", STRING, "Fondstyp (FWW)", IN, FND);

    public static final Field fwwSector = createEnumStatic(251, "fwwSector", STRING, "Sektor (FWW)", IN, FND);

    public static final Field bviperformance6m = createPercent(252, "bviperformance6m", DECIMAL, "BVI-Perf. (6 Monate)", QR, FND);

    public static final Field bviperformance5y = createPercent(253, "bviperformance5y", DECIMAL, "BVI-Perf. (5 Jahre)", QR, FND);

    public static final Field bviperformance10y = createPercent(254, "bviperformance10y", DECIMAL, "BVI-Perf. (10 Jahre)", QR, FND);

    public static final Field negativeMonthsPercent1m = createPercent(255, "negativeMonthsPercent1m", DECIMAL, "Verlustmonate, prozentual (1 Monat)", QR, FND);

    public static final Field negativeMonthsPercent3m = createPercent(256, "negativeMonthsPercent3m", DECIMAL, "Verlustmonate, prozentual (3 Monate)", QR, FND);

    public static final Field negativeMonthsPercent6m = createPercent(257, "negativeMonthsPercent6m", DECIMAL, "Verlustmonate, prozentual (6 Monate)", QR, FND);

    public static final Field negativeMonthsPercent1y = createPercent(258, "negativeMonthsPercent1y", DECIMAL, "Verlustmonate, prozentual (1 Jahr)", QR, FND);

    public static final Field negativeMonthsPercent3y = createPercent(259, "negativeMonthsPercent3y", DECIMAL, "Verlustmonate, prozentual (3 Jahre)", QR, FND);

    public static final Field negativeMonthsPercent5y = createPercent(260, "negativeMonthsPercent5y", DECIMAL, "Verlustmonate, prozentual (5 Jahre)", QR, FND);

    public static final Field negativeMonthsPercent10y = createPercent(261, "negativeMonthsPercent10y", DECIMAL, "Verlustmonate, prozentual (10 Jahre)", QR, FND);

    public static final Field factsetProfit2Y = createStatic(269, "factsetProfit2Y", DECIMAL, "Gewinn (Factset, in 2 Jahren)", IN, STK);

    public static final Field factsetEps2Y = createStatic(270, "factsetEps2Y", DECIMAL, "EPS (Factset, in 2 Jahren)", IN, STK);

    public static final Field factsetDividend2Y = createStatic(271, "factsetDividend2Y", DECIMAL, "Dividende (Factset, in 2 Jahren)", IN, STK);

    public static final Field factsetDividendyield2Y = createPercentStatic(272, "factsetDividendyield2Y", DECIMAL, "Dividendenrendite (Factset, in 2 Jahren)", IN, STK);

    public static final Field factsetProfit1Y = createStatic(273, "factsetProfit1Y", DECIMAL, "Gewinn (Factset, in 1 Jahr)", IN, STK);

    public static final Field factsetEps1Y = createStatic(274, "factsetEps1Y", DECIMAL, "EPS (Factset, in 1 Jahr)", IN, STK);

    public static final Field factsetDividend1Y = createStatic(275, "factsetDividend1Y", DECIMAL, "Dividende (Factset, in 1 Jahr)", IN, STK);

    public static final Field factsetDividendyield1Y = createPercentStatic(276, "factsetDividendyield1Y", DECIMAL, "Dividendenrendite (Factset, in 1 Jahr)", IN, STK);

    public static final Field factsetProfit0Y = createStatic(277, "factsetProfit0Y", DECIMAL, "Gewinn (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetEps0Y = createStatic(278, "factsetEps0Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetDividend0Y = createStatic(279, "factsetDividend0Y", DECIMAL, "Dividende (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetDividendyield0Y = createPercentStatic(280, "factsetDividendyield0Y", DECIMAL, "Dividendenrendite (Factset, aktuelles Jahr)", IN, STK);

    public static final Field convensysProfit_1Y = create(281, "convensysProfit_1Y", DECIMAL, "Gewinn (Convensys, vor 1 Jahr)", IN, STK);

    public static final Field convensysEps_1Y = create(282, "convensysEps_1Y", DECIMAL, "EPS (Convensys, vor 1 Jahr)", IN, STK);

    public static final Field convensysDividend_1Y = create(283, "convensysDividend_1Y", DECIMAL, "Dividende (Convensys, vor 1 Jahr)", IN, STK);

    public static final Field convensysDividendyield_1Y = createPercent(284, "convensysDividendyield_1Y", DECIMAL, "Dividendenrendite (Convensys, vor 1 Jahr)", IN, STK);

    public static final Field convensysProfit_2Y = create(285, "convensysProfit_2Y", DECIMAL, "Gewinn (Convensys, vor 2 Jahren)", IN, STK);

    public static final Field convensysEps_2Y = create(286, "convensysEps_2Y", DECIMAL, "EPS (Convensys, vor 2 Jahren)", IN, STK);

    public static final Field convensysDividend_2Y = create(287, "convensysDividend_2Y", DECIMAL, "Dividende (Convensys, vor 2 Jahren)", IN, STK);

    public static final Field convensysDividendyield_2Y = createPercent(288, "convensysDividendyield_2Y", DECIMAL, "Dividendenrendite (Convensys, vor 2 Jahren)", IN, STK);

    public static final Field gatrixxType = createEnumStatic(289, "gatrixxType", STRING, "Zertifikatetyp (gatrixx)", IN, CER);

    public static final Field gatrixxStrikePrice = createStatic(290, "gatrixxStrikePrice", DECIMAL, "Strike (gatrixx)", IN, CER);

    public static final Field ratingFitchLongTerm = createEnumStatic(291, "ratingFitchLongTerm", STRING, "Fitch long term", IN, BND);

    public static final Field gatrixxCoupon = createPercentStatic(292, "gatrixxCoupon", DECIMAL, "Kupon (gatrixx)", IN, CER);

    public static final Field gatrixxCap = createStatic(293, "gatrixxCap", DECIMAL, "Cap (gatrixx)", IN, CER);

    public static final Field gatrixxKnockin = createStatic(294, "gatrixxKnockin", DECIMAL, "Knockin (gatrixx)", IN, CER);

    public static final Field gatrixxBonuslevel = createStatic(295, "gatrixxBonuslevel", DECIMAL, "Bonuslevel (gatrixx)", IN, CER);

    public static final Field gatrixxBarrier = createStatic(296, "gatrixxBarrier", DECIMAL, "Barriere (gatrixx)", IN, CER);

    public static final Field gatrixxGuaranteeType = createEnumStatic(297, "gatrixxGuaranteeType", STRING, "Garantietyp (gatrixx)", IN, CER);

    public static final Field gatrixxLeverageType = createEnumStatic(298, "gatrixxLeverageType", STRING, "Hebeltyp (gatrixx)", IN, CER);

    public static final Field volatility3y = createPercent(299, "volatility3y", DECIMAL, "Vola. (3 Jahre)", QR, BND, GNS, CER, FND, STK, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field performance1w = createPercent(300, "performance1w", DECIMAL, "Performance (1 Woche)", QR, BND, GNS, CER, STK, WNT, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field wmBondtype = createEnumStatic(301, "wmBondtype", STRING, "Anleihetyp (WM)", LOCALES_DE_EN, IN, BND, GNS);

    public static final Field wmCoupontype = createEnumStatic(302, "wmCoupontype", STRING, "Kupontyp (WM)", LOCALES_DE_EN, IN, BND, GNS);

    public static final Field wmNominalInterest = createPercentStatic(304, "wmNominalInterest", DECIMAL, "Zinssatz (WM)", IN, BND, GNS);

    public static final Field wmInterestPeriod = createEnumStatic(306, "wmInterestPeriod", STRING, "Zinsperiode (WM)", LOCALES_DE_EN, IN, BND, GNS);

    public static final Field correlation1w = create(307, "correlation1w", DECIMAL, "Korrelation (1 Woche)", QR, STK, BND, GNS, FND);

    public static final Field correlation3m = create(308, "correlation3m", DECIMAL, "Korrelation (3 Monat3)", QR, STK, BND, GNS, FND);

    public static final Field correlation6m = create(309, "correlation6m", DECIMAL, "Korrelation (6 Monat3)", QR, STK, BND, GNS, FND);

    public static final Field correlation3y = create(310, "correlation3y", DECIMAL, "Korrelation (3 Jahre)", QR, STK, BND, GNS, FND);

    public static final Field correlation5y = create(311, "correlation5y", DECIMAL, "Korrelation (5 Jahre)", QR, STK, BND, GNS, FND);

    public static final Field correlation10y = create(312, "correlation10y", DECIMAL, "Korrelation (10 Jahre)", QR, STK, BND, GNS, FND);

    public static final Field fwwKag = createEnumStatic(313, "fwwKag", STRING, "KAG (FWW)", IN, FND);

    public static final Field ratingFeri = createEnumStatic(314, "ratingFeri", STRING, "null", IN, FND);

    public static final Field maximumLoss1y = createPercent(315, "maximumLoss1y", DECIMAL, "maximaler Verlust (1 Jahr)", QR, FND, STK, IND);

    public static final Field maximumLossDays1y = create(316, "maximumLossDays1y", NUMBER, "längste Verlustperiode (1 Jahr)", QR, FND);

    public static final Field underlyingIid = create(317, "underlyingIid", NUMBER, "iid (Underlying)", IN, CER, WNT, FUT, OPT);

    public static final Field high1y = create(319, "high1y", DECIMAL, "Hoch (1 Jahr)", QR, BND, CER, GNS, STK, FND, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field low1y = create(320, "low1y", DECIMAL, "Tief (1 Jahr)", QR, BND, CER, GNS, STK, FND, IND, CUR, FUT, OPT, MER, ZNS);

    public static final Field maximumLoss6m = createPercent(321, "maximumLoss6m", DECIMAL, "maximaler Verlust (6 Monate)", QR, FND, STK, IND);

    public static final Field maximumLossMonths3y = create(322, "maximumLossMonths3y", DECIMAL, "Längste Verlustperiode (3 Jahre in Monaten)", QR, FND);

    public static final Field averageVolume1w = create(323, "averagevolume1w", DECIMAL, "Durchschnittsvolumen (1 Woche)", QR, BND, CER, GNS, FND, STK, WNT, IND, FUT);

    public static final Field averageVolume3m = create(324, "averagevolume3m", DECIMAL, "Durchschnittsvolumen (3 Monate)", QR, BND, CER, GNS, FND, STK, IND, FUT);

    public static final Field averageVolume6m = create(325, "averagevolume6m", DECIMAL, "Durchschnittsvolumen (6 Monate)", QR, BND, CER, GNS, FND, STK, IND, FUT);

    public static final Field averageVolume3y = create(326, "averagevolume3y", DECIMAL, "Durchschnittsvolumen (3 Jahre)", QR, BND, CER, GNS, FND, STK, IND, FUT);

    public static final Field averageVolume5y = create(327, "averagevolume5y", DECIMAL, "Durchschnittsvolumen (5 Jahre)", QR, BND, CER, GNS, FND, STK, IND, FUT);

    public static final Field averageVolume10y = create(329, "averagevolume10y", DECIMAL, "Durchschnittsvolumen (10 Jahre)", QR, BND, CER, GNS, FND, STK, IND, FUT);

    public static final Field factsetCurrentPriceEarningRatio1Y = create(330, "factsetcurrentpriceearningratio1y", DECIMAL, "Price/Earning Ratio (Factset und aktueller Kurs, FY1)", QR, STK);

    public static final Field factsetCurrentPriceEarningRatio2Y = create(331, "factsetcurrentpriceearningratio2y", DECIMAL, "Price/Earning Ratio (Factset und aktueller Kurs, FY2)", QR, STK);

    public static final Field marketmanagerName = create(332, "marketmanagerName", STRING, "market manager-Name", QS, BND, CER, CUR, FND, FUT, GNS, IND, MER, OPT, STK, WNT, ZNS);

    public static final Field bviperformancecurrentyear = createPercent(333, "bviperformancecurrentyear", DECIMAL, "BVI-Perf. (lfd. Jahr)", QR, FND);

    public static final Field maximumLoss3y = createPercent(334, "maximumLoss3y", DECIMAL, "maximaler Verlust (3 Jahre)", QR, FND, STK, IND);

    public static final Field volatilityCurrentYear = createPercent(342, "volatilityCurrentYear", DECIMAL, "Volatilität (lfd. Jahr)", QR, BND, CER, CUR, FND, FUT, GNS, IND, MER, OPT, STK, WNT, ZNS);

    public static final Field morningstars = createEnumStatic(343, "morningstars", NUMBER, "Morningstars", IN, FND);

    public static final Field factsetPriceEarningRatio0Y = createStatic(344, "factsetPriceEarningRatio0Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetCashflow0Y = createStatic(345, "factsetCashflow0Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field alpha1m = createPercent(346, "alpha1m", DECIMAL, "Alpha (1 Monat)", QR, STK, BND, GNS, FND);

    public static final Field alpha1y = createPercent(347, "alpha1y", DECIMAL, "Alpha (1 Jahr)", QR, STK, BND, GNS, FND);

    public static final Field factsetLongTermGrowth = createStatic(348, "factsetLongTermGrowth", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetCurrentPriceCashflowRatio1Y = create(349, "factsetcurrentpricecashflowratio1y", DECIMAL, "Price/Cashflow Ratio (Factset und aktueller Kurs, FY1)", QR, STK);

    public static final Field factsetCurrentPriceCashflowRatio2Y = create(350, "factsetcurrentpricecashflowratio2y", DECIMAL, "Price/Cashflow Ratio (Factset und aktueller Kurs, FY2)", QR, STK);

    public static final Field factsetCurrentPriceBookvalueRatio1Y = create(351, "factsetcurrentpricebookvalueratio1y", DECIMAL, "Price/Bookvalue Ratio (Factset und aktueller Kurs, FY1)", QR, STK);

    public static final Field factsetCurrentPriceBookvalueRatio2Y = create(352, "factsetcurrentpricebookvalueratio2y", DECIMAL, "Price/Bookvalue Ratio (Factset und aktueller Kurs, FY2)", QR, STK);

    public static final Field wmCountry = createEnumStatic(353, "wmCountry", STRING, "Land (WM)", LOCALES_DE_EN, IN, BND, GNS, STK);

    public static final Field wmIssueCurrency = createEnumStatic(354, "wmissuecurrency", STRING, "Emissionswährung (WM)", IN, BND, GNS);

    public static final Field mdpsBasePointValue = createPercent(355, "mdpsbasepointvalue", DECIMAL, "BPV (MDPS)", QR, BND, GNS);

    public static final Field factsetPriceTarget = createStatic(356, "factsetPriceTarget", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field mdpsConvexity = create(357, "mdpsconvexity", DECIMAL, "Konvexität (MDPS)", QR, BND, GNS);

    public static final Field mdpsModifiedDuration = create(358, "mdpsmodifiedduration", DECIMAL, "Modified Duration (MDPS)", QR, BND, GNS);

    public static final Field mdpsBrokenPeriodInterest = create(359, "mdpsbrokenperiodinterest", DECIMAL, "Stückzinsen (MDPS)", QR, BND, GNS, CER);

    public static final Field factsetPriceEarningRatio1Y = createStatic(360, "factsetPriceEarningRatio1Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetCashflow1Y = createStatic(361, "factsetCashflow1Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetEPSAfterGoodwill1Y = createStatic(362, "factsetEPSAfterGoodwill1Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetEPSBeforeGoodwill1Y = createStatic(363, "factsetEPSBeforeGoodwill1Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetFiscalYear = createStatic(364, "factsetFiscalYear", NUMBER, "Fiscal Year (Factset, Bezugsjahr)", IN, STK);

    public static final Field factsetBookValue1Y = createStatic(365, "factsetBookValue1Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field mdpsInterestRateElasticity = create(366, "mdpsinterestrateelasticity", DECIMAL, "Zinselastizität (MDPS)", QR, BND, GNS);

    public static final Field factsetEbit1Y = createStatic(367, "factsetEbit1Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetEbitda1Y = createStatic(368, "factsetEbitda1Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetGoodwill1Y = createStatic(369, "factsetGoodwill1Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetNetDebt1Y = createStatic(370, "factsetNetDebt1Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field beta3y = create(371, "beta3y", DECIMAL, "Beta-Faktor (3 Jahre)", QR, STK, BND, GNS, FND);

    public static final Field factsetCurrency = createEnumStatic(372, "factsetCurrency", STRING, "Währung (Factset)", IN, STK);

    public static final Field factsetSales1Y = createStatic(373, "factsetSales1Y", DECIMAL, "EPS (Factset, aktuelles Jahr, in Mio.)", IN, STK);

    public static final Field factsetPostEventConsensus1Y = create(374, "factsetPostEventConsensus1Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetPreTaxProfit1Y = createStatic(375, "factsetPreTaxProfit1Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetPriceEarningRatio2Y = createStatic(376, "factsetPriceEarningRatio2Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetCashflow2Y = createStatic(377, "factsetCashflow2Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetEPSAfterGoodwill2Y = createStatic(378, "factsetEPSAfterGoodwill2Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetEPSBeforeGoodwill2Y = createStatic(379, "factsetEPSBeforeGoodwill2Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field isEtf = createEnumStatic(380, "isEtf", BOOLEAN, "ETF?", IN, FND);

    public static final Field factsetBookValue2Y = createStatic(381, "factsetBookValue2Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field beta5y = create(382, "beta5y", DECIMAL, "Beta-Faktor (5 Jahre)", QR, STK, BND, GNS, FND);

    public static final Field factsetEbit2Y = createStatic(383, "factsetEbit2Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetEbitda2Y = createStatic(384, "factsetEbitda2Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetGoodwill2Y = createStatic(385, "factsetGoodwill2Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetNetDebt2Y = createStatic(386, "factsetNetDebt2Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field beta10y = create(387, "beta10y", DECIMAL, "Beta-Faktor (10 Jahre)", QR, STK, BND, GNS, FND);

    public static final Field factsetSales2Y = createStatic(389, "factsetSales2Y", DECIMAL, "EPS (Factset, aktuelles Jahr, in Mio.)", IN, STK);

    public static final Field factsetPostEventConsensus2Y = create(390, "factsetPostEventConsensus2Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetPreTaxProfit2Y = createStatic(391, "factsetPreTaxProfit2Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field performanceAlltime = createPercent(392, "performanceAlltime", DECIMAL, "hist. Performance", QR, BND, GNS, CER, WNT, STK, IND, CUR, CER, MER, ZNS);

    public static final Field bviperformance1w = createPercent(393, "bviperformance1w", DECIMAL, "BVI-Perf. (1 Woche)", QR, FND);

    public static final Field sharperatio1w = create(394, "sharperatio1w", DECIMAL, "Sharpe Ratio (1 Woche)", QR, CER, FND, IND);

    public static final Field sharperatio10y = create(395, "sharperatio10y", DECIMAL, "Sharpe Ratio (10 Jahre)", QR, CER, FND, IND);

    public static final Field treynor1w = create(396, "treynor1w", DECIMAL, "Treynor (1 Woche)", QR, FND);

    public static final Field treynor1m = create(397, "treynor1m", DECIMAL, "Treynor (1 Monat)", QR, FND);

    public static final Field treynor3m = create(398, "treynor3m", DECIMAL, "Treynor (3 Monate)", QR, FND);

    public static final Field treynor6m = create(399, "treynor6m", DECIMAL, "Treynor (6 Monate)", QR, FND);

    public static final Field treynor5y = create(400, "treynor5y", DECIMAL, "Treynor (5 Jahre)", QR, FND);

    public static final Field treynor10y = create(401, "treynor10y", DECIMAL, "Treynor (10 Jahre)", QR, FND);

    public static final Field maximumLossMonths1y = create(402, "maximumLossMonths1y", DECIMAL, "Längste Verlustperiode (1 Jahr in Monaten)", QR, FND);

    public static final Field maximumLossMonths5y = create(403, "maximumLossMonths5y", DECIMAL, "Längste Verlustperiode (5 Jahre in Monaten)", QR, FND);

    public static final Field maximumLossMonths10y = create(404, "maximumLossMonths10y", DECIMAL, "Längste Verlustperiode (10 Jahre in Monaten)", QR, FND);

    public static final Field referencePrice = create(405, "referencePrice", DECIMAL, "Referenzkurs hist. Kennzahlen", QR, BND, GNS, CER, FND, STK, WNT, IND, CUR, OPT, FUT, MER, ZNS);

    public static final Field bviperformanceAlltime = createPercent(406, "bviperformanceAlltime", DECIMAL, "BVI-Perf. (Allzeit)", QR, FND);

    public static final Field pari = create(407, "pari", DECIMAL, "pari-Indikator (Kurs-Rückzahlung)", QR, BND, GNS);

    public static final Field vrIssuer = create(408, "vrIssuer", BOOLEAN, "DZ,WGZ,UNION", IN, FND, WNT, CER);

    public static final Field wmVrIssuer = create(409, "wmVrIssuer", BOOLEAN, "DZ,WGZ,UNION", IN, FND, WNT, CER);

    public static final Field dzCategory = createEnumStatic(411, "dzCategory", STRING, "Kategorie (DZ BANK)", LOCALES_DE_EN, IN, CER);

    public static final Field gatrixxProtectlevel = createStatic(412, "gatrixxprotectlevel", DECIMAL, "Kapitalschutz-Level (gatrixx)", IN, CER);

    public static final Field gatrixxTypeFtreff = createEnumStatic(413, "gatrixxtypeftreff", STRING, "Derivattyp (gatrixx, Finanztreff)", IN, CER);

    public static final Field vwdMarket = createEnum(414, "vwdMarket", STRING, "vwd-Markt", QS, BND, GNS, CER, FND, STK, WNT, FUT, OPT, IND, CUR, MER, ZNS);

    public static final Field bviKategorieGrob = createEnumStatic(415, "bviKategorieGrob", STRING, "BVI-Grobklassifizierung (Feri)", IN, FND);

    public static final Field marketAdmission = createStatic(416, "marketAdmission", ENUMSET, "Vertriebszulassung (vwd/IVMKAGS)", IN, FND);

    public static final Field closePreviousYear = create(417, "closePreviousYear", DECIMAL, "Vorjahresschluss", QR, BND, GNS, CUR, FND, FUT, IND, OPT, STK, MER, ZNS);

    public static final Field closePreviousYearDate = create(418, "closePreviousYearDate", DATE, "Datum Vorjahresschluss", QR, BND, GNS, CUR, FND, FUT, IND, OPT, STK, MER, ZNS);

    public static final Field closePreviousMonth = create(419, "closePreviousMonth", DECIMAL, "Vormonatsschluss", QR, BND, GNS, CUR, FND, FUT, IND, OPT, STK, MER, ZNS);

    public static final Field closePreviousMonthDate = create(420, "closePreviousMonthDate", DATE, "Datum Vormonatsschluss", QR, BND, GNS, CUR, FND, FUT, IND, OPT, STK, MER, ZNS);

    public static final Field performanceCurrentYear = createPercent(421, "performanceCurrentYear", DECIMAL, "Performance aktuelles Jahr", QR, BND, CER, GNS, CUR, FUT, IND, OPT, STK, MER, ZNS, WNT);

    public static final Field performanceCurrentMonth = createPercent(422, "performanceCurrentMonth", DECIMAL, "Performance aktueller Monat", QR, BND, GNS, CUR, FUT, IND, OPT, STK, MER, ZNS);

    public static final Field highPreviousYear = create(423, "highPreviousYear", DECIMAL, "Vorjahreshoch", QR, BND, GNS, CUR, FND, FUT, IND, OPT, STK, MER, ZNS);

    public static final Field highPreviousYearDate = create(424, "highPreviousYearDate", DATE, "Datum Vorjahreshoch", QR, BND, GNS, CUR, FND, FUT, IND, OPT, STK, MER, ZNS);

    public static final Field lowPreviousYear = create(425, "lowPreviousYear", DECIMAL, "Vorjahrestief", QR, BND, GNS, CUR, FND, FUT, IND, OPT, STK, MER, ZNS);

    public static final Field lowPreviousYearDate = create(426, "lowPreviousYearDate", DATE, "Datum Vorjahrestief", QR, BND, GNS, CUR, FND, FUT, IND, OPT, STK, MER, ZNS);

    public static final Field bviperformancecurrentmonth = createPercent(427, "bviperformancecurrentmonth", DECIMAL, "BVI-Perf. (lfd. Monat)", QR, FND);

    public static final Field changeNetCurrentYear = create(428, "changeNetCurrentYear", DECIMAL, "abs. Veränderung aktuelles Jahr", QR, BND, GNS, CUR, FND, FUT, IND, OPT, STK, MER, ZNS);

    public static final Field changeNetCurrentMonth = create(429, "changeNetCurrentMonth", DECIMAL, "abs. Veränderung aktueller Monat", QR, BND, GNS, CUR, FND, FUT, IND, OPT, STK, MER, ZNS);

    public static final Field msVrIssuer = create(430, "msVrIssuer", BOOLEAN, "DZ,WGZ,UNION (Morningstar)", IN, FND);

    public static final Field msIssuername = createEnumStatic(432, "msIssuername", STRING, "Emittent (Morningstar)", IN, FND);

    public static final Field msCountry = createEnumStatic(433, "msCountry", STRING, "Land (Morningstar)", LOCALES_DE_EN, IN, FND);

    public static final Field msDistributionStrategy = createEnumStatic(434, "msDistributionStrategy", STRING, "Ausschüttungsart (Morningstar)", LOCALES_DE_EN, IN, FND);

    public static final Field msFundVolume = createStatic(435, "msFundVolume", DECIMAL, "Fondsvolumen (Morningstar)", IN, FND);

    public static final Field msIssueDate = createStatic(436, "msIssueDate", DATE, "Emissionsdatum (Morningstar)", IN, FND);

    public static final Field msIssueSurcharge = createPercentStatic(437, "msIssueSurcharge", DECIMAL, "Ausgabeaufschlag (Morningstar)", IN, FND);

    public static final Field msManagementfee = createPercentStatic(438, "msManagementfee", DECIMAL, "Managementfee (Morningstar)", IN, FND);

    public static final Field msAccountfee = createPercentStatic(439, "msAccountfee", DECIMAL, "Depotbankgebühr (Morningstar)", IN, FND);

    public static final Field msTer = createPercentStatic(440, "msTer", DECIMAL, "TER (Morningstar)", IN, FND);

    public static final Field alpha1w = createPercent(441, "alpha1w", DECIMAL, "Alpha (1 Woche)", QR, STK, BND, GNS, FND);

    public static final Field alpha3m = createPercent(442, "alpha3m", DECIMAL, "Alpha (3 Monate)", QR, STK, BND, GNS, FND);

    public static final Field alpha6m = createPercent(443, "alpha6m", DECIMAL, "Alpha (6 Monate)", QR, STK, BND, GNS, FND);

    public static final Field alpha3y = createPercent(444, "alpha3y", DECIMAL, "Alpha (3 Jahre)", QR, STK, BND, GNS, FND);

    public static final Field alpha5y = createPercent(445, "alpha5y", DECIMAL, "Alpha (5 Jahre)", QR, STK, BND, GNS, FND);

    public static final Field alpha10y = createPercent(446, "alpha10y", DECIMAL, "Alpha (10 Jahre)", QR, STK, BND, GNS, FND);

    public static final Field msInvestmentFocus = createEnumStatic(447, "msInvestmentFocus", STRING, "Anlageschwerpunkt (Morningstar)", LOCALES_DE_EN, IN, FND);

    public static final Field mdpsDiscount = create(448, "mdpsDiscount", DECIMAL, "Discount", QR, CER);

    public static final Field mdpsDiscountRelative = createPercent(449, "mdpsDiscountRelative", DECIMAL, "Discount, relativ", QR, CER);

    public static final Field mdpsYield = create(450, "mdpsYield", DECIMAL, "Ertrag", QR, CER);

    public static final Field mdpsYieldRelative = createPercent(451, "mdpsYieldRelative", DECIMAL, "Rendite, relativ", QR, CER);

    public static final Field mdpsYieldRelativePerYear = createPercent(452, "mdpsYieldRelativePerYear", DECIMAL, "Rendite p.a.", QR, CER);

    public static final Field mdpsGapCap = create(453, "mdpsGapCap", DECIMAL, "Abstand zum Cap", QR, CER);

    public static final Field mdpsGapCapRelative = createPercent(454, "mdpsGapCapRelative", DECIMAL, "Abstand zum Cap, relativ", QR, CER);

    public static final Field mdpsMaximumYield = create(455, "mdpsMaximumYield", DECIMAL, "max. Ertrag", QR, CER);

    public static final Field mdpsMaximumYieldRelative = createPercent(456, "mdpsMaximumYieldRelative", DECIMAL, "max. Rendite, relativ", QR, CER);

    public static final Field mdpsMaximumYieldRelativePerYear = createPercent(457, "mdpsMaximumYieldRelativePerYear", DECIMAL, "max. Rendite p.a.", QR, CER);

    public static final Field mdpsUnchangedYieldRelative = createPercent(458, "mdpsUnchangedYieldRelative", DECIMAL, "Seitwärtsertrag, relativ", QR, CER);

    public static final Field mdpsUnchangedYieldRelativePerYear = createPercent(459, "mdpsUnchangedYieldRelativePerYear", DECIMAL, "Seitwärtsrendite p.a.", QR, CER);

    public static final Field mdpsGapBonusLevelRelative = createPercent(460, "mdpsGapBonusLevelRelative", DECIMAL, "Abstand Bonuslevel, relativ", QR, CER);

    public static final Field mdpsGapBonusBufferRelative = createPercent(461, "mdpsGapBonusBufferRelative", DECIMAL, "Abstand Bonuspuffer, relativ", QR, CER);

    public static final Field mdpsAgioRelative = createPercent(462, "mdpsAgioRelative", DECIMAL, "Aufgeld, relativ", QR, CER);

    public static final Field mdpsAgioRelativePerYear = createPercent(463, "mdpsAgioRelativePerYear", DECIMAL, "Aufgeld p.a.", QR, CER);

    public static final Field mdpsGapLowerBarrier = create(465, "mdpsGapLowerBarrier", DECIMAL, "Abstand untere Barriere", QR, CER);

    public static final Field mdpsGapLowerBarrierRelative = createPercent(466, "mdpsGapLowerBarrierRelative", DECIMAL, "Abstand untere Barriere, relativ", QR, CER);

    public static final Field mdpsGapUpperBarrier = create(467, "mdpsGapUpperBarrier", DECIMAL, "Abstand obere Barriere", QR, CER);

    public static final Field mdpsGapUpperBarrierRelative = createPercent(468, "mdpsGapUpperBarrierRelative", DECIMAL, "Abstand obere Barriere, relativ", QR, CER);

    public static final Field mdpsUnderlyingToCapRelative = createPercent(469, "mdpsUnderlyingToCapRelative", DECIMAL, "erlaubter Rückgang", QR, CER);

    public static final Field mdpsCapToUnderlyingRelative = createPercent(470, "mdpsCapToUnderlyingRelative", DECIMAL, "notwendige Performance", QR, CER);

    public static final Field mdpsGapStrikeRelative = createPercent(471, "mdpsGapStrikeRelative", DECIMAL, "Abstand Strike, relativ", QR, CER);

    public static final Field mdpsGapBonusLevel = create(472, "mdpsGapBonusLevel", DECIMAL, "Abstand Bonuslevel", QR, CER);

    public static final Field mdpsPerformanceAlltime = createPercent(473, "mdpsPerformanceAlltime", DECIMAL, "Performance seit Auflegung", QR, CER);

    public static final Field msBenchmarkName = createStatic(488, "msBenchmarkName", STRING, "Name der Benchmark (Morningstar)", IN, FND);

    public static final Field wmDividend = createStatic(489, "wmDividend", DECIMAL, "Dividende (letzte nach WM)", IN, STK, GNS);

    public static final Field wmDividendCurrency = createStatic(490, "wmDividendCurrency", STRING, "Dividendenwährung", IN, STK, GNS);

    public static final Field wmDividendYield = createPercent(491, "wmDividendYield", DECIMAL, "Dividendenrendite (letzte/hochgerechnet nach WM)", QR, STK, GNS);

    public static final Field benchmarkName = create(492, "benchmarkName", STRING, "Name der Benchmark", IN, STK, BND, GNS);

    public static final Field gatrixxParticipationlevel = createStatic(493, "gatrixxParticipationlevel", DECIMAL, "Partizipationsrate (gatrixx)", IN, CER);

    public static final Field gatrixxTypename = createEnumStatic(494, "gatrixxTypename", STRING, "Typname Zertifikat, 3. Ebene (gatrixx)", LOCALES_DE_EN, IN, CER);

    public static final Field gatrixxMultiassetName = createStatic(495, "gatrixxMultiassetName", STRING, "Name des Underlyings für MultiAsset-Underlyings (gatrixx)", IN, CER);

    public static final Field mdpsAgio = create(496, "mdpsAgio", DECIMAL, "Aufgeld", QR, CER);

    public static final Field mdpsUnchangedYield = create(497, "mdpsUnchangedYield", DECIMAL, "Seitwärtsertrag", QR, CER);

    public static final Field mdpsOutperformanceValue = create(498, "mdpsOutperformanceValue", DECIMAL, "Outperformancepunkt", QR, CER);

    public static final Field mdpsGapStrike = create(499, "mdpsGapStrike", DECIMAL, "Abstand Strike", QR, CER);

    public static final Field mdpsLeverage = create(500, "mdpsLeverage", DECIMAL, "Hebel", QR, CER);

    public static final Field mdpsGapBarrier = create(501, "mdpsGapBarrier", DECIMAL, "Abstand Barriere", QR, CER);

    public static final Field mdpsGapBarrierRelative = createPercent(502, "mdpsGapBarrierRelative", DECIMAL, "Abstand Barriere, relativ", QR, CER);

    public static final Field gatrixxStoploss = createStatic(503, "gatrixxStoploss", DECIMAL, "StopLoss (gatrixx)", IN, CER);

    public static final Field dzWgzListid = createStatic(504, "dzWgzListid", NUMBER, "List-ID WGZ-Zertifikate (DZ BANK)", IN, BND, CER);

    public static final Field vwdBenchmarkQid = createStatic(505, "vwdBenchmarkQid", NUMBER, "Benchmark-qid (vwd)", IN, FND);

    public static final Field trackingError1w = create(506, "trackingError1w", DECIMAL, "Tracking Error (1 Woche)", QR, FND);

    public static final Field trackingError1m = create(507, "trackingError1m", DECIMAL, "Tracking Error (1 Monat)", QR, FND);

    public static final Field trackingError3m = create(508, "trackingError3m", DECIMAL, "Tracking Error (3 Monate)", QR, FND);

    public static final Field trackingError6m = create(509, "trackingError6m", DECIMAL, "Tracking Error (6 Monate)", QR, FND);

    public static final Field trackingError1y = create(510, "trackingError1y", DECIMAL, "Tracking Error (1 Jahr)", QR, FND);

    public static final Field trackingError3y = create(511, "trackingError3y", DECIMAL, "Tracking Error (3 Jahre)", QR, FND);

    public static final Field trackingError5y = create(512, "trackingError5y", DECIMAL, "Tracking Error (5 Jahre)", QR, FND);

    public static final Field trackingError10y = create(513, "trackingError10y", DECIMAL, "Tracking Error (10 Jahre)", QR, FND);

    public static final Field informationRatio1w = create(514, "informationRatio1w", DECIMAL, "Information Ratio (1 Woche)", QR, FND);

    public static final Field informationRatio1m = create(515, "informationRatio1m", DECIMAL, "Information Ratio (1 Monat)", QR, FND);

    public static final Field informationRatio3m = create(516, "informationRatio3m", DECIMAL, "Information Ratio (3 Monate)", QR, FND);

    public static final Field informationRatio6m = create(517, "informationRatio6m", DECIMAL, "Information Ratio (6 Monate)", QR, FND);

    public static final Field informationRatio1y = create(518, "informationRatio1y", DECIMAL, "Information Ratio (1 Jahr)", QR, FND);

    public static final Field informationRatio3y = create(519, "informationRatio3y", DECIMAL, "Information Ratio (3 Jahre)", QR, FND);

    public static final Field informationRatio5y = create(520, "informationRatio5y", DECIMAL, "Information Ratio (5 Jahre)", QR, FND);

    public static final Field informationRatio10y = create(521, "informationRatio10y", DECIMAL, "Information Ratio (10 Jahre)", QR, FND);

    public static final Field sterlingRatio1w = create(522, "sterlingRatio1w", DECIMAL, "Sterling Ratio (1 Woche)", QR, FND);

    public static final Field sterlingRatio1m = create(523, "sterlingRatio1m", DECIMAL, "Sterling Ratio (1 Monat)", QR, FND);

    public static final Field sterlingRatio3m = create(524, "sterlingRatio3m", DECIMAL, "Sterling Ratio (3 Monate)", QR, FND);

    public static final Field sterlingRatio6m = create(525, "sterlingRatio6m", DECIMAL, "Sterling Ratio (6 Monate)", QR, FND);

    public static final Field sterlingRatio1y = create(526, "sterlingRatio1y", DECIMAL, "Sterling Ratio (1 Jahr)", QR, FND);

    public static final Field sterlingRatio3y = create(527, "sterlingRatio3y", DECIMAL, "Sterling Ratio (3 Jahre)", QR, FND);

    public static final Field sterlingRatio5y = create(528, "sterlingRatio5y", DECIMAL, "Sterling Ratio (5 Jahre)", QR, FND);

    public static final Field sterlingRatio10y = create(529, "sterlingRatio10y", DECIMAL, "Sterling Ratio (10 Jahre)", QR, FND);

    public static final Field gatrixxIsknockout = createStatic(530, "gatrixxIsknockout", BOOLEAN, "Knockout? (gatrixx)", IN, CER);

    public static final Field qid = create(531, "qid", NUMBER, "qid", QS, BND, GNS, CER, FND, STK, WNT, FUT, OPT, IND, CUR, MER, ZNS);

    public static final Field dateFirstPrice = create(532, "dateFirstPrice", DATE, "Datum erster Kurs", QR, BND, CER, GNS, FND, STK, IND);

    public static final Field issuerCategory = createEnumStatic(533, "issuerCategory", STRING, "Kategorie des Emittenten", LOCALES_DE_EN_IT, IN, BND, GNS);

    public static final Field edgRatingDate = createStatic(535, "edgRatingDate", DATE, "Datum EDG-Rating", IN, CER, WNT);

    public static final Field edgScore1 = createStatic(536, "edgScore1", NUMBER, "EDG-Rating, Risikoklasse 1", IN, CER, WNT);

    public static final Field edgScore2 = createStatic(537, "edgScore2", NUMBER, "EDG-Rating, Risikoklasse 2", IN, CER, WNT);

    public static final Field edgScore3 = createStatic(538, "edgScore3", NUMBER, "EDG-Rating, Risikoklasse 3", IN, CER, WNT);

    public static final Field edgScore4 = createStatic(539, "edgScore4", NUMBER, "EDG-Rating, Risikoklasse 4", IN, CER, WNT);

    public static final Field edgScore5 = createStatic(540, "edgScore5", NUMBER, "EDG-Rating, Risikoklasse 5", IN, CER, WNT);

    public static final Field edgTopScore = createStatic(541, "edgTopScore", NUMBER, "EDG-Rating, Score der besten Risikoklasse", IN, CER, WNT);

    public static final Field edgTopClass = createStatic(542, "edgTopClass", NUMBER, "EDG-Rating, Beste Risikoklasse", IN, CER, WNT);

    public static final Field gatrixxParticipationFactor = createPercentStatic(543, "gatrixxParticipationFactor", DECIMAL, "Partizipationsfaktor (gatrixx)", IN, CER);

    public static final Field gatrixxKnockindate = createStatic(544, "gatrixxKnockindate", DATE, "Knockin-Date (gatrixx)", IN, CER);

    public static final Field fwwTer = createPercentStatic(545, "fwwTer", DECIMAL, "TER (FWW)", IN, FND);

    public static final Field gatrixxStartvalue = createStatic(546, "gatrixxStartvalue", DECIMAL, "Startmarke (gatrixx)", IN, CER);

    public static final Field gatrixxStopvalue = createStatic(547, "gatrixxStopvalue", DECIMAL, "Stopmarke (gatrixx)", IN, CER);

    public static final Field gatrixxRefundMaximum = createStatic(548, "gatrixxRefundMaximum", DECIMAL, "max. Auszahlung (gatrixx)", IN, CER);

    public static final Field probabilityofoutperformance3y = createPercent(550, "probabilityofoutperformance3y", DECIMAL, "Wahrscheinlichkeit der Outperformance (3 Jahre)", QR, FND);

    public static final Field positiveregression3y = create(551, "positiveregression3y", DECIMAL, "Positive Regression (3 Jahre)", QR, FND);

    public static final Field negativeregression3y = create(552, "negativeregression3y", DECIMAL, "Negative Regression (3 Jahre)", QR, FND);

    public static final Field bviperformancelastyear = createPercent(553, "bviperformancelastyear", DECIMAL, "BVI-Performance (letztes abgeschlossenes Jahr)", QR, FND);

    public static final Field bviperformanceyearbeforelastyear = createPercent(554, "bviperformanceyearbeforelastyear", DECIMAL, "BVI-Performance (vorletztes abgeschlossenes Jahr)", QR, FND);

    public static final Field performanceSinceFundIssueDate = createPercent(555, "performanceSinceFundIssueDate", DECIMAL, "(Benchmark-)Performance seit erster Kurs des Fonds", QR, FND);

    public static final Field wmDividendLastYear = createStatic(556, "wmDividendLastYear", DECIMAL, "Dividende (hochgerechnet, letztes Jahr nach WM)", IN, STK, GNS);

    public static final Field rsi7d = createPercent(557, "rsi7d", DECIMAL, "RSI (7 Tage)", QR, STK, IND);

    public static final Field rsi9d = createPercent(558, "rsi9d", DECIMAL, "RSI (9 Tage)", QR, STK, IND);

    public static final Field rsi14d = createPercent(559, "rsi14d", DECIMAL, "RSI (14 Tage)", QR, STK, IND);

    public static final Field rsi25d = createPercent(560, "rsi25d", DECIMAL, "RSI (25 Tage)", QR, STK, IND);

    public static final Field vwdbenlInvestmentFocus = createEnumStatic(561, "vwdbenlInvestmentFocus", STRING, "Anlageschwerpunkt (vwd BE/NL)", LOCALES_VWDBENL, IN, FND);

    public static final Field smfIssuerProductname = createStatic(562, "smfIssuerProductname", STRING, "Produktname des Emittenten (SMF)", IN, CER);

    public static final Field smfLeverageType = createEnumStatic(563, "smfLeverageType", STRING, "Typ (SMF)", IN, CER, WNT);

    public static final Field smfIsQuanto = createStatic(564, "smfIsQuanto", BOOLEAN, "Quanto? (SMF)", IN, CER);

    public static final Field smfIsAmerican = createEnumStatic(565, "smfIsAmerican", BOOLEAN, "american? (SMF)", IN, WNT);

    public static final Field smfExpires = createStatic(566, "smfExpires", DATE, "Verfallstag (SMF)", IN, CER, WNT);

    public static final Field smfParticipationrate = createStatic(567, "smfParticipationrate", DECIMAL, "Partizipationsrate (SMF)", IN, CER);

    public static final Field smfSubscriptionRatio = createStatic(568, "smfSubscriptionRatio", DECIMAL, "Bezugsverhältnis (SMF)", IN, CER, WNT);

    public static final Field smfCoupon = createStatic(569, "smfCoupon", DECIMAL, "Kupon (SMF)", IN, CER);

    public static final Field smfMultiassetName = createStatic(570, "smfMultiassetName", STRING, "Name des Underlyings für MultiAsset-Underlyings (SMF)", IN, CER);

    public static final Field vwdbenlBenchmarkQid = createStatic(571, "vwdbenlBenchmarkQid", NUMBER, "Benchmark-qid (vwd BE/NL)", IN, FND);

    public static final Field vwdbenlFundType = createEnumStatic(572, "vwdbenlFundType", STRING, "Fondstyp (vwd BE/NL)", LOCALES_VWDBENL, IN, FND);

    public static final Field vwdbenlIssuername = createEnumStatic(573, "vwdbenlIssuername", STRING, "Emittent (vwd BE/NL)", LOCALES_VWDBENL, IN, FND);

    public static final Field vwdbenlCountry = createEnumStatic(574, "vwdbenlCountry", STRING, "Land (vwd BE/NL)", LOCALES_VWDBENL, IN, FND);

    public static final Field vwdbenlDistStrategy = createEnumStatic(575, "vwdbenlDistStrategy", STRING, "Ausschüttungsart (vwd BE/NL)", LOCALES_VWDBENL, IN, FND);

    public static final Field vwdbenlFundVolume = createStatic(576, "vwdbenlFundVolume", DECIMAL, "Fondsvolumen (vwd BE/NL)", IN, FND);

    public static final Field vwdbenlIssueDate = createStatic(577, "vwdbenlIssueDate", DATE, "Emissionsdatum (vwd BE/NL)", IN, FND);

    public static final Field vwdbenlIssueSurcharge = createPercentStatic(578, "vwdbenlIssueSurcharge", DECIMAL, "Ausgabeaufschlag (vwd BE/NL)", IN, FND);

    public static final Field vwdbenlManagementfee = createPercentStatic(579, "vwdbenlManagementfee", DECIMAL, "Managementfee (vwd BE/NL)", IN, FND);

    public static final Field vwdbenlTer = createPercentStatic(580, "vwdbenlTer", DECIMAL, "TER (vwd BE/NL)", IN, FND);

    public static final Field vwdbenlBenchmarkName = createStatic(581, "vwdbenlBenchmarkName", STRING, "Name der Benchmark (vwd BE/NL)", IN, FND);

    public static final Field smfCertificateType = createEnumStatic(582, "smfCertificateType", STRING, "Derivattyp (SMF)", IN, CER);

    public static final Field msMarketAdmission = createStatic(583, "msMarketAdmission", ENUMSET, "Vetriebszulassung (Morningstar)", IN, FND);

    public static final Field smfStrike = createStatic(584, "smfStrike", DECIMAL, "Strike (SMF)", IN, WNT);

    public static final Field rsi90d = createPercent(585, "rsi90d", DECIMAL, "RSI (90 Tage)", QR, STK, IND);

    public static final Field rsi450d = createPercent(586, "rsi450d", DECIMAL, "RSI (450 Tage)", QR, STK, IND);

    public static final Field trCurrentPriceSalesRatio1Y = create(587, "trCurrentPriceSalesRatio1Y", DECIMAL, "Price/Sales Ratio (ThomsonReuters und aktueller Kurs, FY1)", QR, STK);

    public static final Field trCurrentPriceSalesRatio2Y = create(588, "trcurrentpricesalesratio2y", DECIMAL, "Price/Sales Ratio (ThomsonReuters und aktueller Kurs, FY2)", QR, STK);

    public static final Field trPriceEarningRatio1Y = createStatic(589, "trPriceEarningRatio1Y", DECIMAL, "EPS (ThomsonReuters, aktuelles Jahr)", IN, STK);

    public static final Field trPriceEarningRatio2Y = createStatic(590, "trPriceEarningRatio2Y", DECIMAL, "EPS (ThomsonReuters, nächstes Jahr)", IN, STK);

    public static final Field trCurrentPriceCashflowRatio1Y = create(591, "trcurrentpricecashflowratio1y", DECIMAL, "Price/Cashflow Ratio (ThomsonReuters und aktueller Kurs, FY1)", QR, STK);

    public static final Field trCurrentPriceCashflowRatio2Y = create(592, "trcurrentpricecashflowratio2y", DECIMAL, "Price/Cashflow Ratio (ThomsonReuters und aktueller Kurs, FY2)", QR, STK);

    public static final Field trCurrentPriceBookvalueRatio1Y = create(593, "trcurrentpricebookvalueratio1y", DECIMAL, "Price/Bookvalue Ratio (ThomsonReuters und aktueller Kurs, FY1)", QR, STK);

    public static final Field trCurrentPriceBookvalueRatio2Y = create(594, "trcurrentpricebookvalueratio2y", DECIMAL, "Price/Bookvalue Ratio (ThomsonReuters und aktueller Kurs, FY2)", QR, STK);

    public static final Field trEps1Y = createStatic(595, "trEps1Y", DECIMAL, "EPS (ThomsonReuters, in 1 Jahr)", IN, STK);

    public static final Field trEps2Y = createStatic(596, "trEps2Y", DECIMAL, "EPS (ThomsonReuters, in 2 Jahren)", IN, STK);

    public static final Field trDividend1Y = createStatic(597, "trDividend1Y", DECIMAL, "Dividende (ThomsonReuters, in 1 Jahr)", IN, STK);

    public static final Field trDividend2Y = createStatic(598, "trDividend2Y", DECIMAL, "Dividende (ThomsonReuters, in 2 Jahren)", IN, STK);

    public static final Field trDividendyield1Y = createPercentStatic(599, "trDividendyield1Y", DECIMAL, "Dividendenrendite (ThomsonReuters, in 1 Jahr)", IN, STK);

    public static final Field trDividendyield2Y = createPercentStatic(600, "trDividendyield2Y", DECIMAL, "Dividendenrendite (ThomsonReuters, in 2 Jahren)", IN, STK);

    public static final Field trSales1Y = createStatic(601, "trSales1Y", DECIMAL, "EPS (ThomsonReuters, aktuelles Jahr, in Mio)", IN, STK);

    public static final Field trSales2Y = createStatic(602, "trSales2Y", DECIMAL, "EPS (ThomsonReuters, nächstes Jahr, in Mio)", IN, STK);

    public static final Field trProfit1Y = createStatic(603, "trProfit1Y", DECIMAL, "Gewinn (ThomsonReuters, in 1 Jahr)", IN, STK);

    public static final Field trProfit2Y = createStatic(604, "trProfit2Y", DECIMAL, "Gewinn (ThomsonReuters, in 2 Jahren)", IN, STK);

    public static final Field trEbit1Y = createStatic(605, "trEbit1Y", DECIMAL, "EPS (ThomsonReuters, aktuelles Jahr)", IN, STK);

    public static final Field trEbit2Y = createStatic(606, "trEbit2Y", DECIMAL, "EPS (ThomsonReuters, nächstes Jahr)", IN, STK);

    public static final Field trEbitda1Y = createStatic(607, "trEbitda1Y", DECIMAL, "EPS (ThomsonReuters, aktuelles Jahr)", IN, STK);

    public static final Field trEbitda2Y = createStatic(608, "trEbitda2Y", DECIMAL, "EPS (ThomsonReuters, nächstes Jahr)", IN, STK);

    public static final Field trRecommendation = createStatic(609, "trRecommendation", DECIMAL, "TR-Rating", IN, STK);

    public static final Field trFiscalYear = createStatic(610, "trFiscalYear", NUMBER, "Fiscal Year (ThomsonReuters, Bezugsjahr)", IN, STK);

    public static final Field trBookValue1Y = create(611, "trBookValue1Y", DECIMAL, "EPS (ThomsonReuters, aktuelles Jahr)", IN, STK);

    public static final Field trBookValue2Y = create(612, "trBookValue2Y", DECIMAL, "EPS (ThomsonReuters, aktuelles Jahr)", IN, STK);

    public static final Field trCashflow1Y = createStatic(613, "trCashflow1Y", DECIMAL, "EPS (ThomsonReuters, aktuelles Jahr)", IN, STK);

    public static final Field trCashflow2Y = createStatic(614, "trCashflow2Y", DECIMAL, "EPS (ThomsonReuters, aktuelles Jahr)", IN, STK);

    public static final Field trCurrency = createEnumStatic(615, "trCurrency", STRING, "Währung (ThomsonReuters)", IN, STK);

    public static final Field trCurrentPriceEarningRatio1Y = create(616, "trcurrentpriceearningratio1y", DECIMAL, "Price/Earning Ratio (ThomsonReuters und aktueller Kurs, FY1)", QR, STK);

    public static final Field trCurrentPriceEarningRatio2Y = create(617, "trcurrentpriceearningratio2y", DECIMAL, "Price/Earning Ratio (ThomsonReuters und aktueller Kurs, FY2)", QR, STK);

    public static final Field optionCategory = createEnumStatic(618, "optionCategory", STRING, "Kategorie der Option (daily, weekly, default)", IN, OPT);

    public static final Field bviperformance2_1y = createPercent(619, "bviperformance2_1y", DECIMAL, "BVI-Perf. (-2 - -1 Jahr)", QR, FND);

    public static final Field bviperformance3_2y = createPercent(620, "bviperformance3_2y", DECIMAL, "BVI-Perf. (-3 - -2 Jahr)", QR, FND);

    public static final Field bviperformance4_3y = createPercent(621, "bviperformance4_3y", DECIMAL, "BVI-Perf. (-4 - -3 Jahr)", QR, FND);

    public static final Field bviperformance5_4y = createPercent(622, "bviperformance5_4y", DECIMAL, "BVI-Perf. (-5 - -4 Jahr)", QR, FND);

    public static final Field type = createEnumStatic(623, "type", STRING, "Typ (allgemein)", LOCALES_DE_EN, IN, CER);

    public static final Field typeKey = createEnumStatic(624, "typeKey", STRING, "Typ-Schlüssel (allgemein)", IN, CER);

    public static final Field subtype = createEnumStatic(625, "subtype", STRING, "Sub-Typ (allgemein)", LOCALES_DE_EN_IT, IN, CER, FND);

    public static final Field typeDZ = createEnumStatic(626, "typeDZ", STRING, "Typ (DZ BANK)", IN, CER);

    public static final Field typeKeyDZ = createEnumStatic(627, "typeKeyDZ", STRING, "Typ-Schlüssel (DZ BANK)", IN, CER);

    public static final Field subtypeDZ = createEnumStatic(628, "subtypeDZ", STRING, "Sub-Typ (DZ BANK)", IN, CER);

    public static final Field typeWGZ = createEnumStatic(629, "typeWGZ", STRING, "Typ (WGZ BANK)", IN, CER);

    public static final Field typeKeyWGZ = createEnumStatic(630, "typeKeyWGZ", STRING, "Typ-Schlüssel (WGZ BANK)", IN, CER);

    public static final Field subtypeWGZ = createEnumStatic(631, "subtypeWGZ", STRING, "Sub-Typ (WGZ BANK)", IN, CER);

    public static final Field gatrixxBonusbufferRelative = createPercentStatic(632, "gatrixxBonusbufferRelative", DECIMAL, "Bonuspuffer (relativ)", IN, CER);

    public static final Field thetaRelative = create(633, "thetaRelative", DECIMAL, "Theta in %", QR, OPT);

    public static final Field theta1w = create(634, "theta1w", DECIMAL, "Theta (1 Woche)", QR, OPT);

    public static final Field theta1wRelative = create(635, "theta1wRelative", DECIMAL, "Theta (1 Woche, in %)", QR, OPT);

    public static final Field theta1m = create(636, "theta1m", DECIMAL, "Theta (1 Monat)", QR, OPT);

    public static final Field theta1mRelative = create(637, "theta1mRelative", DECIMAL, "Theta (1 Monat, in %)", QR, OPT);

    public static final Field vwditInvestmentFocus = createEnumStatic(638, "vwditInvestmentFocus", STRING, "Anlageschwerpunkt (vwd IT)", LOCALES_IT, IN, FND);

    public static final Field vwditFundType = createEnumStatic(639, "vwditFundType", STRING, "Fondstyp (vwd IT)", LOCALES_IT, IN, FND);

    public static final Field vwditIssuername = createEnumStatic(640, "vwditIssuername", STRING, "Emittent (vwd IT)", LOCALES_IT, IN, FND);

    public static final Field vwditBenchmarkName = createStatic(641, "vwditBenchmarkName", STRING, "Name der Benchmark (vwd IT)", IN, FND);

    public static final Field vwditIssueDate = createStatic(642, "vwditIssueDate", DATE, "Emissionsdatum (vwd IT)", IN, FND);

    public static final Field vwditTer = createPercentStatic(643, "vwditTer", DECIMAL, "TER (vwd IT)", IN, FND);

    public static final Field vwditCfsRating = createEnumStatic(644, "vwditCfsRating", STRING, "CFS Rating (vwd IT)", IN, FND);

    public static final Field wmSector = createEnumStatic(645, "wmSector", STRING, "Branche (WM)", LOCALES_DE_EN, IN, STK, GNS, BND);

    public static final Field wmIssueDate = createStatic(646, "wmIssueDate", DATE, "Emissionsdatum (WM)", IN, BND, GNS);

    public static final Field vwdStaticDataAvailable = createStatic(647, "vwdStaticDataAvailable", BOOLEAN, "Static Data Available?", IN, CER, WNT);

    public static final Field maSimple38 = create(648, "maSimple38", DECIMAL, "Simple Moving Average 38", QR, BND, CUR, FND, FUT, GNS, IND, MER, OPT, STK, ZNS);

    public static final Field maSimple90 = create(649, "maSimple90", DECIMAL, "Simple Moving Average 90", QR, BND, CUR, FND, FUT, GNS, IND, MER, OPT, STK, ZNS);

    public static final Field maSimple200 = create(650, "maSimple200", DECIMAL, "Simple Moving Average 200", QR, BND, CUR, FND, FUT, GNS, IND, MER, OPT, STK, ZNS);

    public static final Field wmNumberOfIssuedEquities = createStatic(651, "wmNumberOfIssuedEquities", DECIMAL, "Anzahl nennwertloser Stückaktien (WM)", IN, STK, GNS);

    public static final Field externalReferenceTimestamp = create(652, "externalReferenceTimestamp", TIMESTAMP, "Datum/Zeit Kennzahlenzulieferung", QR, CER);

    public static final Field dzPib = create(653, "dzPib", BOOLEAN, "PIB(DZ)", IN, BND, CER);

    public static final Field marketCapitalizationPreviousDay = create(654, "marketCapitalizationPreviousDay", NUMBER, "Marktkapitalisierung", QR, STK, GNS);

    public static final Field dzIsLeverageProduct = createStatic(655, "dzIsLeverageProduct", BOOLEAN, "Hebelprodukt nach DZ-Klassifikation?", IN, CER);

    public static final Field subtypeKey = createEnumStatic(656, "subtypeKey", STRING, "Subtyp-Schlüssel (allgemein)", IN, CER);

    public static final Field subtypeKeyDZ = createEnumStatic(657, "subtypeKeyDZ", STRING, "Subtyp-Schlüssel (DZ BANK)", IN, CER);

    public static final Field subtypeKeyWGZ = createEnumStatic(658, "subtypeKeyWGZ", STRING, "Subtyp-Schlüssel (WGZ BANK)", IN, CER);

    public static final Field gatrixxIsEndless = createStatic(659, "gatrixxIsEndless", BOOLEAN, "Endlos?", IN, CER);

    public static final Field ratingMoodysLongTermDate = createStatic(660, "ratingMoodysLongTermDate", DATE, "Moodys LT Date", IN, BND);

    public static final Field ratingMoodysLongTermAction = createEnumStatic(661, "ratingMoodysLongTermAction", STRING, "Moodys LT Action", IN, BND);

    public static final Field ratingMoodysShortTerm = createEnumStatic(662, "ratingMoodysShortTerm", STRING, "Moodys ST", IN, BND);

    public static final Field ratingMoodysShortTermDate = createStatic(663, "ratingMoodysShortTermDate", DATE, "Moodys ST Date", IN, BND);

    public static final Field ratingMoodysShortTermAction = createEnumStatic(664, "ratingMoodysShortTermAction", STRING, "Moodys ST Action", IN, BND);

    public static final Field ratingFitchShortTermDate = createStatic(665, "ratingFitchShortTermDate", DATE, "Fitch ST Date", IN, BND);

    public static final Field ratingFitchShortTermAction = createEnumStatic(666, "ratingFitchShortTermAction", STRING, "Fitch ST Action", IN, BND);

    public static final Field ratingFitchLongTermDate = createStatic(667, "ratingFitchLongTermDate", DATE, "Fitch LT Date", IN, BND);

    public static final Field ratingFitchLongTermAction = createEnumStatic(668, "ratingFitchLongTermAction", STRING, "Fitch LT Action", IN, BND);

    public static final Field reference1w = create(669, "reference1w", DATE, "Bezugsdatum (1 Woche)", QR, FND);

    public static final Field reference1m = create(670, "reference1m", DATE, "Bezugsdatum (1 Monat)", QR, FND);

    public static final Field reference3m = create(671, "reference3m", DATE, "Bezugsdatum (3 Monate)", QR, FND);

    public static final Field reference6m = create(672, "reference6m", DATE, "Bezugsdatum (6 Monate)", QR, FND);

    public static final Field reference1y = create(673, "reference1y", DATE, "Bezugsdatum (1 Jahr)", QR, FND);

    public static final Field reference3y = create(674, "reference3y", DATE, "Bezugsdatum (3 Jahre)", QR, FND);

    public static final Field reference5y = create(675, "reference5y", DATE, "Bezugsdatum (5 Jahre)", QR, FND);

    public static final Field reference10y = create(676, "reference10y", DATE, "Bezugsdatum (10 Jahre)", QR, FND);

    public static final Field referenceAlltime = create(677, "referenceAlltime", DATE, "Bezugsdatum (Gesamt)", QR, FND);

    public static final Field ssatFundType = createEnumStatic(678, "ssatFundType", STRING, "Fondstyp (SSAT)", LOCALES_DE_EN, IN, FND);

    public static final Field ssatIssuername = createEnumStatic(679, "ssatIssuername", STRING, "Emittent (SSAT)", LOCALES_DE_EN, IN, FND);

    public static final Field ssatInvestmentFocus = createEnumStatic(680, "ssatInvestmentFocus", STRING, "Anlageschwerpunkt (SSAT)", LOCALES_DE_EN, IN, FND);

    public static final Field ssatCountry = createEnumStatic(681, "ssatCountry", STRING, "Land (SSAT)", LOCALES_DE_EN, IN, FND);

    public static final Field ssatDistributionStrategy = createEnumStatic(682, "ssatDistributionStrategy", STRING, "Ausschüttungsart (SSAT)", LOCALES_DE_EN, IN, FND);

    public static final Field ssatFundVolume = createStatic(683, "ssatFundVolume", DECIMAL, "Fondsvolumen (SSAT)", IN, FND);

    public static final Field ssatIssueDate = createStatic(684, "ssatIssueDate", DATE, "Emissionsdatum (SSAT)", IN, FND);

    public static final Field ssatIssueSurcharge = createPercentStatic(685, "ssatIssueSurcharge", DECIMAL, "Ausgabeaufschlag (SSAT)", IN, FND);

    public static final Field ssatManagementfee = createPercentStatic(686, "ssatManagementfee", DECIMAL, "Managementfee (SSAT)", IN, FND);

    public static final Field ssatAccountfee = createPercentStatic(687, "ssatAccountfee", DECIMAL, "Depotbankgebühr (SSAT)", IN, FND);

    public static final Field ssatTer = createPercentStatic(688, "ssatTer", DECIMAL, "TER (SSAT)", IN, FND);

    public static final Field ssatBenchmarkName = createStatic(689, "ssatBenchmarkName", STRING, "Name der Benchmark (SSAT)", IN, FND);

    public static final Field ssatBenchmarkQid = createStatic(690, "ssatBenchmarkQid", NUMBER, "Benchmark-qid (SSAT)", IN, FND);

    public static final Field ssatMarketAdmission = createStatic(691, "ssatMarketAdmission", ENUMSET, "Vetriebszulassung (SSAT)", IN, FND);

    public static final Field turnoverDay = create(692, "turnoverDay", DECIMAL, "Umsatz gesamt in Währung", QR, STK, BND, GNS);

    public static final Field msOngoingCharge = createStatic(693, "msOngoingCharge", DECIMAL, "Ongoing Charge", IN, FND);

    public static final Field msOngoingChargeDate = createStatic(694, "msOngoingChargeDate", DATE, "Ongoing Charge Date", IN, FND);

    public static final Field tickSize = createStatic(695, "tickSize", DECIMAL, "Tick Size", IN, FUT);

    public static final Field tickValue = createStatic(696, "tickValue", DECIMAL, "Tick Value", IN, FUT);

    public static final Field tickCurrency = createStatic(697, "tickCurrency", STRING, "Tick Currency", IN, FUT);

    public static final Field contractValue = createStatic(698, "contractValue", DECIMAL, "Contract Value", IN, FUT, OPT);

    public static final Field contractValueCalculated = createStatic(699, "contractValueCalculated", DECIMAL, "Contract Value Calculated", IN, FUT, OPT);

    public static final Field contractCurrency = createStatic(700, "contractCurrency", STRING, "Contract Currency", IN, FUT);

    public static final Field contractSize = createStatic(701, "contractSize", DECIMAL, "Contract Size", IN, OPT);

    public static final Field generationNumber = createStatic(702, "generationNumber", NUMBER, "Generation Number", IN, OPT);

    public static final Field versionNumber = createStatic(703, "versionNumber", NUMBER, "Version Number", IN, OPT);  // this is a string in mdp!

    public static final Field tradingMonth = createStatic(704, "tradingMonth", DATE, "Handelsmonat", IN, OPT);

    public static final Field ratingSnPShortTerm = createEnumStatic(705, "ratingSnPShortTerm", STRING, "S&P ST", IN, BND);

    public static final Field ratingSnPShortTermDate = createStatic(706, "ratingSnPShortTermDate", DATE, "S&P ST Date", IN, BND);

    public static final Field ratingSnPShortTermAction = createEnumStatic(707, "ratingSnPShortTermAction", STRING, "S&P ST Action", IN, BND);

    public static final Field ratingSnPLongTerm = createEnumStatic(708, "ratingSnPLongTerm", STRING, "S&P LT", IN, BND);

    public static final Field ratingSnPLongTermDate = createStatic(709, "ratingSnPLongTermDate", DATE, "S&P LT Date", IN, BND);

    public static final Field ratingSnPLongTermAction = createEnumStatic(710, "ratingSnPLongTermAction", STRING, "S&P LT Action", IN, BND);

    public static final Field ratingSnPLongTermRegulatoryId = createEnumStatic(711, "ratingSnPLongTermRegulatoryId", STRING, "S&P LT Regulator ID", IN, BND);

    public static final Field ratingSnPLongTermQualifier = createEnumStatic(712, "ratingSnPLongTermQualifier", STRING, "S&P LT Qualifier", IN, BND);

    public static final Field underlyingProductIid = create(713, "underlyingProductIid", NUMBER, "iid (Underlying)", IN, FUT, OPT);

    public static final Field bisKey = create(714, "bisKey", STRING, "bisKey-Schlüssel", QS, FUT, OPT);

    public static final Field znsCategory = create(715, "znsCategory", STRING, "ZNS Kategorie", IN, ZNS);

    public static final Field maturity = createEnumStatic(716, "maturity", STRING, "Laufzeit", IN, ZNS);

    public static final Field debtRanking = createEnumStatic(717, "debtRanking", STRING, "Debt Ranking", IN, ZNS);

    public static final Field issuerType = createEnumStatic(718, "issuerType", STRING, "Corporate oder Government", IN, ZNS);

    public static final Field restructuringRule = createEnumStatic(719, "restructuringRule", STRING, "Restructuring Rules", IN, ZNS);

    public static final Field source = createEnumStatic(720, "source", STRING, "Source", IN, ZNS);

    public static final Field performanceToBenchmarkCurrentYear = createPercent(721, "performancetobenchmarkcurrentyear", DECIMAL, "rel. Performance zu Benchmark aktuelles Jahr", QR, BND, GNS, STK, FND);

    public static final Field correlationCurrentYear = create(722, "correlationcurrentyear", DECIMAL, "Korrelation aktuelles Jahr", QR, STK, BND, GNS, FND);

    public static final Field vwdbenlSrriValue = createEnumStatic(723, "vwdbenlsrrivalue", NUMBER, "SRRI (vwd BE/NL)", IN, FND);

    public static final Field vwdbenlSrriValueDate = create(724, "vwdbenlsrrivaluedate", DATE, "SRRI Date (vwd BE/NL)", IN, FND);

    public static final Field vwdbenlDiamondRating = createEnumStatic(725, "vwdbenldiamondrating", NUMBER, "Diamond Rating (vwd BE/NL)", IN, FND);

    public static final Field vwdbenlDiamondRatingDate = create(726, "vwdbenldiamondratingdate", DATE, "Diamond Rating Date (vwd BE/NL)", IN, FND);

    // definition as enum is OK since für OPT the symbol is non-unique; used to differentiate different
    // options for the same underlying (e.g., FUO.XEQD.43_5.3L and FUR.XEQD.44.3L)
    public static final Field vwdsymbol = createEnum(727, "vwdsymbol", STRING, "Symbolanteil des vwd-Schlüssels", QS, OPT);

    public static final Field mdpsDateBarrierReached = create(728, "mdpsDateBarrierReached", TIMESTAMP, "Barriereverletzung", QR, CER);

    public static final Field wmIssuerName = createEnumStatic(729, "wmIssuerName", STRING, "WM Issuer Identifier", IN, BND, GNS);

    public static final Field fidaFundType = createEnumStatic(730, "fidaFundType", STRING, "Fondstyp (Fida)", LOCALES_IT, IN, FND);

    public static final Field fidaIssuername = createEnumStatic(731, "fidaIssuername", STRING, "Emittent (Fida)", LOCALES_IT, IN, FND);

    public static final Field fidaInvestmentFocus = createEnumStatic(732, "fidaInvestmentFocus", STRING, "Anlageschwerpunkt (Fida)", LOCALES_IT, IN, FND);

    public static final Field fidaIssueDate = createStatic(733, "fidaIssueDate", DATE, "Emissionsdatum (Fida)", IN, FND);

    public static final Field fidaTer = createPercentStatic(734, "fidaTer", DECIMAL, "TER (Fida)", IN, FND);

    public static final Field fidaBenchmarkName = createStatic(735, "fidaBenchmarkName", STRING, "Name der Benchmark (Fida)", IN, FND);

    public static final Field fidaCountry = createEnumStatic(736, "fidaCountry", STRING, "Land (FIDA)", LOCALES_IT, IN, FND);

    public static final Field fidaDistributionStrategy = createEnumStatic(737, "fidaDistributionStrategy", STRING, "Ausschüttungsart (FIDA)", IN, FND);

    public static final Field fidaFundVolume = createStatic(738, "fidaFundVolume", DECIMAL, "Fondsvolumen (FIDA)", IN, FND);

    public static final Field fidaIssueSurcharge = createPercentStatic(739, "fidaIssueSurcharge", DECIMAL, "Ausgabeaufschlag (FIDA)", IN, FND);

    public static final Field fidaManagementfee = createPercentStatic(740, "fidaManagementfee", DECIMAL, "Managementfee (FIDA)", IN, FND);

    // XXX start here for further FIDA ratio fields
    public static final Field fidaRating = createEnumStatic(746, "fidaRating", NUMBER, "Fund Rating (Fida)", IN, FND);

    public static final Field rsi130d = createPercent(747, "rsi130d", DECIMAL, "RSI (130 Tage)", QR, STK, IND);

    public static final Field marketCapitalizationUSD = create(748, "marketcapitalizationUSD", NUMBER, "Marktkapitalisierung (USD)", QR, STK, GNS);

    public static final Field marketCapitalizationEUR = create(749, "marketcapitalizationEUR", NUMBER, "Marktkapitalisierung (EUR)", QR, STK, GNS);

    public static final Field sedexIssuerName = createEnumStatic(750, "sedexIssuerName", STRING, "Emittent (SEDEX)", IN, CER, WNT);

    public static final Field sedexStrike = createStatic(751, "sedexStrike", DECIMAL, "Strike (SEDEX)", IN, CER, WNT);

    public static final Field sedexIssueDate = createStatic(752, "sedexIssueDate", DATE, "Auflagedatum (SEDEX)", IN, CER, WNT);

    public static final Field sedexExpires = createStatic(753, "sedexExpires", DATE, "Verfallstag (SEDEX)", IN, CER, WNT);

    public static final Field typeSEDEX = createEnumStatic(754, "typeSEDEX", STRING, "Typ (SEDEX)", IN, CER);

    public static final Field typeKeySEDEX = createEnumStatic(755, "typeKeySEDEX", STRING, "Typ-Schlüssel (SEDEX)", IN, CER);

    public static final Field underlyingEurexTicker = createEnum(756, "underlyingEurexTicker", STRING, "Eurex-Ticker (Underlying)", IN, CER, WNT, FUT, OPT);

    public static final Field fidaPermissionType = createEnumStatic(757, "fidaPermissionType", STRING, "Permission Type (Fida)", IN, FND);

    public static final Field wmSmallestTransferableUnit = createStatic(758, "wmSmallestTransferableUnit", DECIMAL, "Stückelung", IN, BND);

    public static final Field msBroadassetclass = createEnumStatic(759, "msBroadassetclass", STRING, "Fondstyp (Morningstar)", LOCALES_DE_EN, IN, FND);

    public static final Field volatilityAlltime = createPercent(760, "volatilityAlltime", DECIMAL, "Volatilität (seit Emission)", QR, BND, CER, CUR, FND, FUT, GNS, IND, MER, OPT, STK, WNT, ZNS);

    public static final Field betaCurrentYear = create(761, "betaCurrentYear", DECIMAL, "Beta (lfd. Jahr)", QR, STK, BND, GNS, FND);

    public static final Field betaAlltime = create(762, "betaAlltime", DECIMAL, "Beta (seit Emission)", QR, STK, BND, GNS, FND);

    public static final Field sharperatioCurrentYear = create(763, "sharperatioCurrentYear", DECIMAL, "Sharpe Ratio (lfd. Jahr)", QR, CER, FND, IND);

    public static final Field sharperatioAlltime = create(764, "sharperatioAlltime", DECIMAL, "Sharpe Ratio (seit Emission)", QR, CER, FND, IND);

    public static final Field alphaCurrentYear = createPercent(765, "alphaCurrentYear", DECIMAL, "Alpha (lfd. Jahr)", QR, STK, BND, GNS, FND);

    public static final Field alphaAlltime = createPercent(766, "alphaAlltime", DECIMAL, "Alpha (seit Emission)", QR, STK, BND, GNS, FND);

    public static final Field treynorCurrentYear = create(767, "treynorCurrentYear", DECIMAL, "Treynor (lfd. Jahr)", QR, FND);

    public static final Field treynorAlltime = create(768, "treynorAlltime", DECIMAL, "Treynor (seit Emission)", QR, FND);

    public static final Field wmNotActive = create(769, "wmNotActive", BOOLEAN, "'nicht aktiv' wenn true, false bedeutet aktiv oder -keine Information- (WM)", IN, BND, GNS, CER, FND, STK, WNT, FUT, OPT, IND, CUR, MER, ZNS);

    public static final Field maximumLoss5y = createPercent(770, "maximumLoss5y", DECIMAL, "maximaler Verlust (5 Jahre)", QR, FND, STK, IND);

    public static final Field maximumLoss10y = createPercent(771, "maximumLoss10y", DECIMAL, "maximaler Verlust (10 Jahre)", QR, FND, STK, IND);

    public static final Field maximumLossAlltime = createPercent(772, "maximumLossAlltime", DECIMAL, "maximaler Verlust (seit Emission)", QR, FND, STK, IND);

    public static final Field vwdFundType = createEnumStatic(773, "vwdFundType", STRING, "Fondstyp (vwd)", LOCALES_DE_EN_FR_NL, IN, FND);

    public static final Field vwdIssuername = createEnumStatic(774, "vwdIssuername", STRING, "Emittent (vwd)", LOCALES_DE_EN_FR_NL, IN, FND);

    public static final Field vwdCountry = createEnumStatic(775, "vwdCountry", STRING, "Land (vwd)", LOCALES_DE_EN_FR_NL, IN, FND);

    public static final Field vwdDistributionStrategy = createEnumStatic(776, "vwdDistributionStrategy", STRING, "Ausschüttungsart (vwd)", LOCALES_DE_EN, IN, FND);

    public static final Field vwdFundVolume = createStatic(777, "vwdFundVolume", DECIMAL, "Fondsvolumen (vwd)", IN, FND);

    public static final Field vwdIssueDate = createStatic(778, "vwdIssueDate", DATE, "Emissionsdatum (vwd)", IN, FND);

    public static final Field vwdIssueSurcharge = createPercentStatic(779, "vwdIssueSurcharge", DECIMAL, "Ausgabeaufschlag (vwd)", IN, FND);

    public static final Field vwdManagementfee = createPercentStatic(780, "vwdManagementFee", DECIMAL, "Managementfee (vwd)", IN, FND);

    public static final Field vwdTer = createPercentStatic(781, "vwdTer", DECIMAL, "TER (vwd)", IN, FND);

    public static final Field vwdBenchmarkName = createStatic(782, "vwdBenchmarkName", STRING, "Name der Benchmark (vwd)", LOCALES_DE_EN_FR_NL, IN, FND);

    public static final Field vwdOngoingCharge = createStatic(783, "vwdOnGoingCharge", DECIMAL, "Ongoing Charge", IN, FND);

    public static final Field vwdOngoingChargeDate = createStatic(784, "vwdOngoingChargeDate", DATE, "Ongoing Charge Date", IN, FND);

    public static final Field vwdMarketAdmission = createStatic(785, "vwdMarketAdmission", ENUMSET, "Vetriebszulassung (vwd)", IN, FND);

    public static final Field vwdInvestmentFocus = createEnumStatic(787, "vwdInvestmentFocus", STRING, "Anlageschwerpunkt (vwd)", LOCALES_DE_EN_FR_NL, IN, FND);

    public static final Field vwdAccountfee = createPercentStatic(788, "vwdAccountfee", DECIMAL, "Depotbankgebühr (vwd)", IN, FND);

    public static final Field wmBondRank = createEnumStatic(789, "wmBondRank", STRING, "Bond Rank", IN, BND);

    public static final Field lmeMetalCode = createEnumStatic(790, "lmeMetalCode", STRING, "LME metal code", IN, MER);

    public static final Field lmeExpirationDate = create(791, "lmeExpirationDate", DATE, "LME expiration date", QR, MER);

    public static final Field wmInvestmentAssetPoolClass = createEnumStatic(792, "wmInvestmentAssetPoolClass", STRING, "OGAW/AIF (WM)", IN, FND);

    public static final Field isSpecialDismissal = createEnumStatic(793, "isSpecialDismissal", BOOLEAN, "Sonderkündigungsrecht", IN, BND);

    public static final Field vwdVrIssuer = create(794, "vwdVrIssuer", BOOLEAN, "DZ,WGZ,UNION (vwd)", IN, FND);

    public static final Field morningstarsDZBANK = createEnumStatic(795, "morningstarsDZBANK", NUMBER, "Morningstar-Rating (DZ BANK, nur für Verbundfonds)", IN, FND);

    public static final Field gicsSector = createEnumStatic(796, "gicsSector", STRING, "GICS Sektor", LOCALES_DE_EN_FR_IT, IN, STK);

    public static final Field gicsIndustryGroup = createEnumStatic(797, "gicsIndustryGroup", STRING, "GICS Industriegruppe", LOCALES_DE_EN_FR_IT, IN, STK);

    public static final Field gicsIndustry = createEnumStatic(798, "gicsIndustry", STRING, "GICS Industriezweig", LOCALES_DE_EN_FR_IT, IN, STK);

    public static final Field gicsSubIndustry = createEnumStatic(799, "gicsSubIndustry", STRING, "GICS Branche", LOCALES_DE_EN_FR_IT, IN, STK);

    public static final Field isLMEComposite = createEnum(800, "lmeComposite", BOOLEAN, "LMEComposite", QR, MER);

    public static final Field ratingSnPLocalLongTerm = createEnumStatic(801, "ratingSnPLocalLongTerm", STRING, "S&P Local LT", IN, BND);

    public static final Field ratingSnPLocalLongTermDate = createStatic(802, "ratingSnPLocalLongTermDate", DATE, "S&P Local LT Date", IN, BND);

    public static final Field ratingSnPLocalShortTerm = createEnumStatic(803, "ratingSnPLocalShortTerm", STRING, "S&P Local ST", IN, BND);

    public static final Field ratingSnPLocalShortTermDate = createStatic(804, "ratingSnPLocalShortTermDate", DATE, "S&P Local ST Date", IN, BND);

    public static final Field ratingMoodysLongTermSource = createEnumStatic(805, "ratingMoodysLongTermSource", STRING, "Moodys LT Source", IN, BND);

    public static final Field ratingMoodysShortTermSource = createEnumStatic(806, "ratingMoodysShortTermSource", STRING, "Moodys ST Source", IN, BND);

    public static final Field gicsSectorKey = createEnumStatic(807, "gicsSectorKey", STRING, "GICS Sektor-Schlüssel", IN, STK);

    public static final Field gicsIndustryGroupKey = createEnumStatic(808, "gicsIndustryGroupKey", STRING, "GICS Industriegruppe-Schlüssel", IN, STK);

    public static final Field gicsIndustryKey = createEnumStatic(809, "gicsIndustryKey", STRING, "GICS Industriezweig-Schlüssel", IN, STK);

    public static final Field gicsSubIndustryKey = createEnumStatic(810, "gicsSubIndustryKey", STRING, "GICS Branche-Schlüssel", IN, STK);

    public static final Field merInstrumentenTyp = createEnum(811, "merInstrumentenTyp", STRING, "merInstrumentenTyp", QR, MER);

    public static final Field merHandelsmonat = createEnum(812, "merHandelsmonat", STRING, "merHandelsmonat", QR, MER);

    public static final Field lei = create(813, "lei", STRING, "legal entity identifier", IN, STK, BND);

    public static final Field volatility = createPercent(814, "volatility", DECIMAL, "volatility", QR, ZNS);

    public static final Field rating = createEnum(815, "rating", STRING, "rating", QR, ZNS);

    public static final Field standard  = createEnum(816, "standard", STRING, "standard", QR, ZNS);

    public static final Field vwdSrriValue = createEnumStatic(817, "vwdsrrivalue", NUMBER, "SRRI (vwd)", IN, FND);

    public static final Field vwdSrriValueDate = create(818, "vwdsrrivaluedate", DATE, "SRRI Date (vwd)", IN, FND);

    public static final Field isFlex = createStatic(819, "isFlex", BOOLEAN, "flex?", IN, OPT);

    public static final Field vwdDiamondRating = createEnumStatic(820, "vwddiamondrating", NUMBER, "Diamond Rating (vwd)", IN, FND);

    public static final Field vwdDiamondRatingDate = create(821, "vwddiamondratingdate", DATE, "Diamond Rating Date (vwd)", IN, FND);

    public static final Field fidaRatingROnly = createEnumStatic(822, "fidaRatingROnly", NUMBER, "Fund Rating (Fida) (R permission)", IN, FND);

    public static final Field fidaRatingIOnly = createEnumStatic(823, "fidaRatingIOnly", NUMBER, "Fund Rating (Fida) (I permission)", IN, FND);

    public static final Field factsetRecommendation = createStatic(824, "factsetRecommendation", DECIMAL, "Rating (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetCurrentPriceSalesRatio3Y = create(825, "factsetcurrentpricesalesratio3y", DECIMAL, "Price/Sales Ratio (Factset und aktueller Kurs, FY3)", QR, STK);

    public static final Field factsetCurrentPriceSalesRatio4Y = create(826, "factsetcurrentpricesalesratio4y", DECIMAL, "Price/Sales Ratio (Factset und aktueller Kurs, FY4)", QR, STK);

    public static final Field factsetPriceEarningRatio3Y = createStatic(827, "factsetPriceEarningRatio3Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetPriceEarningRatio4Y = createStatic(828, "factsetPriceEarningRatio4Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetCurrentPriceCashflowRatio3Y = create(829, "factsetcurrentpricecashflowratio3y", DECIMAL, "Price/Cashflow Ratio (Factset und aktueller Kurs, FY3)", QR, STK);

    public static final Field factsetCurrentPriceCashflowRatio4Y = create(830, "factsetcurrentpricecashflowratio4y", DECIMAL, "Price/Cashflow Ratio (Factset und aktueller Kurs, FY4)", QR, STK);

    public static final Field factsetDividendyield3Y = createPercentStatic(831, "factsetDividendyield3Y", DECIMAL, "Dividendenrendite (Factset, in 3 Jahren)", IN, STK);

    public static final Field factsetDividendyield4Y = createPercentStatic(832, "factsetDividendyield4Y", DECIMAL, "Dividendenrendite (Factset, in 4 Jahren)", IN, STK);

    public static final Field factsetSales3Y = createStatic(833, "factsetSales3Y", DECIMAL, "EPS (Factset, aktuelles Jahr, in Mio.)", IN, STK);

    public static final Field factsetSales4Y = createStatic(834, "factsetSales4Y", DECIMAL, "EPS (Factset, aktuelles Jahr, in Mio.)", IN, STK);

    public static final Field factsetProfit3Y = createStatic(835, "factsetProfit3Y", DECIMAL, "Gewinn (Factset, in 3 Jahren)", IN, STK);

    public static final Field factsetProfit4Y = createStatic(836, "factsetProfit4Y", DECIMAL, "Gewinn (Factset, in 4 Jahren)", IN, STK);

    public static final Field factsetEbit3Y = createStatic(837, "factsetEbit3Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetEbit4Y = createStatic(838, "factsetEbit4Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetEbitda3Y = createStatic(839, "factsetEbitda3Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field factsetEbitda4Y = createStatic(840, "factsetEbitda4Y", DECIMAL, "EPS (Factset, aktuelles Jahr)", IN, STK);

    public static final Field minAmountOfTransferableUnit = createStatic(841, "minAmountOfTransferableUnit", DECIMAL, "minAmountOfTransferableUnit", IN, BND);

    public static final Field ratingSnPLongTermSource = createStatic(842, "ratingSnPLongTermSource", STRING, "S&P LT Source"/*, "ADF_LT_Rating_Source_SuP", 2984 */, IN, BND);

    public static final Field ratingSnPShortTermSource = createStatic(843, "ratingSnPShortTermSource", STRING, "S&P ST Source"/*, "ADF_ST_Rating_Source_SuP", 2985 */, IN, BND);

    public static final Field ratingSnPLocalLongTermSource = createStatic(844, "ratingSnPLocalLongTermSource", STRING, "S&P Local LT Source"/*, "ADF_LOC_LT_Rating_Source_SuP", 2986 */, IN, BND);

    public static final Field ratingSnPLocalShortTermSource = createStatic(845, "ratingSnPLocalShortTermSource", STRING, "S&P Local ST Source"/*, "ADF_LOC_ST_Rating_Source_SuP", 2987 */, IN, BND);

    public static final Field ratingSnPLocalLongTermAction = createStatic(846, "ratingSnPLocalLongTermAction", STRING, "S&P Local LT Action"/*, "ADF_LT_Rating_Action_LOC_SuP", 2469 */, IN, BND);

    public static final Field ratingSnPLocalShortTermAction = createStatic(847, "ratingSnPLocalShortTermAction", STRING, "S&P Local ST Action"/*, "ADF_ST_Rating_Action_LOC_SuP", 2472 */, IN, BND);

    //  createEnumStatic: fields are posted on static data update as instrument fields
    //


    static {
        RatioFieldDescription.expires.setNullAsMin(false);
    }

    static {
        // since we return these BitSets from some methods: make them Unmodifiable.
        QR = new UnmodifiableBitSet(QR);
        QS = new UnmodifiableBitSet(QS);
        IN = new UnmodifiableBitSet(IN);
        PERCENT = new UnmodifiableBitSet(PERCENT);
        ENUMS = new UnmodifiableBitSet(ENUMS);
    }

    public static final String ANY_VWD_TP = "-ANY-VWD-TP";

    public static final String UNDERLYING_PRODUCT_IID = "FUT_UND_PROD_IID";

    public static final String VWD_RIMPAR_FIELDS = "VWD_RIMPAR_FIELDS";

    public static final String FIDA_RATING_R_FIELDS = "FIDA_RATING_R";

    public static final String FIDA_RATING_I_FIELDS = "FIDA_RATING_I";

    static {
        final Map<RatioDataRecord.Field, Field> fnd = new HashMap<>();
        fnd.put(RatioDataRecord.Field.notActive, RatioFieldDescription.wmNotActive);
        fnd.put(RatioDataRecord.Field.name, RatioFieldDescription.name);
        fnd.put(RatioDataRecord.Field.wkn, RatioFieldDescription.wkn);
        fnd.put(RatioDataRecord.Field.isin, RatioFieldDescription.isin);
        fnd.put(RatioDataRecord.Field.vwdCode, RatioFieldDescription.vwdCode);
        fnd.put(RatioDataRecord.Field.market, RatioFieldDescription.vwdMarket);
        fnd.put(RatioDataRecord.Field.issuername, RatioFieldDescription.issuerName);
        fnd.put(RatioDataRecord.Field.vrIssuer, RatioFieldDescription.vrIssuer);
        fnd.put(RatioDataRecord.Field.currency, RatioFieldDescription.currency);
        fnd.put(RatioDataRecord.Field.country, RatioFieldDescription.country);
        fnd.put(RatioDataRecord.Field.etf, RatioFieldDescription.isEtf);

        fnd.put(RatioDataRecord.Field.volatility1m, RatioFieldDescription.volatility1m);
        fnd.put(RatioDataRecord.Field.volatility3m, RatioFieldDescription.volatility3m);
        fnd.put(RatioDataRecord.Field.volatility6m, RatioFieldDescription.volatility6m);
        fnd.put(RatioDataRecord.Field.volatility1y, RatioFieldDescription.volatility1y);
        fnd.put(RatioDataRecord.Field.volatility3y, RatioFieldDescription.volatility3y);
        fnd.put(RatioDataRecord.Field.volatility5y, RatioFieldDescription.volatility5y);
        fnd.put(RatioDataRecord.Field.volatility10y, RatioFieldDescription.volatility10y);
        fnd.put(RatioDataRecord.Field.volatilityCurrentYear, RatioFieldDescription.volatilityCurrentYear);
        fnd.put(RatioDataRecord.Field.changePercent, RatioFieldDescription.changePercent);
        fnd.put(RatioDataRecord.Field.bviperformanceCurrentYear, RatioFieldDescription.bviperformancecurrentyear);
        fnd.put(RatioDataRecord.Field.bviperformance1d, RatioFieldDescription.changePercent);
        fnd.put(RatioDataRecord.Field.bviperformance1w, RatioFieldDescription.bviperformance1w);
        fnd.put(RatioDataRecord.Field.bviperformance1m, RatioFieldDescription.bviperformance1m);
        fnd.put(RatioDataRecord.Field.bviperformance3m, RatioFieldDescription.bviperformance3m);
        fnd.put(RatioDataRecord.Field.bviperformance6m, RatioFieldDescription.bviperformance6m);
        fnd.put(RatioDataRecord.Field.bviperformance1y, RatioFieldDescription.bviperformance1y);
        fnd.put(RatioDataRecord.Field.bviperformance3y, RatioFieldDescription.bviperformance3y);
        fnd.put(RatioDataRecord.Field.bviperformance5y, RatioFieldDescription.bviperformance5y);
        fnd.put(RatioDataRecord.Field.bviperformance10y, RatioFieldDescription.bviperformance10y);
        fnd.put(RatioDataRecord.Field.sharpeRatio1w, RatioFieldDescription.sharperatio1w);
        fnd.put(RatioDataRecord.Field.sharpeRatio1m, RatioFieldDescription.sharperatio1m);
        fnd.put(RatioDataRecord.Field.sharpeRatio3m, RatioFieldDescription.sharperatio3m);
        fnd.put(RatioDataRecord.Field.sharpeRatio6m, RatioFieldDescription.sharperatio6m);
        fnd.put(RatioDataRecord.Field.sharpeRatio1y, RatioFieldDescription.sharperatio1y);
        fnd.put(RatioDataRecord.Field.sharpeRatio3y, RatioFieldDescription.sharperatio3y);
        fnd.put(RatioDataRecord.Field.sharpeRatio5y, RatioFieldDescription.sharperatio5y);
        fnd.put(RatioDataRecord.Field.sharpeRatio10y, RatioFieldDescription.sharperatio10y);
        fnd.put(RatioDataRecord.Field.maximumLoss3y, RatioFieldDescription.maximumLoss3y);
        fnd.put(RatioDataRecord.Field.marketAdmission, RatioFieldDescription.marketAdmission);
        fnd.put(RatioDataRecord.Field.fundtypeBviCoarse, RatioFieldDescription.bviKategorieGrob);
        fnd.put(RatioDataRecord.Field.benchmarkOutperformance1m, RatioFieldDescription.performanceToBenchmark1m);
        fnd.put(RatioDataRecord.Field.benchmarkOutperformance6m, RatioFieldDescription.performanceToBenchmark6m);
        fnd.put(RatioDataRecord.Field.benchmarkOutperformance1y, RatioFieldDescription.performanceToBenchmark1y);
        fnd.put(RatioDataRecord.Field.benchmarkOutperformance3y, RatioFieldDescription.performanceToBenchmark3y);
        fnd.put(RatioDataRecord.Field.benchmarkOutperformance5y, RatioFieldDescription.performanceToBenchmark5y);
        fnd.put(RatioDataRecord.Field.benchmarkOutperformanceCurrentYear, RatioFieldDescription.performanceToBenchmarkCurrentYear);
        fnd.put(RatioDataRecord.Field.probabilityOfOutperformance3y, RatioFieldDescription.probabilityofoutperformance3y);
        fnd.put(RatioDataRecord.Field.alpha1w, RatioFieldDescription.alpha1w);
        fnd.put(RatioDataRecord.Field.alpha1m, RatioFieldDescription.alpha1m);
        fnd.put(RatioDataRecord.Field.alpha3m, RatioFieldDescription.alpha3m);
        fnd.put(RatioDataRecord.Field.alpha6m, RatioFieldDescription.alpha6m);
        fnd.put(RatioDataRecord.Field.alpha1y, RatioFieldDescription.alpha1y);
        fnd.put(RatioDataRecord.Field.alpha3y, RatioFieldDescription.alpha3y);
        fnd.put(RatioDataRecord.Field.alpha5y, RatioFieldDescription.alpha5y);
        fnd.put(RatioDataRecord.Field.alpha10y, RatioFieldDescription.alpha10y);
        FIELDNAMES.put(FND, fnd);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> fwwFields = new HashMap<>();
        fwwFields.put(RatioDataRecord.Field.issueSurcharge, RatioFieldDescription.fwwIssueSurcharge);
        fwwFields.put(RatioDataRecord.Field.managementFee, RatioFieldDescription.fwwManagementFee);
        fwwFields.put(RatioDataRecord.Field.accountFee, RatioFieldDescription.fwwAccountFee);
        fwwFields.put(RatioDataRecord.Field.issuername, RatioFieldDescription.fwwKag);
        fwwFields.put(RatioDataRecord.Field.investmentFocus, RatioFieldDescription.fwwSector);
        fwwFields.put(RatioDataRecord.Field.fundtype, RatioFieldDescription.fwwFundType);
        fwwFields.put(RatioDataRecord.Field.ter, RatioFieldDescription.fwwTer);
        fwwFields.put(RatioDataRecord.Field.fwwRiskclass, RatioFieldDescription.fwwRiskclass);
        FIELDNAMES_BY_PERMISSION.put("FWW", fwwFields); // no rating in FWW?

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> feriFields = new HashMap<>();
        feriFields.put(RatioDataRecord.Field.ratingFeri, RatioFieldDescription.ratingFeri);
        FIELDNAMES_BY_PERMISSION.put(Selector.RATING_FERI.name(), feriFields);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> msrFields = new HashMap<>();
        msrFields.put(RatioDataRecord.Field.ratingMorningstar, RatioFieldDescription.morningstars);
        FIELDNAMES_BY_PERMISSION.put(Selector.RATING_MORNINGSTAR.name(), msrFields);

        // see T-45927
        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> msrDZFields = new HashMap<>();
        msrDZFields.put(RatioDataRecord.Field.ratingMorningstar, RatioFieldDescription.morningstarsDZBANK);
        FIELDNAMES_BY_PERMISSION.put(Selector.RATING_MORNINGSTAR_UNION_FND.name(), msrDZFields);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> msFields = new HashMap<>();
        msFields.put(RatioDataRecord.Field.fundtype, RatioFieldDescription.msBroadassetclass);
        msFields.put(RatioDataRecord.Field.issuername, RatioFieldDescription.msIssuername);
        msFields.put(RatioDataRecord.Field.vrIssuer, RatioFieldDescription.msVrIssuer);
        msFields.put(RatioDataRecord.Field.investmentFocus, RatioFieldDescription.msInvestmentFocus);
        msFields.put(RatioDataRecord.Field.country, RatioFieldDescription.msCountry);
        msFields.put(RatioDataRecord.Field.distributionStrategy, RatioFieldDescription.msDistributionStrategy);
        msFields.put(RatioDataRecord.Field.fundVolume, RatioFieldDescription.msFundVolume);
        msFields.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.msIssueDate);
        msFields.put(RatioDataRecord.Field.issueSurcharge, RatioFieldDescription.msIssueSurcharge);
        msFields.put(RatioDataRecord.Field.managementFee, RatioFieldDescription.msManagementfee);
        msFields.put(RatioDataRecord.Field.accountFee, RatioFieldDescription.msAccountfee);
        msFields.put(RatioDataRecord.Field.ter, RatioFieldDescription.msTer);
        msFields.put(RatioDataRecord.Field.marketAdmission, RatioFieldDescription.msMarketAdmission);
        msFields.put(RatioDataRecord.Field.ongoingCharge, RatioFieldDescription.msOngoingCharge);
        msFields.put(RatioDataRecord.Field.ongoingChargeDate, RatioFieldDescription.msOngoingChargeDate);
        FIELDNAMES_BY_PERMISSION.put("MORNINGSTAR", msFields);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> vwdFields = new HashMap<>();
        vwdFields.put(RatioDataRecord.Field.fundtype, RatioFieldDescription.vwdFundType);
        vwdFields.put(RatioDataRecord.Field.issuername, RatioFieldDescription.vwdIssuername);
        vwdFields.put(RatioDataRecord.Field.vrIssuer, RatioFieldDescription.vwdVrIssuer);
        vwdFields.put(RatioDataRecord.Field.investmentFocus, RatioFieldDescription.vwdInvestmentFocus);
        vwdFields.put(RatioDataRecord.Field.country, RatioFieldDescription.vwdCountry);
        vwdFields.put(RatioDataRecord.Field.distributionStrategy, RatioFieldDescription.vwdDistributionStrategy);
        vwdFields.put(RatioDataRecord.Field.fundVolume, RatioFieldDescription.vwdFundVolume);
        vwdFields.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.vwdIssueDate);
        vwdFields.put(RatioDataRecord.Field.issueSurcharge, RatioFieldDescription.vwdIssueSurcharge);
        vwdFields.put(RatioDataRecord.Field.managementFee, RatioFieldDescription.vwdManagementfee);
        vwdFields.put(RatioDataRecord.Field.ter, RatioFieldDescription.vwdTer);
        vwdFields.put(RatioDataRecord.Field.marketAdmission, RatioFieldDescription.vwdMarketAdmission);
        vwdFields.put(RatioDataRecord.Field.ongoingCharge, RatioFieldDescription.vwdOngoingCharge);
        vwdFields.put(RatioDataRecord.Field.ongoingChargeDate, RatioFieldDescription.vwdOngoingChargeDate);
        vwdFields.put(RatioDataRecord.Field.accountFee, RatioFieldDescription.vwdAccountfee);
        vwdFields.put(RatioDataRecord.Field.srriValue, RatioFieldDescription.vwdSrriValue);
        vwdFields.put(RatioDataRecord.Field.srriValueDate, RatioFieldDescription.vwdSrriValueDate);
        FIELDNAMES_BY_PERMISSION.put("VWD", vwdFields);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> ssatFields = new HashMap<>();
        ssatFields.put(RatioDataRecord.Field.fundtype, RatioFieldDescription.ssatFundType);
        ssatFields.put(RatioDataRecord.Field.issuername, RatioFieldDescription.ssatIssuername);
        ssatFields.put(RatioDataRecord.Field.investmentFocus, RatioFieldDescription.ssatInvestmentFocus);
        ssatFields.put(RatioDataRecord.Field.country, RatioFieldDescription.ssatCountry);
        ssatFields.put(RatioDataRecord.Field.distributionStrategy, RatioFieldDescription.ssatDistributionStrategy);
        ssatFields.put(RatioDataRecord.Field.fundVolume, RatioFieldDescription.ssatFundVolume);
        ssatFields.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.ssatIssueDate);
        ssatFields.put(RatioDataRecord.Field.issueSurcharge, RatioFieldDescription.ssatIssueSurcharge);
        ssatFields.put(RatioDataRecord.Field.managementFee, RatioFieldDescription.ssatManagementfee);
        ssatFields.put(RatioDataRecord.Field.accountFee, RatioFieldDescription.ssatAccountfee);
        ssatFields.put(RatioDataRecord.Field.ter, RatioFieldDescription.ssatTer);
        ssatFields.put(RatioDataRecord.Field.marketAdmission, RatioFieldDescription.ssatMarketAdmission);
        FIELDNAMES_BY_PERMISSION.put("SSAT", ssatFields); // no rating in SSAT?

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> fidaFields = new HashMap<>();
        fidaFields.put(RatioDataRecord.Field.fundtype, RatioFieldDescription.fidaFundType);
        fidaFields.put(RatioDataRecord.Field.subtype, RatioFieldDescription.subtype);
        fidaFields.put(RatioDataRecord.Field.issuername, RatioFieldDescription.fidaIssuername);
        fidaFields.put(RatioDataRecord.Field.investmentFocus, RatioFieldDescription.fidaInvestmentFocus);
        fidaFields.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.fidaIssueDate);
        fidaFields.put(RatioDataRecord.Field.ter, RatioFieldDescription.fidaTer);
        fidaFields.put(RatioDataRecord.Field.country, RatioFieldDescription.fidaCountry);
        fidaFields.put(RatioDataRecord.Field.distributionStrategy, RatioFieldDescription.fidaDistributionStrategy);
        fidaFields.put(RatioDataRecord.Field.issueSurcharge, RatioFieldDescription.fidaIssueSurcharge);
        fidaFields.put(RatioDataRecord.Field.managementFee, RatioFieldDescription.fidaManagementfee);
        fidaFields.put(RatioDataRecord.Field.fundVolume, RatioFieldDescription.fidaFundVolume);
        FIELDNAMES_BY_PERMISSION.put("FIDA", fidaFields);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> fidaRatingROnlyFields = new HashMap<>();
        fidaRatingROnlyFields.put(RatioDataRecord.Field.fidaPermissionType, RatioFieldDescription.fidaPermissionType);
        fidaRatingROnlyFields.put(RatioDataRecord.Field.fidaRating, RatioFieldDescription.fidaRatingROnly);
        FIELDNAMES_BY_PERMISSION.put(FIDA_RATING_R_FIELDS, fidaRatingROnlyFields);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> fidaRatingIOnlyFields = new HashMap<>();
        fidaRatingIOnlyFields.put(RatioDataRecord.Field.fidaPermissionType, RatioFieldDescription.fidaPermissionType);
        fidaRatingIOnlyFields.put(RatioDataRecord.Field.fidaRating, RatioFieldDescription.fidaRatingIOnly);
        FIELDNAMES_BY_PERMISSION.put(FIDA_RATING_I_FIELDS, fidaRatingIOnlyFields);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> vwdbenlFields = new HashMap<>();
        vwdbenlFields.put(RatioDataRecord.Field.fundtype, RatioFieldDescription.vwdbenlFundType);
        vwdbenlFields.put(RatioDataRecord.Field.issuername, RatioFieldDescription.vwdbenlIssuername);
        vwdbenlFields.put(RatioDataRecord.Field.investmentFocus, RatioFieldDescription.vwdbenlInvestmentFocus);
        vwdbenlFields.put(RatioDataRecord.Field.country, RatioFieldDescription.vwdbenlCountry);
        vwdbenlFields.put(RatioDataRecord.Field.distributionStrategy, RatioFieldDescription.vwdbenlDistStrategy);
        vwdbenlFields.put(RatioDataRecord.Field.fundVolume, RatioFieldDescription.vwdbenlFundVolume);
        vwdbenlFields.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.vwdbenlIssueDate);
        vwdbenlFields.put(RatioDataRecord.Field.issueSurcharge, RatioFieldDescription.vwdbenlIssueSurcharge);
        vwdbenlFields.put(RatioDataRecord.Field.managementFee, RatioFieldDescription.vwdbenlManagementfee);
        vwdbenlFields.put(RatioDataRecord.Field.ter, RatioFieldDescription.vwdbenlTer);
        vwdbenlFields.put(RatioDataRecord.Field.srriValue, RatioFieldDescription.vwdbenlSrriValue);
        vwdbenlFields.put(RatioDataRecord.Field.srriValueDate, RatioFieldDescription.vwdbenlSrriValueDate);
        FIELDNAMES_BY_PERMISSION.put("VWD_BENL", vwdbenlFields);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> vwdDiamondRatingFields = new HashMap<>();
        vwdDiamondRatingFields.put(RatioDataRecord.Field.diamondRating, RatioFieldDescription.vwdDiamondRating);
        vwdDiamondRatingFields.put(RatioDataRecord.Field.diamondRatingDate, RatioFieldDescription.vwdDiamondRatingDate);
        FIELDNAMES_BY_PERMISSION.put(Selector.DIAMOND_RATING.name(), vwdDiamondRatingFields);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> vwditFields = new HashMap<>();
        vwditFields.put(RatioDataRecord.Field.fundtype, RatioFieldDescription.vwditFundType);
        vwditFields.put(RatioDataRecord.Field.issuername, RatioFieldDescription.vwditIssuername);
        vwditFields.put(RatioDataRecord.Field.investmentFocus, RatioFieldDescription.vwditInvestmentFocus);
        vwditFields.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.vwditIssueDate);
        vwditFields.put(RatioDataRecord.Field.ter, RatioFieldDescription.vwditTer);
        FIELDNAMES_BY_PERMISSION.put("VWD_IT", vwditFields);

        FIELDNAMES_BY_PERMISSION.put("VWD_IT_RATING",
                Collections.singletonMap(RatioDataRecord.Field.cfsRating, RatioFieldDescription.vwditCfsRating));

        FIELDNAMES_BY_PERMISSION.put("FND_DZ",
                Collections.singletonMap(RatioDataRecord.Field.wmInvestmentAssetPoolClass, RatioFieldDescription.wmInvestmentAssetPoolClass));

        final Map<RatioDataRecord.Field, Field> stk = new HashMap<>();
        stk.put(RatioDataRecord.Field.notActive, RatioFieldDescription.wmNotActive);
        stk.put(RatioDataRecord.Field.qid, RatioFieldDescription.qid);
        stk.put(RatioDataRecord.Field.name, RatioFieldDescription.name);
        stk.put(RatioDataRecord.Field.wkn, RatioFieldDescription.wkn);
        stk.put(RatioDataRecord.Field.isin, RatioFieldDescription.isin);
        stk.put(RatioDataRecord.Field.currency, RatioFieldDescription.currency);
        stk.put(RatioDataRecord.Field.market, RatioFieldDescription.vwdMarket);
        stk.put(RatioDataRecord.Field.region, RatioFieldDescription.country);
        stk.put(RatioDataRecord.Field.country, RatioFieldDescription.country);
        stk.put(RatioDataRecord.Field.sector, RatioFieldDescription.sector);
        stk.put(RatioDataRecord.Field.benchmarkName, RatioFieldDescription.benchmarkName);
        stk.put(RatioDataRecord.Field.index, null);
        stk.put(RatioDataRecord.Field.marketCapitalization, RatioFieldDescription.marketCapitalization);
        stk.put(RatioDataRecord.Field.marketCapitalizationUSD, RatioFieldDescription.marketCapitalizationUSD);
        stk.put(RatioDataRecord.Field.marketCapitalizationEUR, RatioFieldDescription.marketCapitalizationEUR);
        stk.put(RatioDataRecord.Field.volatility1w, RatioFieldDescription.volatility1w);
        stk.put(RatioDataRecord.Field.volatility1m, RatioFieldDescription.volatility1m);
        stk.put(RatioDataRecord.Field.volatility3m, RatioFieldDescription.volatility3m);
        stk.put(RatioDataRecord.Field.volatility6m, RatioFieldDescription.volatility6m);
        stk.put(RatioDataRecord.Field.volatility1y, RatioFieldDescription.volatility1y);
        stk.put(RatioDataRecord.Field.volatility3y, RatioFieldDescription.volatility3y);
        stk.put(RatioDataRecord.Field.volatility5y, RatioFieldDescription.volatility5y);
        stk.put(RatioDataRecord.Field.volatility10y, RatioFieldDescription.volatility10y);
        stk.put(RatioDataRecord.Field.volatilityCurrentYear, RatioFieldDescription.volatilityCurrentYear);
        stk.put(RatioDataRecord.Field.changePercent, RatioFieldDescription.changePercent);
        stk.put(RatioDataRecord.Field.performance1w, RatioFieldDescription.performance1w);
        stk.put(RatioDataRecord.Field.performance1m, RatioFieldDescription.performance1m);
        stk.put(RatioDataRecord.Field.performance3m, RatioFieldDescription.performance3m);
        stk.put(RatioDataRecord.Field.performance6m, RatioFieldDescription.performance6m);
        stk.put(RatioDataRecord.Field.performance1y, RatioFieldDescription.performance1y);
        stk.put(RatioDataRecord.Field.performance3y, RatioFieldDescription.performance3y);
        stk.put(RatioDataRecord.Field.performance5y, RatioFieldDescription.performance5y);
        stk.put(RatioDataRecord.Field.performance10y, RatioFieldDescription.performance10y);
        stk.put(RatioDataRecord.Field.performanceCurrentYear, RatioFieldDescription.performanceCurrentYear);
        stk.put(RatioDataRecord.Field.performanceToBenchmark1d, RatioFieldDescription.performanceToBenchmark1d);
        stk.put(RatioDataRecord.Field.performanceToBenchmark1w, RatioFieldDescription.performanceToBenchmark1w);
        stk.put(RatioDataRecord.Field.performanceToBenchmark1m, RatioFieldDescription.performanceToBenchmark1m);
        stk.put(RatioDataRecord.Field.performanceToBenchmark3m, RatioFieldDescription.performanceToBenchmark3m);
        stk.put(RatioDataRecord.Field.performanceToBenchmark6m, RatioFieldDescription.performanceToBenchmark6m);
        stk.put(RatioDataRecord.Field.performanceToBenchmark1y, RatioFieldDescription.performanceToBenchmark1y);
        stk.put(RatioDataRecord.Field.performanceToBenchmark3y, RatioFieldDescription.performanceToBenchmark3y);
        stk.put(RatioDataRecord.Field.performanceToBenchmark5y, RatioFieldDescription.performanceToBenchmark5y);
        stk.put(RatioDataRecord.Field.performanceToBenchmark10y, RatioFieldDescription.performanceToBenchmark10y);
        stk.put(RatioDataRecord.Field.performanceToBenchmarkCurrentYear, RatioFieldDescription.performanceToBenchmarkCurrentYear);
        stk.put(RatioDataRecord.Field.beta1m, RatioFieldDescription.beta1m);
        stk.put(RatioDataRecord.Field.beta1y, RatioFieldDescription.beta1y);
        stk.put(RatioDataRecord.Field.alpha1m, RatioFieldDescription.alpha1m);
        stk.put(RatioDataRecord.Field.alpha1y, RatioFieldDescription.alpha1y);
        stk.put(RatioDataRecord.Field.changePercentHigh1y, RatioFieldDescription.changePercentHigh52Weeks);
        stk.put(RatioDataRecord.Field.changePercentLow1y, RatioFieldDescription.changePercentLow52Weeks);
        stk.put(RatioDataRecord.Field.changePercentHighAlltime, RatioFieldDescription.changePercentAlltimeHigh);
        stk.put(RatioDataRecord.Field.correlation1w, RatioFieldDescription.correlation1w);
        stk.put(RatioDataRecord.Field.correlation1m, RatioFieldDescription.correlation1m);
        stk.put(RatioDataRecord.Field.correlation3m, RatioFieldDescription.correlation3m);
        stk.put(RatioDataRecord.Field.correlation6m, RatioFieldDescription.correlation6m);
        stk.put(RatioDataRecord.Field.correlation1y, RatioFieldDescription.correlation1y);
        stk.put(RatioDataRecord.Field.correlation3y, RatioFieldDescription.correlation3y);
        stk.put(RatioDataRecord.Field.correlation5y, RatioFieldDescription.correlation5y);
        stk.put(RatioDataRecord.Field.correlation10y, RatioFieldDescription.correlation10y);
        stk.put(RatioDataRecord.Field.correlationCurrentYear, RatioFieldDescription.correlationCurrentYear);
        stk.put(RatioDataRecord.Field.averageVolume1w, RatioFieldDescription.averageVolume1w);
        stk.put(RatioDataRecord.Field.averageVolume1m, RatioFieldDescription.averageVolume1m);
        stk.put(RatioDataRecord.Field.averageVolume3m, RatioFieldDescription.averageVolume3m);
        stk.put(RatioDataRecord.Field.averageVolume6m, RatioFieldDescription.averageVolume6m);
        stk.put(RatioDataRecord.Field.averageVolume1y, RatioFieldDescription.averageVolume1y);
        stk.put(RatioDataRecord.Field.averageVolume3y, RatioFieldDescription.averageVolume3y);
        stk.put(RatioDataRecord.Field.averageVolume5y, RatioFieldDescription.averageVolume5y);
        stk.put(RatioDataRecord.Field.averageVolume10y, RatioFieldDescription.averageVolume10y);
        stk.put(RatioDataRecord.Field.turnoverDay, RatioFieldDescription.turnoverDay);
        stk.put(RatioDataRecord.Field.screenerInterest, RatioFieldDescription.screenerInterest);
        stk.put(RatioDataRecord.Field.dividend, RatioFieldDescription.wmDividend);
        stk.put(RatioDataRecord.Field.dividendYield, RatioFieldDescription.wmDividendYield);
        stk.put(RatioDataRecord.Field.dividendCurrency, RatioFieldDescription.wmDividendCurrency);
        stk.put(RatioDataRecord.Field.gicsSector, RatioFieldDescription.gicsSector);
        stk.put(RatioDataRecord.Field.gicsIndustryGroup, RatioFieldDescription.gicsIndustryGroup);
        stk.put(RatioDataRecord.Field.gicsIndustry, RatioFieldDescription.gicsIndustry);
        stk.put(RatioDataRecord.Field.gicsSubIndustry, RatioFieldDescription.gicsSubIndustry);
        stk.put(RatioDataRecord.Field.gicsSectorKey, RatioFieldDescription.gicsSectorKey);
        stk.put(RatioDataRecord.Field.gicsIndustryGroupKey, RatioFieldDescription.gicsIndustryGroupKey);
        stk.put(RatioDataRecord.Field.gicsIndustryKey, RatioFieldDescription.gicsIndustryKey);
        stk.put(RatioDataRecord.Field.gicsSubIndustryKey, RatioFieldDescription.gicsSubIndustryKey);
        stk.put(RatioDataRecord.Field.lei, RatioFieldDescription.lei);
        stk.put(RatioDataRecord.Field.averagePrice1w, RatioFieldDescription.averagePrice1w);
        stk.put(RatioDataRecord.Field.averagePrice1m, RatioFieldDescription.averagePrice1m);
        stk.put(RatioDataRecord.Field.averagePrice3m, RatioFieldDescription.averagePrice3m);
        stk.put(RatioDataRecord.Field.averagePrice6m, RatioFieldDescription.averagePrice6m);
        stk.put(RatioDataRecord.Field.averagePrice1y, RatioFieldDescription.averagePrice1y);
        stk.put(RatioDataRecord.Field.averagePrice3y, RatioFieldDescription.averagePrice3y);
        stk.put(RatioDataRecord.Field.averagePrice5y, RatioFieldDescription.averagePrice5y);
        stk.put(RatioDataRecord.Field.averagePrice10y, RatioFieldDescription.averagePrice10y);
        FIELDNAMES.put(STK, stk);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> factsetFields = new HashMap<>();
        factsetFields.put(RatioDataRecord.Field.priceSalesRatio1y, RatioFieldDescription.factsetCurrentPriceSalesRatio1Y);
        factsetFields.put(RatioDataRecord.Field.priceSalesRatio2y, RatioFieldDescription.factsetCurrentPriceSalesRatio2Y);
        factsetFields.put(RatioDataRecord.Field.priceSalesRatio3y, RatioFieldDescription.factsetCurrentPriceSalesRatio3Y);
        factsetFields.put(RatioDataRecord.Field.priceSalesRatio4y, RatioFieldDescription.factsetCurrentPriceSalesRatio4Y);
        factsetFields.put(RatioDataRecord.Field.priceEarningRatio1y, RatioFieldDescription.factsetPriceEarningRatio1Y);
        factsetFields.put(RatioDataRecord.Field.priceEarningRatio2y, RatioFieldDescription.factsetPriceEarningRatio2Y);
        factsetFields.put(RatioDataRecord.Field.priceEarningRatio3y, RatioFieldDescription.factsetPriceEarningRatio3Y);
        factsetFields.put(RatioDataRecord.Field.priceEarningRatio4y, RatioFieldDescription.factsetPriceEarningRatio4Y);
        factsetFields.put(RatioDataRecord.Field.priceBookvalueRatio1y, RatioFieldDescription.factsetCurrentPriceBookvalueRatio1Y);
        factsetFields.put(RatioDataRecord.Field.priceBookvalueRatio2y, RatioFieldDescription.factsetCurrentPriceBookvalueRatio2Y);
        factsetFields.put(RatioDataRecord.Field.priceCashflowRatio1y, RatioFieldDescription.factsetCurrentPriceCashflowRatio1Y);
        factsetFields.put(RatioDataRecord.Field.priceCashflowRatio2y, RatioFieldDescription.factsetCurrentPriceCashflowRatio2Y);
        factsetFields.put(RatioDataRecord.Field.priceCashflowRatio3y, RatioFieldDescription.factsetCurrentPriceCashflowRatio3Y);
        factsetFields.put(RatioDataRecord.Field.priceCashflowRatio4y, RatioFieldDescription.factsetCurrentPriceCashflowRatio4Y);
        factsetFields.put(RatioDataRecord.Field.eps1y, RatioFieldDescription.factsetEps1Y);
        factsetFields.put(RatioDataRecord.Field.eps2y, RatioFieldDescription.factsetEps2Y);
        factsetFields.put(RatioDataRecord.Field.dividend1y, RatioFieldDescription.factsetDividend1Y);
        factsetFields.put(RatioDataRecord.Field.dividend2y, RatioFieldDescription.factsetDividend2Y);
        factsetFields.put(RatioDataRecord.Field.dividendYield1y, RatioFieldDescription.factsetDividendyield1Y);
        factsetFields.put(RatioDataRecord.Field.dividendYield2y, RatioFieldDescription.factsetDividendyield2Y);
        factsetFields.put(RatioDataRecord.Field.dividendYield3y, RatioFieldDescription.factsetDividendyield3Y);
        factsetFields.put(RatioDataRecord.Field.dividendYield4y, RatioFieldDescription.factsetDividendyield4Y);
        factsetFields.put(RatioDataRecord.Field.sales1y, RatioFieldDescription.factsetSales1Y);
        factsetFields.put(RatioDataRecord.Field.sales2y, RatioFieldDescription.factsetSales2Y);
        factsetFields.put(RatioDataRecord.Field.sales3y, RatioFieldDescription.factsetSales3Y);
        factsetFields.put(RatioDataRecord.Field.sales4y, RatioFieldDescription.factsetSales4Y);
        factsetFields.put(RatioDataRecord.Field.profit1y, RatioFieldDescription.factsetProfit1Y);
        factsetFields.put(RatioDataRecord.Field.profit2y, RatioFieldDescription.factsetProfit2Y);
        factsetFields.put(RatioDataRecord.Field.profit3y, RatioFieldDescription.factsetProfit3Y);
        factsetFields.put(RatioDataRecord.Field.profit4y, RatioFieldDescription.factsetProfit4Y);
        factsetFields.put(RatioDataRecord.Field.ebit1y, RatioFieldDescription.factsetEbit1Y);
        factsetFields.put(RatioDataRecord.Field.ebit2y, RatioFieldDescription.factsetEbit2Y);
        factsetFields.put(RatioDataRecord.Field.ebit3y, RatioFieldDescription.factsetEbit3Y);
        factsetFields.put(RatioDataRecord.Field.ebit4y, RatioFieldDescription.factsetEbit4Y);
        factsetFields.put(RatioDataRecord.Field.ebitda1y, RatioFieldDescription.factsetEbitda1Y);
        factsetFields.put(RatioDataRecord.Field.ebitda2y, RatioFieldDescription.factsetEbitda2Y);
        factsetFields.put(RatioDataRecord.Field.ebitda3y, RatioFieldDescription.factsetEbitda3Y);
        factsetFields.put(RatioDataRecord.Field.ebitda4y, RatioFieldDescription.factsetEbitda4Y);
        factsetFields.put(RatioDataRecord.Field.recommendation, RatioFieldDescription.factsetRecommendation);
        factsetFields.put(RatioDataRecord.Field.fiscalYear, RatioFieldDescription.factsetFiscalYear);
        FIELDNAMES_BY_PERMISSION.put("MMXML", factsetFields);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> trFields = new HashMap<>();
        trFields.put(RatioDataRecord.Field.priceSalesRatio1y, RatioFieldDescription.trCurrentPriceSalesRatio1Y);
        trFields.put(RatioDataRecord.Field.priceSalesRatio2y, RatioFieldDescription.trCurrentPriceSalesRatio2Y);
        trFields.put(RatioDataRecord.Field.priceEarningRatio1y, RatioFieldDescription.trPriceEarningRatio1Y);
        trFields.put(RatioDataRecord.Field.priceEarningRatio2y, RatioFieldDescription.trPriceEarningRatio2Y);
        trFields.put(RatioDataRecord.Field.priceBookvalueRatio1y, RatioFieldDescription.trCurrentPriceBookvalueRatio1Y);
        trFields.put(RatioDataRecord.Field.priceBookvalueRatio2y, RatioFieldDescription.trCurrentPriceBookvalueRatio2Y);
        trFields.put(RatioDataRecord.Field.priceCashflowRatio1y, RatioFieldDescription.trCurrentPriceCashflowRatio1Y);
        trFields.put(RatioDataRecord.Field.priceCashflowRatio2y, RatioFieldDescription.trCurrentPriceCashflowRatio2Y);
        trFields.put(RatioDataRecord.Field.eps1y, RatioFieldDescription.trEps1Y);
        trFields.put(RatioDataRecord.Field.eps2y, RatioFieldDescription.trEps2Y);
        trFields.put(RatioDataRecord.Field.dividend1y, RatioFieldDescription.trDividend1Y);
        trFields.put(RatioDataRecord.Field.dividend2y, RatioFieldDescription.trDividend2Y);
        trFields.put(RatioDataRecord.Field.dividendYield1y, RatioFieldDescription.trDividendyield1Y);
        trFields.put(RatioDataRecord.Field.dividendYield2y, RatioFieldDescription.trDividendyield2Y);
        trFields.put(RatioDataRecord.Field.sales1y, RatioFieldDescription.trSales1Y);
        trFields.put(RatioDataRecord.Field.sales2y, RatioFieldDescription.trSales2Y);
        trFields.put(RatioDataRecord.Field.profit1y, RatioFieldDescription.trProfit1Y);
        trFields.put(RatioDataRecord.Field.profit2y, RatioFieldDescription.trProfit2Y);
        trFields.put(RatioDataRecord.Field.ebit1y, RatioFieldDescription.trEbit1Y);
        trFields.put(RatioDataRecord.Field.ebit2y, RatioFieldDescription.trEbit2Y);
        trFields.put(RatioDataRecord.Field.ebitda1y, RatioFieldDescription.trEbitda1Y);
        trFields.put(RatioDataRecord.Field.ebitda2y, RatioFieldDescription.trEbitda2Y);
        trFields.put(RatioDataRecord.Field.recommendation, RatioFieldDescription.trRecommendation);
        trFields.put(RatioDataRecord.Field.fiscalYear, RatioFieldDescription.trFiscalYear);
        FIELDNAMES_BY_PERMISSION.put("TR-ESTIMATES", trFields);


        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> wmStkFields = new HashMap<>();
        wmStkFields.put(RatioDataRecord.Field.country, RatioFieldDescription.wmCountry);
        wmStkFields.put(RatioDataRecord.Field.sector, RatioFieldDescription.wmSector);
        FIELDNAMES_BY_PERMISSION.put(STK.name() + ANY_VWD_TP, wmStkFields);

        final Map<RatioDataRecord.Field, Field> wnt = new HashMap<>();
        wnt.put(RatioDataRecord.Field.notActive, RatioFieldDescription.wmNotActive);
        wnt.put(RatioDataRecord.Field.name, RatioFieldDescription.name);
        wnt.put(RatioDataRecord.Field.wkn, RatioFieldDescription.wkn);
        wnt.put(RatioDataRecord.Field.isin, RatioFieldDescription.isin);
        wnt.put(RatioDataRecord.Field.market, RatioFieldDescription.vwdMarket);
        wnt.put(RatioDataRecord.Field.underlyingVwdcode, null);
        wnt.put(RatioDataRecord.Field.underlyingQid, null);
        wnt.put(RatioDataRecord.Field.underlyingIsin, RatioFieldDescription.underlyingIsin);
        wnt.put(RatioDataRecord.Field.underlyingName, RatioFieldDescription.underlyingName);
        wnt.put(RatioDataRecord.Field.underlyingIid, RatioFieldDescription.underlyingIid);
        wnt.put(RatioDataRecord.Field.currency, RatioFieldDescription.currency);
        wnt.put(RatioDataRecord.Field.warrantType, RatioFieldDescription.osType);
        wnt.put(RatioDataRecord.Field.strike, RatioFieldDescription.strikePrice);
        wnt.put(RatioDataRecord.Field.expirationDate, RatioFieldDescription.expires);
        wnt.put(RatioDataRecord.Field.issuername, RatioFieldDescription.issuerName);
        wnt.put(RatioDataRecord.Field.vrIssuer, RatioFieldDescription.vrIssuer);
        wnt.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.issueDate);
        wnt.put(RatioDataRecord.Field.isAmerican, RatioFieldDescription.isAmerican);

        wnt.put(RatioDataRecord.Field.averageVolume1w, RatioFieldDescription.averageVolume1w);

        wnt.put(RatioDataRecord.Field.omega, RatioFieldDescription.omega);
        wnt.put(RatioDataRecord.Field.impliedVolatility, RatioFieldDescription.impliedvolatility);
        wnt.put(RatioDataRecord.Field.delta, RatioFieldDescription.delta);
        wnt.put(RatioDataRecord.Field.intrinsicValue, RatioFieldDescription.intrinsicvalue);
        wnt.put(RatioDataRecord.Field.extrinsicValue, RatioFieldDescription.extrinsicvalue);
        wnt.put(RatioDataRecord.Field.optionPrice, RatioFieldDescription.optionprice);
        wnt.put(RatioDataRecord.Field.optionPricePerYear, RatioFieldDescription.optionpriceperyear);
        wnt.put(RatioDataRecord.Field.fairValue, RatioFieldDescription.fairvalue);
        wnt.put(RatioDataRecord.Field.breakeven, RatioFieldDescription.breakeven);
        wnt.put(RatioDataRecord.Field.performance1w, RatioFieldDescription.performance1w);
        wnt.put(RatioDataRecord.Field.performance1m, RatioFieldDescription.performance1m);
        wnt.put(RatioDataRecord.Field.performance3m, RatioFieldDescription.performance3m);
        wnt.put(RatioDataRecord.Field.performance6m, RatioFieldDescription.performance6m);
        wnt.put(RatioDataRecord.Field.performance1y, RatioFieldDescription.performance1y);
        wnt.put(RatioDataRecord.Field.volatility1m, RatioFieldDescription.volatility1m);
        wnt.put(RatioDataRecord.Field.volatility3m, RatioFieldDescription.volatility3m);
        wnt.put(RatioDataRecord.Field.volatilityCurrentYear, RatioFieldDescription.volatilityCurrentYear);
        wnt.put(RatioDataRecord.Field.leverage, RatioFieldDescription.leverage);
        wnt.put(RatioDataRecord.Field.spread, RatioFieldDescription.spread);
        wnt.put(RatioDataRecord.Field.spreadPercent, RatioFieldDescription.spreadRelative);
        wnt.put(RatioDataRecord.Field.theta, RatioFieldDescription.theta);
        wnt.put(RatioDataRecord.Field.gamma, RatioFieldDescription.gamma);
        wnt.put(RatioDataRecord.Field.rho, RatioFieldDescription.rho);
        wnt.put(RatioDataRecord.Field.vega, RatioFieldDescription.vega);
        wnt.put(RatioDataRecord.Field.totalVolume, RatioFieldDescription.totalVolume);
        wnt.put(RatioDataRecord.Field.vwdStaticDataAvailable, RatioFieldDescription.vwdStaticDataAvailable);
        FIELDNAMES.put(WNT, wnt);

        final Map<RatioDataRecord.Field, Field> smf_static_wnt = new HashMap<>();
        smf_static_wnt.put(RatioDataRecord.Field.name, RatioFieldDescription.name);
        smf_static_wnt.put(RatioDataRecord.Field.wkn, RatioFieldDescription.wkn);
        smf_static_wnt.put(RatioDataRecord.Field.isin, RatioFieldDescription.isin);
        smf_static_wnt.put(RatioDataRecord.Field.market, RatioFieldDescription.vwdMarket);
        smf_static_wnt.put(RatioDataRecord.Field.underlyingVwdcode, null);
        smf_static_wnt.put(RatioDataRecord.Field.underlyingQid, null);
        smf_static_wnt.put(RatioDataRecord.Field.underlyingIsin, RatioFieldDescription.underlyingIsin);
        smf_static_wnt.put(RatioDataRecord.Field.underlyingName, RatioFieldDescription.underlyingName);
        smf_static_wnt.put(RatioDataRecord.Field.underlyingIid, RatioFieldDescription.underlyingIid);
        smf_static_wnt.put(RatioDataRecord.Field.currency, RatioFieldDescription.currency);
        smf_static_wnt.put(RatioDataRecord.Field.strike, RatioFieldDescription.smfStrike);
        smf_static_wnt.put(RatioDataRecord.Field.issuername, RatioFieldDescription.issuerName);
        smf_static_wnt.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.issueDate);

        smf_static_wnt.put(RatioDataRecord.Field.warrantType, RatioFieldDescription.smfLeverageType);
        smf_static_wnt.put(RatioDataRecord.Field.expirationDate, RatioFieldDescription.smfExpires);
        smf_static_wnt.put(RatioDataRecord.Field.isAmerican, RatioFieldDescription.smfIsAmerican);

        smf_static_wnt.put(RatioDataRecord.Field.averageVolume1w, RatioFieldDescription.averageVolume1w);
        smf_static_wnt.put(RatioDataRecord.Field.totalVolume, RatioFieldDescription.totalVolume);
        smf_static_wnt.put(RatioDataRecord.Field.performance3m, RatioFieldDescription.performance3m);
        smf_static_wnt.put(RatioDataRecord.Field.volatility1m, RatioFieldDescription.volatility1m);
        smf_static_wnt.put(RatioDataRecord.Field.volatility3m, RatioFieldDescription.volatility3m);
        FIELDNAMES_BY_PERMISSION.put("WNT-STATIC-SMF", smf_static_wnt);

        final Map<RatioDataRecord.Field, Field> smf_ratio_wnt = new HashMap<>();
        smf_ratio_wnt.put(RatioDataRecord.Field.omega, RatioFieldDescription.omega);
        smf_ratio_wnt.put(RatioDataRecord.Field.impliedVolatility, RatioFieldDescription.impliedvolatility);
        smf_ratio_wnt.put(RatioDataRecord.Field.delta, RatioFieldDescription.delta);
        smf_ratio_wnt.put(RatioDataRecord.Field.intrinsicValue, RatioFieldDescription.intrinsicvalue);
        smf_ratio_wnt.put(RatioDataRecord.Field.extrinsicValue, RatioFieldDescription.extrinsicvalue);
        smf_ratio_wnt.put(RatioDataRecord.Field.optionPrice, RatioFieldDescription.optionprice);
        smf_ratio_wnt.put(RatioDataRecord.Field.optionPricePerYear, RatioFieldDescription.optionpriceperyear);
        smf_ratio_wnt.put(RatioDataRecord.Field.fairValue, RatioFieldDescription.fairvalue);
        smf_ratio_wnt.put(RatioDataRecord.Field.breakeven, RatioFieldDescription.breakeven);
        smf_ratio_wnt.put(RatioDataRecord.Field.leverage, RatioFieldDescription.leverage);
        smf_ratio_wnt.put(RatioDataRecord.Field.spread, RatioFieldDescription.spread);
        smf_ratio_wnt.put(RatioDataRecord.Field.spreadPercent, RatioFieldDescription.spreadRelative);
        smf_ratio_wnt.put(RatioDataRecord.Field.theta, RatioFieldDescription.theta);
        smf_ratio_wnt.put(RatioDataRecord.Field.gamma, RatioFieldDescription.gamma);
        smf_ratio_wnt.put(RatioDataRecord.Field.rho, RatioFieldDescription.rho);
        smf_ratio_wnt.put(RatioDataRecord.Field.vega, RatioFieldDescription.vega);
        FIELDNAMES_BY_PERMISSION.put("WNT-RATIO-SMF", smf_ratio_wnt);

        final Map<RatioDataRecord.Field, Field> wnt_ratio_sedex = new HashMap<>();
        wnt_ratio_sedex.put(RatioDataRecord.Field.issuername, RatioFieldDescription.sedexIssuerName);
        wnt_ratio_sedex.put(RatioDataRecord.Field.strike, RatioFieldDescription.sedexStrike);
        wnt_ratio_sedex.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.sedexIssueDate);
        wnt_ratio_sedex.put(RatioDataRecord.Field.expirationDate, RatioFieldDescription.sedexExpires);
        FIELDNAMES_BY_PERMISSION.put("WNT-STATIC-SEDEX", wnt_ratio_sedex);

        final Map<RatioDataRecord.Field, Field> cer = new HashMap<>();
        cer.put(RatioDataRecord.Field.notActive, RatioFieldDescription.wmNotActive);
        cer.put(RatioDataRecord.Field.name, RatioFieldDescription.name);
        cer.put(RatioDataRecord.Field.wkn, RatioFieldDescription.wkn);
        cer.put(RatioDataRecord.Field.isin, RatioFieldDescription.isin);
        cer.put(RatioDataRecord.Field.market, RatioFieldDescription.vwdMarket);
        cer.put(RatioDataRecord.Field.underlyingVwdcode, null);
        cer.put(RatioDataRecord.Field.underlyingQid, null);
        cer.put(RatioDataRecord.Field.underlyingWkn, RatioFieldDescription.underlyingWkn);
        cer.put(RatioDataRecord.Field.underlyingIsin, RatioFieldDescription.underlyingIsin);
        cer.put(RatioDataRecord.Field.underlyingName, RatioFieldDescription.underlyingName);
        cer.put(RatioDataRecord.Field.underlyingIid, RatioFieldDescription.underlyingIid);
        cer.put(RatioDataRecord.Field.underlyingType, RatioFieldDescription.underlyingType);
        cer.put(RatioDataRecord.Field.currency, RatioFieldDescription.currency);
        cer.put(RatioDataRecord.Field.expirationDate, RatioFieldDescription.expires);
        cer.put(RatioDataRecord.Field.issuername, RatioFieldDescription.issuerName);
        cer.put(RatioDataRecord.Field.vrIssuer, RatioFieldDescription.vrIssuer);
        cer.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.issueDate);

        cer.put(RatioDataRecord.Field.price, RatioFieldDescription.lastPrice);
        cer.put(RatioDataRecord.Field.spread, RatioFieldDescription.spread);
        cer.put(RatioDataRecord.Field.spreadPercent, RatioFieldDescription.spreadRelative);
        cer.put(RatioDataRecord.Field.averageVolume1m, RatioFieldDescription.averageVolume1m);

        cer.put(RatioDataRecord.Field.type, RatioFieldDescription.type);
        cer.put(RatioDataRecord.Field.typeKey, RatioFieldDescription.typeKey);
        cer.put(RatioDataRecord.Field.subtype, RatioFieldDescription.subtype);
        cer.put(RatioDataRecord.Field.subtypeKey, RatioFieldDescription.subtypeKey);
        cer.put(RatioDataRecord.Field.postbankType, RatioFieldDescription.gatrixxType);
        cer.put(RatioDataRecord.Field.certificateType, RatioFieldDescription.gatrixxTypeFtreff);   // CER_FinderMetadata
        cer.put(RatioDataRecord.Field.certificateSubtype, RatioFieldDescription.gatrixxTypename);
        cer.put(RatioDataRecord.Field.multiassetName, RatioFieldDescription.gatrixxMultiassetName);
        cer.put(RatioDataRecord.Field.certificateTypeDZBANK, RatioFieldDescription.dzCategory);    // CER_FinderMetadata
        cer.put(RatioDataRecord.Field.protectLevel, RatioFieldDescription.gatrixxProtectlevel);
        cer.put(RatioDataRecord.Field.cap, RatioFieldDescription.gatrixxCap);
        cer.put(RatioDataRecord.Field.capLevel, RatioFieldDescription.capLevel);
        cer.put(RatioDataRecord.Field.bonusLevel, RatioFieldDescription.gatrixxBonuslevel);
        cer.put(RatioDataRecord.Field.participationLevel, RatioFieldDescription.gatrixxParticipationlevel);
        cer.put(RatioDataRecord.Field.participationFactor, RatioFieldDescription.gatrixxParticipationFactor);
        cer.put(RatioDataRecord.Field.stoploss, RatioFieldDescription.gatrixxStoploss);
        cer.put(RatioDataRecord.Field.isknockout, RatioFieldDescription.gatrixxIsknockout);
        cer.put(RatioDataRecord.Field.coupon, RatioFieldDescription.gatrixxCoupon);
        cer.put(RatioDataRecord.Field.barrier, RatioFieldDescription.gatrixxBarrier);
        cer.put(RatioDataRecord.Field.guaranteeType, RatioFieldDescription.gatrixxGuaranteeType);
        cer.put(RatioDataRecord.Field.leverageType, RatioFieldDescription.gatrixxLeverageType);
        cer.put(RatioDataRecord.Field.isEndless, RatioFieldDescription.gatrixxIsEndless);

        cer.put(RatioDataRecord.Field.leverage, RatioFieldDescription.mdpsLeverage);
        cer.put(RatioDataRecord.Field.dateBarrierReached, RatioFieldDescription.mdpsDateBarrierReached);
        cer.put(RatioDataRecord.Field.discount, RatioFieldDescription.mdpsDiscount);
        cer.put(RatioDataRecord.Field.discountRelative, RatioFieldDescription.mdpsDiscountRelative);
        cer.put(RatioDataRecord.Field.yield, RatioFieldDescription.mdpsYield);
        cer.put(RatioDataRecord.Field.yieldRelative, RatioFieldDescription.mdpsYieldRelative);
        cer.put(RatioDataRecord.Field.yieldRelativePerYear, RatioFieldDescription.mdpsYieldRelativePerYear);
        cer.put(RatioDataRecord.Field.gapCap, RatioFieldDescription.mdpsGapCap);
        cer.put(RatioDataRecord.Field.gapCapRelative, RatioFieldDescription.mdpsGapCapRelative);
        cer.put(RatioDataRecord.Field.gapBonusLevel, RatioFieldDescription.mdpsGapBonusLevel);
        cer.put(RatioDataRecord.Field.gapBonusLevelRelative, RatioFieldDescription.mdpsGapBonusLevelRelative);
        // TODO: remove gapBonusBufferRelative and mdpsGapBonusBufferRelative ????
        cer.put(RatioDataRecord.Field.gapBonusBufferRelative, RatioFieldDescription.mdpsGapBonusBufferRelative);
        cer.put(RatioDataRecord.Field.agio, RatioFieldDescription.mdpsAgio);
        cer.put(RatioDataRecord.Field.agioRelative, RatioFieldDescription.mdpsAgioRelative);
        cer.put(RatioDataRecord.Field.agioRelativePerYear, RatioFieldDescription.mdpsAgioRelativePerYear);
        cer.put(RatioDataRecord.Field.maximumYield, RatioFieldDescription.mdpsMaximumYield);
        cer.put(RatioDataRecord.Field.maximumYieldRelative, RatioFieldDescription.mdpsMaximumYieldRelative);
        cer.put(RatioDataRecord.Field.maximumYieldRelativePerYear, RatioFieldDescription.mdpsMaximumYieldRelativePerYear);
        cer.put(RatioDataRecord.Field.outperformanceValue, RatioFieldDescription.mdpsOutperformanceValue);
        cer.put(RatioDataRecord.Field.unchangedYield, RatioFieldDescription.mdpsUnchangedYield);
        cer.put(RatioDataRecord.Field.unchangedYieldRelative, RatioFieldDescription.mdpsUnchangedYieldRelative);
        cer.put(RatioDataRecord.Field.unchangedYieldRelativePerYear, RatioFieldDescription.mdpsUnchangedYieldRelativePerYear);
        cer.put(RatioDataRecord.Field.capToUnderlyingRelative, RatioFieldDescription.mdpsCapToUnderlyingRelative);
        cer.put(RatioDataRecord.Field.underlyingToCapRelative, RatioFieldDescription.mdpsUnderlyingToCapRelative);
        cer.put(RatioDataRecord.Field.strike, RatioFieldDescription.gatrixxStrikePrice);
        cer.put(RatioDataRecord.Field.startvalue, RatioFieldDescription.gatrixxStartvalue);
        cer.put(RatioDataRecord.Field.stopvalue, RatioFieldDescription.gatrixxStopvalue);
        cer.put(RatioDataRecord.Field.refundMaximum, RatioFieldDescription.gatrixxRefundMaximum);
        cer.put(RatioDataRecord.Field.knockin, RatioFieldDescription.gatrixxKnockin);
        cer.put(RatioDataRecord.Field.knockindate, RatioFieldDescription.gatrixxKnockindate);
        cer.put(RatioDataRecord.Field.quanto, RatioFieldDescription.gatrixxQuanto);
        cer.put(RatioDataRecord.Field.gapStrike, RatioFieldDescription.mdpsGapStrike);
        cer.put(RatioDataRecord.Field.gapStrikeRelative, RatioFieldDescription.mdpsGapStrikeRelative);
        cer.put(RatioDataRecord.Field.gapBarrier, RatioFieldDescription.mdpsGapBarrier);
        cer.put(RatioDataRecord.Field.gapBarrierRelative, RatioFieldDescription.mdpsGapBarrierRelative);
        cer.put(RatioDataRecord.Field.gapLowerBarrier, RatioFieldDescription.mdpsGapLowerBarrier);
        cer.put(RatioDataRecord.Field.gapLowerBarrierRelative, RatioFieldDescription.mdpsGapLowerBarrierRelative);
        cer.put(RatioDataRecord.Field.gapUpperBarrier, RatioFieldDescription.mdpsGapUpperBarrier);
        cer.put(RatioDataRecord.Field.gapUpperBarrierRelative, RatioFieldDescription.mdpsGapUpperBarrierRelative);
        cer.put(RatioDataRecord.Field.performanceAlltime, RatioFieldDescription.performanceAlltime);
        cer.put(RatioDataRecord.Field.performance1w, RatioFieldDescription.performance1w);
        cer.put(RatioDataRecord.Field.performance1m, RatioFieldDescription.performance1m);
        cer.put(RatioDataRecord.Field.performance3m, RatioFieldDescription.performance3m);
        cer.put(RatioDataRecord.Field.performance6m, RatioFieldDescription.performance6m);
        cer.put(RatioDataRecord.Field.performance1y, RatioFieldDescription.performance1y);
        cer.put(RatioDataRecord.Field.performance3y, RatioFieldDescription.performance3y);
        cer.put(RatioDataRecord.Field.performance5y, RatioFieldDescription.performance5y);
        cer.put(RatioDataRecord.Field.performance10y, RatioFieldDescription.performance10y);
        cer.put(RatioDataRecord.Field.volatility3m, RatioFieldDescription.volatility3m);
        cer.put(RatioDataRecord.Field.wgzListid, RatioFieldDescription.dzWgzListid);
        cer.put(RatioDataRecord.Field.vwdStaticDataAvailable, RatioFieldDescription.vwdStaticDataAvailable);
        FIELDNAMES.put(CER, cer);

        final Map<RatioDataRecord.Field, Field> edg = new HashMap<>();
        edg.put(RatioDataRecord.Field.edgRatingDate, RatioFieldDescription.edgRatingDate);
        edg.put(RatioDataRecord.Field.edgScore1, RatioFieldDescription.edgScore1);
        edg.put(RatioDataRecord.Field.edgScore2, RatioFieldDescription.edgScore2);
        edg.put(RatioDataRecord.Field.edgScore3, RatioFieldDescription.edgScore3);
        edg.put(RatioDataRecord.Field.edgScore4, RatioFieldDescription.edgScore4);
        edg.put(RatioDataRecord.Field.edgScore5, RatioFieldDescription.edgScore5);
        edg.put(RatioDataRecord.Field.edgTopScore, RatioFieldDescription.edgTopScore);
        edg.put(RatioDataRecord.Field.edgTopClass, RatioFieldDescription.edgTopClass);
        FIELDNAMES_BY_PERMISSION.put("EDG", edg);

        final Map<RatioDataRecord.Field, Field> cer_dz = new HashMap<>();
        cer_dz.put(RatioDataRecord.Field.typeKey, RatioFieldDescription.typeKeyDZ);
        cer_dz.put(RatioDataRecord.Field.type, RatioFieldDescription.typeDZ);
        cer_dz.put(RatioDataRecord.Field.subtypeKey, RatioFieldDescription.subtypeKeyDZ);
        cer_dz.put(RatioDataRecord.Field.subtype, RatioFieldDescription.subtypeDZ);
        cer_dz.put(RatioDataRecord.Field.pibAvailable, RatioFieldDescription.dzPib);
        cer_dz.put(RatioDataRecord.Field.dzIsLeverageProduct, RatioFieldDescription.dzIsLeverageProduct);
        FIELDNAMES_BY_PERMISSION.put("CER_DZ", cer_dz);

        final Map<RatioDataRecord.Field, Field> cer_wgz = new HashMap<>();
        cer_wgz.put(RatioDataRecord.Field.typeKey, RatioFieldDescription.typeKeyWGZ);
        cer_wgz.put(RatioDataRecord.Field.type, RatioFieldDescription.typeWGZ);
        cer_wgz.put(RatioDataRecord.Field.subtypeKey, RatioFieldDescription.subtypeKeyWGZ);
        cer_wgz.put(RatioDataRecord.Field.subtype, RatioFieldDescription.subtypeWGZ);
        FIELDNAMES_BY_PERMISSION.put("CER_WGZ", cer_wgz);

        final Map<RatioDataRecord.Field, Field> cer_static_smf = new HashMap<>();
        cer_static_smf.put(RatioDataRecord.Field.name, RatioFieldDescription.name);
        cer_static_smf.put(RatioDataRecord.Field.wkn, RatioFieldDescription.wkn);
        cer_static_smf.put(RatioDataRecord.Field.isin, RatioFieldDescription.isin);
        cer_static_smf.put(RatioDataRecord.Field.market, RatioFieldDescription.vwdMarket);
        cer_static_smf.put(RatioDataRecord.Field.underlyingVwdcode, null);
        cer_static_smf.put(RatioDataRecord.Field.underlyingQid, null);
        cer_static_smf.put(RatioDataRecord.Field.underlyingWkn, RatioFieldDescription.underlyingWkn);
        cer_static_smf.put(RatioDataRecord.Field.underlyingIsin, RatioFieldDescription.underlyingIsin);
        cer_static_smf.put(RatioDataRecord.Field.underlyingName, RatioFieldDescription.underlyingName);
        cer_static_smf.put(RatioDataRecord.Field.underlyingIid, RatioFieldDescription.underlyingIid);
        cer_static_smf.put(RatioDataRecord.Field.underlyingType, RatioFieldDescription.underlyingType);
        cer_static_smf.put(RatioDataRecord.Field.currency, RatioFieldDescription.currency);
        cer_static_smf.put(RatioDataRecord.Field.issuername, RatioFieldDescription.issuerName);
        cer_static_smf.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.issueDate);

        cer_static_smf.put(RatioDataRecord.Field.price, RatioFieldDescription.lastPrice);
        cer_static_smf.put(RatioDataRecord.Field.spread, RatioFieldDescription.spread);
        cer_static_smf.put(RatioDataRecord.Field.spreadPercent, RatioFieldDescription.spreadRelative);

        cer_static_smf.put(RatioDataRecord.Field.type, RatioFieldDescription.smfCertificateType);
        cer_static_smf.put(RatioDataRecord.Field.typeKey, RatioFieldDescription.smfCertificateType);
        cer_static_smf.put(RatioDataRecord.Field.certificateType, RatioFieldDescription.smfCertificateType);
        cer_static_smf.put(RatioDataRecord.Field.certificateSubtype, RatioFieldDescription.smfCertificateType);
        cer_static_smf.put(RatioDataRecord.Field.expirationDate, RatioFieldDescription.smfExpires);
        cer_static_smf.put(RatioDataRecord.Field.multiassetName, RatioFieldDescription.smfMultiassetName);
        cer_static_smf.put(RatioDataRecord.Field.participationLevel, RatioFieldDescription.smfParticipationrate);
        cer_static_smf.put(RatioDataRecord.Field.coupon, RatioFieldDescription.smfCoupon);
        cer_static_smf.put(RatioDataRecord.Field.leverageType, RatioFieldDescription.smfLeverageType);
        cer_static_smf.put(RatioDataRecord.Field.quanto, RatioFieldDescription.smfIsQuanto);
        cer_static_smf.put(RatioDataRecord.Field.performanceAlltime, RatioFieldDescription.mdpsPerformanceAlltime);
        cer_static_smf.put(RatioDataRecord.Field.performance1w, RatioFieldDescription.performance1w);
        cer_static_smf.put(RatioDataRecord.Field.performance1m, RatioFieldDescription.performance1m);
        cer_static_smf.put(RatioDataRecord.Field.performance3m, RatioFieldDescription.performance3m);
        cer_static_smf.put(RatioDataRecord.Field.performance6m, RatioFieldDescription.performance6m);
        cer_static_smf.put(RatioDataRecord.Field.performance1y, RatioFieldDescription.performance1y);
        cer_static_smf.put(RatioDataRecord.Field.performance3y, RatioFieldDescription.performance3y);
        cer_static_smf.put(RatioDataRecord.Field.performance5y, RatioFieldDescription.performance5y);
        cer_static_smf.put(RatioDataRecord.Field.performance10y, RatioFieldDescription.performance10y);
        FIELDNAMES_BY_PERMISSION.put("CER-STATIC-SMF", cer_static_smf);

        // TODO: remove redundancy with cer map
        final Map<RatioDataRecord.Field, Field> cer_ratio_smf = new HashMap<>();
        cer_ratio_smf.put(RatioDataRecord.Field.leverage, RatioFieldDescription.mdpsLeverage);
        cer_ratio_smf.put(RatioDataRecord.Field.dateBarrierReached, RatioFieldDescription.mdpsDateBarrierReached);
        cer_ratio_smf.put(RatioDataRecord.Field.discount, RatioFieldDescription.mdpsDiscount);
        cer_ratio_smf.put(RatioDataRecord.Field.discountRelative, RatioFieldDescription.mdpsDiscountRelative);
        cer_ratio_smf.put(RatioDataRecord.Field.yield, RatioFieldDescription.mdpsYield);
        cer_ratio_smf.put(RatioDataRecord.Field.yieldRelative, RatioFieldDescription.mdpsYieldRelative);
        cer_ratio_smf.put(RatioDataRecord.Field.yieldRelativePerYear, RatioFieldDescription.mdpsYieldRelativePerYear);
        cer_ratio_smf.put(RatioDataRecord.Field.gapCap, RatioFieldDescription.mdpsGapCap);
        cer_ratio_smf.put(RatioDataRecord.Field.gapCapRelative, RatioFieldDescription.mdpsGapCapRelative);
        cer_ratio_smf.put(RatioDataRecord.Field.gapBonusLevel, RatioFieldDescription.mdpsGapBonusLevel);
        cer_ratio_smf.put(RatioDataRecord.Field.gapBonusLevelRelative, RatioFieldDescription.mdpsGapBonusLevelRelative);
        cer_ratio_smf.put(RatioDataRecord.Field.gapBonusBufferRelative, RatioFieldDescription.mdpsGapBonusBufferRelative);
        cer_ratio_smf.put(RatioDataRecord.Field.agio, RatioFieldDescription.mdpsAgio);
        cer_ratio_smf.put(RatioDataRecord.Field.agioRelative, RatioFieldDescription.mdpsAgioRelative);
        cer_ratio_smf.put(RatioDataRecord.Field.agioRelativePerYear, RatioFieldDescription.mdpsAgioRelativePerYear);
        cer_ratio_smf.put(RatioDataRecord.Field.maximumYield, RatioFieldDescription.mdpsMaximumYield);
        cer_ratio_smf.put(RatioDataRecord.Field.maximumYieldRelative, RatioFieldDescription.mdpsMaximumYieldRelative);
        cer_ratio_smf.put(RatioDataRecord.Field.maximumYieldRelativePerYear, RatioFieldDescription.mdpsMaximumYieldRelativePerYear);
        cer_ratio_smf.put(RatioDataRecord.Field.outperformanceValue, RatioFieldDescription.mdpsOutperformanceValue);
        cer_ratio_smf.put(RatioDataRecord.Field.unchangedYield, RatioFieldDescription.mdpsUnchangedYield);
        cer_ratio_smf.put(RatioDataRecord.Field.unchangedYieldRelative, RatioFieldDescription.mdpsUnchangedYieldRelative);
        cer_ratio_smf.put(RatioDataRecord.Field.unchangedYieldRelativePerYear, RatioFieldDescription.mdpsUnchangedYieldRelativePerYear);
        cer_ratio_smf.put(RatioDataRecord.Field.capToUnderlyingRelative, RatioFieldDescription.mdpsCapToUnderlyingRelative);
        cer_ratio_smf.put(RatioDataRecord.Field.underlyingToCapRelative, RatioFieldDescription.mdpsUnderlyingToCapRelative);
        cer_ratio_smf.put(RatioDataRecord.Field.gapStrike, RatioFieldDescription.mdpsGapStrike);
        cer_ratio_smf.put(RatioDataRecord.Field.gapStrikeRelative, RatioFieldDescription.mdpsGapStrikeRelative);
        cer_ratio_smf.put(RatioDataRecord.Field.gapBarrier, RatioFieldDescription.mdpsGapBarrier);
        cer_ratio_smf.put(RatioDataRecord.Field.gapBarrierRelative, RatioFieldDescription.mdpsGapBarrierRelative);
        cer_ratio_smf.put(RatioDataRecord.Field.gapLowerBarrier, RatioFieldDescription.mdpsGapLowerBarrier);
        cer_ratio_smf.put(RatioDataRecord.Field.gapLowerBarrierRelative, RatioFieldDescription.mdpsGapLowerBarrierRelative);
        cer_ratio_smf.put(RatioDataRecord.Field.gapUpperBarrier, RatioFieldDescription.mdpsGapUpperBarrier);
        cer_ratio_smf.put(RatioDataRecord.Field.gapUpperBarrierRelative, RatioFieldDescription.mdpsGapUpperBarrierRelative);
        FIELDNAMES_BY_PERMISSION.put("CER-RATIO-SMF", cer_ratio_smf);

        final Map<RatioDataRecord.Field, Field> cer_ratio_sedex = new HashMap<>();
        cer_ratio_sedex.put(RatioDataRecord.Field.typeKey, RatioFieldDescription.typeKeySEDEX);
        cer_ratio_sedex.put(RatioDataRecord.Field.type, RatioFieldDescription.typeSEDEX);
        cer_ratio_sedex.put(RatioDataRecord.Field.issuername, RatioFieldDescription.sedexIssuerName);
        cer_ratio_sedex.put(RatioDataRecord.Field.strike, RatioFieldDescription.sedexStrike);
        cer_ratio_sedex.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.sedexIssueDate);
        cer_ratio_sedex.put(RatioDataRecord.Field.expirationDate, RatioFieldDescription.sedexExpires);
        FIELDNAMES_BY_PERMISSION.put("CER-STATIC-SEDEX", cer_ratio_sedex);

        final Map<RatioDataRecord.Field, Field> bnd = new HashMap<>();
        bnd.put(RatioDataRecord.Field.notActive, RatioFieldDescription.wmNotActive);
        bnd.put(RatioDataRecord.Field.name, RatioFieldDescription.name);
        bnd.put(RatioDataRecord.Field.wkn, RatioFieldDescription.wkn);
        bnd.put(RatioDataRecord.Field.isin, RatioFieldDescription.isin);
        bnd.put(RatioDataRecord.Field.currency, RatioFieldDescription.currency);
        bnd.put(RatioDataRecord.Field.issueCurrency, RatioFieldDescription.wmIssueCurrency);
        bnd.put(RatioDataRecord.Field.market, RatioFieldDescription.vwdMarket);
        bnd.put(RatioDataRecord.Field.price, RatioFieldDescription.lastPrice);
        bnd.put(RatioDataRecord.Field.coupon, RatioFieldDescription.wmCoupon);
        bnd.put(RatioDataRecord.Field.nominalInterest, RatioFieldDescription.wmNominalInterest);
        bnd.put(RatioDataRecord.Field.expirationDate, RatioFieldDescription.wmExpirationDate);
        bnd.put(RatioDataRecord.Field.issuername, RatioFieldDescription.issuerName);
        bnd.put(RatioDataRecord.Field.couponType, RatioFieldDescription.wmCoupontype);
        bnd.put(RatioDataRecord.Field.country, RatioFieldDescription.country);
        bnd.put(RatioDataRecord.Field.bondType, RatioFieldDescription.wmBondtype);
        bnd.put(RatioDataRecord.Field.bondRank, RatioFieldDescription.wmBondRank);
        bnd.put(RatioDataRecord.Field.yieldRelativePerYear, RatioFieldDescription.yieldRelative_mdps);
        bnd.put(RatioDataRecord.Field.basePointValue, RatioFieldDescription.mdpsBasePointValue);
        bnd.put(RatioDataRecord.Field.duration, RatioFieldDescription.duration_mdps);
        bnd.put(RatioDataRecord.Field.convexity, RatioFieldDescription.mdpsConvexity);
        bnd.put(RatioDataRecord.Field.modifiedDuration, RatioFieldDescription.mdpsModifiedDuration);
        bnd.put(RatioDataRecord.Field.brokenPeriodInterest, RatioFieldDescription.mdpsBrokenPeriodInterest);
        bnd.put(RatioDataRecord.Field.interestPeriod, RatioFieldDescription.wmInterestPeriod);
        bnd.put(RatioDataRecord.Field.interestRateElasticity, RatioFieldDescription.mdpsInterestRateElasticity);
        bnd.put(RatioDataRecord.Field.changePercent, RatioFieldDescription.changePercent);
        bnd.put(RatioDataRecord.Field.performance1w, RatioFieldDescription.performance1w);
        bnd.put(RatioDataRecord.Field.performance1m, RatioFieldDescription.performance1m);
        bnd.put(RatioDataRecord.Field.performance3m, RatioFieldDescription.performance3m);
        bnd.put(RatioDataRecord.Field.performance6m, RatioFieldDescription.performance6m);
        bnd.put(RatioDataRecord.Field.performance1y, RatioFieldDescription.performance1y);
        bnd.put(RatioDataRecord.Field.performance3y, RatioFieldDescription.performance3y);
        bnd.put(RatioDataRecord.Field.performance5y, RatioFieldDescription.performance5y);
        bnd.put(RatioDataRecord.Field.performance10y, RatioFieldDescription.performance10y);
        bnd.put(RatioDataRecord.Field.performanceCurrentYear, RatioFieldDescription.performanceCurrentYear);
        bnd.put(RatioDataRecord.Field.volatilityCurrentYear, RatioFieldDescription.volatilityCurrentYear);
        bnd.put(RatioDataRecord.Field.volatility3m, RatioFieldDescription.volatility3m);
        bnd.put(RatioDataRecord.Field.volatility6m, RatioFieldDescription.volatility6m);
        bnd.put(RatioDataRecord.Field.volatility1y, RatioFieldDescription.volatility1y);
        bnd.put(RatioDataRecord.Field.volatility3y, RatioFieldDescription.volatility3y);
        bnd.put(RatioDataRecord.Field.pari, RatioFieldDescription.pari);
        bnd.put(RatioDataRecord.Field.issuerCategory, RatioFieldDescription.issuerCategory);
        bnd.put(RatioDataRecord.Field.totalVolume, RatioFieldDescription.totalVolume);
        bnd.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.issueDate);
        bnd.put(RatioDataRecord.Field.averageVolume1w, RatioFieldDescription.averageVolume1w);
        bnd.put(RatioDataRecord.Field.turnoverDay, RatioFieldDescription.turnoverDay);
        bnd.put(RatioDataRecord.Field.pibAvailable, RatioFieldDescription.dzPib);
        bnd.put(RatioDataRecord.Field.lei, RatioFieldDescription.lei);
        bnd.put(RatioDataRecord.Field.minimumInvestmentAmount, RatioFieldDescription.minAmountOfTransferableUnit);
        bnd.put(RatioDataRecord.Field.averagePrice1w, RatioFieldDescription.averagePrice1w);
        bnd.put(RatioDataRecord.Field.averagePrice1m, RatioFieldDescription.averagePrice1m);
        bnd.put(RatioDataRecord.Field.averagePrice3m, RatioFieldDescription.averagePrice3m);
        bnd.put(RatioDataRecord.Field.averagePrice6m, RatioFieldDescription.averagePrice6m);
        bnd.put(RatioDataRecord.Field.averagePrice1y, RatioFieldDescription.averagePrice1y);
        bnd.put(RatioDataRecord.Field.averagePrice3y, RatioFieldDescription.averagePrice3y);
        bnd.put(RatioDataRecord.Field.averagePrice5y, RatioFieldDescription.averagePrice5y);
        bnd.put(RatioDataRecord.Field.averagePrice10y, RatioFieldDescription.averagePrice10y);
        FIELDNAMES.put(BND, bnd);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> bndFieldsRatingMoodys = new HashMap<>();
        bndFieldsRatingMoodys.put(RatioDataRecord.Field.ratingMoodys, RatioFieldDescription.ratingMoodysLongTerm);
        bndFieldsRatingMoodys.put(RatioDataRecord.Field.ratingMoodysShortTerm, RatioFieldDescription.ratingMoodysShortTerm);
        bndFieldsRatingMoodys.put(RatioDataRecord.Field.ratingMoodysShortTermDate, RatioFieldDescription.ratingMoodysShortTermDate);
        bndFieldsRatingMoodys.put(RatioDataRecord.Field.ratingMoodysShortTermAction, RatioFieldDescription.ratingMoodysShortTermAction);
        bndFieldsRatingMoodys.put(RatioDataRecord.Field.ratingMoodysShortTermSource, RatioFieldDescription.ratingMoodysShortTermSource);
        bndFieldsRatingMoodys.put(RatioDataRecord.Field.ratingMoodysLongTerm, RatioFieldDescription.ratingMoodysLongTerm);
        bndFieldsRatingMoodys.put(RatioDataRecord.Field.ratingMoodysLongTermDate, RatioFieldDescription.ratingMoodysLongTermDate);
        bndFieldsRatingMoodys.put(RatioDataRecord.Field.ratingMoodysLongTermAction, RatioFieldDescription.ratingMoodysLongTermAction);
        bndFieldsRatingMoodys.put(RatioDataRecord.Field.ratingMoodysLongTermSource, RatioFieldDescription.ratingMoodysLongTermSource);
        FIELDNAMES_BY_PERMISSION.put("RATING_MOODYS", bndFieldsRatingMoodys);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> bndFieldsRatingFitch = new HashMap<>();
        bndFieldsRatingFitch.put(RatioDataRecord.Field.ratingFitchShortTerm, RatioFieldDescription.ratingFitchShortTerm);
        bndFieldsRatingFitch.put(RatioDataRecord.Field.ratingFitchShortTermDate, RatioFieldDescription.ratingFitchShortTermDate);
        bndFieldsRatingFitch.put(RatioDataRecord.Field.ratingFitchShortTermAction, RatioFieldDescription.ratingFitchShortTermAction);
        bndFieldsRatingFitch.put(RatioDataRecord.Field.ratingFitchLongTerm, RatioFieldDescription.ratingFitchLongTerm);
        bndFieldsRatingFitch.put(RatioDataRecord.Field.ratingFitchLongTermDate, RatioFieldDescription.ratingFitchLongTermDate);
        bndFieldsRatingFitch.put(RatioDataRecord.Field.ratingFitchLongTermAction, RatioFieldDescription.ratingFitchLongTermAction);
        FIELDNAMES_BY_PERMISSION.put("RATING_FITCH", bndFieldsRatingFitch);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> bndFieldsRatingSnP = new HashMap<>();
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPShortTerm, RatioFieldDescription.ratingSnPShortTerm);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPShortTermDate, RatioFieldDescription.ratingSnPShortTermDate);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPShortTermAction, RatioFieldDescription.ratingSnPShortTermAction);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLongTerm, RatioFieldDescription.ratingSnPLongTerm);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLongTermDate, RatioFieldDescription.ratingSnPLongTermDate);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLongTermAction, RatioFieldDescription.ratingSnPLongTermAction);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLongTermRegulatoryId, RatioFieldDescription.ratingSnPLongTermRegulatoryId);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLongTermQualifier, RatioFieldDescription.ratingSnPLongTermQualifier);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLocalLongTerm, RatioFieldDescription.ratingSnPLocalLongTerm);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLocalLongTermDate, RatioFieldDescription.ratingSnPLocalLongTermDate);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLocalShortTerm, RatioFieldDescription.ratingSnPLocalShortTerm);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLocalShortTermDate, RatioFieldDescription.ratingSnPLocalShortTermDate);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLongTermSource, RatioFieldDescription.ratingSnPLongTermSource);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPShortTermSource, RatioFieldDescription.ratingSnPShortTermSource);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLocalLongTermSource, RatioFieldDescription.ratingSnPLocalLongTermSource);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLocalShortTermSource, RatioFieldDescription.ratingSnPLocalShortTermSource);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLocalLongTermAction, RatioFieldDescription.ratingSnPLocalLongTermAction);
        bndFieldsRatingSnP.put(RatioDataRecord.Field.ratingSnPLocalShortTermAction, RatioFieldDescription.ratingSnPLocalShortTermAction);
        FIELDNAMES_BY_PERMISSION.put("RATING_SNP", bndFieldsRatingSnP);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> wmBndFields = new HashMap<>();
        wmBndFields.put(RatioDataRecord.Field.country, RatioFieldDescription.wmCountry);
        wmBndFields.put(RatioDataRecord.Field.issueDate, RatioFieldDescription.wmIssueDate);
        wmBndFields.put(RatioDataRecord.Field.issuername, RatioFieldDescription.wmIssuerName);
        wmBndFields.put(RatioDataRecord.Field.smallestTransferableUnit, RatioFieldDescription.wmSmallestTransferableUnit);
        wmBndFields.put(RatioDataRecord.Field.sector, RatioFieldDescription.wmSector);
        wmBndFields.put(RatioDataRecord.Field.isSpecialDismissal, RatioFieldDescription.isSpecialDismissal);
        FIELDNAMES_BY_PERMISSION.put(BND.name() + ANY_VWD_TP, wmBndFields);

        final Map<RatioDataRecord.Field, Field> opt = new HashMap<>();
        opt.put(RatioDataRecord.Field.notActive, RatioFieldDescription.wmNotActive);
        opt.put(RatioDataRecord.Field.name, RatioFieldDescription.name);
        opt.put(RatioDataRecord.Field.wkn, RatioFieldDescription.wkn);
        opt.put(RatioDataRecord.Field.isin, RatioFieldDescription.isin);
        opt.put(RatioDataRecord.Field.market, RatioFieldDescription.vwdMarket);
        opt.put(RatioDataRecord.Field.vwdsymbol, RatioFieldDescription.vwdsymbol);
        opt.put(RatioDataRecord.Field.underlyingQid, null);
        opt.put(RatioDataRecord.Field.underlyingVwdcode, null);
        opt.put(RatioDataRecord.Field.underlyingIsin, RatioFieldDescription.underlyingIsin);
        opt.put(RatioDataRecord.Field.underlyingName, RatioFieldDescription.underlyingName);
        opt.put(RatioDataRecord.Field.underlyingIid, RatioFieldDescription.underlyingIid);
        opt.put(RatioDataRecord.Field.currency, RatioFieldDescription.currency);
        opt.put(RatioDataRecord.Field.strike, RatioFieldDescription.strikePrice);
        opt.put(RatioDataRecord.Field.expirationDate, RatioFieldDescription.expires);
        opt.put(RatioDataRecord.Field.tradingMonth, RatioFieldDescription.tradingMonth);
        opt.put(RatioDataRecord.Field.isAmerican, RatioFieldDescription.isAmerican);
        opt.put(RatioDataRecord.Field.exerciseType, RatioFieldDescription.exerciseType);
        opt.put(RatioDataRecord.Field.optionType, RatioFieldDescription.osType);
        opt.put(RatioDataRecord.Field.optionCategory, RatioFieldDescription.optionCategory);
        opt.put(RatioDataRecord.Field.totalVolume, RatioFieldDescription.totalVolume);
        opt.put(RatioDataRecord.Field.delta, RatioFieldDescription.delta);
        opt.put(RatioDataRecord.Field.gamma, RatioFieldDescription.gamma);
        opt.put(RatioDataRecord.Field.rho, RatioFieldDescription.rho);
        opt.put(RatioDataRecord.Field.vega, RatioFieldDescription.vega);
        opt.put(RatioDataRecord.Field.theta, RatioFieldDescription.theta);
        opt.put(RatioDataRecord.Field.omega, RatioFieldDescription.omega);
        opt.put(RatioDataRecord.Field.thetaRelative, RatioFieldDescription.thetaRelative);
        opt.put(RatioDataRecord.Field.theta1w, RatioFieldDescription.theta1w);
        opt.put(RatioDataRecord.Field.theta1wRelative, RatioFieldDescription.theta1wRelative);
        opt.put(RatioDataRecord.Field.theta1m, RatioFieldDescription.theta1m);
        opt.put(RatioDataRecord.Field.theta1mRelative, RatioFieldDescription.theta1mRelative);
        opt.put(RatioDataRecord.Field.contractSize, RatioFieldDescription.contractSize);
        opt.put(RatioDataRecord.Field.contractValue, RatioFieldDescription.contractValue);
        opt.put(RatioDataRecord.Field.contractValueCalculated, RatioFieldDescription.contractValueCalculated);
        opt.put(RatioDataRecord.Field.generationNumber, RatioFieldDescription.generationNumber);
        opt.put(RatioDataRecord.Field.impliedVolatility, RatioFieldDescription.impliedvolatility);
        // wrong datatype...
        opt.put(RatioDataRecord.Field.versionNumber, RatioFieldDescription.versionNumber);
        opt.put(RatioDataRecord.Field.isFlex, RatioFieldDescription.isFlex);
        FIELDNAMES.put(OPT, opt);

        final Map<RatioDataRecord.Field, Field> fut = new HashMap<>();
        fut.put(RatioDataRecord.Field.notActive, RatioFieldDescription.wmNotActive);
        fut.put(RatioDataRecord.Field.name, RatioFieldDescription.name);
        fut.put(RatioDataRecord.Field.wkn, RatioFieldDescription.wkn);
        fut.put(RatioDataRecord.Field.isin, RatioFieldDescription.isin);
        fut.put(RatioDataRecord.Field.market, RatioFieldDescription.vwdMarket);
        fut.put(RatioDataRecord.Field.underlyingQid, null);
        fut.put(RatioDataRecord.Field.underlyingVwdcode, null);
        fut.put(RatioDataRecord.Field.underlyingIsin, RatioFieldDescription.underlyingIsin);
        fut.put(RatioDataRecord.Field.underlyingName, RatioFieldDescription.underlyingName);
        fut.put(RatioDataRecord.Field.underlyingIid, RatioFieldDescription.underlyingIid);
        fut.put(RatioDataRecord.Field.currency, RatioFieldDescription.currency);
        fut.put(RatioDataRecord.Field.expirationDate, RatioFieldDescription.expires);
        fut.put(RatioDataRecord.Field.totalVolume, RatioFieldDescription.totalVolume);
        fut.put(RatioDataRecord.Field.tickSize, RatioFieldDescription.tickSize);
        fut.put(RatioDataRecord.Field.tickValue, RatioFieldDescription.tickValue);
        fut.put(RatioDataRecord.Field.tickCurrency, RatioFieldDescription.tickCurrency);
        fut.put(RatioDataRecord.Field.contractValue, RatioFieldDescription.contractValue);
        fut.put(RatioDataRecord.Field.contractValueCalculated, RatioFieldDescription.contractValueCalculated);
        fut.put(RatioDataRecord.Field.contractCurrency, RatioFieldDescription.contractCurrency);
        FIELDNAMES.put(FUT, fut);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> futUnderlyingProductIid = new HashMap<>();
        futUnderlyingProductIid.put(RatioDataRecord.Field.underlyingIid, RatioFieldDescription.underlyingProductIid);
        FIELDNAMES_BY_PERMISSION.put(UNDERLYING_PRODUCT_IID, futUnderlyingProductIid);

        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> vwdRimparFields = new HashMap<>();
        vwdRimparFields.put(RatioDataRecord.Field.bisKey, RatioFieldDescription.bisKey);
        FIELDNAMES_BY_PERMISSION.put(VWD_RIMPAR_FIELDS, vwdRimparFields);

        final Map<RatioDataRecord.Field, Field> ind = new HashMap<>();
        ind.put(RatioDataRecord.Field.notActive, RatioFieldDescription.wmNotActive);
        ind.put(RatioDataRecord.Field.name, RatioFieldDescription.name);
        ind.put(RatioDataRecord.Field.wkn, RatioFieldDescription.wkn);
        ind.put(RatioDataRecord.Field.isin, RatioFieldDescription.isin);
        ind.put(RatioDataRecord.Field.price, RatioFieldDescription.lastPrice);

        ind.put(RatioDataRecord.Field.performance1w, RatioFieldDescription.performance1w);
        ind.put(RatioDataRecord.Field.performance1m, RatioFieldDescription.performance1m);
        ind.put(RatioDataRecord.Field.performance3m, RatioFieldDescription.performance3m);
        ind.put(RatioDataRecord.Field.performance6m, RatioFieldDescription.performance6m);
        ind.put(RatioDataRecord.Field.performance1y, RatioFieldDescription.performance1y);
        ind.put(RatioDataRecord.Field.performance3y, RatioFieldDescription.performance3y);
        ind.put(RatioDataRecord.Field.performance5y, RatioFieldDescription.performance5y);
        ind.put(RatioDataRecord.Field.performance10y, RatioFieldDescription.performance10y);

        ind.put(RatioDataRecord.Field.changePercentHigh1y, RatioFieldDescription.changePercentHigh52Weeks);
        ind.put(RatioDataRecord.Field.changePercentLow1y, RatioFieldDescription.changePercentLow52Weeks);
        ind.put(RatioDataRecord.Field.changePercentHighAlltime, RatioFieldDescription.changePercentAlltimeHigh);

        ind.put(RatioDataRecord.Field.averageVolume1w, RatioFieldDescription.averageVolume1w);
        ind.put(RatioDataRecord.Field.averageVolume1m, RatioFieldDescription.averageVolume1m);
        ind.put(RatioDataRecord.Field.averageVolume3m, RatioFieldDescription.averageVolume3m);
        ind.put(RatioDataRecord.Field.averageVolume6m, RatioFieldDescription.averageVolume6m);
        ind.put(RatioDataRecord.Field.averageVolume1y, RatioFieldDescription.averageVolume1y);
        ind.put(RatioDataRecord.Field.averageVolume3y, RatioFieldDescription.averageVolume3y);
        ind.put(RatioDataRecord.Field.averageVolume5y, RatioFieldDescription.averageVolume5y);
        ind.put(RatioDataRecord.Field.averageVolume10y, RatioFieldDescription.averageVolume10y);

        ind.put(RatioDataRecord.Field.volatility1w, RatioFieldDescription.volatility1w);
        ind.put(RatioDataRecord.Field.volatility1m, RatioFieldDescription.volatility1m);
        ind.put(RatioDataRecord.Field.volatility3m, RatioFieldDescription.volatility3m);
        ind.put(RatioDataRecord.Field.volatility6m, RatioFieldDescription.volatility6m);
        ind.put(RatioDataRecord.Field.volatility1y, RatioFieldDescription.volatility1y);
        ind.put(RatioDataRecord.Field.volatility3y, RatioFieldDescription.volatility3y);
        ind.put(RatioDataRecord.Field.volatility5y, RatioFieldDescription.volatility5y);
        ind.put(RatioDataRecord.Field.volatility10y, RatioFieldDescription.volatility10y);
        ind.put(RatioDataRecord.Field.volatilityCurrentYear, RatioFieldDescription.volatilityCurrentYear);

        ind.put(RatioDataRecord.Field.sharpeRatio1w, RatioFieldDescription.sharperatio1w);
        ind.put(RatioDataRecord.Field.sharpeRatio1m, RatioFieldDescription.sharperatio1m);
        ind.put(RatioDataRecord.Field.sharpeRatio3m, RatioFieldDescription.sharperatio3m);
        ind.put(RatioDataRecord.Field.sharpeRatio6m, RatioFieldDescription.sharperatio6m);
        ind.put(RatioDataRecord.Field.sharpeRatio1y, RatioFieldDescription.sharperatio1y);
        ind.put(RatioDataRecord.Field.sharpeRatio3y, RatioFieldDescription.sharperatio3y);
        ind.put(RatioDataRecord.Field.sharpeRatio5y, RatioFieldDescription.sharperatio5y);
        ind.put(RatioDataRecord.Field.sharpeRatio10y, RatioFieldDescription.sharperatio10y);

        ind.put(RatioDataRecord.Field.beta1m, RatioFieldDescription.beta1m);
        ind.put(RatioDataRecord.Field.beta1y, RatioFieldDescription.beta1y);

        ind.put(RatioDataRecord.Field.alpha1m, RatioFieldDescription.alpha1m);
        ind.put(RatioDataRecord.Field.alpha1y, RatioFieldDescription.alpha1y);
        FIELDNAMES.put(IND, ind);

        final Map<RatioDataRecord.Field, Field> zns = new HashMap<>();
        zns.put(RatioDataRecord.Field.notActive, RatioFieldDescription.wmNotActive);
        zns.put(RatioDataRecord.Field.name, RatioFieldDescription.name);
        zns.put(RatioDataRecord.Field.wkn, RatioFieldDescription.wkn);
        zns.put(RatioDataRecord.Field.isin, RatioFieldDescription.isin);
        zns.put(RatioDataRecord.Field.price, RatioFieldDescription.lastPrice);
        zns.put(RatioDataRecord.Field.znsCategory, RatioFieldDescription.znsCategory);
        zns.put(RatioDataRecord.Field.maturity, RatioFieldDescription.maturity);
        zns.put(RatioDataRecord.Field.debtRanking, RatioFieldDescription.debtRanking);
        zns.put(RatioDataRecord.Field.issuername, RatioFieldDescription.issuerName);
        zns.put(RatioDataRecord.Field.issuerType, RatioFieldDescription.issuerType);
        zns.put(RatioDataRecord.Field.restructuringRule, RatioFieldDescription.restructuringRule);
        zns.put(RatioDataRecord.Field.currency, RatioFieldDescription.currency);
        zns.put(RatioDataRecord.Field.source, RatioFieldDescription.source);
        zns.put(RatioDataRecord.Field.sector, RatioFieldDescription.sector);
        zns.put(RatioDataRecord.Field.volatility, RatioFieldDescription.volatility);
        zns.put(RatioDataRecord.Field.rating, RatioFieldDescription.rating);
        zns.put(RatioDataRecord.Field.standard, RatioFieldDescription.standard);
        zns.put(RatioDataRecord.Field.country, RatioFieldDescription.country);
        FIELDNAMES.put(ZNS, zns);

        final Map<RatioDataRecord.Field, Field> mer = new HashMap<>();
        mer.put(RatioDataRecord.Field.name, RatioFieldDescription.name);
        mer.put(RatioDataRecord.Field.market, RatioFieldDescription.vwdMarket);
        mer.put(RatioDataRecord.Field.lmeMetalCode, RatioFieldDescription.lmeMetalCode);
        mer.put(RatioDataRecord.Field.lmeExpirationDate, RatioFieldDescription.lmeExpirationDate);
        mer.put(RatioDataRecord.Field.currency, RatioFieldDescription.currency);
        mer.put(RatioDataRecord.Field.lmeComposite, RatioFieldDescription.isLMEComposite);
        mer.put(RatioDataRecord.Field.merInstrumentenTyp, RatioFieldDescription.merInstrumentenTyp);
        mer.put(RatioDataRecord.Field.merHandelsmonat, RatioFieldDescription.merHandelsmonat);
        FIELDNAMES.put(MER, mer);
    }

    /**
     * Returns the field  with the given name.
     * @param name identifies field
     * @return field or null if no such field exists.
     */
    public static Field getFieldByName(String name) {
        return FIELDS_BY_NAME.get(name.toLowerCase());
    }

    public static Field getFieldById(int id) {
        return (id >= 0 && id < FIELDS.length) ? FIELDS[id] : null;
    }

    public static boolean isInstrumentField(int id) {
        return IN.get(id);
    }

    private static List<Integer> getFieldIdsSortedByName(BitSet bitSet) {
        final List<Field> list = new ArrayList<>();
        for (int i = 0; i < bitSet.length(); i++) {
            if (bitSet.get(i)) {
                list.add(FIELDS[i]);
            }
        }
        list.sort((o1, o2) -> o1.name.compareTo(o2.name));
        final List<Integer> result = new ArrayList<>(list.size());
        result.addAll(list.stream().map(Field::id).collect(Collectors.toList()));
        return result;
    }

    public static List<Integer> getInstrumentFieldIdsSortedByName() {
        return getFieldIdsSortedByName(IN);
    }

    public static List<Integer> getQuoteRatiosFieldIdsSortedByName() {
        final BitSet bs = new BitSet();
        bs.or(QR);
        bs.or(QS);
        return getFieldIdsSortedByName(bs);
    }

    public static int getMaxFieldId() {
        return maxFieldId;
    }

    public static BitSet getInstrumentFieldids() {
        return IN;
    }

    public static BitSet getQuoteStaticFieldids() {
        return QS;
    }

    public static BitSet getQuoteRatiosFieldids() {
        return QR;
    }

    public static BitSet getEnums() {
        return ENUMS;
    }

    public static BitSet getDeprecated() {
        return DEPRECATED;
    }

    /**
     * Find the locale index position of the first existing locale out of the locales list.
     * @return the locale index or 0 if none of the locales was found.
     */
    public static int getLocaleIndex(RatioFieldDescription.Field field, List<Locale> locales) {
        try {
            return getLocaleIndexCommon(field, locales);
        } catch (LocaleNotDefinedForFieldException e) {
            return 0;
        }
    }

    /**
     * Find the locale index position of the first existing locale out of the locales list.
     * @return the locale index
     * @throws de.marketmaker.istar.ratios.RatioFieldDescription.LocaleNotDefinedForFieldException thrown if none of the locales was found
     */
    public static int getLocaleIndexOrException(RatioFieldDescription.Field field,
            List<Locale> locales)
            throws LocaleNotDefinedForFieldException {
        if (locales == null) return 0;

        return getLocaleIndexCommon(field, locales);
    }

    private static int getLocaleIndexCommon(RatioFieldDescription.Field field, List<Locale> locales)
            throws LocaleNotDefinedForFieldException {
        if (field.getLocales() == null) {
            return -1;
        }

        if (locales != null) {
            final Locale[] availableLocales = field.getLocales();
            for (final Locale locale : locales) {
                for (int i = 0; i < availableLocales.length; i++) {
                    if (availableLocales[i].getLanguage().equals(locale.getLanguage())) {
                        return i;
                    }
                }
            }

            throw new LocaleNotDefinedForFieldException(locales.toString());
        }

        throw new LocaleNotDefinedForFieldException();
    }

    public static class LocaleNotDefinedForFieldException extends Exception {
        public LocaleNotDefinedForFieldException() {
            super();
        }

        public LocaleNotDefinedForFieldException(String message) {
            super(message);
        }
    }

    public static List<Field> getStaticFields(InstrumentTypeEnum type) {
        final ArrayList<Field> list = new ArrayList<>();
        for (int i = STATIC.nextSetBit(0); i >= 0; i = STATIC.nextSetBit(i + 1)) {
            if (FIELDS[i].isApplicableFor(type)) {
                list.add(FIELDS[i]);
            }
        }
        return list;
    }

    public static void main(String[] args) {
        for (int i = 1; i < getMaxFieldId(); i++) {
            if (getFieldById(i) == null) {
                System.out.println("no field for id: " + i);
            }
        }
    }
}