/*
 * DailyIndexMaintenanceMethod.java
 *
 * Created on 28.09.2009 14:08:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.search.Query;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.core.io.Resource;

import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsRecord;

/**
 * Performs all tasks that need to be performed on a daily basis to maintain the NewsIndexes.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class DailyIndexMaintenanceMethod {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NewsServerImpl server;

    private final File updatesFile;

    private final Resource deleteQueriesSource;

    private NewsIndex historicIndex;

    private NewsIndex dailyIndex;

    DailyIndexMaintenanceMethod(NewsServerImpl server, NewsIndex[] indexes) {
        this.server = server;
        this.updatesFile = server.getUpdatesFile();
        this.deleteQueriesSource = server.getDeleteQueriesSource();
        this.dailyIndex = indexes[1];
        this.historicIndex = indexes[2];
    }

    /**
     * Performs index maintenance
     * @return list of ids of news to be deleted from that database
     * @throws IOException on error
     */
    List<String> invoke() throws IOException {
        this.logger.info("<invoke> ...");

        final List<String> idsOfUpdatedNews = updateNews();
        this.logger.info("updated " + idsOfUpdatedNews.size());

        this.dailyIndex.closeWriter();
        this.logger.info("closed daily index");

        this.historicIndex.addIndex(this.dailyIndex);
        this.logger.info("added yesterday's index to historic");

        final List<String> idsToBeDeleted = deleteOldNews();
        this.logger.info("deleted " + idsToBeDeleted.size());

        // in Lucene 2.9ff, searching etc. is per segment, so frequent optimizations
        // are no longer necessary. 
        if (new LocalDate().getDayOfWeek() == DateTimeConstants.SUNDAY) {
            this.historicIndex.optimize();
            this.logger.info("optimized historic index");
        }

        this.historicIndex.closeWriter();
        this.logger.info("finished");
        return merge(idsToBeDeleted, idsOfUpdatedNews);
    }

    private List<String> merge(List<String> list1, List<String> list2) {
        if (list1.isEmpty()) {
            return list2;
        }
        list1.addAll(list2);
        return list1;
    }

    private List<String> updateNews() throws IOException {
        try {
            final List<NewsRecordUpdate> updates = new NewsUpdatesParser(this.updatesFile).getUpdates();
            if (updates.isEmpty()) {
                return Collections.emptyList();
            }

            final List<Query> queries = getUpdateQueries(updates);
            this.logger.info("<updateNews> for news matching " + queries);

            return merge(
                    updateNews(this.dailyIndex, updates, queries),
                    updateNews(this.historicIndex, updates, queries)
            );
        } catch (Throwable t) {
            this.logger.error("<updateNews> failed", t);
            return Collections.emptyList();
        }
    }

    private List<String> updateNews(NewsIndex index, List<NewsRecordUpdate> updates,
            List<Query> queries) throws IOException {
        final List<String> idsOfUpdatedNews = index.delete(queries);
        this.logger.info("<updateNews> found " + idsOfUpdatedNews.size() + " news to be updated");

        updateNewsRecords(updates, idsOfUpdatedNews);
        this.logger.info("<updateNews> finished for " + index);

        return idsOfUpdatedNews;
    }

    private void updateNewsRecords(List<NewsRecordUpdate> updates, List<String> ids) {
        for (int i = 0; i < ids.size(); i += 20) {
            final List<NewsRecord> records =
                    this.server.getItems(ids.subList(i, Math.min(i + 20, ids.size())), true, true);
            for (NewsRecord record : records) {
                final NewsRecordImpl updated = new NewsRecordEditor((NewsRecordImpl) record).apply(updates);
                this.server.assignInstrumentsTo(updated);
                this.server.handle(updated, true);
            }
        }
    }

    private List<Query> getUpdateQueries(List<NewsRecordUpdate> updates) {
        return updates.stream().map(NewsRecordUpdate::toQuery).collect(Collectors.toList());
    }

    private List<String> deleteOldNews() {
        final List<Query> queries = getDeleteQueries();

        this.logger.info("<deleteOldNews> ...");

        if (queries.isEmpty()) {
            this.logger.info("<deleteOldNews> nothing to delete");
            return Collections.emptyList();
        }

        try {
            final List<String> result = this.historicIndex.delete(queries);
            this.logger.info("<deleteOldNews> finished");
            return result;
        } catch (IOException e) {
            this.logger.error("<deleteOldNews> failed to delete old news", e);
            return Collections.emptyList();
        }
    }

    private List<Query> getDeleteQueries() {
        final List<Query> result = new ArrayList<>();

        if (this.deleteQueriesSource != null && this.deleteQueriesSource.exists()) {
            result.addAll(new DeleteQueriesParser(this.deleteQueriesSource).getQueries());
        }
        else {
            this.logger.warn("<deleteOldNews> deleteQueriesSource " + this.deleteQueriesSource + " does not exist");
        }

        this.server.drainDeleteFromHistoryQueriesTo(result);
        return result;
    }
}
