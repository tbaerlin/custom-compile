/*
 * StatisticsServiceImpl.java
 *
 * Created on 13.01.2010 15:25:49
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.statistics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.marketmaker.istar.domain.profile.UserMasterData;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.PageStatsCommand;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.Pages;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.Selectors;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.StatisticsMetaData;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.StatisticsService;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.StatsResult;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.TopStatsCommand;
import de.marketmaker.iview.mmgwt.mmweb.server.ClientConfig;
import de.marketmaker.iview.mmgwt.mmweb.server.GwtService;
import de.marketmaker.iview.mmgwt.mmweb.server.UserServiceIfc;

/**
 * @author oflege
 */
@SuppressWarnings("GwtServiceNotRegistered")
public class StatisticsServiceImpl extends GwtService implements StatisticsService {
    private UserStatsDao statsDao;

    private UserServiceIfc userService;

    public void setUserService(UserServiceIfc userService) {
        this.userService = userService;
    }

    public void setStatsDao(UserStatsDao statsDao) {
        this.statsDao = statsDao;
    }

    public StatisticsMetaData getMetaData(String module, String vwdId) {
        final ClientConfig config = getConfig(module);

        final StatisticsMetaData result = new StatisticsMetaData();
        result.setSelectors(getSelectors(vwdId, config));
        result.setPages(getPages(config));

        this.logger.info("<getMetaData> " + result);
        return result;
    }

    private ClientConfig getConfig(String module) {
        return this.userService.getConfig(module);
    }

    public StatsResult getStats(PageStatsCommand command) {
        return this.statsDao.getStats(command);
    }

    public StatsResult getStats(TopStatsCommand command) {
        return this.statsDao.getStats(command);
    }

    private Selectors getSelectors(String vwdId, ClientConfig config) {
//        final Profile profile = this.userService.getProfileByVwdId(vwdId, config.getAppId());
//
//        final VwdProfile vwdProfile = (VwdProfile) profile;

        final UserMasterData data = this.userService.getUserMasterData(vwdId, config.getAppId());
        final String[] selectors = config.getUserSelectors(data);

        final Selectors result = new Selectors();
        int n = 0;
        for (ClientConfig.SessionSelectorDescription description : config.getSessionSelectors()) {
            if (userIsAllowedToSelectFrom(n)) {
                final String[] higherOrderSelectors = Arrays.copyOfRange(selectors, 0, n);
                for (int i = 0; i < n; i++) {
                    if (userIsAllowedToSelectFrom(i)) {
                        higherOrderSelectors[i] = null;
                    }
                }
                final List<String> values
                        = this.statsDao.getSelectors(config.getId(), higherOrderSelectors);
                result.add(description.getName(), values);
            }
            else {
                result.add(description.getName(), Collections.singletonList(selectors[n]));
            }
            n++;
        }
        return result;
    }

    private Pages getPages(ClientConfig config) {
        final Pages result = new Pages();
        final PageDefinitions pageDefinitions = this.statsDao.getPageDefinitions(config.getId());
        for (PageDefinition definition : pageDefinitions.getDefinitions()) {
            result.add(Integer.toString(definition.getId()), definition.getName(), definition.getModule());
        }
        return result;
    }

    private boolean userIsAllowedToSelectFrom(int i) {
        // TODO: use Profile to determine if this is allowed
        return true;
    }
}
