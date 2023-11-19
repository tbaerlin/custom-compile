/*
 * AbstractFeedAdminCommand.java
 *
 * Created on 22.06.2005 15:26:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.feed.admin.dao.ConnectionInfo;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AbstractFeedAdminCommand {
    private String name;

    private MBeanServerConnection connection;

    private ObjectName objectName;

    private String serverName;

    private ConnectionInfo info;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConnectionInfo getInfo() {
        return info;
    }

    public void setInfo(ConnectionInfo info) {
        this.info = info;
    }

    public MBeanServerConnection getConnection() {
        return this.info.getConnection();
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
