/*
 * AggregatedTickReader.java
 *
 * Created on 11.07.12 15:16
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.ordered.tick.TickFileIndexReader;
import de.marketmaker.istar.feed.ordered.tick.TickFileReader;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.RawTick;
import de.marketmaker.istar.feed.tick.TickRecordImpl;

/**
 * @author zzhao
 */
public class DayTickExtractorNegative implements Iterable<DayTick>, Closeable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AbstractTickRecord.TickItem.Encoding tickItemEncoding;

    private final TickType tickType;

    private final ByteString marketName;

    private final int date;

    private List<IndexItem> oaLs = new ArrayList<>();

    private TickFileReader reader;

    public DayTickExtractorNegative(TickFileReader reader, TickType tickType) {
        this.reader = reader;
        this.tickType = tickType;
        this.marketName = new ByteString(reader.getMarketBaseName());
        this.date = reader.getDay();
        this.tickItemEncoding = reader.getEncoding();
    }

    public int getDate() {
        return date;
    }

    public ByteString getMarketName() {
        return marketName;
    }

    public void readVendorKeys() throws Exception {
        logger.info("<readVendorKeys> {} from: {}", String.valueOf(this.tickType),
                this.reader.getFile().getAbsolutePath());
        int vendorKeyCount = readIndex();
        if (logger.isDebugEnabled()) {
            logger.debug("<readVendorKeys> {} vendor keys", vendorKeyCount);
        }
        if (!this.oaLs.isEmpty()) {
            this.oaLs.sort(null); // symbols are already in the form MARKET.KEY
        }
    }

    private int readIndex() throws IOException {
        final ByteString prefix = this.marketName.append(new ByteString("."));
        this.reader.readIndex(new TickFileIndexReader.IndexHandler() {
            @Override
            public void handle(ByteString key, long position, int length) {
                if (length > 0) {
                    oaLs.add(new IndexItem(prefix.append(key), position, length));
                }
            }
        });
        return this.oaLs.size();
    }

    @Override
    public void close() throws IOException {
        IoUtils.close(this.reader);
    }

    @Override
    public Iterator<DayTick> iterator() {
        return new MyIterator(this.oaLs.iterator());
    }

    private class MyIterator implements Iterator<DayTick> {

        private final Iterator<IndexItem> vendorKeyIt;

        private final DayTick dayTick = new DayTick();

        private MyIterator(Iterator<IndexItem> vendorKeyIt) {
            this.vendorKeyIt = vendorKeyIt;
        }

        @Override
        public boolean hasNext() {
            return this.vendorKeyIt.hasNext();
        }

        @Override
        public DayTick next() {
            final IndexItem oaL = this.vendorKeyIt.next();
            return readDayTick(oaL, this.dayTick);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("not implemented");
        }
    }

    private DayTick readDayTick(IndexItem oaL, DayTick dayTick) {
        try {
            final byte[] bytes = getTickData(oaL);
            final TickRecordImpl record = new TickRecordImpl();
            record.add(this.date, bytes, this.tickItemEncoding);
            dayTick.reset().withSymbol(oaL.getSymbol());

            final AbstractTickRecord.TickItem item = record.getItem(this.date);
            final boolean negative = containsNegativeTicks(item, this.tickType);
            if (!negative) {
                return null;
            }
            final HistoryTickProcessor processor = new HistoryTickProcessor(item,
                    EnumSet.of(this.tickType), false);
            final Map<TickType, AbstractTickRecord.TickItem> result = item.accept(processor);

            for (Map.Entry<TickType, AbstractTickRecord.TickItem> entry : result.entrySet()) {
                dayTick.withTickItem(entry.getKey(), entry.getValue());
            }

            return dayTick.isEmpty() ? null : dayTick;
        } catch (Exception e) {
            logger.warn("cannot read ticks for: '" + oaL.getSymbol() + "'", e);
            return null;
        }
    }

    private boolean containsNegativeTicks(AbstractTickRecord.TickItem item, TickType tickType) {
        final Iterator<RawTick> it = item.createIterator();
        while (it.hasNext()) {
            final RawTick rawTick = it.next();
            if (pricePresent(rawTick, tickType) && getPrice(rawTick, tickType) <= 0) {
                return true;
            }
        }
        return false;
    }

    private boolean pricePresent(RawTick rawTick, TickType tickType) {
        switch (tickType) {
            case ASK:
                return rawTick.isAskPresent();
            case BID:
                return rawTick.isBidPresent();
            case TRADE:
            case SYNTHETIC_TRADE:
                return rawTick.isPricePresent();
            default:
                throw new UnsupportedOperationException("no support for: " + tickType);
        }
    }

    private long getPrice(RawTick rawTick, TickType tickType) {
        switch (tickType) {
            case ASK:
                return rawTick.getAskPrice();
            case BID:
                return rawTick.getBidPrice();
            case TRADE:
            case SYNTHETIC_TRADE:
                return rawTick.getPrice();
            default:
                throw new UnsupportedOperationException("no support for: " + tickType);
        }
    }

    private byte[] getTickData(IndexItem oaL) throws IOException {
        return this.reader.readTicks(oaL.getOffset(), oaL.getLength());
    }
}
