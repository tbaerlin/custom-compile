package de.marketmaker.istar.merger.provider.lbbwresearch;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * Interface for data provider
 * @author mcoenen
 */
@AmqpAddress(queue = "istar.lbbwresearch")
public interface LbbwResearchProvider {
    LbbwResearchResponse search(LbbwResearchRequest r);
}
