/*
 * TickChunk.java
 *
 * Created on 04.11.2004 14:58:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.delay;

import java.nio.ByteBuffer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class DataChunk {
    private DataChunk previous;

    private DataChunk next;

    private final ByteBuffer data;

    DataChunk(ByteBuffer data) {
        this.data = data;
    }

    void setPrevious(DataChunk previous) {
        this.previous = previous;
        if (previous != null) {
            previous.next = this;
        }
    }

    DataChunk getNext() {
        return next;
    }

    DataChunk getPrevious() {
        return this.previous;
    }

    int remaining() {
        return this.data.remaining();
    }

    /**
     * adds the contents of bb to this chunk, adds at most {@link #remaining()} or
     * bb.remaining() bytes, whichever is smaller.
     *
     * @param bb content to be added.
     */
    void append(ByteBuffer bb) {
        this.data.put(bb);
    }

    ByteBuffer getData() {
        final ByteBuffer result = this.data.duplicate();
        result.order(this.data.order()).flip();
        return result;
    }

    void reset() {
        this.data.clear();
        this.previous = null;
        this.next = null;
    }

    int getLength() {
        return this.data.position();
    }

    public String toString() {
        return "DataChunk[@" + Integer.toHexString(System.identityHashCode(this))
                + "  " + this.data
                + "]";
    }
}
