/*
 * AbstractMdpsMessageServer.java
 *
 * Created on 30.06.2006 09:16:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.nioframework.ByteArrayServer;
import de.marketmaker.istar.feed.connect.BufferWriter;

/**
 * Base class for servers that send mdps feed records to connected clients.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractMdpsMessageServer implements BufferWriter {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private ByteArrayServer server;

    public void setServer(ByteArrayServer server) {
        this.server = server;
    }

    protected ByteArrayServer getServer() {
        return this.server;
    }

    public void write(ByteBuffer bb) {
        this.server.send(bb);
    }
}
