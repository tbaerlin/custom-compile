/*
 * Server.java
 *
 * Created on 01.03.2011 17:04:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.sample;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class Server {
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("de.marketmaker.itools.amqprpc.connections:type=AmqpRpcConnectionManager");

        ApplicationContext cxt = new ClassPathXmlApplicationContext("serverContext.xml", Server.class);
        mbs.registerMBean(cxt.getBean("connectionManager"), name);

//        System.out.println(((AmqpServiceExporter) cxt.getBean("serviceAmqpExporter")).getAddress());
    }
}
