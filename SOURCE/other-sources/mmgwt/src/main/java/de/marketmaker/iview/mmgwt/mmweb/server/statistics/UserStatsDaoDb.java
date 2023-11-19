/*
 * UserStatsDaoDb.java
 *
 * Created on 09.12.2009 09:01:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.statistics;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.servlet.http.HttpServletRequest;

import de.marketmaker.iview.mmgwt.mmweb.client.UserRequest;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.object.BatchSqlUpdate;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.ClassUtils;

import de.marketmaker.istar.domain.profile.UserMasterData;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.PageStatsCommand;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.PlaceStatistics;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.StatsResult;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.TopStatsCommand;
import de.marketmaker.iview.mmgwt.mmweb.server.ClientConfig;

/**
 * Dao object for storing/retrieving usage statistics.
 * @author oflege
 */
public class UserStatsDaoDb extends JdbcDaoSupport implements UserStatsDao, Lifecycle,
        DisposableBean {

    private boolean running;

    private class InsertVisit extends SqlUpdate {
        private InsertVisit() {
            super(getDataSource(), "INSERT INTO visits" +
                    " (client, created, user_agent, ip, selector1, selector2, selector3, selector4, screen)" +
                    " VALUES" +
                    " (?, now(), ?, ?, ?, ?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            setReturnGeneratedKeys(true);
            compile();
        }

        public long insert(Object... values) {
            final KeyHolder key = new GeneratedKeyHolder();
            update(values, key);
            return key.getKey().longValue();
        }
    }

    private class InsertProcessedVisit extends BatchSqlUpdate {
        private InsertProcessedVisit() {
            super(getDataSource(), "INSERT INTO visits_processed" +
                    " (id, client, created, ip, user_agent, user_os, selector1, selector2, selector3, selector4)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.BIGINT));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
            setBatchSize(50);
        }
    }

    private class SelectVisit extends MappingSqlQuery {
        private SelectVisit(boolean  processed) {
            super(getDataSource(), "SELECT id, client, created, ip, user_agent," +
                    " selector1, selector2, selector3, selector4" +
                    " FROM " + (processed ? "visits_processed" : "visits") +
                    " WHERE created > ? and created < ?");
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            final Visit result = new Visit();
            result.setId(rs.getInt(1));
            result.setClient(rs.getInt(2));
            result.setCreated(rs.getTimestamp(3).getTime());
            result.setIp(rs.getString(4));
            result.setUserAgent(rs.getString(5));
            result.setSelector1(rs.getString(6));
            result.setSelector2(rs.getString(7));
            result.setSelector3(rs.getString(8));
            result.setSelector4(rs.getString(9));
            return result;
        }
    }

    private class InsertPageVisit extends BatchSqlUpdate {
        private InsertPageVisit() {
            super(getDataSource(), "INSERT INTO page_visits" +
                    " (visit, date, page, num_requests, num_blocks," +
                    " request_time, max_request_time, process_time, max_process_time)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.NUMERIC));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
            setBatchSize(20);
        }
    }

    private class InsertPageAggregation extends BatchSqlUpdate {
        private InsertPageAggregation() {
            super(getDataSource(), "INSERT INTO page_aggregations" +
                    " (client, page_def, day, num," +
                    "  selector1, selector2, selector3, selector4)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.NUMERIC));
            declareParameter(new SqlParameter(Types.NUMERIC));
            declareParameter(new SqlParameter(Types.DATE));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
            setBatchSize(20);
        }

        private void insert(Date d, PageAggregation pa) {
            update(new Object[]{
                    pa.getClientId(), pa.getPageDefId(), d, pa.getNum(),
                    pa.getSelector1(), pa.getSelector2(), pa.getSelector3(), pa.getSelector4()
            });
        }
    }

    private class DeletePageAggregations extends SqlUpdate {
        private DeletePageAggregations() {
            super(getDataSource(), "DELETE FROM page_aggregations WHERE day = ?");
            declareParameter(new SqlParameter(Types.DATE));
            compile();
        }
    }

    private class DeleteProcessedVisits extends SqlUpdate {
        private DeleteProcessedVisits() {
            super(getDataSource(), "DELETE FROM visits_processed WHERE created >= ? AND created < ?");
            declareParameter(new SqlParameter(Types.DATE));
            declareParameter(new SqlParameter(Types.DATE));
            compile();
        }
    }

    private InsertVisit insertVisit;

    private InsertProcessedVisit insertProcessedVisit;

    private SelectVisit selectVisit;

    private SelectVisit selectProcessedVisit;

    private InsertPageVisit insertPageVisit;

    private InsertPageAggregation insertPageAggregation;

    private DeletePageAggregations deletePageAggregations;

    private DeleteProcessedVisits deleteProcessedVisits;

    private final ExecutorService es = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new Thread(r, ClassUtils.getShortName(UserStatsDaoDb.class));
        }
    });

    @Override
    protected void initDao() throws Exception {
        super.initDao();
        this.insertVisit = new InsertVisit();
        this.insertProcessedVisit = new InsertProcessedVisit();
        this.selectVisit = new SelectVisit(false);
        this.selectProcessedVisit = new SelectVisit(true);
        this.insertPageVisit = new InsertPageVisit();
        this.insertPageAggregation = new InsertPageAggregation();
        this.deletePageAggregations = new DeletePageAggregations();
        this.deleteProcessedVisits = new DeleteProcessedVisits();
    }

    public void destroy() throws Exception {
        this.insertPageVisit.flush();
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    public void start() {
        this.running = true;
    }

    public void stop() {
        this.es.shutdown();
    }

    public long insertVisit(HttpServletRequest request, ClientConfig client, UserMasterData data, UserRequest userRequest) {
        final int clientId = client.getId();
        final String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 255) {
            userAgent = userAgent.substring(0, 255);
        }
        final String[] selectors = client.getUserSelectors(data);
        final String screenInfo = userRequest.getScreenInfo();
        return this.insertVisit.insert(clientId, userAgent, ip,
                selectors[0], selectors[1], selectors[2], selectors[3],
                screenInfo);
    }

    public void insertStats(final long sessionId, final List<PlaceStatistics> stats) {
        this.es.submit(new Runnable() {
            public void run() {
                doInsertStats(sessionId, stats);
            }
        });
    }

    private void doInsertStats(long sessionId, List<PlaceStatistics> stats) {
        for (PlaceStatistics stat : stats) {
            try {
                this.insertPageVisit.update(new Object[]{
                        sessionId, new Date((long) stat.getTimestamp()),
                        stat.getHistoryToken(),
                        stat.getNumRequests(), stat.getNumBlocks(),
                        stat.getRequestTime(), stat.getMaxRequestTime(),
                        stat.getProcessTime(), stat.getMaxProcessTime()
                });
            } catch (DataAccessException e) {
                this.logger.warn("<doInsertStats> failed", e);
            }
        }
    }

    public PageDefinitions getPageDefinitions(final int client) {
        final PageDefinitions result = new PageDefinitions();
        final String sql = "SELECT id, name, pattern, module FROM page_defs" +
                    " WHERE client is NULL OR client = " + client + " ORDER BY priority asc";
        getJdbcTemplate().query(sql,
                new RowCallbackHandler() {
                    public void processRow(ResultSet rs) throws SQLException {
                        final PageDefinition pd = new PageDefinition();
                        pd.setId(rs.getInt(1));
                        pd.setName(rs.getString(2));
                        pd.setPattern(rs.getString(3));
                        pd.setModule(rs.getString(4));
                        result.add(pd);
                    }
                }
        );
        return result;
    }

    public UserAgentDefinitions getUserAgentDefinitions() {
        final UserAgentDefinitions result = new UserAgentDefinitions();
        getJdbcTemplate().query("SELECT name, pattern FROM user_agent_defs ORDER BY priority asc",
                new RowCallbackHandler() {
                    public void processRow(ResultSet rs) throws SQLException {
                        result.add(rs.getString(1), rs.getString(2));
                    }
                }
        );
        return result;
    }

    public UserAgentDefinitions getUserOSDefinitions() {
        final UserAgentDefinitions result = new UserAgentDefinitions();
        getJdbcTemplate().query("SELECT name, pattern FROM user_os_defs ORDER BY priority asc",
                new RowCallbackHandler() {
                    public void processRow(ResultSet rs) throws SQLException {
                        result.add(rs.getString(1), rs.getString(2));
                    }
                }
        );
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<String> getSelectors(int client, final String... higherOrderSelectors) {
        final StringBuilder sb = new StringBuilder();
        final String col = "selector" + (higherOrderSelectors.length + 1);
        sb.append("SELECT distinct(").append(col).append(") s")
                .append(" FROM visits_processed WHERE client=").append(client)
                .append(" AND ").append(col).append(" IS NOT NULL");
        for (int i = 0; i < higherOrderSelectors.length; i++) {
            final String s = higherOrderSelectors[i];
            if (s != null) {
                sb.append(" AND selector").append(i + 1).append("=?");
            }
        }
        sb.append(" ORDER BY s");
        return getJdbcTemplate().query(sb.toString(), new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                for (int i = 0; i < higherOrderSelectors.length; i++) {
                    if (higherOrderSelectors[i] != null) {
                        ps.setString(i + 1, higherOrderSelectors[i]);
                    }
                }
            }
        }, new RowMapper() {
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString(1);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public List<Visit> getVisits(LocalDate day) {
        return this.selectVisit.execute(new Object[]{
                day.toDateTimeAtStartOfDay().toDate(),
                day.plusDays(1).toDateTimeAtStartOfDay().toDate()
        });
    }

    @SuppressWarnings("unchecked")
    public List<Visit> getProcessedVisits(LocalDate day) {
        return this.selectProcessedVisit.execute(new Object[]{
                day.minusDays(1).toDateTimeAtStartOfDay().toDate(),
                day.plusDays(1).toDateTimeAtStartOfDay().toDate()
        });
    }

    public void selectPageVisits(final LocalDate day, final StatsAggregator aggregator) {
        final String sql = "SELECT visit, page FROM page_visits WHERE date >= ? AND date < ?";

        getJdbcTemplate().query(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(
                    Connection connection) throws SQLException {
                final PreparedStatement result = connection.prepareStatement(sql,
                        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                result.setFetchSize(Integer.MIN_VALUE);
                result.setTimestamp(1, new Timestamp(day.toDateTimeAtStartOfDay().getMillis()));
                result.setTimestamp(2, new Timestamp(day.plusDays(1).toDateTimeAtStartOfDay().getMillis()));
                return result;
            }

        }, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                aggregator.handlePageVisit(rs.getLong(1), rs.getString(2));
            }
        });
    }

    public void insertAggregations(LocalDate day, Collection<PageAggregation> aggregations) {
        final Date date = toDate(day);
        this.deletePageAggregations.update(date);
        for (PageAggregation aggregation : aggregations) {
            this.insertPageAggregation.insert(date, aggregation);
        }
        this.insertPageAggregation.flush();
    }

    public void insertProcessedVisits(LocalDate day, Collection<Visit> visits) {
        this.deleteProcessedVisits.update(toDate(day), toDate(day.plusDays(1)));
        for (Visit visit : visits) {
            this.insertProcessedVisit.update(visit.getId(),
                    visit.getClient(),
                    new Date(visit.getCreated()),
                    visit.getIp(),
                    visit.getUserAgent(),
                    visit.getUserOS(),
                    visit.getSelector1(),
                    visit.getSelector2(),
                    visit.getSelector3(),
                    visit.getSelector4());
        }
        this.insertProcessedVisit.flush();
    }

    private Date toDate(LocalDate day) {
        return new Date(day.toDateTimeAtStartOfDay().getMillis());
    }

    public StatsResult getStats(PageStatsCommand command) {
        final PageStatsSqlBuilder b = new PageStatsSqlBuilder(command);
        return (StatsResult) getJdbcTemplate().query(b, b, b);
    }

    public StatsResult getStats(TopStatsCommand command) {
        final TopStatsSqlBuilder b = new TopStatsSqlBuilder(command);
        return (StatsResult) getJdbcTemplate().query(b, b, b);
    }

    public static void main(String[] args) {
        final SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setPassword("merger");
        ds.setUsername("merger");
        ds.setUrl("jdbc:mysql://neutron/gwtuser");

        final UserStatsDaoDb dao = new UserStatsDaoDb();
        dao.setDataSource(ds);
        dao.afterPropertiesSet();

        final PageDefinitions pageDefinitions = dao.getPageDefinitions(1);
        System.out.println(pageDefinitions);

        System.out.println(pageDefinitions.getDefinitionFor("P_V"));
        System.out.println(pageDefinitions.getDefinitionFor("P_STK/8.qid/C"));
        System.out.println(pageDefinitions.getDefinitionFor("P_V/5013"));

        ds.destroy();
    }
}
