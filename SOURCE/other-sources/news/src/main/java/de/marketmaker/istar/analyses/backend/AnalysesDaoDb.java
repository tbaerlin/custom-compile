/*
 * AnalysesDaoDb.java
 *
 * Created on 23.03.12 15:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.InvalidProtocolBufferException;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

import de.marketmaker.istar.analyses.frontend.AnalysisImpl;

/**
 * DB-Store for analyses;
 *    a table with same structure as for news.
 *
 * @author oflege
 */
public class AnalysesDaoDb extends JdbcDaoSupport implements AnalysesDao {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private class InsertAnalysis extends SqlUpdate {
        public InsertAnalysis() {
            super(getDataSource(),
                    "INSERT INTO tab_analyses (provider, id, analysisdate, data) VALUES (?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.BIGINT));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.BLOB));
            compile();
        }
    }

    private class SelectAnalysisIdsFromTo extends MappingSqlQuery {
        public SelectAnalysisIdsFromTo() {
            super(getDataSource(),
                    "SELECT id FROM tab_analyses WHERE provider = ? AND analysisdate >= ? AND analysisdate < ?");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            compile();
        }

        @Override
        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            return rs.getLong(1);
        }
    }

    private class InsertImage extends SqlUpdate {
        public InsertImage() {
            super(getDataSource(),
                    "INSERT INTO tab_images (provider, analysis, name, data) VALUES (?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.BIGINT));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.BLOB));
            compile();
        }
    }

    private class SelectImage extends MappingSqlQuery {
        private SelectImage() {
            super(getDataSource(),
                    "SELECT data FROM tab_images WHERE provider = ? AND name = ?");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        @Override
        protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getBytes(1);
        }
    }

    private static final int DEFAULT_DELETE_BATCH_SIZE = 40;

    private static final int DEFAULT_SLEEP_BETWEEN_DELETES = 200;

    /**
     * Mysql locks the whole table for deletes; in order not to suspend reads for too long, we
     * split the deletes in chunks of this size and sleep some time between chunks.
     * Default is {@value #DEFAULT_DELETE_BATCH_SIZE}
     */
    private int deleteBatchSize = DEFAULT_DELETE_BATCH_SIZE;

    /**
     * Milliseconds to sleep between batched deletes; makes sure that reads have some time
     * to run. Default is {@value #DEFAULT_SLEEP_BETWEEN_DELETES}
     */
    private long sleepBetweenDeletes = DEFAULT_SLEEP_BETWEEN_DELETES;


    private InsertAnalysis insertAnalysis;

    private SelectAnalysisIdsFromTo selectAnalysisIdsFromTo;

    private InsertImage insertImage;

    private SelectImage selectImage;

    /**
     * Delete a set of analyses from a specific agency.
     *
     * @param provider analyses provider / agency
     * @param ids keys of news to be deleted
     * @return number of news that have been deleted
     */
    @SuppressWarnings("Duplicates")
    @Override
    public int deleteItems(Protos.Analysis.Provider provider, List<Long> ids) {
        int n = 0;
        for (int from = 0; from < ids.size(); from += this.deleteBatchSize) {
            final int to = Math.min(from + this.deleteBatchSize, ids.size());
            n += doDeleteItems(provider, ids.subList(from, to));
            if (to < ids.size()) {
                try {
                    Thread.sleep(this.sleepBetweenDeletes);
                } catch (InterruptedException e) {
                    this.logger.warn("<deleteItems> interrupted!?, returning");
                    Thread.currentThread().interrupt();
                    return n;
                }
            }
        }
        return n;
    }

    /**
     * Process all analyses from a specific provider
     *
     * @param provider the id of the provider
     * @param handler will be invoked for each analyses
     */
    @Override
    public void getAllItems(Protos.Analysis.Provider provider, AnalysisHandler handler) {
        getJdbcTemplate().query(connection -> {
            return prepareSelectAll(connection, provider);
        }, resultSet -> {
            handler.handle(toAnalysis(resultSet));
        });
    }


    @SuppressWarnings("unchecked")
    public List<Long> getIdsFromTo(Protos.Analysis.Provider provider, DateTime from, DateTime to) {
        return this.selectAnalysisIdsFromTo.execute(provider.getNumber(), from.toDate(), to.toDate());
    }

    @Override
    public List<Long> getIdsSince(Protos.Analysis.Provider provider, DateTime dt) {
        //noinspection unchecked
        return getIdsFromTo(provider, dt, new LocalDate().plusWeeks(1).toDateTimeAtStartOfDay());
    }

