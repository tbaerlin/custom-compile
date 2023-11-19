/*
 * StatisticsMetaData.java
 *
 * Created on 14.01.2010 14:56:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import java.io.Serializable;

/**
 * Meta data needed to show statistics information: the pages that are known to the application
 * and various selectors that can be used to restrict queries to certain user groups.
 * @author oflege
 */
public class StatisticsMetaData implements Serializable {
    private Selectors selectors;

    private Pages pages;

    public Selectors getSelectors() {
        return this.selectors;
    }

    public void setSelectors(Selectors selectors) {
        this.selectors = selectors;
    }

    public Pages getPages() {
        return this.pages;
    }

    public void setPages(Pages pages) {
        this.pages = pages;
    }

    @Override
    public String toString() {
        return "StatisticsMetaData[#selectors=" + this.selectors.getElements().size() // $NON-NLS-0$
                + ", #pages=" + this.pages.getModuleNames().size() + "]"; // $NON-NLS-0$ $NON-NLS-1$
    }
}
