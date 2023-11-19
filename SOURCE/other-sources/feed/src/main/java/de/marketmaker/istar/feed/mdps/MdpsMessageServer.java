/*
 * MdpsMessageServer.java
 *
 * Created on 06.03.2006 09:47:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import de.marketmaker.istar.common.nioframework.ByteArrayServer;
import de.marketmaker.istar.common.nioframework.AcceptorListener;
import de.marketmaker.istar.common.nioframework.Acceptor;
import de.marketmaker.istar.feed.connect.BufferWriter;

/**
 * Send mdps records to clients that actively connect to this server.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsMessageServer extends AbstractMdpsMessageServer implements AcceptorListener {

    public void socketConnected(Acceptor acceptor, SocketChannel sc) throws IOException {
        // just forward new client connection to server.
        getServer().addClient(acceptor, sc);
    }
}
