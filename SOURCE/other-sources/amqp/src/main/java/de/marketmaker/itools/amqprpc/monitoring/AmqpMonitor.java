/*
 * ExportMonitor.java
 *
 * Created on 04.09.15 07:55
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.monitoring;

/**
 * @author oflege
 */
public interface AmqpMonitor {
    default void ack(int numBytesOut, int numBytesIn) {};
}
