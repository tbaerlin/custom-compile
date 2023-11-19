/*
 * EodMonth.java
 *
 * Created on 11.01.13 12:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.nio.ByteBuffer;

import de.marketmaker.istar.merger.provider.history.eod.BCD;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

/**
 * @author zzhao
 */
class EodMonth {

    static byte[] setPrice(ByteBuffer buf, int days, int dayBit, byte[] bytes) {
        final int dayBits = EodUtil.getDayBits(days, dayBit, bytes.length == 0);
        if (dayBits == 0) {
            return EodUtil.EMPTY_BA;
        }

        final ByteBuffer bb = ByteBuffer.allocate(buf.remaining() + bytes.length);
        final int bypassCnt = EodUtil.units2Bypass(days, dayBit);

        byte lastByte = BCD.BOUNDARY_BYTE;
        int seen = 0;
        while (seen < bypassCnt) {
            lastByte = buf.get();
            bb.put(lastByte);
            if (BCD.isBoundary(lastByte)) {
                seen++;
            }
        }

        if (bytes.length > 0) {
            if (BCD.checkBoundary(lastByte) == BCD.Boundary.Low) {
                bb.put(bytes);
            }
            else { // can only be BCD.Boundary.High
                bb.position(bb.position() - 1);
                shiftPrices(bb, bytes);
            }
        }

        int rest = EodUtil.countUnits(days) - bypassCnt;
        if ((days & dayBit) == dayBit) {
            do { // bypass one day's price
                lastByte = buf.get();
            } while (!BCD.isBoundary(lastByte));
            rest--;
        }

        // transfer rest if any
        if (rest > 0) {
            transferDay(bb, bb.position() > 0 ? EodUtil.lastByte(bb) : BCD.BOUNDARY_BYTE,
                    buf, lastByte, rest);
        }

        return EodUtil.fromBuffer(bb);
    }

    private static void transferDay(ByteBuffer tar, byte lastByteTar, ByteBuffer src,
            byte lastByteSrc, int cnt) {
        final BCD.Boundary boundaryTar = BCD.checkBoundary(lastByteTar);
        if (BCD.Boundary.None == boundaryTar) {
            throw new IllegalStateException("data inconsistent, last price does not end");
        }
        final BCD.Boundary boundarySrc = BCD.checkBoundary(lastByteSrc);
        if (BCD.Boundary.None == boundarySrc) {
            throw new IllegalStateException("data inconsistent, last price does not end");
        }

        if (boundaryTar == BCD.Boundary.Low) {
            if (boundarySrc == BCD.Boundary.Low) {
                BCD.transferPrices(tar, src, cnt);
            }
            else {
                shiftPrices(tar, src, (byte) (lastByteSrc << 4), cnt);
            }
        }
        else {
            if (boundarySrc == BCD.Boundary.Low) {
                tar.position(tar.position() - 1);
                shiftPrices(tar, src, lastByteTar, cnt);
            }
            else {
                tar.put(tar.position() - 1, lastByteSrc);
                BCD.transferPrices(tar, src, cnt);
            }
        }
    }

    private static void shiftPrices(ByteBuffer tar, ByteBuffer src, byte msb, int cnt) {
        int seen = 0;
        byte b;
        do {
            b = src.get();
            tar.put((byte) ((msb & 0x0F0) | ((b >> 4) & 0x0F)));
            msb = (byte) (b << 4);
            if (BCD.isBoundary(b)) {
                seen++;
            }
        } while (seen < cnt);

        if (BCD.checkBoundary(b) == BCD.Boundary.Low) {
            BCD.putBoundary(tar);
        }
    }

    private static void shiftPrices(ByteBuffer bb, byte[] bytes) {
        byte msb = BCD.BOUNDARY_BYTE_W;
        for (final byte b : bytes) {
            bb.put((byte) ((msb & 0x0F0) | ((b >> 4) & 0x0F)));
            msb = (byte) (b << 4);
        }

        if (BCD.checkBoundary(msb) == BCD.Boundary.High) {
            BCD.putBoundary(bb);
        }
    }

    static byte[] merge(ByteBuffer base, ByteBuffer delta) {
        final ByteBuffer bb = ByteBuffer.allocate(base.remaining() + delta.remaining());
        final int dsBase = base.getInt();
        final int dsDelta = delta.getInt();

        bb.putInt(0); // place holder for days

        final int dCntBase = EodUtil.countUnits(dsBase);
        final int dCntDelta = EodUtil.countUnits(dsDelta);

        int days = 0;
        int dIdxBase = 0;
        int dIdxDelta = 0;

        byte lastByteTar = BCD.BOUNDARY_BYTE;
        byte lastByteBase = BCD.BOUNDARY_BYTE;
        byte lastByteDelta = BCD.BOUNDARY_BYTE;

        while (dIdxBase < dCntBase || dIdxDelta < dCntDelta) {
            final int dayBase = dIdxBase < dCntBase ? EodUtil.decodeDay(dsBase, dIdxBase) : 0;
            final int dayDelta = dIdxDelta < dCntDelta ? EodUtil.decodeDay(dsDelta, dIdxDelta) : 0;

            if (dayBase > dayDelta) {
                // from base to tar, advance base
                days |= EodUtil.dayBit(dayBase);
                transferDay(bb, lastByteTar, base, lastByteBase, 1);
                dIdxBase++;
                lastByteBase = EodUtil.lastByte(base);
            }
            else if (dayBase < dayDelta) {
                // from delta to tar, advance delta
                days |= EodUtil.dayBit(dayDelta);
                transferDay(bb, lastByteTar, delta, lastByteDelta, 1);
                dIdxDelta++;
                lastByteDelta = EodUtil.lastByte(delta);
            }
            else {
                // from delta to tar, advance both
                days |= EodUtil.dayBit(dayBase);
                transferDay(bb, lastByteTar, delta, lastByteDelta, 1);
                BCD.bypassPrices(base, 1);
                dIdxBase++;
                dIdxDelta++;
                lastByteBase = EodUtil.lastByte(base);
                lastByteDelta = EodUtil.lastByte(delta);
            }
            lastByteTar = EodUtil.lastByte(bb);
        }

        if (days == 0) {
            // no day prices transferred
            return EodUtil.EMPTY_BA;
        }

        bb.putInt(0, days);
        return EodUtil.fromBuffer(bb);
    }
}
