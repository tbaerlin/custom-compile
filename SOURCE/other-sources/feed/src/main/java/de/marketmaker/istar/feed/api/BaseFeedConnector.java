/*
 * BaseFeedConnector.java
 *
 * Created on 05.03.15 08:53
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

/**
 * @author oflege
 */
public interface BaseFeedConnector {
    IntradayResponse getIntradayData(IntradayRequest request);
}
