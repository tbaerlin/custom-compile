/*
 * OnewayClient.java
 *
 * Created on 18.03.2011 17:25:48
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.sample;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.marketmaker.itools.amqprpc.connections.AmqpRpcConnectionManager;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class OnewayClient {
    Service proxy;

    public Service getProxy() {
        return proxy;
    }

    public void setProxy(Service proxy) {
        this.proxy = proxy;
    }


    public static void main(String[] args) throws Exception {
        ApplicationContext cxt = new ClassPathXmlApplicationContext("onewayClientContext.xml", OnewayClient.class);

        for (int i = 0; i < 100; ++i) {
            try {
                Object result = ((OnewayClient) cxt.getBean("client")).getProxy().pingpong("NOTIFY TOKEN " + i);
                assert result == null;
                System.out.println("i = " + i);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }
        ((AmqpRpcConnectionManager) cxt.getBean("connectionManager")).destroy();
    }

}
