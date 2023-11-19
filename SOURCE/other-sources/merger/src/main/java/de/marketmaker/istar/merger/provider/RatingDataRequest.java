/*
 * RatingDataRequest.java
 *
 * Created on 02.11.11 18:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tkiesgen
 */
public class RatingDataRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private Set<Long> iids = new HashSet<>();

    public RatingDataRequest(Long... iids) {
        this(Arrays.asList(iids));
    }

    public RatingDataRequest(Collection<Long> iids) {
        this.iids.addAll(iids);
    }

    public Set<Long> getIids() {
        return this.iids;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", iids=").append(this.iids);
    }
}
