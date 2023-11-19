/*
 * PageAggregator.java
 *
 * Created on 11.01.2010 17:03:13
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import de.marketmaker.iview.mmgwt.mmweb.server.ClientConfig;
import de.marketmaker.iview.mmgwt.mmweb.server.UserDao;
import de.marketmaker.iview.mmgwt.mmweb.server.UserDaoDb;

/**
 * Processes statistics by writing data from "live" tables, where inserts happen for every
 * visit or page_visit, into tables that are used for evaluation only. For visits, the
 * user agents are normalized, for page_visits, the records aggregated.
 * Expected to be run daily for the previous day, but can also be configured to run process
 * all days between two given dates.
 * @author oflege
 */
public class StatsAggregator implements InitializingBean {
    private final Log logger = LogFactory.getLog(getClass());

    private UserStatsDao statsDao;

    private UserDao userDao;

    private LocalDate from;

    private LocalDate to;

    private LocalDate day;

    private final Map<Integer, PageDefinitions> pageDefintionsByClient
            = new HashMap<>();

    private final Map<PageAggregation, PageAggregation> pageAggregations
            = new HashMap<>();

    private int numPageVisits = 0;

    private Set<String> unknownVisits = new HashSet<>();

    private Set<Integer> unknownClients = new HashSet<>();

    private Set<String> unknownPages = new HashSet<>();

    private Map<Long, Visit> visitsById;

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public void setDay(LocalDate day) {
        this.from = day;
        this.to = day.plusDays(1);
    }

