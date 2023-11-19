/*
 * AnalysesProviderWebSim.java
 *
 * Created on 22.03.12 09:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Filter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.analyses.backend.Protos.Analysis.Builder;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Provider;
import de.marketmaker.istar.analyses.frontend.AnalysesMetaRequest;
import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * @author oflege
 */
@ManagedResource
public class AnalysesProviderWebSim extends PassiveAnalysesProvider {

    private static final DateTimeFormatter DAY_DIR_NAME = DateTimeFormat.forPattern("yyyyMMdd");

    private final WebSimAnalysesBuilder builderFactory = new WebSimAnalysesBuilder();

    private File imagesDir;

    @Override
    protected Filter createWithoutRecommendationFilter() throws ParseException {
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.imagesDir = ensureDir(new File(this.archiveDir, "images"));
        if (size() == 0) {
            initFromArchive();
        }
    }

    @ManagedOperation
    public void dumpState(String filename) throws IOException {
        dumpState(new File(filename));
    }

    private void initFromArchive() {
        this.logger.info("<initFromArchive> ...");
        final File[] dayDirs = new File(baseDir, "archive").listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() && f.getName().matches("[0-9]{8}");
            }
        });

        for (File dayDir : dayDirs) {
            addFiles(dayDir);
        }
        this.logger.info("<initFromArchive> added " + size() + " analyses");
    }

    @Override
    protected void addFile(File file) {
        try {
            final Builder builder = this.builderFactory.createBuilderFrom(file);
            // cannot call this after builder has been used to build analysis
            final long timestamp = builder.getAgencyDate();
            addInstruments(builder);

            long id = addAnalysis(builder);
            if (id > 0) {
                moveToArchive(file, getArchiveFile(file, timestamp));
                addImages(id, file);
            }
            else {
                this.logger.warn("<addFile> not added: " + file.getName());
                moveToProblems(file);
            }
        } catch (Exception e) {
            this.logger.warn("<addFile> failed for " + file.getName(), e);
            moveToProblems(file);
        }
    }

    private void addInstruments(Builder builder) {
        for (int i = 0; i < builder.getSymbolCount(); i++) {
            addInstrument(builder, builder.getSymbol(i));
        }
    }

    private File getArchiveFile(File file, long agencyDate) {
        final String dayDir = DAY_DIR_NAME.print(agencyDate);
        return new File(ensureDir(new File(this.archiveDir, dayDir)), file.getName());
    }

    @Override
    protected File getArchiveFile(File f) {
        return new File(this.imagesDir, f.getName());
    }

    private void addImages(long id, File file) throws IOException {
        final String imagePrefix = getBasename(file) + "-";
        final File dir = file.getParentFile().equals(this.incomingDir)
                ? this.incomingDir : this.imagesDir;
        final File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(imagePrefix);
            }
        });
        for (File imageFile : files) {
            addImage(id, imageFile);
        }
    }

    private void addImage(long id, File imageFile) throws IOException {
        addImage(id, getBasename(imageFile), FileCopyUtils.copyToByteArray(imageFile));
        moveToArchive(imageFile);
    }

    private String getBasename(File file) {
        return file.getName().substring(0, file.getName().indexOf('.'));
    }


    @Override
    protected String getId() {
        return "websim";
    }

    @Override
    Map<String, Map<String, String>> doGetMetaData(AnalysesMetaRequest request) {
        HashMap<String, Map<String, String>> result = new HashMap<>();

        HashMap<String, String> categories = new HashMap<>();
        categories.put("ANA_TECN", "ANA_TECN");
        categories.put("ANA_FOND", "ANA_FOND");
        categories.put("FAT_EFF", "FAT_EFF");
        categories.put("STRATEGIE", "STRATEGIE");
        categories.put("TRAD_INTRA", "TRAD_INTRA");
        result.put("categories", categories);

        HashMap<String, String> ratings = new HashMap<>();
        ratings.put("INTERESSANTE", "INTERESSANTE");
        ratings.put("MOLTO INTERESSANTE", "MOLTO INTERESSANTE");
        ratings.put("NEUTRAL","NEUTRAL");
        ratings.put("NON COPERTURA", "NON COPERTURA");
        ratings.put("POCO INTERESSANTE", "POCO INTERESSANTE");
        ratings.put("SOSPESO", "SOSPESO");
        result.put("ratings", ratings);

        return result;
    }

    @Override
    protected Set<Long> searchSummariesWithRating() {
        return super.searchAllSummaries();
    }

    @Override
    public Selector getSelector() {
        return Selector.WEB_SIM_ANALYSES;
    }

    @Override
    public Provider getProvider() {
        return Provider.WEBSIM;
    }

    public static void main(String[] args) throws Exception {
        AnalysesProviderWebSim ws = new AnalysesProviderWebSim();
        ws.setBaseDir(new File("d:/produktion/var/data/analyses/"));
        ws.setActiveMonitor(new ActiveMonitor(true));
        ws.afterPropertiesSet();
    }
}
