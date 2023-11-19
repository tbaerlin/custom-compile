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

import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapFieldComparators;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.tick.DumpDecompressor;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ViewableRecords extends ViewableTimeseries {

    private Iterator<ViewableRecord> records;


    public Iterator<ViewableRecord> getRecords() {
        return records;
    }

    public ViewableRecords(AbstractTickRecord.TickItem ticks, AbstractTicksCommand command) {
        super(command, ticks);
    }

    protected void computeTicks(final AbstractTickRecord.TickItem ticks, final int from,
            final int to) {
        if (this.selectedChunk == null) {
            this.records = Collections.emptyIterator();
            return;
        }
        this.records = new Iterator<ViewableRecord>() {
            final Iterator<DumpDecompressor.Element> it = new DumpDecompressor(ticks).iterator();

            ViewableRecord r = decompressNext();

            boolean timeExceeded = false;

            private ViewableRecord decompressNext() {
                while (!this.timeExceeded && it.hasNext()) {
                    DumpDecompressor.Element e = it.next();
                    final int time = DateTimeProvider.Timestamp.decodeTime(e.getTimestamp());
                    if (time < from) {
                        continue;
                    }
                    if (time >= to) {
                        this.timeExceeded = true;
                        return null;
                    }
                    if (oids != null) {
                        if (!isFieldPresent(e.getData())) {
                            continue;
                        }
                        e.getData().rewind();
                    }
                    return new ViewableRecord(e, time);
                }
                return null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                return r != null;
            }

            @Override
            public ViewableRecord next() {
                ViewableRecord result = this.r;
                this.r = decompressNext();
                return result;
            }
        };
    }

    protected void doComputeChunks(AbstractTickRecord.TickItem ticks) {
        for (DumpDecompressor.Element next : new DumpDecompressor(ticks)) {
            if (this.oids == null || isFieldPresent(next.getData())) {
                ackEvent(DateTimeProvider.Timestamp.decodeTime(next.getTimestamp()));
            }
        }
    }

    public class ViewableRecord {
        private final int flags;

        private final int secOfDay;

        private final int mdpsKeyType;

        private final int mdpsMsgType;

        private List<SnapField> fields = new ArrayList<>();

        public ViewableRecord(DumpDecompressor.Element e, int secOfDay) {
            this.secOfDay = secOfDay;
            this.flags = e.getFlags();
            this.mdpsKeyType = e.getMdpsKeyType();
            this.mdpsMsgType = e.getMdpsMsgType();
            BufferFieldData fd = e.getData();
            for (int id = fd.readNext(); id > 0; id = fd.readNext()) {
                SnapField sf = ViewableRecords.this.toField(fd);
                fields.add(sf);
                addField(sf.getId());
            }
            if (this.fields != null && this.fields.size() > 1) {
                this.fields.sort(SnapFieldComparators.BY_ID);
            }
        }

        public List<SnapField> getFields() {
            return this.fields;
        }

        public String getFieldStr() {
            return this.fields.stream().map(Object::toString).collect(Collectors.joining(", "));
        }

        public String getFlags() {
            return Integer.toHexString(flags);
        }

        public String getTime() {
            return TimeFormatter.formatSecondsInDay(secOfDay);
        }

        public int getMdpsKeyType() {
            return mdpsKeyType;
        }

        public int getMdpsMsgType() {
            return mdpsMsgType;
        }
    }
}
