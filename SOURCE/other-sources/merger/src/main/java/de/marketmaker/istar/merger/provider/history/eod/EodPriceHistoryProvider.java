/*
 * EodPriceHistoryProvider.java
 *
 * Created on 23.10.12 14:34
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author zzhao
 */
@AmqpAddress(queue = "istar.history.eod")
public interface EodPriceHistoryProvider {

    EodPriceHistoryResponse query(EodPriceHistoryRequest req);
}
