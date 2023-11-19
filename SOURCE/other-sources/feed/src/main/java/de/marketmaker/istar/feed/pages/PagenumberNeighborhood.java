/*
 * PagenumberNeighborhood.java
 *
 * Created on 23.08.2010 11:25:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.pages;

/**
 * @author swild
 */
final class PagenumberNeighborhood implements PageDao.Neighborhood {

    private final Integer nextPagenumber;

    private final Integer previousPagenumber;

    PagenumberNeighborhood(final Integer nextPagenumber, final Integer previousPagenumber) {
        this.nextPagenumber = nextPagenumber;
        this.previousPagenumber = previousPagenumber;
    }

    public Integer getNextPagenumber() {
        return nextPagenumber;
    }

    public Integer getPreviousPagenumber() {
        return previousPagenumber;
    }
}

