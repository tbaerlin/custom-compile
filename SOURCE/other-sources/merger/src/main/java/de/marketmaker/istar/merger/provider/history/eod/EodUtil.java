/*
 * EodHistory.java
 *
 * Created on 06.12.12 13:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zzhao
 */
public class EodUtil {

    public static final byte ZERO_BYTE = (byte) 0;

    public static final byte[] EMPTY_BA = new byte[0];

    public static final ByteBuffer EMPTY_BB = ByteBuffer.wrap(new byte[0]);

    public static final String FN_UPDATE_LOCK = "eod_update.lck";

    private static final Logger log = LoggerFactory.getLogger(EodUtil.class);

    private EodUtil() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    public static File getUpdateLockFile(File workDir) {
        return new File(workDir, EodUtil.FN_UPDATE_LOCK);
    }

    /**
     * Encodes the given two numbers into one number of type int as following:
     * <ul>
     * <li>encode the first number using bits 12 ~ 31, e.g. 20 bits</li>
     * <li>encode the second number using bits 0 ~ 11, e.g. 12 bits</li>
     * </ul>
     *
     * @param length
     * @param month
     * @return
     */
    public static int encodeLengthMonth(int length, short month) {
        if (length < 0) {
            throw new IllegalArgumentException("length underflow: " + length);
        }
        if (length > 0x0FFFFF) {
            throw new IllegalArgumentException("length overflow: " + length);
        }

        if (month < 0) {
            throw new IllegalArgumentException("month underflow: " + month);
        }
        if (month > 0x0FFF) {
            throw new IllegalArgumentException("month overflow: " + month);
        }

        return length << 12 | month;
    }

    public static int decodeYearLength(final int len20Month12) {
        return 0x0FFFFF & (len20Month12 >> 12);
    }

    public static short decodeMonth(final int len20Month12) {
        return (short) (len20Month12 & 0x0FFF);
    }

    public static int encodeLengthField(int length, byte field) {
        if (length < 0) {
            throw new IllegalArgumentException("length underflow: " + length);
        }
        if (length > 0x0FFFFFF) {
            throw new IllegalArgumentException("length overflow: " + length);
        }

        if (field < 0) {
            throw new IllegalArgumentException("month underflow: " + field);
        }
        if (field > 0x0FF) {
            throw new IllegalArgumentException("month overflow: " + field);
        }

        return length << 8 | field;
    }

    public static int decodeFieldLength(final int len24Field8) {
        return 0x0FFFFFF & (len24Field8 >> 8);
    }

    public static byte decodeField(final int len24Field8) {
        return (byte) (len24Field8 & 0x0FF);
    }

    /**
     * @param units non-negative
     * @param unit non-negative
     * @return
     */
    public static int units2Bypass(int units, int unit) {
        int ret = 0;
        while (units >= unit) {
            units >>>= 1;
            if ((units & unit) == unit) {
                ret++;
            }
        }

        return ret;
    }

    public static int countUnits(int units) {
        return Integer.bitCount(units);
    }

    public static int pivot(int bits, int bit) {
        return bits & (~(bit - 1));
    }

    public static short getYear(int date) {
        return (short) (date / 10000);
    }

    public static short monthBit(String yyyy_dd_mm) {
        return monthBit(Integer.parseInt(yyyy_dd_mm.substring(8, 10)));
    }

    public static short monthBit(int month) {
        return (short) (0x01 << (month - 1));
    }

    public static int dayBit(String yyyy_dd_mm) {
        return dayBit(Integer.parseInt(yyyy_dd_mm.substring(5, 7)));
    }

    public static int getDayBits(int dayBits, int dayBit, boolean remove) {
        if (remove) {
            return dayBits & ~dayBit;
        }
        else {
            return dayBits | dayBit;
        }
    }

    public static int dayBit(int day) {
        return 0x01 << (day - 1);
    }

    public static int getDate(String yyyy_dd_mm) {
        return Integer.parseInt(yyyy_dd_mm.substring(0, 4)) * 10000 +
                Integer.parseInt(yyyy_dd_mm.substring(8, 10)) * 100 +
                Integer.parseInt(yyyy_dd_mm.substring(5, 7));
    }

    public static int decodeMonth(short months, int idx) {
        int seen = -1;
        for (int i = 11; i >= 0; i--) {
            if ((months & (0x01 << i)) != 0) {
                seen++;
            }
            if (seen == idx) {
                return (i + 1);
            }
        }

        throw new IllegalStateException("invalid months bits: " + months + " and idx: " + idx);
    }

    public static int decodeDay(int days, int idx) {
        int seen = -1;
        for (int i = 30; i >= 0; i--) {
            if ((days & (0x01 << i)) != 0) {
                seen++;
            }
            if (seen == idx) {
                return (i + 1);
            }
        }

        throw new IllegalStateException("invalid months bits: " + days + " and idx: " + idx);
    }

    public static int calcDate(short year, short months, int monthIdx, int days, int dayIdx) {
        return year * 10000 + decodeMonth(months, monthIdx) * 100 + decodeDay(days, dayIdx);
    }

    public static Reader getFileReader(File mpa) throws IOException {
        if (mpa.getName().endsWith(".gz")) {
            return new InputStreamReader(new GZIPInputStream(new FileInputStream(mpa)));
        }
        else if (mpa.getName().endsWith(".zip")) {
            final ZipFile zipFile = new ZipFile(mpa);
            return new InputStreamReader(zipFile.getInputStream(zipFile.entries().nextElement()));
        }

        return new FileReader(mpa);
    }

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyyMMdd");

    public static LocalDate getDateFromCSV(String name) {
        final String ext;
        if (name.endsWith(".gz") || name.endsWith(".zip")) {
            ext = name.substring(name.indexOf(".") + 1, name.lastIndexOf("."));
        }
        else {
            ext = name.substring(name.indexOf(".") + 1);
        }
        return DTF.parseDateTime(ext.substring(3)).toLocalDate().minusDays(1);
    }

    public static LocalDate getDateFromProtobuf(String name) {
        final int pos = name.indexOf(".");
        final String ext = name.substring(pos - 8, pos);
        // TODO depends on how the eod file is named
        return DTF.parseDateTime(ext).toLocalDate();
    }


    public static byte lastByte(ByteBuffer bb) {
        return bb.get(bb.position() - 1);
    }

    public static byte[] fromBuffer(ByteBuffer bb) {
        return bb.position() == 0 ? EodUtil.EMPTY_BA :
                bb.hasRemaining() ? Arrays.copyOfRange(bb.array(), 0, bb.position()) : bb.array();
    }

    public static interface EodOperation {
        void process() throws IOException;
    }

    public static void updateWithinLock(File lockFile, EodOperation op) throws IOException {
        if (lockFile.createNewFile()) {
            log.info("<updateWithinLock> created lock file {}", lockFile.getAbsolutePath());
        }

        if (!lockFile.exists()) {
            throw new IllegalStateException("cannot get hold of lock file: " + lockFile.getAbsolutePath());
        }

        try (
                final FileChannel channel =
                        new RandomAccessFile(lockFile, "rw").getChannel()
        ) {
            final FileLock lock = channel.tryLock();
            if (null == lock) {
                log.warn("<tickFile> another update already in process, this update attemp ignored");
            }
            else {
                try {
                    op.process();
                } finally {
                    lock.release();
                }
            }
        }
    }
}
