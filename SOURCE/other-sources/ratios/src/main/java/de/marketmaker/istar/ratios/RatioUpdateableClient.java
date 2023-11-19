/*
 * RatioUpdateableMulticastSender.java
 *
 * Created on 31.07.2006 18:55:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.marketmaker.istar.feed.connect.BufferWriter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatioUpdateableClient implements BufferWriter {

    private RatioUpdateable ratioUpdateable;

    public void setRatioUpdateable(RatioUpdateable ratioUpdateable) {
        this.ratioUpdateable = ratioUpdateable;
    }

    public void write(ByteBuffer bb) throws IOException {        
        final int totalLimit = bb.limit();
        while (bb.hasRemaining()) {
            final int start = bb.position();
            if (bb.remaining() < 4) {
                return;
            }
            final int len = bb.getInt();
            if (bb.remaining() < bb.position() + len) {
                bb.position(start);
                return;
            }
            bb.limit(bb.position() + len);
            this.ratioUpdateable.update(bb);
            bb.position(bb.limit());
            bb.limit(totalLimit);
        }
    }
}