/*
 * AggregatedTickDecoder.java
 *
 * Created on 03.03.2005 15:58:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AggregatedTickDecoder implements Iterable<RawAggregatedTick>, Iterator<RawAggregatedTick> {

    private final ByteBuffer bb;

    private final RawAggregatedTick tick = new RawAggregatedTick();

    public AggregatedTickDecoder(AbstractTickRecord.TickItem item) {
        this(ByteBuffer.wrap(item.getData()));
    }

    public AggregatedTickDecoder(ByteBuffer bb) {
        this.bb = bb;
    }

    @Override
    public Iterator<RawAggregatedTick> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return this.bb.hasRemaining();
    }

    @Override
    public RawAggregatedTick next() {
        final int header = bb.getShort() & 0xffff;

//        String s = "0000000000000000" + Integer.toString(header, 2);
//        System.out.println("de-header = " + s.substring(s.length() - 16));

        decodeTime(header);
        decodeNum(header);
        decodeVolume(header);
        decodeOhlc(header);

        return this.tick;
    }

    private void decodeOhlc(int header) {
        if ((header & AggregatedTickCoder.OHLC_RAW) == 0) {
            long[] ohlc = new long[4];
            final int type = header & AggregatedTickCoder.OHLC_TYPE_MASK;

            if ((header & AggregatedTickCoder.OPEN_UNCHANGED) == 0) {
                ohlc[0] = readPrice(type);
            }
            if ((header & AggregatedTickCoder.HIGH_EQUALS_OPEN) == 0) {
                ohlc[1] = readPrice(type);
            }
            if ((header & AggregatedTickCoder.LOW_EQUALS_OPEN) == 0) {
                ohlc[2] = readPrice(type);
            }
            if ((header & AggregatedTickCoder.CLOSE_EQUALS_OPEN) == 0) {
                ohlc[3] = readPrice(type);
            }

            final int factor = decodeFactor(header);
            for (int i = 0; i < ohlc.length; i++) {
                ohlc[i] *= factor;
            }

            if ((header & AggregatedTickCoder.OPEN_ABSOLUTE) == 0) {
                this.tick.setOpen(this.tick.getOpen() + ohlc[0]);
            }
            else {
                this.tick.setOpen(ohlc[0]);
            }

            this.tick.setHigh(this.tick.getOpen() - ohlc[1]);
            this.tick.setLow(this.tick.getOpen() - ohlc[2]);
            this.tick.setClose(this.tick.getOpen() - ohlc[3]);
        }
        else {
            this.tick.setOpen(this.bb.getLong());
            if ((header & AggregatedTickCoder.HIGH_EQUALS_OPEN) == 0) {
                this.tick.setHigh(this.bb.getLong());
            }
            else {
                this.tick.setHigh(this.tick.getOpen());
            }
            if ((header & AggregatedTickCoder.LOW_EQUALS_OPEN) == 0) {
                this.tick.setLow(this.bb.getLong());
            }
            else {
                this.tick.setLow(this.tick.getOpen());
            }
            if ((header & AggregatedTickCoder.CLOSE_EQUALS_OPEN) == 0) {
                this.tick.setClose(this.bb.getLong());
            }
            else {
                this.tick.setClose(this.tick.getOpen());
            }
        }

    }

    private long readPrice(int type) {
        switch (type) {
            case AggregatedTickCoder.OHLC_TYPE_BYTE:
                return this.bb.get();
            case AggregatedTickCoder.OHLC_TYPE_SHORT:
                return this.bb.getShort();
            case AggregatedTickCoder.OHLC_TYPE_INT:
                return this.bb.getInt();
            case AggregatedTickCoder.OHLC_TYPE_LONG:
                return this.bb.getLong();
        }
        return 0;
    }


    private int decodeFactor(int header) {
        int factorEncoding = header & AggregatedTickCoder.OHLC_NULLS_MASK;
        switch (factorEncoding) {
            case AggregatedTickCoder.OHLC_ONE_NULL:
                return 10;
            case AggregatedTickCoder.OHLC_TWO_NULLS:
                return 100;
            case AggregatedTickCoder.OHLC_THREE_NULLS:
                return 1000;
            default:
                return 1;
        }
    }

    private void decodeVolume(int header) {
        final int encodedType = (header & AggregatedTickCoder.VOLUME_TYPE_MASK);
        if (encodedType == AggregatedTickCoder.VOLUME_TYPE_BYTE) {
            this.tick.setVolume(this.bb.get() & 0xff);
        }
        else if (encodedType == AggregatedTickCoder.VOLUME_TYPE_SHORT) {
            this.tick.setVolume(this.bb.getShort() & 0xffff);
        }
        else if (encodedType == AggregatedTickCoder.VOLUME_TYPE_INT) {
            this.tick.setVolume(this.bb.getInt() & 0xffffffffL);
        }
        else {
            this.tick.setVolume(this.bb.getLong());
        }
    }

    private void decodeNum(int header) {
        final int encodedType = (header & AggregatedTickCoder.NUMBER_TYPE_MASK);
        if (encodedType == AggregatedTickCoder.NUMBER_TYPE_BYTE) {
            this.tick.setNumberOfAggregatedTicks(this.bb.get() & 0xff);
        }
        else if (encodedType == AggregatedTickCoder.NUMBER_TYPE_SHORT) {
            this.tick.setNumberOfAggregatedTicks(this.bb.getShort() & 0xffff);
        }
        else {
            this.tick.setNumberOfAggregatedTicks(this.bb.getInt());
        }
    }

    private void decodeTime(final int header) {
        final boolean timeAbsolute = (header & AggregatedTickCoder.TIME_ABSOLUTE) != 0;

        if (timeAbsolute) {
            int time = this.bb.getShort() & 0xffff;
            if ((header & AggregatedTickCoder.TIME_MSB) != 0) {
                time |= 0x10000;
            }
            tick.setTime(time);
        }
        else {
            int time = this.bb.get() & 0xff;
            if ((header & AggregatedTickCoder.TIME_MSB) != 0) {
                time |= 0x100;
            }
            tick.setTime(tick.getTime() + time);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
