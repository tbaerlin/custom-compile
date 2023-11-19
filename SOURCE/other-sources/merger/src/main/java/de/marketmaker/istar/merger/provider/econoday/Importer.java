/*
 * Importer.java
 *
 * Created on 16.03.12 11:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.DirectoryResource;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;

/**
 * @author zzhao
 */
public class Importer implements InitializingBean {

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss");

    private static final Comparator<File> COMP_FILE
            = (o1, o2) -> Long.compare(o1.lastModified(), o2.lastModified());

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File inDir;

    private File backupDir;

    private ActiveMonitor monitor;

    private EventReleaseImporter releaseImporter;

    public void setBackupDir(String path) {
        this.backupDir = new File(path);
    }

    public void setInDir(String path) {
        this.inDir = new File(path);
    }

    public void setMonitor(ActiveMonitor monitor) {
        this.monitor = monitor;
    }

    public void setReleaseImporter(EventReleaseImporter releaseImporter) {
        this.releaseImporter = releaseImporter;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.isTrue(this.inDir.exists() && this.inDir.canRead() && this.inDir.isDirectory(),
                "invalid in dir '" + this.inDir.getAbsolutePath() + "'");
        Assert.isTrue(this.backupDir.exists() && this.backupDir.canWrite() && this.backupDir.isDirectory(),
                "invalid in dir '" + this.backupDir.getAbsolutePath() + "'");
        Assert.notNull(this.monitor, "monitor for input dir required");
        Assert.notNull(this.releaseImporter, "event release importer required");

        final DirectoryResource res = new DirectoryResource(this.inDir.getAbsolutePath());
        res.addPropertyChangeListener(evt -> {
            final String pn = evt.getPropertyName();
            //noinspection unchecked
            final Set<File> files = (Set<File>) evt.getNewValue();
            if (DirectoryResource.ADDED.equals(pn) || DirectoryResource.MODIFIED.equals(pn)) {
                importEvents(files);
            }
        });
        this.monitor.addResource(res);
    }

    private void importEvents(Set<File> files) {
        final TimeTaker timeTaker = new TimeTaker();
        this.logger.info("<importEvents> start importing " + files.size() + " event release files");
        final ArrayList<File> sortedFiles = new ArrayList<>(files);
        if (sortedFiles.size() > 1) {
            sortedFiles.sort(COMP_FILE);
        }
        for (File file : sortedFiles) {
            if (file.getName().startsWith(".")) {
                this.logger.warn("<importEvents> ignoring hidden file: " + file.getAbsolutePath());
                continue;
            }
            this.logger.info("<importEvents> for file: " + file.getAbsolutePath());

            final TimeTaker tt = new TimeTaker();
            int count = -1;
            try (InputStream is = getInputStream(file)) {
                count = this.releaseImporter.execute(is);
            } catch (Exception e) {
                this.logger.error("<importEvents> failed importing " + file.getAbsolutePath(), e);
            }
            final File dst = moveToBackDir(file);
            this.logger.info("<importEvents> imported " + count + " events, took: " + tt
                + ", stored input file as " + dst.getName());
        }
        this.logger.info("<importEvents> importing " + files.size()
                + " event release files finished in: " + timeTaker);
    }

    private InputStream getInputStream(File file) throws IOException {
        final FileInputStream fis = new FileInputStream(file);
        return (file.getName().endsWith(".gz")) ? new GZIPInputStream(fis) : fis;
    }

    private File moveToBackDir(File file) {
        final String suffix = file.getName().substring(file.getName().indexOf("."));
        final String name = DTF.print(new DateTime());
        File backup = new File(this.backupDir, name + suffix);
        for (int i = 1; backup.exists(); i++) {
            backup = new File(this.backupDir, name + "_" + i + suffix);
        }
        if (!file.renameTo(backup)) {
            this.logger.error("<moveToBackDir> cannot rename "
                    + file.getAbsolutePath() + " to " + backup.getAbsolutePath());
        }
        return backup;
    }
}
