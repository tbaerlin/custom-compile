/*
 * IstarMdpExportReader.java
 *
 * Created on 07.10.2010 10:55:46
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.xml;

import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Reader that simplifies reading mdp exports. XML MDP exports have the following structure:
 * <pre>
 * &lt;?xml version = '1.0' encoding='UTF-8'?>
 * &lt;ROWS>
 *  &lt;ROW num="1">
 *    &lt;<em>COLNAME-1</em>>22162593&lt;/<em>COLNAME-1</em>>
 *    &lt;<em>COLNAME-2</em>>20032434&lt;/<em>COLNAME-2</em>>
 *    &lt;...
 *    &lt;<em>COLNAME-n</em>>12563130&lt;/<em>COLNAME-n</em>>
 *  &lt;/ROW>
 *  &lt;ROW num="2">
 *   ...
 * </pre>
 * This class collects the columns internally and whenever a row has been read completely,
 * {@link #handleRow()} is invoked. Subclasses can retrieve column values by calling
 * {@link #get(String)}, {@link #getLong(String)}, {@link #getInt(String)}.
 * <p>
 * Internally, a SAXReader is used to be able to handle large documents w/o causing memory problems.
 *
 * @author oflege
 */
@NotThreadSafe
public abstract class IstarMdpExportReader<V> {
    protected class MdpSaxReader extends AbstractSaxReader {
        private boolean inRow = false;

        protected void reset() {
            row.clear();
        }

        public void startElement(String uri, String localName, String tagName,
                Attributes attributes) throws SAXException {
            if (rowTag.equals(tagName)) {
                this.inRow = true;
            }
            else {
                resetCurrentString();
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(rowTag)) {
                onRow();
                if (limited) {
                    ackAction();
                }
                this.inRow = false;
                reset();
            }
            else if (this.inRow) {
                row.put(qName, getCurrentString(cachedColumns.contains(qName)));
            }
            else {
                endNonRowElement(uri, localName, qName);
            }
        }

        protected void endNonRowElement(String uri, String localName, String qName) {
            // empty
        }

        public void endDocument() throws SAXException {
            IstarMdpExportReader.this.endDocument();
        }
    }

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private static final MathContext MC = new MathContext(8, RoundingMode.HALF_EVEN);

    private static final String DEFAULT_ROW_TAG = "ROW";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, String> row = new HashMap<>();

    protected String rowTag = DEFAULT_ROW_TAG;

    private DateTimeFormatter defaultDateTimeFormatter = DateTimeFormat.forPattern("dd.MM.yyyy");

    private int rowNum = 0;
    
    private int numRowsWithErrors = 0;
    
    private Exception firstRowError = null;

    private int firstRowErrorRow = -1;

    private final Set<String> cachedColumns;

    private final boolean limited;

    // use a delegate for reading; if this class would extend AbstractSaxReader, subclasses
    // would have access to all sorts of methods that make no sense in the context
    // if row-based data processing
    private final AbstractSaxReader delegate = createMdpSaxReader();

    /**
     * Creates a subclass/instance of MdpSaxReader; must not access any fields in this object
     * as they are not yet initialized.
     * @return
     */
    protected MdpSaxReader createMdpSaxReader() {
        return new MdpSaxReader();
    }

    /**
     * Creates a new reader
     * @param limited whether the number of rows processed per second should be limited.
     * @param cachedColumns names of columns whose values should be cached; whenever a value in a
     * column is the same for many rows and that value is to be stored for each row, it pays off to
     * specify the column's name here. If, however, the value is turned into another kind of object
     * before it is stored, caching it does not make any sense.
     */
    protected IstarMdpExportReader(boolean limited, String... cachedColumns) {
        this.limited = limited;
        this.cachedColumns = new HashSet<>(Arrays.asList(cachedColumns));
    }

    /**
     * Creates a new reader with limited throughput
     * @param cachedColumns names of columns whose values should be cached; whenever a value in a
     * column is the same for many rows and that value is to be stored for each row, it pays off to
     * specify the column's name here. If, however, the value is turned into another kind of object
     * before it is stored, caching it does not make any sense.
     */
    protected IstarMdpExportReader(String... cachedColumns) {
        this(true, cachedColumns);
    }

    public void setDefaultDateTimeFormatter(DateTimeFormatter defaultDateTimeFormatter) {
        this.defaultDateTimeFormatter = defaultDateTimeFormatter;
    }

