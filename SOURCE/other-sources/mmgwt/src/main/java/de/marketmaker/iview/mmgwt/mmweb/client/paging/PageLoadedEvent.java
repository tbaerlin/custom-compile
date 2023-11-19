/*
 * PageLoadedEvent.java
 *
 * Created on 31.03.2008 17:25:16
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.paging;

/**
 * @author Ulrich Maurer
 */
public class PageLoadedEvent {
    private final int currentPage;
    private final int numPages;
    private final int resultOffset;
    private final int resultCount;
    private final int resultTotal;
    private final int pageSize;

    public PageLoadedEvent(int currentPage, int numPages, int resultOffset, int resultCount, int resultTotal, int pageSize) {
        this.currentPage = currentPage;
        this.numPages = numPages;
        this.resultOffset = resultOffset;
        this.resultCount = resultCount;
        this.resultTotal = resultTotal;
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getNumPages() {
        return numPages;
    }

    public int getResultOffset() {
        return resultOffset;
    }

    public int getResultCount() {
        return resultCount;
    }

    public int getResultTotal() {
        return resultTotal;
    }

    public int getPageSize() {
        return pageSize;
    }

    @Override
    public String toString() {
        return "PageLoadedEvent{" +  // $NON-NLS$
                "currentPage=" + currentPage +  // $NON-NLS$
                ", numPages=" + numPages +  // $NON-NLS$
                ", resultOffset=" + resultOffset +  // $NON-NLS$
                ", resultCount=" + resultCount +  // $NON-NLS$
                ", resultTotal=" + resultTotal +  // $NON-NLS$
                ", pageSize=" + pageSize +  // $NON-NLS$
                '}';
    }
}
