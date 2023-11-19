/*
 * BewDaoDb.java
 *
 * Created on 05.10.2010 15:16:06
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.BatchSqlUpdate;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author oflege
 */
public class BewDaoDb extends JdbcDaoSupport implements BewDao {

    private class InsertTask extends SqlUpdate {
        public InsertTask() {
            super(getDataSource(), "INSERT INTO tasks (customer, requestdate, percentage) VALUES (?, now(), 0)");
            declareParameter(new SqlParameter(Types.VARCHAR));
            setReturnGeneratedKeys(true);
            compile();
        }

        public int insert(String name) {
            final KeyHolder key = new GeneratedKeyHolder();
            update(new Object[]{name}, key);
            return key.getKey().intValue();
        }
    }

    private abstract class TaskQuery extends SqlQuery<TaskInfo> {
        protected TaskQuery(String where) {
            super(getDataSource(),
                    "SELECT id, customer, requestdate, percentage, completed from tasks WHERE " + where);
        }

        protected RowMapper<TaskInfo> newRowMapper(Object[] objects, Map map) {
            return (rs, rowNum)
                    -> new TaskInfo(rs.getInt(1), rs.getString(2), rs.getTimestamp(3), rs.getInt(4), rs.getTimestamp(5));
        }
    }

    private class SelectTasks extends TaskQuery {
        private SelectTasks() {
            super("requestdate >= ? AND requestdate < ? and percentage = 100 ORDER BY requestdate ASC");
            declareParameter(new SqlParameter(Types.DATE));
            declareParameter(new SqlParameter(Types.DATE));
            compile();
        }
    }

    private class SelectCustomerTasks extends TaskQuery {
        private SelectCustomerTasks() {
            super("customer = ? AND requestdate >= ? AND requestdate < ? and percentage = 100 ORDER BY requestdate ASC");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.DATE));
            declareParameter(new SqlParameter(Types.DATE));
            compile();
        }
    }

    private class SelectTask extends TaskQuery {
        private SelectTask() {
            super("id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }
    }

    private class UpdateTask extends SqlUpdate {
        public UpdateTask() {
            super(getDataSource(), "UPDATE tasks SET percentage = ?, completed = now() WHERE id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }
    }

    private class SelectSymbols extends MappingSqlQuery {
        private SelectSymbols() {
            super(getDataSource(), "SELECT symbol, exchange, vwd_symbol, vwd_exchange, iid" +
                    " FROM symbols WHERE taskid=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            return new SymbolInfo(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getLong(5));
        }
    }

    private class DeleteSymbols extends SqlUpdate {
        private DeleteSymbols() {
            super(getDataSource(), "DELETE FROM symbols WHERE taskid=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }
    }

    private class InsertSymbols extends BatchSqlUpdate {
        private InsertSymbols() {
            super(getDataSource(), "INSERT INTO symbols (taskid, symbol, exchange, vwd_symbol, vwd_exchange, iid)" +
                    " VALUES (?, ?, ?, ?, ?, ?)", new int[]{
                    Types.INTEGER,
                    Types.VARCHAR,
                    Types.VARCHAR,
                    Types.VARCHAR,
                    Types.VARCHAR,
                    Types.BIGINT
            }, 100);
            compile();
        }

        void insert(int taskId, List<ResultItem> items) {
            for (ResultItem item : items) {
                final Quote q = item.getQuote();
                update(taskId,
                        item.getSymbol(),
                        item.getRequest().getExchange(),
                        (q != null) ? q.getSymbolVwdcode() : null,
                        (q != null) ? q.getSymbolVwdfeedMarket() : null,
                        (q != null) ? q.getInstrument().getId() : null);
            }
            flush();
        }
    }

    private InsertTask insertTask;

    private UpdateTask updateTask;

    private DeleteSymbols deleteSymbols;

    private InsertSymbols insertSymbols;

    private SelectSymbols selectSymbols;

    private SelectTasks selectTasks;

    private SelectCustomerTasks selectCustomerTasks;

    private SelectTask selectTask;

    protected void initDao() throws Exception {
        this.insertTask = new InsertTask();
        this.updateTask = new UpdateTask();
        this.insertSymbols = new InsertSymbols();
        this.deleteSymbols = new DeleteSymbols();
        this.selectSymbols = new SelectSymbols();
        this.selectCustomerTasks = new SelectCustomerTasks();
        this.selectTasks = new SelectTasks();
        this.selectTask = new SelectTask();
    }

    public int createTask(String customer) {
        return this.insertTask.insert(customer);
    }

    @Override
    public TaskInfo getTask(int taskId) {
        return this.selectTask.findObject(taskId);
    }

    @Override
    public void ackTaskProgress(int taskId, int percentage) {
        this.updateTask.update(percentage, taskId);
        if (percentage < 0) {
            this.deleteSymbols.update(taskId);
        }
    }

    public void addItems(int taskId, List<ResultItem> items) {
        this.insertSymbols.insert(taskId, items);
    }

    @SuppressWarnings({"unchecked"})
    public List<TaskInfo> getTasks(String customer, DateTime from, DateTime to) {
        if (StringUtils.hasText(customer)) {
            return this.selectCustomerTasks.execute(customer, from.toDate(), to.toDate());
        }
        else {
            return this.selectTasks.execute(from.toDate(), to.toDate());
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<SymbolInfo> getSymbols(int taskId) {
        return this.selectSymbols.execute(taskId);
    }

    @SuppressWarnings({"unchecked"})
    public List<String> getCustomerNames() {
        return getJdbcTemplate().queryForList("SELECT DISTINCT customer FROM tasks ORDER BY customer", String.class);
    }
}
