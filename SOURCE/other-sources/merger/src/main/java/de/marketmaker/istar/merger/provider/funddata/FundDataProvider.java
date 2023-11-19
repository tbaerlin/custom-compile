/*
 * FundDataProvider.java
 *
 * Created on 11.08.2006 18:34:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.funddata;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.provider.funddata")
public interface FundDataProvider {
    /**
     * Get data for a list of funds.
     *
     * @param request specifies instruments and data items to be retrieved
     * @return data for requested instruments and data items
     */
    FundDataResponse getFundData(FundDataRequest request);
}
