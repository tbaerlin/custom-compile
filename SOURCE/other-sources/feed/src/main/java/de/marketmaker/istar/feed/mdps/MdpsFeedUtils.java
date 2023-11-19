/*
 * MdpsFeedUtils.java
 *
 * Created on 01.08.2005 14:02:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Static helper methods to deal with mdps feed data.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsFeedUtils {

    static final int TRACK_ENTRY_LENGTH = 10;

    static int getTrackId(int module) {
        // bits 15..13 in the ushort value are for module id, so only 7 values can be used
        if (module < 1 || module > 7) {
            throw new IllegalArgumentException(module + " not in [1..7]");
        }
        final int pid = Integer.getInteger("mdps.processId", 0);
        if (pid > 0x1FFF) {
            // pid would overwrite module bits
            throw new IllegalArgumentException("mdps.processId > 8191: " + pid);
        }
        return (pid == 0) ? 0 : (module << 13) + pid;
    }

    // type mapping lookup table to speed things up compared to a more complex switch statement
    private static final byte[] TYPE_MAPPINGS = new byte[MdpsMessageTypes.MAX_MESSAGE_TYPE_VALUE + 1];

    static {
        Arrays.fill(TYPE_MAPPINGS, VwdFeedConstants.MESSAGE_TYPE_UNKNOWN);

        // not implemented in mdps, so don't add mapping
//        TYPE_MAPPINGS[MdpsMessageTypes.DELETE_FIELD] = VwdFeedConstants.MESSAGE_TYPE_DELETE_FIELDS;

        TYPE_MAPPINGS[MdpsMessageTypes.STATIC_DELETE] = VwdFeedConstants.MESSAGE_TYPE_STATIC_DELETE;

        TYPE_MAPPINGS[MdpsMessageTypes.DELETE] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_DELETE;
        TYPE_MAPPINGS[MdpsMessageTypes.DELAY_DELETE] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_DELETE;
        TYPE_MAPPINGS[MdpsMessageTypes.STRANGLE_DELETE] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_DELETE;

        TYPE_MAPPINGS[MdpsMessageTypes.DELETE_FIELD] = VwdFeedConstants.MESSAGE_TYPE_DELETE_FIELDS;

        TYPE_MAPPINGS[MdpsMessageTypes.UPDATE] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.UPDATE_COMPFX] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.UPDATE_COMPFU] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.UPDATE_INDICATOR] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.UPDATE_XRATE] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.DELAY_UPDATE] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.STRANGLE_UPDATE] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.INTERNAL_UPDATE] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.UPDATE_AVS] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE;

        TYPE_MAPPINGS[MdpsMessageTypes.STATIC_UPDATE] = VwdFeedConstants.MESSAGE_TYPE_STATIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.STATIC_RECAP] = VwdFeedConstants.MESSAGE_TYPE_STATIC_RECAP;
        TYPE_MAPPINGS[MdpsMessageTypes.STATIC_RECAP_ON_NONWM] = VwdFeedConstants.MESSAGE_TYPE_STATIC_RECAP;

        // obsolete static messages
        TYPE_MAPPINGS[MdpsMessageTypes.STATIC_UPDATE_KL1] = VwdFeedConstants.MESSAGE_TYPE_STATIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.STATIC_UPDATE_KL2] = VwdFeedConstants.MESSAGE_TYPE_STATIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.STATIC_UPDATE_NONWM] = VwdFeedConstants.MESSAGE_TYPE_STATIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.STATIC_RECAP_KL1] = VwdFeedConstants.MESSAGE_TYPE_STATIC_RECAP;
        TYPE_MAPPINGS[MdpsMessageTypes.STATIC_RECAP_KL2] = VwdFeedConstants.MESSAGE_TYPE_STATIC_RECAP;
        TYPE_MAPPINGS[MdpsMessageTypes.STATIC_RECAP_NONWM] = VwdFeedConstants.MESSAGE_TYPE_STATIC_RECAP;
        TYPE_MAPPINGS[MdpsMessageTypes.STATIC_RECAP_ON_KL1] = VwdFeedConstants.MESSAGE_TYPE_STATIC_RECAP;
        TYPE_MAPPINGS[MdpsMessageTypes.STATIC_RECAP_ON_KL2] = VwdFeedConstants.MESSAGE_TYPE_STATIC_RECAP;

        // manually edited feed messages that do not trigger calculations (high/low/...)
        TYPE_MAPPINGS[MdpsMessageTypes.FIX_DY] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.FIX_SEND_DY] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.FIX_ST] = VwdFeedConstants.MESSAGE_TYPE_STATIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.FIX_SEND_ST] = VwdFeedConstants.MESSAGE_TYPE_STATIC_UPDATE;
        TYPE_MAPPINGS[MdpsMessageTypes.DEL_DY] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_DELETE;
        TYPE_MAPPINGS[MdpsMessageTypes.DEL_SEND_DY] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_DELETE;
        TYPE_MAPPINGS[MdpsMessageTypes.DEL_ST] = VwdFeedConstants.MESSAGE_TYPE_STATIC_DELETE;
        TYPE_MAPPINGS[MdpsMessageTypes.DEL_SEND_ST] = VwdFeedConstants.MESSAGE_TYPE_STATIC_DELETE;

        TYPE_MAPPINGS[MdpsMessageTypes.RECAP] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_RECAP;
        TYPE_MAPPINGS[MdpsMessageTypes.RECAP_ON] = VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_RECAP;

        TYPE_MAPPINGS[MdpsMessageTypes.NEWS_STORY] = VwdFeedConstants.MESSAGE_TYPE_NEWS;
        TYPE_MAPPINGS[MdpsMessageTypes.NEWS_SOURCE] = VwdFeedConstants.MESSAGE_TYPE_NEWS;

        TYPE_MAPPINGS[MdpsMessageTypes.PAGE_PDLFMT] = VwdFeedConstants.MESSAGE_TYPE_PAGE;

        TYPE_MAPPINGS[MdpsMessageTypes.INDICATOR_UPDATE] = VwdFeedConstants.MESSAGE_TYPE_RATIOS;

        TYPE_MAPPINGS[MdpsMessageTypes.BLOB] = VwdFeedConstants.MESSAGE_TYPE_BLOB;
    }

    private MdpsFeedUtils() {
    }

    public static ByteOrder getByteOrder(int version) {
        switch (version) {
            case 1:
                return ByteOrder.BIG_ENDIAN;
            case 3:
                return ByteOrder.LITTLE_ENDIAN;
            default:
                throw new IllegalArgumentException("unknown protocol " + version);
        }
    }

    static int getUnsignedByte(ByteBuffer bb, int pos) {
        return ((int) bb.get(pos)) & 0xFF;
    }

    static int getUnsignedByte(ByteBuffer bb) {
        return ((int) bb.get()) & 0xFF;
    }

    public static int getUnsignedShort(ByteBuffer bb, int pos) {
        return bb.getChar(pos);
    }

    public static int getUnsignedShort(ByteBuffer bb) {
        return bb.getChar();
    }

    public static int getUnsignedShort(ByteBuffer bb, ByteOrder order) {
        if (bb.order() == order) {
            return bb.getChar();
        }
        final int hi, lo;
        if (order == ByteOrder.LITTLE_ENDIAN) {
            lo = ((int) bb.get()) & 0xFF;
            hi = ((int) bb.get()) & 0xFF;
        }
        else {
            hi = ((int) bb.get()) & 0xFF;
            lo = ((int) bb.get()) & 0xFF;
        }
        return (hi << 8) | lo;
    }

    public static long getUnsignedInt(ByteBuffer bb) {
        return ((long) bb.getInt()) & 0xFFFFFFFFL;
    }

    /**
     * adds <tt>trackId</tt> id and timestamp at the end of <tt>track</tt> (i.e., the last
     * {@value #TRACK_ENTRY_LENGTH} bytes will be overridden).
     * @param track destination
     * @param trackId encoded module and process id as obtained from {@link #getTrackId(int)}.
     */
    static void expandTrack(byte[] track, int trackId) {
        ByteBuffer bbTrack = ByteBuffer.wrap(track).order(LITTLE_ENDIAN /* todo: correct? */);
        bbTrack.position(track.length - TRACK_ENTRY_LENGTH);
        bbTrack.putShort((short) trackId);
        bbTrack.putLong(NANOSECONDS.convert(System.currentTimeMillis(), MILLISECONDS)); // todo: use jni?
    }

    static long getTimestamp(ByteBuffer bb) {
        return getUnsignedInt(bb) * 1000 + bb.getShort();
    }

    public static long getPrice(ByteBuffer bb, boolean mdpsFormat) {
        return mdpsFormat ? getMdpsPrice(bb) : getPrice(bb);
    }

    public static long getPrice0(ByteBuffer bb, boolean mdpsFormat) {
        return mdpsFormat ? getMdpsPrice0(bb) : getPrice(bb);
    }

    public static long getPrice(ByteBuffer bb) {
        return PriceCoder.encode(bb.getInt(), bb.get());
    }

    public static long getMdpsPrice(ByteBuffer bb) {
        return encodePrice(bb.getInt(), bb.get());
    }

    public static long getMdpsPrice0(ByteBuffer bb) {
        final int base = bb.getInt();
        final int exp = bb.get();
        return isValidExponent(exp) ? encodePrice(base, exp) : 0L;
    }

    private static boolean isValidExponent(int i) {
        return (Math.abs(i) & 0xC0) == 0;
    }

    public static ByteBuffer putMdpsPrice(ByteBuffer bb, long value) {
        return bb.putInt((int) value).put(getMdpsPriceScale(value));
    }

    public static ByteBuffer putMdpsDate(ByteBuffer bb, int yyyymmdd) {
        bb.putShort((short) (yyyymmdd / 10000));
        bb.put((byte) ((yyyymmdd % 10000) / 100));
        bb.put((byte) (yyyymmdd % 100));
        return bb;
    }

    public static byte getMdpsPriceScale(long mdpsPrice) {
        return (byte) (mdpsPrice >> 32);
    }

    public static int getMdpsPriceBase(long mdpsPrice) {
        return (int) mdpsPrice;
    }

    static int getDate(ByteBuffer bb) {
        int year = bb.getShort();
        int month = bb.get();
        int day = bb.get();
        if (year == 0) {
            return (month == 0 && day == 0) ? 0 : -1;
        }
        return (year >= 1900 && month > 0 && month < 13 && day > 0 && day < 32)
                ? (year * 10000 + month * 100 + day)
                : -1;
    }

    public static long encodePrice(int unscaled, int scale) {
        return (((long) unscaled) & 0xFFFFFFFFL) | ((long) scale << 32);
    }

    public static long encodePrice(BigDecimal bd) {
        return doEncodePrice(bd.stripTrailingZeros());
    }

    private static long doEncodePrice(BigDecimal bd) {
        final long unscaled = bd.unscaledValue().longValue();
        assert unscaled >= Integer.MIN_VALUE && unscaled <= Integer.MAX_VALUE : "out-of-range: " + bd.toPlainString();
        return (0xFFFFFFFFL & unscaled) + ((0xFFL & -bd.scale()) << 32);
    }

    public static BigDecimal decodePrice(long v) {
        return BigDecimal.valueOf(getMdpsPriceBase(v), -getMdpsPriceScale(v));
    }

    public static int addZeroBit(int b) {
        // positive values up to 63 start with 00_xx_xx_xx, modify to 01_xx_xx_xx
        // negative values up to -63 start with 11_xx_xx_xx, modify to 10_xx_xx_xx
        return (b >= 0) ? (b | 0x40) : (b & ~0x40);
    }

    public static int removeZeroBit(int b) {
        return (b >= 0) ? (b & 0x3F) : (b | 0x40);
    }

    public static boolean hasZeroBit(int b) {
        return b != removeZeroBit(b);
    }

    public static int decodeDate(int i) {
        return 10000 * (i >> 16) + 100 * ((i >> 8) & 0xFF) + (i & 0xFF);
    }

    private static int encodeDate(int yyyy, int mm, int dd) {
        return (yyyy << 16) | (mm << 8) | dd;
    }

    public static int encodeDate(DateTime dt) {
        return encodeDate(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
    }

    public static int encodeDate(int yyyymmdd) {
        int yyyy = yyyymmdd / 10000;
        int mm = (yyyymmdd % 10000) / 100;
        int dd = yyyymmdd % 100;
        return encodeDate(yyyy, mm, dd);
    }

    public static int getTime(ByteBuffer bb) {
        return decodeTime(bb.getInt());
    }

    public static int decodeTimeMillis(int i) {
        return i & 0x7FFF;
    }

    public static int decodeTime(int i) {
        int hh = i >>> 27;
        int mm = (i >>> 21) & 0x3F;
        int ss = (i >>> 15) & 0x3F;
        return hh * 3600 + mm * 60 + ss;
    }

    public static LocalTime decodeLocalTime(int i) {
        return new LocalTime(i >>> 27, (i >>> 21) & 0x3F, (i >>> 15) & 0x3F, i & 0x7FFF);
    }

    public static int encodeTime(int hh, int mm, int ss, int ms) {
        return (hh << 27) | (mm << 21) | (ss << 15) | ms;
    }

    public static int encodeTime(DateTime dt) {
        return encodeTime(dt.getHourOfDay(), dt.getMinuteOfHour(), dt.getSecondOfMinute(),
                dt.getMillisOfSecond());
    }

    public static int encodeTime(LocalTime lt) {
        return encodeTime(lt.getHourOfDay(), lt.getMinuteOfHour(), lt.getSecondOfMinute(),
                lt.getMillisOfSecond());
    }

    public static int encodeTime(int secondsInDay, int ms) {
        int ss = secondsInDay % 60;
        int mm = (secondsInDay % 3600) / 60;
        int hh = secondsInDay / 3600;
        return encodeTime(hh, mm, ss, ms);
    }

    public static int encodeTime(int secondsInDay) {
        return encodeTime(secondsInDay, 0);
    }

    public static int toSecondOfDay(int time) {
        return decodeTime(time);
    }

    public static byte toXfeedMessageType(int mdpsMessageType) {
        if (mdpsMessageType < 0 || mdpsMessageType > MdpsMessageTypes.MAX_MESSAGE_TYPE_VALUE) {
            return VwdFeedConstants.MESSAGE_TYPE_UNKNOWN;
        }
        return TYPE_MAPPINGS[mdpsMessageType];
    }
}
