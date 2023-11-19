/*
 * StatisticsService.java
 *
 * Created on 13.01.2010 15:21:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author oflege
 */
public interface StatisticsService extends RemoteService {
    StatisticsMetaData getMetaData(String module, String vwdId);

    StatsResult getStats(PageStatsCommand command);

    StatsResult getStats(TopStatsCommand command);
}
