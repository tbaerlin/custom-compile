/*
 * BufferMulticastReceiver.java
 *
 * Created on 18.02.11 14:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.common.mcast.MulticastReceiver;
import de.marketmaker.istar.feed.FeedRecord;

/**
 * Receives data as a BufferWriter from one thread (e.g., a FeedConnector attached to some feed)
 * and acts as a (fake) MulticastReceiver by storing the received data in DatagramPakets
 * provided by another thread.
 * Useful for testing messages in multicast format that are actually sent over tcp.
 * @author oflege
 */
public class BufferMulticastReceiver implements MulticastReceiver, BufferWriter {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final InetSocketAddress mockAddress = new InetSocketAddress(9999);

    private final Exchanger<byte[]> exchanger = new Exchanger<>();

    public void write(ByteBuffer bb) throws IOException {
        while (bb.hasRemaining()) {
            bb.mark();
            final int len = bb.getShort() - 2;
            if (bb.remaining() < len) {
                bb.reset();
                return;
            }
            final byte[] bytes = new byte[len];
            bb.get(bytes);
            try {
                this.exchanger.exchange(bytes, 5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                this.logger.warn("<write> interrupted?!");
                return;
            } catch (TimeoutException e) {
                this.logger.warn("<write> timeout?!");
                return;
            }
        }
    }

    public void receive(DatagramPacket packet) throws IOException {
        packet.setSocketAddress(mockAddress);
        try {
            final byte[] data = this.exchanger.exchange(null);
            System.arraycopy(data, 0, packet.getData(), 0, data.length);
            packet.setLength(data.length);
        } catch (InterruptedException e) {
            this.logger.warn("<receive> interrupted?!");
        }
    }

    public static void main(String[] args) throws IOException {
        final byte[] bytes = FileCopyUtils.copyToByteArray(new File("d:/temp/dump.out"));
        final BufferMulticastReceiver receiver = new BufferMulticastReceiver();

        final SimpleMulticastRecordSource smrs = new SimpleMulticastRecordSource();
        smrs.setReceiver(receiver);
        smrs.setLengthIsInt(false);


        new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 10000000; i++) {
                    try {
                        final FeedRecord feedRecord = smrs.getFeedRecord();
                        System.out.println(feedRecord.getLength());

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        receiver.write(ByteBuffer.wrap(bytes));
    }
}
