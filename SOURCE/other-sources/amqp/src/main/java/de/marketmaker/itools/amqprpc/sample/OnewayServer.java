/*
 * OnewayServer.java
 *
 * Created on 18.03.2011 17:25:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.sample;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class OnewayServer {
    public static void main(String[] args) throws Exception {
        new ClassPathXmlApplicationContext("onewayServerContext.xml", OnewayServer.class);
    }
}
