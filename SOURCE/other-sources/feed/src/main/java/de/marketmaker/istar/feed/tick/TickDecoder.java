/*
 * TickDecoder.java
 *
 * Created on 15.11.2004 15:49:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.util.Iterator;

import java.nio.ByteBuffer;

import de.marketmaker.istar.common.util.PriceCoder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TickDecoder implements Iterable<RawTick>, Iterator<RawTick> {
    private final static int[] FACTORS = new int[]{1, 10, 100, 1000, 10000, 100000};

    private ByteBuffer buffer;
    private String lastSupplement = null;
    private String lastNotierungsart = null;
    private long lastPrice = 0;
    private int lastTime = Integer.MIN_VALUE;
    private long lastVolume = Long.MIN_VALUE;

    private long lastTradePrice = 0;
    private long lastBidPrice = 0;
    private long lastAskPrice = 0;

    private static final int MAX_SUPPLEMENT_LENGTH = 4;
    final RawTick tick = new RawTick();

    public TickDecoder(ByteBuffer buffer) {
        // BUGFIX for a miscalculation of tick length in chicago
        int n = buffer.position();
        while (buffer.get(n) == 0) {
            n++;
        }
        buffer.position(n);

        this.buffer = buffer;
    }

    public Iterator<RawTick> iterator() {
        return this;
    }

    public boolean hasNext() {
        return this.buffer.hasRemaining();
    }

    public RawTick next() {
        fillWithNext(this.tick);
        return this.tick;
    }

    private void fillWithNext(RawTick tick) {
        if (!hasNext()) {
            throw new IllegalStateException("no more data");
        }

        tick.setWithClose(false);
        tick.setWithKassa(false);
        tick.setYield(Long.MIN_VALUE);

        final byte header = this.buffer.get();
        final int type;
        if ((header & TickCoder.TYPE_MASK) == 0) {
            final byte supplementalHeader = this.buffer.get();
            if ((supplementalHeader & TickCoder.TICK_SUSPEND) != 0) {
                type = supplementalHeader;
            }
            else {
                type = supplementalHeader & TickCoder.TYPE_MASK;
                handleSupplementalHeader(supplementalHeader, tick);
            }
        }
        else {
            type = header & TickCoder.TYPE_MASK;
        }

//        final String x = Integer.toBinaryString(header);
//        System.out.println(LEADING_NULLS.substring(0, 8 - x.length()) + x);

        tick.setHeader(header);
        tick.setType(type);

        decodeTime(header, tick);
        decodeSupplement(header, tick);

        if (tick.isTrade()) {
            final byte tickHeader = this.buffer.get();
            tick.setTradeHeader(tickHeader);
            long price = decodePrice(tickHeader);
            if (isOutOfRange(price)) {
                price = this.lastTradePrice;
                tick.setPricePresent(false);
            }
            else {
                tick.setPricePresent(true);
                this.lastTradePrice = price;
            }
            tick.setPrice(price);
            tick.setVolume(decodeVolume(tickHeader));
        }
        if (tick.isBid()) {
            final byte tickHeader = this.buffer.get();
            long bidPrice = decodePrice(tickHeader);
            if (isOutOfRange(bidPrice)) {
                bidPrice = this.lastBidPrice;
                tick.setBidPresent(false);
            }
            else {
                this.lastBidPrice = bidPrice;
            }
            tick.setBidPrice(bidPrice);
            tick.setBidVolume(decodeVolume(tickHeader));
        }
        if (tick.isAsk()) {
            final byte tickHeader = this.buffer.get();
            long askPrice = decodePrice(tickHeader);
            if (isOutOfRange(askPrice)) {
                askPrice = this.lastAskPrice;
                tick.setAskPresent(false);
            }
            else {
                this.lastAskPrice = askPrice;
            }
            tick.setAskPrice(askPrice);
            tick.setAskVolume(decodeVolume(tickHeader));
        }
    }

    private static boolean isOutOfRange(long x) {
        if (x > -100000000000000L && x < 100000000000000L) {
            return false;
        }
        if (x == Long.MIN_VALUE) {
            return true;
        }
        final double d = PriceCoder.decodeAsDouble(x);
        return (d < -100000000000d || d > 100000000000d);
    }

    private void handleSupplementalHeader(final byte supplementalHeader, final RawTick tick) {
        if ((supplementalHeader & TickCoder.TICK_WITH_CLOSE) != 0) {
            tick.setWithClose(true);
        }
        if ((supplementalHeader & TickCoder.TICK_WITH_KASSA) != 0) {
            tick.setWithKassa(true);
        }
        if ((supplementalHeader & TickCoder.TICK_HAS_YIELD) != 0) {
            final long yield;
            if ((supplementalHeader & TickCoder.TICK_YIELD_TYPE_INT) != 0) {
                yield = this.buffer.getInt();
            }
            else {
                yield = this.buffer.getLong();
            }
            tick.setYield(yield);
        }
    }

    private void decodeSupplement(int header, RawTick tick) {
        final boolean supplementPresent = (header & TickCoder.SUPPLEMENT_PRESENT) != 0;

        if (!supplementPresent) {
            tick.setSupplement(null);
            tick.setNotierungsart(null);
            return;
        }

        final boolean supplementUnchanged = (header & TickCoder.SUPPLEMENT_UNCHANGED) != 0;

        if (!supplementUnchanged) {
            final byte flagsOrFirstByte = this.buffer.get();
            if (flagsOrFirstByte == 0) {
                this.lastSupplement = "";
                this.lastNotierungsart = null;
            }
            else if ((flagsOrFirstByte & 0x03) == flagsOrFirstByte) {
                char notierungsart = (char) (this.buffer.get() & 0xff);
                this.lastNotierungsart = Character.toString(notierungsart);
                if ((flagsOrFirstByte & TickCoder.WITH_SUPPLEMENT) != 0) {
                    if (notierungsart == '\u0000') {
                        // hack/bugfix: before TickBuilderData ignored empty notierungsart strings,
                        // TickEncoder would just write the \u0000 notierungsart but NOT the
                        // supplement.
                        this.lastSupplement = null;
                    }
                    else {
                        this.lastSupplement = readSupplement(this.buffer.get());
                    }
                }
                else {
                    this.lastSupplement = null;
                }
            }
            else {
                this.lastSupplement = readSupplement(flagsOrFirstByte);
                this.lastNotierungsart = null;
            }
        }
        tick.setNotierungsart(this.lastNotierungsart);
        tick.setSupplement(this.lastSupplement);
    }

    private String readSupplement(byte first) {
        final byte[] supp = new byte[MAX_SUPPLEMENT_LENGTH];
        supp[0] = first;
        if (first == 0) {
            supp[0] = this.buffer.get();
            if (supp[0] == 0) {
                return "";
            }
        }
        int num = 1;
        while ((num < MAX_SUPPLEMENT_LENGTH) && (supp[num++] = this.buffer.get()) != 0) {
            // empty
        }
        return new String(supp, 0, num - 1);
    }

    private long decodeVolume(int header) {
        final boolean volumePresent = (header & TickCoder.VOLUME_PRESENT) != 0;

        if (!volumePresent) {
            return Long.MIN_VALUE;
        }

        final boolean volumeUnchanged = (header & TickCoder.VOLUME_UNCHANGED) != 0;
        if (volumeUnchanged) {
            return this.lastVolume;
        }

        this.lastVolume = decodeValue(this.buffer.get());
        if (this.lastVolume < 0) {
            this.lastVolume &= 0xffffffffL;
        }
        return this.lastVolume;
    }

    private long decodePrice(int header) {
        final boolean pricePresent = (header & TickCoder.PRICE_PRESENT) != 0;
        final boolean priceAbsolute = (header & TickCoder.PRICE_ABSOLUTE) != 0;

//        System.out.println(pricePresent + " " + priceAbsolute);
        if (!pricePresent) {
            if (priceAbsolute) {
                return Long.MIN_VALUE;
            }
            else {
                return this.lastPrice;
            }
        }

        final long price = decodeValue(header);

        this.lastPrice = priceAbsolute ? price : (this.lastPrice + price);
        return this.lastPrice;
    }

    private long decodeValue(int header) {
        final int valueType = header & TickCoder.DATA_TYPE_MASK;
        long value;

        switch (valueType) {
            case TickCoder.DATA_TYPE_BYTE:
                value = this.buffer.get();
                break;
            case TickCoder.DATA_TYPE_SHORT:
                value = this.buffer.getShort();
                break;
            case TickCoder.DATA_TYPE_INT:
                value = this.buffer.getInt();
                break;
            default:
                value = this.buffer.getLong();
                break;
        }

        final int numNulls = (header & TickCoder.NULLS_MASK) >> 2;
        if (numNulls != 0) {
            value *= FACTORS[numNulls];
        }
        return value;
    }

    private void decodeTime(final int header, final RawTick tick) {
        final boolean timeUnchanged = (header & TickCoder.TIME_UNCHANGED) != 0;
        if (timeUnchanged) {
            tick.setTime(this.lastTime);
        }
        else {
            final boolean timeAbsolute = (header & TickCoder.TIME_ABSOLUTE) != 0;

            if (timeAbsolute) {
                int time = this.buffer.getShort() & 0xffff;
                if ((header & TickCoder.TIME_MSB) != 0) {
                    time |= 0x10000;
                }
                tick.setTime(time);
            }
            else {
                int time = this.buffer.get() & 0xff;
                if ((header & TickCoder.TIME_MSB) != 0) {
                    time |= 0x100;
                }
                tick.setTime(this.lastTime + time);
            }
        }

        this.lastTime = tick.getTime();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public int numBids() {
        return numType(0);
    }

    public int numAsks() {
        return numType(1);
    }

    public int numTrades() {
        return numType(2);
    }

    private int numType(int type) {
        final RawTick tick = new RawTick();

        final int pos = this.buffer.position();

        int count = 0;
        while (hasNext()) {
            fillWithNext(tick);
            if (type == 0 && tick.isBid()) {
                count++;
            }
            else if (type == 1 && tick.isAsk()) {
                count++;
            }
            else if (type == 2 && tick.isTrade()) {
                count++;
            }
        }

        this.buffer.position(pos);

        return count;
    }
}
