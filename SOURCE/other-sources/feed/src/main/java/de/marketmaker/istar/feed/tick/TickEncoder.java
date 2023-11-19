/*
 * TickCoder.java
 *
 * Created on 15.11.2004 11:36:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.nio.ByteBuffer;

import de.marketmaker.istar.common.util.PriceCoder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TickEncoder {
    private TickDataDefault tickdata;
    private int header;

    private final ByteBuffer bb = ByteBuffer.wrap(new byte[255]);

    private static final int MAX_UNSIGNED_BYTE = 255;
    private static final int MAX_UNSIGNED_SHORT = 65535;

    public TickEncoder() {
    }

    public void initialize(TickDataDefault tickdata) {
        this.tickdata = tickdata;

        this.header = 0;
        this.bb.clear();
    }

    private void processSupplement(TickBuilderData data) {
        if (data.isWithoutSupplement()) {
            return;
        }

        final byte[] supplement = data.getSupplementIfChanged(this.tickdata.getLastSupplement());

        this.header |= TickCoder.SUPPLEMENT_PRESENT;

        if (supplement == null) {
            this.header |= TickCoder.SUPPLEMENT_UNCHANGED;
            return;
        }

        for (byte aSupplement : supplement) {
            this.bb.put(aSupplement);
            if (aSupplement == 0) {
                break;
            }
        }

        this.tickdata.setLastSupplement(supplement);
    }


    private void processVolume(int volume) {
        if (volume == Integer.MIN_VALUE) {
            return;
        }

        this.header |= TickCoder.VOLUME_PRESENT;

        if (volume == tickdata.getLastVolume()) {
            this.header |= TickCoder.VOLUME_UNCHANGED;
            return;
        }

        final int index = this.bb.position();
        this.bb.put((byte) 0);
        this.bb.put(index, (byte) encodeValueAndWrite(volume));

        this.tickdata.setLastVolume(volume);
    }

    private void processPrice(long price) {
        if (price == Long.MIN_VALUE) {
            // no price in this record.
            this.header |= TickCoder.PRICE_ABSOLUTE;
        }
        else if (!PriceCoder.isEncodedWithDefaultFractions(price)){
            // price stored as is
            this.header |= TickCoder.PRICE_ABSOLUTE;
            this.header |= TickCoder.PRICE_PRESENT;
            this.header |= encodeValueAndWrite(price);
            this.tickdata.setLastPrice(Long.MIN_VALUE);
        }
        else if (this.tickdata.getLastPrice() == Long.MIN_VALUE) {
            // no previous price
            this.header |= TickCoder.PRICE_ABSOLUTE;
            this.header |= TickCoder.PRICE_PRESENT;
            this.header |= encodeValueAndWrite(price);
            this.tickdata.setLastPrice(price);
        }
        else {
            final long priceDiff = price - this.tickdata.getLastPrice();

            if (priceDiff == 0) {
                // leave PRICE_PRESENT and PRICE_ABSOLUTE as 0
                // to signal unchanged price
            }
            else {
                this.header |= TickCoder.PRICE_PRESENT;
                this.header |= encodeValueAndWrite(priceDiff);
                this.tickdata.setLastPrice(price);
            }
        }
    }

    private void processTime(int time) {
        if (this.tickdata.getLastTime() < 0) {
            encodeAbsoluteTimeAndWrite(time);
            this.tickdata.setLastTime(time);
            return;
        }

        final int timeDiff = time - this.tickdata.getLastTime();
        if (timeDiff == 0) {
            this.header |= TickCoder.TIME_UNCHANGED;
        }
        else {
            this.tickdata.setLastTime(time);

            if (timeDiff > 0 && timeDiff < 512) {
                if (timeDiff > MAX_UNSIGNED_BYTE) {
                    this.header |= TickCoder.TIME_MSB;
                    this.bb.put((byte) (timeDiff & 0xff));
                }
                else {
                    this.bb.put((byte) timeDiff);
                }
            }
            else {
                encodeAbsoluteTimeAndWrite(time);
            }
        }
    }

    private void encodeAbsoluteTimeAndWrite(int time) {
        this.header |= TickCoder.TIME_ABSOLUTE;
        if (time > MAX_UNSIGNED_SHORT) {
            this.header |= TickCoder.TIME_MSB;
        }
        this.bb.putShort((short) (time & 0xffff));
    }

    private int encodeValueAndWrite(long value) {
        if ((value % 1000) == 0) {
            return TickCoder.THREE_NULLS | writeValue(value / 1000);
        }

        if ((value % 100) == 0) {
            return TickCoder.TWO_NULLS | writeValue(value / 100);
        }

        if ((value % 10) == 0) {
            return TickCoder.ONE_NULL | writeValue(value / 10);
        }

        return TickCoder.ZERO_NULLS | writeValue(value);
    }

    /**
     * Writes a volume or price value
     */
    private int writeValue(long value) {
        if (value <= Byte.MAX_VALUE && value >= Byte.MIN_VALUE) {
            this.bb.put((byte) value);
            return TickCoder.DATA_TYPE_BYTE;
        }

        if (value <= Short.MAX_VALUE && value >= Short.MIN_VALUE) {
            this.bb.putShort((short) value);
            return TickCoder.DATA_TYPE_SHORT;
        }

        if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE) {
            this.bb.putInt((int) value);
            return TickCoder.DATA_TYPE_INT;
        }

        this.bb.putLong(value);
        return TickCoder.DATA_TYPE_LONG;
    }

    public ByteBuffer finish() {
        this.bb.flip();
        return this.bb;
    }

    public ByteBuffer addSuspendStart(int tickTime) {
        return addSuspend(tickTime, TickCoder.TICK_SUSPEND_START);
    }

    public ByteBuffer addSuspendEnd(int tickTime) {
        return addSuspend(tickTime, TickCoder.TICK_SUSPEND_END);
    }

    private ByteBuffer addSuspend(int tickTime, final int type) {
        // reserve space for header
        this.bb.put((byte) 0);
        this.bb.put((byte) (TickCoder.TICK_SUSPEND | type));

        processTime(tickTime);
        this.bb.put(0, (byte) this.header);
        return finish();
    }

    public void add(TickParameters tickparams, int time, TickBuilderData builderData) {
        // reserve space for header
        this.bb.put((byte) 0);
        this.header = 0;

        final boolean nonStandardTick = tickparams.isNonStandardTick();
        final int typeEncoding = getTypeEncoding(tickparams);

        if (nonStandardTick) {
            // write 2nd header byte:
            this.bb.put((byte) typeEncoding);

            if (tickparams.isWithYield()) {
                if ((typeEncoding & TickCoder.TICK_YIELD_TYPE_INT) != 0) {
                    this.bb.putInt((int) tickparams.getYield());
                }
                else {
                    this.bb.putLong(tickparams.getYield());
                }
            }
        }
        else {
            this.header |= typeEncoding;
        }

        processTime(time);
        processSupplement(builderData);

        this.bb.put(0, (byte) this.header);
    }

    private int getTypeEncoding(TickParameters tickparams) {
        int result = 0;
        if (tickparams.isTrade()) {
            result |= TickCoder.TYPE_TRADE;
        }
        if (tickparams.isBid()) {
            result |= TickCoder.TYPE_BID;
        }
        if (tickparams.isAsk()) {
            result |= TickCoder.TYPE_ASK;
        }

        // hasYield test and close test are only true, if
        // tick is a non-standard tick

        if (tickparams.isWithYield()) {
            result |= TickCoder.TICK_HAS_YIELD;
            if (tickparams.getYield() >= Integer.MIN_VALUE && tickparams.getYield() <= Integer.MAX_VALUE) {
                result |= TickCoder.TICK_YIELD_TYPE_INT;
            }
        }
        if (tickparams.isWithClose()) {
            result |= TickCoder.TICK_WITH_CLOSE;
        }
        if (tickparams.isWithKassa()) {
            result |= TickCoder.TICK_WITH_KASSA;
        }

        return result;
    }

    public void addPrice(long price, int volume) {
        final int headerPos = this.bb.position();
        this.bb.put((byte) 0);

        this.header = 0;

        processPrice(price);
        processVolume(volume);

        this.bb.put(headerPos, (byte) this.header);
    }

}
