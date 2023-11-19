/*
 * PageLoader.java
 *
 * Created on 16.07.2008 12:53:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.paging;

/**
 * This interface is implemented by classes able to reload their data.
 * It is used, for example, by {@link PagingFeature} to reload data
 * after user requested to show another page.
 *
 * @author Ulrich Maurer
 */
public interface PageLoader {
    void reload();
}
