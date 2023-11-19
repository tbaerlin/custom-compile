/*
 * BondDataProvider.java
 *
 * Created on 13.09.2006 10:59:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.bonddata;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.data.MasterDataBond;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.provider.bonddata")
public interface BondDataProvider {
    MasterDataBond getMasterData(long instrumentid);

    BenchmarkHistoryResponse getBenchmarkHistory(BenchmarkHistoryRequest request);
}
