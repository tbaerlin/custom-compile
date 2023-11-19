/*
 * EodPriceHistoryProviderImpl.java
 *
 * Created on 09.01.13 15:13
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.read;

import de.marketmaker.istar.feed.history.HistoryProviderBase;
import de.marketmaker.istar.merger.provider.history.eod.EodPriceHistoryProvider;
import de.marketmaker.istar.merger.provider.history.eod.EodPriceHistoryRequest;
import de.marketmaker.istar.merger.provider.history.eod.EodPriceHistoryResponse;

/**
 * @author zzhao
 */
public class EodPriceHistoryProviderImpl extends HistoryProviderBase<EodPriceHistoryGatherer>
        implements EodPriceHistoryProvider {

    @Override
    public EodPriceHistoryResponse query(EodPriceHistoryRequest req) {
        try {
            return new EodPriceHistoryResponse(req.getQuote(), this.gatherer.gatherPrices(req));
        } catch (Exception e) {
            this.logger.error("<query> failed querying end-of-day price: {}", req, e);
            return EodPriceHistoryResponse.INVALID;
        }
    }
}
