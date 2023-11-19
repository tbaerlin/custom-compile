/*
 * DzResearchProvider.java
 *
 * Created on 24.03.14 14:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author oflege
 */
@AmqpAddress(queue = "istar.gisresearch")
public interface GisResearchProvider {
    GisResearchResponse search(GisResearchRequest r);
}
