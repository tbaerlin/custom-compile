/*
 * AggregatedTickEncoder.java
 *
 * Created on 03.03.2005 15:58:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.nio.ByteBuffer;

import de.marketmaker.istar.common.util.PriceCoder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id$
 */
public class AggregatedTickEncoder {
    private static final int MAX_UNSIGNED_BYTE = 0xff;
    private static final int MAX_UNSIGNED_SHORT = 0xffff;
    private static final long MAX_UNSIGNED_INT = 0xffffffffL;

    private ByteBuffer bb = ByteBuffer.wrap(new byte[64]);

    private int header;

    private int numEncoded = 0;

    public AggregatedTickEncoder() {
    }

    public int getNumEncoded() {
        return numEncoded;
    }

    public ByteBuffer encode(AggregatedTickData data) {
        this.header = 0;
        this.bb.clear();

        this.bb.clear();
        this.bb.position(2); // leave space for header

        encodeTime(data.getTime(), data.getLastTime());
        encodeNum(data.getNumberOfAggregatedTicks());
        encodeVolume(data.getVolume());
        encodeOhlc(data.getLastOpen(), data.getOpen(), data.getHigh(), data.getLow(), data.getClose());

        this.bb.putShort(0, (short) this.header);

//        String s = "0000000000000000" + Integer.toString(this.header, 2);
//        System.out.println("en-header = " + s.substring(s.length() - 16));

        this.numEncoded++;

        this.bb.flip();
        return this.bb;
    }

    private void encodeOhlc(long lastOpen, long open, long high, long low, long close) {
        if (high == open) {
            this.header |= AggregatedTickCoder.HIGH_EQUALS_OPEN;
        }
        if (low == open) {
            this.header |= AggregatedTickCoder.LOW_EQUALS_OPEN;
        }
        if (close == open) {
            this.header |= AggregatedTickCoder.CLOSE_EQUALS_OPEN;
        }

        if (PriceCoder.isEncodedWithDefaultFractions(open | high | low | close)) {
            final long[] ohlc = new long[4];
            if (lastOpen == -1) {
                this.header |= AggregatedTickCoder.OPEN_ABSOLUTE;
                ohlc[0] = open;
            }
            else {
                ohlc[0] = open - lastOpen;
                if (ohlc[0] == 0) {
                    this.header |= AggregatedTickCoder.OPEN_UNCHANGED;
                }
            }
            ohlc[1] = open - high;
            ohlc[2] = open - low;
            ohlc[3] = open - close;
            encodeAndWrite(ohlc, lastOpen == -1);
        }
        else {
            this.header |= AggregatedTickCoder.OHLC_RAW;
            this.bb.putLong(open);
            if (high != open) {
                this.bb.putLong(high);
            }
            if (low != open) {
                this.bb.putLong(low);
            }
            if (close != open) {
                this.bb.putLong(close);
            }
        }        
    }

