/*
 * PageSearchProviderImpl.java
 *
 * Created on 16.07.2010 14:40:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

/**
 * This provider handles requests to search for pages.
 * Consider using {@link de.marketmaker.istar.merger.provider.pages.IndexDirectoryController}
 * for wrapping an updatable file system directory.
 * @author Sebastian Wild
 */
public class PageSearchProviderImpl implements PageSearchProvider, UpdatableDirectory {

    private IndexSearcher searcher;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The maximal number of search results returned by Lucene. Currently, this number
     * is much higher than the number of pages, so it allows arbitrarily many pages in effect.
     * Since the returned objects only contain pagenumber and summary, this is considered OK.
     */
    public static final int MAX_SEARCH_RESULTS = 100000;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final Map<DocumentFactory.PageLanguage, Filter> languageFilters
            = new EnumMap<>(DocumentFactory.PageLanguage.class);

    /**
     * Process {@code mergerRequest}.<br/>
     * <b>Make sure to set an index directory via {@link #setDirectory(org.apache.lucene.store.Directory)}
     * <em>before</em> calling this method.</b>
     * @param request request to search pages
     * @return response
     */
    public MergerPageSearchResponse searchPages(MergerPageSearchRequest request) {

        lock.readLock().lock();
        try {
            if (searcher == null) {
                this.logger.error("<searchPages> IndexSearcher is null!");
                return getInvalidResponse();
            }
            final Query query = request.getLuceneQuery();
            if (query == null) {
                this.logger.error("<searchPages> query in request is null!");
                return getInvalidResponse();
            }

            try {
                TopDocs topDocs = searcher.search(
                        query, getLanguageFilter(request.getPreferredLanguage()), MAX_SEARCH_RESULTS);

                ArrayList<PageSummary> searchResults = new ArrayList<>(topDocs.totalHits);
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document pageDoc = searcher.doc(scoreDoc.doc);
                    final String pageNumberString = pageDoc.get(DocumentFactory.PageField.PAGE_NUMBER.fieldName());
                    final String heading = pageDoc.get(DocumentFactory.PageField.HEADING.fieldName());
                    final String title = pageDoc.get(DocumentFactory.PageField.TITLE.fieldName());
                    final String languageString = pageDoc.get(DocumentFactory.PageField.LANGUAGE.fieldName());
                    final float score = scoreDoc.score;
                    // prefer heading, if available
                    final String summary = (heading == null || heading.length() < 5) ?
                            title : heading;
                    DocumentFactory.PageLanguage language = null;
                    try {
                        language = DocumentFactory.PageLanguage.valueOf(languageString);
                    }
                    catch (IllegalArgumentException e) {
                        this.logger.warn("<searchPages> Found page with unknown language field " +
                                languageString + ".");
                    }
                    int pageNumber = -1;
                    try {
                        pageNumber = Integer.parseInt(pageNumberString);
                    } catch (NumberFormatException e) {
                        this.logger.error("<searchPages> Index contained page with page number " +
                                pageNumberString + ", this is not an integer!");
                    }
                    searchResults.add(new PageSummary(pageNumber, summary, language, score));
                }
                return new MergerPageSearchResponse(searchResults);
            } catch (Exception
                    e) {
                this.logger.error("<searchPages> Searching the index failed: ", e);
                return getInvalidResponse();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * This method sets the {@link org.apache.lucene.store.Directory} to use for searching. It
     * is required to contain a Lucene index. This method will <b>block</b>, until the current
     * directory is no longer in use, i.e. until all running searches have finished.
     * @param indexDirectory the new index directory
     */
    public void setDirectory(Directory indexDirectory) {
        this.logger.info("<setDirectory> Switching to index directory " + indexDirectory);
        lock.writeLock().lock();
        try {
            if (this.searcher != null) {
                try {
                    this.searcher.close();
                } catch (IOException e) {
                    this.logger.warn("<setDirectory> Failed to close old searcher, old index may" +
                            " remain locked.");
                }
            }
            try {
                this.searcher = new IndexSearcher(indexDirectory, true);
            } catch (IOException e) {
                this.logger.error("<setDirectory> Error opening IndexSearcher for directory " +
                        indexDirectory, e);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    private MergerPageSearchResponse getInvalidResponse() {
        MergerPageSearchResponse result = new MergerPageSearchResponse(
                Collections.<PageSummary>emptyList());
        result.setInvalid();
        return result;
    }

    private Filter getLanguageFilter(DocumentFactory.PageLanguage prefLang) {
        if (prefLang == null) {
            return null;
        }
        if (!languageFilters.containsKey(prefLang)) {
            final TermsFilter allowedLangs = new TermsFilter();
            allowedLangs.addTerm(new Term(DocumentFactory.PageField.LANGUAGE.fieldName(),
                    DocumentFactory.PageLanguage.UNSPECIFIED.name()));
            allowedLangs.addTerm(new Term(DocumentFactory.PageField.LANGUAGE.fieldName(),
                    prefLang.name()));
            languageFilters.put(prefLang, new CachingWrapperFilter(allowedLangs));
        }
        return languageFilters.get(prefLang);
    }
}
