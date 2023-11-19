/*
 * Util.java
 *
 * Created on 10.07.12 16:14
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import static de.marketmaker.istar.feed.history.HistoryUnit.valueOf;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zzhao
 */
public final class HistoryUtil {

    public static final DateTimeFormatter DTF_YMDHMS = DateTimeFormat.forPattern("yyyyMMdd-HHmmss");

    public static final DateTimeFormatter DTF_YMDHM = DateTimeFormat.forPattern("yyyyMMdd-HHmm");

    public static final DateTimeFormatter DTF_MINUTE = DateTimeFormat.forPattern("HH:mm");

    public static final DateTimeFormatter DTF_HOUR = DateTimeFormat.forPattern("yyyyMMdd-HH");

    public static final DateTimeFormatter DTF_DAY = DateTimeFormat.forPattern("yyyyMMdd");

    public static final DateTimeFormatter DTF_MONTH = DateTimeFormat.forPattern("yyyyMM");

    public static final DateTimeFormatter DTF_YEAR = DateTimeFormat.forPattern("yyyy");

    public static final DateTimeFormatter DTF_EXCEL_YMDHMS = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");

    public static final DateTimeFormatter DTF_EXCEL_DAY = DateTimeFormat.forPattern("dd.MM.yyyy");

    public static final ByteBuffer EMPTY_BB = ByteBuffer.wrap(new byte[0]).asReadOnlyBuffer();

    public static final String F_UNITS = "working_units.txt";

    private static final Logger log = LoggerFactory.getLogger(HistoryUtil.class);

    private HistoryUtil() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    public static int daysFromBegin(LocalDate genesis, LocalDate dayLastMonth) {
        return Days.daysBetween(genesis, dayLastMonth).getDays();
    }

    public static int daysFromBegin2Date(LocalDate genesis, int days) {
        return DateUtil.toYyyyMmDd(genesis.plusDays(days));
    }

    public static LocalDate toLocalDate(LocalDate genesis, short days) {
        return DateUtil.yyyyMmDdToLocalDate(HistoryUtil.daysFromBegin2Date(genesis,
                HistoryUtil.fromUnsignedShort(days)));
    }

    public static LocalDate toLocalDate(LocalDate genesis, int days) {
        return DateUtil.yyyyMmDdToLocalDate(HistoryUtil.daysFromBegin2Date(genesis, days));
    }

    public static void replaceFile(File replacee, File replacer) {
        if (replacee.exists()) {
            ensureFileOpOK(replacee.delete(), "delete file: " + replacee.getAbsolutePath());
        }

        if (replacer.exists()) {
            ensureFileOpOK(replacer.renameTo(replacee), "rename file: " + replacer.getAbsolutePath()
                    + " to: " + replacee.getAbsolutePath());
        }
    }

    private static final byte[] POINT = new byte[]{'.'};

    public static ByteString getKey(ByteString symbol) {
        final int idx1 = symbol.indexOf('.');
        final int idx2 = symbol.indexOf('.', idx1 + 1);
        if (idx2 < 0) {
            return symbol.substring(idx1 + 1).append(POINT).append(symbol.substring(0, idx1));
        }
        else {
            return symbol.substring(idx1 + 1, idx2 + 1).
                    append(symbol.substring(0, idx1)).append(symbol.substring(idx2));
        }
    }

    public static ByteString removeTypeAndGetKey(ByteString symbol) {
        return getKey(symbol.substring(symbol.indexOf('.') + 1));
    }

    public static void ensureFileOpOK(boolean succ, String opMsg) {
        if (!succ) {
            throw new IllegalStateException(opMsg + " failed");
        }
    }

    public static void reportFileOpStatus(boolean succ, String opMsg) {
        if (!succ) {
            log.error("<reportFileOpStatus> {} failed", opMsg);
        }
    }

    /**
     * Delete older files related to the given {@link HistoryUnit} in the given directory and keep 2
     * recent files.
     *
     * @param dir a directory
     * @param units {@link HistoryUnit}s
     */
    public static void deleteOlderFiles(File dir, HistoryUnit... units) {
        deleteOlderFiles(dir, 2, units);
    }

