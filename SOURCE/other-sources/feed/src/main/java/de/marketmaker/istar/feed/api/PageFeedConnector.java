/*
 * FeedConnector.java
 *
 * Created on 14.12.2005 09:46:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.pages")
public interface PageFeedConnector {
    PageResponse getPage(PageRequest request);
}
