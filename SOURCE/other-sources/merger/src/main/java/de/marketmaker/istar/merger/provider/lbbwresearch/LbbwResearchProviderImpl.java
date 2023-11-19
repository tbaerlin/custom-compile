/*
 * DzResearchProviderImpl.java
 *
 * Created on 25.03.14 14:09
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.lbbwresearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.FacetedSearchResult;

import static de.marketmaker.istar.merger.provider.lbbwresearch.LbbwResearchIndexer.LBBW_RESEARCH_DOCS;

/**
 * @author mcoenen
 */
@ManagedResource
public class LbbwResearchProviderImpl implements LbbwResearchProvider, InitializingBean,
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

    private Map<String, BasicDocument> currentDocs = new HashMap<>();

    private LbbwIndexSearcher searcher;

    public void setBaseDir(File baseDir) {
        this.baseDir = FileUtil.ensureDir(baseDir);
    }

    @Override
    public LbbwResearchResponse search(LbbwResearchRequest r) {
        this.readLock.lock();
        try {
            return doSearch(r);
        } finally {
            this.readLock.unlock();
        }
    }

    private LbbwResearchResponse doSearch(LbbwResearchRequest r) {
        Set<String> ids;
        try {
            ids = this.searcher.search(r);
        } catch (IOException e) {
            this.logger.error("<search> failed", e);
            return new LbbwResearchResponse();
        }

        return createResult(r, ids);
    }

    private LbbwResearchResponse createResult(LbbwResearchRequest r, Set<String> ids) {
        final List<BasicDocument> docs = getBasicDocuments(ids);
        docs.sort(getComparator(r));

        final List<ResultDocument> items = new ArrayList<>(r.getCount());
        int n = Math.min(r.getOffset() + r.getCount(), docs.size());
        for (int i = r.getOffset(); i < n; i++) {
            items.add(new ResultDocument(docs.get(i)));
        }
        return new LbbwResearchResponse(items, ids.size(), countMetaData(docs));
    }

    private Comparator<BasicDocument> getComparator(LbbwResearchRequest r) {
        final Comparator<BasicDocument> c = BasicDocument.COMPARATORS.get(r.getSortBy());
        return r.isAscending() ? c : Collections.reverseOrder(c);
    }

    private List<BasicDocument> getBasicDocuments(Set<String> ids) {
        List<BasicDocument> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            final BasicDocument basicDocument = currentDocs.get(id);
            if (basicDocument == null) {
                this.logger.warn("<search> result w/o doc: " + id);
                continue;
            }
            result.add(basicDocument);
        }
        return result;
    }

    private FacetedSearchResult countMetaData(List<BasicDocument> docs) {
        final FacetedSearchResult.Builder b = FacetedSearchResult.createBuilder()
                .withEnum("documentType")
                .withEnum("category")
                .withEnum("rating")
                .withEnum("documentLanguage")
                .withEnum("sector")
                .withEnum("country");


        for (BasicDocument doc : docs) {
            b.addValue("documentType", doc.getDocumentType());
            b.addValue("category", doc.getCategory());
            b.addValue("rating", String.valueOf(doc.getRating()), BasicDocument.RATING_NAMES.get(doc.getRating()));
            b.addValue("documentLanguage", doc.getLanguage());
            b.addValue("sector", doc.getSector());

            doc.getCompanyInfos().stream()
                    .map(CompanyInfo::getCountry)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .distinct()
                    .forEach(country -> b.addValue("country", country));
        }

        return b.build();
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

            // No active dir
            if (!this.work0Active) {
                if(!this.initWorkDir()) {
                    throw new IllegalStateException("no active work directory but at least one contains data, manual action necessary");
                }
            }
            else {
                throw new IllegalStateException("two active work directories, manual action necessary");
            }
        }

        this.checkIncoming();

        this.searcher = new LbbwIndexSearcher(getIndexDir());
        this.currentDocs = readDocs(getWorkDir(this.work0Active));
    }

    private boolean initWorkDir() throws IOException {
        String[] work0Files = new File(this.baseDir, "work0").list();
        if (work0Files.length > 0) {
            return false;
        }
        String[] work1Files = new File(this.baseDir, "work1").list();
        if (work1Files.length > 0) {
            return false;
        }

        // Both directories empty -> create a new checkFile in work0
        return this.getCheckFile(true).createNewFile();
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

    private void unzipIncoming(File zip) throws Exception {
        File nonWorkDir = getWorkDir(!this.work0Active);
        FileUtils.cleanDirectory(nonWorkDir);
        this.logger.info("<unzipIncoming> to " + nonWorkDir.getName() + "...");
        FileUtil.unzipToDir(zip, nonWorkDir);
        this.logger.info("<unzipIncoming> done.");

        Map<String, BasicDocument> newDocs = readDocs(nonWorkDir);
        LbbwIndexSearcher newSearcher = new LbbwIndexSearcher(getIndexDir(!this.work0Active));

        this.work0Active = !this.work0Active;
        switchCheckFile();

        this.writeLock.lock();
        try (LbbwIndexSearcher ignored = this.searcher) {
            this.searcher = newSearcher;
            this.currentDocs = newDocs;
        } finally {
            this.writeLock.unlock();
        }
    }

    private Map<String, BasicDocument> readDocs(File dir) throws Exception {
        File docsFile = new File(dir, LBBW_RESEARCH_DOCS);
        if (!docsFile.isFile()) {
            return ImmutableMap.of();
        }
        Map<String, BasicDocument> result;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(docsFile))) {
            result = (Map<String, BasicDocument>) ois.readObject();
        }
        this.logger.info("<readDocs> read " + result.size() + " docs");
        return result;
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
        LbbwResearchProviderImpl impl = new LbbwResearchProviderImpl();
        impl.setBaseDir(LocalConfigProvider.getProductionDir("var/data/lbbwresearch/"));
        impl.afterPropertiesSet();

        LbbwResearchRequest r = new LbbwResearchRequest();
        r.setSelectors(getSelectors());
        r.setQuery(new MatchAllDocsQuery());
        r.setCount(1000);

        LbbwResearchResponse response = impl.search(r);
        for (FacetedSearchResult.Facet f : response.getFacetedSearchResult().getFacets()) {
            System.out.println(f.getId());
            for (FacetedSearchResult.Value v : f.getValues()) {
                System.out.println(" " + v.getId() + ": " + v.getCount());
            }
        }

        System.out.println(response);
    }

    private static EnumSet<Selector> getSelectors() {
        EnumSet<Selector> selectors = EnumSet.noneOf(Selector.class);
        selectors.add(Selector.LBBW_RESEARCH_MAERKTE_IM_BLICK);
        selectors.add(Selector.LBBW_RESEARCH_RESTRICTED_REPORTS);
        return selectors;
    }
}
