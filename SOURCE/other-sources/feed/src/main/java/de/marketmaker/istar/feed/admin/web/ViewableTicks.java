/*
 * ViewableTicks.java
 *
 * Created on 29.04.2005 10:33:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapFieldComparators;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.OrderedSnapRecord;
import de.marketmaker.istar.feed.ordered.tick.TickDecompressor;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.FeedUpdateFlags.*;
import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ViewableTicks extends ViewableTimeseries {

    private Iterator<ViewableTick> ticks;

    public Iterator<ViewableTick> getTicks() {
        return ticks;
    }

    public ViewableTicks(AbstractTickRecord.TickItem ticks, AbstractTicksCommand command) {
        super(command, ticks);
    }

    protected void computeTicks(final AbstractTickRecord.TickItem ticks, final int from,
            final int to) {
        if (this.selectedChunk == null) {
            this.ticks = Collections.emptyIterator();
            return;
        }

        this.ticks = new Iterator<ViewableTick>() {
            final Iterator<TickDecompressor.Element> it = new TickDecompressor(ticks).iterator(true);

            ViewableTick vt = decompressNext();

            boolean timeExceeded = false;

            private ViewableTick decompressNext() {
                while (!this.timeExceeded && it.hasNext()) {
                    TickDecompressor.Element e = it.next();
                    if (command.isOnlyTrades() && !e.hasFlag(FLAG_WITH_TRADE)) {
                        continue;
                    }
                    final BufferFieldData fd = e.getData();
                    final int mdpsTime = fd.getInt();
                    final int time = decodeTime(mdpsTime);
                    if (time < from) {
                        continue;
                    }
                    if (time >= to) {
                        this.timeExceeded = true;
                        return null;
                    }
                    if (oids != null) {
                        if (!isFieldPresent(fd)) {
                            continue;
                        }
                        fd.rewind(4);
                    }
                    ViewableTick next = new ViewableTick(e.getFlags(), fd, mdpsTime);
                    if (next.extraFields != null) {
                        for (SnapField sf : next.extraFields) {
                            addField(sf.getId());
                        }
                    }
                    return next;
                }
                return null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                return vt != null;
            }

            @Override
            public ViewableTick next() {
                ViewableTick result = this.vt;
                this.vt = decompressNext();
                return result;
            }
        };
    }

    protected void doComputeChunks(AbstractTickRecord.TickItem ticks) {
        for (TickDecompressor.Element next : new TickDecompressor(ticks)) {
            if (this.command.isOnlyTrades() && !next.hasFlag(FLAG_WITH_TRADE)) {
                continue;
            }
            final BufferFieldData fd = next.getData();
            final int mdpsTime = fd.getInt();
            if (this.oids != null && !isFieldPresent(fd)) {
                continue;
            }
            final int time = decodeTime(mdpsTime);
            ackEvent(time);
            this.timeWithMillis |= (decodeTimeMillis(mdpsTime) != 0);
        }
    }

    public class ViewableTick {
        private final int flags;

        private int mdpsTime;

        private long price = Long.MIN_VALUE;

        private long volume = Long.MIN_VALUE;

        private long ask = Long.MIN_VALUE;

        private long askVolume = Long.MIN_VALUE;

        private long bid = Long.MIN_VALUE;

        private long bidVolume = Long.MIN_VALUE;

        private String supplement;

        private String notierungsart;

        private List<SnapField> extraFields;

        public ViewableTick(int flags, BufferFieldData fd, int mdpsTime) {
            this.flags = flags;
            this.mdpsTime = mdpsTime;
            for (int id = fd.readNext(); id > 0; id = fd.readNext()) {
                switch (id) {
                    case VwdFieldOrder.ORDER_ADF_BEZAHLT:
                        this.price = encodePrice(fd.getInt(), fd.getByte());
                        break;
                    case VwdFieldOrder.ORDER_ADF_GELD:
                        this.bid = encodePrice(fd.getInt(), fd.getByte());
                        break;
                    case VwdFieldOrder.ORDER_ADF_GELD_UMSATZ:
                        this.bidVolume = fd.getUnsignedInt();
                        break;
                    case VwdFieldOrder.ORDER_ADF_BRIEF:
                        this.ask = encodePrice(fd.getInt(), fd.getByte());
                        break;
                    case VwdFieldOrder.ORDER_ADF_BRIEF_UMSATZ:
                        this.askVolume = fd.getUnsignedInt();
                        break;
                    case VwdFieldOrder.ORDER_ADF_BEZAHLT_UMSATZ:
                        this.volume = fd.getUnsignedInt();
                        break;
                    case VwdFieldOrder.ORDER_ADF_BEZAHLT_KURSZUSATZ:
                        this.supplement = OrderedSnapRecord.toString(fd.getBytes());
                        break;
                    case VwdFieldOrder.ORDER_ADF_NOTIERUNGSART:
                        this.notierungsart = OrderedSnapRecord.toString(fd.getBytes());
                        break;
                    default:
                        final SnapField field = toField(fd);
                        if (extraFields == null) {
                            this.extraFields = new ArrayList<>(4);
                        }
                        this.extraFields.add(field);
                        break;
                }
            }
            if (this.extraFields != null && this.extraFields.size() > 1) {
                this.extraFields.sort(SnapFieldComparators.BY_ID);
            }
        }

        public boolean isWithExtraFields() {
            return this.extraFields != null;
        }

        public String getExtraFields() {
            return this.extraFields.stream().map(Object::toString).collect(Collectors.joining(", "));
        }

        public String getPrice() {
            return formatPrice(this.price);
        }

        private String formatPrice(final long p) {
            return (p != Long.MIN_VALUE) ? format(p) : "";
        }

        public String getTimeStr() {
            return formatMdpsTime(this.mdpsTime);
        }

        public String getVolume() {
            return formatVolume(this.volume);
        }

        private String formatVolume(long v) {
            return v != Long.MIN_VALUE ? Long.toString(v) : "";
        }

        public String getSupplement() {
            return (this.supplement != null) ? this.supplement : "";
        }

        public String getNotierungsart() {
            return (this.notierungsart != null) ? this.notierungsart : "";
        }

        public String getBid() {
            return formatPrice(this.bid);
        }

        public String getBidVolume() {
            return formatVolume(this.bidVolume);
        }

        public String getAsk() {
            return formatPrice(this.ask);
        }

        public String getAskVolume() {
            return formatVolume(this.askVolume);
        }

        public boolean isCorrectionDelete() {
            return hasFlag(FLAG_TICK_CORRECTION_DELETE);
        }

        public boolean isCorrectionInsert() {
            return hasFlag(FLAG_TICK_CORRECTION_INSERT);
        }

        public String getInfo() {
            return new String(new char[]{
                    flagChar(FLAG_WITH_TRADE, 'T'),
                    flagChar(FLAG_WITH_BID, 'B'),
                    flagChar(FLAG_WITH_ASK, 'A'),
                    flagChar(FLAG_WITH_TICK_FIELD, 'X')
            });
        }

        private char flagChar(int flag, char c) {
            return hasFlag(flag) ? c : '-';
        }

        private boolean hasFlag(int f) {
            return (this.flags & f) != 0;
        }
    }
}
