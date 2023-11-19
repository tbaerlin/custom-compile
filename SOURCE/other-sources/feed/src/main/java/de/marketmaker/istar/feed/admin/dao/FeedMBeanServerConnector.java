/*
 * FeedMBeanServerDao.java
 *
 * Created on 24.04.2005 11:37:48
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.jmx.IstarNamingStrategy;

/**
 * Keeps track of all the JMX backend services, pings them regularly and tries to reconnect
 * to services that have been lost temporarily.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FeedMBeanServerConnector implements InitializingBean, DisposableBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, String> backendServiceUrls = null;

    private Map<String, ConnectionInfo> backendServices = new HashMap<>();

    private Timer t = new Timer(FeedMBeanServerConnector.class.getSimpleName(), true);

    public void setBackendServiceUrls(Map<String, String> backendServiceUrls) {
        this.backendServiceUrls = backendServiceUrls;
    }

    public String[] getBackendNames() {
        return this.backendServiceUrls.keySet().stream().toArray(String[]::new);
    }

    public synchronized ConnectionInfo getConnectionInfo(String backendName) {
        ConnectionInfo result = this.backendServices.get(backendName);
        if (result == null) {
            result = connect(backendName);
            if (result != null && !ping(backendName, result)) {
                result = connect(backendName);
            }
        }
        if (result != null) {
            result.ackAccess();
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.backendServiceUrls == null || this.backendServiceUrls.isEmpty()) {
            throw new IllegalStateException("no backendServiceUrls set");
        }
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                closeIdleConnections();
            }
        }, DateTimeConstants.MILLIS_PER_MINUTE, DateTimeConstants.MILLIS_PER_MINUTE);
    }

    private synchronized void closeIdleConnections() {
        long threshold = System.currentTimeMillis() - 5 * DateTimeConstants.MILLIS_PER_MINUTE;
        for (String name : this.backendServiceUrls.keySet()) {
            ConnectionInfo ci = this.backendServices.get(name);
            if (ci != null && ci.lastAccessAt < threshold) {
                close(name);
            }
        }
    }

    public String getUrlForName(String serviceName) {
        return this.backendServiceUrls.get(serviceName);
    }

    private ConnectionInfo connect(String serviceName) {
        final String serviceUrl = this.backendServiceUrls.get(serviceName);
        this.logger.info("<connect> trying " + serviceUrl);
        try {
            JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(serviceUrl));
            MBeanServerConnection connection = connector.getMBeanServerConnection();

            final ConnectionInfo info = new ConnectionInfo(connector, connection);

            final ObjectName o1 = IstarNamingStrategy.byDomain();

            final Set<ObjectName> names = connection.queryNames(o1, null);

            if (names.isEmpty()) {
                throw new Exception("No Istar beans found");
            }

            info.setObjectNames(names);

            final Set<ObjectName> monitorNames  = connection.queryNames(new ObjectName("servo.monitors:*"), null);
            info.setWithMonitoring(!monitorNames.isEmpty());

            this.backendServices.put(serviceName, info);
            this.logger.info("<connect> succeeded for " + serviceName);
            return info;
        }
        catch (Exception e) {
            this.logger.warn("<connect> failed for " + serviceName + ": " + e.getMessage());
            return null;
        }
    }

    private void ackDisconnect(String serviceName) {
        close(serviceName);
    }

    @Override
    public synchronized void destroy() throws Exception {
        for (String s : this.backendServiceUrls.keySet()) {
            close(s);
        }
    }

    protected void close(String serviceName) {
        final ConnectionInfo info = this.backendServices.remove(serviceName);
        if (info == null) {
            return;
        }
        try {
            info.getConnector().close();
            this.logger.info("<close> " + serviceName);
        }
        catch (IOException e) {
            this.logger.warn("<close> failed for " + serviceName, e);
        }
    }

    private boolean ping(String serviceName, ConnectionInfo info) {
        try {
            info.getConnection().getDefaultDomain();
            return true;
        }
        catch (IOException e) {
            this.logger.warn("<ping> problem with " + serviceName + ": " + e.getMessage());
            ackDisconnect(serviceName);
            return false;
        }
    }
}
