/*
 * StaticReader.java
 *
 * Created on 25.10.2005 20:32:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.frontend.BackendUpdateLogger;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class StaticReader implements InitializingBean, DisposableBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static String ADDITIONAL_FILE_PREFIX = "istar-ratios-";

    private File[] additionalFiles;

    private File baseDir;

    private final List<InstrumentTypeEnum> workingTypes = new ArrayList<>();

    private boolean initialUpdate = true;

    private StaticDataCallback frontend;

    private StaticDataCallback backend;

    private ActiveMonitor activeMonitor;

    private final ExecutorService es = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, StaticReader.class.getSimpleName());
        t.setDaemon(true);
        return t;
    });

    private volatile boolean stop;

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setAdditionalFiles(File[] additionalFiles) {
        this.additionalFiles = ArraysUtil.copyOf(additionalFiles);
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setTypes(String... types) {
        for (final String type : types) {
            this.workingTypes.add(InstrumentTypeEnum.valueOf(type));
        }
        this.logger.info("<setTypes> " + this.workingTypes);
    }

    public void setFrontend(StaticDataCallback frontend) {
        this.frontend = frontend;
    }

    public void setBackend(StaticDataCallback backend) {
        this.backend = backend;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.activeMonitor == null) {
            return;
        }
        for (final InstrumentTypeEnum type : this.workingTypes) {
            FileResource resource = new FileResource(getFile(type, false));
            resource.addPropertyChangeListener(evt -> triggerReadFile(type, ((FileResource) evt.getSource()).getFile()));
            this.activeMonitor.addResource(resource);
        }
    }

    @Override
    public void destroy() throws Exception {
        this.stop = true;
        this.es.shutdown();
        if (!this.es.awaitTermination(60, TimeUnit.SECONDS)) {
            this.logger.warn("<destroy> awaitTermination failed");
        }
    }

    @ManagedOperation
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "update", description = "true sends only _inc data")
    })
    public void afterInstrumentUpdate(final boolean update) {
        if (this.initialUpdate) {
            // synchronous execution on startup, so everything has been read when we return
            runAfterInstrumentUpdate(update);
        }
        else {
            // after first time this runs in a single threaded scheduler
            this.es.submit((Runnable) () -> runAfterInstrumentUpdate(update));
            this.logger.info("<afterInstrumentUpdate> scheduled run, update=" + update);
        }
    }

    private void runAfterInstrumentUpdate(boolean update) {
        final StaticDataCallback[] callbacks = this.initialUpdate
                ? new StaticDataCallback[] { this.backend }
                : new StaticDataCallback[] { this.frontend, this.backend };

        // this is a mess, as there will always be inconsistencies between the instrument index
        // and the type-specific files with additional data. We send data from all complete files
        // after a full instrument update and data from incremental files after each partial update.
        // In addition, complete files will also be monitored and sent as soon as they change.
        for (final InstrumentTypeEnum type : this.workingTypes) {
            final File file = getFile(type, false);
            final File incFile = getFile(type, true);
            if (!update) {
                // after full update, read all files
                readFile(type, file, false, callbacks);
            }
            if (isMoreRecentThan(incFile, file)) {
                readFile(type, incFile, false, callbacks);
            }
        }

        if (!update && !this.initialUpdate) {
            for (final File file : this.additionalFiles) {
                final InstrumentTypeEnum type = inferInstrumentType(file);
                readFile(type, file, true, this.frontend);
            }
        }
        this.initialUpdate = false;
    }

    private boolean isMoreRecentThan(File file1, File file2) {
        return file1.canRead() && file2.canRead() && file1.lastModified() > file2.lastModified();
    }

    private InstrumentTypeEnum inferInstrumentType(File file) {
        final int end = file.getName().indexOf("-", ADDITIONAL_FILE_PREFIX.length() + 1);
        final String name = file.getName().substring(ADDITIONAL_FILE_PREFIX.length(), end);
        return InstrumentTypeEnum.valueOf(name.toUpperCase());
    }

    private File getFile(InstrumentTypeEnum type, boolean inc) {
        return new File(this.baseDir, "istar-ratios-" + type.name().toLowerCase()
                + (inc ? "_inc" : "") + ".xml.gz");
    }

    private void triggerReadFile(final InstrumentTypeEnum type, final File file) {
        this.es.submit((Runnable) () -> readFile(type, file, false, frontend, backend));
    }

    private void readFile(InstrumentTypeEnum type, File file, boolean additionalFile,
            StaticDataCallback... listener) {
        if (this.stop) {
            this.logger.info("<readFile> service was stopped, ignoring " + file.getName());
            return;
        }
        if (!file.canRead()) {
            this.logger.info("<readFile> no such file " + file.getAbsolutePath());
            return;
        }
        this.logger.info("<readFile> starting to read " + file.getAbsolutePath());
        final TimeTaker tt = new TimeTaker();
        try {
            final XmlFieldsReader reader = new XmlFieldsReader(type, !additionalFile);
            final GZIPInputStream is = new GZIPInputStream(new FileInputStream(file));
            reader.read(is, listener);
        } catch (Exception e) {
            this.logger.error("<readFile> failed", e);
        }
        this.logger.info("<readFile> finished, took " + tt);
    }

    public static void main(String[] args) throws Exception {
        final StaticReader sr = new StaticReader();
        sr.setBaseDir(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/ratios-backend"));
//        sr.setAdditionalFiles(new File[]{new File(LocalConfigProvider.getProductionBaseDir(), "var/data/istar-ratios-stk-convensys.xml.gz")});
        final ActiveMonitor activeMonitor = new ActiveMonitor();
        activeMonitor.setFrequency(3000);
        activeMonitor.start();
        sr.setActiveMonitor(activeMonitor);
        sr.setTypes("CER");

        //final RatioUpdateableMulticastSender sender = new RatioUpdateableMulticastSender();
        //final MulticastSenderImpl msender = new MulticastSenderImpl();
        //msender.setGroupname("224.0.0.0");
        //msender.setPort(62626);
        //msender.initialize();
        //sender.setSender(msender);
        //sender.afterPropertiesSet();
        final BackendUpdateLogger logger = new BackendUpdateLogger();
        logger.setIids(Collections.singletonList(29786L));
        logger.afterPropertiesSet();
        final StaticMessageBuilder builder = new StaticMessageBuilder();
        builder.setFrontend(logger);
        sr.setBackend(builder);
        sr.setFrontend(builder);

        sr.afterPropertiesSet();
        sr.afterInstrumentUpdate(false);
        System.out.println("***************************************");
        sr.afterInstrumentUpdate(false);
        Thread.sleep(Long.MAX_VALUE);
    }
}
