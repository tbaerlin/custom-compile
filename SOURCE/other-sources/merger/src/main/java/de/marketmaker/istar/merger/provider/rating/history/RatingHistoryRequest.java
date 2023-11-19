/*
 * RatingHistoryRequest.java
 *
 * Created on 11.09.12 14:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating.history;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author zzhao
 */
public class RatingHistoryRequest extends AbstractIstarRequest {
    private static final long serialVersionUID = -8010432793703867248L;

    private final long iid;

    public RatingHistoryRequest(long iid) {
        this.iid = iid;
    }

    public long getIid() {
        return iid;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(" iid: ").append(iid);
    }
}
