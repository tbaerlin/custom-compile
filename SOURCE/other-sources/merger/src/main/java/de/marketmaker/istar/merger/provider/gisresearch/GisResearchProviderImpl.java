/*
 * DzResearchProviderImpl.java
 *
 * Created on 25.03.14 14:09
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.FacetedSearchResult;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static de.marketmaker.istar.merger.provider.gisresearch.GisResearchIndexConstants.FIELD_ISSUER;
import static de.marketmaker.istar.merger.provider.gisresearch.GisResearchIndexer.GIS_RESEARCH_DOCS;
import static de.marketmaker.istar.merger.web.easytrade.block.GisResearchFinder.*;

/**
 * @author oflege
 */
@ManagedResource
public class GisResearchProviderImpl implements GisResearchProvider, InitializingBean,
        DisposableBean {

    private static final String ACTIVEDIR_NAME = "activedir.lck";

    protected final Logger logger = LoggerFactory.getLogger(getClass());


    private File baseDir;

    private File incomingDir;

    private File archiveDir;

    private boolean work0Active = false;

    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock(true);

    private final ReentrantReadWriteLock.ReadLock readLock = this.reentrantReadWriteLock.readLock();

    private final ReentrantReadWriteLock.WriteLock writeLock = this.reentrantReadWriteLock.writeLock();

    private Map<String, GisResearchDoc> currentDocs = new HashMap<>();

    private GisResearchSearcher searcher;

    public void setBaseDir(File baseDir) {
        this.baseDir = FileUtil.ensureDir(baseDir);
    }

    @Override
    public GisResearchResponse search(GisResearchRequest r) {
        this.readLock.lock();
        try {
            return doSearch(r);
        } finally {
            this.readLock.unlock();
        }
    }

    private GisResearchResponse doSearch(GisResearchRequest r) {
        Set<String> ids;
        try {
            ids = this.searcher.search(r);
        } catch (IOException e) {
            this.logger.error("<search> failed", e);
            return new GisResearchResponse();
        }

        return createResult(r, ids);
    }

    private GisResearchResponse createResult(GisResearchRequest r, Set<String> ids) {
        final List<GisResearchDoc> docs = getGisResearchDocs(ids);
        docs.sort(getComparator(r));

        final List<GisResearchDoc> items = new ArrayList<>(r.getCount());
        int n = Math.min(r.getOffset() + r.getCount(), docs.size());
        for (int i = r.getOffset(); i < n; i++) {
            items.add(docs.get(i));
        }
        return new GisResearchResponse(items, ids.size(), countMetaData(docs, r.getLocale()));
    }

    private Comparator<GisResearchDoc> getComparator(GisResearchRequest r) {
        final Comparator<GisResearchDoc> c = getAscComparator(r);
        return r.isAscending() ? c : Collections.reverseOrder(c);
    }

    private Comparator<GisResearchDoc> getAscComparator(GisResearchRequest r) {
        if (SORT_BY_RECOMMENDATION.equals(r.getSortBy())) {
            return GisResearchDoc.BY_RECOMMENDATION;
        }
        if (SORT_BY_SECTOR.equals(r.getSortBy())) {
            return GisResearchDoc.BY_SECTOR;
        }
        if (SORT_BY_TITLE.equals(r.getSortBy())) {
            return GisResearchDoc.BY_TITLE;
        }
        if (SORT_BY_ASSET_CLASS.equals(r.getSortBy())) {
            return GisResearchDoc.BY_ASSET_CLASS;
        }
        return GisResearchDoc.BY_DATE;
    }

    private FacetedSearchResult countMetaData(List<GisResearchDoc> docs, Locale locale) {
        final FacetedSearchResult.Builder b = FacetedSearchResult.createBuilder()
                .withEnum("type")
                .withEnum("documentType")
                .withEnum("sector")
                .withEnum("issuer")
                .withEnum("country")
                .withEnum("recommendation");


        for (GisResearchDoc doc : docs) {
            DocumentType documentType = doc.getDocumentType();
            b.addValue("documentType", documentType.name(), documentType.getDescription());

            String assetClass = documentType.getAssetClass();
            if (assetClass != null) {
                b.addValue("type", assetClass);
            }

            if (doc.getSector() != null) {
                b.addValue("sector", doc.getSector());
            }

            for (GisResearchIssuer issuer : doc.getIssuers()) {
                b.addValue("issuer", issuer.getNumber(), issuer.getName());
            }

            for (String country : doc.getCountries()) {
                b.addValue("country", country, new Locale("", country).getDisplayCountry(locale));
            }

            if (doc.getRecommendation() != null) {
                b.addValue("recommendation", doc.getRecommendation(), doc.getRecommendationText());
            }
        }

        return b.build();
    }

    private List<GisResearchDoc> getGisResearchDocs(Set<String> ids) {
        List<GisResearchDoc> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            final GisResearchDoc cf = currentDocs.get(id);
            if (cf == null) {
                this.logger.warn("<search> result w/o doc: " + id);
                continue;
            }
            result.add(cf);
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.incomingDir = FileUtil.ensureDir(new File(this.baseDir, "incoming"));
        this.archiveDir = FileUtil.ensureDir(new File(this.baseDir, "archive"));

        FileUtil.ensureDir(new File(this.baseDir, "work0"));
        FileUtil.ensureDir(new File(this.baseDir, "work1"));

        this.work0Active = getCheckFile(true).canRead();
        boolean work1Active = getCheckFile(false).canRead();

        if (this.work0Active == work1Active) {
            throw new IllegalStateException("none or two active work directories, manual action necessary");
        }

        this.searcher = new GisResearchSearcher(getIndexDir());
        this.currentDocs = readDocs(getWorkDir(this.work0Active));
    }

    @Override
    public void destroy() throws Exception {
        this.searcher.close();
    }

    @ManagedOperation
    public void checkIncoming() {
        File[] files = this.incomingDir.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".zip"));
        if (files.length > 0) {
            Arrays.sort(files, FileUtil.COMPARE_BY_ABSOLUTE_PATH);
            processIncoming(files);
            archiveIncoming(files);
        }
    }

    private void processIncoming(File[] files) {
        try {
            unzipIncoming(files[files.length - 1]);
        } catch (Throwable t) {
            this.logger.error("<processIncoming> failed", t);
        }
    }

    private void archiveIncoming(File[] files) {
        for (File file : files) {
            if (!file.renameTo(new File(this.archiveDir, file.getName()))) {
                this.logger.warn("<archiveIncoming> failed for " + file.getName());
                if (!file.delete()) {
                    this.logger.warn("<archiveIncoming> failed to delete " + file.getAbsolutePath());
                }
            }
        }
    }

    private void unzipIncoming(File zip) throws Exception {
        File nonWorkDir = getWorkDir(!this.work0Active);
        FileUtils.cleanDirectory(nonWorkDir);
        this.logger.info("<unzipIncoming> to " + nonWorkDir.getName() + "...");
        FileUtil.unzipToDir(zip, nonWorkDir);
        this.logger.info("<unzipIncoming> done.");

        Map<String, GisResearchDoc> newDocs = readDocs(nonWorkDir);
        GisResearchSearcher newSearcher = new GisResearchSearcher(getIndexDir(!this.work0Active));

        this.work0Active = !this.work0Active;
        switchCheckFile();

        this.writeLock.lock();
        try (GisResearchSearcher ignored = this.searcher) {
            this.searcher = newSearcher;
            this.currentDocs = newDocs;
        } finally {
            this.writeLock.unlock();
        }
    }

    private Map<String, GisResearchDoc> readDocs(File dir) throws Exception {
        File docsFile = new File(dir, GIS_RESEARCH_DOCS);
        HashMap<String, GisResearchDoc> result = new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(docsFile))) {
            int num = ois.readInt();
            for (int i = 0; i < num; i++) {
                GisResearchDoc doc = (GisResearchDoc) ois.readObject();
                result.put(doc.getResearchId(), doc);
            }
        }
        this.logger.info("<readDocs> read " + result.size() + " docs");
        return result;
    }

    private File getDataDir() {
        return getDataDir(this.work0Active);
    }

    private File getDataDir(boolean work0) {
        return new File(getWorkDir(work0), "data");
    }

    private File getIndexDir() {
        return getIndexDir(this.work0Active);
    }

    private File getIndexDir(boolean work0) {
        return new File(getWorkDir(work0), "index");
    }

    private File getWorkDir(boolean work0) {
        return new File(this.baseDir, work0 ? "work0" : "work1");
    }

    private File getCheckFile(boolean work0) {
        return new File(getWorkDir(work0), ACTIVEDIR_NAME);
    }

    private void switchCheckFile() {
        final File oldFile = getCheckFile(!this.work0Active);
        final File newFile = getCheckFile(this.work0Active);
        try {
            if (oldFile.delete()) {
                this.logger.info("<switchCheckFile> deleted " + oldFile.getAbsolutePath());
            }
            else {
                this.logger.error("<switchCheckFile> failed deleting old check file: " + oldFile.getAbsolutePath());
            }

            if (newFile.createNewFile()) {
                this.logger.info("<switchCheckFile> created " + newFile.getAbsolutePath());
            }
            else {
                this.logger.error("<switchCheckFile> failed creating check file: " + newFile.getAbsolutePath());
            }
        } catch (IOException e) {
            this.logger.error("<switchCheckFile> failed creating check file", e);
        }
    }

    public static void main(String[] args) throws Exception {
        GisResearchProviderImpl impl = new GisResearchProviderImpl();
        impl.setBaseDir(LocalConfigProvider.getProductionDir("var/data/gisresearch/"));
        impl.incomingDir = FileUtil.ensureDir(new File(impl.baseDir, "incoming"));
        impl.archiveDir = FileUtil.ensureDir(new File(impl.baseDir, "archive"));

        FileUtil.ensureDir(new File(impl.baseDir, "work0"));
        FileUtil.ensureDir(new File(impl.baseDir, "work1"));

        impl.work0Active = impl.getCheckFile(true).canRead();
        boolean work1Active = impl.getCheckFile(false).canRead();

        impl.checkIncoming();
        impl.afterPropertiesSet();


        GisResearchRequest r = new GisResearchRequest(new Selector[]{Selector.DZ_HM3});
        r.setSortBy(SORT_BY_DATE);
        r.setQuery(new TermQuery(new org.apache.lucene.index.Term(FIELD_ISSUER, "555700")));
        r.setLocale(Locale.GERMAN);
        r.setCount(1);

        GisResearchResponse response = impl.search(r);
        for (FacetedSearchResult.Facet f : response.getFacetedSearchResult().getFacets()) {
            System.out.println(f.getId());
            for (FacetedSearchResult.Value v : f.getValues()) {
                System.out.println(" " + v.getId() + " - " + v.getName() + " :" + v.getCount());
            }
        }

        System.out.println(response.getDocs());
    }
}
