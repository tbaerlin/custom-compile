/*
 * NewsDbCopier.java
 *
 * Created on 08.02.2009 11:22:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsRecord;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsDbCopier implements InitializingBean, NewsRecordHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private NewsDaoDb inDao;

    private NewsDaoDb outDao;

    private int numInserted = 0;

    private DateTime from = null;

    public void setFrom(String from) {
        this.from = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(from);
    }

    public void setInDao(NewsDaoDb inDao) {
        this.inDao = inDao;
    }

    public void setOutDao(NewsDaoDb outDao) {
        this.outDao = outDao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (inDao == outDao) {
            throw new IllegalArgumentException("in and out are the same");
        }
        if (this.from == null) {
            this.logger.info("<afterPropertiesSet> copy all news");
            this.inDao.getAllItems(this, -1, false);
        }
        else {
            this.logger.info("<afterPropertiesSet> copy news >= " + this.from);
            final List<String> ids = this.inDao.getIdsSince(this.from);
            final List<String> chunk = new ArrayList<>(10);
            for (Iterator<String> it = ids.iterator(); it.hasNext(); ) {
                chunk.add(it.next());
                if (chunk.size() == 10 || !it.hasNext()) {
                    final List<NewsRecord> news = this.inDao.getItems(chunk, true, true);
                    for (NewsRecord nr : news) {
                        handle((NewsRecordImpl) nr);
                    }
                    chunk.clear();
                }
            }
        }
        this.logger.info("<afterPropertiesSet> finished, inserted " + numInserted);
    }

    public void handle(NewsRecordImpl newsRecord) {
        this.outDao.insertItem(newsRecord);
        this.numInserted++;
        if (numInserted % 10000 == 0) {
            this.logger.info("<insert> #" + this.numInserted);
        }
    }
}
