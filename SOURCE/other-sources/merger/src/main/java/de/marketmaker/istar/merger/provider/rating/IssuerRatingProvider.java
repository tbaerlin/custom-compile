/*
 * IssuerRatingProvider.java
 *
 * Created on 07.05.12 11:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import java.util.List;
import java.util.Map;

/**
 * @author zzhao
 */
public interface IssuerRatingProvider {

    public Map<IssuerRatingMetaDataKey, List<Object>> getMetaData(boolean withDetailedSymbol);

    public IssuerRatingSearchResponse search(IssuerRatingSearchRequest req);
}
