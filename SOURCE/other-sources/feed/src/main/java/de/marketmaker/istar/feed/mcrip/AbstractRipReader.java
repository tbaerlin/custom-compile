/*
 * AbstractRipReader.java
 *
 * Created on 15.07.13 09:16
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mcrip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.zip.InflaterInputStream;

import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.common.io.ByteBufferOutputStream;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VolatileFeedDataRegistry;
import de.marketmaker.istar.feed.ordered.MulticastFeedParser;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;

/**
 * @author oflege
 */
public abstract class AbstractRipReader {

    protected int numFiles = 0;

    protected FeedUpdateFormatter fmt = new FeedUpdateFormatter(Boolean.getBoolean("withSourceId"));

    protected void read(File f, VendorkeyFilter filter,
            OrderedUpdateBuilder... builders) throws Exception {
        if (!f.canRead()) {
            System.err.println("no such file: " + f.getAbsolutePath());
            return;
        }
        System.err.println("file: " + f.getAbsolutePath());
        this.numFiles++;

        ByteBuffer b = ByteBuffer.allocate(1024 * 600).order(ByteOrder.LITTLE_ENDIAN);
        b.flip();
        final FileChannel channel = new RandomAccessFile(f, "r").getChannel();

        ByteBufferOutputStream os = new ByteBufferOutputStream(b.capacity() << 2);

        MulticastFeedParser mfp = createParser(filter, builders);

        do {
            if (b.remaining() < 4) {
                if (!compactAndFill(b, channel, 4)) {
                    System.err.println("incomplete file, fragment starts with length");
                    break;
                }
            }
            final int length = b.getInt();
            if (b.remaining() < length) {
                if (!compactAndFill(b, channel, length)) {
                    System.err.println("incomplete file, fragment of length " + length);
                    break;
                }
            }

            final ByteBuffer data = inflateData(b, length, os);
            onEvent(mfp, data);

            b.position(b.position() + length);
        } while (b.hasRemaining());

        channel.close();
    }

    protected MulticastFeedParser createParser(VendorkeyFilter filter,
            OrderedUpdateBuilder[] builders) throws Exception {
        MulticastFeedParser mfp = new MulticastFeedParser();
        mfp.setBuilders(builders);
        mfp.setVendorkeyFilter(filter);
        mfp.setUseFilterInUpdate(true);
        mfp.setRegistry(getRegistry());
        mfp.afterPropertiesSet();
        return mfp;
    }

    protected VolatileFeedDataRegistry getRegistry() {
        return new VolatileFeedDataRegistry();
    }

    protected void onEvent(MulticastFeedParser mfp, ByteBuffer data) {
        mfp.onEvent(data);
    }

    private boolean compactAndFill(ByteBuffer b, FileChannel channel,
            int n) throws IOException {
        b.compact();
        channel.read(b);
        b.flip();
        return b.remaining() >= n;
    }

    private ByteBuffer inflateData(ByteBuffer b, int length, ByteBufferOutputStream os)
            throws IOException {
        final InflaterInputStream iis = new InflaterInputStream(
                new ByteArrayInputStream(b.array(), b.position(), length));
        os.reset();
        FileCopyUtils.copy(iis, os);
        return os.toBuffer().order(ByteOrder.LITTLE_ENDIAN);
    }
}
