/*
 * PageDao.java
 *
 * Created on 13.06.2005 11:37:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.pages;

import java.util.List;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface PageDao {
    void store(PageData page);

    PageData getPageData(int pagenumber);

    // more like: getPageIds
    List<Integer> getPagenumbers();

    List<Integer> getPagenumbersChangedAfter(DateTime dt);

    Neighborhood getNeighborhood(int pagenumber);

    /**
     * gets all pages from the data store and forwards them one by one to handler
     * @param handler handles retrieved pages
     * @param dynamic TRUE to request only dynamic pages, FALSE for non-dynamic, null for both
     */
    void getAllPages(Handler handler, Boolean dynamic);

    interface Neighborhood {
        Integer getNextPagenumber();
        Integer getPreviousPagenumber();
    }

    interface Handler {
        void handle(PageData data);
    }

}
