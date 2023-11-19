/*
 * UserDaoDb.java
 *
 * Created on 31.07.2006 11:29:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import org.joda.time.DateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.MappingSqlQueryWithParameters;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserDaoDb extends JdbcDaoSupport implements UserDao {

    private InsertUser insertUser;
    private DeleteUser deleteUser;
    private UpdateUserLogin updateUserLogin;
    private SelectUserById selectUserById;
    private SelectUsersByCompanyId selectUsersByCompanyId;
    private SelectUserLoginById selectUserLoginById;
    private SelectUserByLoginAndCompany selectUserByLoginAndCompany;
    private SelectNotesByUserId selectNotesByUserId;
    private SelectUserProperties selectUserProperties;

    private InsertPortfolio insertPortfolio;
    private DeletePortfolio deletePortfolio;
    private SelectPortfolios selectPortfolios;
    private UpdatePortfolio updatePortfolio;
    private UpdatePortfolioCash updatePortfolioCash;
    private SelectAlternativeIids selectAlternativeIids;

    private SelectPositions selectPositions;
    private InsertPosition insertPosition;
    private DeletePosition deletePosition;
    private UpdatePosition updatePosition;

    private DeleteOrder deleteOrder;
    private UpdateOrder updateOrder;
    private InsertOrder insertOrder;
    private SelectOrders selectOrders;

    private class InsertPosition extends SqlUpdate {
        public InsertPosition() {
            super(getDataSource(), "INSERT INTO portfoliopositions " +
                    "(userid, portfolioid, quoteid, instrumentid, instrumenttype, isquotedperpercent)" +
                    " VALUES (?, ?, ?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.BOOLEAN));
            setReturnGeneratedKeys(true);
            compile();
        }

        public long insert(long userid, long portfolioid, PortfolioPosition pp) {
            final KeyHolder key = new GeneratedKeyHolder();
            this.update(new Object[]{
                    userid,
                    portfolioid,
                    pp.getQid(),
                    pp.getIid(),
                    pp.getInstrumentType().getId(),
                    pp.isQuotedPerPercent(),
            }, key);
            return key.getKey().longValue();
        }
    }

    private class DeletePosition extends SqlUpdate {
        public DeletePosition() {
            super(getDataSource(), "DELETE FROM portfoliopositions WHERE id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }
    }

    private class DeletePortfolio extends SqlUpdate {
        public DeletePortfolio() {
            super(getDataSource(), "DELETE FROM portfolios WHERE id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }
    }

    private class DeleteOrder extends SqlUpdate {
        public DeleteOrder() {
            super(getDataSource(), "DELETE FROM orders WHERE id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }
    }

    private class InsertPortfolio extends SqlUpdate {
        public InsertPortfolio() {
            super(getDataSource(), "INSERT INTO portfolios " +
                    "(userid, name, iswatchlist, currencycode, cash)" +
                    " VALUES (?, ?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.BOOLEAN));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            setReturnGeneratedKeys(true);
            compile();
        }

        public long insert(User user, Portfolio p) {
            final KeyHolder key = new GeneratedKeyHolder();
            this.update(new Object[]{
                    user.getId(),
                    p.getName(),
                    p.isWatchlist(),
                    p.getCurrencyCode(),
                    toPlainString(p.getCash()),
            }, key);
            return key.getKey().longValue();
        }
    }

    private class InsertOrder extends SqlUpdate {
        public InsertOrder() {
            super(getDataSource(), "INSERT INTO orders " +
                    "(userid, portfoliopositionid, price, exchangerate, volume, isbuy, ondate, charge)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.BOOLEAN));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.VARCHAR));
            setReturnGeneratedKeys(true);
            compile();
        }

        public long insert(User user, PortfolioPosition p, Order o) {
            final KeyHolder key = new GeneratedKeyHolder();
            this.update(new Object[]{
                    user.getId(),
                    p.getId(),
                    toPlainString(o.getPrice()),
                    toPlainString(o.getExchangerate()),
                    toPlainString(o.getVolume()),
                    o.isBuy(),
                    toDate(o.getDate()),
                    toPlainString(o.getCharge())
            }, key);
            return key.getKey().longValue();
        }
    }

    private class DeleteUser extends SqlUpdate {
        public DeleteUser() {
            super(getDataSource(), "DELETE FROM users WHERE id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }
    }

    private class InsertUser extends SqlUpdate {
        public InsertUser() {
            super(getDataSource(), "INSERT INTO users " +
                    "(login, password, companyid, createdon, lastloginon, deletedon, blockedsince, " +
                    "numlogins, salutation, firstname, lastname, street, city, country, zipcode, email, " +
                    "phone, fax, costcenter) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
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

        public long insert(User user) {
            final KeyHolder key = new GeneratedKeyHolder();
            this.update(new Object[]{
                    user.getLogin(),
                    user.getPassword(),
                    user.getCompanyid(),
                    toDate(user.getCreatedon()),
                    toDate(user.getLastloginon()),
                    toDate(user.getDeletedon()),
                    toDate(user.getBlockedsince()),
                    user.getNumlogins(),
                    user.getSalutation(),
                    user.getFirstname(),
                    user.getLastname(),
                    user.getStreet(),
                    user.getCity(),
                    user.getCountry(),
                    user.getZipcode(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getFax(),
                    user.getCostcenter()
            }, key);
            return key.getKey().longValue();
        }

    }

    private class UpdatePosition extends SqlUpdate {
        public UpdatePosition() {
            super(getDataSource(), "UPDATE portfoliopositions set quoteid=?, instrumentid=? WHERE id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public int update(PortfolioPosition pp, long id) {
            return update(new Object[]{pp.getQid(), pp.getIid(), id});
        }
    }

    private class UpdateUserLogin extends SqlUpdate {
        public UpdateUserLogin() {
            super(getDataSource(), "UPDATE users SET login=? WHERE id=?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public int update(String login, long id) {
            return update(new Object[]{login, id});
        }
    }

    private class UpdatePortfolio extends SqlUpdate {
        public UpdatePortfolio() {
            super(getDataSource(), "UPDATE portfolios set name=?, cash=? WHERE id=?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public int update(UpdatePortfolioCommand upc) {
            return update(new Object[]{upc.getName(),
                    (upc.isWatchlist() ? null : toPlainString(upc.getCash())),
                    upc.getPortfolioid()});
        }
    }

    private class UpdatePortfolioCash extends SqlUpdate {
        public UpdatePortfolioCash() {
            super(getDataSource(), "UPDATE portfolios set cash=? WHERE id=?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public int update(long portfolioid, BigDecimal cash) {
            return update(new Object[]{toPlainString(cash), portfolioid});
        }
    }

    private class UpdateOrder extends SqlUpdate {
        public UpdateOrder() {
            super(getDataSource(), "UPDATE orders set price=?, volume=?, isbuy=?, ondate=?, charge=?" +
                    " WHERE id=?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.BOOLEAN));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public int update(UpdateOrderCommand uoc) {
            return update(new Object[]{
                    toPlainString(uoc.getPrice()),
                    toPlainString(uoc.getVolume()),
                    uoc.isBuy(),
                    toDate(uoc.getDate()),
                    toPlainString(uoc.getCharge()),
                    uoc.getOrderid()
            });
        }
    }

    private class SelectUserProperties extends MappingSqlQuery {

        public SelectUserProperties() {
            super(getDataSource(), "SELECT id, code, value FROM  userproperties WHERE userid=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            return new Property(rs.getLong(1), rs.getString(2), rs.getString(3));
        }

        protected Map<String, Property> getProperties(long id) {
            //noinspection unchecked
            final List<Property> properties = execute(id);
            if (properties == null || properties.isEmpty()) {
                return null;
            }
            final Map<String, Property> result = new HashMap<>();
            for (Property property : properties) {
                result.put(property.getKey(), property);
            }
            return result;
        }
    }

    private abstract class SelectUser extends MappingSqlQuery {
        private static final String SQL_PREFIX = "SELECT id,login,password,companyid,createdon," +
                "lastloginon,deletedon,blockedsince,numlogins,salutation,firstname,lastname,street," +
                "city,country,zipcode,email,phone,fax,costcenter FROM users";

        public SelectUser(String sql) {
            super(getDataSource(), sql);
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            final User u = new User();
            int n = 0;
            u.setId(rs.getLong(++n));
            u.setLogin(rs.getString(++n));
            u.setPassword(rs.getString(++n));
            u.setCompanyid(rs.getLong(++n));
            u.setCreatedon(toDateTime(rs.getTimestamp(++n)));
            u.setLastloginon(toDateTime(rs.getTimestamp(++n)));
            u.setDeletedon(toDateTime(rs.getTimestamp(++n)));
            u.setBlockedsince(toDateTime(rs.getTimestamp(++n)));
            u.setNumlogins(rs.getInt(++n));
            u.setSalutation(rs.getString(++n));
            u.setFirstname(rs.getString(++n));
            u.setLastname(rs.getString(++n));
            u.setStreet(rs.getString(++n));
            u.setCity(rs.getString(++n));
            u.setCountry(rs.getString(++n));
            u.setZipcode(rs.getString(++n));
            u.setEmail(rs.getString(++n));
            u.setPhone(rs.getString(++n));
            u.setFax(rs.getString(++n));
            u.setCostcenter(rs.getString(++n));
            return u;
        }
    }

    private class SelectUserById extends SelectUser {
        public SelectUserById() {
            super(SelectUser.SQL_PREFIX + " WHERE id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public User findUser(long id) {
            return (User) findObject(id);
        }
    }

    private class SelectUsersByCompanyId extends MappingSqlQuery {
        public SelectUsersByCompanyId() {
            super(getDataSource(), "SELECT id FROM users WHERE companyid=? and deletedon IS NULL ORDER BY id ASC");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            return rs.getLong("id");
        }

        List<Long> getUserIds(long companyid) {
            //noinspection unchecked
            return execute(companyid);
        }
    }

    private class SelectUserLoginById extends MappingSqlQuery {
        public SelectUserLoginById() {
            super(getDataSource(), "SELECT login FROM users WHERE id=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            return rs.getString(1);
        }
    }

    private class SelectUserByLoginAndCompany extends SelectUser {
        public SelectUserByLoginAndCompany() {
            super(SelectUser.SQL_PREFIX + " WHERE login=? AND companyid=?");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        public User findUser(String login, long companyid) {
            return (User) findObject(new Object[]{login, companyid});
        }
    }

    private class SelectPortfolios extends MappingSqlQuery {
        public SelectPortfolios() {
            super(getDataSource(),
                    "SELECT id, name, iswatchlist, currencycode, cash" +
                            " FROM portfolios WHERE userid=? ORDER BY id ASC");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            final Portfolio p = new Portfolio();
            int n = 0;
            p.setId(rs.getLong(++n));
            p.setName(rs.getString(++n));
            p.setWatchlist(rs.getBoolean(++n));
            p.setCurrencyCode(rs.getString(++n));
            p.setCash(toBigDecimal(rs, ++n));
            return p;
        }

        List<Portfolio> getPortfolios(long userid) {
            //noinspection unchecked
            return execute(userid);
        }
    }

    private class SelectAlternativeIids extends MappingSqlQuery {

        public SelectAlternativeIids() {
            super(getDataSource(), "SELECT a.instrumentid, COUNT(a.instrumentid) AS instrumentcount, " +
                    "   COUNT(a.instrumentid) " +
                    "   / " +
                    "   (SELECT count(distinct portfolioid) FROM portfoliopositions WHERE instrumentid = ? and userid != ?) " +
                    "   AS weight " +
                    "FROM portfoliopositions a, portfoliopositions b " +
                    "WHERE a.portfolioid = b.portfolioid " +
                    "   AND a.instrumenttype = b.instrumenttype " +
                    "   AND b.instrumentid = ? " +
                    "   AND a.instrumentid != ? " +
                    "   AND a.userid != ? " +
                    "GROUP BY a.instrumentid " +
                    "ORDER BY weight DESC, a.instrumentid LIMIT 40;");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        @Override
        protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AlternativeIid(rs.getLong("instrumentid"), rs.getInt("instrumentcount"), rs.getFloat("weight"));
        }

        List<AlternativeIid> getAlternativeIids(int iid, User user) {
            //noinspection unchecked
            long userId = user != null ? user.getId() : -1;
            return execute(new Object[]{iid, userId, iid, iid, userId});
        }
    }

    private class SelectOrders extends MappingSqlQueryWithParameters {
        public SelectOrders() {
            super(getDataSource(), "SELECT portfoliopositionid, id, price, exchangerate," +
                    " volume, isbuy, ondate, charge" +
                    " FROM orders WHERE userid=? ORDER BY portfoliopositionid, ondate asc, id");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i, Object[] objects, Map map) throws SQLException {
            final Order order = new Order();
            int n = 0;
            final Long ppid = rs.getLong(++n);
            //noinspection unchecked
            List<Order> orders = (List<Order>) map.get(ppid);
            if (orders == null) {
                orders = new ArrayList<>(2);
                //noinspection unchecked
                map.put(ppid, orders);
            }
            orders.add(order);

            order.setPositionId(ppid);
            order.setId(rs.getLong(++n));
            order.setPrice(toBigDecimal(rs, ++n));
            order.setExchangerate(toBigDecimal(rs, ++n));
            order.setVolume(toBigDecimal(rs, ++n));
            order.setBuy(rs.getBoolean(++n));
            order.setDate(toDateTime(rs.getTimestamp(++n)));
            order.setCharge(toBigDecimal(rs, ++n));
            return order;
        }


        private Map<Long, List<Order>> getOrdersByPositionid(long userid) {
            final Map<Long, List<Order>> result = new HashMap<>();
            execute(userid, result);
            return result;
        }
    }

    private class SelectNotesByUserId extends MappingSqlQueryWithParameters {

        public SelectNotesByUserId() {
            super(getDataSource(),
                    "SELECT n.portfolioid, n.itemid, n.content"
                            + " FROM notes n "
                            + " INNER JOIN portfolios p"
                            + " ON n.portfolioid = p.id "
                            + " WHERE p.userid=?");
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i, Object[] objects, Map commentsByPortfolio) throws SQLException {
            int n = 0;
            final Long portfolioId = rs.getLong(++n);
            final PortfolioPositionNote note = new PortfolioPositionNote(rs.getString(++n), rs.getString(++n));

            if (!commentsByPortfolio.containsKey(portfolioId)) {
                commentsByPortfolio.put(portfolioId, new HashMap<>());
            }

            HashMap<String, PortfolioPositionNote> portfolioComments = (HashMap<String, PortfolioPositionNote>) commentsByPortfolio.get(portfolioId);
            portfolioComments.put(note.getItemId(), note);
            return null;
        }

        public Map<Long, Map<String, PortfolioPositionNote>> getNotesByPortfolioId(long userId) {
            final Map<Long, Map<String, PortfolioPositionNote>> result = new HashMap<>();
            execute(userId, result);
            return result;
        }
    }

    private class SelectPositions extends MappingSqlQueryWithParameters {
        protected static final String PORTFOLIO_BY_ID = "portfolioById";
        protected static final String ORDERS_BY_POSITION_ID = "ordersByPositionId";


        public SelectPositions() {
            this("SELECT portfolioid, id, quoteid, isquotedperpercent, "
                    + "instrumentid FROM portfoliopositions WHERE userid=?");
        }

        protected SelectPositions(String select) {
            super(getDataSource(), select);
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i, Object[] objects, Map map) throws SQLException {
            //noinspection unchecked
            final Map<Long, Portfolio> portfolioById = (Map<Long, Portfolio>) map.get(PORTFOLIO_BY_ID);
            //noinspection unchecked
            final Map<Long, List<Order>> ordersByPositionId = (Map<Long, List<Order>>) map.get(ORDERS_BY_POSITION_ID);

            int n = 0;
            final Long pid = rs.getLong(++n);
            final Portfolio portfolio = portfolioById.get(pid);

            if (portfolio == null) {
                logger.warn("<mapRow> orphan position " + rs.getLong(++n));
                return null;
            }

            final PortfolioPosition pp = new PortfolioPosition();
            pp.setId(rs.getLong(++n));
            pp.setQid(rs.getLong(++n));
            pp.setQuotedPerPercent(rs.getBoolean(++n));
            pp.setIid(rs.getLong(++n));
            pp.setOrders(ordersByPositionId.get(pp.getId()));

            portfolio.addPosition(pp);
            return null;
        }

        @SuppressWarnings({"unchecked"})
        protected void addPositions(long userid,
                                    Map<Long, Portfolio> portfolioById,
                                    Map<Long, List<Order>> ordersByPositionId) {
            final Map m = new HashMap();
            m.put(PORTFOLIO_BY_ID, portfolioById);
            m.put(ORDERS_BY_POSITION_ID, ordersByPositionId);
            execute(userid, m);
        }
    }

    protected void initDao() throws Exception {
        super.initDao();
        this.selectUserById = new SelectUserById();
        this.selectUsersByCompanyId = new SelectUsersByCompanyId();
        this.selectUserByLoginAndCompany = new SelectUserByLoginAndCompany();
        this.selectUserLoginById = new SelectUserLoginById();
        this.selectUserProperties = new SelectUserProperties();
        this.selectPortfolios = new SelectPortfolios();
        this.selectPositions = new SelectPositions();
        this.selectNotesByUserId = new SelectNotesByUserId();
        this.selectOrders = new SelectOrders();
        this.deleteOrder = new DeleteOrder();
        this.updateOrder = new UpdateOrder();
        this.insertOrder = new InsertOrder();
        this.updatePosition = new UpdatePosition();
        this.insertUser = new InsertUser();
        this.deleteUser = new DeleteUser();
        this.updateUserLogin = new UpdateUserLogin();
        this.insertPosition = new InsertPosition();
        this.insertPortfolio = new InsertPortfolio();
        this.deletePosition = new DeletePosition();
        this.deletePortfolio = new DeletePortfolio();
        this.updatePortfolio = new UpdatePortfolio();
        this.updatePortfolioCash = new UpdatePortfolioCash();
        this.selectAlternativeIids = new SelectAlternativeIids();
    }

    private static BigDecimal toBigDecimal(ResultSet rs, int i) throws SQLException {
        final String s = rs.getString(i);
        return s != null ? new BigDecimal(s) : null;
    }

    private static DateTime toDateTime(Date d) {
        return (d != null) ? new DateTime(d) : null;
    }

    private static Date toDate(DateTime dt) {
        return dt != null ? dt.toDate() : null;
    }

    private static String toPlainString(BigDecimal bd) {
        return (bd != null) ? bd.toPlainString() : null;
    }

    public User selectUser(long userId) {
        final User u = this.selectUserById.findUser(userId);
        return addUserAttributes(u);
    }

    public int removeUser(long userId) {
        return this.deleteUser.update(userId);
    }

    public User selectUser(String login, long companyid) {
        final User u = this.selectUserByLoginAndCompany.findUser(login, companyid);
        return addUserAttributes(u);
    }

    public String selectLogin(long userId) {
        return (String) this.selectUserLoginById.findObject(userId);
    }

    public int updatePosition(Long positionid, PortfolioPosition pp) {
        return this.updatePosition.update(pp, positionid);
    }

    public long insertUser(User user) {
        try {
            return this.insertUser.insert(user);
        } catch (DataIntegrityViolationException e) {
            throw new LoginExistsException("Login exists: " + user.getLogin(), user.getLogin());
        }
    }

    public long insertPortfolio(User user, Portfolio p) {
        return this.insertPortfolio.insert(user, p);
    }

    public long insertPosition(AddPositionCommand apc, PortfolioPosition pp) {
        return this.insertPosition.insert(apc.getUserid(), apc.getPortfolioid(), pp);
    }

    public int deletePosition(RemovePositionCommand rpc) {
        return this.deletePosition.update(rpc.getPositionid());
    }

    public void updatePortfolio(UpdatePortfolioCommand uwc) {
        this.updatePortfolio.update(uwc);
    }

    public void updatePortfolioCash(Portfolio p) {
        this.updatePortfolioCash.update(p.getId(), p.getCash());
    }

    public List<AlternativeIid> getAlternativeIids(int iid, User user) {
        return this.selectAlternativeIids.getAlternativeIids(iid, user);
    }

    @Override
    public List<Long> getUserIds(long companyId) {
        return this.selectUsersByCompanyId.getUserIds(companyId);
    }

    public void updateLogin(long userId, String newLogin) {
        this.updateUserLogin.update(newLogin, userId);
    }

    public int deletePortfolio(RemovePortfolioCommand rwc) {
        return this.deletePortfolio.update(rwc.getPortfolioid());
    }

    public int deleteOrder(RemoveOrderCommand command, long positionid) {
        final int numRows = this.deleteOrder.update(command.getOrderid());
        if (positionid >= 0) {
            this.deletePosition.update(positionid);
        }
        return numRows;
    }

    public void updateOrder(UpdateOrderCommand command) {
        this.updateOrder.update(command);
    }

    public long insertOrder(User user, PortfolioPosition position, Order o) {
        return this.insertOrder.insert(user, position, o);
    }

    private User addUserAttributes(User u) {
        if (u == null) {
            return null;
        }
        u.setProperties(this.selectUserProperties.getProperties(u.getId()));

        List<Portfolio> portfolios = this.selectPortfolios.getPortfolios(u.getId());

        if (portfolios == null) {
            portfolios = Collections.emptyList();
        }

        if (!portfolios.isEmpty()) {
            final Map<Long, Portfolio> portfolioById = new HashMap<>();

            final Map<Long, Map<String, PortfolioPositionNote>> notesByPortfolioId =
                    this.selectNotesByUserId.getNotesByPortfolioId(u.getId());

            for (Portfolio portfolio : portfolios) {
                portfolio.setNotes(notesByPortfolioId.get(portfolio.getId()));
                portfolioById.put(portfolio.getId(), portfolio);
            }

            final Map<Long, List<Order>> ordersByPositionid = this.selectOrders.getOrdersByPositionid(u.getId());

            this.selectPositions.addPositions(u.getId(), portfolioById, ordersByPositionid);

        }

        u.setPortfolios(new Portfolios(portfolios));

        return u;
    }
}
