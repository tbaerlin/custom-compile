/*
 * IstarListRequest.java
 *
 * Created on 07.05.12 15:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author zzhao
 */
public class IstarListRequest extends AbstractIstarRequest {
    static final long serialVersionUID = -4825116993376436277L;

    private final int offset;

    private final int count;

    private final String sortBy;

    private final boolean ascending;

    public IstarListRequest(int offset, int count, String sortBy, boolean ascending) {
        this.offset = offset;
        this.count = count;
        this.sortBy = sortBy;
        this.ascending = ascending;
    }

    public int getOffset() {
        return offset;
    }

    public int getCount() {
        return count;
    }

    public boolean isAscending() {
        return ascending;
    }

    public String getSortBy() {
        return sortBy;
    }

    @Override
    public String toString() {
        return "IstarListRequest{" +
                "offset=" + offset +
                ", count=" + count +
                ", sortBy='" + sortBy + '\'' +
                ", ascending=" + ascending +
                "} " + super.toString();
    }
}
