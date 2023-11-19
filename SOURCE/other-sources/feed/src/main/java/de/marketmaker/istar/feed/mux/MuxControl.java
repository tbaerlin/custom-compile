/*
 * MuxControl.java
 *
 * Created on 08.10.12 15:55
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mux;

import de.marketmaker.istar.common.nioframework.Acceptor;
import de.marketmaker.istar.common.spring.ApplicationObjectSupport;
import de.marketmaker.istar.feed.multicast.FeedMulticastMux;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * @author oflege
 */
@ManagedResource
public class MuxControl extends ApplicationObjectSupport implements InitializingBean {
    private final MuxIn muxIn;

    private final FeedMulticastMux multicastMux;

    private final MuxOut muxOut;

    private final MuxBuffer muxBuffer;

    private final List<Acceptor> acceptors = new ArrayList<>();

    private final Map<MuxOut.Client, Long> clients = new IdentityHashMap<>();

    public MuxControl(FeedMulticastMux multicastMux, MuxOut muxOut, Acceptor acceptor) {
        this.muxIn = null;
        this.muxBuffer = null;
        this.multicastMux = multicastMux;
        this.muxOut = muxOut;
        this.acceptors.add(acceptor);
    }

    public MuxControl(MuxIn muxIn, MuxOut muxOut, MuxBuffer muxBuffer) {
        this.muxIn = muxIn;
        this.multicastMux = null;
        this.muxOut = muxOut;
        this.muxBuffer = muxBuffer;
    }

    public MuxControl(MuxIn muxIn, MuxOut muxOut) {
        this(muxIn, muxOut, null);
    }

    public void setAcceptors(Collection<Acceptor> beans) {
        this.acceptors.clear();
        this.acceptors.addAll(beans);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.acceptors.isEmpty()) {
            setAcceptors(getApplicationContext().getBeansOfType(Acceptor.class).values());
        }
    }

    @ManagedAttribute
    public float getUsedBufferPercentage() {
        return (isBufferAvailable() && isBufferEnabled())
                ? ((float)this.muxBuffer.getWritePos() * 100 / this.muxOut.getRingBufferSize()) : -1;
    }

    @ManagedAttribute
    public boolean isBufferAvailable() {
        return this.muxBuffer != null;
    }

    @ManagedAttribute
    public boolean isBufferEnabled() {
        return this.muxBuffer != null && this.muxBuffer.isEnabled();
    }

    @ManagedOperation
    public boolean enableBuffer() {
        return this.muxBuffer != null && this.muxBuffer.enable(true);
    }

    @ManagedOperation
    public boolean disableBuffer() {
        return this.muxBuffer != null && this.muxBuffer.enable(false);
    }

    @ManagedOperation
    public void connectToPrimary() {
        if (this.muxIn == null) {
            throw new UnsupportedOperationException();
        }
        this.muxIn.connectToPrimary();
    }

    @ManagedOperation
    public void connectToBackup() {
        if (this.muxIn == null) {
            throw new UnsupportedOperationException();
        }
        this.muxIn.connectToBackup();
    }

    @ManagedAttribute
    public String getStatus() {
        StringWriter sw = new StringWriter(4096);
        try (PrintWriter pw = new PrintWriter(sw)) {
            if (this.muxIn != null) {
                this.muxIn.appendStatusTo(pw);
                pw.println();
            }
            if (this.multicastMux != null) {
                this.multicastMux.appendStatusTo(pw);
                pw.println();
            }
            appendAcceptorStatusTo(pw);
            pw.println();
            this.muxOut.appendStatus(pw);
        }
        return sw.toString();
    }

    private void appendAcceptorStatusTo(PrintWriter pw) {
        pw.println("--ACCEPT----");
        int i = 0;
        for (Acceptor acceptor : this.acceptors) {
            pw.printf("%d %s%n", ++i, acceptor);
        }
    }

    @ManagedOperation
    public void logStatus() {
        this.logger.info(System.lineSeparator() + getStatus());
    }

    @ManagedOperation
    public void logMulticastSourceStatus() {
        if (this.multicastMux == null) {
            return;
        }
        this.multicastMux.logStatus();
    }

    @ManagedOperation
    public void logClientStatus() {
        this.muxOut.logClientStatus();
    }

    @ManagedOperation
    public void resetClientStats() {
        this.muxOut.resetClientStats();
    }

    @ManagedOperation
    public void disconnectClient(int which) {
        this.muxOut.disconnectClient(which);
    }

    @ManagedOperation
    public void logProblems() {
        final List<MuxOut.Client> current = this.muxOut.getClients();
        synchronized (this.clients) {
            this.clients.keySet().retainAll(current);
            for (MuxOut.Client client : current) {
                final long numLost = client.getNumDiscarded();
                if (numLost == 0) {
                    continue;
                }
                final Long memento = this.clients.get(client);
                long numLostWas = (memento != null) ? memento : 0;
                this.logger.warn("client " + client.getRemoteAddress()
                        + " numLost " + (numLost - numLostWas) + " bytes");
                this.clients.put(client, numLost);
            }
        }
    }

}
