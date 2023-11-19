/*
 * Client.java
 *
 * Created on 01.03.2011 16:55:48
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.sample;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.remoting.RemoteAccessException;

import de.marketmaker.itools.amqprpc.connections.PersistentAmqpConnectionManager;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class SpeedTestClient {

    SpeedTestService proxy;

    final int oneKB = 1024;
    final int oneMB = 1024 * oneKB;
    final int tenMB = 10 * oneMB;

    int packetSize;

    public SpeedTestService getProxy() {
        return proxy;
    }

    public void setProxy(SpeedTestService proxy) {
        this.proxy = proxy;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public void run(int numThreads) {
        Worker[] workers = new Worker[numThreads];
        for (int i = 0; i < numThreads; ++i) {
            Worker w = new Worker(packetSize);
            w.setName("Worker " + i);
            workers[i] = w;
            w.start();
        }
        while (true) {
            final long then = System.nanoTime();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            float totalKbs = 0;
            int totalExc = 0;
            for (Worker worker : workers) {
                final float kbs = 1024.0f * 1024.0f * worker.received / (System.nanoTime() - then);
                totalKbs += kbs;
                totalExc += worker.exceptions;
//                System.out.println(worker.getName() + ": " + kbs + " KB/s\t\t" +
//                        worker.exceptions + " exceptions");
                worker.received = 0;
                worker.exceptions = 0;
            }
            System.out.println("TOTAL: " + totalKbs + " KB/s\t\t" + totalExc + " exceptions");
            System.out.println();
        }
    }

    class Worker extends Thread {
        int size;

        volatile int received = 0;
        volatile int exceptions = 0;


        public Worker(int size) {
            this.size = size;
        }

        @Override
        public void run() {
            final String me = Thread.currentThread().getName();
            while (true) {
                try {
                    byte[] result = proxy.returnJunkOfSize(size);
                    received += result.length;
                } catch (RemoteAccessException e) {
                    exceptions++;
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {
        ApplicationContext cxt = new ClassPathXmlApplicationContext("speedTestClientContext.xml",
                SpeedTestClient.class);

        SpeedTestClient client = (SpeedTestClient) cxt.getBean("client");
        client.run(6);

        ((PersistentAmqpConnectionManager) cxt.getBean("connectionManager")).closeCurrentConnection();
    }

}