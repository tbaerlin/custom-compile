/*
 * FeedDataFactory.java
 *
 * Created on 18.09.2008 09:23:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface FeedDataFactory {
    FeedData create(final Vendorkey vendorkey, final FeedMarket market);
}
