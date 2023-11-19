/*
 * RatioUpdateableMulticastSender.java
 *
 * Created on 31.07.2006 18:55:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios;

import java.nio.ByteBuffer;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.ClassUtils;

import de.marketmaker.istar.feed.connect.OrderedMulticastReceiver;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class RatioUpdateableMulticastReceiver extends OrderedMulticastReceiver
        implements Lifecycle, Runnable, BeanNameAware {

    private RatioUpdateable ratioUpdateable;
    
    private volatile boolean stopped = false;

    private String name = ClassUtils.getShortName(getClass());

    private Thread thread;

    public void setBeanName(String s) {
        this.name = s;
    }

    public void setRatioUpdateable(RatioUpdateable ratioUpdateable) {
        this.ratioUpdateable = ratioUpdateable;
    }

    @Override
    public boolean isRunning() {
        return this.thread != null && this.thread.isAlive();
    }

    public void start() {
        this.thread = new Thread(this, this.name);
        this.thread.setDaemon(true);
        this.thread.start();
    }

    public void stop() {
        this.stopped = true;
        try {
            this.thread.join(10000);
        } catch (InterruptedException e) {
            this.logger.error("<stop> interrupted?!");
            Thread.currentThread().interrupt();
        }
        if (this.thread.isAlive()) {
            this.logger.warn("<stop> failed to join receive thread");
        }
    }

    public void run() {
        while(!this.stopped) {
            try {
                update(receive());
            } catch (Throwable t) {
                this.logger.error("<run> failed", t);
            }
        }
    }

    void update(ByteBuffer bb) {
        final int totalLimit = bb.limit();
        while (bb.hasRemaining()) {
            final int len = bb.getInt();
            bb.limit(bb.position() + len);
            this.ratioUpdateable.update(bb);
            bb.position(bb.limit());
            bb.limit(totalLimit);
        }
    }
}
