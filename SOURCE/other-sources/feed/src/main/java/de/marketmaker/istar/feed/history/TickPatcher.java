/*
 * Csv2History.java
 *
 * Created on 27.09.12 15:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.ordered.tick.TickDirectory;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * @author zzhao
 */
public class TickPatcher implements HistoryDataSource<ByteString> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final TickDirectory tickDir;

    private final Map<ByteString, List<ByteString>> map;

    private final TickType tickType;

    private final EnumSet<TickType> tickTypes;

    private final TickHistoryContext context;

    private boolean patchEmptyTicks = false;

    public TickPatcher(File tickDir, String[] symbols, TickType tickType,
            TickHistoryContext context)
            throws IOException {
        this.tickDir = TickDirectory.open(tickDir);
        this.tickType = tickType;
        this.tickTypes = EnumSet.of(tickType);
        this.map = categorize(symbols);
        this.context = context;
    }

    /**
     * Set to true to produce tick history entries with 0 ticks and 0 data, effectively set the
     * tick history for the given key and that day to empty, i.e. delete the tick history for that
     * key and day.
     *
     * @param patchEmptyTicks
     */
    public void setPatchEmptyTicks(boolean patchEmptyTicks) {
        this.patchEmptyTicks = patchEmptyTicks;
    }

    private void writeDayTicks(HistoryWriter<ByteString> writer, ByteString market,
            List<ByteString> list) throws IOException {
        DayTickExtractor dte = null;
        try {
            dte = new DayTickExtractor(this.tickDir.getTickFileReader(market.toString()), tickTypes, context);
            dte.readVendorKeys();
            final int days = HistoryUtil.daysFromBegin(context.getGenesis(),
                    DateUtil.yyyyMmDdToLocalDate(dte.getDate()));
            if (!HistoryUtil.isUnsignedShort(days)) {
                throw new IllegalStateException("date coding overflow: " + dte.getDate());
            }

            for (ByteString symbol : list) {
                final DayTick dayTick = dte.getDayTick(symbol);
                if (null == dayTick || null == dayTick.getTickItem(tickType)
                        || dayTick.getTickItem(tickType).getData().length == 0) {
                    if (this.patchEmptyTicks) {
                        final ByteBuffer bb = ByteBuffer.allocate(6);
                        bb.putShort((short) days);
                        bb.putShort((short) 0);
                        bb.putShort((short) 0);
                        writer.withEntry(symbol, bb.array());
                        this.logger.info("<writeDayTicks> empty ticks for: {}", symbol);
                    }
                    else {
                        logger.warn("<writeDayTicks> no ticks found for: {}", symbol);
                    }
                }
                else {
                    final AbstractTickRecord.TickItem tickItem = dayTick.getTickItem(tickType);
                    final byte[] data = context.postProcessTickData(tickType, tickItem.getData());
                    final int dataLen = data.length;
                    if (!HistoryUtil.isUnsignedShort(dataLen)) {
                        logger.error("<writeDayTicks> data out of range {}, length {}",
                                dayTick.getSymbol(), dataLen);
                        continue;
                    }
                    final ByteBuffer bb = ByteBuffer.allocate(6 + dataLen);
                    bb.putShort((short) days);
                    bb.putShort((short) tickItem.getNumTicks());
                    bb.putShort((short) dataLen);
                    bb.put(data);
                    writer.withEntry(symbol, bb.array());
                }
            }
        } catch (Exception e) {
            logger.error("<writeDayTicks> cannot write day ticks for: {}", list, e);
        } finally {
            IoUtils.close(dte);
        }
    }

    private static Map<ByteString, List<ByteString>> categorize(String[] symbols) {
        final TreeMap<ByteString, List<ByteString>> map = new TreeMap<>();
        for (String symbol : symbols) {
            final ByteString key = getKey(symbol);
            final ByteString market = key.substring(0, key.indexOf('.'));

            if (!map.containsKey(market)) {
                map.put(market, new ArrayList<ByteString>());
            }
            map.get(market).add(key);
        }

        for (List<ByteString> list : map.values()) {
            list.sort(null);
        }

        return map;
    }

    private static ByteString getKey(String arg) {
        final ByteString symbol = new ByteString(arg);
        if (VendorkeyVwd.isKeyWithTypePrefix(symbol)) {
            return HistoryUtil.removeTypeAndGetKey(symbol);
        }
        else {
            return HistoryUtil.getKey(symbol);
        }
    }

    @Override
    public void transfer(HistoryWriter<ByteString> writer) throws IOException {
        for (Map.Entry<ByteString, List<ByteString>> entry : map.entrySet()) {
            writeDayTicks(writer, entry.getKey(), entry.getValue());
        }
    }
}
