/*
 * UserStatsDao.java
 *
 * Created on 07.12.2009 15:10:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.statistics;

import java.util.List;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import de.marketmaker.iview.mmgwt.mmweb.client.UserRequest;
import org.joda.time.LocalDate;

import de.marketmaker.iview.mmgwt.mmweb.client.statistics.StatsResult;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.PageStatsCommand;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.PlaceStatistics;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.TopStatsCommand;
import de.marketmaker.iview.mmgwt.mmweb.server.ClientConfig;
import de.marketmaker.istar.domain.profile.UserMasterData;

/**
 * @author oflege
 */
public interface UserStatsDao {
    public static final String VISIT_ID = "visit_id";

    long insertVisit(HttpServletRequest request, ClientConfig client, UserMasterData data, UserRequest userRequest);

    void insertStats(long sessionId, List<PlaceStatistics> stats);

    PageDefinitions getPageDefinitions(int client);

    List<String> getSelectors(int client, String... higherOrderSelectors);

    List<Visit> getVisits(LocalDate day);

    void selectPageVisits(LocalDate day, StatsAggregator aggregator);

    void insertAggregations(LocalDate day, Collection<PageAggregation> aggregations);

    void insertProcessedVisits(LocalDate day, Collection<Visit> visits);

    StatsResult getStats(PageStatsCommand command);

    StatsResult getStats(TopStatsCommand command);

    UserAgentDefinitions getUserAgentDefinitions();

    UserAgentDefinitions getUserOSDefinitions();

    @SuppressWarnings("unchecked")
    List<Visit> getProcessedVisits(LocalDate day);
}
