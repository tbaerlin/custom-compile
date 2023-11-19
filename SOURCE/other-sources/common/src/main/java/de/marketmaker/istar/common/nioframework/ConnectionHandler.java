/*
 * ConnectionHandler.java
 *
 * Created on 30.06.2006 07:06:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;

import java.nio.channels.SocketChannel;
import java.io.IOException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ConnectionHandler extends SelectorThreadProvider {
    void handleConnection(SocketChannel sc) throws IOException;
}
