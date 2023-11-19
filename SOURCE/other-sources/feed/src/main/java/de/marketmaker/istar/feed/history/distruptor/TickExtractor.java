/*
 * AggregatedTickReader.java
 *
 * Created on 11.07.12 15:16
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history.distruptor;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.history.TickHistoryContext;
import de.marketmaker.istar.feed.ordered.tick.TickFileIndexReader;
import de.marketmaker.istar.feed.ordered.tick.TickFileReader;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;

/**
 * @author zzhao
 */
class TickExtractor implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EnumSet<TickType> tickTypes;

    private final ByteString marketName;

    private final int date;

    private final TickHistoryContext ctx;

    private List<IndexItem> oaLs = new ArrayList<>();

    private TickFileReader reader;

    public TickExtractor(TickFileReader reader, EnumSet<TickType> tickTypes, TickHistoryContext ctx) {
        this.reader = reader;
        this.tickTypes = tickTypes;
        this.ctx = ctx;
        this.marketName = new ByteString(reader.getMarketBaseName());
        this.date = reader.getDay();
    }

    public int getDate() {
        return date;
    }

    public ByteString getMarketName() {
        return marketName;
    }

    public AbstractTickRecord.TickItem.Encoding getTickItemEncoding() {
        return this.reader.getEncoding();
    }

    public void readVendorKeys() throws Exception {
        logger.info("<readVendorKeys> {} from: {}", String.valueOf(this.tickTypes),
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
                addOffsetAndLengthWithSymbol(new IndexItem(prefix.append(key), position, length));
            }
        });
        return this.oaLs.size();
    }

    @Override
    public void close() throws IOException {
        IoUtils.close(this.reader);
    }

    private void addOffsetAndLengthWithSymbol(IndexItem indexItem) {
        if (indexItem.getLength() > 0 && this.ctx.isRelevantSymbol(indexItem.getSymbol())) {
            this.oaLs.add(indexItem);
        }
    }

    List<IndexItem> getOaLs() {
        return oaLs;
    }

    private byte[] getTickData(IndexItem oaL) throws IOException {
        return this.reader.readTicks(oaL.getOffset(), oaL.getLength());
    }

    void getTickData(IndexItem oaL, RequestItem requestItem) {
        try {
            requestItem.withTickData(getTickData(oaL));
        } catch (Exception e) {
            logger.error("<getTickData> failed reading tick data {}", oaL);
            requestItem.setDamaged(true);
        }
    }
}
