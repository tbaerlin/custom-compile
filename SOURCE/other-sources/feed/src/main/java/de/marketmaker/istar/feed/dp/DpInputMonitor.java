/*
 * DpInputMonitor.java
 *
 * Created on 26.02.2004 08:59:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.DirectoryResource;

/**
 * Used to monitor the input directory and forward changes to the DpManager.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DpInputMonitor implements InitializingBean, Lifecycle, PropertyChangeListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * writer whose files are being monitored
     */
    private DpManager manager;

    /**
     * directory with input files
     */
    private String inputDirectory;

    /**
     * if not null, only those files in the inputDirectory with this suffix
     * will be considered
     */
    private String inputFileSuffix;

    /**
     * monitors input directory
     */
    private final ActiveMonitor monitor = new ActiveMonitor();

    private Set<File> initialFiles;

    private DirectoryResource resource;

    private boolean monitorSubDirectories = false;

    public void setManager(DpManager manager) {
        this.manager = manager;
    }

    public void setMonitorSubDirectories(boolean monitorSubDirectories) {
        this.monitorSubDirectories = monitorSubDirectories;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public void setInputFileSuffix(String inputFileSuffix) {
        this.inputFileSuffix = inputFileSuffix;
    }

    public void afterPropertiesSet() throws Exception {
        this.resource = new DirectoryResource(this.inputDirectory, getFileFilter(),
                this.monitorSubDirectories);
        this.resource.addPropertyChangeListener(this);
        this.monitor.addResource(this.resource);
        this.initialFiles = this.resource.getFiles();
    }

    @Override
    public boolean isRunning() {
        return this.monitor.isRunning();
    }

    public void start() {
        // the initial files will not be added by the monitor, so do it here:
        this.manager.addFiles(this.initialFiles);
        this.initialFiles = null;

        this.monitor.start();
        this.logger.info("<start> monitor started");
    }

    public void stop() {
        this.monitor.stop();
        this.logger.info("<stop> monitor stopped");
    }

    private FileFilter getFileFilter() {
        return f -> !f.isDirectory()
            && (inputFileSuffix == null || f.getName().endsWith(inputFileSuffix));
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (DirectoryResource.ADDED.equals(evt.getPropertyName())) {
            this.manager.addFiles((Collection) evt.getNewValue());
        } else if (DirectoryResource.MODIFIED.equals(evt.getPropertyName())) {
            this.manager.updateFiles((Collection) evt.getNewValue());
        } else if (DirectoryResource.REMOVED.equals(evt.getPropertyName())) {
            this.manager.removeFiles((Collection) evt.getNewValue());
        } else {
            this.logger.warn("<propertyChange> unknown property: " + evt.getPropertyName());
        }
    }

    File findInputFile(String name) {
        for (File f : this.resource.getFiles()) {
            if (f.getAbsolutePath().endsWith(name)) {
                return f;
            }
        }
        return null;
    }


}