    private void encodeAndWrite(long[] ohlc, boolean alwaysWriteOpen) {
        final int factor = getFactor(ohlc);
        if (factor == 10) {
            this.header |= AggregatedTickCoder.OHLC_ONE_NULL;
        }
        else if (factor == 100) {
            this.header |= AggregatedTickCoder.OHLC_TWO_NULLS;
        }
        else if (factor == 1000) {
            this.header |= AggregatedTickCoder.OHLC_THREE_NULLS;
        }

        long absMaxToStore = 0;

        for (int i = 0; i < ohlc.length; i++) {
            ohlc[i] /= factor;
            absMaxToStore = Math.max(absMaxToStore, Math.abs(ohlc[i]));
        }

        if (absMaxToStore <= Byte.MAX_VALUE) {
            this.header |= AggregatedTickCoder.OHLC_TYPE_BYTE;
            for (int i = 0; i < ohlc.length; i++) {
                if (ohlc[i] != 0 || (i == 0 && alwaysWriteOpen)) {
                    this.bb.put((byte) ohlc[i]);
                }
            }
        }
        else if (absMaxToStore <= Short.MAX_VALUE) {
            this.header |= AggregatedTickCoder.OHLC_TYPE_SHORT;
            for (int i = 0; i < ohlc.length; i++) {
                if (ohlc[i] != 0 || (i == 0 && alwaysWriteOpen)) {
                    this.bb.putShort((short) ohlc[i]);
                }
            }
        }
        else if (absMaxToStore <= Integer.MAX_VALUE) {
            this.header |= AggregatedTickCoder.OHLC_TYPE_INT;
            for (int i = 0; i < ohlc.length; i++) {
                if (ohlc[i] != 0 || (i == 0 && alwaysWriteOpen)) {
                    this.bb.putInt((int) ohlc[i]);
                }
            }
        }
        else {
            this.header |= AggregatedTickCoder.OHLC_TYPE_LONG;
            for (int i = 0; i < ohlc.length; i++) {
                if (ohlc[i] != 0 || (i == 0 && alwaysWriteOpen)) {
                    this.bb.putLong(ohlc[i]);
                }
            }
        }
    }

    private int getFactor(long[] ohlc) {
        NEXT_FACTOR: for (int i = 1000; i > 1; i /= 10) {
            for (int j = 0; j < ohlc.length; j++) {
                if ((ohlc[j] % i) != 0) {
                    continue NEXT_FACTOR;
                }
            }
            return i;
        }
        return 1;
    }

    private void encodeVolume(long volume) {
        if (volume < MAX_UNSIGNED_BYTE) {
            this.header |= AggregatedTickCoder.VOLUME_TYPE_BYTE;
            this.bb.put((byte)(volume & MAX_UNSIGNED_BYTE));
        }
        else if (volume < MAX_UNSIGNED_SHORT) {
            this.header |= AggregatedTickCoder.VOLUME_TYPE_SHORT;
            this.bb.putShort((short) (volume & MAX_UNSIGNED_SHORT));
        }
        else if (volume < MAX_UNSIGNED_INT) {
            this.header |= AggregatedTickCoder.VOLUME_TYPE_INT;
            this.bb.putInt((int) (volume & MAX_UNSIGNED_INT));
        }
        else {
            this.header |= AggregatedTickCoder.VOLUME_TYPE_LONG;
            this.bb.putLong(volume);
        }
    }

    private void encodeNum(int num) {
        if (num < MAX_UNSIGNED_BYTE) {
            this.header |= AggregatedTickCoder.NUMBER_TYPE_BYTE;
            this.bb.put((byte)(num & MAX_UNSIGNED_BYTE));
        }
        else if (num < MAX_UNSIGNED_SHORT) {
            this.header |= AggregatedTickCoder.NUMBER_TYPE_SHORT;
            this.bb.putShort((short) (num & MAX_UNSIGNED_SHORT));
        }
        else {
            this.header |= AggregatedTickCoder.NUMBER_TYPE_INT;
            this.bb.putInt(num);
        }
    }

    private void encodeTime(int time, int lastTime) {
        if (lastTime == -1) {
            encodeAbsoluteTimeAndWrite(time);
        }
        else {
            final int diff = time - lastTime;
            if (diff < 512) {
                if (diff > MAX_UNSIGNED_BYTE) {
                    this.header |= AggregatedTickCoder.TIME_MSB;
                }
                this.bb.put((byte) (diff & MAX_UNSIGNED_BYTE));
            }
            else {
                encodeAbsoluteTimeAndWrite(time);
            }
        }
    }

    private void encodeAbsoluteTimeAndWrite(int time) {
        this.header |= AggregatedTickCoder.TIME_ABSOLUTE;
        if (time > MAX_UNSIGNED_SHORT) {
            this.header |= AggregatedTickCoder.TIME_MSB;
        }
        this.bb.putShort((short) (time & 0xffff));
    }


}
