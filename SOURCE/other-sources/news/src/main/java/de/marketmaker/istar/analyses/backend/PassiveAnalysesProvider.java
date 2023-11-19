/*
 * PassiveAnalysisProvider.java
 *
 * Created on 24.04.12 13:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.DirectoryResource;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.news.backend.NewsSymbolIdentifier;

/**
 * An AnalysesProvider that pulls new analyses by monitoring an "incoming" directory
 * @author oflege
 */
abstract class PassiveAnalysesProvider extends AnalysesProvider {
    protected static final FileFilter FILE_FILTER = pathname -> pathname.isFile()
            && pathname.getName().endsWith(".xml");

    private ActiveMonitor activeMonitor;

    protected File archiveDir;

    private File problemDir;

    private NewsSymbolIdentifier symbolIdentifier;

    protected File incomingDir;

    protected abstract void addFile(File file);

    private void addFiles(Set<File> files) {
        for (File file : files) {
            addFile(file);
        }
    }

    public void setSymbolIdentifier(NewsSymbolIdentifier symbolIdentifier) {
        this.symbolIdentifier = symbolIdentifier;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        this.archiveDir = ensureDir(new File(this.baseDir, "archive"));
        this.problemDir = ensureDir(new File(this.baseDir, "problems"));
        this.incomingDir = ensureDir(new File(this.baseDir, "incoming"));

        final DirectoryResource dr = new DirectoryResource(incomingDir.getAbsolutePath(), FILE_FILTER);
        this.activeMonitor.addResource(dr);
        dr.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals(DirectoryResource.ADDED)) {
                addFiles((Set<File>) evt.getNewValue());
            }
        });

        addFiles(this.incomingDir);
    }

    protected void addFiles(final File dir) {
        final File[] files = dir.listFiles(FILE_FILTER);
        if (files != null && files.length > 0) {
            addFiles(new HashSet<>(Arrays.asList(files)));
        }
    }

    protected void moveToArchive(File f) {
        moveToArchive(f, getArchiveFile(f));
    }

    protected void moveToArchive(File f, File dest) {
        if (f.equals(dest)) {
            return;
        }
        if (!f.renameTo(dest)) {
            this.logger.error("<moveToArchive> failed for " + f.getAbsolutePath());
        }
        else if (this.logger.isDebugEnabled()) {
            this.logger.debug("<moveToArchive> " + dest.getAbsolutePath());
        }
    }

    protected File getArchiveFile(File f) {
        return new File(this.archiveDir, f.getName());
    }

    protected void moveToProblems(File f) {
        if (!f.renameTo(new File(this.problemDir, f.getName()))) {
            this.logger.error("<moveToProblems> failed for " + f.getAbsolutePath());
        }
    }

    protected Instrument identify(String isin) {
        final Map<Long, Instrument> map = this.symbolIdentifier.identify(Collections.singleton(isin));
        if (map.isEmpty()) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<identify> unknown isin: '" + isin + "'");
            }
            return null;
        }
        return map.values().iterator().next();
    }

    protected boolean addInstrument(Protos.Analysis.Builder builder, String isin) {
        final Instrument instrument = identify(isin);
        if (instrument == null) {
            return false;
        }
        builder.addIid(instrument.getId());
        if (!builder.hasCompanyName()) {
            builder.setCompanyName(instrument.getName());
        }
        final String country = getCountry(instrument);
        if (country != null) {
            builder.addCountry(country);
        }
        final String sector = getSector(instrument);
        if (sector != null) {
            builder.addBranch(sector);
        }
        return true;
    }

    private String getCountry(Instrument instrument) {
        return instrument.getCountry() != null ? instrument.getCountry().getName() : null;
    }

    private String getSector(Instrument instrument) {
        if (instrument.getSector() != null
                && !"Unknown".equals(instrument.getSector().getName())) {
            return instrument.getSector().getName();
        }
        return null;
    }
}
