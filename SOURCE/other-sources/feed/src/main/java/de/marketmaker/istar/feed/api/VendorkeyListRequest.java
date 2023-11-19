/*
 * SymbolSortRequest.java
 *
 * Created on 17.02.2005 13:48:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * Requests all <em>typed</em> vendorkeys for a given market.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VendorkeyListRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private final String market;

    public VendorkeyListRequest(String market) {
        this.market = market;
    }

    public String getMarket() {
        return market;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", market=").append(this.market);
    }

    @Override
    public String toString() {
        return "VendorkeyListRequest[" +
                "market='" + market + '\'' +
                ']';
    }
}

