/*
 * PageProvider.java
 *
 * Created on 22.02.2008 17:19:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface PageProvider {
    MergerPageResponse getPage(MergerPageRequest requestMerger) throws Exception;
}
