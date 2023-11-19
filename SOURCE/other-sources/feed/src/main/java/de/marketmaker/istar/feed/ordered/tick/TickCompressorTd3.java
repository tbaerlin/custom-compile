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
import java.nio.ByteOrder;

import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldDataBuilder;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICK3;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICKZ;

/**
 * Compresses data in td3 tick files that have been compressed with snappy (fast but not that
 * good wrt. compression).
 * @author oflege
 */
class TickCompressorTd3 extends TickCompressor {

    /**
     * Temporary HACK to recode tick data. For some markets, bids/asks come only with zeit and
     * zeit_quotierung, not with boersenzeit. The old code would use zeit as tick time and
     * also store zeit_quotierung as tick field, as zeit usually comes w/o ms and zeit_quotierung has
     * them. If this flag is set, those files can be recoded such that zeit_quotierung is used
     * as time for bid/ask ticks.
     */
    private static final boolean USE_ZEIT_QUOTIERUNG = Boolean.getBoolean("useZeitQuotierung");

    private static final int BID_ASK_FLAGS = FeedUpdateFlags.FLAG_WITH_BID | FeedUpdateFlags.FLAG_WITH_ASK;

    private static class Recoder extends FieldDataBuilder {
        public Recoder() {
            super(255);
        }

        private boolean recode(BufferFieldData fd) {
            fd.getInt(); // ignore original time
            int id = fd.readNext();
            if (id != VwdFieldOrder.ORDER_ADF_ZEIT_QUOTIERUNG) {
                return false;
            }
            reset();
            putInt(fd.getInt());
            while (fd.readNext() > 0) {
                addFieldToBuffer(fd);
            }
            return true;
        }
    }

    private final Recoder builder = new Recoder();

    private final AbstractTickRecord.TickItem.Encoding encoding;

    protected TickCompressorTd3(File file, File out, Filter filter) {
        super(file, out, ByteOrder.LITTLE_ENDIAN, filter);
        this.encoding = file.getName().endsWith(".td3") ? TICK3 : TICKZ;
    }

    @Override
    protected int addTicks(OrderedTickData td, TickDeflater deflater) throws IOException {
        byte[] ticks = new byte[td.getLength()];
        int numSeeks = fillFileTickStoreTicks(td.getStoreAddress(), ticks);

        if (USE_ZEIT_QUOTIERUNG) {
            recodeElements(deflater, ticks);
        }
        else {
            addElements(deflater, ticks);
        }

        return numSeeks;
    }

    private void addElements(TickDeflater deflater, byte[] ticks) throws IOException {
        for (TickDecompressor.Element e : new TickDecompressor(ticks, encoding)) {
            deflater.add(e);
        }
    }

    private void recodeElements(TickDeflater deflater, byte[] ticks) throws IOException {
        for (TickDecompressor.Element e : new TickDecompressor(ticks, encoding)) {
            final int flags = e.getFlags();
            if ((flags & BID_ASK_FLAGS) == 0 || (flags & FeedUpdateFlags.FLAG_WITH_TRADE) != 0) {
                // not bid/ask or trade (which came with boersenzeit), just add e
                deflater.add(e);
                continue;
            }

            if (this.builder.recode(e.getData())) {
                deflater.add(flags, builder);
            }
            else {
                deflater.add(e);
            }
        }
    }
}
