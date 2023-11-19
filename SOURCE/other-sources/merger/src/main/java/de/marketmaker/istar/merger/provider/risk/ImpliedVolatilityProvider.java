/*
 * ImpliedVolatilityProvider.java
 *
 * Created on 14.12.11 13:28
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.risk;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author oflege
 */
@AmqpAddress(queue = "istar.provider.impliedVola")
public interface ImpliedVolatilityProvider {
    ImpliedVolatilityResponse getImpliedVolatilities(ImpliedVolatilityRequest request);
}
