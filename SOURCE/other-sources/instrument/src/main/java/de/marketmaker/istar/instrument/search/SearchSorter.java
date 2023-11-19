/*
 * SearchSorter.java
 *
 * Created on 20.08.2009 11:12:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import org.apache.lucene.search.SortField;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface SearchSorter {
    SortField getSortField();
}
