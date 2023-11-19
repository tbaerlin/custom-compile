/*
 * StockDataProvider.java
 *
 * Created on 25.09.2008 17:53:33
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.stockdata;

import java.util.List;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.data.MasterDataStock;
import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.provider.stockdata")
public interface StockDataProvider {

    MasterDataStock getMasterData(long iid, Profile profile);

    List<MasterDataStock> getMasterData(List<Long> iids, Profile profile);

    StockDataResponse getStockData(StockDataRequest request);
}
