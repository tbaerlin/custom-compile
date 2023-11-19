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

import org.apache.commons.lang3.ArrayUtils;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;

/**
 * Merger for month and patch files onto year file.
 *
 * @author zzhao
 */
public class HistoryMerger<T extends Comparable<T>> extends MergerBase<T> {

    private final File patch;

    private final SymbolRetainer<T> symbolRetainer;

    public HistoryMerger(File base, File delta, File patch, Class<T> clazz,
            SymbolRetainer<T> symbolRetainer) throws IOException {
        super(base, delta, clazz);
        HistoryUnit.ensureSameContent(patch, this.delta);
        this.patch = patch;
        this.symbolRetainer = symbolRetainer;
    }

    @Override
    public void merge(File result, EntryMerger entryMerger) throws IOException {
        this.logger.info("<merge> " + this.base.getAbsolutePath()
                + " and " + this.patch.getAbsolutePath()
                + " with " + this.delta.getAbsolutePath()
                + " into " + result.getAbsolutePath());

        HistoryUnit.ensureSameContent(result, this.delta);
        DataFile dfBase = null;
        DataFile dfDelta = null;
        DataFile dfPatch = null;
        HistoryWriter<T> writer = null;
        try {
            final TimeTaker tt = new TimeTaker();
            dfBase = new DataFile(this.base, true);
            dfDelta = new DataFile(this.delta, true);
            dfPatch = new DataFile(this.patch, true);
            writer = new HistoryWriter<>(result, getCoder(), this.clazz);

            merge(dfBase, dfDelta, dfPatch, entryMerger, writer);

            this.logger.info("<merge> took: " + tt);
        } finally {
            IoUtils.close(writer);
            IoUtils.close(dfBase);
            IoUtils.close(dfDelta);
            IoUtils.close(dfPatch);
        }
    }

    private final class ThreeWayMerger {

        private final ByteArrayTarget targetBase;

        private final ByteArrayTarget targetDelta;

        private final ByteArrayTarget targetPatch;

        private final Iterator<Item<T>> itPatch;

        private final BufferedBytesTransporter tranPatch;

        private final EntryMerger entryMerger;

        private Item<T> patchItem;

        private ThreeWayMerger(ByteArrayTarget targetBase, ByteArrayTarget targetDelta,
                DataFile dfPatch, EntryMerger entryMerger) throws IOException {
            this.targetBase = targetBase;
            this.targetDelta = targetDelta;
            this.itPatch = new ItemExtractor<>(clazz, dfPatch).iterator();
            this.targetPatch = new ByteArrayTarget();
            this.tranPatch = new BufferedBytesTransporter(dfPatch, getBufSize());
            this.entryMerger = entryMerger;
            nextPatchItem();
        }

        private void nextPatchItem() {
            if (this.itPatch.hasNext()) {
                this.patchItem = this.itPatch.next();
            }
            else {
                this.patchItem = null;
            }
        }

        public void forKey(T key, HistoryWriter<T> writer) throws IOException {
            try {
                while (null != this.patchItem && this.patchItem.getKey().compareTo(key) < 0) {
                    this.tranPatch.transferTo(this.patchItem.getOffset(),
                            this.patchItem.getLength(), this.targetPatch);
                    writer.withEntry(this.patchItem.getKey(),
                            this.entryMerger.merge(null, this.targetPatch.data()));
                    nextPatchItem();
                }

                final byte[] bb = ArrayUtils.addAll(this.targetDelta.data(), this.targetBase.data());
                if (null != this.patchItem && this.patchItem.getKey().compareTo(key) == 0) {
                    this.tranPatch.transferTo(this.patchItem.getOffset(),
                            this.patchItem.getLength(), this.targetPatch);
                    writer.withEntry(key, this.entryMerger.merge(bb, this.targetPatch.data()));
                    nextPatchItem();
                }
                else {
                    writer.withEntry(key, this.entryMerger.merge(null, bb));
                }
            } finally {
                this.targetBase.reset();
                this.targetDelta.reset();
                this.targetPatch.reset();
            }
        }

        public void finish(HistoryWriter<T> writer) throws IOException {
            while (null != this.patchItem) {
                this.tranPatch.transferTo(this.patchItem.getOffset(),
                        this.patchItem.getLength(), this.targetPatch);
                writer.withEntry(this.patchItem.getKey(),
                        this.entryMerger.merge(null, this.targetPatch.data()));
                nextPatchItem();
            }
        }
    }

    private void merge(DataFile dfBase, DataFile dfDelta, DataFile dfPatch,
            EntryMerger entryMerger, HistoryWriter<T> writer) throws IOException {
        final Iterator<Item<T>> itBase = new ItemExtractor<>(this.clazz, dfBase).iterator();
        final Iterator<Item<T>> itDelta = new ItemExtractor<>(this.clazz, dfDelta).iterator();

        final BufferedBytesTransporter tranBase = new BufferedBytesTransporter(dfBase, getBufSize());
        final BufferedBytesTransporter tranDelta = new BufferedBytesTransporter(dfDelta, getBufSize());

        final ByteArrayTarget targetBase = new ByteArrayTarget();
        final ByteArrayTarget targetDelta = new ByteArrayTarget();
        final ThreeWayMerger threeWayMerger = new ThreeWayMerger(targetBase, targetDelta,
                dfPatch, entryMerger);

        Item<T> itemBase = nextItem(itBase);
        Item<T> itemDelta = nextItem(itDelta);

        while (null != itemBase || null != itemDelta) {
            final int diff = compare(itemBase, itemDelta);
            if (diff == 0) {
                // merge, advance both iterator
                tranDelta.transferTo(itemDelta.getOffset(), itemDelta.getLength(), targetDelta);
                tranBase.transferTo(itemBase.getOffset(), itemBase.getLength(), targetBase);

                threeWayMerger.forKey(itemBase.getKey(), writer);

                itemBase = nextItem(itBase);
                itemDelta = nextItem(itDelta);
            }
            else if (diff > 0) {
                if (null != itemDelta) {
                    // from delta to result, advance delta iterator
                    tranDelta.transferTo(itemDelta.getOffset(), itemDelta.getLength(), targetDelta);
                    threeWayMerger.forKey(itemDelta.getKey(), writer);
                    itemDelta = nextItem(itDelta);
                }
                else {
                    // base iterator contains more symbols than delta iterator
                    if (this.symbolRetainer.shouldRetain(itemBase.getKey())) {
                        // from base to result, advance base iterator
                        tranBase.transferTo(itemBase.getOffset(), itemBase.getLength(), targetBase);
                        threeWayMerger.forKey(itemBase.getKey(), writer);
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
                        threeWayMerger.forKey(itemBase.getKey(), writer);
                    }
                    // in compact mode, jump over symbols without ticks in delta
                    itemBase = nextItem(itBase);
                }
                else {
                    // delta iterator contains more symbols than base iterator
                    // from delta to result, advance delta iterator
                    tranDelta.transferTo(itemDelta.getOffset(), itemDelta.getLength(), targetDelta);
                    threeWayMerger.forKey(itemDelta.getKey(), writer);
                    itemDelta = nextItem(itDelta);
                }
            }
        }

        threeWayMerger.finish(writer);
    }
}
