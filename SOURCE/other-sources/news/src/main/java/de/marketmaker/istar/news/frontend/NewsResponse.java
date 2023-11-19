/*
 * NewsResponse.java
 *
 * Created on 15.03.2007 13:14:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.frontend;

import java.util.List;

import de.marketmaker.istar.common.request.IstarResponse;
import de.marketmaker.istar.news.frontend.NewsRecord;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface NewsResponse extends IstarResponse {
    List<NewsRecord> getRecords();

    int getHitCount();

    String getNextPageRequestId();

    String getPrevPageRequestId();
}
