/*
 * IndexMembershipRequest.java
 *
 * Created on 28.03.14 14:11
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author oflege
 */
public class IndexMembershipRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 122L;

    private final Set<Long> qids;

    public IndexMembershipRequest(Quote quote) {
        this.qids = Collections.singleton(quote.getId());
    }

    public IndexMembershipRequest(Instrument instrument) {
        this.qids = new HashSet<>();
        for (Quote quote : instrument.getQuotes()) {
            this.qids.add(quote.getId());
        }
    }

    public Set<Long> getQids() {
        return qids;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", qids=").append(this.qids);
    }
}


