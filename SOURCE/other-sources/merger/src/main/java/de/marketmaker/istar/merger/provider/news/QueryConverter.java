/*
 * QueryConverter.java
 *
 * Created on 27.03.2007 17:58:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.news;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface QueryConverter {
    String toLuceneQueryString(String s) throws Exception;
}
