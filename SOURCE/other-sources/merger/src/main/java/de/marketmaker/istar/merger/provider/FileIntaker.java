/*
 * FileIntaker.java
 *
 * Created on 30.11.2016 12:05
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.common.util.TimeTaker;

/**
 * @author mwilke
 */
public class FileIntaker implements InitializingBean, DisposableBean {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected ActiveMonitor activeMonitor;

    protected File source;

    protected boolean gunzipSource;

    protected File destination;

    protected File destinationTmp;

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public void setGunzipSource(boolean gunzipSource) {
        this.gunzipSource = gunzipSource;
    }

    public void setDestination(File destination) {
        this.destination = destination;
        this.destinationTmp = new File(destination.getAbsolutePath() + ".tmp");
        ;
    }


    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.activeMonitor != null) {
            final FileResource resource = new FileResource(this.source);
            resource.addPropertyChangeListener(evt -> onUpdate());
            this.activeMonitor.addResource(resource);
        }

    }

    private void onUpdate() {
        TimeTaker tt = new TimeTaker();
        try {
            if (!this.gunzipSource) {
                this.logger.info("<onUpdate> copy " + this.source.getAbsolutePath() + " to " + this.destination.getAbsolutePath());
                FileUtils.copyFile(this.source, this.destinationTmp);
                FileUtils.moveFile(this.destinationTmp, this.destination);
                this.logger.info("<onUpdate> copy " + this.source.getAbsolutePath() + " to " + this.destination.getAbsolutePath() + " took " + tt);
            }
            else {
                this.logger.info("<onUpdate> gunzip and copy " + this.source.getAbsolutePath() + " to " + this.destination.getAbsolutePath());
                FileUtil.unGZip(this.source, this.destinationTmp, false);
                FileUtils.moveFile(this.destinationTmp, this.destination);
                this.logger.info("<onUpdate> gunzip and copy " + this.source.getAbsolutePath() + " to " + this.destination.getAbsolutePath() + " took " + tt);
            }

        } catch (Exception e) {
            this.logger.error("<onUpdate> failed", e);

        }


    }
}