    public V read(File f) throws Exception {
        try {
            this.rowNum = 0;
            this.delegate.read(f);
            return getResult();
        } finally {
            if (this.numRowsWithErrors == 0) {
                this.logger.info("<read> " + this.rowNum + " rows from " + f.getAbsolutePath());
            }
            else {
                this.logger.error("<read> " + this.rowNum + " rows from " + f.getAbsolutePath()
                    + ", got errors for " + this.numRowsWithErrors + " rows, first error was in row " + this.firstRowErrorRow, this.firstRowError);
            }
        }
    }

    private void onRow() {
        this.rowNum++;
        try {
            handleRow();
        } catch (Exception e) {
            if (this.numRowsWithErrors++ == 0) {
                this.firstRowError = e;
                this.firstRowErrorRow = this.rowNum;
            }
        }
    }
    
    /**
     * @param name column name
     * @return value of that column in the current row; null if that column is not present
     */
    protected String get(String name) {
        return this.row.get(name);
    }

    /**
     * @param name column name
     * @return value of that column in the current row; null if that column is not present or has
     * no text; might throw NumberFormatException if column's value cannot be converted
     */
    protected Long getLong(String name) {
        final String s = get(name);
        return (StringUtils.hasText(s)) ? Long.valueOf(s) : null;
    }

    protected Long getLong(String name, String alternative) {
        final String s = get(name);
        final String a = get(alternative);
        return (StringUtils.hasText(s)) ? Long.valueOf(s) : ((StringUtils.hasText(a)) ? Long.valueOf(a) : null);
    }

    /**
     * @param name column name
     * @return value of that column in the current row; null if that column is not present or has
     * no text; might throw NumberFormatException if column's value cannot be converted
     */
    protected Integer getInt(String name) {
        final String s = get(name);
        return (StringUtils.hasText(s)) ? Integer.valueOf(s) : null;
    }

    /**
     * @param name column name
     * @return value of that column in the current row; null if that column is not present or has
     * no text; might throw NumberFormatException if column's value cannot be converted
     */
    protected Double getDouble(String name) {
        final String s = get(name);
        return (StringUtils.hasText(s)) ? Double.valueOf(s) : null;
    }

    /**
     * @param name column name
     * @return value of that column in the current row; null if that column is not present or has
     * no text; might throw NumberFormatException if column's value cannot be converted
     */
    protected double getDouble(String name, double defaultValue) {
        final String s = get(name);
        return (StringUtils.hasText(s)) ? Double.valueOf(s) : defaultValue;
    }

    /**
     * @param name column name
     * @return value of that column in the current row; null if that column is not present or has
     * no text; might throw NumberFormatException if column's value cannot be converted
     */
    protected Float getFloat(String name) {
        final String s = get(name);
        return (StringUtils.hasText(s)) ? Float.valueOf(s) : null;
    }

    /**
     * @param name column name
     * @return value of that column in the current row; null if that column is not present or has
     * no text; might throw NumberFormatException if column's value cannot be converted
     */
    protected float getFloat(String name, float defaultValue) {
        final String s = get(name);
        return (StringUtils.hasText(s)) ? Float.valueOf(s) : defaultValue;
    }

    /**
     * @param name column name
     * @return value of that column in the current row; null if that column is not present or has
     * no text; might throw NumberFormatException if column's value cannot be converted
     */
    protected BigDecimal getBigDecimal(String name) {
        final String s = get(name);
        return (StringUtils.hasText(s)) ? new BigDecimal(s) : null;
    }

    protected BigDecimal getPercent(String name) {
        final BigDecimal bd = getBigDecimal(name);
        return (bd != null) ? bd.divide(ONE_HUNDRED, MC) : null;
    }

    protected boolean getBoolean(String name) {
        final String s = get(name);
        return StringUtils.hasText(s) ? Boolean.valueOf(s) : false;
    }

    protected DateTime getDateTime(String name) {
        return getDateTime(this.defaultDateTimeFormatter, name);
    }

    protected DateTime getDateTime(DateTimeFormatter fmt, String name) {
        final String s = get(name);
        return StringUtils.hasText(s) ? fmt.parseDateTime(s) : null;
    }

    protected void endDocument() {
        // empty, subclasses can override
    }

    protected int getRowNum() {
        return this.rowNum;
    }

    /**
     * Invoked whenever a row has been read completely; the values of the columns in that row
     * can be retrieved from inside this method.
     */
    protected abstract void handleRow();

    protected abstract V getResult();
}
