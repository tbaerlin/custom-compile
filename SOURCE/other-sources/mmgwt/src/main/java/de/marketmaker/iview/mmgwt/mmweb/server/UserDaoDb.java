/*
 * UserDaoDb.java
 *
 * Created on 30.06.2008 14:49:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.MessageOfTheDay;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserDaoDb extends JdbcDaoSupport implements UserDao {
    protected final Log logger = LogFactory.getLog(getClass());

    private class InsertUser extends SqlUpdate {

        public InsertUser() {
            super(getDataSource(), "INSERT INTO users" +
                    " (id, password, password2, password3, created, updated, data)" +
                    " VALUES (?, ?, password, password, now(), now(), ?)");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.BLOB));
            compile();
        }

    }

    private class UpdateSession extends SqlUpdate {
        public UpdateSession() {
            super(getDataSource(), "UPDATE users SET sessionid=?, lastlogin=now(), numlogins=numlogins+1 WHERE id=?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }
    }

    private class DeleteSession extends SqlUpdate {
        public DeleteSession() {
            super(getDataSource(), "UPDATE users SET sessionid=null WHERE id=?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }
    }

    private class UpdateUser extends SqlUpdate {
        public UpdateUser() {
            super(getDataSource(), "UPDATE users SET updated=now(), data=? WHERE id=?");
            declareParameter(new SqlParameter(Types.BLOB));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }
    }

    private class UpdatePassword extends SqlUpdate {
        public UpdatePassword() {
            super(getDataSource(), "UPDATE users" +
                    " SET password3=password2, password2=password, password=?, passworddate=now()" +
                    " WHERE id=? AND password=?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }
    }

    private class SelectSession extends MappingSqlQuery {
        public SelectSession() {
            super(getDataSource(), "SELECT sessionid FROM users WHERE id=?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        protected Object mapRow(ResultSet resultSet, int i) throws SQLException {
            return resultSet.getString(1);
        }
    }

    private class SelectClientConfigs extends MappingSqlQuery {
        public SelectClientConfigs() {
            super(getDataSource(), "SELECT id, module_name, vwd_client_id, vwd_app_id, session_mode"
                    + ", login_auth_type, musterdepot_user, template_user, change_password_after_days"
                    + ", app_title, initial_password, initial_password_hash FROM clients");
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            int col = 0;
            final ClientConfig result = new ClientConfig(rs.getInt(++col));
            result.setModuleName(rs.getString(++col));
            result.setClientId(rs.getString(++col));
            result.setAppId(rs.getString(++col));
            result.setSessionMode(rs.getString(++col));
            result.setLoginAuthType(rs.getString(++col));
            result.setIdOfMusterdepotUser(rs.getString(++col));
            result.setIdOfTemplateUser(rs.getString(++col));
            result.setChangePasswordAfterDays(rs.getInt(++col));
            result.setAppTitle(rs.getString(++col));
            result.setInitialPassword(rs.getString(++col));
            result.setInitialPasswordHash(rs.getString(++col));
            return result;
        }
    }

    /**
     * Reset password; in order to require an immediate password change, the passworddate
     * will be set to null
     */
    private class ResetPassword extends SqlUpdate {
        static final String SQL = "UPDATE users" +
                " SET password3=password2, password2=password, password=?, passworddate=NULL" +
                " WHERE id=?";

        public ResetPassword() {
            super(getDataSource(), SQL);
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }
    }

    private class SelectUser extends SelectUsers {
        public SelectUser() {
            super("SELECT id, password, password2, password3, passworddate, data" +
                    " FROM users WHERE id=?", new SqlParameter(Types.VARCHAR));
        }
    }

    private class SelectUsers extends MappingSqlQuery {
        public SelectUsers() {
            this("SELECT id, password, password2, password3, passworddate, data" +
                    " FROM users");
        }

        public SelectUsers(String sql) {
            this(sql, new SqlParameter[0]);
        }

        public SelectUsers(String sql, SqlParameter... params) {
            super(getDataSource(), sql);
            if (params != null && params.length > 0) {
                for (SqlParameter param : params) {
                    declareParameter(param);
                }
            }
            compile();
        }

        protected Object mapRow(ResultSet resultSet, int i) throws SQLException {
            final User result = new User();
            result.setVwdId(resultSet.getString(1));
            result.setPasswords(new String[]{
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4)
            });
            result.setPasswordChangeDate(toDate(resultSet.getDate(5)));
            result.setAppConfig(deserialize(resultSet.getBytes(6)));
            return result;
        }

    }

    private Date toDate(java.sql.Date d) {
        if (d == null) {
            return null;
        }
        return new Date(d.getTime());
    }

    private class SelectMessageOfTheDay extends MappingSqlQuery {
        private SelectMessageOfTheDay() {
            super(getDataSource(), "SELECT firstDate, lastDate, message FROM message_of_the_day WHERE module_name = ?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        @Override
        protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            final MessageOfTheDay motd = new MessageOfTheDay();
            motd.setFirstDate(toDate(rs.getDate("firstDate")));
            motd.setLastDate(toDate(rs.getDate("lastDate")));
            motd.setMessage(rs.getString("message"));
            return motd;
        }
    }

    private class SelectMessageOfTheDayByDate extends MappingSqlQuery {
        private SelectMessageOfTheDayByDate() {
            super(getDataSource(), "SELECT message FROM message_of_the_day WHERE module_name = ? AND firstDate <= curdate() AND lastDate >= curdate()");
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        @Override
        protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("message");
        }
    }

    private class UpdateMessageOfTheDay extends SqlUpdate {
        private UpdateMessageOfTheDay() {
            super(getDataSource(), "INSERT INTO message_of_the_day VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE firstDate = ?, lastDate = ?, message = ?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.DATE));
            declareParameter(new SqlParameter(Types.DATE));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.DATE));
            declareParameter(new SqlParameter(Types.DATE));
            declareParameter(new SqlParameter(Types.VARCHAR));
        }
    }

    private InsertUser insertUser;

    private UpdateUser updateUser;

    private UpdatePassword updatePassword;

    private ResetPassword resetPassword;

    private SelectUser selectUser;

    private SelectUsers selectUsers;

    private SelectSession selectSession;

    private UpdateSession updateSession;

    private DeleteSession deleteSession;

    private SelectClientConfigs selectClientConfigs;

    private SelectMessageOfTheDay selectMessageOfTheDay;

    private SelectMessageOfTheDayByDate selectMessageOfTheDayByDate;

    private UpdateMessageOfTheDay updateMessageOfTheDay;

    protected void initDao() throws Exception {
        super.initDao();
        this.insertUser = new InsertUser();
        this.updateUser = new UpdateUser();
        this.updatePassword = new UpdatePassword();
        this.resetPassword = new ResetPassword();
        this.selectUser = new SelectUser();
        this.selectUsers = new SelectUsers();
        this.selectSession = new SelectSession();
        this.updateSession = new UpdateSession();
        this.deleteSession = new DeleteSession();
        this.selectClientConfigs = new SelectClientConfigs();
        this.selectMessageOfTheDay = new SelectMessageOfTheDay();
        this.selectMessageOfTheDayByDate = new SelectMessageOfTheDayByDate();
        this.updateMessageOfTheDay = new UpdateMessageOfTheDay();
    }

    public User getUser(String id) {
        try {
            return (User) this.selectUser.findObject(id);
        } catch (IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() > 1) {
                this.logger.error("<getUser> " + e.getActualSize() + " users with key " + id);
            }
            return null;
        } catch (DataAccessException dae) {
            this.logger.error("<getUser> failed", dae);
            return null;
        }
    }


    @Override
    public List<User> getUsers() {
        final List users = this.selectUsers.execute();
        final List<User> result = new ArrayList<>();
        for (Object user : users) {
            result.add((User) user);
        }
        return result;
    }

    public void storeUserConfig(String userId, AppConfig config) {
        try {
            this.updateUser.update(serialize(config), userId);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<storeUser> " + userId);
            }
        } catch (IOException e) {
            this.logger.fatal("<serialize> failed for " + userId + ": ", e);
        }
    }

    public void insertUser(User u) {
        try {
            this.insertUser.update(u.getVwdId(), u.getPassword(), serialize(u.getAppConfig()));
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<insertUser> " + u.getVwdId());
            }
        } catch (IOException e) {
            this.logger.fatal("<serialize> failed for " + u.getUid() + ": ", e);
        }
    }

    public boolean changePassword(String uid, String oldPassword, String newPassword) {
        return this.updatePassword.update(newPassword, uid, oldPassword) == 1;
    }

    public boolean resetPassword(String uid, String password) {
        return this.resetPassword.update(new Object[]{password, uid}) == 1;
    }


    public String getSessionId(String uid) {
        return (String) this.selectSession.findObject(uid);
    }

    public void setSessionId(String uid, String id) {
        if (id != null) {
            this.updateSession.update(id, uid);
        }
        else {
            this.deleteSession.update(uid);
        }
    }

    public Map<String, ClientConfig> getClientConfigs() {
        final List<ClientConfig> list = this.selectClientConfigs.execute();
        assignSelectorDescriptions(list);
        final Map<String, ClientConfig> result = new HashMap<>();
        for (ClientConfig config : list) {
            result.put(config.getModuleName(), config);
        }
        return result;
    }

    private void assignSelectorDescriptions(List<ClientConfig> list) {
        for (ClientConfig config : list) {
            assignSelectorDescriptions(config);
        }
    }

    private void assignSelectorDescriptions(final ClientConfig config) {
        getJdbcTemplate().query("SELECT name, xpath FROM visit_config WHERE client="
                + config.getId() + " ORDER BY selector", new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                config.add(rs.getString(1), rs.getString(2));
            }
        });
    }

    public NavigableMap<DateTime, AppConfig> getAppConfigHistory(String uid) {
        return getJdbcTemplate().query("SELECT created, data FROM appconfhistory WHERE uid=?",
                new String[]{uid}, new ResultSetExtractor<NavigableMap<DateTime, AppConfig>>() {
                    private final TreeMap<DateTime, AppConfig> result = new TreeMap<>();

                    public NavigableMap<DateTime, AppConfig> extractData(
                            ResultSet rs) throws SQLException, DataAccessException {
                        while (rs.next()) {
                            result.put(new DateTime(rs.getTimestamp(1)), deserialize(rs.getBytes(2)));
                        }
                        return result;
                    }
                });
    }

    @Override
    public AppConfig getAppConfig(final int id) {
        return getJdbcTemplate().query("SELECT data FROM appconfhistory WHERE id=" + id,
                new ResultSetExtractor<AppConfig>() {
                    public AppConfig extractData(
                            ResultSet rs) throws SQLException, DataAccessException {
                        if (rs.next()) {
                            return deserialize(rs.getBytes(1));
                        }
                        throw new EmptyResultDataAccessException("no appconfhistory with id " + id, 1);
                    }
                });
    }

    @Override
    public MessageOfTheDay getMessageOfTheDay(String zone) {
        final List result = this.selectMessageOfTheDay.execute(new Object[]{zone});
        return result.size() == 1 ? (MessageOfTheDay) result.get(0) : null;
    }

    @Override
    public String getMessageOfTheDayByDate(String zone) {
        final List result = this.selectMessageOfTheDayByDate.execute(new Object[]{zone});
        return result.size() == 1 ? (String) result.get(0) : null;
    }

    @Override
    public void setMessageOfTheDay(String zone, MessageOfTheDay motd) {
        this.updateMessageOfTheDay.update(zone, motd.getFirstDate(), motd.getLastDate(), motd.getMessage(), // for insert
                motd.getFirstDate(), motd.getLastDate(), motd.getMessage() // for update
        );
    }

    private byte[] serialize(AppConfig config) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(baos));
        oos.writeObject(config);
        oos.close();
        return baos.toByteArray();
    }

    private AppConfig deserialize(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try {
            return (AppConfig) new ObjectInputStream(new GZIPInputStream(bais)).readObject();
        } catch (Exception e) {
            this.logger.fatal("<deserialize> failed", e);
            return null;
        }
    }

    public static void main(String[] args) {
        final UserDaoDb dao = new UserDaoDb();
        final SingleConnectionDataSource dataSource =
                new SingleConnectionDataSource();
        dao.setDataSource(dataSource);
    }
}
