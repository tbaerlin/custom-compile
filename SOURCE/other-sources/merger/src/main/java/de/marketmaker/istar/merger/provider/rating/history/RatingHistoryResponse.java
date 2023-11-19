/*
 * RatingHistoryResponse.java
 *
 * Created on 11.09.12 14:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating.history;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author zzhao
 */
public class RatingHistoryResponse extends AbstractIstarResponse {
    private static final long serialVersionUID = -8089509490019701786L;

    private final long iid;

    private List<RatingHistory> ratingHistories = new ArrayList<>();

    public RatingHistoryResponse(boolean valid, long iid) {
        if (!valid) {
            setInvalid();
        }
        this.iid = iid;
    }

    public long getIid() {
        return iid;
    }

    public RatingHistoryResponse(long iid, List<RatingHistory> ratingHistories) {
        this(true, iid);
        this.ratingHistories = new ArrayList<>(ratingHistories);
    }

    public List<RatingHistory> getRatingHistories() {
        return new ArrayList<>(this.ratingHistories);
    }
}
