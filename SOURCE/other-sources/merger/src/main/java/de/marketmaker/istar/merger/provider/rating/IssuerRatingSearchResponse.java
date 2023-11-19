/*
 * IssuerRatingSearchResponse.java
 *
 * Created on 07.05.12 11:33
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zzhao
 */
public class IssuerRatingSearchResponse extends AbstractIstarResponse {

    public static final IssuerRatingSearchResponse INVALID = new IssuerRatingSearchResponse();

    public static final IssuerRatingSearchResponse EMPTY = new IssuerRatingSearchResponse(0, new ArrayList<>(0), new HashMap<>(0));

    private static final long serialVersionUID = -6748898981396993743L;

    private int totalCount;

    private List<IssuerRating> issuerRatings;

    private Map<IssuerRatingMetaDataKey, List<FinderMetaItem>> metaData;

    private String message;

    private IssuerRatingSearchResponse() {
        this.totalCount = 0;
        this.issuerRatings = Collections.emptyList();
        this.metaData = Collections.emptyMap();
        setInvalid();
    }

    public IssuerRatingSearchResponse(String msg) {
        this();
        this.message = msg;
    }

    public IssuerRatingSearchResponse(int totalCount, List<IssuerRating> issuerRatings,
            Map<IssuerRatingMetaDataKey, List<FinderMetaItem>> metaData) {
        this.totalCount = totalCount;
        this.issuerRatings = issuerRatings;
        this.metaData = metaData;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public List<IssuerRating> getIssuerRatings() {
        return issuerRatings;
    }

    public Map<IssuerRatingMetaDataKey, List<FinderMetaItem>> getMetaData() {
        return metaData;
    }

    public String getMessage() {
        return message;
    }
}
