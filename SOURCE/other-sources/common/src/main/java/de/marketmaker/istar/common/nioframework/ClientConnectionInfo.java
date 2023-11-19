/*
 * ClientConnectionInfo.java
 *
 * Created on 01.10.14 16:31
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;

import java.net.SocketAddress;

import org.joda.time.DateTime;

/**
* @author oflege
*/
public interface ClientConnectionInfo {
    SocketAddress getLocalAddress();

    SocketAddress getRemoteAddress();

    DateTime getConnectedSince();

    long getNumDiscarded();

    long getNumSent();
}
