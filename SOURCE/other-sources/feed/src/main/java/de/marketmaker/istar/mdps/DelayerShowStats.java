/*
 * DelayerShowStats.java
 *
 * Created on 04.03.2010 13:53:59
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps;

import org.springframework.beans.BeansException;

import de.marketmaker.istar.common.nioframework.ClientConnectionInfo;
import de.marketmaker.istar.feed.delay.DataChunkPool;
import de.marketmaker.istar.common.statistics.ResetStatistics;
import de.marketmaker.istar.feed.connect.FeedInputSource;
import de.marketmaker.istar.feed.delay.Delayer;
import de.marketmaker.istar.feed.mdps.MdpsFeedConnector;
import de.marketmaker.istar.feed.mdps.MdpsFeedParser;
import de.marketmaker.istar.feed.mdps.MdpsRecordProvider;
import de.marketmaker.istar.feed.mux.MuxOut;

/**
 * @author oflege
 */
public class DelayerShowStats extends AbstractShowStats {
    private MdpsFeedConnector connector;

    private MuxOut server;

    private Delayer delayer;

    private DataChunkPool pool;

    private MdpsFeedParser parser;

    private ResetStatistics resetStatistics;

    @Override
    protected void initApplicationContext() throws BeansException {
        super.initApplicationContext();

        this.connector = getBean(MdpsFeedConnector.class);
        this.parser = getBean(MdpsFeedParser.class);
        this.server = getBean(MuxOut.class);
        this.delayer = getBean(Delayer.class);
        this.pool = delayer.getDataChunkPool();
        this.resetStatistics = getBean(ResetStatistics.class);
    }


    public String getStats() {
        final OutputBuilder builder = createBuilder();
        if (this.resetStatistics != null && this.resetStatistics.getResetAt() != null) {
            builder.print("Reset statistics at : ").print(this.resetStatistics.getResetAt()).println().println();
        }

        boolean connected = false;
        builder.print("primary IN connection: " + this.connector.getPrimaryInputSource().getAddress());
        if (this.connector.getPrimaryInputSource() == this.connector.getCurrentInputSource()) {
            builder.print(", connected since ").print(this.connector.getConnectedSince());
            connected = true;
        }
        builder.println();

        builder.print("backup  IN connection: " + this.connector.getBackupInputSource().getAddress());
        if (this.connector.getBackupInputSource() == this.connector.getCurrentInputSource()) {
            builder.print(", connected since ").print(this.connector.getConnectedSince());
            connected = true;
        }
        builder.println();
        if (!connected) {
            builder.println("!!! NOT CONNECTED !!!");
        }

        builder.println().line(80).println();
        builder.printf("%-30s%18s%18s%8s%n", "IN CONNECTIONS(S)", "BYTES RECVD", "MSGS RECVD", "FMT");
        builder.line(80).println();
        if (this.connector != null) {
            final long numBytes = this.connector.getNumBytesProcessed();
            final long numMsgs = getNumReceived();
            builder.printf("%-30s%18d%18d%8s%n", getSource(), numBytes, numMsgs,
                    "PV" + this.connector.getProtocolVersion());
        }
        builder.println();
        
        builder.line(80).println();
        builder.printf("%-30s%18s%18s%8s%n", "OUT CONNECTION(S)", "BYTES SENT", "BYTES DROPPED", "FMT");
        builder.line(80).println();
        if (this.server != null) {
            for (ClientConnectionInfo info : this.server.getClientInfo()) {
                builder.printf("%-30s%18d%18d%8s%n", info.getRemoteAddress(), info.getNumSent(),
                        info.getNumDiscarded(), "PV" + this.connector.getProtocolVersion());
            }
        }
        builder.println().println();

        builder.printf("%-25s:%10s%n", "Num Delayed Records", delayer.getNumDelayed());
        builder.printf("%-25s:%10s%n", "Dropped Delayed Records", delayer.getNumDiscarded());
        builder.printf("%-25s:%10s%%%n", "Delay Memory Used", pool.getUsedChunksPct());

        return builder.build();
    }

    private String getSource() {
        final FeedInputSource inputSource = this.connector.getCurrentInputSource();
        if (inputSource == null) {
            return "--NOT-CONNECTED--";
        }
        return inputSource.getAddress().getAddress() + ":" + inputSource.getAddress().getPort();
    }

    private long getNumReceived() {
        if (this.parser != null) {
            return this.parser.numRecordsParsed();
        }
        return -1L;
    }

    public static void main(String[] args) {
        System.out.println(new DelayerShowStats().getStats());
    }
}
