/*
 * NioUtils.java
 *
 * Created on 07.06.2002 08:06:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.net.InetSocketAddress;

/** collection of utils for the java.nio package.
 *
 * @author Oliver Flege
 * @version $Id: NioUtils.java,v 1.1 2004/11/17 15:12:36 tkiesgen Exp $
 */
public final class NioUtils {

    /**
     * do not allow instantiation.
     */
    private NioUtils() {
    }

    /**
     * create a non-blocking SocketChannel.
     * @return the newly created and connected SocketChannel
     * @throws IOException the typical exception raised by used classes
     */
    public static SocketChannel createNBSocketChannel(InetSocketAddress isa, int receiveBufferSize)
            throws IOException {
        return createSocketChannel(isa, false, receiveBufferSize);
    }

    /**
     * create a blocking SocketChannel.
     * @return the newly created and connected SocketChannel
     * @throws IOException the typical exception raised by used classes
     */
    public static SocketChannel createSocketChannel(InetSocketAddress isa, int receiveBufferSize)
            throws IOException {
        return createSocketChannel(isa, true, receiveBufferSize);
    }

    private static SocketChannel createSocketChannel(InetSocketAddress isa, boolean blocking, int receiveBufferSize) throws IOException {
        SocketChannel sChannel = SocketChannel.open();
        sChannel.configureBlocking(blocking);

        if (receiveBufferSize > 0) {
            sChannel.socket().setReceiveBufferSize(receiveBufferSize);
        }

        sChannel.connect(isa);
        while (!sChannel.finishConnect()) {
            try {
                Thread.sleep(10);
            }
            catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return sChannel;
    }

    /**
     * Closes the given socket channel
     * @param sc to be closed
     * @throws IOException if closing fails
     */
    public static void close(final SocketChannel sc) throws IOException {
        sc.socket().setTcpNoDelay(true);
        sc.socket().shutdownInput();
        sc.socket().shutdownOutput();

        sc.close();
        sc.socket().close();
    }


}
