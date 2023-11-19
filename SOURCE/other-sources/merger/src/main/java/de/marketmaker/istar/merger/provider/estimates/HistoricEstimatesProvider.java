/*
 * HistoricEstimatesProvider.java
 *
 * Created on 10.10.11 14:28
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.estimates;

import de.marketmaker.istar.domain.data.HistoricEstimates;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface HistoricEstimatesProvider {
    HistoricEstimates getHistoricEstimates(long instrumentid);
}
