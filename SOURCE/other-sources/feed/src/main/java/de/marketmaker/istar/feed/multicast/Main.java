/*
 * Main.java
 *
 * Created on 29.08.14 09:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.multicast;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import com.lmax.disruptor.EventHandler;

import de.marketmaker.istar.common.nioframework.Acceptor;
import de.marketmaker.istar.common.nioframework.SelectorThread;

/**
 * @author oflege
 */
class Main {
    public static void main(String[] args) throws Exception {
        String group = "224.0.1.20";
        String networkInterface = "lo0";
        int port = 24100;

        int i = 0;
        while (i < args.length && args[i].startsWith("-")) {
            if ("-ni".equals(args[i])) {
                networkInterface = args[++i];
            }
            else if ("-g".equals(args[i])) {
                group = args[++i];
            }
            else if ("-p".equals(args[i])) {
                port = Integer.parseInt(args[++i]);
            }
            i++;
        }

        SelectorThread st = new SelectorThread();

        FeedMulticastReceiver mr = new FeedMulticastReceiver();
        mr.setNetworkInterface(networkInterface);
        mr.setGroup(group);
        mr.setPort(port);
        mr.setSelectorThread(st);
        mr.setHandler(new EventHandler<ByteBuffer>() {
            int n = 0;
            @Override
            public void onEvent(ByteBuffer event, long sequence,
                    boolean endOfBatch) throws Exception {
                System.out.println("<= " + event + " " + event.getLong(0) + "  " + sequence + " " + endOfBatch);
            }
        });
        mr.afterPropertiesSet();


        FeedMulticastSender ms = new FeedMulticastSender();
        ms.setNetworkInterface(networkInterface);
        ms.setGroup(group);
        ms.setPort(port);
        ms.afterPropertiesSet();

        FeedMulticastStore store = new FeedMulticastStore(FeedMulticastSender.MULTICAST_PACKET_SIZE);
        store.setSender(ms);
        store.setNumBuffered(32);
        store.afterPropertiesSet();

        SelectorThread st2 = new SelectorThread();
        FeedTcpResender rsnd = new FeedTcpResender();
        rsnd.setSelectorThread(st2);
        rsnd.setStore(store);
        rsnd.afterPropertiesSet();

        Acceptor acceptor = new Acceptor();
        acceptor.setSelectorThread(st2);
        acceptor.setHost("localhost");
        acceptor.setListenPort(port + 1);
        acceptor.setMaxNumClients(16);
        acceptor.setSendBufferSize(1 << 20);
        acceptor.setListener(rsnd);
        acceptor.afterPropertiesSet();

        st.start();
        st2.start();
        mr.start();

        store.start();

        for (int n = 1; n < 24; n++) {
            store.write(ByteBuffer.allocate(1220 + (n & 0x7)));
            if ((n % 3) == 0) {
                TimeUnit.MILLISECONDS.sleep(20);
            }
        }
        store.stop();

        TimeUnit.MILLISECONDS.sleep(40);

        st2.stop();
        st.stop();
        mr.stop();

        System.out.println(mr.getNumPacketsReceived() + " / " + mr.getNumPacketsMissed() + " / " + mr.getNumTcpConnects());
    }

}
