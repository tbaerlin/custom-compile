/*
 * ServiceSelectionUtil.java
 *
 * Created on 15.03.2011 13:01:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.impl;

import java.lang.management.ManagementFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collects some helper functions for the selection of specific service providers.
 * It mainly serves the purpose to have all design decisions related to that topic at a single
 * place
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class ServiceProviderSelectionUtil {

    private static final Pattern SERVER_ID = Pattern.compile("(\\d+@[^.]+)(\\.[^.]+)*");

    public static final String APP_ID = shortServerId(ManagementFactory.getRuntimeMXBean().getName());

    public static String shortServerId(String serverId) {
        Matcher m = SERVER_ID.matcher(serverId);
        if (m.matches()) {
            return m.group(1);
        }
        return serverId;
    }

    /**
     * The result of this method is used as routingKey for requests that are directly sent
     * to a specific server
     *
     * @param generalRequestQueue
     * @param serverId
     * @return
     */
    public static String getRoutingKeyFor(String generalRequestQueue, String serverId) {
        return generalRequestQueue + "_" + shortServerId(serverId);
    }
}
