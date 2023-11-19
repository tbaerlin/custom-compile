/*
 * ServoMonitoring.java
 *
 * Created on 03.09.15 08:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.servo;

import org.slf4j.LoggerFactory;

/**
 * Simple class that can be loaded to check whether servo monitoring is available, as it does
 * not have any direct dependency on <tt>com.netfilx.servo</tt> classes.
 * @author oflege
 */
public final class ServoMonitoring {
    public static final boolean MONITORING_AVAILABLE = isAvailable();

    public static MonitorSetup getSetup() {
        return MONITORING_AVAILABLE ? MonitorSetup.INSTANCE : null;
    }

    private static boolean isAvailable() {
        try {
            Class.forName("com.netflix.servo.Metric");
            return MonitorSetup.isActive();
        } catch (ClassNotFoundException e) {
            return false;
        } catch (NoClassDefFoundError e) {
            LoggerFactory.getLogger(ServoMonitoring.class).warn("monitoring is disabled due to " + e);
            return false;
        }
    }
}
