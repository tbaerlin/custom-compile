/*
 * NewsReIndexer.java
 *
 * Created on 06.12.2007 15:55:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.instrument.export.InstrumentDao;
import de.marketmaker.istar.news.analysis.NewsAnalyzer;
import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;

/**
 * Uses the dao to select all news records and forwards them to a delegate NewsRecordHandler.
 * Can be used to re-index all news found in a database or s.th. like that. For test purposes,
 * the number of news returned by the dao can be limited by setting the limit parameter.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsReIndexer implements InitializingBean, NewsRecordHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private NewsDao newsDao;

    private News2Document news2Document;

    private InstrumentDao instrumentDao;

    private int count = 0;

    private int limit = 0;

    private final Map<String, Instrument> cache = new LinkedHashMap<String, Instrument>(1000, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Instrument> eldest) {
            return size() > 1000;
        }
    };

    private boolean useCompoundFile = true;

    private int rAMBufferSizeMB = 48;

    private int mergeFactor = 20;

    private File indexBaseDir;

    private IndexWriter indexWriter;

    private TopicBuilder topicBuilder;

    private DateTime now = new LocalDate().toDateTimeAtStartOfDay();

    @Required
    public void setNews2Document(News2Document news2Document) {
        this.news2Document = news2Document;
    }

    @Required
    public void setIndexBaseDir(File indexBaseDir) {
        this.indexBaseDir = indexBaseDir;
    }

    public void setTopicBuilder(TopicBuilder topicBuilder) {
        this.topicBuilder = topicBuilder;
    }

    public void setNow(String nowStr) {
        this.now = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(nowStr);
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setUseCompoundFile(boolean useCompoundFile) {
        this.useCompoundFile = useCompoundFile;
    }

    public void setRAMBufferSizeMB(int rAMBufferSizeMB) {
        this.rAMBufferSizeMB = rAMBufferSizeMB;
    }

    public void setMergeFactor(int mergeFactor) {
        this.mergeFactor = mergeFactor;
    }

    @Required
    public void setNewsDao(NewsDao newsDao) {
        this.newsDao = newsDao;
    }

    @Required
    public void setInstrumentDao(InstrumentDao instrumentDao) {
        this.instrumentDao = instrumentDao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        reindex();
    }

    private void reindex() throws IOException {
        final TimeTaker tt = new TimeTaker();
        this.logger.info("<reindex> starting, using limit " + this.limit);

        this.indexWriter = createIndexWriter();

        this.newsDao.getAllItems(this, this.limit, false);
        this.indexWriter.close(true);

        this.logger.info("<reindex> finished in " + tt + " for " + this.count);
    }

    private IndexWriter createIndexWriter() throws IOException {
        final IndexWriter result = new IndexWriter(
                FSDirectory.open(this.indexBaseDir),
                new NewsAnalyzer(),
                true,
                NewsIndex.FIELD_LENGTH);
        result.setMergeFactor(this.mergeFactor);
        result.setRAMBufferSizeMB(this.rAMBufferSizeMB);
        // we can roughly fit 1000 news in a meg, use 5.5 times that according to
        // http://www.gossamer-threads.com/lists/lucene/java-dev/51041
        result.setMaxBufferedDocs((int) (5.5d * this.rAMBufferSizeMB * 1000));
        // this.writer.setInfoStream(System.out);
        result.setUseCompoundFile(this.useCompoundFile);
        return result;
    }

    @Override
    public void handle(NewsRecordImpl record) {
        if (NewsServerImpl.isPageToBeIgnored(record)) {
            return;
        }
        if (!record.getTimestamp().isBefore(now)) {
            return;
        }

        if (++this.count % 10000 == 0) {
            this.logger.info("<handle> count = " + this.count);
        }

        assignInstruments(record);
        if (this.topicBuilder != null) {
            this.topicBuilder.handle(record);
        }

        try {
            final Document document = this.news2Document.toDocument(record);
            this.indexWriter.addDocument(document);
        } catch (Exception e) {
            this.logger.warn("<handle> failed for " + record, e);
        }
    }

    private void assignInstruments(NewsRecordImpl newsRecord) {
        final Set<String> iids = newsRecord.getAttributes(NewsAttributeEnum.IID);
        if (iids.isEmpty()) {
            return;
        }
        final ArrayList<Instrument> instruments = new ArrayList<>(iids.size());
        for (String iid : iids) {
            Instrument instrument = this.cache.get(iid);
            if (instrument == null) {
                instrument = this.instrumentDao.getInstrument(Long.parseLong(iid));
                if (instrument != null) {
                    this.cache.put(iid, instrument);
                }
            }
            if (instrument != null) {
                instruments.add(instrument);
            }
        }
        if (!instruments.isEmpty()) {
            newsRecord.setInstruments(instruments);
        }
    }
}
