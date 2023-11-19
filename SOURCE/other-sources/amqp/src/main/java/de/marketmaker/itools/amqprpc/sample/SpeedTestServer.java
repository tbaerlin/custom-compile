/*
 * SpeedTestServer.java
 *
 * Created on 07.03.2011 10:51:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.sample;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class SpeedTestServer {
    public static void main(String[] args) throws Exception {
        new ClassPathXmlApplicationContext("speedTestServerContext.xml", SpeedTestServer.class);
    }
}
