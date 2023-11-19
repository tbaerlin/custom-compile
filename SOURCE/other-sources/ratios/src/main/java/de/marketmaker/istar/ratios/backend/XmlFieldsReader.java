/*
 * XmlFieldsReader.java
 *
 * Created on 16.09.2005 13:42:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.RatioEnumSetFactory;

import static de.marketmaker.istar.ratios.RatioFieldDescription.edgScore1;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class XmlFieldsReader extends DefaultHandler {

    public static final String UNION_INVEST_KVG_PREFIX = "union invest";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Matcher matcher = Pattern.compile("(.*)__(.*)").matcher("");

    /**
     * contains the fields for the currently read security
     */
    private Map<Integer, Object> fields = new HashMap<>();

    private final Map<String, TagElement> tagelements = new HashMap<>();

    private boolean errorOccured = false;

    private StringBuilder stb = new StringBuilder();

    private StaticDataCallback[] listener = null;

    private int numFields;

    private int numRows;

    private long instrumentid;

    private InstrumentTypeEnum type;

    private boolean limitTo5DecimalPlaces = true;

    private final boolean addRequiredFields;

    private final List<RatioFieldDescription.Field> staticFields;

    public XmlFieldsReader(InstrumentTypeEnum type, boolean addRequiredFields) {
        this.type = type;
        this.addRequiredFields = addRequiredFields;
        this.staticFields = RatioFieldDescription.getStaticFields(type);
    }

    public void setLimitTo5DecimalPlaces(boolean limitTo5DecimalPlaces) {
        this.limitTo5DecimalPlaces = limitTo5DecimalPlaces;
    }

    public void read(final InputStream is, StaticDataCallback[] callback) throws Exception {
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }

        this.listener = callback;

        final SAXParserFactory spf = SAXParserFactory.newInstance();
        final SAXParser parser = spf.newSAXParser();

        reset();

        final TimeTaker tt = new TimeTaker();

        try {
            parser.parse(is, this);
        } catch (Exception e) {
            this.logger.error("<read> failed", e);
        }
        is.close();

        this.logger.info("<read> #numRows=" + this.numRows + ", #numFields=" + this.numFields
                + ", took: " + tt);
    }

    public void startElement(String uri, String localName, String tagName,
            Attributes attributes) throws SAXException {
        this.stb.setLength(0);
    }

    public void endElement(String uri, String localName, String tagName) throws SAXException {
        try {
            switch (tagName) {
                case "ROW":
                    // i have finished a new quote => process
                    storeFields();
                    break;
                case "IID":
                    this.instrumentid = getCurrentLong();
                    break;
                case "INSTRUMENTID":
                    this.instrumentid = getCurrentLong();
                    break;
                default:
                    processField(tagName);
                    break;
            }
        } catch (Exception e) {
            this.logger.error("<endElement> error in " + tagName, e);
            this.errorOccured = true;
        }
    }

    private TagElement getTagElement(String tagname) {
        final TagElement te = this.tagelements.get(tagname);
        if (te != null) {
            return te;
        }

        RatioFieldDescription.Field field;
        int localeIndex;

        field = RatioFieldDescription.getFieldByName(tagname);
        if (field == null && this.matcher.reset(tagname).matches()) {
            field = RatioFieldDescription.getFieldByName(this.matcher.group(1));
            if (field != null) {
                final Locale locale = new Locale(this.matcher.group(2).toLowerCase());
                try {
                    localeIndex = RatioFieldDescription.getLocaleIndexOrException(field, Collections.singletonList(locale));
                } catch (RatioFieldDescription.LocaleNotDefinedForFieldException e) {
                    field = null;
                    localeIndex = -1;
                }
            }
            else {
                localeIndex = -1;
            }
        }
        else {
            localeIndex = (null != field && field.isLocalized()) ?
                    RatioFieldDescription.getLocaleIndex(field, Collections.singletonList(Locale.GERMAN))
                    : -1;
        }

        final TagElement nte = new TagElement(field, localeIndex);
        this.tagelements.put(tagname, nte);
        return nte;
    }

    private void processField(String tagName) {
        final TagElement te = getTagElement(tagName);

        final RatioFieldDescription.Field field = te.getField();

        if (field == null) {
            return;
        }

        Object value = null;
        if (field.type() == RatioFieldDescription.Type.NUMBER) {
            value = getCurrentLong();
        }
        else if (field.type() == RatioFieldDescription.Type.DECIMAL) {
            value = getCurrentPrice();
        }
        else if (field.type() == RatioFieldDescription.Type.STRING) {
            if (field.isLocalized()) {
                value = this.fields.get(field.id());
                if (value == null) {
                    value = new String[field.getLocales().length];
                }
                ((String[]) value)[te.getLocaleIndex()] = getCurrentString();
            }
            else {
                value = getCurrentString();
            }
        }
        else if (field.type() == RatioFieldDescription.Type.DATE) {
            value = getCurrentDate();
        }
        else if (field.type() == RatioFieldDescription.Type.BOOLEAN) {
            value = getCurrentBoolean();
        }
        else if (field.type() == RatioFieldDescription.Type.ENUMSET) {
            value = RatioEnumSetFactory.toBits(field.id(), getEnumSetString(field));
        }

        if (value == null) {
            return;
        }

        this.fields.put(field.id(), value);
    }

    private String getEnumSetString(RatioFieldDescription.Field field) {
        final String str = getCurrentString();

        if (field.id() == RatioFieldDescription.msMarketAdmission.id()) {
            // handling sales regions for morning star fund
            return MarketAdmissionUtil.iso3166Alpha3To2(str);
        }

        return str;
    }

    private Boolean getCurrentBoolean() {
        final String s = getCurrentString();
        return s.equalsIgnoreCase("j")
                || s.equalsIgnoreCase("ja")
                || s.equalsIgnoreCase("y")
                || s.equalsIgnoreCase("yes")
                || s.equalsIgnoreCase("t")
                || s.equalsIgnoreCase("true")
                || s.equalsIgnoreCase("1");
    }

    private Integer getCurrentDate() {
        final String[] tokens = getCurrentString().split("\\.");
        if (tokens.length != 3) {
            return null;
        }

        return Integer.parseInt(tokens[2]) * 10000
                + Integer.parseInt(tokens[1]) * 100
                + Integer.parseInt(tokens[0]);

    }

    private long getCurrentLong() {
        try {
            return Long.parseLong(getCurrentString());
        } catch (NumberFormatException e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getCurrentLong> failed for " + getCurrentString());
            }
        }
        return Long.MIN_VALUE;
    }

    private Long getCurrentPrice() {
        try {
            String s = getCurrentString();
            if (!StringUtils.hasText(s)) {
                return null;
            }
            final int i = s.indexOf('/');
            if (i != -1) {
                s = s.substring(0, i).trim();
            }

            if (s.indexOf('.') >= 0 && s.indexOf(',') >= 0) {
                s = s.replace(".", "");
            }

            return PriceCoder.encode(getPriceString(s.replace(',', '.')));
        } catch (NumberFormatException e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getCurrentPrice> failed for " + getCurrentString());
            }
        }
        return Long.MIN_VALUE;
    }

    private String getPriceString(String str) {
        if (!this.limitTo5DecimalPlaces) {
            return str;
        }

        final int index = str.indexOf(".");
        if (index >= 0 && str.length() - str.indexOf(".") > 6) {
            return str.substring(0, str.indexOf(".") + 6);
        }
        return str;
    }

    private String getCurrentString() {
        return this.stb.toString().trim();
    }

    public void characters(char[] chars, int start, int length) throws SAXException {
        this.stb.append(chars, start, length);
    }

    private void storeFields() {
        if (this.errorOccured) {
            reset();
            return;
        }

        this.numRows++;
        this.numFields += this.fields.size();

        if (this.instrumentid < 0) {
            this.logger.error("<storeFields> no instrumentid");
            return;
        }

        rewriteFields();

        if (this.addRequiredFields) {
            addRequiredFields();
        }

        for (final StaticDataCallback callback : this.listener) {
            callback.process(this.type, this.instrumentid, this.fields);
        }

        reset();
    }

    private boolean hasField(RatioFieldDescription.Field f) {
        return this.fields.containsKey(f.id());
    }

    private Object getField(RatioFieldDescription.Field f) {
        return this.fields.get(f.id());
    }

    private Object addField(RatioFieldDescription.Field f, Object value) {
        return this.fields.put(f.id(), value);
    }

    private void addRequiredFields() {
        this.staticFields.stream()
                .filter(field -> !hasField(field))
                .forEach(field -> addField(field, null));
    }

    /**
     * special purpose method: rewrite certain fields
     */
    private void rewriteFields() {
        if (hasField(RatioFieldDescription.ratingFeri)) {
            final String rating = (String) getField(RatioFieldDescription.ratingFeri);
            if (rating == null || "n/a".equals(rating)) {
                addField(RatioFieldDescription.ratingFeri, null);
            }
            else {
                addField(RatioFieldDescription.ratingFeri, rating.replace("(", "").replace(")", ""));
            }
        }

        if (this.type == InstrumentTypeEnum.CER || this.type == InstrumentTypeEnum.WNT) {
            addField(RatioFieldDescription.vrIssuer,
                    checkIssuer(RatioFieldDescription.issuerName, "dz bank", "wgz", "westdeutsche gen"));
            addField(RatioFieldDescription.wmVrIssuer, false);
        }
        else if (this.type == InstrumentTypeEnum.FND) {
            addField(RatioFieldDescription.vrIssuer,
                    checkIssuer(RatioFieldDescription.issuerName, "dz bank", "wgz", "westdeutsche gen"));
            addField(RatioFieldDescription.msVrIssuer, checkIssuer(RatioFieldDescription.msIssuername, UNION_INVEST_KVG_PREFIX));

            final boolean vwdVrIssuer = checkIssuer(RatioFieldDescription.vwdIssuername, UNION_INVEST_KVG_PREFIX);
            addField(RatioFieldDescription.vwdVrIssuer, vwdVrIssuer);

            final Long ratingMorningstar = (Long) getField(RatioFieldDescription.morningstars);
            final Long ratingMorningstarDZBANK = vwdVrIssuer ? ratingMorningstar : null;
            // add "copy" of morningstar rating as new field for all funds of the DZ BANK "universe", see T-45927
            addField(RatioFieldDescription.morningstarsDZBANK, ratingMorningstarDZBANK);

            // fida
            final String fidaPermissionType = (String) getField(RatioFieldDescription.fidaPermissionType);
            final boolean isPermissionR = "R".equals(fidaPermissionType);
            final boolean isPermissionI = "I".equals(fidaPermissionType);
            if (isPermissionR) {
                addField(RatioFieldDescription.fidaRatingROnly, getField(RatioFieldDescription.fidaRating));
            } else if (isPermissionI) {
                addField(RatioFieldDescription.fidaRatingIOnly, getField(RatioFieldDescription.fidaRating));
            }
        }

        if (hasField(RatioFieldDescription.issuerName)) {
            final String name = (String) getField(RatioFieldDescription.issuerName);
            if ("DZ Bank Deutsche Zentral-Genossenschaftsbank AG".equals(name)
                    || "DZ Bank".equals(name)) {
                addField(RatioFieldDescription.issuerName, "DZ BANK");
            }
            else if ("Westdeutsche Gen.-Zentralbank".equals(name)
                    || "WGZ Bank".equals(name)) {
                addField(RatioFieldDescription.issuerName, "WGZ BANK");
            }
            else if ("WGZ-Bank Luxemburg".equals(name)) {
                addField(RatioFieldDescription.issuerName, "WGZ BANK Luxemburg");
            }
        }

        if (hasField(RatioFieldDescription.smfLeverageType)) {
            final String name = (String) getField(RatioFieldDescription.smfLeverageType);
            if ("Call".equals(name)
                    || "Long".equals(name)) {
                addField(RatioFieldDescription.smfLeverageType, "C");
            }
            else if ("Put".equals(name)
                    || "Short".equals(name)) {
                addField(RatioFieldDescription.smfLeverageType, "P");
            }
        }

        addEdgTopScore();
    }

    private void addEdgTopScore() {
        if (hasField(RatioFieldDescription.edgTopClass)) {
            final Long value = getEdgTopScore();
            if (value != null) {
                addField(RatioFieldDescription.edgTopScore, value);
            }
        }
    }

    private Long getEdgTopScore() {
        final Long topClass = (Long) getField(RatioFieldDescription.edgTopClass);
        if (topClass >= 1 && topClass <= 5) {
            return getEdgScore(topClass.intValue());
        }
        this.logger.warn("<rewriteFields> invalid edgTopClass " + topClass
                + " for " + this.instrumentid + ".iid");
        this.fields.remove(RatioFieldDescription.edgTopClass.id());
        this.fields.remove(RatioFieldDescription.edgTopScore.id());
        this.fields.remove(RatioFieldDescription.edgRatingDate.id());
        for (int i = 0; i < 5; i++) {
            this.fields.remove(RatioFieldDescription.edgScore1.id() + i);
        }
        return null;
    }

    private Long getEdgScore(int i) {
        return (Long) this.fields.get((edgScore1.id() - 1) + i);
    }

    private boolean checkIssuer(RatioFieldDescription.Field field, String... strings) {
        final String s;
        if (field.isLocalized()) {
            final int localeIndex = RatioFieldDescription.getLocaleIndex(field, Collections.singletonList(Locale.GERMAN));
            final String[] localizedStrings = (String[]) getField(field);
            s = localizedStrings != null ? localizedStrings[localeIndex] : null;
        }
        else {
            s = (String) getField(field);
        }

        return checkIssuer(s, strings);
    }

    public static boolean checkIssuer(String issuername, String... strings) {
        if (!StringUtils.hasText(issuername)) {
            return false;
        }

        for (final String str : strings) {
            if (issuername.toLowerCase().contains(str)) {
                return true;
            }
        }

        return false;
    }

    private void reset() {
        this.fields = new HashMap<>();
        this.instrumentid = -1;
        this.errorOccured = false;
    }

    private static class TagElement {
        private final RatioFieldDescription.Field field;

        private final int localeIndex;

        private TagElement(RatioFieldDescription.Field field, int localeIndex) {
            this.field = field;
            this.localeIndex = localeIndex;
        }

        public RatioFieldDescription.Field getField() {
            return field;
        }

        public int getLocaleIndex() {
            return localeIndex;
        }
    }

    public static void main(String[] args) throws Exception {
        new XmlFieldsReader(InstrumentTypeEnum.FND, true)
                .read(new GZIPInputStream(new FileInputStream("/Users/tkiesgen/tmp/istar-ratios-fnd.xml.gz")),
                        new StaticDataCallback[]{
                                (type, instrumentid, fields) -> {
                                    if (instrumentid == 52336 || instrumentid == 52112) {
                                        System.out.println();
                                    }
                                }
                        });
    }
}

