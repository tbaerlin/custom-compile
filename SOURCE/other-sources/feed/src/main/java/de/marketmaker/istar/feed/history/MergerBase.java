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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.io.OffsetLengthCoder;

/**
 * @author zzhao
 */
public abstract class MergerBase<T extends Comparable<T>> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final File base;

    protected final File delta;

    protected final Class<T> clazz;

    private final OffsetLengthCoder coder;

    public MergerBase(File base, File delta, Class<T> clazz) throws IOException {
        HistoryUnit.ensureSameContent(base, delta);
        this.base = base;
        this.delta = delta;
        this.clazz = clazz;
        this.coder = new OffsetLengthCoder(Integer.getInteger("LengthBits",
                getOffsetLengthCoder()));
        this.logger.info("<MergerBase> use " + this.coder);
    }

    private int getOffsetLengthCoder() throws IOException {
        return this.base.exists() && this.base.length() > 0
                ? HistoryReader.fromHistoryFile(this.base)
                : HistoryReader.fromHistoryFile(this.delta);
    }

    protected int getBufSize() {
        return this.coder.maxLength();
    }

    protected OffsetLengthCoder getCoder() {
        return this.coder;
    }

    public abstract void merge(File result, EntryMerger entryMerger) throws IOException;

    public static <T extends Comparable<T>> Item<T> nextItem(Iterator<Item<T>> it) {
        return it.hasNext() ? it.next() : null;
    }

    protected int compare(Item<T> itemA, Item<T> itemB) {
        // itemA and itemB won't be null at the same time
        if (null != itemA && null != itemB) {
            return itemA.compareTo(itemB);
        }
        else if (null == itemA) {
            return -1;
        }
        else {
            return 1;
        }
    }
}
