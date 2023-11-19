/*
 * IssuerRatingSearchRequest.java
 *
 * Created on 07.05.12 11:33
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import java.util.List;

import de.marketmaker.istar.merger.provider.IstarQueryListRequest;

/**
 * @author zzhao
 */
public class IssuerRatingSearchRequest extends IstarQueryListRequest {

    private static final long serialVersionUID = -5288825960912075041L;

    private final boolean withDetailedSymbol;

    private final List<IssuerRatingMetaDataKey> metaDataKeys;

    public IssuerRatingSearchRequest(int offset, int count, String sortBy, boolean ascending,
            String query, boolean withDetailedSymbol, List<IssuerRatingMetaDataKey> metaDataKeys) {
        super(offset, count, sortBy, ascending, query);
        this.withDetailedSymbol = withDetailedSymbol;
        this.metaDataKeys = metaDataKeys;
    }

    public boolean isWithDetailedSymbol() {
        return withDetailedSymbol;
    }

    public List<IssuerRatingMetaDataKey> getMetaDataKeys() {
        return metaDataKeys;
    }
}
