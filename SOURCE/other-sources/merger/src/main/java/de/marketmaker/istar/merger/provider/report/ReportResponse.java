/*
 * ReportResponse.java
 *
 * Created on 16.05.12 16:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.report;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.DownloadableItem;

/**
 * @author zzhao
 */
public class ReportResponse extends AbstractIstarResponse {

    public static final ReportResponse EMPTY = new ReportResponse(false) {
        @Override
        public Map<DownloadableItem.Source, List<DownloadableItem>> getReports() {
            return Collections.emptyMap();
        }
    };

    public static final ReportResponse INVALID = new ReportResponse(true);

    private Map<DownloadableItem.Source, List<DownloadableItem>> reports;

    private ReportResponse(boolean invalid) {
        if (invalid) {
            setInvalid();
        }
    }

    public ReportResponse(Map<DownloadableItem.Source, List<DownloadableItem>> items) {
        this.reports = new EnumMap<>(items);
    }

    public Map<DownloadableItem.Source, List<DownloadableItem>> getReports() {
        return new EnumMap<>(this.reports);
    }
}
