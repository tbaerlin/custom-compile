/*
 * AnalysesServer.java
 *
 * Created on 21.03.12 09:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.frontend;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author oflege
 */
@AmqpAddress(queue = "istar.analyses")
public interface AnalysesServer {

    AnalysesMetaResponse getMetaData(AnalysesMetaRequest request);

    AnalysesSummaryResponse getSummary(AnalysesRequest request);

    AnalysisResponse getAnalyses(AnalysesRequest request);

    AnalysisImageResponse getImage(AnalysisImageRequest request);

}
