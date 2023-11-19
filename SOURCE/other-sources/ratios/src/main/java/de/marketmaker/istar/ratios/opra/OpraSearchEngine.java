/*
 * OpraSearchEngine.java
 *
 * Created on 21.08.14 09:09
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.opra;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.ratios.frontend.MatrixMetadataRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.RatioSearchMetaResponse;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;

/**
 * @author oflege
 */
@AmqpAddress(queue = "istar.opra")
public interface OpraSearchEngine {

    OpraRatioSearchResponse getOpraItems(RatioSearchRequest request);

    MatrixMetadataRatioSearchResponse getOpraMatrix(RatioSearchRequest request);

    RatioSearchMetaResponse getOpraMetaData();

}
