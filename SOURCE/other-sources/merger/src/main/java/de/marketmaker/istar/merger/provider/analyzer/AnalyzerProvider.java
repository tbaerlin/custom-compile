package de.marketmaker.istar.merger.provider.analyzer;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 *
 */
@AmqpAddress(queue = "istar.analyzer")
public interface AnalyzerProvider {

    AnalyzerResponse getAnalytics(AnalyzerRequest analyzerRequest);

}
