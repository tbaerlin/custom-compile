/*
 * DefaultIndexHandler.java
 *
 * Created on 17.02.15 08:28
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.tick.TickFiles;

/**
 * IndexHandler which creates a vwdcode for each tick file index entry before
 * @author oflege
 */
public abstract class DefaultIndexHandler implements TickFileIndexReader.IndexHandler {
    private final ByteString dotMarket;

    public DefaultIndexHandler(File f) {
        this.dotMarket = new ByteString("." + TickFiles.getMarketBaseName(f));
    }

    @Override
    public final void handle(ByteString key, long position, int length) {
        doHandle(addMarket(key), position, length);
    }

    protected abstract void doHandle(ByteString vwdcode, long position, int length);

    private ByteString addMarket(ByteString key) {
        final int i = key.indexOf('.');
        return (i < 0) ? key.append(this.dotMarket) : key.replace(i, i, this.dotMarket);
    }
}
