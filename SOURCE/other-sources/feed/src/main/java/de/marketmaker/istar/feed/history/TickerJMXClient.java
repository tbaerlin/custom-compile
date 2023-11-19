/*
 * TickHistoryPersisterJMXClient.java
 *
 * Created on 26.07.12 14:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import de.marketmaker.istar.common.util.IoUtils;

/**
 * @author zzhao
 */
public class TickerJMXClient implements Closeable {

    private static final String DEFAULT_ON = "de.marketmaker.istar:" +
            "type=TickHistoryController," +
            "name=tickHistoryController";

    private final JMXConnector connector;

    private final TickerMBean mBean;

    public TickerJMXClient(String jmxUrl, String objectName)
            throws IOException, MalformedObjectNameException {
        final JMXServiceURL url = new JMXServiceURL(jmxUrl);
        this.connector = JMXConnectorFactory.connect(url, null);
        final MBeanServerConnection conn = connector.getMBeanServerConnection();
        this.mBean = MBeanServerInvocationHandler.newProxyInstance(conn,
                new ObjectName(objectName), TickerMBean.class, false);
    }

    public static void main(String[] args) throws Exception {
        if (null == args || args.length != 3) {
            System.err.println("Usage: [MBean serverUrl] [ObjectName] [tick dir]");
            System.exit(1);
        }
        TickerJMXClient client = null;
        try {
            client = new TickerJMXClient(args[0], args[1]);
            System.exit(client.tick(new File(args[2])));
        } finally {
            IoUtils.close(client);
        }
    }

    public int tick(File tickDir) {
        if (tickDir.exists()) {
            final boolean succ = this.mBean.tick(tickDir.getAbsolutePath());
            System.out.println("scheduled tick for: " + tickDir.getAbsolutePath() + ", " + succ);
            return succ ? 0 : 1;
        }
        else {
            System.out.println(tickDir.getAbsolutePath() + " not found");
            return 1;
        }
    }


    @Override
    public void close() throws IOException {
        this.connector.close();
    }
}
