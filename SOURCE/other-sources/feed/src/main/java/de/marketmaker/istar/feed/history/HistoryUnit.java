/*
 * TickHistoryUnit.java
 *
 * Created on 23.08.12 13:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import de.marketmaker.istar.common.util.DateUtil;


/**
 * History units that are currently in use.
 * <p>
 * Each history file is denoted by one unit, which further decorates the file with a special
 * suffix. Upon creating history unit file a content type is also required to further distinguish
 * the content contained in such files.
 * </p>
 * <p>
 * <b><i>Note that it is vital to organize history unit files in ONE folder for ONLY ONE kind of
 * content. The methods in this class which are responsible for searching relevant files depend on
 * this restriction. The restriction is there to avoid using an additional parameter "contentType"
 * everywhere, which is not always necessary.</i></b>
 * </p>
 *
 * @author zzhao
 */
public enum HistoryUnit {
    Rest(".rest"),
    Decade(".decade"),
    Year(".year"),
    Months(".months"),
    Month(".month"),
    Day(".day"),
    Patch(".patch"),
    Change(".chg");

    private final String suffix;

    private final IOFileFilter filter;

    private HistoryUnit(String suffix) {
        this.suffix = suffix;
        this.filter = FileFilterUtils.makeFileOnly(FileFilterUtils.suffixFileFilter(suffix));
    }

    public static HistoryUnit fromExt(File file) {
        final String fileName = file.getName();
        for (final HistoryUnit unit : values()) {
            if (fileName.endsWith(unit.suffix)) {
                return unit;
            }
        }

        throw new IllegalArgumentException("no matching history unit found for: " + fileName);
    }

    private FileFilter getFilter() {
        return this.filter;
    }

    public List<File> getRelevantFiles(File dir) {
        final File[] candidates = dir.listFiles(getFilter());
        if (null == candidates) {
            return Collections.emptyList();
        }
        final List<File> files = Arrays.asList(candidates);
        files.sort(Collections.reverseOrder());
        return files;
    }

    public File getLatestFile(File dir) {
        final List<File> files = getRelevantFiles(dir);
        return files.isEmpty() ? null : files.get(0);
    }

    public List<File> getRelevantFilesDistinctMonth(File dir) {
        final ArrayList<File> files = new ArrayList<>(getRelevantFiles(dir));
        final Iterator<File> it = files.iterator();
        LocalDate monthBegin = null;
        while (it.hasNext()) {
            final File next = it.next();
            final LocalDate fromDate = DateUtil.yyyyMmDdToLocalDate(getFromDate(next));
            if (null != monthBegin && monthBegin.getYear() == fromDate.getYear()
                    && monthBegin.getMonthOfYear() == fromDate.getMonthOfYear()) {
                it.remove();
            }
            else {
                monthBegin = fromDate;
            }
        }

        return files;
    }

    private static final Pattern CONTENT_TYPE = Pattern.compile("([A-Z][A-Z_]*[A-Z])");

    public static String getContentType(File file) {
        final String fn = file.getName();
        final Matcher matcher = CONTENT_TYPE.matcher(fn);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("cannot extract content type from: " + file.getAbsolutePath());
    }

    public static void ensureSameContent(File fileA, File fileB) {
        if (!getContentType(fileA).equals(getContentType(fileB))) {
            throw new IllegalStateException("different content: " + fileA.getName() + ", "
                    + fileB.getName());
        }
    }

    public File createFile(String contentType, File dir, int fromDate, int toDate) {
        return createFile(contentType, dir, fromDate + "-" + toDate);
    }

    public File createFile(String contentType, File dir, String fileName) {
        return createFile(dir, contentType + "_" + fileName);
    }

    private File createFile(File dir, String fileName) {
        switch (this) {
            case Rest:
            case Decade:
            case Months:
            case Change:
            case Patch:
                return new File(dir, System.currentTimeMillis() + "_" + fileName
                        + this.suffix);
            default:
                return new File(dir, fileName + this.suffix);
        }
    }

    public File createTmpFile(String contentType, File dir, LocalDate fromDate, LocalDate toDate) {
        return createTmpFile(contentType, dir, DateUtil.toYyyyMmDd(fromDate), DateUtil.toYyyyMmDd(toDate));
    }

    public File createTmpFile(String contentType, File dir, int fromDate, int toDate) {
        return createTmpFile(contentType, dir, fromDate + "-" + toDate);
    }

    public File createTmpFile(String contentType, File dir, String fileName) {
        return createTmpFile(dir, contentType + "_" + fileName);
    }

    private File createTmpFile(File dir, String fileName) {
        switch (this) {
            case Rest:
            case Decade:
            case Months:
            case Change:
            case Patch:
                return new File(dir, System.currentTimeMillis() + "_" + fileName + this.suffix + ".tmp");
            default:
                return new File(dir, fileName + this.suffix + ".tmp");
        }
    }

    public File convert(File file, HistoryUnit unit) {
        switch (this) {
            case Rest:
            case Decade:
            case Months:
            case Change:
            case Patch:
                return new File(file.getParentFile(), getFileNameNoExt(file) + unit.suffix);
            default:
                return unit.createFile(file.getParentFile(), getFileNameNoExt(file));
        }
    }

    public String getFileNameNoExt(File file) {
        final String name = file.getName();
        return name.substring(0, name.indexOf(this.suffix));
    }

    public int getFromDate(File file) {
        final String name = getRelevantName(file);
        final int idx = name.indexOf("-");
        if (idx < 0) {
            return Integer.parseInt(name.substring(0, name.indexOf(this.suffix)));
        }
        else {
            return Integer.parseInt(name.substring(0, idx));
        }
    }

    private String getRelevantName(File file) {
        return file.getName().substring(file.getName().lastIndexOf("_") + 1);
    }

    public int getToDate(File file) {
        final String name = getRelevantName(file);
        final int idx = name.indexOf("-");
        if (idx < 0) {
            return Integer.parseInt(name.substring(0, name.indexOf(this.suffix)));
        }
        else {
            return Integer.parseInt(name.substring(idx + 1, name.indexOf(this.suffix)));
        }
    }

    public Interval getInterval(File file) {
        return new Interval(
                DateUtil.yyyymmddToDateTime(getFromDate(file)),
                DateUtil.yyyymmddToDateTime(getToDate(file)).plusDays(1));
    }
}
