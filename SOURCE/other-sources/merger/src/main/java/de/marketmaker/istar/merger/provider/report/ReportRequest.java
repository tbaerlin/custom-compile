/*
 * ReportRequest.java
 *
 * Created on 16.05.12 16:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.report;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.data.DownloadableItem;

/**
 * @author zzhao
 */
public class ReportRequest extends AbstractIstarRequest {

    private final long instrumentId;

    private List<DownloadableItem.Source> providerPref;

    public ReportRequest(long instrumentId) {
        this.instrumentId = instrumentId;
        this.providerPref = new ArrayList<>(5);
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public List<DownloadableItem.Source> getProviderPref() {
        return new ArrayList<>(this.providerPref);
    }

    public ReportRequest addProvider(DownloadableItem.Source provider) {
        if (!this.providerPref.contains(provider)) {
            this.providerPref.add(provider);
        }
        return this;
    }
}
