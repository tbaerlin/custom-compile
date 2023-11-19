/*
 * NewsRecordHandler.java
 *
 * Created on 12.03.2007 13:43:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import de.marketmaker.istar.news.data.NewsRecordImpl;

/**
 * processing records from news-feed
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface NewsRecordHandler {

    void handle(NewsRecordImpl newsRecord);

}
