/*
 * SymbolSortResponse.java
 *
 * Created on 17.02.2005 13:48:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VendorkeyListResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private final String market;

    private final List<String> vendorkeys;

    public static VendorkeyListResponse createInvalid() {
        final VendorkeyListResponse result = new VendorkeyListResponse();
        result.setInvalid();
        return result;
    }

    private VendorkeyListResponse() {
        this(null, 0);
    }

    public VendorkeyListResponse(String marketName, int size) {
        this.market = marketName;
        this.vendorkeys = new ArrayList<>(size);
    }

    public void add(String vendorkey) {
        this.vendorkeys.add(vendorkey);
    }

    public void addAll(List<String> vendorkeys) {
        this.vendorkeys.addAll(vendorkeys);
    }

    public List<String> getVendorkeys() {
        return vendorkeys;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        if (this.market != null) {
            sb.append(", market=").append(this.market);
        }
        if (!this.vendorkeys.isEmpty()) {
            sb.append(", vendorkeys=")
                    .append(this.vendorkeys.subList(0, Math.min(10, this.vendorkeys.size())));
            if (this.vendorkeys.size() > 10) {
                sb.append("#").append(this.vendorkeys.size());
            }
        }
    }
}
