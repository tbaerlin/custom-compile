/*
 * ViewableTickLikeData.java
 *
 * Created on 21.01.15 14:29
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.joda.time.DateTimeConstants;

import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldData;
import de.marketmaker.istar.feed.ordered.OrderedSnapRecord;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.domain.data.LiteralSnapField.createNumber;
import static de.marketmaker.istar.domain.data.LiteralSnapField.createString;
import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.*;

/**
 * @author oflege
 */
abstract class ViewableTimeseries {
    /**
     * This bitset is filled as a side-effect of iterating over the ticks, so it must not be
     * evaluated before happens.
     */
    private BitSet fields = new BitSet(VwdFieldDescription.length());

    protected boolean isFieldPresent(BufferFieldData data) {
        for (int oid = data.readNext(); oid > 0; oid = data.readNext()) {
            if (this.oids.get(oid)) {
                return true;
            }
            data.skipCurrent();
        }
        return false;
    }

    public static class Chunk {
        int from;

        int to;

        int numTicks;

        int startTickNo;

        public int getFrom() {
            return from;
        }

        public int getNumTicks() {
            return numTicks;
        }

        public int getStartTickNo() {
            return startTickNo;
        }

        public int getTo() {
            return to;
        }

        public String getFromToStr() {
            return TimeFormatter.formatSecondsInDay(this.from).substring(0, 5)
                    + "-" + (to == DateTimeConstants.SECONDS_PER_DAY
                    ? "24:00" : TimeFormatter.formatSecondsInDay(this.to).substring(0, 5));
        }

        public String toString() {
            return "Chunk[" + getFromToStr() + ", at: " + startTickNo + ", #" + numTicks + "]";
        }
    }

    protected final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    private static final int[] AGGREGATION_SECS = {
            //  5m   15m   30m  1h    2h    4h     8h     24h
            300, 900, 1800, 3600, 7200, 14400, 28800, 86400
    };

    protected static final int MIN_INTERVAL_SECS = AGGREGATION_SECS[0];

    private static final int MAX_TICKS_PER_CHUNK = 500;

    protected final AbstractTicksCommand command;

    protected int numTicks;

    protected int numBytes;

    private int firstTickTime;

    private int lastTickTime;

    protected boolean timeWithMillis = false;

    protected ArrayList<Chunk> chunks = new ArrayList<>();

    protected Chunk selectedChunk = null;

    protected final BitSet oids;

    public ViewableTimeseries(AbstractTicksCommand command, AbstractTickRecord.TickItem ticks) {
        this.command = command;
        this.oids = command.getFieldOrders();
        this.df.applyLocalizedPattern("0.00######");

        this.numBytes = ticks.getData().length;

        int from = command.getFrom();
        int to = command.getTo();

        if (from < 0 || to > DateTimeConstants.SECONDS_PER_DAY || (to - from) < MIN_INTERVAL_SECS) {
            from = 0;
            to = DateTimeConstants.SECONDS_PER_DAY;
        }

        computeChunks(ticks);
        if (!this.chunks.isEmpty()) {
            if (from == 0 && to == DateTimeConstants.SECONDS_PER_DAY) {
                this.selectedChunk = command.isReverseOrder()
                        ? this.chunks.get(this.chunks.size() - 1)
                        : this.chunks.get(0);
            }
            else {
                this.selectedChunk = findSelectedChunk(from);
            }
            computeTicks(ticks, selectedChunk.from, this.selectedChunk.to);
        }
    }

    public String getFieldNames() {
        return this.fields.stream()
                .mapToObj(fid -> fid + ":'" + VwdFieldDescription.getField(fid).name() + "'")
                .collect(Collectors.joining(", ", "{", "}"));
    }

    protected abstract void computeTicks(AbstractTickRecord.TickItem ticks, int from, int to);

    protected abstract void doComputeChunks(AbstractTickRecord.TickItem ticks);

    protected void computeChunks(AbstractTickRecord.TickItem ticks) {
        doComputeChunks(ticks);
        if (!this.chunks.isEmpty()) {
            aggregateChunks();
            this.firstTickTime = this.chunks.get(0).from;
            this.lastTickTime = this.chunks.get(this.chunks.size() - 1).to;
        }
    }

    protected void ackEvent(int time) {
        Chunk current = chunks.isEmpty() ? null : chunks.get(chunks.size() - 1);
        if (current == null || time >= current.to) {
            this.chunks.add(current = new Chunk());
            current.from = getRoundedTime(time, MIN_INTERVAL_SECS);
            current.to = current.from + MIN_INTERVAL_SECS;
            current.startTickNo = this.numTicks;
        }
        current.numTicks++;
        this.numTicks++;
    }

    protected void addField(int id) {
        this.fields.set(id);
    }

    public List<Chunk> getChunks() {
        return this.chunks;
    }

    public int getFirstTickTime() {
        return this.firstTickTime;
    }

    public int getLastTickTime() {
        return this.lastTickTime;
    }

    public int getNumTicks() {
        return this.numTicks;
    }

    public int getNumBytes() {
        return numBytes;
    }

    public Chunk getSelectedChunk() {
        return this.selectedChunk;
    }

    protected Chunk findSelectedChunk(int from) {
        for (Chunk chunk : this.chunks) {
            if (chunk.from == from) {
                return chunk;
            }
        }
        return this.chunks.get(this.chunks.size() - 1);
    }

    private void aggregateChunks() {
        int j = 0;
        for (int i = 1; i < chunks.size(); i++) {
            Chunk jc = chunks.get(j);
            Chunk ic = chunks.get(i);
            if (jc.numTicks + ic.numTicks < MAX_TICKS_PER_CHUNK) {
                jc.numTicks += ic.numTicks;
                jc.to = ic.to;
            }
            else {
                this.chunks.set(++j, ic);
            }
        }
        if (++j < chunks.size()) {
            this.chunks.subList(j, chunks.size()).clear();
        }
    }

    protected int getRoundedTime(int secs, int interval) {
        return (secs / interval) * interval;
    }

    protected String format(long encodedPrice) {
        return df.format(decodePrice(encodedPrice));
    }

    protected String formatMdpsTime(final int time) {
        final int secs = decodeTime(time);
        final int ms = decodeTimeMillis(time);
        if (ms > 0 || this.timeWithMillis) {
            return TimeFormatter.formatSecondsInDay(secs, ms);
        }
        return TimeFormatter.formatSecondsInDay(secs);
    }

    protected SnapField toField(BufferFieldData fd) {
        VwdFieldDescription.Field f = VwdFieldOrder.getField(fd.getId());
        int fid = f.id();
        switch (fd.getType()) {
            case FieldData.TYPE_INT:
                if (f.type() == VwdFieldDescription.Type.UINT) {
                    return createNumber(fid, fd.getUnsignedInt());
                } else {
                    return createNumber(fid, fd.getInt());
                }
            case FieldData.TYPE_TIME:
                return createString(fid, formatMdpsTime(fd.getInt()));
            case FieldData.TYPE_PRICE:
                BigDecimal bd = BigDecimal.valueOf(fd.getInt(), -fd.getByte());
                return createString(fid, df.format(bd));
            case FieldData.TYPE_STRING:
                return fd.getLength() == 0
                        ? createString(fid, "")
                        : createString(fid, OrderedSnapRecord.toString(fd.getBytes()));
            default:
                throw new IllegalStateException("Unknown type: " + fd.getType());
        }
    }
}
