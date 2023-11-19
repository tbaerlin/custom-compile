/*
 * TickEntryIterator.java
 *
 * Created on 28.09.12 14:51
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * @author zzhao
 */
public class MutableEntryIterator<T extends MutableEntry> implements Iterable<T>, Iterator<T> {

    private final ByteBuffer bb;

    private final int limit;

    private final T entry;

    public MutableEntryIterator(ByteBuffer bb, Class<T> clazz) {
        this.limit = bb.limit();
        this.bb = bb.asReadOnlyBuffer();
        try {
            this.entry = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("cannot instantiate entry", e);
        }
    }

    @Override
    public Iterator<T> iterator() {
        this.bb.position(0);
        this.bb.limit(this.limit);
        return this;
    }

    @Override
    public boolean hasNext() {
        return this.bb.hasRemaining();
    }

    @Override
    public T next() {
        EntryFactory.fromBuffer(this.bb, this.entry);
        return this.entry;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("not implemented");
    }
}
