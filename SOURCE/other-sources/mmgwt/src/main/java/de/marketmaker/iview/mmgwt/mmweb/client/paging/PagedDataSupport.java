/*
 * PagedDataSupport.java
 *
 * Created on 31.03.2008 14:52:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.paging;

/**
 * @author Ulrich Maurer
 */
public interface PagedDataSupport {
    void gotoFirstPage();
    void gotoNextPage();
    void gotoPage(int p);
    void gotoPreviousPage();
    void gotoLastPage();
}
