/*
 * DumpTickWriter.java
 *
 * Created on 20.01.15 17:16
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.nio.ByteBuffer;

/**
 * @author oflege
 */
public class DumpTickWriter extends TickWriter {
    @Override
    protected int getTickLength(ByteBuffer bb, int p) {
        int n = bb.get(p);
        if (n < 0) {
            return 1 + (n & 0x7F);
        }
        return 2 + ((n << 7) | (bb.get(p + 1) & 0x7F));
    }
}
