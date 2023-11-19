/*
 * PriceProvider.java
 *
 * Created on 18.10.2005 11:00:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.domain.data.SnapRecord;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface PriceProvider {
    SnapRecord getSnapRecord(long quoteid);
}
