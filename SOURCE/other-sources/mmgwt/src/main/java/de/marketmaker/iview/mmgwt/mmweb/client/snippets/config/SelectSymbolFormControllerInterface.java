/*
 * SelectSymbolFormControllerInterface.java
 *
 * Created on 04.01.13 10:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;

/**
 * @author Markus Dick
 */
public interface SelectSymbolFormControllerInterface {
    IndexedViewSelectionModel getIndexedViewSelectionModel();
    void setView(SelectSymbolForm view);
    void setTypes(String[] types);
    void setWithMsc(boolean withMsc);
    PagingFeature getPagingFeature();
    void search(String s);
    QuoteWithInstrument getResultQwi(int n);
    boolean hasData();

    void cancelPendingRequests();

    void setFilterForUnderlyingsOfLeverageProducts(Boolean filterForUnderlyingsOfLeveragProducts, boolean fireChange);
    void setFilterForUnderlyingsForType(String filterForUnderlyingsForType);
    void setFilterForUnderlyingsForType(String filterForUnderlyingsForType, boolean fireChange);
}