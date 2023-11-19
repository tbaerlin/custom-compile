/*
 * EconodaySearchRequest.java
 *
 * Created on 30.03.12 13:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaItem;

/**
 * @author zzhao
 */
public class EconodaySearchResponse extends AbstractIstarResponse {

    public static final EconodaySearchResponse INVALID = new EconodaySearchResponse();

    private final int totalCount;

    private final List<Release> releases;

    private Map<EconodayMetaDataKey, List<FinderMetaItem>> metaData;

    private EconodaySearchResponse() {
        this.totalCount = 0;
        this.releases = Collections.emptyList();
        setInvalid();
    }

    public EconodaySearchResponse(int totalCount, List<Release> releases,
            Map<EconodayMetaDataKey, List<FinderMetaItem>> metaData) {
        this.releases = releases;
        this.totalCount = totalCount;
        this.metaData = metaData;
    }

    public List<Release> getReleases() {
        return releases;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public Map<EconodayMetaDataKey, List<FinderMetaItem>> getMetaData() {
        return this.metaData;
    }
}
