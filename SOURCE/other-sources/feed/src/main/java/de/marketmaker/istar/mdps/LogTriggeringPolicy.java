/*
 * LogTriggeringPolicy.java
 *
 * Created on 29.06.2010 13:35:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * A problem with SizeBasedTriggeringPolicy is that it won't trigger rolling on startup
 * which is exactly what this subclass adds.
 * Another benefit is that rolling can be triggered by JMX command. Since logback will create
 * its own instance of this class and not register it with the MBeanServer, we create another
 * instance in the spring context which is only used as JMX access point. Both instances
 * communicate using the static TRIGGER variable, which is fine as long as only one instance
 * of the class is used by logback. If more are to be used, some more programming would be needed.
 * 
 * @author oflege
 */
@ManagedResource
public class LogTriggeringPolicy<E> extends SizeBasedTriggeringPolicy<E> {
    private static final AtomicBoolean TRIGGER = new AtomicBoolean(true);

    @Override
    public boolean isTriggeringEvent(File activeFile, E event) {
        return TRIGGER.compareAndSet(true, false) || super.isTriggeringEvent(activeFile, event);
    }

    @ManagedOperation
    public void triggerLogRotation() {
        TRIGGER.set(true);
    }

}
