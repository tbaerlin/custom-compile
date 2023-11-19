/*
 * MulticastReceiver.java
 *
 * Created on 14.11.2005 10:34:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mcast;

import java.net.DatagramPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface MulticastReceiver {
    /**
     * Receive the next available data in packet. This method will block until the next data
     * is available.
     * @param packet
     * @throws IOException
     */
    void receive(DatagramPacket packet) throws IOException;
}
