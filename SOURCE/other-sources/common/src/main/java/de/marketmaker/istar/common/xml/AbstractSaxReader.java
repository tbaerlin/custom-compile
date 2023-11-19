/*
 * AbstractSaxReader.java
 *
 * Created on 11.08.2006 18:38:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.xml;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.marketmaker.istar.common.monitor.MeterSupport;
import de.marketmaker.istar.common.util.ThroughputLimiter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractSaxReader extends DefaultHandler {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private static final MathContext MC = new MathContext(8, RoundingMode.HALF_EVEN);

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected boolean errorOccured = false;

    protected StringBuilder stb = new StringBuilder();

    protected Map<String, String> strmap = new HashMap<>();

    protected Map<String, DateTime> dateTimeCache = new HashMap<>();

    private final Map<String, BigDecimal> bigDecimalCache = new HashMap<>();

    protected int countStrings;

    protected long countChars;

    protected final ThroughputLimiter limiter = new ThroughputLimiter(30000);

    private final Multiset<String> notParsed = HashMultiset.create();

    private MeterRegistry meterRegistry;

    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void ackAction() {
        this.limiter.ackAction();
    }

    protected String getCurrentString(boolean useCache) {
        return useCache ? getCurrentString() : doGetCurrentString();
    }

    protected String getCurrentString() {
        final String str = doGetCurrentString();

        this.countChars += str.length();
        this.countStrings++;

        // This is basiscally String.intern() but these strings won't live
        // until the JVM is terminated as String.intern() strings do
        return this.strmap.computeIfAbsent(str, Function.identity());
    }

    protected boolean hasText() {
        for (int i = 0; i < this.stb.length(); i++) {
            if (!Character.isWhitespace(this.stb.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private String doGetCurrentString() {
        return this.stb.toString().trim();
    }

    protected long getCurrentLong() {
        return Long.parseLong(doGetCurrentString());
    }

    protected int getCurrentInt() {
        return Integer.parseInt(doGetCurrentString());
    }

    protected float getCurrentFloat() {
        return Float.parseFloat(doGetCurrentString());
    }

    protected double getCurrentDouble() {
        return Double.parseDouble(doGetCurrentString());
    }

    protected boolean getCurrentBoolean() {
//        return this.stb.length() == 4 && this.stb.charAt(0) == 't';
        final String str = doGetCurrentString();
        return StringUtils.hasText(str) ? Boolean.valueOf(str) : false;
    }

    protected BigDecimal getCurrentPercent() {
        return toPercent(doGetCurrentString());
    }

    protected BigDecimal toPercent(String str) {
        return StringUtils.hasText(str) ? new BigDecimal(str).divide(ONE_HUNDRED, MC) : null;
    }

    protected BigDecimal getCurrentBigDecimal() {
        return getCurrentBigDecimal(false);
    }

    protected BigDecimal getCurrentBigDecimal(boolean fromCache) {
        final String str = doGetCurrentString();
        if (!fromCache) {
            return StringUtils.hasText(str) ? new BigDecimal(str) : null;
        }
        if (!StringUtils.hasText(str)) {
            return null;
        }
        if (this.bigDecimalCache.containsKey(str)) {
            return this.bigDecimalCache.get(str);
        }
        final BigDecimal result = new BigDecimal(str);
        this.bigDecimalCache.put(str, result);
        return result;
    }

    protected DateTime getCurrentDateTime(DateTimeFormatter fmt) {
        return getCurrentDateTime(fmt, false);
    }

    protected DateTime getCurrentDateTime(DateTimeFormatter fmt, boolean cache) {
        final String key = doGetCurrentString();
        if (!StringUtils.hasText(key)) {
            return null;
        }
        if (!cache) {
            return fmt.parseDateTime(key);
        }
        final DateTime cached = this.dateTimeCache.get(key);
        if (cached != null) {
            return cached;
        }
        final DateTime result = fmt.parseDateTime(key);
        this.dateTimeCache.put(key, result);
        return result;
    }

    public void characters(char[] chars, int start, int length) throws SAXException {
        this.stb.append(chars, start, length);
    }

    public void startElement(String uri, String localName, String tagName,
            Attributes attributes) throws SAXException {
        resetCurrentString();
    }

    protected void resetCurrentString() {
        this.stb.setLength(0);
    }

    /**
     * Parses xml from the given file
     *
     * @param f file, assumed to be gzipped if name ends with .gz
     * @throws Exception if reading/processing fails
     */
    public void read(final File f) throws Exception {
        final Sample sample = Timer.start();
        try {
            if (f.getName().endsWith(".gz")) {
                read(new GZIPInputStream(new FileInputStream(f)));
            } else {
                read(new FileInputStream(f));
            }
        } finally {
            MeterSupport.stopSample(sample, this.meterRegistry,
                "sax.reader", Tags.of("file.name", f.getName()));
        }
    }

    /**
     * Parses xml from the given stream, finally closes the stream
     * @param is data source
     * @throws Exception if reading/processing fails
     */
    private void read(final InputStream is) throws Exception {
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        final SAXParser parser = spf.newSAXParser();

        reset();

        try {
            parser.parse(is, this);
        } catch (org.xml.sax.SAXParseException s) {
            this.logger.error("<readMasterDataFund> SAXParseException at line/column " + s.getLineNumber() + "/" + s.getColumnNumber());
            throw s;
        } finally {
            is.close();
        }

        if (notParsed.isEmpty()) {
            this.logger.info("<read> data in file completely read");
        }
        else {
            this.logger.warn("<read> data in file but not read " + notParsed);
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<read> this.countChars = " + this.countChars);
            this.logger.debug("<read> countStrings = " + countStrings);
            this.logger.debug("<read> uniqueStrings = " + strmap.size());
        }

        this.strmap = new HashMap<>();
    }

    protected void notParsed(String tagName) {
        notParsed.add(tagName);
    }

    abstract protected void reset();
}
