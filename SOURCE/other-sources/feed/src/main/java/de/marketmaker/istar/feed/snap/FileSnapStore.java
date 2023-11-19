/*
 * FileSnapStore.java
 *
 * Created on 30.01.2007 15:51:27
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.snap;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ThroughputLimiter;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.FeedDataRegistry;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;
import de.marketmaker.istar.feed.delay.DelayProvider;

/**
 * Coordinator for storing snap data in a file and restoring snap data from a file.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class FileSnapStore implements InitializingBean, DisposableBean, Lifecycle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File snapFile;

    protected FeedDataRegistry registry;

    private DelayProvider delayProvider;

    private ThroughputLimiter limiter = null;

    private IndexAndOffsetFactory iaoFactory;

    private boolean storeRealtime = true;

    private boolean storeDelayed = true;

    private boolean storeSnapsOnDispose = true;

    private AtomicBoolean storeInProgress = new AtomicBoolean(false);
    
    private volatile boolean started = false;

    /**
     * This filter will be applied on {@link #restore(boolean, boolean)} and prevents
     * data to be restored whose vendorkey does not pass the filter.
     */
    private VendorkeyFilter vendorkeyFilter = VendorkeyFilterFactory.ACCEPT_ALL;

    static final int VERSION = 5;


    public void setDelayProvider(DelayProvider delayProvider) {
        this.delayProvider = delayProvider;
    }

    public void setRestoreThroughput(int limit) {
        this.limiter = new ThroughputLimiter(limit);
    }

    public void setVendorkeyFilter(VendorkeyFilter vendorkeyFilter) {
        this.vendorkeyFilter = vendorkeyFilter;
        this.logger.info("<setVendorkeyFilter> " + vendorkeyFilter);
    }

    public void setSnapFile(File snapFile) {
        this.snapFile = snapFile;
    }

    @Override
    public boolean isRunning() {
        return this.started;
    }

    @Override
    public void start() {
        this.started = true;
    }

    @Override
    public void stop() {
        // empty
    }

    /**
     * @param repository
     * @deprecated use {@link #setRegistry(de.marketmaker.istar.feed.FeedDataRegistry)}
     */
    public void setRepository(FeedDataRepository repository) {
        this.registry = repository;
    }

    public void setRegistry(FeedDataRegistry registry) {
        this.registry = registry;
    }

    public void setIaoFactory(IndexAndOffsetFactory iaoFactory) {
        this.iaoFactory = iaoFactory;
    }

    public void setStoreRealtime(boolean storeRealtime) {
        this.storeRealtime = storeRealtime;
    }

    public void setStoreDelayed(boolean storeDelayed) {
        this.storeDelayed = storeDelayed;
    }

    public void setStoreSnapsOnDispose(boolean storeSnapsOnDispose) {
        this.storeSnapsOnDispose = storeSnapsOnDispose;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.storeRealtime || this.storeDelayed) {
            restore(this.storeRealtime, this.storeDelayed);
        }
    }

    public void destroy() throws Exception {
        if (this.storeSnapsOnDispose && this.started) {
            storeSnaps();
        }
    }

    public void storeSnaps() throws IOException {
        if (!this.storeInProgress.compareAndSet(false, true)) {
            this.logger.warn("<storeSnaps> another store is in progress, returning");
            return;
        }

        try {
            storeSnaps(null, this.storeRealtime, this.storeDelayed);
        } finally {
            this.storeInProgress.set(false);
        }
    }

    /**
     * Invoking this method must be guarded by a successful
     * this.storeInProgress.compareAndSet(false, true)
     */
    private void storeSnaps(String filename, boolean realtime, boolean delayed) throws IOException {
        this.logger.info("<storeSnaps>");
        final TimeTaker tt = new TimeTaker();

        if (filename != null) {
            store(new File(filename), realtime, delayed);
        }
        else {
            store(realtime, delayed);
        }

        this.logger.info("<storeSnaps> took " + tt);
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "filename", description = "filename"),
            @ManagedOperationParameter(name = "realtime", description = "store realtime"),
            @ManagedOperationParameter(name = "delayed", description = "store delayed")
    })
    public String storeSnapsJmx(String filename, boolean realtime, boolean delayed) {
        if (!this.storeInProgress.compareAndSet(false, true)) {
            return "storeSnapsJmx another store is in progress, returning";
        }

        try {
            storeSnaps(filename, realtime, delayed);
        } catch (IOException e) {
            return "storeSnapsJmx failed";
        } finally {
            this.storeInProgress.set(false);
        }

        return "storeSnapsJmx succeeded";
    }


    public void store(boolean storeRealtime, boolean storeDelayed) throws IOException {
        if (this.snapFile == null) {
            this.logger.info("<store> not feasible, no snapFile defined");
            return;
        }
        store(this.snapFile, storeRealtime, storeDelayed);
    }

    private void store(File snapFile, boolean storeRealtime, boolean storeDelayed) throws IOException {
        new FileSnapStoreMethod(this.registry).store(snapFile, storeRealtime, storeDelayed);
    }


    static byte getEncodedRtNt(boolean rt, boolean nt) {
        final int n = (rt ? 1 : 0) + (nt ? 2 : 0);
        return (byte) n;
    }

    static boolean isRt(byte encodedRtNt) {
        return (encodedRtNt & 1) != 0;
    }

    static boolean isNt(byte encodedRtNt) {
        return (encodedRtNt & 2) != 0;
    }

    public void restore(boolean restoreRealtime, boolean restoreDelayed) throws Exception {
        final FileSnapRestoreMethod method = new FileSnapRestoreMethod(snapFile, registry, iaoFactory);
        method.setDelayProvider(this.delayProvider);
        method.setLimiter(this.limiter);
        method.setRestoreRealtime(restoreRealtime);
        method.setRestoreDelayed(restoreDelayed);
        method.setVendorkeyFilter(this.vendorkeyFilter);
        method.restore();
    }
}