    @Override
    public List<AnalysisImpl> getItems(Protos.Analysis.Provider provider, List<Long> ids) {
        final StringBuilder sb = new StringBuilder(ids.size() * 12 + 40)
                .append("SELECT id, data FROM tab_analyses");
        sb.append(" WHERE provider=").append(provider.getNumber());
        appendIds(sb, ids);
        final String sql = sb.toString();

        // the database is not required to return records in the same order as keys appear in the
        // IN clause, so we collect records in a map and create the result later:
        //noinspection unchecked
        final Map<Long, AnalysisImpl> byId = new HashMap<>();
        getJdbcTemplate().query(sql, (ResultSetExtractor) rs -> {
            while (rs.next()) {
                byId.put(rs.getLong(1), new AnalysisImpl(rs.getBytes(2)));
            }
            return byId;
        });

        final List<AnalysisImpl> result = new ArrayList<>(ids.size());
        for (Long id : ids) {
            final AnalysisImpl analysis = byId.get(id);
            if (analysis != null) {
                result.add(analysis);
            }
            else {
                this.logger.warn("<getItems> analysis not found: " + id);
            }
        }
        return result;
    }

    @Override
    public boolean insertAnalysis(Protos.Analysis item) {
        try {
            this.insertAnalysis.update(item.getProvider().getNumber(), item.getId(),
                    new Date(item.getAgencyDate()), item.toByteArray());
            return true;
        } catch (DataIntegrityViolationException e) {
            this.logger.warn("<insertAnalysis> duplicate key for "
                    + item.getProvider() + "/"
                    + item.getId() + "/"
                    + Long.toString(item.getId(), Character.MAX_RADIX));
            return false; // duplicate key, we already stored (and indexed) this news
        } catch (DataAccessException e) {
            this.logger.error("<store> failed", e);
            return false;
        }
    }

    @Override
    public boolean insertImage(Protos.Analysis.Provider provider, long analysis, String name, byte[] data) {
        try {
            this.insertImage.update(provider.getNumber(), analysis, name, data);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false; // duplicate key, we already stored (and indexed) this image?!
        } catch (DataAccessException e) {
            this.logger.error("<insertImage> failed", e);
            return false;
        }
    }

    @Override
    public byte[] getImage(Protos.Analysis.Provider provider, String id) {
        return (byte[]) this.selectImage.findObject(provider.getNumber(), id);
    }

    public void setDeleteBatchSize(int deleteBatchSize) {
        this.deleteBatchSize = deleteBatchSize;
        this.logger.info("<setDeleteBatchSize> deleteBatchSize = " + this.deleteBatchSize);
    }

    public void setSleepBetweenDeletes(long sleepBetweenDeletes) {
        this.sleepBetweenDeletes = sleepBetweenDeletes;
        this.logger.info("<setSleepBetweenDeletes> sleepBetweenDeletes = " + this.sleepBetweenDeletes);
    }

    @Override
    protected void initDao() throws Exception {
        super.initDao();
        this.insertAnalysis = new InsertAnalysis();
        this.selectAnalysisIdsFromTo = new SelectAnalysisIdsFromTo();
        this.insertImage = new InsertImage();
        this.selectImage = new SelectImage();
    }

    private void appendIds(StringBuilder sb, List<Long> ids) {
        sb.append(" AND id IN (0"); // add dummy to simplify sql creation
        for (long id : ids) {
            sb.append(",").append(id);
        }
        sb.append(")");
    }

    private int doDeleteItems(final Protos.Analysis.Provider provider, final List<Long> ids) {
        final int[] results = getJdbcTemplate().batchUpdate(
                "DELETE LOW_PRIORITY FROM tab_analyses WHERE provider = ? AND id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        preparedStatement.setInt(1, provider.getNumber());
                        preparedStatement.setLong(2, ids.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return ids.size();
                    }
                });
        int n = 0;
        for (int result : results) {
            n += result;
        }
        return n;
    }

    /**
     * Prepares a statement that will select all analyses. Since mysql's default behaviour is to
     * retrieve ResultSets completely and store them in memory, which cannot be done with a
     * data set this large, set parameters to tell the driver to stream the results one by one
     * @param connection used for selecting all records
     * @param provider the provider's id
     * @return prepared statement
     * @throws SQLException if preparing fails
     * @see <a href="http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-implementation-notes.html">mysql doc</a>
     */
    private PreparedStatement prepareSelectAll(Connection connection,
            Protos.Analysis.Provider provider) throws SQLException {
        final String sql = "SELECT data FROM tab_analyses WHERE provider=" + provider.getNumber();

        final PreparedStatement result = connection.prepareStatement(sql,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        result.setFetchSize(Integer.MIN_VALUE);
        return result;
    }

    /**
     *
     * @param rs ResultSet from DB
     * @return protobuf analyses  object
     * @throws SQLException
     */
    private Protos.Analysis toAnalysis(ResultSet rs) throws SQLException {
        try {
            return Protos.Analysis.newBuilder().mergeFrom(rs.getBytes(1)).build();
        } catch (InvalidProtocolBufferException e) {
            throw new SQLException(e);
        }
    }
}
