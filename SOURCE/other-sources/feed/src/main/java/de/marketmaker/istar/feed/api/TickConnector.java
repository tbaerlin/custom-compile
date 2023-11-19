/*
 * FeedConnector.java
 *
 * Created on 14.12.2005 09:46:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.feed.history.TickHistoryRequest;
import de.marketmaker.istar.feed.history.TickHistoryResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.chicago3.ticks")
public interface TickConnector extends BaseFeedConnector {

    TickHistoryResponse getTickHistory(TickHistoryRequest req);
}
