/*
 * MonitorSupport.java
 *
 * Created on 04.09.15 07:57
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.monitoring;

import de.marketmaker.istar.common.servo.MonitorSetup;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress;

/**
 * @author oflege
 */
public class MonitorSupport {
    public static AmqpMonitor getExportMonitor(Class<?> clazz,
            AmqpRpcAddress address) {
        if (MonitorSetup.isActive()) {
            return AmqpMonitors.INSTANCE.getMonitor(clazz, address);
        }
        return new AmqpMonitor() { }; // null impl
    }
}
