/*
 * BewHistoricSnap.java
 *
 * Created on 27.10.2010 15:11:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author oflege
 */
@AmqpAddress(queue = "istar.merger.bewprices")
public interface BewHistoricPriceProvider {
    BewHistoricPriceResponse getPrices(BewHistoricPriceRequest request);
}
