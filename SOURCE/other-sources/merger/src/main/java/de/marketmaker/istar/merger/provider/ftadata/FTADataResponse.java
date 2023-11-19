/*
 * FTADataResponse.java
 *
 * Created on 5/17/13 1:53 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.ftadata;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.FTACommentary;
import de.marketmaker.istar.domain.data.FTAMasterData;
import de.marketmaker.istar.domain.data.FTAMetaData;

/**
 * @author Stefan Willenbrock
 */
public class FTADataResponse extends AbstractIstarResponse {
    private final static long serialVersionUID = 1L;

    private final FTAMasterData masterData;

    private final FTACommentary commentary;

    private final FTAMetaData metadata;

    FTADataResponse(final FTAMasterData masterData, final FTACommentary commentary,
            final FTAMetaData metadata) {
        this.masterData = masterData;
        this.commentary = commentary;
        this.metadata = metadata;
        if (masterData == null || commentary == null || metadata == null) {
            setInvalid();
        }
    }

    public FTAMasterData getMasterData() {
        return masterData;
    }

    public FTACommentary getCommentary() {
        return commentary;
    }

    public FTAMetaData getMetaData() {
        return metadata;
    }
}
