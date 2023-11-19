/*
 * IndexCompositionRepository.java
 *
 * Created on 26.04.2005 06:58:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.data.screener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.ScreenerUpDownData;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.Instrument;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ScreenerRepository implements InitializingBean, ScreenerDataRepository {
    private static final Logger logger = LoggerFactory.getLogger(ScreenerRepository.class);

    private static final String SHORT = "short";

    private static final String LONG = "long";

    private final static DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

    private ActiveMonitor monitor;

    private final Map<Long, SnapRecord> snaps = new HashMap<>();

    private final Map<Integer, ScreenerConfig> confs = new HashMap<>();

    private File dataFile;

    private File dataFileAltCountry;

    private File dataFileAltGroup;

    private File dataFileUpDown;

    private File configFile;

    private File imageBasePath;

    private static final String NOT_AVAILABLE = "n/a";

    private final Map<String, ScreenerUpDownData> upDownDataByRegion = new HashMap<>();

    private static final Pattern PATTERN = Pattern.compile("<(\\w+).*?/>");

    private final ScriptEngine engine;

    public ScreenerRepository() {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("js");
    }

    public void setDataFile(File dataFile) {
        this.dataFile = dataFile;
    }

    public void setDataFileAltCountry(File dataFileAltCountry) {
        this.dataFileAltCountry = dataFileAltCountry;
    }

    public void setDataFileAltGroup(File dataFileAltGroup) {
        this.dataFileAltGroup = dataFileAltGroup;
    }

    public void setDataFileUpDown(File dataFileUpDown) {
        this.dataFileUpDown = dataFileUpDown;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    public void setImageBasePath(File imageBasePath) {
        this.imageBasePath = imageBasePath;
    }

    public void setMonitor(ActiveMonitor monitor) {
        this.monitor = monitor;
    }

    public void afterPropertiesSet() throws Exception {
        final FileResource dataResource = new FileResource(this.dataFile);
        dataResource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    readScreenerData();
                } catch (Exception e) {
                    logger.error("<readScreenerData> failed", e);
                }
            }
        });
        this.monitor.addResource(dataResource);
        readScreenerData();

        final FileResource dataResourceUpDown = new FileResource(this.dataFileUpDown);
        dataResourceUpDown.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    readUpDownData();
                } catch (Exception e) {
                    logger.error("<readUpDownData> failed", e);
                }
            }
        });
        this.monitor.addResource(dataResourceUpDown);
        readUpDownData();

        final FileResource configResource = new FileResource(this.configFile);
        dataResource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                readScreenerConfig();
            }
        });
        this.monitor.addResource(configResource);
        readScreenerConfig();
    }

    private void readUpDownData() throws Exception {
        if (this.dataFileUpDown == null) {
            logger.info("<readUpDownData> no input file defined, return");
            return;
        }

        final TimeTaker tt = new TimeTaker();

        final SAXBuilder builder = new SAXBuilder();

        final GZIPInputStream is = new GZIPInputStream(new FileInputStream(this.dataFileUpDown));
        final Document document = builder.build(is);
        is.close();

        final Map<String, Map<Boolean, List<Long>>> tmp = new HashMap<>();

        LocalDate referencedate = null;

        //noinspection unchecked
        final List<Element> rows = document.getRootElement().getChildren();
        for (Element row : rows) {
            if (referencedate == null) {
                referencedate = DTF.parseDateTime(row.getChildTextTrim("ANALYSISDATE")).toLocalDate();
            }
            final boolean up = "up".equals(row.getChildTextTrim("UPDOWN"));
            final String region = row.getChildTextTrim("UNIVERSE");
            final long iid = Long.parseLong(row.getChildTextTrim("IID"));

            Map<Boolean, List<Long>> updownMap = tmp.get(region);
            if (updownMap == null) {
                updownMap = new HashMap<>();
                tmp.put(region, updownMap);
            }

            List<Long> iids = updownMap.get(up);
            if (iids == null) {
                iids = new ArrayList<>();
                updownMap.put(up, iids);
            }

            iids.add(iid);
        }


        final Map<String, ScreenerUpDownData> result = new HashMap<>();
        for (Map.Entry<String, Map<Boolean, List<Long>>> entry : tmp.entrySet()) {
            final String region = entry.getKey();

            final ScreenerUpDownData data = new ScreenerUpDownData(referencedate, region,
                    entry.getValue().get(true), entry.getValue().get(false));

            result.put(region, data);

        }

        synchronized (this.upDownDataByRegion) {
            this.upDownDataByRegion.clear();
            this.upDownDataByRegion.putAll(result);

        }

        logger.info("<readUpDownData> read " + result.size() + " regions, took " + tt);
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "REC", justification = "catch all exceptions")
    private void readScreenerData() throws Exception {
        final TimeTaker tt = new TimeTaker();


        final Map<String, List<ScreenerAlternative>> mapAltGroup = this.dataFileAltGroup == null ? null : readAlternatives(this.dataFileAltGroup, "ALTGROUP");
        final Map<String, List<ScreenerAlternative>> mapAltCountry = this.dataFileAltCountry == null ? null : readAlternatives(this.dataFileAltCountry, "ALTCOUNTRY");

        final Map<Long, SnapRecord> map = readScreenerSnaps(this.dataFile, mapAltGroup, mapAltCountry);

        synchronized (this) {
            this.snaps.clear();
            this.snaps.putAll(map);
        }

        logger.info("<readScreenerData> " + map.size() + " took " + tt);

        setUpDownStars();
    }

    public static Map<Long, SnapRecord> readScreenerSnaps(File file,
            Map<String, List<ScreenerAlternative>> mapAltGroup,
            Map<String, List<ScreenerAlternative>> mapAltCountry) throws Exception {
        final Map<Long, SnapRecord> tmpSnaps = new HashMap<>();

        final SAXBuilder saxBuilder = new SAXBuilder();
        final GZIPInputStream is = new GZIPInputStream(new FileInputStream(file));
        final Document document = saxBuilder.build(is);
        is.close();

        final List rows = document.getRootElement().getChildren();
        for (final Object o : rows) {
            final Element row = (Element) o;

            long instrumentid = Long.MIN_VALUE;
            final Map<Integer, String> values = new HashMap<>();

            final List fields = row.getChildren();
            for (final Object fieldO : fields) {
                final Element field = (Element) fieldO;
                final String fieldname = field.getName();

                if (fieldname.equals("SECURITY")) {
                    instrumentid = Long.parseLong(field.getTextTrim());
                }
                else if (fieldname.startsWith("A_")) {
                    final int fieldid = ScreenerFieldDescription.getFieldByName(getFieldname(fieldname.substring(2)));
                    if (fieldid > 0) {
                        values.put(fieldid, field.getTextTrim());
                    }
                }
            }

            if (instrumentid == Long.MIN_VALUE) {
                continue;
            }

            final SnapRecord sr = toSnapRecord(values, mapAltGroup, mapAltCountry);
            tmpSnaps.put(instrumentid, sr);
        }

        return tmpSnaps;
    }

    private void setUpDownStars() {
        synchronized (this.upDownDataByRegion) {
            for (final ScreenerUpDownData data : this.upDownDataByRegion.values()) {
                for (int i = 0; i < data.getUps().size(); i++) {
                    final Long iid = data.getUps().get(i);
                    data.setUpStar(i, getIrst(iid));
                }

                for (int i = 0; i < data.getDowns().size(); i++) {
                    final Long iid = data.getDowns().get(i);
                    data.setDownStar(i, getIrst(iid));
                }
            }
        }
    }

    private Integer getIrst(Long iid) {
        if (iid == null) {
            return null;
        }
        final ScreenerData sd = getScreenerData(iid, "de");
        if (sd == null) {
            return null;
        }
        final SnapField field = sd.getField(ScreenerFieldDescription.MMF_IRST);
        return field.isDefined() ? ((Number) field.getValue()).intValue() : null;
    }

    private void readScreenerConfig() {
        try {
            final TimeTaker tt = new TimeTaker();

            final Map<Integer, ScreenerConfig> tmpConfs = new HashMap<>();

            final SAXBuilder saxBuilder = new SAXBuilder();
            final InputStream is = new FileInputStream(this.configFile);
            final Document document = saxBuilder.build(is);
            is.close();

            final List rows = document.getRootElement().getChildren();
            for (final Object o : rows) {
                final Element row = (Element) o;

                final String id = row.getAttributeValue("id");
                final int screenerFieldid = ScreenerFieldDescription.getFieldByName(getFieldname(id));
                final String name = row.getAttributeValue("name");

                final ScreenerConfig sc = new ScreenerConfig(screenerFieldid, name);
                final List rules = row.getChildren();
                for (final Object ro : rules) {
                    final Element rule = (Element) ro;
                    readRule(rule, sc);
                }

                tmpConfs.put(screenerFieldid, sc);
            }

            synchronized (this) {
                this.confs.clear();
                this.confs.putAll(tmpConfs);
            }

            logger.info("<readScreenerConfig> took " + tt);
        } catch (Exception e) {
            logger.error("<readScreenerConfig> failed", e);
        }
    }

    private void readRule(final Element rule, final ScreenerConfig sc) throws Exception {
        final String condition = rule.getChildTextTrim("condition");
        final String imageName = rule.getChildTextTrim("imageName");

        final ScreenerRule screenerRule = new ScreenerRule();
        screenerRule.setCondition(condition);
        if (StringUtils.hasText(imageName)) {
            final URL imageUrl = new URL("http://$path$/" + imageName);
            screenerRule.setImageUrl(imageUrl);
            screenerRule.setImage(getEncodedImage(imageName));
        }

        final List texts = rule.getChildren();
        for (final Object to : texts) {
            final Element text = (Element) to;
            if (SHORT.equals(text.getName())) {
                final String language = text.getAttributeValue("language");
                screenerRule.addShortText(language, text.getTextTrim());
            }
            else if (LONG.equals(text.getName())) {
                final String language = text.getAttributeValue("language");
                screenerRule.addLongText(language, text.getTextTrim());
            }
        }

        sc.addRule(screenerRule);
    }

    private String getEncodedImage(String name) throws IOException {
        File f = new File(this.imageBasePath, name);
        return ByteUtil.toBase64String(FileCopyUtils.copyToByteArray(f));
    }

    public ScreenerUpDownData getUpDownData(String region) {
        synchronized (this.upDownDataByRegion) {
            return this.upDownDataByRegion.get(region);
        }
    }

    public boolean hasScreenerData(Instrument instrument) {
        return hasScreenerData(instrument.getId());
    }

    public synchronized boolean hasScreenerData(long instrumentid) {
        return null != this.snaps.get(instrumentid);
    }

    public ScreenerData getScreenerData(Instrument instrument, String language) {
        return getScreenerData(instrument.getId(), language);
    }

    public synchronized ScreenerData getScreenerData(long instrumentid, String languageTag) {
        Locale locale = Locale.forLanguageTag(languageTag);

        final SnapRecord sr = this.snaps.get(instrumentid);

        if (sr == null) {
            return null;
        }

        final ScreenerData sd = new ScreenerData(instrumentid, locale, sr);

        for (final SnapField snapField : sr.getSnapFields()) {
            final ScreenerConfig sc = this.confs.get(snapField.getId());
            if (sc == null) {
                continue;
            }

            final ScreenerRule rule = getRule(sc, snapField);
            if (rule == null) {
                continue;
            }

            final String image = rule.getImage();
            final URL imageUrl = rule.getImageUrl();
            final String shortText = eval(sr, locale, rule.getShortText(locale.getLanguage()));
            final String longText = eval(sr, locale, rule.getLongText(locale.getLanguage()));
            final ScreenerData.EvaluatedRule er = new ScreenerData.EvaluatedRule(shortText, longText);
            if (image != null) {
                er.setImageUrl(imageUrl);
                er.setEncodedImage(image);
            }

            sd.add(snapField, er);
        }

        return sd;
    }

    public synchronized List<String> getIsinsWithPdf() {
        final List<String> isinsWithPdf = new ArrayList<>();

        for (final SnapRecord snapRecord : this.snaps.values()) {
            final Object pdf = snapRecord.getField(ScreenerFieldDescription.MMF_PDF).getValue();
            if ("True".equals(pdf)) {
                isinsWithPdf.add((String) snapRecord.getField(ScreenerFieldDescription.MMF_ISIN).getValue());
            }
        }

        return isinsWithPdf;
    }


    private String eval(SnapRecord sr, Locale locale, String text) {
        if (text == null) {
            return null;
        }

        final StringBuffer sb = new StringBuffer(text.length() * 2);
        try {
            final Matcher matcher = PATTERN.matcher(text);

            while (matcher.find()) {
                final boolean isPercent = matcher.group(0).endsWith("pct=\"yes\"/>");

                final String id = matcher.group(1);
                final String replacee = getReplacement(sr, locale, text, id);

                matcher.appendReplacement(sb, replacee);
                sb.append(isPercent ? "%" : "");
            }
            matcher.appendTail(sb);

        } catch (Exception e) {
            logger.warn("<eval> failed for " + text, e);
        }

        return sb.toString();
    }

    private String getReplacement(SnapRecord sr, Locale locale, String text, String id) {
        if ("warnsign".equals(id)) {
            return "(!)";
        }
        final SnapField field = sr.getField(getFieldname(id));
        if (!field.isDefined()) {
            logger.warn("<eval> no field " + id + " for " + text);
        }
        return field.isDefined() ? getFormattedValue(field, locale) : getNaValue(id);
    }

    private DecimalFormat getPriceFormat(Locale locale) {
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(locale);
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(5);
        return format;
    }

    private DecimalFormat getFloatFormat(Locale locale) {
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(locale);
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(5);
        return format;
    }

    private DateFormat getDateFormat(Locale locale) {
        return SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, locale);
    }

    private String getNaValue(String id) {
        if ("warnsign".equals(id)) {
            return "";
        }
        return "n/a";
    }

    private static String getFieldname(final String id) {
        return "MMF_" + id.toUpperCase().replaceAll("-", "_");
    }

    private ScreenerRule getRule(final ScreenerConfig sc, final SnapField snapField) {
        Number value = getValue(snapField);
        Bindings b = engine.createBindings();
        b.put("value", value);

        for (final ScreenerRule rule : sc.getRules()) {
            String cond = rule.getCondition().replace("final boolean condition = ", "");
            try {
                if (engine.eval(cond, b) == Boolean.TRUE) {
                    return rule;
                }
            } catch (ScriptException e) {
                logger.error("<getRule> failed for '" + cond + "' with value=" + value, e);
            }
        }
        return null;
    }

    private Number getValue(SnapField snapField) {
        Number result = (Number) snapField.getValue();
        if (ScreenerFieldDescription.TYPES[snapField.getId()] == ScreenerFieldDescription.TYPE_PRICE) {
            return result.longValue() / 100000d;
        }
        return result;
    }

    private static SnapRecord toSnapRecord(Map<Integer, String> values,
            Map<String, List<ScreenerAlternative>> mapAltGroup,
            Map<String, List<ScreenerAlternative>> mapAltCountry) {
        final List<Integer> fieldids = new ArrayList<>(values.keySet());
        fieldids.sort(null);

        final int[] index = new int[fieldids.size() + 1];
        final int[] offset = new int[fieldids.size() + 1];
        int numBytes = 0;

        for (int i = 0; i < index.length - 1; i++) {
            final int fieldid = fieldids.get(i);
            index[i] = fieldid;
            offset[i] = numBytes;

            numBytes += getLength(fieldid);
        }
        index[index.length - 1] = Integer.MAX_VALUE;
        offset[index.length - 1] = numBytes;

        final byte[] data = new byte[numBytes];
        final ByteBuffer bb = ByteBuffer.wrap(data);
        for (int i = 0; i < index.length - 1; i++) {
            final int fieldid = fieldids.get(i);
            final String value = values.get(fieldid);
            bb.position(offset[i]);

            try {
                switch (ScreenerFieldDescription.TYPES[fieldid]) {
//               case ScreenerFieldDescription.TYPE_UNUM2:
                    case ScreenerFieldDescription.TYPE_UNUM4:
                        bb.putInt(Integer.parseInt(value));
                        break;
//                case ScreenerFieldDescription.TYPE_TIME:
                    case ScreenerFieldDescription.TYPE_DATE:
                        bb.putInt(DateUtil.dateToYyyyMmDd(DTF.parseDateTime(value).toDate()));
                        break;
                    case ScreenerFieldDescription.TYPE_PRICE:
//                case ScreenerFieldDescription.TYPE_TIMESTAMP:
                        bb.putLong(Math.round(Double.parseDouble(value) * 100000d * ScreenerFieldDescription.PRICE_FACTORS[fieldid]));
                        break;
                    case ScreenerFieldDescription.TYPE_UCHAR:
//                case ScreenerFieldDescription.TYPE_CHARV:
                        final byte[] strBytes = value.getBytes();
                        bb.put(strBytes);
                        break;
                }
            } catch (Exception e) {
                logger.warn("<toSnapRecord> failed", e);
            }
        }

        final String isin = values.get(ScreenerFieldDescription.MMF_ISIN);
        final String currency = values.get(ScreenerFieldDescription.MMF_CCY);
        final String country = values.get(ScreenerFieldDescription.MMF_COUNTRY);
        final String key = isin + "-" + currency + "-" + country;
        final List<ScreenerAlternative> altGroup = mapAltGroup == null ? null : mapAltGroup.get(key);
        final List<ScreenerAlternative> altCountry = mapAltCountry == null ? null : mapAltCountry.get(key);

        return new SnapRecordScreener(index, offset, data, altGroup, altCountry);
    }

    private String getFormattedValue(SnapField field, Locale locale) {
        if (!field.isDefined()) {
            return NOT_AVAILABLE;
        }
        switch (ScreenerFieldDescription.FORMATTING_HINTS[field.getId()]) {
            case ScreenerFieldDescription.FH_NUMBER:
                return field.getValue().toString();
            case ScreenerFieldDescription.FH_DATE:
                final int date = (Integer) field.getValue();
                return getDateFormat(locale).format(DateUtil.yyyyMmDdToDate(date));
            case ScreenerFieldDescription.FH_FLOAT:
                final double value = (Long) field.getValue();
                return getFloatFormat(locale).format(value / 100000d);
            case ScreenerFieldDescription.FH_TEXT:
                return field.getValue().toString();
            case ScreenerFieldDescription.FH_PRICE:
                final double price = (Long) field.getValue();
                return getPriceFormat(locale).format(price / 100000d);
        }
        return NOT_AVAILABLE;
    }


    private Map<String, List<ScreenerAlternative>> readAlternatives(final File file,
            final String type) throws ParserConfigurationException, SAXException, IOException {
        final AlternativesXmlReader axr = new AlternativesXmlReader(type);
        final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        final SAXParser saxParser = saxParserFactory.newSAXParser();
        final InputStream inputStream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
        saxParser.parse(inputStream, axr);
        inputStream.close();
        return axr.getMapAlternatives();
    }


    private static class AlternativesXmlReader extends DefaultHandler {
        private final String type;

        private final CharArrayWriter caw = new CharArrayWriter();

        private Map<String, String> map = new HashMap<>(6);

        private Map<String, List<ScreenerAlternative>> mapAlternatives = new HashMap<>();

        public AlternativesXmlReader(String type) {
            this.type = type;
        }

        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            this.caw.reset();
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.startsWith("A_") || "SECURITYID".equals(qName) || "NAME".equals(qName)) {
                this.map.put(qName.substring(2), this.caw.toString());
            }
            else if ("ROW".equals(qName)) {
                final String isin = this.map.get("ISIN");
                final String currency = this.map.get("CCY");
                final String country = this.map.get("COUNTRY");
                final long altSecurityId = Long.parseLong(this.map.get(this.type + "_SECURITYID"));
                final String altIsin = this.map.get(this.type + "_ISIN");
                final String altName = this.map.get(this.type + "_NAME");
                final String altCurrency = this.map.get(this.type + "_CCY");
                final String altCountry = this.map.get(this.type + "_COUNTRY");
                final String key = isin + "-" + currency + "-" + country;
                List<ScreenerAlternative> list = this.mapAlternatives.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                    this.mapAlternatives.put(key, list);
                }
                list.add(new ScreenerAlternative(altSecurityId, altIsin, altName, altCurrency, altCountry));
            }
        }

        public void characters(char ch[], int start, int length) throws SAXException {
            this.caw.write(ch, start, length);
        }

        public Map<String, List<ScreenerAlternative>> getMapAlternatives() {
            return this.mapAlternatives;
        }
    }


    private static int getLength(int fieldid) {
        // TODO: generalize w/ VwdF...
        switch (ScreenerFieldDescription.TYPES[fieldid]) {
//            case ScreenerFieldDescription.TYPE_UNUM2:
//            case ScreenerFieldDescription.TYPE_TIME:
            case ScreenerFieldDescription.TYPE_UNUM4:
            case ScreenerFieldDescription.TYPE_DATE:
                return 4;
            case ScreenerFieldDescription.TYPE_PRICE:
//            case ScreenerFieldDescription.TYPE_TIMESTAMP:
                return 8;
        }
        return ScreenerFieldDescription.LENGTHS[fieldid];
    }


    public static void main(String[] args) throws Exception {
        ActiveMonitor am = new ActiveMonitor();
        am.setFrequency(5000);
        am.start();

        final ScreenerRepository r = new ScreenerRepository();

        r.setMonitor(am);
        r.setDataFileUpDown(LocalConfigProvider.getProductionDir("var/data/provider/istar-screener-updown.xml.gz"));
        r.setDataFile(LocalConfigProvider.getProductionDir("var/data/provider/istar-screener.xml.gz"));
        r.setDataFileAltGroup(LocalConfigProvider.getProductionDir("var/data/provider/istar-screener-altgroup.xml.gz"));
        r.setDataFileAltCountry(LocalConfigProvider.getProductionDir("var/data/provider/istar-screener-altcountry.xml.gz"));
        r.setConfigFile(LocalConfigProvider.getProductionDir("var/data/provider/screener.xconf"));
        r.setImageBasePath(new File(LocalConfigProvider.getIstarSrcDir(), "/merger/src/conf/dmxml-1/images/screener"));
        r.afterPropertiesSet();

        printInstrument(r);

        printIsinsWithPdf(r);

        System.out.println(r.getUpDownData("EP"));
        System.out.println(r.getUpDownData("US"));

        Thread.sleep(Long.MAX_VALUE);
    }

    private static void printInstrument(ScreenerRepository r) {
        final int instrumentId = 34742;
        System.out.println("***************** display screener data of instrument " + instrumentId);

        final ScreenerData screenerData = r.getScreenerData(instrumentId, "de");
        if (screenerData == null) {
            return;
        }
        final TimeTaker tt = new TimeTaker();
        System.out.println(tt);

        for (final SnapField snapField : screenerData.getFields()) {
            System.out.println(snapField.getName() + " => " + r.getFormattedValue(snapField, Locale.GERMANY));
            final ScreenerData.EvaluatedRule er = screenerData.getEvaluatedRule(snapField.getId());
            if (er != null) {
                System.out.println("   " + er.getShortText());
                System.out.println("   " + er.getLongText());
                System.out.println("   " + er.getEncodedImage());
                System.out.println("   " + er.getImageUrl());
            }
        }

        System.out.println("alternatives in group:");
        for (final ScreenerAlternative screenerAlternative : screenerData.getAltGroup()) {
            System.out.print("   " + screenerAlternative.getIsin());
            System.out.print(" " + screenerAlternative.getCurrency());
            System.out.println(" " + screenerAlternative.getCountry());
        }

        System.out.println("alternatives in country:");
        for (final ScreenerAlternative screenerAlternative : screenerData.getAltCountry()) {
            System.out.print("   " + screenerAlternative.getIsin());
            System.out.print(" " + screenerAlternative.getCurrency());
            System.out.println(" " + screenerAlternative.getCountry());
        }
    }


    private static void printIsinsWithPdf(ScreenerRepository r) {
        System.out.println("***************** display all isins with pdf");
        final long start = System.currentTimeMillis();
        final List<String> isinsWithPdf = r.getIsinsWithPdf();
        final long duration = System.currentTimeMillis() - start;
        System.out.println("returned " + isinsWithPdf.size() + " of " + r.snaps.size() + " isins in " + duration + " ms");
        System.out.println(isinsWithPdf);
    }

    private static class ScreenerConfig {
        private final int fieldid;

        private final String name;

        private final List<ScreenerRule> rules = new ArrayList<>();

        public ScreenerConfig(int fieldid, String name) {
            this.fieldid = fieldid;
            this.name = name;
        }

        public void addRule(ScreenerRule rule) {
            this.rules.add(rule);
        }

        public List<ScreenerRule> getRules() {
            return rules;
        }

        public String toString() {
            return "ScreenerConfig[fieldid=" + fieldid
                    + ", name=" + name
                    + ", rules=" + rules
                    + "]";
        }
    }

    private static class ScreenerRule {
        private String condition;

        private String image = null;

        private URL imageUrl = null;

        private final Map<String, String> text = new HashMap<>();

        public ScreenerRule() {
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public void setImageUrl(URL imageUrl) {
            this.imageUrl = imageUrl;
        }

        public void addShortText(String language, String text) {
            this.text.put(SHORT + "-" + language, text);
        }

        public void addLongText(String language, String text) {
            this.text.put(LONG + "-" + language, text);
        }

        public String getShortText(String language) {
            return this.text.get(SHORT + "-" + language);
        }

        public String getLongText(String language) {
            return this.text.get(LONG + "-" + language);
        }

        public String getCondition() {
            return condition;
        }

        public String getImage() {
            return image;
        }

        public URL getImageUrl() {
            return imageUrl;
        }

        public String toString() {
            return "ScreenerRule[condition=" + condition
                    + ", imageUrl=" + imageUrl
                    + ", image=" + image
                    + ", text=" + text
                    + "]";
        }
    }
}
