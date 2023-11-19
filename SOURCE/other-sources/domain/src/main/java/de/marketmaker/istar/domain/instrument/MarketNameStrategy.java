/*
 * MarketNameStrategy.java
 *
 * Created on 26.06.12 16:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

/**
 * Obtains the name for a quote's market
 * @author oflege
 */
public interface MarketNameStrategy extends NameStrategy<MarketNameStrategy, Quote> {
}
