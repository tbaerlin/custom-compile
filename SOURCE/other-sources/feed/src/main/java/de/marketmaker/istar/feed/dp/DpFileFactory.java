/*
 * DpFileFactory.java
 *
 * Created on 19.11.2003 14:52:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronTrigger;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

/**
 * Creates DpFile objects from xml-Files; the DpFile object refers to an intermediate file that
 * contains data for all vendorkeys and their respective alias found in the input file if that
 * file is vendorkey-based. That file will be read whenever we need to create an instance of the
 * output file. The reason for using an intermediate file is that usually there are hundreds of
 * thousands of vendorkeys in a file and it would be a waste of memory to keep them all buffered
 * between writes. If, however, the DpFile is market based, it will just refer to the names
 * of the markets and no intermediate file will be created.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ThreadSafe
public class DpFileFactory extends DefaultHandler {

//    static {
//         make sure the xerces sax parser is used.
//        System.setProperty("javax.xml.parsers.SAXParserFactory",
//                "org.apache.xerces.jaxp.SAXParserFactoryImpl");
//    }

    private static final Map<String, String> DAY_MAPPINGS = new HashMap<>();

    static {
        DAY_MAPPINGS.put("MO", "MON");
        DAY_MAPPINGS.put("TU", "TUE");
        DAY_MAPPINGS.put("WE", "WED");
        DAY_MAPPINGS.put("TH", "THU");
        DAY_MAPPINGS.put("FR", "FRI");
        DAY_MAPPINGS.put("SA", "SAT");
        DAY_MAPPINGS.put("SU", "SUN");
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static SAXParserFactory SPF = SAXParserFactory.newInstance();

    private final StringBuilder sb = new StringBuilder();

    /**
     * attributes of the current xml tag
     */
    private Attributes attributes;

    private List<DpField> fieldAliases;

    private DpFile result;

    private Locator locator;

    /**
     * where intermediate files are stored
     */
    private final File workDir;

    /**
     * intermediate file while DpFile is being created
     */
    private File workFile;

    /**
     * number of entries in the current DpFile
     */
    private int size;

    /**
     * buffer used for writing data via workChannel
     */
    private final ByteBuffer bb = ByteBuffer.allocate(16 * 1024);

    /**
     * file channel for workFile, used for writing keys and aliases to
     */
    private FileChannel workChannel;

    private final Comparator<DpField> comparator;

    public DpFileFactory(File workDir) {
        this(workDir, false);
    }

    public DpFileFactory(File workDir, boolean compareFieldsByOrder) {
        this.workDir = workDir;
        this.comparator = compareFieldsByOrder ? DpField.COMPARATOR_BY_ORDER : DpField.COMPARATOR_BY_ID;
    }

    /**
     * Create a new DpFile using the definition in the given file.
     * @param f specifies DpFile
     * @return new DpFile
     * @throws Exception on error
     */
    synchronized DpFile createFrom(File f) throws Exception {
        reset();
        this.result = new DpFile(f);

        InputStream bis = null;
        try {
            final SAXParser parser = SPF.newSAXParser();
            bis = new BufferedInputStream(new FileInputStream(f));

            parser.parse(bis, this);
            finish();
            return this.result;
        } catch (SAXParseException spe) {
            throw new SAXException("<createFrom> failed in line: " + getLine(), spe);
        } finally {
            close(bis);
            close(this.workChannel);
            this.workChannel = null;
            this.workFile = null;
        }
    }

    private void close(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            this.logger.warn("<close> close failed for " + c);
        }
    }

    private void reset() {
        this.attributes = null;
        this.fieldAliases = new ArrayList<>();
        this.size = 0;
        this.bb.clear();
        this.sb.setLength(0);
    }

    private void finish() throws Exception {
        if (this.fieldAliases.isEmpty()) {
            throw new Exception("file does not specify any fields");
        }

        this.fieldAliases.sort(this.comparator);
        this.result.setFields(this.fieldAliases);
        this.result.setSize(this.size);

        flushBuffer();
        close(this.workChannel);
        if (this.workFile != null) {
            File keyFile = new File(this.workDir, this.result.getFile().getName() + ".bin");
            if (keyFile.exists()) {
                if (!keyFile.delete()) {
                    this.logger.error("<finish> could not delete " + keyFile.getAbsolutePath());
                    throw new IOException();
                }
            }
            if (!this.workFile.renameTo(keyFile)) {
                this.logger.error("<finish> could not rename file to " + keyFile.getAbsolutePath());
                throw new IOException();
            }

            this.result.setKeyFile(keyFile);
        }
    }

    private void flushBuffer() throws IOException {
        this.bb.flip();
        if (this.bb.hasRemaining()) {
            ensureWorkChannel();
            this.workChannel.write(this.bb);
        }
        this.bb.clear();
    }

    private void ensureWorkChannel() throws IOException {
        if (this.workChannel == null) {
            this.workFile = File.createTempFile("dp2", "tmp", this.workDir);
            this.workChannel = new RandomAccessFile(this.workFile, "rw").getChannel();
        }
    }

    @Override
    public synchronized void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }

    @Override
    public synchronized void characters(char[] chars, int start, int length) throws SAXException {
        try {
            this.sb.append(chars, start, length);
        } catch (Exception e) {
            throw new SAXException("parse error: " + e.getMessage());
        }
    }

    @Override
    public synchronized void startElement(String uri, String localName, String tagName,
            Attributes attributes) throws SAXException {
        this.sb.setLength(0);
        this.attributes = attributes;
    }

    @Override
    public synchronized void endElement(String uri, String localName,
            String tagName) throws SAXException {
        try {
            if ("field".equals(tagName)) {
                addField();
            }
            else if ("vendorkey".equals(tagName)) {
                addVendorkeyMapping();
            }
            else if ("trigger".equals(tagName)) {
                addSchedule();
            }
            else if ("keyAlias".equals(tagName)) {
                this.result.setKeyAlias(getString());
            }
            else if ("endTag".equals(tagName)) {
                this.result.setEndTag(getString());
            }
            else if ("filePrefix".equals(tagName)) {
                this.result.setOuputFilePrefix(getString());
            }
            else if ("fileTimestamp".equals(tagName)) {
                this.result.setOuputFileTimestamp(getString());
            }
            else if ("fileSuffix".equals(tagName)) {
                this.result.setOuputFileSuffix(getString());
            }
            else if ("isoDateFormat".equals(tagName)) {
                this.result.setIsoDateFormat(Boolean.valueOf(getString()));
            }
            else if ("priority".equals(tagName)) {
                this.result.setPriority(Integer.parseInt(getString()));
            }
            else if ("market".equals(tagName)) {
                addMarket();
            }
            else if ("typeMapping".equals(tagName)) {
                this.result.setMarketTypeMapping(getString());
            }
            else if ("realtime".equals(tagName)) {
                this.result.setRealtime("true".equalsIgnoreCase(getString()));
            }
            else if ("onlyUpdated".equals(tagName)) {
                if ("true".equalsIgnoreCase(getString())) {
                    this.result.setLastRunTimestamp(0);
                }
            }
            else if ("fieldSeparator".equals(tagName)) {
                setFieldSeparator();
            }
        } catch (Exception e) {
            throw new SAXException("<endElement> failed in line: " + getLine(), e);
        }
    }

    private void setFieldSeparator() throws SAXException {
        String sep = getString();
        if (sep.length() > 1) {
            throw new SAXException("cannot have fieldSeparator longer than one char");
        }
        this.result.setFieldSeparator(sep.charAt(0));
    }

    private void addMarket() throws SAXException {
        if (this.size > 0) {
            throw new SAXException("cannot have market and vendorkeys in same file");
        }
        final String name = this.attributes.getValue("name");
        if (name != null) {
            this.result.addMarketName(new ByteString(name));
        }
        else {
            throw new SAXException("missing name attribute for market in line: " + getLine());
        }
    }

    private String getString() {
        return this.sb.toString().trim();
    }

    private int getLine() {
        return (this.locator != null) ? this.locator.getLineNumber() : -1;
    }

    private void addField() throws SAXException {
        final String name = this.attributes.getValue("name");
        String alias = this.attributes.getValue("alias");
        if (name != null) {
            final VwdFieldDescription.Field field = VwdFieldDescription.getFieldByName(name);
            if (field == null) {
                throw new SAXException("invalid field: " + name + " in line: " + getLine());
            }
            if (alias == null) {
                alias = Integer.toString(field.id());
            }
            this.fieldAliases.add(new DpField(field.id(), alias));
        }
    }

    private void addVendorkeyMapping() throws IOException, SAXException {
        if (this.result.getMarkets() != null) {
            throw new SAXException("cannot have market and vendorkeys in same file");
        }
        final String nameStr = this.attributes.getValue("name");
        final String aliasStr = this.attributes.getValue("alias");
        if (nameStr != null && aliasStr != null) {

            ByteString vkey = new ByteString(nameStr);
            if (VendorkeyVwd.isKeyWithTypePrefix(vkey)) {
                vkey = vkey.substring(vkey.indexOf('.') + 1);
            }

            final ByteString alias = new ByteString(aliasStr);
            vkey.writeTo(this.bb, ByteString.LENGTH_ENCODING_BYTE);
            alias.writeTo(this.bb, ByteString.LENGTH_ENCODING_BYTE);
            if (this.bb.remaining() < 512) {
                flushBuffer();
            }
            this.size++;
        }
    }

    private void addSchedule() throws SAXException {

        String cronExpression = this.sb.toString().trim().toUpperCase();
        final String[] tokens = cronExpression.split(" ");

        if (tokens.length == 5) {
            final String tmp = convertCronExpressionToQuartz(tokens);

            this.logger.info("<addSchedule> converted cron expression from '" + cronExpression
                    + "' to '" + tmp + "'");
            cronExpression = tmp;
        }

        checkCronExpressionValidity(cronExpression);

        this.result.addCronExpression(cronExpression);
    }

    private void checkCronExpressionValidity(String cronExpression) throws SAXException {
        try {
            new CronTrigger(cronExpression);
        } catch (IllegalArgumentException e) {
            throw new SAXException("Illegal cron expression", e);
        }
    }

    /**
     * Used to convert from the old, excalibur style format to the new cron format.
     */
    private static String convertCronExpressionToQuartz(final String[] tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("0 ");      // seconds
        sb.append(tokens[0]); // minutes
        sb.append(" ");
        sb.append(tokens[1]); // hours
        sb.append(" ");

        if ("*".equals(tokens[2]) && !"?".equals(tokens[4])) {
            sb.append("? "); // day of month, don't care
        }
        else {
            sb.append(tokens[2]);
            sb.append(" ");
        }

        sb.append(tokens[3]); // month
        sb.append(" ");
        sb.append(convertDaysOfWeek(tokens[4]));

        return sb.toString();
    }

    private static String convertDaysOfWeek(String expr) {
        final Pattern p = Pattern.compile("(MO|TU|WE|TH|FR|SA|SU)([-/,]|$)");
        final Matcher m = p.matcher(expr);
        final StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, DAY_MAPPINGS.get(m.group(1)) + m.group(2));
        }
        m.appendTail(sb);

        return sb.toString();
    }
}
