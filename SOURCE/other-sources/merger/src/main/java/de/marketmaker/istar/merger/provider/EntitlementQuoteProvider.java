/*
 * EntitlementQuoteProvider.java
 *
 * Created on 25.02.2008 10:00:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.List;

import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface EntitlementQuoteProvider {
    List<Quote> getQuotes(List<String> vendorkeys);
    Quote getQuote(String vendorkey);
}
