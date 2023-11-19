/*
 * ByteBufferResponse.java
 *
 * Created on 18.10.13 14:33
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.nio.ByteBuffer;

/**
 * Wrapper around a response in a ByteBuffer
 * @author oflege
 */
class ByteBufferResponse implements Response {

    private final ByteBuffer msg;

    public ByteBufferResponse(ByteBuffer msg) {
        this.msg = msg;
    }

    @Override
    public int size() {
        return this.msg.remaining();
    }

    @Override
    public void appendTo(ByteBuffer bb) {
        bb.put(this.msg);
    }
}
