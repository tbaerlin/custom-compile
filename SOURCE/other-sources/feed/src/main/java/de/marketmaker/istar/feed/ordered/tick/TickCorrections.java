/*
 * TickCorrections.java
 *
 * Created on 18.06.14 09:11
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.feed.mdps.MdpsPriceUtils;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.FeedUpdateFlags.FLAGS_WITH_TICK;
import static de.marketmaker.istar.feed.FeedUpdateFlags.FLAG_TICK_CORRECTION_DELETE;
import static de.marketmaker.istar.feed.ordered.FieldDataUtil.getPrice;

/**
 * @author oflege
 */
class TickCorrections {

    protected static class Correction {
        /**
         * -1 or a time that that must match the time of a tick to be corrected
         */
        int time;

        /**
         * tick flags that must match the flags of a tick to be corrected
         */
        int flags;

        final boolean delete;

        /**
         * contains time and field(s) that must match a tick to be corrected
         */
        BufferFieldData data;

        /**
         * negative value: apply correction to all matched ticks
         * postivie value n: apply correction to the n-ths matched tick
         */
        int matchCount;

        byte[][] inserts;

        public Correction(int matchCount, int flags, byte[] dataBytes, byte[][] inserts) {
            this.matchCount = matchCount;
            this.delete = (flags & FLAG_TICK_CORRECTION_DELETE) != 0;
            this.flags = flags & FLAGS_WITH_TICK;
            this.data = new BufferFieldData(dataBytes);
            this.time = this.data.getInt();
            this.inserts = inserts;
        }

        boolean canMatchAnother() {
            return this.matchCount != 0;
        }

        boolean matches(TickDecompressor.Element e) {
            if ((this.flags & e.getFlags()) != this.flags) {
                return false;
            }
            if (!matches(e.getData().rewind())) {
                return false;
            }
            this.matchCount -= 1;
            return this.matchCount <= 0;
        }

        private boolean matches(BufferFieldData tickFields) {
            int otherTime = tickFields.getInt();
            if (this.time != -1 && this.time != otherTime) {
                return false;
            }

            this.data.rewind(4);
            int oid = data.readNext();
            int tickFieldOid = tickFields.readNext();

            while (oid > 0 && tickFieldOid > 0) {
                if (tickFieldOid < oid) {
                    tickFields.skipCurrent();
                    tickFieldOid = tickFields.readNext();
                }
                else if (tickFieldOid > oid) {
                    return false;
                }
                else if (!fieldMatches(tickFields)) {
                    return false;
                }
                else {
                    oid = data.readNext();
                    tickFieldOid = tickFields.readNext();
                }
            }
            return oid == 0;
        }

        private boolean fieldMatches(BufferFieldData other) {
            switch (data.getType()) {
                case FieldData.TYPE_INT:
                    // Intentional fallthrough
                case FieldData.TYPE_TIME:
                    return data.getInt() == other.getInt();
                case FieldData.TYPE_PRICE:
                    // use compare to match prices that have different scales
                    return MdpsPriceUtils.compare(getPrice(data), getPrice(other)) == 0;
                case FieldData.TYPE_STRING:
                    boolean result = data.stringEquals(other.getBytes());
                    this.data.skipCurrent();
                    return result;
                default:
                    throw new IllegalStateException("Unknown type: " + data.getType());
            }
        }

        int getFlags(int srcFlags) {
            if (this.delete) {
                return srcFlags | FLAG_TICK_CORRECTION_DELETE;
            }
            return srcFlags;
        }

        @Override
        public String toString() {
            return "Correction{" +
                    new TickCli.LineBuilder().build(this.flags, this.time, this.data.rewind(4))
                    + '}';
        }
    }

    /**
     * Parse encoded correction data as created by
     * {@link de.marketmaker.istar.feed.ordered.tick.TickCorrectionReader}.
     * @param update contains encoded corrections for a single symbol
     * @return decoded corrections
     */
    public static List<Correction> decodeCorrections(OrderedUpdate update) {
        BufferFieldData fd = update.getFieldData();
        int oid = fd.readNext();
        if (oid != VwdFieldOrder.getOrder(VwdFieldDescription.ADF_Blob_Content.id())) {
            return null;
        }
        ArrayList<Correction> result = new ArrayList<>();
        ByteBuffer bb = BufferFieldData.asBuffer(fd.getBytes());
        while (bb.hasRemaining()) {
            int matchCount = bb.get();
            int length = (bb.get() & 0xFF);
            int flags = bb.get();
            byte[] bytes = new byte[length - 1];
            bb.get(bytes);
            int numInserts = bb.get() & 0xFF;
            byte[][] inserts = null;
            if (numInserts > 0) {
                inserts = new byte[numInserts][];
                for (int i = 0; i < inserts.length; i++) {
                    inserts[i] = new byte[bb.get() & 0xFF];
                    bb.get(inserts[i]);
                }
            }
            result.add(new Correction(matchCount, flags, bytes, inserts));
        }
        return result;
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Correction[] corrections;

    private int numActive;

    public TickCorrections(List<Correction> corrections) {
        this.corrections = corrections.toArray(new Correction[corrections.size()]);
        this.numActive = this.corrections.length;
    }

    void applyTo(TickDecompressor td, TickDeflater deflater) throws IOException {
        Iterator<TickDecompressor.Element> it = td.iterator(true);
        while (it.hasNext()) {
            TickDecompressor.Element e = it.next();
            Correction c = findMatching(e);
            if (c != null) {
                this.logger.info("<applyTo> " + c + " matched " + e);
                deflater.add(c.getFlags(e.getFlags()), e.getData());
                if (c.inserts != null) {
                    for (byte[] insert : c.inserts) {
                        deflater.add(insert);
                    }
                }
            }
            else {
                deflater.add(e);
            }
        }
        deflater.flushCompressedTicks();
        deflater.flushWriteBuffer();
    }

    protected Correction findMatching(TickDecompressor.Element e) {
        for (int i = 0; i < this.numActive; i++) {
            Correction c = this.corrections[i];
            if (c.matches(e)) {
                if (!c.canMatchAnother()) {
                    this.numActive--;
                    if (this.numActive - i > 0) {
                        System.arraycopy(this.corrections, i + 1, this.corrections, i, numActive - i);
                    }
                }
                return c;
            }
        }
        return null;
    }
}