    /**
     * Delete older files related to the given {@link HistoryUnit} in the given directory and keep the
     * given number of recent files.
     *
     * @param dir a directory
     * @param filesToKeep recent files to keep
     * @param units {@link HistoryUnit}s
     */
    public static void deleteOlderFiles(File dir, int filesToKeep,
        HistoryUnit... units) {
        if (null == units) {
            return;
        }
        for (HistoryUnit unit : units) {
            try {
                final List<File> files = unit.getRelevantFiles(dir);
                deleteOlderFiles(files, filesToKeep);
            } catch (Exception e) {
                log.error("<deleteOlderFiles> failed deleting old files for: " + unit, e);
            }
        }
    }

    private static void deleteOlderFiles(List<File> rmf, int from) {
        if (rmf.size() > from) {
            for (int i = from; i < rmf.size(); i++) {
                final File file = rmf.get(i);
                log.info("<deleteOlderFiles> delete {}", file.getAbsolutePath());
                reportFileOpStatus(file.delete(), "deleting: " + file.getAbsolutePath());
            }
        }
    }

    public static <T extends Comparable<T>> File createChangeFile(Class<T> clazz, int lengthBits,
        String prefix, File targetDir, HistoryDataSource<T> historyDataSource,
        int fromDate, int toDate) throws IOException {
        final TimeTaker tt = new TimeTaker();
        final File file = HistoryUnit.Change.createFile(prefix, targetDir, fromDate, toDate);
        HistoryWriter<T> writer = null;
        try {
            writer = new HistoryWriter<>(file, lengthBits, clazz);
            historyDataSource.transfer(writer);
        } finally {
            IoUtils.close(writer);
        }

        log.info("<patch> creation of change file took: " + tt);
        return file;
    }

    public static boolean isUnsignedShort(int num) {
        return num >= 0 && num <= 65535; // 2^16-1
    }

    public static int fromUnsignedShort(short s) {
        return (0x0FFFF & s);
    }

    public static boolean isUnsignedByte(int num) {
        return num >= 0 && num <= 255; // 2^16-1
    }

    public static void ensureUnsignedByte(int num) {
        if (!isUnsignedByte(num)) {
            throw new IllegalArgumentException("not unsigned byte: " + num);
        }
    }

    public static int fromUnsignedByte(byte b) {
        return (0x0FF & b);
    }

    public static File getWorkingUnitsFile(File workDir) {
        return new File(workDir, F_UNITS);
    }

    public static void updateHistoryUnits(File workDir, EnumSet<HistoryUnit> units)
            throws IOException {
        FileUtils.writeLines(getWorkingUnitsFile(workDir), units);
    }

    public static EnumSet<HistoryUnit> loadHistoryUnits(File workDir) throws IOException {
        final File file = getWorkingUnitsFile(workDir);
        final EnumSet<HistoryUnit> ret = EnumSet.noneOf(HistoryUnit.class);
        if (file.exists()) {
            final List<String> list = FileUtils.readLines(file);
            if (list.isEmpty()) {
                return ret;
            }
            else {
                for (String val : list) {
                    ret.add(valueOf(val));
                }
            }
        }

        return ret;
    }

    public static LocalDate getMonthBeginDate(int yyyyMmdd) {
        return getMonthBeginDate(DateUtil.yyyyMmDdToLocalDate(yyyyMmdd));
    }

