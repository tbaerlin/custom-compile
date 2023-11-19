/*
 * ByteBufferTarget.java
 *
 * Created on 26.10.12 13:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.nio.ByteBuffer;

/**
 * @author zzhao
 */
public class ByteArrayTarget implements TransferTarget {

    private byte[] bytes;

    @Override
    public int transfer(ByteBuffer bb) {
        this.bytes = new byte[bb.remaining()];
        bb.get(this.bytes);
        return this.bytes.length;
    }

    public byte[] data() {
        return this.bytes;
    }

    public void reset() {
        this.bytes = null;
    }
}
