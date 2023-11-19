/*
 * RatioSearchEngine.java
 *
 * Created on 21.08.14 09:08
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author oflege
 */
@AmqpAddress(queue = "istar.merger.ratiosfrontend")
public interface RatioSearchEngine {
    RatioSearchResponse search(RatioSearchRequest request);

    RatioSearchMetaResponse getMetaData(RatioSearchMetaRequest request);
}
