/*
 * DayTicker.java
 *
 * Created on 20.08.12 14:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;

/**
 * Merger for two history files into one new history file.
 *
 * @author zzhao
 */
public class HistoryFileMerger<T extends Comparable<T>> extends MergerBase<T> {

    private final SymbolRetainer<T> symbolRetainer;

    public HistoryFileMerger(File base, File delta, Class<T> clazz,
            SymbolRetainer<T> symbolRetainer) throws IOException {
        super(base, delta, clazz);
        this.symbolRetainer = symbolRetainer;
    }

    @Override
    public void merge(File result, EntryMerger entryMerger) throws IOException {
        this.logger.info("<merge> " + this.base.getAbsolutePath()
                + " with " + this.delta.getAbsolutePath()
                + " into " + result.getAbsolutePath());

        HistoryUnit.ensureSameContent(result, this.delta);
        DataFile dfBase = null;
        DataFile dfDelta = null;
        HistoryWriter<T> writer = null;
        try {
            final TimeTaker tt = new TimeTaker();
            dfBase = new DataFile(this.base, true);
            dfDelta = new DataFile(this.delta, true);
            writer = new HistoryWriter<>(result, getCoder(), this.clazz);

            merge(dfBase, dfDelta, entryMerger, writer);

            this.logger.info("<merge> took: " + tt);
        } finally {
            IoUtils.close(writer);
            IoUtils.close(dfBase);
            IoUtils.close(dfDelta);
        }
    }

    private void merge(DataFile dfBase, DataFile dfDelta, EntryMerger entryMerger,
            HistoryWriter<T> writer) throws IOException {
        final Iterator<Item<T>> itBase = new ItemExtractor<>(this.clazz, dfBase).iterator();
        final Iterator<Item<T>> itDelta = new ItemExtractor<>(this.clazz, dfDelta).iterator();

        final BufferedBytesTransporter tranBase = new BufferedBytesTransporter(dfBase, getBufSize());
        final BufferedBytesTransporter tranDelta = new BufferedBytesTransporter(dfDelta, getBufSize());

        final ByteArrayTarget targetBase = new ByteArrayTarget();
        final ByteArrayTarget targetDelta = new ByteArrayTarget();

        Item<T> itemBase = nextItem(itBase);
        Item<T> itemDelta = nextItem(itDelta);

        while (null != itemBase || null != itemDelta) {
            final int diff = compare(itemBase, itemDelta);
            if (diff == 0) {
                // merge, advance both iterator
                tranDelta.transferTo(itemDelta.getOffset(), itemDelta.getLength(), targetDelta);
                tranBase.transferTo(itemBase.getOffset(), itemBase.getLength(), targetBase);

                writer.withEntry(itemBase.getKey(), entryMerger.merge(targetBase.data(), targetDelta.data()));

                itemBase = nextItem(itBase);
                itemDelta = nextItem(itDelta);
            }
            else if (diff > 0) {
                if (null != itemDelta) {
                    // from delta to result, advance delta iterator
                    tranDelta.transferTo(itemDelta.getOffset(), itemDelta.getLength(), targetDelta);
                    writer.withEntry(itemDelta.getKey(), entryMerger.merge(null, targetDelta.data()));
                    itemDelta = nextItem(itDelta);
                }
                else {
                    // base iterator contains more symbols than delta iterator
                    if (this.symbolRetainer.shouldRetain(itemBase.getKey())) {
                        // from base to result, advance base iterator
                        tranBase.transferTo(itemBase.getOffset(), itemBase.getLength(), targetBase);
                        writer.withEntry(itemBase.getKey(), entryMerger.merge(targetBase.data(), null));
                    }
                    // in compact mode, jump over symbols without ticks in delta
                    itemBase = nextItem(itBase);
                }
            }
            else {
                if (null != itemBase) {
                    if (this.symbolRetainer.shouldRetain(itemBase.getKey())) {
                        // from base to result, advance base iterator
                        tranBase.transferTo(itemBase.getOffset(), itemBase.getLength(), targetBase);
                        writer.withEntry(itemBase.getKey(), entryMerger.merge(targetBase.data(), null));
                    }
                    // in compact mode, jump over symbols without ticks in delta
                    itemBase = nextItem(itBase);
                }
                else {
                    // delta iterator contains more symbols than base iterator
                    // from delta to result, advance delta iterator
                    tranDelta.transferTo(itemDelta.getOffset(), itemDelta.getLength(), targetDelta);
                    writer.withEntry(itemDelta.getKey(), entryMerger.merge(null, targetDelta.data()));
                    itemDelta = nextItem(itDelta);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: HistoryFileMerger {base} {delta} {result}");
            System.exit(1);
        }

        final File base = new File(args[0]);
        final File delta = new File(args[1]);
        final File result = new File(args[2]);

        final HistoryFileMerger<ByteString> merger =
                new HistoryFileMerger<>(base, delta, ByteString.class, SymbolRetainer.YES);
        merger.merge(result, new EntryMergerJoin<>(0, MutableTickEntry.class));
    }
}
