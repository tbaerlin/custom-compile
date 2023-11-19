/*
 * PageDbDao.java
 *
 * Created on 13.06.2005 11:37:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.pages;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.Monitor;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.BatchSqlUpdate;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.connect.BufferWriter;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;

/**
 * Implements PageDao using a database as page store.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@SuppressWarnings("ALL")
public class PageDbDao extends JdbcDaoSupport implements PageDao, BufferWriter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private UpdatePageOnResend updatePageOnResend;

    private UpdatePageOnChange updatePageOnChange;

    private UpdateTextG updateTextG;

    private InsertPage insertPage;

    private SelectPage selectPage;

    private SelectPageHash selectPageHash;

    private SelectPageNumbers selectPageNumbers;

    private SelectPageNumbersChangedAfter selectPageNumbersChangedAfter;

    private SelectPageNumberNeighborhood selectPageNumberNeighborhood;

    private DeletePageKeys deletePageKeys;

    @Monitor(type = COUNTER)
    private AtomicLong numPagesStored = new AtomicLong();

    private boolean insertOnly = false;

    /**
     * We need to record whether a page's text has changed or the old text has just been resent.
     * Since pages are stored using binary text data, the comparison cannot by handled by the
     * database. Instead, we use a message digest for the page's text.
     */
    private MessageDigest md;

    private static final String SELECTPAGE
            = "SELECT pagenumber, text, textg, isdynamic, changed, updated, selectors FROM tab_pages";

    public void setInsertOnly(boolean insertOnly) {
        this.insertOnly = insertOnly;
    }

    private class UpdatePageOnResend extends SqlUpdate {
        public UpdatePageOnResend() {
            super(getDataSource(), "UPDATE tab_pages SET updated=? WHERE pagenumber=?");
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public void update(PageData page) {
            Object[] objs = new Object[]{
                    new Timestamp(page.getTimestamp()),
                    page.getId()
            };
            super.update(objs);
        }
    }

    private class UpdatePageOnChange extends SqlUpdate {
        public UpdatePageOnChange() {
            super(getDataSource(), "UPDATE tab_pages"
                    + " SET updated=?, changed=?, md5text=?,"
                    + " text=?, isdynamic=?, selectors=? WHERE pagenumber=?");
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.LONGVARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public void update(PageData page, String hash) {
            final Timestamp timestamp = new Timestamp(page.getTimestamp());
            Object[] objs = new Object[]{
                    timestamp,
                    timestamp,
                    hash,
                    page.getText(),
                    (page.isDynamic() ? 1 : 0),
                    StringUtils.join(page.getSelectors(), ','),
                    page.getId()
            };
            super.update(objs);
        }
    }

    private class UpdateTextG extends SqlUpdate {
        public UpdateTextG() {
            super(getDataSource(), "UPDATE tab_pages"
                    + " SET updated=?, textg=? "
                    + " WHERE pagenumber=?");
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public void update(PageData page) {
            final Object[] objs = new Object[]{
                    new Timestamp(page.getTimestamp()),
                    page.getTextg(),
                    page.getId()
            };
            super.update(objs);
        }
    }

    private class InsertPage extends SqlUpdate {
        public InsertPage() {
            super(getDataSource(), "INSERT INTO tab_pages"
                    + " (updated, changed, md5text, text, textg, isdynamic, pagenumber, selectors)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.LONGVARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        public void insert(PageData page, String hash) {
            Object[] objs = new Object[]{
                    new Timestamp(page.getTimestamp()),
                    new Timestamp(page.getTimestamp()),
                    hash,
                    page.getText(),
                    page.getTextg(),
                    page.isDynamic() ? 1 : 0,
                    page.getId(),
                    StringUtils.join(page.getSelectors(), ',')
            };
            super.update(objs);
        }
    }

    public class DeletePageKeys extends SqlUpdate {
        public DeletePageKeys() {
            super(getDataSource(), "DELETE FROM tab_page_keys WHERE pagenumber=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }
    }

    /**
     * NOTE: There is no SelectPageKeys, as keys can always be retrieved by parsing the
     * page's text. BUT: MDP needs the tab_page_keys table, so we have to keep it up to date.
     */
    public class InsertPageKeys extends BatchSqlUpdate {
        public InsertPageKeys() {
            super(getDataSource(), "INSERT INTO tab_page_keys (pagenumber, rc3key) VALUES (?, ?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }
    }

    private class SelectPageHash extends MappingSqlQuery<String> {
        public SelectPageHash() {
            super(getDataSource(), "SELECT md5text FROM tab_pages WHERE pagenumber=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        @Override
        protected String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString(1);
        }

        String getHash(int pagenumber) {
            return (String) findObject(pagenumber);
        }
    }

    private class SelectPageNumbers extends MappingSqlQuery<Integer> {
        public SelectPageNumbers() {
            super(getDataSource(), "SELECT pagenumber FROM tab_pages");
            compile();
        }

        @Override
        protected Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt(1);
        }
    }

    private class SelectPageNumbersChangedAfter extends MappingSqlQuery<Integer> {
        public SelectPageNumbersChangedAfter() {
            super(getDataSource(), "SELECT pagenumber FROM tab_pages WHERE changed > ?");
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            compile();
        }

        @Override
        protected Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt(1);
        }

        protected List<Integer> execute(DateTime dt) {
            return execute(dt.toDate());
        }
    }

    /**
     * Get the two pages surrounding a pagenumber. We combine this into one statement to
     * reduce the number of DB accesses
     */
    private class SelectPageNumberNeighborhood extends MappingSqlQuery {
        public SelectPageNumberNeighborhood() {
            super(getDataSource(), "SELECT MIN(pagenumber) FROM tab_pages WHERE pagenumber > ? " +
                    "UNION SELECT MAX(pagenumber) FROM tab_pages WHERE pagenumber < ?");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        @Override
        protected Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            Integer number = rs.getInt(1); // pagenumber
            return rs.wasNull() ? null : number;
        }

        public Neighborhood getNeighborhoodFor(int pagenumber) {
            @SuppressWarnings({"unchecked"})
            final List<Integer> result = execute(new Object[]{pagenumber, pagenumber});
            assert result.size() <= 2;
            Integer next = null;
            Integer prev = null;
            for (Integer p : result) {
                if (p == null) continue;
                if (p < pagenumber) {
                    prev = p;
                }
                else {
                    next = p;
                }
            }
            return new PagenumberNeighborhood(next, prev);
        }
    }

    private class SelectPage extends MappingSqlQuery {
        public SelectPage() {
            super(getDataSource(), SELECTPAGE + " WHERE pagenumber=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        @Override
        protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toPageRecord(rs);
        }

        PageData getPageData(int pagenumber) {
            return (PageData) findObject(pagenumber);
        }
    }

    @Override
    public void initDao() throws Exception {
        this.insertPage = new InsertPage();
        this.updatePageOnResend = new UpdatePageOnResend();
        this.updatePageOnChange = new UpdatePageOnChange();
        this.updateTextG = new UpdateTextG();
        this.selectPage = new SelectPage();
        this.selectPageHash = new SelectPageHash();
        this.selectPageNumbers = new SelectPageNumbers();
        this.selectPageNumbersChangedAfter = new SelectPageNumbersChangedAfter();
        this.selectPageNumberNeighborhood = new SelectPageNumberNeighborhood();
        this.deletePageKeys = new DeletePageKeys();

        this.md = MessageDigest.getInstance("MD5");
    }

    private String getTextMD5(PageData page) {
        if (page.getText() == null) {
            return "0";
        }
        this.md.reset();
        this.md.update(page.getText().getBytes());
        if (page.getSelectors() != null) {
            this.md.update(page.getSelectors().toString().getBytes());
        }
        return ByteUtil.toBase64String(this.md.digest());
    }

    /**
     * non-dynamic pages are sometimes received at almost the same time in the news and the pages
     * feed; w/o synchronization, the two threads could try to insert the page simultaneously,
     * which would trigger a warning. Synchronization avoids that.
     * @param page
     */
    @Override
    public synchronized void store(PageData page) {
        final TimeTaker tt = this.logger.isDebugEnabled() ? new TimeTaker() : null;
        try {
            final String hash = this.selectPageHash.getHash(page.getId());
            final String newHash = getTextMD5(page);
            if (hash == null) {
                this.insertPage.insert(page, newHash);
                storeKeys(page);
            }
            else if (!insertOnly) {
                if (page.getTextg() != null) {
                    this.updateTextG.update(page);
                }
                else if (hash.equals(newHash)) {
                    this.updatePageOnResend.update(page);
                }
                else {
                    this.updatePageOnChange.update(page, newHash);
                    deletePageKeys.update(page.getId());
                    storeKeys(page);
                }
            }
            this.numPagesStored.incrementAndGet();
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<store> page " + page.getId() + " took " + tt);
            }
        } catch (Exception e) {
            this.logger.warn("<store> failed for page " + page, e);
        }
    }

    private void storeKeys(PageData page) {
        if (page.getKeys().isEmpty()) {
            return;
        }
        final InsertPageKeys insertPageKeys = new InsertPageKeys();
        for (String key : page.getKeys()) {
            insertPageKeys.update(page.getId(), key);
        }
        insertPageKeys.flush();
    }

    @Override
    public PageData getPageData(int pagenumber) {
        final TimeTaker tt = this.logger.isDebugEnabled() ? new TimeTaker() : null;

        try {
            final PageData pageData = this.selectPage.getPageData(pagenumber);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getPageData> for " + pagenumber + " took " + tt);
            }
            return pageData;
        } catch (Exception e) {
            this.logger.warn("<getPageData> failed for page " + pagenumber + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public void getAllPages(final Handler handler, final Boolean dynamic) {
        getJdbcTemplate().query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(
                    Connection connection) throws SQLException {
                return prepareSelectAll(connection, dynamic);
            }
        }, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                PageData pageData = toPageRecord(rs);
                handler.handle(pageData);
            }
        });
    }

    private PreparedStatement prepareSelectAll(Connection connection,
            final Boolean dynamic) throws SQLException {
        final StringBuilder sb = new StringBuilder(SELECTPAGE);
        if (dynamic != null) {
            sb.append(" WHERE isdynamic=").append(dynamic ? 1 : 0);
        }
        final String sql = sb.toString();

        final PreparedStatement result = connection.prepareStatement(sql,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        result.setFetchSize(Integer.MIN_VALUE);
        return result;
    }

    @Override
    public List<Integer> getPagenumbers() {
        return this.selectPageNumbers.execute();
    }

    @Override
    public List<Integer> getPagenumbersChangedAfter(DateTime dt) {
        return this.selectPageNumbersChangedAfter.execute(dt);
    }

    @Override
    public Neighborhood getNeighborhood(int pagenumber) {
        return this.selectPageNumberNeighborhood.getNeighborhoodFor(pagenumber);
    }

    public long getNumPagesStored() {
        return this.numPagesStored.get();
    }

    @Override
    public void write(ByteBuffer bb) throws IOException {
        final PageData data = PageData.readFrom(bb);
        store(data);
    }

    private PageData toPageRecord(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp(5);
        if (rs.wasNull()) {
            ts = rs.getTimestamp(6);
        }

        return new PageData(
                rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getInt(4) == 1,
                ts.getTime(),
                toSelectors(rs.getString(7))
        );
    }

    private Set<String> toSelectors(String str) {
        return (str != null) ? new HashSet<>(Arrays.asList(str.split(","))) : null;
    }
}
