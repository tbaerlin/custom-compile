/*
 * Timeseries.java
 *
 * Created on 01.03.2005 10:45:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.timeseries;

import de.marketmaker.istar.domain.data.DataWithInterval;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Timeseries<K> extends Iterable<DataWithInterval<K>> {
}
