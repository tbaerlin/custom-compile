/*
 * TestRatioUpdateableMulticast.java
 *
 * Created on 30.04.2010 15:41:30
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios;

import java.nio.ByteBuffer;
import java.util.Arrays;

import de.marketmaker.istar.common.mcast.MulticastReceiverImpl;
import de.marketmaker.istar.common.mcast.MulticastSenderImpl;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.backend.RatiosEncoder;

/**
 * @author oflege
 */
public class TestRatioUpdateableMulticast {
    private static String groupname = "224.0.0.0";

    private static int port = 14000;

    private static int nameLength = 1400;

    private static int num = 20;

    private static int sleepTime = 100;

    private static String interfaceName = "eth0";


    public static void main(String[] args) throws Exception {
        boolean send = true;
        int i = 0;
        while (i < args.length) {
            if ("-r".equals(args[i])) {
                send = false;
            }
            if ("-i".equals(args[i])) {
                interfaceName = args[++i];
            }
            if ("-g".equals(args[i])) {
                groupname = args[++i];
            }
            if ("-p".equals(args[i])) {
                port = Integer.parseInt(args[++i]);
            }
            if ("-l".equals(args[i])) {
                nameLength = Integer.parseInt(args[++i]);
            }
            if ("-n".equals(args[i])) {
                num = Integer.parseInt(args[++i]);
            }
            if ("-t".equals(args[i])) {
                sleepTime = Integer.parseInt(args[++i]);
            }
            i++;
        }

        if (!send) {
            receive();
        }
        else {
            send();
        }
    }

    private static void send() throws Exception {
        final MulticastSenderImpl ms = new MulticastSenderImpl();
        ms.setGroupname(groupname);
        ms.setPort(port);
        ms.setInterfaceName(interfaceName);
        ms.setDoSend(true);
        ms.initialize();

        final RatioUpdateableMulticastSender sender = new RatioUpdateableMulticastSender();
        sender.setSender(ms);
        sender.start();
        sender.afterPropertiesSet();

        final char[] nameChars = new char[nameLength];
        Arrays.fill(nameChars, 'X');
        final String name = String.valueOf(nameChars);

        final RatiosEncoder encoder = new RatiosEncoder();
        for (int i = 0; i < num; ) {
            encoder.reset(InstrumentTypeEnum.FND, 2187L, 136079L);
            encoder.add(RatioFieldDescription.name, name);
            sender.update(encoder.getData());
            if (num < 100) {
                System.out.println(++i);
            }
            Thread.sleep(sleepTime);
        }

        Thread.sleep(5000);
//        mri.stop();
        sender.stop();
    }

    private static void receive() throws Exception {
        MulticastReceiverImpl mri = new MulticastReceiverImpl();
        mri.setGroupname(groupname);
        mri.setPort(port);
        mri.setInterfaceName(interfaceName);
        mri.setReceiveBufferSize(1 << 20);
        mri.afterPropertiesSet();

        RatioUpdateableMulticastReceiver receiver = new RatioUpdateableMulticastReceiver();
        receiver.setReceiver(mri);
        receiver.setRatioUpdateable(new RatioUpdateable() {
            int n = 0;
            public void update(ByteBuffer bytes) {
                System.out.println(++n + " = " + bytes.position() + "/" + bytes.limit());
            }
        });
        receiver.start();
    }

}
