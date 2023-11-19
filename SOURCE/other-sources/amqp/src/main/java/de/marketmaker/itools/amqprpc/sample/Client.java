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

import de.marketmaker.istar.common.amqp.ServiceProviderSelection;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class Client implements Runnable {

    Service proxy;

    public Service getProxy() {
        return proxy;
    }

    public void setProxy(Service proxy) {
        this.proxy = proxy;
    }

    public void run() {

        int i = 0;
        Integer pingPonged;

        do {
            ServiceProviderSelection.useLastProviderAgain();
//            ServiceProviderSelection.ID_FOR_NEXT_SEND.set(null);
//            System.out.println(ServiceProviderSelection.idForNextSend.get());
//            System.out.println("Sending " + i);
            pingPonged = null;
            try {
                pingPonged = (Integer) proxy.pingpong(++i);
            } catch (RemoteAccessException e) {
                System.out.println(Thread.currentThread().getName()
                        + " failed for " + i + ": " + e.getMessage());
            }
//            System.out.println(ServiceProviderSelection.idForNextSend.get());
            if (pingPonged != null) {
                System.out.println(Thread.currentThread().getName()
                        + " pingPonged = " + pingPonged);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (pingPonged == null || pingPonged < 1000);

        System.out.println("client terminated");
    }

    public static void main(String[] args) throws Exception {
        ApplicationContext cxt = new ClassPathXmlApplicationContext("clientContext.xml", Client.class);

        for (int i = 0; i < 1; ++i) {
            final Client client = (Client) cxt.getBean("client");
            new Thread(client, "Client-" + (i + 1)).start();
            Thread.sleep(100);
        }

//        ((PersistentAmqpConnectionManager) cxt.getBean("connectionManager")).closeCurrentConnection();

    }

}
