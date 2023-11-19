/*
 * FeedMulticastMux.java
 *
 * Created on 20.11.14 14:14
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.multicast;

import java.io.PrintWriter;

import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.feed.mux.MuxOut;

import static de.marketmaker.istar.feed.multicast.FeedMulticastSender.MULTICAST_PACKET_SIZE;

/**
 * @author oflege
 */
@ManagedResource
public class FeedMulticastMux extends AbstractFeedMulticastReceiver {

    private MuxOut out;

    public FeedMulticastMux() {
        this(MULTICAST_PACKET_SIZE);
    }

    public FeedMulticastMux(int multicastPacketSize) {
        super(multicastPacketSize);
        this.mcBuffer = createBuffer(true);
    }

    public void setOut(MuxOut out) {
        this.out = out;
    }

    @Override
    protected void publish() {
        this.out.append(this.mcBuffer);
    }

    public void appendStatusTo(PrintWriter pw) {
        pw.println("--IN--------");
        pw.append("#Packets received: ").println(getNumPacketsReceived());
        pw.append("#Packets missed  : ").println(getNumPacketsMissed());
        String lastMissAt = getLastMissAt();
        if (lastMissAt != null) {
            pw.append("Last miss at     : ").println(lastMissAt);
        }
    }
}
