/*
 * TickHistoryProviderImpl.java
 *
 * Created on 23.08.12 15:27
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;

/**
 * @author zzhao
 */
public abstract class HistoryProviderBase<G extends HistoryGatherer> implements InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected G gatherer;

    private File workDir;

    private ActiveMonitor monitor;

    public void setGatherer(G gatherer) {
        this.gatherer = gatherer;
    }

    public void setMonitor(ActiveMonitor monitor) {
        this.monitor = monitor;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.isTrue(this.gatherer != null, "tick history reader required");
        final boolean canProvideService = getWorkingStatus();
        if (!canProvideService) {
            this.logger.error("<afterPropertiesSet> no work dir set, cannot provide service");
        }
        else {
            updateGatherer();
            if (null != this.monitor) {
                FileResource resource =
                        new FileResource(HistoryUtil.getWorkingUnitsFile(this.workDir));
                resource.addPropertyChangeListener(evt -> updateGatherer());
                this.monitor.addResource(resource);
            }
        }
    }

    private boolean getWorkingStatus() throws IOException {
        return this.workDir != null && this.workDir.exists() && this.workDir.canRead();
    }

    private void updateGatherer() {
        try {
            final EnumSet<HistoryUnit> units = HistoryUtil.loadHistoryUnits(this.workDir);
            this.gatherer.updateUnits(this.workDir, units);
        } catch (Exception e) {
            this.logger.error("<run> failed updating with latest month and year file", e);
        }
    }
}
