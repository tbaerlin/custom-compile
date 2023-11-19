/*
 * DelayBucket.java
 *
 * Created on 07.02.2005 17:14:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.delay;

import java.nio.ByteBuffer;

/**
 * Stores all delayed data that needs to be released in the same second
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class DelayBucket {
    private final DataChunk firstChunk;

    private DataChunk currentChunk;

    DelayBucket(DataChunk chunk) {
        this.firstChunk = chunk;
        this.currentChunk = chunk;
    }

    DataChunk getFirstChunk() {
        return firstChunk;
    }

    DataChunk getLastChunk() {
        return currentChunk;
    }

    boolean canAppend(ByteBuffer bb) {
        return bb.remaining() <= this.currentChunk.remaining();
    }

    void append(DataChunk chunk) {
        chunk.setPrevious(this.currentChunk);
        this.currentChunk = chunk;
    }

    boolean append(ByteBuffer bb) {
        if (canAppend(bb)) {
            this.currentChunk.append(bb);
            return true;
        }
        return false;
    }

    int length() {
        int n = this.firstChunk.getLength();
        DataChunk tmp = this.firstChunk.getNext();
        while (tmp != null) {
            n += tmp.getLength();
            tmp = tmp.getNext();
        }
        return n;
    }

    int numChunks() {
        int result = 1;
        DataChunk tmp = this.firstChunk.getNext();
        while (tmp != null) {
            result++;
            tmp = tmp.getNext();
        }
        return result;
    }
}
