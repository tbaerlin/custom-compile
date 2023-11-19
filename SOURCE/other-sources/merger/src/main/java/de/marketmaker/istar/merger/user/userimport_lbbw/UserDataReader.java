/*
 * UserDataReader.java
 *
 * Created on 22.11.2006 14:24:20
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user.userimport_lbbw;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.MappingSqlQueryWithParameters;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ResourcePermissionProvider;
import de.marketmaker.istar.instrument.InstrumentRequest;
import de.marketmaker.istar.instrument.InstrumentResponse;
import de.marketmaker.istar.instrument.InstrumentServer;
import de.marketmaker.istar.merger.provider.UserProvider;
import de.marketmaker.istar.merger.user.AddOrderCommand;
import de.marketmaker.istar.merger.user.AddPortfolioCommand;
import de.marketmaker.istar.merger.user.AddPositionCommand;
import de.marketmaker.istar.merger.user.UserContext;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserDataReader extends JdbcDaoSupport {
    static final Map<String, List<String>> MARKETS = new HashMap<>();

    private InstrumentServer instrumentServer;
    private UserProvider userProvider;

    private SelectLogins selectLogins;
    private SelectPortfolios selectPortfolios;
    private SelectWatchlists selectWatchlists;
    private SelectOrders selectOrders;
    private SelectWatchlistelements selectWatchlistelements;
    private static final int COMPANYID = 99;
    private Map<Long, List<OrderOld>> orders;
    private Map<Long, List<PortfolioOld>> portfolios;
    private Map<Long, List<OrderOld>> watchlistelements;
    private Map<Long, List<PortfolioOld>> watchlists;
    private Map<Long, Instrument> instruments = new HashMap<>();
    private int numUserWithNoPortfolio;
    private int numPortfoliosWithNoOrders;
    private int numPfPositionsNoInstrument;
    private int numAlteredQuotes;
    private int numUserWithNoWatchlist;
    private int numWatchlistsWithNoElement;
    private int numWlPositionsNoInstrument;

    public void setInstrumentServer(InstrumentServer instrumentServer) {
        this.instrumentServer = instrumentServer;
    }

    public void setUserProvider(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    protected void initDao() throws Exception {
        super.initDao();
        this.selectLogins = new SelectLogins();
        this.selectPortfolios = new SelectPortfolios();
        this.selectOrders = new SelectOrders();
        this.selectWatchlists = new SelectWatchlists();
        this.selectWatchlistelements = new SelectWatchlistelements();
    }

    public void start() throws Exception {
        final List<UserOld> users = this.selectLogins.getLogins();
        System.out.println("#user: " + users.size());

        this.portfolios = this.selectPortfolios.getPortfolios();
        System.out.println("#portfolios: " + portfolios.values().size());

        this.orders = this.selectOrders.getOrders();
        System.out.println("#orders: " + orders.values().size());

        this.watchlists = this.selectWatchlists.getPortfolios();
        System.out.println("#watchlists: " + watchlists.values().size());

        this.watchlistelements = this.selectWatchlistelements.getOrders();
        System.out.println("#watchlistelements: " + watchlistelements.values().size());

        int count = 0;
        for (UserOld user : users) {
            final UserContext userContext = this.userProvider.getUserContext(user.getLogin(), COMPANYID);
            final long userid = userContext.getUser().getId();

            insertPortfolios(user,userid);
            insertWatchlists(user,userid);

            count++;
            if (count % 1000 == 0) {
                System.out.println("inserted " + count + " users");
//                break;
            }
        }

        System.out.println("numAlteredQuotes = " + numAlteredQuotes);
        System.out.println("numPfPositionsNoInstrument = " + numPfPositionsNoInstrument);
        System.out.println("numPortfoliosWithNoOrders = " + numPortfoliosWithNoOrders);
        System.out.println("numUserWithNoPortfolio = " + numUserWithNoPortfolio);
        System.out.println("numUserWithNoWatchlist = " + numUserWithNoWatchlist);
        System.out.println("numWatchlistsWithNoElement = " + numWatchlistsWithNoElement);
        System.out.println("numWlPositionsNoInstrument = " + numWlPositionsNoInstrument);
    }

    private void insertWatchlists(UserOld user, long userid) {
        final List<PortfolioOld> wls = this.watchlists.get(user.getId());
        if (wls == null) {
            this.numUserWithNoWatchlist++;
            return;
        }

        for (PortfolioOld wl : wls) {
            final AddPortfolioCommand apc = new AddPortfolioCommand();
            apc.setUserid(userid);
            apc.setName(wl.getName());
            apc.setWatchlist(true);

            final Long portfolioid = this.userProvider.addPortfolio(apc);

            final List<OrderOld> wle = this.watchlistelements.get(wl.getId());
            if (wle == null) {
                this.numWatchlistsWithNoElement++;
//                System.out.println("no wles for " + wl.getId());
                continue;
            }

            wle.sort(new Comparator<OrderOld>() {
                public int compare(OrderOld o1, OrderOld o2) {
                    return (int) (o1.getId() - o2.getId());
                }
            });

//            System.out.println();
            for (OrderOld order : wle) {
                final Quote quote = getQuote(order.getIid(), order.getMarket(), order.getCurrency());

                if (quote == null) {
                    this.numWlPositionsNoInstrument++;
                    System.out.println("no quote for " + order.getIid());
                    continue;
                }

                final AddPositionCommand aoc = new AddPositionCommand();
                aoc.setUserid(userid);
                aoc.setPortfolioid(portfolioid);

                aoc.setQuote(quote);

                this.userProvider.insertPosition(aoc);
            }
        }
    }

    private void insertPortfolios(UserOld user, long userid) {
        final List<PortfolioOld> pfs = this.portfolios.get(user.getId());
        if (pfs == null) {
//            System.out.println("no portfolios for user " + login);
            this.numUserWithNoPortfolio++;
            return;
        }

        for (PortfolioOld pf : pfs) {
            final AddPortfolioCommand apc = new AddPortfolioCommand();
            apc.setUserid(userid);
            apc.setName(pf.getName());
            apc.setWatchlist(false);
            apc.setCash(getBigDecimal(pf.getCash()));
            apc.setCurrencycode(pf.getCurrency());

            final Long portfolioid = this.userProvider.addPortfolio(apc);

            final List<OrderOld> orders = this.orders.get(pf.getId());
            if (orders == null) {
                this.numPortfoliosWithNoOrders++;
//                System.out.println("no orders for " + pf.getId());
                continue;
            }

            orders.sort(new Comparator<OrderOld>() {
                public int compare(OrderOld o1, OrderOld o2) {
                    return (int) (o1.getId() - o2.getId());
                }
            });

//            System.out.println();
            for (OrderOld order : orders) {
                final Quote quote = getQuote(order.getIid(), order.getMarket(), order.getCurrency());

                if (quote == null) {
                    this.numPfPositionsNoInstrument++;
                    System.out.println("no quote for " + order.getIid());
                    continue;
                }

                final AddOrderCommand aoc = new AddOrderCommand();
                aoc.setUserid(userid);
                aoc.setPortfolioid(portfolioid);
                aoc.setBuy(order.getType() == 0);
                aoc.setDate(new DateTime(order.getDate().getTime()));
                aoc.setExchangeRate(getBigDecimal(order.getRate()));
                final BigDecimal price = getBigDecimal(order.getPrice());
//                System.out.println("price: " + price + " " + order.getPrice() + " "+ order.getId());
                aoc.setPrice(price);
                aoc.setVolume(getBigDecimal(order.getVolume()));

                aoc.setQuote(quote);
                final long orderid = this.userProvider.addOrder(aoc);
            }
        }
    }

    private BigDecimal getBigDecimal(double value) {
        return new BigDecimal(Double.toString(value));
    }

    private Quote getQuote(long iid, String market, String currency) {
        Instrument instrument = this.instruments.get(iid);
        if (instrument == null) {
            try {
                final InstrumentRequest request = new InstrumentRequest();
                request.addItem(String.valueOf(iid), InstrumentRequest.KeyType.IID);
                final InstrumentResponse response = this.instrumentServer.identify(request);
                instrument = response.getInstruments().get(0);
            } catch (Exception e) {
                return null;
            }
            this.instruments.put(iid, instrument);
        }

        if (instrument == null) {
            System.out.println("no instrument for " + iid);
            return null;
        }

        for (Quote quote : instrument.getQuotes()) {
            if (market.equals(quote.getSymbolVwdfeedMarket())) {
                return quote;
            }
        }

        if (StringUtils.hasText(currency)) {
            for (Quote quote : instrument.getQuotes()) {
                if (currency.equals(quote.getCurrency().getSymbolIso())) {
                    this.numAlteredQuotes++;
                    return quote;
                }
            }
        }

        final List<Quote> quotes = ProfiledInstrument.quotesWithPrices(instrument, ProfileFactory.createInstance(ResourcePermissionProvider.getInstance("lbbw-nt")));
        if (!quotes.isEmpty()) {
            this.numAlteredQuotes++;
            return quotes.get(0);
        }

        System.out.println("no quote for " + iid + ".iid at " + market);

        return null;
    }

    private boolean isPercentQuoted(Quote q) {
        return q.getMinimumQuotationSize().isUnitPercent();
    }


    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(new String[]{
                new ClassPathResource("user-import-context.xml", UserDataReader.class).getURL().toString()
        }, false);

        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        if (args.length > 0) {
            ppc.setLocation(new FileSystemResource(args[0]));
        } else {
            ppc.setLocation(new ClassPathResource("user-import.properties", UserDataReader.class));
        }

        ac.addBeanFactoryPostProcessor(ppc);
        ac.refresh();

        final UserDataReader reader = (UserDataReader) ac.getBean("userDataReader");
        reader.start();

        ac.close();
    }

    private class SelectLogins extends MappingSqlQuery {
        public SelectLogins() {
            super(getDataSource(), "SELECT userid, login FROM users WHERE login not like '%60050000'");
//            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
//            final Portfolio p = new Portfolio();
//            int n = 0;
//            p.setId(rs.getLong(++n));
//            p.setName(rs.getString(++n));
//            p.setWatchlist(rs.getBoolean(++n));
//            p.setCurrencyCode(rs.getString(++n));
//            p.setCash(toBigDecimal(rs, ++n));
//            return p;
            return new UserOld(rs.getLong("userid"), rs.getString("login"));
        }

        List<UserOld> getLogins() {
            //noinspection unchecked
            return execute();
        }
    }

    private class SelectPortfolios extends MappingSqlQueryWithParameters {
        public SelectPortfolios() {
            super(getDataSource(), "SELECT portfolioid, name, currencyname, charge, initialinvestment, userid FROM portfolios");
            compile();
        }

        protected Object mapRow(ResultSet rs, int i, Object[] objects, Map map) throws SQLException {
            //noinspection unchecked
            final Map<Long, List<PortfolioOld>> pById = (Map<Long, List<PortfolioOld>>) map;

            final long userid = rs.getLong("userid");

            List<PortfolioOld> list = pById.get(userid);
            if (list == null) {
                list = new ArrayList<>();
                pById.put(userid, list);
            }

            list.add(new PortfolioOld(rs.getLong("portfolioid"), rs.getString("name"), rs.getString("currencyname"), rs.getDouble("charge"), rs.getDouble("initialinvestment")));
            return null;
        }

        Map<Long, List<PortfolioOld>> getPortfolios() {
            final Map<Long, List<PortfolioOld>> map = new HashMap<>();
            //noinspection unchecked
            execute(map);
            return map;
        }
    }

    private class SelectWatchlists extends MappingSqlQueryWithParameters {
        public SelectWatchlists() {
            super(getDataSource(), "SELECT listid, listname, userid FROM watchlists");
            compile();
        }

        protected Object mapRow(ResultSet rs, int i, Object[] objects, Map map) throws SQLException {
            //noinspection unchecked
            final Map<Long, List<PortfolioOld>> pById = (Map<Long, List<PortfolioOld>>) map;

            final long userid = rs.getLong("userid");

            List<PortfolioOld> list = pById.get(userid);
            if (list == null) {
                list = new ArrayList<>();
                pById.put(userid, list);
            }

            list.add(new PortfolioOld(rs.getLong("listid"), rs.getString("listname"), null, Double.NaN, Double.NaN));
            return null;
        }

        Map<Long, List<PortfolioOld>> getPortfolios() {
            final Map<Long, List<PortfolioOld>> map = new HashMap<>();
            //noinspection unchecked
            execute(map);
            return map;
        }
    }

    private class SelectWatchlistelements extends MappingSqlQueryWithParameters {
        public SelectWatchlistelements() {
//            super(getDataSource(), "SELECT portfolioid,wkn FROM portfolioelements ");
            super(getDataSource(), "SELECT listid, wkn, place, elementid FROM watchlistelements");
            compile();
        }

        protected Object mapRow(ResultSet rs, int i, Object[] objects, Map map) throws SQLException {
            //noinspection unchecked
            final Map<Long, List<OrderOld>> oById = (Map<Long, List<OrderOld>>) map;

            final long portfolioid = rs.getLong("listid");

            List<OrderOld> list = oById.get(portfolioid);
            if (list == null) {
                list = new ArrayList<>();
                oById.put(portfolioid, list);
            }

            final String s = rs.getString("wkn");
            if (!s.endsWith(".iid")) {
                System.out.println("unknown symbol: " + s);
            }
            list.add(new OrderOld(rs.getLong("elementid"),
                    Double.NaN, Double.NaN, Double.NaN,
                    0, null, Double.NaN, Long.parseLong(s.substring(0, s.length() - 4)),
                    rs.getString("place"), null, null));
            return null;
        }

        Map<Long, List<OrderOld>> getOrders() {
            final Map<Long, List<OrderOld>> map = new HashMap<>();
            //noinspection unchecked
            execute(map);
            return map;
        }
    }

    private class SelectOrders extends MappingSqlQueryWithParameters {
        public SelectOrders() {
//            super(getDataSource(), "SELECT portfolioid,wkn FROM portfolioelements ");
            super(getDataSource(), "SELECT portfolioid, wkn, name, defaultplace, currencyname, orderid, price, exchangetodepotcurrency, volume, type, ondate, charge FROM portfolioelements e, orders o WHERE o.elementid=e.elementid and name not like 'no hit%'");
            compile();
        }

        protected Object mapRow(ResultSet rs, int i, Object[] objects, Map map) throws SQLException {
            //noinspection unchecked
            final Map<Long, List<OrderOld>> oById = (Map<Long, List<OrderOld>>) map;

            final long portfolioid = rs.getLong("portfolioid");

            List<OrderOld> list = oById.get(portfolioid);
            if (list == null) {
                list = new ArrayList<>();
                oById.put(portfolioid, list);
            }

            final String s = rs.getString("wkn");
            if (!s.endsWith(".iid")) {
                System.out.println("unknown symbol: " + s);
            }
            list.add(new OrderOld(rs.getLong("orderid"),
                    rs.getDouble("price"), rs.getDouble("exchangetodepotcurrency"), rs.getDouble("volume"),
                    rs.getInt("type"), new Date(rs.getDate("ondate").getTime()), rs.getDouble("charge"), Long.parseLong(s.substring(0, s.length() - 4)),
                    rs.getString("defaultplace"), rs.getString("name"), rs.getString("currencyname")));
            return null;
        }

        Map<Long, List<OrderOld>> getOrders() {
            final Map<Long, List<OrderOld>> map = new HashMap<>();
            //noinspection unchecked
            execute(map);
            return map;
        }
    }

    private static class UserOld {
        private final long id;
        private final String login;

        private UserOld(long id, String login) {
            this.id = id;
            this.login = login;
        }

        public long getId() {
            return id;
        }

        public String getLogin() {
            return login;
        }
    }

    private static class PortfolioOld {
        private final long id;
        private final String name;
        private final String currency;
        private final double charge;
        private final double cash;

        private PortfolioOld(long id, String name, String currency, double charge, double cash) {
            this.id = id;
            this.name = name;
            this.currency = currency;
            this.charge = charge;
            this.cash = cash;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCurrency() {
            return currency;
        }

        public double getCharge() {
            return charge;
        }

        public double getCash() {
            return cash;
        }
    }

    private static class OrderOld {
        private final long id;
        private final double price;
        private final double rate;
        private final double volume;
        private final int type;
        private final Date date;
        private final double charge;
        private final long iid;
        private final String market;
        private final String name;
        private final String currency;

        private OrderOld(long id, double price, double rate, double volume, int type, Date date, double charge, long iid, String market, String name, String currency) {
            this.id = id;
            this.price = price;
            this.rate = rate;
            this.volume = volume;
            this.type = type;
            this.date = date;
            this.charge = charge;
            this.iid = iid;
            this.market = market;
            this.name = name;
            this.currency = currency;
        }

        public long getId() {
            return id;
        }

        public double getPrice() {
            return price;
        }

        public double getRate() {
            return rate;
        }

        public double getVolume() {
            return volume;
        }

        public int getType() {
            return type;
        }

        public Date getDate() {
            return date;
        }

        public double getCharge() {
            return charge;
        }

        public long getIid() {
            return iid;
        }

        public String getMarket() {
            return market;
        }

        public String getName() {
            return name;
        }

        public String getCurrency() {
            return currency;
        }
    }
}
