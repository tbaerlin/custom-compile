/*
 * NewsResponse.java
 *
 * Created on 15.03.2007 12:17:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.frontend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsResponseImpl extends AbstractIstarResponse implements NewsResponse {
    protected static final long serialVersionUID = 1L;

    private List<NewsRecord> records;

    /** only needed by backend to retrieve records */
    private transient List<String> ids;

    private int hitCount = -1;

    private String prevPageRequestId;

    private String nextPageRequestId;

    public static NewsResponseImpl getInvalid() {
        final NewsResponseImpl result = new NewsResponseImpl();
        result.setInvalid();
        return result;
    }

    public void setRecords(List<NewsRecord> records) {
        this.records = new ArrayList<>(records.size());
        this.records.addAll(records);
    }

    @Override
    public List<NewsRecord> getRecords() {
        if (this.records != null) {
            return this.records;
        }
        return Collections.emptyList();
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getIds() {
        return (this.ids != null) ? this.ids : Collections.<String>emptyList();
    }

    @Override
    public String getNextPageRequestId() {
        return this.nextPageRequestId;
    }

    public void setNextPageRequestId(String nextPageRequestId) {
        this.nextPageRequestId = nextPageRequestId;
    }

    @Override
    public String getPrevPageRequestId() {
        return this.prevPageRequestId;
    }

    public void setPrevPageRequestId(String prevPageRequestId) {
        this.prevPageRequestId = prevPageRequestId;
    }

    @Override
    public int getHitCount() {
        return hitCount;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        final List<NewsRecord> myItems = getRecords();
        sb.append(", hits=").append(this.hitCount);
        if (this.records == null && ids != null) {
            sb.append(", ids=").append(this.ids);
        }
        else if (this.records != null) {
            sb.append(", records=");
            char sep = '[';
            for (NewsRecord myItem : myItems) {
                sb.append(sep).append(myItem.getId());
                sep = ',';
            }
            sb.append("]");
        }
    }
}
