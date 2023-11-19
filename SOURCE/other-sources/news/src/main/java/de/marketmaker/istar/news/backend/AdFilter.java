/*
 * AdFilter.java
 *
 * Created on 07.11.2008 14:01:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.news.data.NewsRecordImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AdFilter implements NewsRecordHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private NewsRecordHandler delegate;

    @Required
    public void setDelegate(NewsRecordHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void handle(NewsRecordImpl newsRecord) {
        if (!newsRecord.isAd()) {
            this.delegate.handle(newsRecord);
        }
        else {
            this.logger.info("<handle> blocked ad: " + newsRecord);
        }
    }
}
