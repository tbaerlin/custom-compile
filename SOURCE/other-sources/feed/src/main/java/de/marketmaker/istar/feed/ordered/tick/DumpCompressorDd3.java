/*
 * Foo.java
 *
 * Created on 05.12.12 07:38
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

import de.marketmaker.istar.feed.tick.AbstractTickRecord;

import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.DUMP3;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.DUMPZ;

/**
 * Compresses feeddump data in dd3 tick files that have been compressed with snappy (fast but not that
 * good wrt. compression).
 * @author oflege
 */
class DumpCompressorDd3 extends TickCompressor {

    private final AbstractTickRecord.TickItem.Encoding encoding;

    protected DumpCompressorDd3(File file, File out, Filter filter) {
        super(file, out, ByteOrder.LITTLE_ENDIAN, filter);
        this.encoding = file.getName().endsWith(FileTickStore.DD3) ? DUMP3 : DUMPZ;
    }

    @Override
    protected int addTicks(OrderedTickData td, TickDeflater deflater) throws IOException {
        byte[] ticks = new byte[td.getLength()];
        int numSeeks = fillFileTickStoreTicks(td.getStoreAddress(), ticks);
        addElements(deflater, ticks);
        return numSeeks;
    }

    private void addElements(TickDeflater deflater, byte[] ticks) throws IOException {
        Iterator<ByteBuffer> it = new DumpDecompressor(ticks, encoding).recordIterator(true);
        while (it.hasNext()) {
            deflater.add(it.next());
        }
    }
}
