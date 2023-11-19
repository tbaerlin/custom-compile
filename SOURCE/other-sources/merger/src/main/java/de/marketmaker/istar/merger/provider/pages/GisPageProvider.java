/*
 * GisPageProvider.java
 *
 * Created on 16.04.2009 09:20:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface GisPageProvider {
    String getPage(String pagenumber) throws Exception;
}
