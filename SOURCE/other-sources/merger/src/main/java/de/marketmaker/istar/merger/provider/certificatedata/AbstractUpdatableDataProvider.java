/*
 * AbstractUpdatableDataProvider.java
 *
 * Created on 27.05.2009 15:03:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.TimeTaker;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
abstract class AbstractUpdatableDataProvider<T>
        implements InitializingBean, InstrumentBasedUpdatable<T>{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @GuardedBy("this.data")
    private final Map<Long, T> data = new HashMap<>();

    private ActiveMonitor activeMonitor;

    protected File file;

    private Set<Long> withoutUpdate;

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void afterPropertiesSet() throws Exception {
        readData();

        if (this.activeMonitor != null) {
            final FileResource masterDataResource = new FileResource(this.file);
            masterDataResource.addPropertyChangeListener(evt -> readData());
            this.activeMonitor.addResource(masterDataResource);
        }
    }

    void readData() {
        final TimeTaker tt = new TimeTaker();
        try {
            this.withoutUpdate = existingIds();
            read(this.file);

            removeItemsWithoutUpdate();

            this.logger.info("<readData> read " + this.file.getName() + ", took " + tt);
        }
        catch (Exception e) {
            this.logger.error("<readData> failed", e);
        }
    }

    protected abstract void read(File file) throws Exception;

    protected T getData(Long instrumentid) {
        synchronized (this.data) {
            return this.data.get(instrumentid);
        }
    }

    public void addOrReplace(long instrumentid, T t) {
        ackUpdate(instrumentid);
        synchronized (this.data) {
            this.data.put(instrumentid, t);
        }
    }

    protected void removeItemsWithoutUpdate() {
        synchronized (this.data) {
            for (Long anId : this.withoutUpdate) {
                this.data.remove(anId);
            }
        }
        this.logger.info("<removeItemsWithoutUpdate> removed " + this.withoutUpdate.size());
        this.withoutUpdate = null;
    }

    protected Set<Long> existingIds() {
        synchronized (this.data) {
            return new HashSet<>(this.data.keySet());
        }
    }

    protected void ackUpdate(long id) {
        this.withoutUpdate.remove(id);
    }
}
