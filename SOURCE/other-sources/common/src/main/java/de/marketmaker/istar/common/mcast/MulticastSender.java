/*
 * MulticastSender.java
 *
 * Created on 14.11.2005 11:32:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mcast;

import java.io.IOException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface MulticastSender {
    /**
     * Size of a packet that can be sent without fragmentation
     */
    public static final int MULTICAST_PACKET_SIZE =
            Integer.getInteger("istar.multicast.packetsize", 1250);

    /**
     * Sends the contents of the buf from offset to (offset + length).
     * @throws IOException if sending fails.
     */
    void sendPacket(byte[] buf, int offset, int length) throws IOException;
}
