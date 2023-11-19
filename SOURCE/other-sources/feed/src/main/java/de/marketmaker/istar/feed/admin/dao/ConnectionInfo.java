/*
 * ConnectionInfo.java
 *
 * Created on 24.04.2005 11:56:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ConnectionInfo {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JMXConnector connector;

    private final MBeanServerConnection connection;

    private Set<ObjectName> objectNames = Collections.emptySet();

    private final Map<String, Object> properties = new ConcurrentHashMap<>();

    private boolean withMonitoring;

    long lastAccessAt;


    ConnectionInfo(JMXConnector connector, MBeanServerConnection connection) {
        this.connector = connector;
        this.connection = connection;
    }

    void ackAccess() {
        this.lastAccessAt = System.currentTimeMillis();
    }

    public MBeanServerConnection getConnection() {
        return this.connection;
    }

    public JMXConnector getConnector() {
        return this.connector;
    }

    public boolean isWithMonitoring() {
        return withMonitoring;
    }

    public void setWithMonitoring(boolean withMonitoring) {
        this.withMonitoring = withMonitoring;
    }

    void setObjectNames(Set<ObjectName> objectNames) {
        this.objectNames = objectNames;
        this.logger.info("<setObjectNames> " + objectNames);
    }

    public Set<ObjectName> getObjectNames(String pattern) {
        ObjectName onPattern;
        try {
            onPattern = new ObjectName(pattern);

            final Set<ObjectName> result = new HashSet<>();
            for (ObjectName name : objectNames) {
                if (onPattern.apply(name)) {
                    result.add(name);
                }
            }
//            if (result.isEmpty()) {
//                this.logger.warn("<getObjectNames> no match for " + pattern);
//            }
            return result;
        } catch (MalformedObjectNameException e) {
            this.logger.warn("<getObjectNames> malformed name", e);
            return Collections.emptySet();
        }
    }

    public Object get(String key) {
        return properties.get(key);
    }

    public Object put(String key, Object value) {
        return properties.put(key, value);
    }

    public Map getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }
}
