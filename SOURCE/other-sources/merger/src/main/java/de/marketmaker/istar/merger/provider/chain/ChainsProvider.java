package de.marketmaker.istar.merger.provider.chain;

import de.marketmaker.istar.common.amqp.AmqpAddress;


@AmqpAddress(queue = "istar.chains")
public interface ChainsProvider {

    ChainsResponse searchChainElements(ChainsRequest request);

}
