/*
 * NewsDaoDb.java
 *
 * Created on 15.03.2007 13:25:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

import de.marketmaker.istar.fast.FastReadBuffer;
import de.marketmaker.istar.fast.FastWriteBuffer;
import de.marketmaker.istar.fast.U32Encoder;
import de.marketmaker.istar.feed.vwd.SnapRecordVwd;
import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsRecord;

/**
 * NewsDao implementation that uses a database.
 * The Data Objects contains the record blob plus the pre-decoded text content of the news
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsDaoDb extends JdbcDaoSupport implements NewsDao {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private class InsertNewsOperation extends SqlUpdate {
        public InsertNewsOperation() {
            super(getDataSource(), "INSERT INTO `tab_news` (`id`, `previousid`, `newsdate`, `data`, `story`, `rawstory`) "
                                       + " VALUES (?, ?, ?, ?, compress(?), compress(?))");
            declareParameter(new SqlParameter(Types.BIGINT));
            declareParameter(new SqlParameter(Types.BIGINT));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.BLOB));
            declareParameter(new SqlParameter(Types.BLOB));
            declareParameter(new SqlParameter(Types.BLOB));
            compile();
        }
    }

    private class SelectNewsIdOperation extends MappingSqlQuery<String> {
        public SelectNewsIdOperation() {
            super(getDataSource(), "SELECT `id` FROM `tab_news` WHERE `newsdate` >= ? AND `newsdate` < ?");
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            compile();
        }

        @Override
        protected String mapRow(ResultSet rs, int i) throws SQLException {
            return NewsRecordBuilder.encodeId(rs.getLong(1));
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

    private InsertNewsOperation insertNewsOperation;

    private SelectNewsIdOperation selectNewsIdOperation;

    /**
     * Milliseconds to sleep between batched deletes; makes sure that reads have some time
     * to run. Default is {@value #DEFAULT_SLEEP_BETWEEN_DELETES}
     */
    private long sleepBetweenDeletes = DEFAULT_SLEEP_BETWEEN_DELETES;

    @SuppressWarnings("Duplicates")
    @Override
    public int deleteItems(List<String> ids) {
        int n = 0;
        for (int from = 0; from < ids.size(); from += this.deleteBatchSize) {
            final int to = Math.min(from + this.deleteBatchSize, ids.size());
            n += doDeleteItems(ids.subList(from, to));
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

    @Override
    public void getAllItems(NewsRecordHandler handler, int limit, boolean withRawText) {
        getJdbcTemplate().query(
           connection -> {
              return prepareSelectAll(connection, limit, withRawText);
           },
           resultSet -> {
              final NewsRecordImpl record = toNewsRecord(resultSet, true, withRawText);
              handler.handle(record);
           });
    }

    @Override
    public List<String> getIdsFromTo(DateTime from, DateTime to) {
        return this.selectNewsIdOperation.execute(from.toDate(), to.toDate());
    }

    @Override
    public List<String> getIdsSince(DateTime dt) {
        final DateTime to = new DateTime().plusWeeks(1).withTimeAtStartOfDay();
        return getIdsFromTo(dt, to);
    }

    @Override
    public List<NewsRecord> getItems(List<String> ids, boolean withText, boolean withRawText) {
        final StringBuilder sb = createSelectStatement(ids.size() * 12 + 40, withText, withRawText);
        appendIds(sb, ids);
        final String sql = sb.toString();

        // the database is not required to return records in the same order as keys appear in the
        // IN clause, so we collect records in a map and create the result later:
        //noinspection unchecked
        final Map<String, NewsRecord> recordsById = getJdbcTemplate().query(sql, rs -> {
            final Map<String, NewsRecord> result = new HashMap<>();
            while (rs.next()) {
                final NewsRecordImpl value = toNewsRecord(rs, withText, withRawText);
                result.put(value.getId(), value);
            }
            return result;
        });

        final List<NewsRecord> result = new ArrayList<>(ids.size());
        result.addAll(ids.stream().map(recordsById::get).collect(Collectors.toList()));
        return result;
    }

    @Override
    public boolean insertItem(NewsRecordImpl item) {
        try {
            final byte[] blob = toBlob(item.getSnapRecord());
            String previousId = item.getPreviousId();
            this.insertNewsOperation.update(
                    NewsRecordBuilder.decodeId(item.getId()),
                    previousId != null ? NewsRecordBuilder.decodeId(previousId) : null,
                    item.getTimestamp().toDate(),
                    blob,
                    item.getStory(),
                    item.getRawStory());
            return true;
        } catch (DataIntegrityViolationException e) {
            return false; // duplicate key, we already stored (and indexed) this news
        } catch (DataAccessException e) {
            this.logger.error("<store> failed", e);
            return false;
        }
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
        this.insertNewsOperation = new InsertNewsOperation();
        this.selectNewsIdOperation = new SelectNewsIdOperation();
    }

    byte[] toBlob(SnapRecordVwd snap) {
        final byte[] data = snap.getData();
        final int[] fieldids = snap.getFieldids();
        final int[] offsets = snap.getOffsets();

        // large enough to store everything, but actual result may be shorter
        final FastWriteBuffer fwb = new FastWriteBuffer(data.length + 2 * fieldids.length * 2 + 2);
        final U32Encoder enc = new U32Encoder();
        enc.encode(fwb, fieldids.length);
        for (int i = 0; i < fieldids.length; i++) {
            enc.encode(fwb, fieldids[i]);
            enc.encode(fwb, offsets[i]);
        }
        fwb.put(data);
        // return just as many bytes as have been added to fwb
        return fwb.flip().toArray();
    }

    SnapRecordVwd toSnapRecord(byte[] data) {
        final FastReadBuffer frb = new FastReadBuffer(data, 0, data.length);
        final U32Encoder dec = new U32Encoder();
        final int len = dec.decode(frb);
        final int fieldids[] = new int[len];
        final int offsets[] = new int[len];
        for (int i = 0; i < len; i++) {
            fieldids[i] = dec.decode(frb);
            offsets[i] = dec.decode(frb);
        }
        return new SnapRecordVwd(fieldids, offsets, Arrays.copyOfRange(data, frb.position(), data.length));
    }

    private void appendIds(StringBuilder sb, List<String> ids) {
        sb.append(" WHERE `id` IN (");
        boolean anyId = false;
        for (String idStr : ids) {
            try {
                final long id = NewsRecordBuilder.decodeId(idStr);
                if (anyId) {
                    sb.append(",");
                }
                sb.append(id);
                anyId = true;
            } catch (NumberFormatException e) {
                // ignore invalid id
            }
        }
        if (!anyId) {
            sb.append("0"); // no valid ids, so add 0 to create valid sql
        }
        sb.append(")");
    }

    /**
     * return a select for the news table, the news record is in data, optionally we can get the
     * pre-decoded story/rawstory from the table
     *
     * @param capacity estimated sql-string length
     * @param withText add sql for returning text
     * @param withRawText add sql for returning rawText
     * @return select string to retrieve a complete table entry
     */
    private StringBuilder createSelectStatement(int capacity, boolean withText, boolean withRawText) {
        final StringBuilder result = new StringBuilder(capacity);
        result.append("SELECT `id`, `previousid`, `data`");
        if (withText) {
            result.append(", uncompress(`story`)");
        }
        if (withRawText) {
            result.append(", uncompress(`rawstory`)");
        }
        result.append(" FROM `tab_news`");
        return result;
    }

    private int doDeleteItems(List<String> ids) {
        final int[] results = getJdbcTemplate().batchUpdate(
                "DELETE LOW_PRIORITY FROM `tab_news` WHERE `id` = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        preparedStatement.setLong(1, NewsRecordBuilder.decodeId(ids.get(i)));
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
     * Prepares a statement that will select all news. Since mysql's default behaviour is to
     * retrieve ResultSets completely and store them in memory, which cannot be done with a
     * data set this large, set parameters to tell the driver to stream the results one by one
     * @param connection used for selecting all records
     * @param limit limits the number of results if &gt; 0;
     * @return prepared statement
     * @throws SQLException if preparing fails
     * @see <a href="http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-implementation-notes.html">mysql doc</a>
     */
    private PreparedStatement prepareSelectAll(Connection connection, int limit, boolean withRawText) throws SQLException {
        final StringBuilder sb = createSelectStatement(50, true, withRawText);
        if (limit > 0) {
            sb.append(" LIMIT ").append(limit);
        }
        final String sql = sb.toString();

        final PreparedStatement result = connection.prepareStatement(sql,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        result.setFetchSize(Integer.MIN_VALUE);
        return result;
    }

    private NewsRecordImpl toNewsRecord(ResultSet rs, boolean withText, boolean withRawText) throws SQLException {
        int col = 1;
        final String id = NewsRecordBuilder.encodeId(rs.getLong(col++));
        long previousIdInDb = rs.getLong(col++);
        final String previousId = rs.wasNull() ? null : NewsRecordBuilder.encodeId(previousIdInDb);
        final byte[] data = rs.getBytes(col++);
        final SnapRecordVwd snapRecord = toSnapRecord(data);
        final byte[] story = withText ? rs.getBytes(col++) : null;
        @SuppressWarnings("UnusedAssignment")
        final byte[] rawStory = withRawText ? rs.getBytes(col++) : null;
        return new NewsRecordImpl(id, previousId, snapRecord, story, rawStory);
    }
}
