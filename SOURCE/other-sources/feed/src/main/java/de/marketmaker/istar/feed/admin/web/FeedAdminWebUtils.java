/*
 * ConnectionInfoUtils.java
 *
 * Created on 25.04.2005 08:30:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import de.marketmaker.istar.common.jmx.IstarNamingStrategy;
import de.marketmaker.istar.feed.IntradayServer;
import de.marketmaker.istar.feed.admin.dao.ConnectionInfo;
import de.marketmaker.istar.feed.admin.dao.FeedMBeanServerConnector;
import de.marketmaker.istar.feed.dp.DpManager;
import de.marketmaker.istar.feed.tick.TickServer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FeedAdminWebUtils {

    public static class ManagableBean {
        private final ObjectName on;

        public ManagableBean(ObjectName on) {
            this.on = on;
        }

        public String getType() {
            return IstarNamingStrategy.getType(this.on);
        }

        public String getName() {
            return IstarNamingStrategy.getName(this.on);
        }
    }

    private static final String[] BEAN_TYPES = new String[] {
        ClassUtils.getShortName(IntradayServer.class),
        ClassUtils.getShortName(TickServer.class),
        ClassUtils.getShortName(DpManager.class),
    };

    private FeedAdminWebUtils() {
    }

    public static String getServerName(HttpServletRequest httpServletRequest, FeedMBeanServerConnector connector) {

        String serverName = httpServletRequest.getParameter("serverName");
        if (StringUtils.hasText(serverName)) {
            return serverName;
        }

        serverName = (String) WebUtils.getSessionAttribute(httpServletRequest, "serverName");
        ConnectionInfo ci = null;
        if (serverName != null) {
            ci = connector.getConnectionInfo(serverName);
        }

        if (ci != null) {
            return serverName;
        }

        final String[] backendNames = connector.getBackendNames();
        if (backendNames == null || backendNames.length == 0) {
            return null;
        }
        serverName = backendNames[0];
        WebUtils.setSessionAttribute(httpServletRequest, "serverName", serverName);

        return serverName;
    }

    public static ObjectName getDefaultObject(ConnectionInfo info) {
        for (String s : BEAN_TYPES) {
            Set<ObjectName> objectNames = info.getObjectNames(IstarNamingStrategy.byTypeStr(s));
            if (objectNames.size() == 1) {
                return objectNames.iterator().next();
            }
            else if (objectNames.size() > 1) {
                return sort(objectNames)[1];
            }
        }
        return null;
    }

    public static Map<String, Object> getDefaultModel(FeedMBeanServerConnector connector, String serverName) {
        final ConnectionInfo info = connector.getConnectionInfo(serverName);
        Map<String, Object> result = new HashMap<>();

        final List<ObjectName> tmp = new ArrayList<>();

        for (String BEAN_TYPE : BEAN_TYPES) {
            tmp.addAll(info.getObjectNames(IstarNamingStrategy.byTypeStr(BEAN_TYPE)));
        }
        if (info.isWithMonitoring()) {
            tmp.add(ViewStatisticsController.MONITOR_OBJECT);
        }

        final List<ManagableBean> mbs = new ArrayList<>(tmp.size());
        for (ObjectName objectName : tmp) {
            mbs.add(new ManagableBean(objectName));
        }

        result.put("managableBeans", mbs);

        result.put("serverName", serverName);
        result.put("serverNames", connector.getBackendNames());
        result.put("properties", info.getProperties());

        return result;
    }


    public static ObjectName getNameForBean(ConnectionInfo info, String beanName) {
        Set<ObjectName> s = info.getObjectNames(IstarNamingStrategy.byNameStr(beanName));
        return (s.size() != 0) ? s.iterator().next() : null;
    }

    private static ObjectName[] sort(Set<ObjectName> names) {
        final ObjectName[] objectNames = names.toArray(new ObjectName[names.size()]);
        if (objectNames.length == 0) {
            return objectNames;
        }
        Arrays.sort(objectNames, IstarNamingStrategy.COMPARE_BY_NAME);
        return objectNames;
    }


}
