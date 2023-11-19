/*
 * FileKeySource.java
 *
 * Created on 17.12.2009 17:24:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;

/**
 * @author oflege
 */
class FileKeySource implements KeySource {

    private final RandomAccessFile raf;

    private FileChannel channel;

    private final ByteBuffer bb = ByteBuffer.allocate(8192);

    private boolean readAll = false;

    private ByteString vwdcode;

    private ByteString alias;

    private final FeedDataRepository repository;

    FileKeySource(FeedDataRepository repository, File keys) throws IOException {
        if (!keys.canRead()) {
            throw new IOException("no such file " + keys.getAbsolutePath());
        }
        this.raf = new RandomAccessFile(keys, "r");
        this.channel = raf.getChannel();
        this.repository = repository;
        this.bb.flip();
    }

    public boolean hasNext() throws IOException {
        if (this.bb.remaining() < 512) {
            if (!this.readAll) {
                this.bb.compact();
                this.readAll = this.channel.read(this.bb) == -1;
                this.bb.flip();
            }
            if (this.readAll && !this.bb.hasRemaining()) {
                return false;
            }
        }
        this.vwdcode = ByteString.readFrom(this.bb, ByteString.LENGTH_ENCODING_BYTE);
        this.alias = ByteString.readFrom(this.bb, ByteString.LENGTH_ENCODING_BYTE);
        return true;
    }

    public FeedData nextFeedData() {
        return this.repository.get(this.vwdcode);
    }

    public ByteString getAlias() {
        return this.alias;
    }

    public void close() {
        IoUtils.close(this.raf);
    }
}
