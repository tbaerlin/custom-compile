/*
 * FeedStats.java
 *
 * Created on 10.06.2010 14:56:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.Lifecycle;

/**
 * Logs how many bytes/messages/packets have been received/sent in the last second
 * by associated components.
 * @author oflege
 */
public class FeedStats implements Lifecycle, BeanNameAware {
    public interface ByteSink {
        public long numBytesReceived();
    }

    public interface ByteSource {
        public long numBytesSent();
    }

    public interface MessageSink {
        public long numMessagesReceived();
    }

    public interface MessageSource {
        public long numMessagesSent();
    }

    public interface PacketSink {
        public long numPacketsReceived();
    }

    public interface PacketSource {
        public long numPacketsSent();
    }

    private final long[] lasts = new long[6];

    private final Logger logger;

    private ByteSink byteSink;

    private ByteSource byteSource;

    private MessageSink messageSink;

    private MessageSource messageSource;

    private PacketSink packetSink;

    private PacketSource packetSource;

    private final StringBuilder sb = new StringBuilder(64);

    private Timer timer;

    private String name;

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    public FeedStats() {
        this.logger = LoggerFactory.getLogger(getClass());
    }

    public FeedStats(String logger) {
        this.logger = LoggerFactory.getLogger(logger);
    }

    public void setByteSink(ByteSink byteSink) {
        this.byteSink = byteSink;
    }

    public void setByteSource(ByteSource byteSource) {
        this.byteSource = byteSource;
    }

    public void setMessageSink(MessageSink messageSink) {
        this.messageSink = messageSink;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setPacketSink(PacketSink packetSink) {
        this.packetSink = packetSink;
    }

    public void setPacketSource(PacketSource packetSource) {
        this.packetSource = packetSource;
    }

    public void start() {
        if (this.byteSink == null && this.byteSource == null
                && this.messageSink == null && this.messageSource == null
                && this.packetSink == null && this.packetSource == null) {
            return;
        }
        this.timer = new Timer((this.name != null) ? this.name : getClass().getSimpleName());
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateStats();
            }
        }, 1000, 1000);
    }

    public void stop() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }

    @Override
    public boolean isRunning() {
        return this.timer != null;
    }

    private void updateStats() {
        this.sb.setLength(0);
        append("bi", 0, this.byteSink != null ? this.byteSink.numBytesReceived() : -1L);
        append("bo", 1, this.byteSource != null ? this.byteSource.numBytesSent() : -1L);
        append("mi", 2, this.messageSink != null ? this.messageSink.numMessagesReceived() : -1L);
        append("mo", 3, this.messageSource != null ? this.messageSource.numMessagesSent() : -1L);
        append("pi", 4, this.packetSink != null ? this.packetSink.numPacketsReceived() : -1L);
        append("po", 5, this.packetSource != null ? this.packetSource.numPacketsSent() : -1L);
        this.logger.info(sb.toString());
    }

    private void append(String key, int i, long value) {
        if (value < 0L) {
            return;
        }
        long diff = value - this.lasts[i];
        if (diff < 0) {
            diff = value;
        }
        if (sb.length() > 0) {
            sb.append(" ");
        }
        sb.append(key).append('=').append(diff);
        this.lasts[i] = value;
    }
}
