/*
 * ScreenerProvider.java
 *
 * Created on 03.04.2007 19:14:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.screener;

import de.marketmaker.istar.domain.data.ScreenerUpDownData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ScreenerProvider {
    ScreenerResult getScreenerResult(long instrumentid, String language);

    ScreenerUpDownData getUpDownData(String region);
}