    public void setStatsDao(UserStatsDao statsDao) {
        this.statsDao = statsDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void afterPropertiesSet() throws Exception {
        loadPageDefinitions();

        this.day = this.from;
        while (this.day.isBefore(this.to)) {
            process();

            this.day = this.day.plusDays(1);    
            this.pageAggregations.clear();

            Thread.sleep(5000);
        }

        if (!this.unknownClients.isEmpty()) {
            this.logger.warn("<afterPropertiesSet> unknown clients: " + this.unknownClients);
        }
        if (!this.unknownPages.isEmpty()) {
            this.logger.warn("<afterPropertiesSet> unknown pages: " + this.unknownPages);
        }
    }

    private void process() {
        this.logger.info("<process> " + this.day);
        processVisits();
        aggregatePageVisits();
    }

    private void aggregatePageVisits() {
        this.visitsById = loadProcessedVisits();
        this.statsDao.selectPageVisits(this.day, this);
        this.logger.info("<aggregatePageVisits> selected " + numPageVisits + " visits resulting in "
                + this.pageAggregations.size() + " aggregated records, (orphan=" + this.unknownVisits + ")");
        this.statsDao.insertAggregations(this.day, this.pageAggregations.values());
        this.logger.info("<aggregatePageVisits> inserted aggregations");
        this.numPageVisits = 0;
        this.unknownVisits.clear();
    }

    private void processVisits() {
        final UserAgentDefinitions userAgentDefinitions = this.statsDao.getUserAgentDefinitions();
        final UserAgentDefinitions userOSDefinitions = this.statsDao.getUserOSDefinitions();
        final List<Visit> visits = this.statsDao.getVisits(this.day);
        this.logger.info("<processVisits> read #" + visits.size() + " visits");
        resolveUserOS(userOSDefinitions, visits);
        resolveUserAgents(userAgentDefinitions, visits);
        this.statsDao.insertProcessedVisits(this.day, visits);
    }

    private void resolveUserAgents(UserAgentDefinitions userAgentDefinitions, List<Visit> visits) {
        for (Visit visit : visits) {
            final String userAgent = visit.getUserAgent();
            final String resolved = userAgentDefinitions.resolveName(userAgent);
            if (resolved != null) {
                visit.setUserAgent(resolved);
            }
            else {
                this.logger.info("<processVisits> unknown user agent: " + userAgent);
                visit.setUserAgent("Unbekannt");
            }
        }
    }

    private void resolveUserOS(UserAgentDefinitions userOSDefinitions, List<Visit> visits) {
        for (Visit visit : visits) {
            final String userOS = visit.getUserAgent();
            final String resolved = userOSDefinitions.resolveName(userOS);
            if (resolved != null) {
                visit.setUserOS(resolved);
            }
            else {
                this.logger.info("<processVisits> unknown user OS: " + userOS);
                visit.setUserOS("Unbekannt");
            }
        }
    }

    private Map<Long, Visit> loadProcessedVisits() {
        final Map<Long, Visit> result = new HashMap<>();
        final List<Visit> visits = this.statsDao.getProcessedVisits(this.day);
        for (Visit visit : visits) {
            if (this.pageDefintionsByClient.containsKey(visit.getClient())) {
                result.put(visit.getId(), visit);
            }
            else {
                this.unknownClients.add(visit.getClient());
            }
        }
        return result;
    }

    private void loadPageDefinitions() {
        final Map<String, ClientConfig> configs = this.userDao.getClientConfigs();
        for (ClientConfig config : configs.values()) {
            addPageDefinitions(config);
        }
    }

    private void addPageDefinitions(ClientConfig config) {
        final PageDefinitions pds = this.statsDao.getPageDefinitions(config.getId());
        this.pageDefintionsByClient.put(config.getId(), pds);
    }

    public void handlePageVisit(long visitId, String page) {
        if (page.length() == 0) {
            return;
        }
        final Visit visit = this.visitsById.get(visitId);
        if (visit == null) {
            this.unknownVisits.add(Long.toString(visitId));
            return;
        }
        final PageAggregation aggregation = getAggregation(visit, page);
        aggregation.incNum();
        this.numPageVisits++;
    }

    private PageAggregation getAggregation(Visit visit, String page) {
        final PageDefinition pageDefinition
                = this.pageDefintionsByClient.get(visit.getClient()).getDefinitionFor(page);
        final PageAggregation pa = createAggregation(page, visit, pageDefinition);
        return resolveAggregation(pa);
    }

    private PageAggregation createAggregation(String page, Visit visit, PageDefinition pd) {
        if (pd.getId() == 0) {
            this.unknownPages.add(page);
        }
        return new PageAggregation(visit, pd.getId());
    }

    private PageAggregation resolveAggregation(PageAggregation pa) {
        final PageAggregation existing = this.pageAggregations.get(pa);
        if (existing != null) {
            return existing;
        }
        this.pageAggregations.put(pa, pa);
        return pa;
    }

    public static void main(String[] args) throws Exception {
        String host = "msgsrv1";
        LocalDate day = new LocalDate().minusDays(1);
        LocalDate from = null;
        LocalDate to = new LocalDate();

        int i = 0;
        while (i < (args.length - 1) && args[i].startsWith("-")) {
            String key = args[i++];
            String value = args[i++];
            if ("-h".equals(key)) {
                host = value;
            }
            else if ("-d".equals(key)) {
                day = parseDay(value);
            }
            else if ("-f".equals(key)) {
                from = parseDay(value);
            }
            else if ("-t".equals(key)) {
                to = parseDay(value);
            }
        }


        final SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setPassword("merger");
        ds.setUsername("merger");
        ds.setUrl("jdbc:mysql://" + host + "/gwtuser");

        final UserStatsDaoDb statsDao = new UserStatsDaoDb();
        statsDao.setDataSource(ds);
        statsDao.afterPropertiesSet();

        final UserDaoDb userDao = new UserDaoDb();
        userDao.setDataSource(ds);
        userDao.afterPropertiesSet();

        final StatsAggregator aggregator = new StatsAggregator();
        aggregator.setUserDao(userDao);
        aggregator.setStatsDao(statsDao);
        if (from != null) {
            aggregator.setFrom(from);
            aggregator.setTo(to);
        }
        else {
            aggregator.setDay(day);
        }

        aggregator.afterPropertiesSet();
    }

    private static LocalDate parseDay(String value) {
        return DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(value).toLocalDate();
    }
}
