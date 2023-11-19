/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import java.util.List;
import java.util.Locale;

public class RegulatoryReportingRequest extends AbstractIstarRequest {

    static final long serialVersionUID = 1L;

    private final long instrumentId;

    public RegulatoryReportingRequest(long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public long getInstrumentId() {
        return instrumentId;
    }
}
