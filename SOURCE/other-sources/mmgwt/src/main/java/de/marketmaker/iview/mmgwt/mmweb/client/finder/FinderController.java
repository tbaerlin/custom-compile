/*
 * FinderController.java
 *
 * Created on 11.06.2008 11:57:48
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface FinderController {
    String getId();

    String getViewGroup();

    void search();

    void prepareFind(String field1, String value1, String field2, String value2);

    void prepareFind(FinderFormConfig config);

    void onSearchLoaded();
}
