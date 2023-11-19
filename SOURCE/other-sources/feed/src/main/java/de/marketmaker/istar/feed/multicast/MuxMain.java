/*
 * MuxMain.java
 *
 * Created on 21.11.14 15:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.multicast;

import java.util.concurrent.CountDownLatch;

import de.marketmaker.istar.common.nioframework.SelectorThread;
import de.marketmaker.istar.feed.VolatileFeedDataRegistry;
import de.marketmaker.istar.feed.mux.MuxIn;
import de.marketmaker.istar.feed.mux.MuxProtocolHandlers;
import de.marketmaker.istar.feed.ordered.MulticastFeedParser;

/**
 * Sample starting point for a program that uses a SelectorThread and processes
 * a multicast feed dump (e.g. created by connecting to an
 * output socket of <tt>istar-multicast-receive</tt> and dumping the results to a file).
 * <p>
 * In order to make the feed available on a local socket, use for example netcat like this:
 * <pre>
 * cat feeddump.bin | nc -l 8987
 * </pre>
 * @author oflege
 */
public class MuxMain {
    public static void main(String[] args) throws Exception {
        final CountDownLatch stopLatch = new CountDownLatch(1);

        MulticastFeedParser p = new MulticastFeedParser();
        p.setRegistry(new VolatileFeedDataRegistry());
        p.afterPropertiesSet();

//        p.setBuilders(...);

        final SelectorThread st = new SelectorThread();

        MuxIn muxIn = new MuxIn(st) {
            @Override
            protected void onInClosed() {
                super.onInClosed();
                stopLatch.countDown();
            }
        };
        muxIn.setOut(p);
        muxIn.setProtocolHandler(MuxProtocolHandlers.MDPS_V3);
        muxIn.setPrimarySourceAddress("127.0.0.1:8987");
        muxIn.afterPropertiesSet();

        st.start();
        muxIn.start();

        stopLatch.await();
        muxIn.destroy();
        st.stop();
    }
}