    public static LocalDate getMonthBeginDate(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    public static <T extends MutableEntry> Iterable<T> patchIt(ByteBuffer buffer, Interval interval,
            ByteBuffer patchBuf, Class<T> clazz, LocalDate genesis) {
        final MutableEntryIterator<T> baseIT = new MutableEntryIterator<>(buffer, clazz);
        if (!patchBuf.hasRemaining()) {
            return baseIT;
        }
        else {
            MutableEntryIterator<T> patchIT = new MutableEntryIterator<>(patchBuf, clazz);
            final ArrayList<T> list = new ArrayList<>();

            final int daysFrom = HistoryUtil.daysFromBegin(genesis, interval.getStart().toLocalDate());
            final int daysTo = HistoryUtil.daysFromBegin(genesis, interval.getEnd().toLocalDate());

            T base = nextTickEntry(baseIT);
            T patch = nextTickEntry(patchIT);
            while (null != base || null != patch) {
                final int diff = compareTickEntry(base, patch);
                if (diff == 0) {
                    list.add(clazz.cast(patch.copy()));
                    base = nextTickEntry(baseIT);
                    patch = nextTickEntry(patchIT);
                }
                else if (diff > 0) {
                    if (null != patch) {
                        if (patch.getDays() >= daysFrom && patch.getDays() < daysTo) {
                            list.add(clazz.cast(patch.copy()));
                        }
                        patch = nextTickEntry(patchIT);
                    }
                    else {
                        list.add(clazz.cast(base.copy()));
                        base = nextTickEntry(baseIT);
                    }
                }
                else {
                    if (null != base) {
                        list.add(clazz.cast(base.copy()));
                        base = nextTickEntry(baseIT);
                    }
                    else {
                        if (patch.getDays() >= daysFrom && patch.getDays() < daysTo) {
                            list.add(clazz.cast(patch.copy()));
                        }
                        patch = nextTickEntry(patchIT);
                    }
                }
            }

            return list;
        }
    }

    private static <T extends MutableEntry> int compareTickEntry(T entryA, T entryB) {
        if (null != entryA && null != entryB) {
            return entryB.getDays() - entryA.getDays();
        }
        else if (null == entryA) {
            return 1;
        }
        else {
            return -1;
        }
    }

    private static <T extends MutableEntry> T nextTickEntry(Iterator<T> it) {
        return it.hasNext() ? it.next() : null;
    }

    public static <T> T nextItem(Iterator<T> it) {
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public static <T> void sortMerge(Iterator<T> base, SortMergeContext<T> ctx, Iterator<T> delta) {
        T baseItem = nextItem(base);
        T deltaItem = nextItem(delta);

        while (null != baseItem || null != deltaItem) {
            if (null == deltaItem) {
                ctx.onItem(baseItem);
                baseItem = nextItem(base);
            }
            else if (null == baseItem) {
                ctx.onItem(deltaItem);
                deltaItem = nextItem(delta);
            }
            else {
                final int order = ctx.compare(baseItem, deltaItem);
                if (order > 0) {
                    ctx.onItem(deltaItem);
                    deltaItem = nextItem(delta);
                }
                else if (order < 0) {
                    ctx.onItem(baseItem);
                    baseItem = nextItem(base);
                }
                else {
                    ctx.onItem(ctx.merge(baseItem, deltaItem));
                    baseItem = nextItem(base);
                    deltaItem = nextItem(delta);
                }
            }
        }
    }

    public interface SortMergeContext<T> {
        void onItem(T item);

        T merge(T baseItem, T deltaItem);

        int compare(T baseItem, T deltaItem);
    }

    public interface Op {
        void process() throws IOException;
    }

    public static boolean updateWithinLock(File lockFile, Op op) throws IOException {
        boolean canCreateLockFile = lockFile.createNewFile();
        if (!canCreateLockFile && !lockFile.exists()) {
            throw new IllegalStateException("cannot get hold of lock file: " + lockFile.getAbsolutePath());
        }

        try (
                final FileChannel channel =
                        new RandomAccessFile(lockFile, "rw").getChannel()
        ) {
            final FileLock lock = channel.tryLock();
            if (null == lock) {
                log.warn("<tickFile> another update already in process, this update attempt ignored");
                return false;
            }
            else {
                try {
                    op.process();
                } finally {
                    lock.release();
                }
            }
            return true;
        }
    }

    public static boolean isStartOfDay(DateTime dt) {
        return dt.getMillisOfSecond() == 0 && dt.getSecondOfMinute() == 0
                && dt.getMinuteOfHour() == 0 && dt.getHourOfDay() == 0;
    }

    public static String toIntervalString(Interval interval) {
        return toIntervalString(interval.getStart(), interval.getEnd());
    }

    public static String toIntervalString(DateTime start, DateTime end) {
        return "[" // inclusive start
                + HistoryUtil.DTF_YMDHM.print(start)
                + " ~ "
                + HistoryUtil.DTF_YMDHM.print(end)
                + ">"; // exclusive end
    }

    public static <T> int countKeys(Class<T> clazz, ByteBuffer bb) {
        if (!bb.hasRemaining()) {
            return 0;
        }
        int count = 0;
        do {
            bb.position(bb.position() + 8); // read pass offset
            if (bb.hasRemaining()) {
                Item.readPassKey(clazz, bb);
                count++;
            }
        } while (bb.hasRemaining());

        return count;
    }
}
