/*
 * TickHistoryController.java
 *
 * Created on 22.08.12 14:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.history.HistoryController;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

/**
 * @author zzhao
 */
@ManagedResource(description = "End of day price history controller")
public class EodPriceHistoryController extends HistoryController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EodHistoryArchive archive;

    private Path artifactPath;

    private ActiveMonitor activeMonitor;

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setArtifactPath(String artifactPath) {
        this.artifactPath = Paths.get(artifactPath);
    }

    public void setArchive(EodHistoryArchive archive) {
        this.archive = archive;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Assert.isTrue(null != this.archive, "eod price history archive required");

        if (null != this.activeMonitor && null != this.artifactPath) {
            this.logger.info("<afterPropertiesSet> set up monitoring on {}", this.artifactPath);
            final FileResource resource = new FileResource(this.artifactPath.toFile());
            resource.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    try {
                        final List<String> lines = FileUtils.readLines(artifactPath.toFile(), "UTF-8");
                        if (!CollectionUtils.isEmpty(lines)) {
                            tick(lines.get(0).trim());
                        }
                        else {
                            logger.warn("no content found in {}", artifactPath);
                        }
                    } catch (IOException e) {
                        logger.error("cannot read update file", e);
                    }
                }
            });
            this.activeMonitor.addResource(resource);
            this.logger.info("<afterPropertiesSet> {} monitored", this.artifactPath);
        }
    }

    public static void main(String[] args) throws IOException {
        if (null == args || args.length < 2) {
            usage();
            System.exit(1);
        }

        final boolean recursive = "-r".equals(args[0]);
        final File eodDir;
        final File historyDir;
        String months = null;

        if (recursive) {
            if (args.length < 3) {
                usage();
                System.exit(1);
            }

            eodDir = new File(args[1]);
            historyDir = new File(args[2]);
            if (args.length == 4) {
                months = args[3];
            }
        }
        else {
            eodDir = new File(args[0]);
            historyDir = new File(args[1]);
            if (args.length == 3) {
                months = args[2];
            }
        }

        if (!eodDir.exists()) {
            System.err.println(eodDir.getAbsolutePath() + " not found");
            System.exit(1);
        }

        TimeTaker tt = new TimeTaker();
        final EodPriceHistoryController controller = new EodPriceHistoryController();
        final EodHistoryArchiveDaily archive = new EodHistoryArchiveDaily();
        if (null != months) {
            archive.setMonths(Integer.parseInt(months));
        }
        archive.setWorkDir(eodDir);
        controller.setArchive(archive);

        if (recursive) {
            final File[] folders = historyDir.listFiles(
                    (FileFilter) FileFilterUtils.directoryFileFilter());
            if (null != folders) {
                final TreeSet<File> files = new TreeSet<>(Arrays.asList(folders));
                for (File file : files) {
                    controller.tickIntern(file);
                }
            }
        }
        else {
            controller.tickIntern(historyDir);
        }

        System.out.println("took: " + tt);
    }

    private static void usage() {
        System.err.println("Usage: [-r] eod_dir history_dir [months]");
    }

    @ManagedOperation(description = "invoke to create day tick file ")
    @ManagedOperationParameters(
            @ManagedOperationParameter(
                    name = "path",
                    description = "the folder contains tick data for days")
    )
    public boolean tickAll(String path) {
        final File eodDir = new File(path);
        if (!eodDir.exists()) {
            this.logger.warn("<tick> {} not found", eodDir.getAbsolutePath());
            return false;
        }

        final File[] files = eodDir.listFiles();
        if (null == files) {
            this.logger.warn("<tick> {} empty", eodDir.getAbsolutePath());
            return false;
        }
        TreeSet<File> set = new TreeSet<>(Arrays.asList(files));
        for (File f : set) {
            if (!tick(f.getAbsolutePath())) {
                this.logger.warn("<tickAll> {} not scheduled", f.getAbsolutePath());
            }
        }

        return true;
    }

    protected void tickIntern(final File eod) throws IOException {
        EodUtil.updateWithinLock(this.archive.getUpdateLockFile(), new EodUtil.EodOperation() {
            @Override
            public void process() throws IOException {
                tickWithinLock(eod);
            }
        });
    }

    private void tickWithinLock(File eod) throws IOException {
        if (!eod.isDirectory()) {
            this.logger.error("<tickIntern> End-of-Day data must reside in a folder with date");
        }
        else {
            final File[] files = eod.listFiles((FileFilter) EodTicker.Type.FILTER);
            if (null == files || files.length == 0) {
                this.logger.warn("<tickIntern> no End-of-Day files found");
            }
            else {
                final List<File> list = Arrays.asList(files);
                list.sort(EodTicker.Type.COMPARATOR);
                final TimeTaker tt = new TimeTaker();
                this.archive.begin(Integer.parseInt(eod.getName()));
                for (File file : list) {
                    this.logger.info("<tickIntern> {}", file.getAbsolutePath());
                    try (final EodTickerProtobuf ticker = new EodTickerProtobuf(file)) {
                        while (ticker.hasNext()) {
                            final EodTick tick = ticker.next();
                            if (null != tick) {
                                this.archive.update(tick.getQuote(), tick.getDate(), tick.getValues());
                            }
                        }
                    }
                }
                this.archive.finish();
                /**
                 * Since merge can take a long time. During this time many minor GCs could happen.
                 * This causes the bytes used to import and encode EOD data to be promoted to PS
                 * Old Gen. Those memory can only be collected during a full GC.
                 * DON'T use -XX:+DisableExplicitGC
                 * USE -XX:-DisableExplicitGC
                 */
                System.gc(); // hopefully to trigger a full GC
                this.logger.info("<tickIntern> {} took: {}", eod.getAbsolutePath(), tt);
            }
        }
    }
}
