/*
 * UserInserterImpl.java
 *
 * Created on 23.11.2006 19:10:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user.userimport_pb;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.user.LoginExistsException;
import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserInserterImpl  extends JdbcDaoSupport implements UserInserter {
    private static final String PF_CURRENCY = "EUR";
    private static final int COMPANYID = 1;

    private IsoCurrencyConversionProvider isoCurrencyConversionProvider;

    private boolean issueDbStatements;

    private InsertUser insertUser;
    private InsertPortfolio insertPortfolio;
    private InsertPosition insertPosition;
    private InsertOrder insertOrder;

    private Map<String, Instrument> instrumentsByWkn ;

    public void setIsoCurrencyConversionProvider(IsoCurrencyConversionProvider isoCurrencyConversionProvider) {
        this.isoCurrencyConversionProvider = isoCurrencyConversionProvider;
    }

    public void setInstrumentsByWkn(Map<String, Instrument> instrumentsByWkn) {
        this.instrumentsByWkn = instrumentsByWkn;
    }

    public void setIssueDbStatements(boolean issueDbStatements) {
        this.issueDbStatements = issueDbStatements;
    }

    protected void initDao() throws Exception {
        super.initDao();

        this.insertUser = new InsertUser();
        this.insertPortfolio = new InsertPortfolio();
        this.insertPosition = new InsertPosition();
        this.insertOrder = new InsertOrder();
    }

    public void insertUsers(List<String> usernames, Map<String, List<ImportPortfolio>> map) {
        for (final String username : usernames) {
//            System.out.println("insert user: login=" + username);
            final long userid = insertUser(username, COMPANYID, new Date(), "pb-import");

            final List<ImportPortfolio> ips = map.get(username);
            if (ips != null) {
                ips.sort(new Comparator<ImportPortfolio>() {
                    public int compare(ImportPortfolio o1, ImportPortfolio o2) {
                        return o1.getNum() - o2.getNum();
                    }
                });
                for (final ImportPortfolio ip : ips) {
                    if (ip.isPortfolio()) {
                        insertPortfolio(ip, userid);
                    }
                    else {
                        insertWatchlist(ip, userid);
                    }
                }
            }
        }
    }

    private long insertUser(String username, int companyid, Date date, String costcenter) {
        if (!this.issueDbStatements) {
            return -1;
        }

        try {
            return this.insertUser.insert(username, companyid, date, costcenter);
        }
        catch (DataIntegrityViolationException e) {
            throw new LoginExistsException("Login exists: " + username, username);
        }
    }

    private void insertPortfolio(ImportPortfolio ip, long userid) {
//        System.out.println("insert portfolio: userid=<id from above user insert>, name=" + ip.getListname()
//                + ", iswatchlist=false, currencycode=EUR, cash=0");
        final long portfolioid = this.insertPortfolio.insert(userid, ip.getListname(), false, PF_CURRENCY);

        for (final ImportPosition position : ip.getPositions()) {
            final Instrument instrument = this.instrumentsByWkn.get(position.getWkn());
            if (instrument == null) {
                continue;
            }
            final Quote quote = getQuote(position, instrument);

            if (quote == null) {
                continue;
            }

            if (quote.getCurrency().getSymbolIso() == null || !quote.getCurrency().getSymbolIso().equals(position.getCurrency())) {
                System.out.println("ALARM: wrong currency, " + position.getWkn() + ", " + ip.getUsername());
            }

//            System.out.println("insert portfolio element: userid=<id from above user insert>, " +
//                    "portfolioid=<id from above watchlist insert>, qid=" + quote.getId()
//                    + ", iid=" + instrument.getId() + ", isquotedperpercent=" + getQuotedPerPercent(instrument) + ", note=" + position.getNotiz());
            final long positionid = this.insertPosition.insert(userid, portfolioid, quote.getId(), instrument.getId(), getQuotedPerPercent(instrument), position.getNotiz());

//            System.out.println("insert order: userid=<id from above user insert>, " +
//                    "portfoliopositionid=<id from above position insert>, price=" + position.getOrdervalue()
//                    + ", exchangerate=1, volume=" + position.getVolume() + ", isbuy=true, ondate=" + position.getOrderdate().toString(DTF) + ", charge=0");
            final BigDecimal exchangeRate;
            if (!PF_CURRENCY.equals(quote.getCurrency().getSymbolIso())) {
                exchangeRate = this.isoCurrencyConversionProvider.getConversion(quote.getCurrency().getSymbolIso(), PF_CURRENCY, position.getOrderdate()).getRate().getValue();
            }
            else {
                exchangeRate = BigDecimal.ONE;
            }
            this.insertOrder.insert(userid, positionid, position.getOrdervalue(), exchangeRate, position.getVolume(), true, position.getOrderdate());
        }
    }

    private void insertWatchlist(ImportPortfolio ip, long userid) {
//        System.out.println("insert watchlist: userid=<id from above user insert>, name=" + ip.getListname() + ", iswatchlist=true");
        final long watchlistid = this.insertPortfolio.insert(userid, ip.getListname(), true, PF_CURRENCY);

        for (final ImportPosition position : ip.getPositions()) {
            final Instrument instrument = this.instrumentsByWkn.get(position.getWkn());
            if (instrument == null) {
                continue;
            }
            final Quote quote = getQuote(position, instrument);

            if (quote == null) {
                continue;
            }
//            System.out.println("insert watchlist element: userid=<id from above user insert>, " +
//                    "portfolioid=<id from above watchlist insert>, qid=" + quote.getId()
//                    + ", iid=" + instrument.getId() + ", isquotedperpercent=" + getQuotedPerPercent(instrument) + ", note=" + position.getNotiz());
            this.insertPosition.insert(userid, watchlistid, quote.getId(), instrument.getId(), getQuotedPerPercent(instrument), position.getNotiz());
        }
    }

    private boolean getQuotedPerPercent(Instrument instrument) {
        for (Quote quote : instrument.getQuotes()) {
            if (quote.getMinimumQuotationSize().getUnit() == MinimumQuotationSize.Unit.PERCENT) {
                return true;
            }
        }
        return false;
    }

    private Quote getQuote(ImportPosition position, Instrument instrument) {
        for (final String vwdmarket : UserDataReader.MARKETS.get(position.getMarket())) {
            for (final Quote quote : instrument.getQuotes()) {
                if (vwdmarket.equals(quote.getSymbolVwdfeedMarket()) && position.getCurrency().equals(quote.getCurrency().getSymbolIso())) {
                    return quote;
                }
            }
        }

        for (final String vwdmarket : UserDataReader.MARKETS.get(position.getMarket())) {
            for (final Quote quote : instrument.getQuotes()) {
                if (vwdmarket.equals(quote.getSymbolVwdfeedMarket())) {
                    return quote;
                }
            }
        }

        for (final Quote quote : instrument.getQuotes()) {
            if (position.getCurrency().equals(quote.getCurrency().getSymbolIso())) {
                return quote;
            }
        }

        System.out.println("no quote by market: " + instrument.getId() + "/" + position.getWkn() + "@" + position.getMarket());
        return null;
    }

    private class InsertUser extends SqlUpdate {
        public InsertUser() {
            super(getDataSource(), "INSERT INTO users (login, password, companyid, createdon, lastloginon, costcenter) VALUES (?, '', ?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.VARCHAR));
            setReturnGeneratedKeys(true);
            compile();
        }

        public long insert(String login, int companyid, Date createdOn, String costcenter) {
            final KeyHolder key = new GeneratedKeyHolder();
            this.update(new Object[]{
                    login,
                    companyid,
                    createdOn,
                    createdOn,
                    costcenter
            }, key);
            return key.getKey().longValue();
        }

    }

    private class InsertPortfolio extends SqlUpdate {
        public InsertPortfolio() {
            super(getDataSource(), "INSERT INTO portfolios " +
                    "(userid, name, iswatchlist, currencycode,cash)" +
                    " VALUES (?, ?, ?, ?, '0')");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.BOOLEAN));
            declareParameter(new SqlParameter(Types.VARCHAR));
            setReturnGeneratedKeys(true);
            compile();
        }

        public long insert(long userid, String listname, boolean isWatchlist, String currencyCode) {
            final KeyHolder key = new GeneratedKeyHolder();
            this.update(new Object[]{
                    userid,
                    listname,
                    isWatchlist,
                    currencyCode
            }, key);
            return key.getKey().longValue();
        }
    }

    private class InsertPosition extends SqlUpdate {
        public InsertPosition() {
            super(getDataSource(), "INSERT INTO portfoliopositions " +
                    "(userid, portfolioid, quoteid, instrumentid, isquotedperpercent, note)" +
                    " VALUES (?, ?, ?, ?, ?, ?)");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.BOOLEAN));
            declareParameter(new SqlParameter(Types.VARCHAR));
            setReturnGeneratedKeys(true);
            compile();
        }

        public long insert(long userid, long portfolioid, long qid, long iid, boolean isQuotedPerPercent, String note) {
            final KeyHolder key = new GeneratedKeyHolder();
            this.update(new Object[]{
                    userid,
                    portfolioid,
                    qid,
                    iid,
                    isQuotedPerPercent,
                    note
            }, key);
            return key.getKey().longValue();
        }
    }

    private class InsertOrder extends SqlUpdate {
        public InsertOrder() {
            super(getDataSource(), "INSERT INTO orders " +
                    "(userid, portfoliopositionid, price, exchangerate, volume, isbuy, ondate, charge)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, '0')");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.BOOLEAN));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            setReturnGeneratedKeys(true);
            compile();
        }

        public long insert(long userid, long positionid, BigDecimal price, BigDecimal exchangeRate, BigDecimal volume, boolean isBuy, YearMonthDay orderDate) {
            final KeyHolder key = new GeneratedKeyHolder();
            this.update(new Object[]{
                    userid,
                    positionid,
                    toPlainString(price),
                    toPlainString(exchangeRate),
                    toPlainString(volume),
                    isBuy,
                    toDate(orderDate.toDateTimeAtMidnight())
            }, key);
            return key.getKey().longValue();
        }
    }

    private static Date toDate(DateTime dt) {
        return dt != null ? dt.toDate() : null;
    }

    private static String toPlainString(BigDecimal bd) {
        return (bd != null) ? bd.toPlainString() : null;
    }
}
