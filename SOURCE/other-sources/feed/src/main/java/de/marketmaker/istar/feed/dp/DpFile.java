/*
 * DpFile.java
 *
 * Created on 19.11.2003 14:51:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import net.jcip.annotations.GuardedBy;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.domain.data.SnapRecord;

/**
 * Represents a DP file: it encapsulates the real file on the filesystem and provides additional
 * information stored in that file such as requested markets, symbols, cron expressions, etc.
 * It also allows to detect when the underlying file was changed so that we can, for example,
 * ignore such DpFile and not write output for it.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class DpFile {
    interface Writer {
        File write(DpFile dpFile) throws Exception;
    }


    static final char DEFAULT_FIELD_SEPARATOR = ',';         // field separator

    private char fieldSeparator = DEFAULT_FIELD_SEPARATOR;

    /**
     * file that defines this output file
     */
    private final File file;

    /**
     * file with vendorkeys and aliases
     */
    private File keyFile;

    private MarketKeySource.TypeMapping typeMapping;

    /**
     * name of markets to be dumped
     */
    private Set<ByteString> markets;

    /**
     * last modification date of file
     */
    private final long lastModified;

    /**
     * when this file should be generated
     */
    private List<String> cronExpressions;

    /**
     * is used to cancel cron jobs in case the file was removed/replaced
     * or the system is shutting down.
     */
    @GuardedBy("this")
    private List<ScheduledFuture<?>> futures = new ArrayList<>();

    /**
     * first part of output file name
     */
    private String ouputFilePrefix;

    /**
     * timestamp part of output file name
     */
    private String ouputFileTimestamp;

    /**
     * final part of output file name
     */
    private String ouputFileSuffix;

    /**
     * formatter for ouputFileTimestamp
     */
    private SimpleDateFormat df = null;

    /**
     * name of the key system used to map vendorkeys
     */
    private String keyAlias;

    /**
     * content of output file's final line
     */
    private byte[] endTag;

    /**
     * content of output file's first line
     */
    private byte[] header;

    /**
     * defines all fields to be written and their respective aliases
     */
    private DpField[] fields;

    private int size;

    private int priority;

    private boolean isoDateFormat;

    /**
     * Setting this to true means that only realtime snap data is accessed. False means that the
     * delay snap record will be returned if it exists, otherwise realtime snap data will be returned
     * (a non-existing delay snap indicates that the data is not to be delayed and therefore
     * realtime access is allowed for anyone).
     */
    private boolean realtime = false;

    /**
     * some information/statistics
     */
    private final DpFileInfo info;

    private int lastRunTimestamp = -1;

    DpFile(File f) {
        this.file = f;
        this.lastModified = f.lastModified();
        this.info = new DpFileInfo(f.getAbsolutePath());
    }

    DpFileInfo getInfo() {
        return this.info;
    }

    public String toString() {
        return "DpFile["
                + " info: [" + this.info + "]"
                + ", out: " + getOutputFile(this.ouputFileTimestamp)
                + ", RT: " + this.realtime
                + ", at: " + this.cronExpressions
                + ", size: " + this.size
                + ", markets: " + this.markets
                + ", prio: " + this.priority
                + ", last: " + this.lastRunTimestamp
                + ", spec: " + Arrays.toString(this.fields)
                + "]";
    }

    public char getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(char fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public void setIsoDateFormat(boolean isoDateFormat) {
        this.isoDateFormat = isoDateFormat;
    }

    public boolean isIsoDateFormat() {
        return isoDateFormat;
    }

    public boolean isRealtime() {
        return realtime;
    }

    public void setRealtime(boolean realtime) {
        this.realtime = realtime;
    }

    File getKeyFile() {
        return keyFile;
    }

    void setKeyFile(File keyFile) {
        this.keyFile = keyFile;
    }

    public Set<ByteString> getMarkets() {
        return this.markets;
    }

    void setMarketTypeMapping(String mappingName) {
        this.typeMapping = MarketKeySource.TypeMapping.valueOf(mappingName);
    }

    MarketKeySource.TypeMapping getTypeMapping() {
        return typeMapping;
    }

    public void addMarketName(ByteString marketName) {
        if (this.markets == null) {
            this.markets = new LinkedHashSet<>();
        }
        this.markets.add(marketName);
    }

    boolean isUpToDate() {
        return this.lastModified == this.file.lastModified();
    }

    File getFile() {
        return file;
    }

    List<String> getCronExpressions() {
        return cronExpressions;
    }

    void addCronExpression(String cronExpression) {
        if (this.cronExpressions == null) {
            List<String> ce = new ArrayList<>();
            ce.add(cronExpression);
            this.cronExpressions = ce;
        }
        else {
            this.cronExpressions.add(cronExpression);
        }
    }

    synchronized void add(ScheduledFuture<?> f) {
        this.futures.add(f);
    }

    synchronized void cancel() {
        for (ScheduledFuture<?> future : this.futures) {
            future.cancel(false);
        }
    }

    void setOuputFileTimestamp(String ouputFileTimestamp) {
        this.ouputFileTimestamp = ouputFileTimestamp;
        if (this.ouputFileTimestamp != null) {
            this.df = new SimpleDateFormat(this.ouputFileTimestamp);
        }
    }

    void setEndTag(String endTag) {
        if (StringUtils.hasText(endTag)) {
            this.endTag = endTag.getBytes(SnapRecord.DEFAULT_CHARSET);
        }
    }

    void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    void setOuputFilePrefix(String ouputFilePrefix) {
        this.ouputFilePrefix = ouputFilePrefix;
    }

    void setOuputFileSuffix(String ouputFileSuffix) {
        this.ouputFileSuffix = ouputFileSuffix;
    }

    byte[] getEndTag() {
        return endTag;
    }

    byte[] getHeader() {
        if (this.header == null) {
            final StringBuilder sb = new StringBuilder();
            sb.append(getKeyAlias());
            for (DpField fa : this.fields) {
                sb.append(this.fieldSeparator);
                sb.append(fa.getAlias());
            }
            this.header = sb.toString().getBytes(SnapRecord.DEFAULT_CHARSET);
        }
        return this.header;
    }

    String getKeyAlias() {
        return keyAlias;
    }

    File getOutputFile() {
        return getOutputFile(this.df != null ? this.df.format(new Date()) : "");
    }

    private File getOutputFile(String timestamp) {
        return new File(this.ouputFilePrefix
                + (timestamp != null ? timestamp : "")
                + this.ouputFileSuffix);
    }

    void setFields(List<DpField> fields) {
        this.fields = fields.toArray(new DpField[fields.size()]);
    }

    public DpField[] getFields() {
        return this.fields;
    }

    int getSize() {
        return size;
    }

    void setSize(int size) {
        this.size = size;
    }

    int getPriority() {
        return priority;
    }

    void setPriority(int priority) {
        if (priority < 0 || priority > 10) {
            throw new IllegalArgumentException(priority + " not in [0..10]");
        }
        this.priority = priority;
    }

    void setLastRunTimestamp(int lastRunTimestamp) {
        this.lastRunTimestamp = lastRunTimestamp;
    }

    int getLastRunTimestamp() {
        return lastRunTimestamp;
    }
}
