/*
 * RatioUpdateableMulticastSender.java
 *
 * Created on 31.07.2006 18:55:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.mcast.MulticastSender;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class RatioUpdateableMulticastSender extends RatioUpdateableSender {
    private MulticastSender sender;

    public void setMaxMulticastsPerSecond(int maxMulticastsPerSecond) {
        setMaxPacketsPerSecond(maxMulticastsPerSecond);
    }

    public void setSender(MulticastSender sender) {
        this.sender = sender;
    }

    @Override
    protected ByteBuffer createBuffer() {
        return ByteBuffer.allocate(MulticastSender.MULTICAST_PACKET_SIZE);        
    }

    @Override
    protected boolean isPacketWithSequenceNumber() {
        return true;
    }

    @Override
    protected void sendBuffer(ByteBuffer bb) throws IOException {
        this.sender.sendPacket(bb.array(), 0, bb.remaining());
    }

    @ManagedAttribute
    public boolean isPaused() {
        return super.isPaused();
    }

    @ManagedAttribute
    public void setPaused(boolean paused) {
        super.setPaused(paused);
    }

    @ManagedAttribute
    public void doPause() {
        super.setPaused(true);
    }

    @ManagedAttribute
    public void doResume() {
        super.setPaused(false);
    }
}
