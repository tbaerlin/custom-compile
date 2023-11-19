/*
 * RatingHistoryProvider.java
 *
 * Created on 11.09.12 14:05
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating.history;

import java.util.List;
import java.util.Map;

import de.marketmaker.istar.merger.web.easytrade.FinderMetaItem;

/**
 * @author zzhao
 */
public interface RatingHistoryProvider {

    RatingHistoryResponse getRatingHistory(RatingHistoryRequest req);

    Map<String, List<FinderMetaItem>> getRatingHistoryMetaData();
}
