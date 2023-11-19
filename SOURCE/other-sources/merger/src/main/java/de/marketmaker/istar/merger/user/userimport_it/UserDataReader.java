/*
 * UserDataReader.java
 *
 * Created on 26.03.13 07:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user.userimport_it;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.VwdProfileFactory;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.UserProvider;
import de.marketmaker.istar.merger.user.AddOrderCommand;
import de.marketmaker.istar.merger.user.AddPortfolioCommand;
import de.marketmaker.istar.merger.user.AddPositionCommand;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy;

/**
 * Helps migrating vwd IT user data to istar based systems
 * @author tkiesgen
 */
public class UserDataReader implements InitializingBean {
    protected static final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd/MM/yyyy");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private UserProvider userProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private File userFile;

    private File watchlistFile;

    private File portfolioFile;

    private int istarCompanyId;

    private File profileFile;

    @SuppressWarnings("UnusedDeclaration")
    public void setProfileFile(File profileFile) {
        this.profileFile = profileFile;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setUserFile(File userFile) {
        this.userFile = userFile;
    }

    public void setWatchlistFile(File watchlistFile) {
        this.watchlistFile = watchlistFile;
    }

    public void setPortfolioFile(File portfolioFile) {
        this.portfolioFile = portfolioFile;
    }

    public void setIstarCompanyId(int istarCompanyId) {
        this.istarCompanyId = istarCompanyId;
    }

    public void setUserProvider(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    public void afterPropertiesSet() throws Exception {
        final Map<String, String> records = readUserRecords();

        final Profile profile = getProfile();

        RequestContextHolder.setRequestContext(new RequestContext(profile, LbbwMarketStrategy.INSTANCE));

        final Map<String, ImportWatchlists> allWatchlists = readWatchlists();
        final Map<String, ImportPortfolios> allPortfolios = readPortfolios();

        for (final Map.Entry<String, String> entry : records.entrySet()) {
            final String login = entry.getKey();
            final String vwdId = entry.getValue();
            try {
                final ImportWatchlists wls = allWatchlists.get(login);
                final ImportPortfolios pfs = allPortfolios.get(login);
                migrate(wls, pfs, login, vwdId);
                this.logger.info("<migrate> migrated " + login + "/" + vwdId);
            } catch (Exception e) {
                this.logger.error("<migrate> failed for " + login + "/" + vwdId, e);
            }
        }
    }

    private Profile getProfile() throws Exception {
        if (this.profileFile != null) {
            return new VwdProfileFactory().read(new FileInputStream(this.profileFile));
        }
        return ProfileFactory.valueOf(true);
    }

    private void migrate(ImportWatchlists wls, ImportPortfolios pfs, String login,
            String vwdId) throws Exception {
        final UserContext toDelete = this.userProvider.retrieveUserContext(vwdId, this.istarCompanyId);
        if (toDelete != null) {
            this.userProvider.removeUser(toDelete.getUser().getId());
        }

        final UserContext context = this.userProvider.getUserContext(vwdId, this.istarCompanyId);
        final User istarUser = context.getUser();

        boolean createdAnyWatchlist = false;

        if (wls != null) {
            for (final Map.Entry<String, ImportWatchlist> entry : wls.getWatchlists().entrySet()) {
                createdAnyWatchlist |= insertWatchlist(login, entry.getKey(), entry.getValue(), istarUser);
            }
        }

        if (!createdAnyWatchlist) {
            addWatchlist(istarUser.getId(), "Watchlist 1");
        }


        boolean createdAnyPortfolio = false;

        if (pfs != null) {
            for (final Map.Entry<String, ImportPortfolio> entry : pfs.getPortfolios().entrySet()) {
                createdAnyPortfolio |= insertPortfolio(login, entry.getValue(), istarUser);
            }
        }

        if (!createdAnyPortfolio) {
            addPortfolio(istarUser.getId(), "Portfolio 1", "EUR", BigDecimal.ZERO);
        }
    }

    private Map<String, ImportWatchlists> readWatchlists() throws Exception {
        final Scanner scanner = new Scanner(this.watchlistFile);

        scanner.nextLine(); //header

        final Map<String, ImportWatchlists> login2wls = new HashMap<>();
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final String[] tokens = line.split(Pattern.quote(","));

            final String login = tokens[0];
            final String watchlistName = tokens[1];
            final String vwdcode = tokens[2];

            ImportWatchlists wls = login2wls.get(login);
            if (wls == null) {
                wls = new ImportWatchlists();
                login2wls.put(login, wls);
            }
            wls.addElement(watchlistName, vwdcode);
        }

        scanner.close();

        return login2wls;
    }

    private Map<String, ImportPortfolios> readPortfolios() throws Exception {
        final Scanner scanner = new Scanner(this.portfolioFile);

        scanner.nextLine(); //header

        final Map<String, ImportPortfolios> login2pfs = new HashMap<>();
        final List<ImportOrder> orders = new ArrayList<>();
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final String[] tokens = line.split(Pattern.quote(","));

            final String login = tokens[0];
            final String portfolioName = tokens[1];
            final String liquidityStr = tokens[2];
            final String vwdcode = tokens[3];
            final String currency = tokens[4];
            final DateTime date = DTF.parseLocalDate(tokens[5]).toDateTime(new LocalTime(12, 0, 0, 0));
            final BigDecimal buyQuantity = new BigDecimal(tokens[6]);
            final BigDecimal sellQuantity = new BigDecimal(tokens[7]);
            final BigDecimal price = new BigDecimal(tokens[8]);
            final String exchangeRateStr = tokens[9];
            final BigDecimal exchangeRate = "0".equals(exchangeRateStr)
                    ? BigDecimal.ONE
                    : new BigDecimal(exchangeRateStr);

            final BigDecimal liquidity = "-1".equals(liquidityStr)
                    ? new BigDecimal(100 * 1000) // default: 100.000 EUR
                    : new BigDecimal(liquidityStr);

            final boolean buy;
            final BigDecimal quantity;
            if (sellQuantity.compareTo(BigDecimal.ZERO) > 0) {
                buy = false;
                quantity = sellQuantity;
            }
            else {
                buy = true;
                quantity = buyQuantity;
            }

            orders.add(new ImportOrder(login, portfolioName, liquidity, vwdcode, currency, date, buy, price, quantity, exchangeRate));
        }

        orders.sort(new Comparator<ImportOrder>() {
            @Override
            public int compare(ImportOrder o1, ImportOrder o2) {
                final int byLogin = o1.getLogin().compareTo(o2.getLogin());
                if (byLogin != 0) {
                    return byLogin;
                }
                final int byVwdcode = o1.getVwdcode().compareTo(o2.getVwdcode());
                if (byVwdcode != 0) {
                    return byVwdcode;
                }
                final int byDate = o1.getDate().compareTo(o2.getDate());
                if (byDate != 0) {
                    return byDate;
                }
                return o1.isBuy() && !o2.isBuy()
                        ? -1
                        : o2.isBuy() && !o1.isBuy()
                        ? 1
                        : 0;
            }
        });

        for (final ImportOrder order : orders) {
            ImportPortfolios pfs = login2pfs.get(order.getLogin());
            if (pfs == null) {
                pfs = new ImportPortfolios();
                login2pfs.put(order.getLogin(), pfs);
            }
            pfs.addElement(order.getName(), order.getCurrency(), order.getLiquidity(), order);
        }

        scanner.close();

        return login2pfs;
    }

    @SuppressWarnings({"unchecked"})
    private boolean insertPortfolio(String login, ImportPortfolio portfolio,
            User istarUser) throws Exception {
        final List<ImportOrder> orders = portfolio.getOrders();
        if (orders.isEmpty()) {
            return false;
        }

        final Long pfid = addPortfolio(istarUser.getId(), portfolio.getName(), portfolio.getCurrency(),
                portfolio.getLiquidity());

        for (ImportOrder order : orders) {
            final Quote q = identify(order.getVwdcode());
            if (q == null) {
                this.logger.warn("<insertPortfolio> user '" + login + "' unknown: " + order);
                continue;
            }

            final AddOrderCommand ocmd = new AddOrderCommand();

            ocmd.setBuy(order.isBuy());
            ocmd.setCharge(BigDecimal.ZERO);
            ocmd.setDate(order.getDate());
            ocmd.setExchangeRate(order.getExchangeRate());
            ocmd.setPortfolioid(pfid);
            ocmd.setPrice(order.getPrice());
            ocmd.setQuote(q);
            ocmd.setUserid(istarUser.getId());
            ocmd.setVolume(order.getQuantity());

            this.userProvider.addOrder(ocmd);
        }

        return true;
    }

    private Long addPortfolio(final long userId, final String name,
            final String currency, final BigDecimal cash) {
        final AddPortfolioCommand cmd = new AddPortfolioCommand();
        cmd.setUserid(userId);
        cmd.setWatchlist(false);
        cmd.setName(name);
        cmd.setCurrencycode(currency);
        cmd.setCash(cash);
        return this.userProvider.addPortfolio(cmd);
    }

    @SuppressWarnings({"unchecked"})
    private boolean insertWatchlist(String login, String watchlistName, ImportWatchlist watchlist,
            User istarUser) throws Exception {
        final List<String> vwdcodes = watchlist.getVwdcodes();
        if (vwdcodes.isEmpty()) {
            return false;
        }

        final Long wlid = addWatchlist(istarUser.getId(), watchlistName);

        for (final String vwdcode : vwdcodes) {
            final Quote q = identify(vwdcode);
            if (q == null) {
                this.logger.warn("<insertWatchlist> " + login + " unknown: " + vwdcode);
                continue;
            }

            final AddPositionCommand pcmd = new AddPositionCommand();
            pcmd.setUserid(istarUser.getId());
            pcmd.setPortfolioid(wlid);
            pcmd.setQuote(q);
            this.userProvider.insertPosition(pcmd);
        }
        return true;
    }

    private Long addWatchlist(final long userId, final String name) {
        final AddPortfolioCommand cmd = new AddPortfolioCommand();
        cmd.setUserid(userId);
        cmd.setWatchlist(true);
        cmd.setName(name);
        return this.userProvider.addPortfolio(cmd);
    }

    private Quote identify(String vwdcode) {
        try {
            return this.instrumentProvider.identifyQuote(vwdcode, SymbolStrategyEnum.VWDCODE, null);
        } catch (UnknownSymbolException e) {
            return null;
        }
    }

    private Map<String, String> readUserRecords() throws IOException {
        final FileInputStream fis = new FileInputStream(this.userFile);
        final Workbook input = new HSSFWorkbook(fis);
        fis.close();

        final Sheet sheet = input.getSheet("USER");

        if (sheet == null) {
            throw new IllegalArgumentException("sheet 'USER' not found");
        }

        final Row header = sheet.getRow(0);
        final int colNum = header.getLastCellNum();

        int columnVwdId = -1;
        int columnLogin = -1;
        for (int i = 0; i < colNum; i++) {
            final Cell cell = header.getCell(i);
            if ("vwd-Id".equals(cell.getStringCellValue())) {
                columnVwdId = i;
            }
            else if ("Kennung (Altsystem)".equals(cell.getStringCellValue())) {
                columnLogin = i;
            }
        }

        if (columnVwdId < 0 || columnLogin < 0) {
            throw new IllegalArgumentException("columns for vwdId and/or login not found");
        }

        final Map<String, String> login2vwdid = new HashMap<>();

        final int numRows = sheet.getPhysicalNumberOfRows();

        for (int i = 1; i < numRows; i++) {
            final Row row = sheet.getRow(i);
            final Cell vwdIdCell = row.getCell(columnVwdId);
            final Cell loginCell = row.getCell(columnLogin);
            if (row == null || vwdIdCell == null || loginCell == null) {
                continue;
            }

            final String vwdId = ((Number) vwdIdCell.getNumericCellValue()).intValue() + "";
            final String login = loginCell.getStringCellValue();
            if (StringUtils.hasText(login) && StringUtils.hasText(vwdId)) {
                login2vwdid.put(login, vwdId);
            }
        }
        this.logger.info("<readUserRecords> login2vwdid: " + login2vwdid);
        return login2vwdid;
    }

    private static String normalize(String vwdcode) {
        if (vwdcode.startsWith("/D")) {
            return vwdcode.substring(2);
        }

        return vwdcode;
    }

    public static class ImportWatchlists {
        private Map<String, ImportWatchlist> watchlists = new LinkedHashMap<>();

        public void addElement(String watchlistName, String vwdcode) {
            final ImportWatchlist wl = this.watchlists.get(watchlistName);
            if (wl != null) {
                wl.addVwdcode(vwdcode);
                return;
            }

            final ImportWatchlist newWl = new ImportWatchlist(watchlistName);
            newWl.addVwdcode(vwdcode);
            this.watchlists.put(watchlistName, newWl);
        }

        public Map<String, ImportWatchlist> getWatchlists() {
            return watchlists;
        }
    }

    public static class ImportWatchlist {
        private final String name;

        private final List<String> vwdcodes = new ArrayList<>();

        public ImportWatchlist(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void addVwdcode(String vwdcode) {
            this.vwdcodes.add(normalize(vwdcode));
        }

        public List<String> getVwdcodes() {
            return vwdcodes;
        }
    }

    public static class ImportPortfolios {
        private Map<String, ImportPortfolio> portfolios = new LinkedHashMap<>();

        public void addElement(String portfolioName, String currency, BigDecimal liquidity,
                ImportOrder order) {
            final ImportPortfolio pf = this.portfolios.get(portfolioName);
            if (pf != null) {
                pf.addOrder(order);
                return;
            }

            final ImportPortfolio newPf = new ImportPortfolio(portfolioName, currency, liquidity);
            newPf.addOrder(order);
            this.portfolios.put(portfolioName, newPf);
        }

        public Map<String, ImportPortfolio> getPortfolios() {
            return portfolios;
        }
    }

    public static class ImportPortfolio {
        private final String name;

        private final String currency;

        private final BigDecimal liquidity;

        private final List<ImportOrder> orders = new ArrayList<>();

        public ImportPortfolio(String name, String currency, BigDecimal liquidity) {
            this.name = name;
            this.currency = currency;
            this.liquidity = liquidity;
        }

        public String getName() {
            return name;
        }

        public String getCurrency() {
            return currency;
        }

        public BigDecimal getLiquidity() {
            return liquidity;
        }

        public void addOrder(ImportOrder order) {
            this.orders.add(order);
        }

        public List<ImportOrder> getOrders() {
            return orders;
        }
    }

    public static class ImportOrder {
        private final String login;

        private final String name;

        private final BigDecimal liquidity;

        private final String vwdcode;

        private final String currency;

        private final DateTime date;

        private final boolean buy;

        private final BigDecimal price;

        private final BigDecimal quantity;

        private final BigDecimal exchangeRate;

        public ImportOrder(String login, String name, BigDecimal liquidity, String vwdcode,
                String currency, DateTime date, boolean buy, BigDecimal price,
                BigDecimal quantity, BigDecimal exchangeRate) {
            this.login = login;
            this.name = name;
            this.liquidity = liquidity;
            this.vwdcode = vwdcode;
            this.currency = currency;
            this.date = date;
            this.buy = buy;
            this.price = price;
            this.quantity = quantity;
            this.exchangeRate = exchangeRate;
        }

        public String getLogin() {
            return login;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getLiquidity() {
            return liquidity;
        }

        public String getVwdcode() {
            return vwdcode;
        }

        public String getCurrency() {
            return currency;
        }

        public DateTime getDate() {
            return date;
        }

        public boolean isBuy() {
            return buy;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public BigDecimal getExchangeRate() {
            return exchangeRate;
        }

        @Override
        public String toString() {
            return "ImportOrder{" +
                    "login='" + login + '\'' +
                    ", name='" + name + '\'' +
                    ", liquidity=" + liquidity +
                    ", vwdcode='" + vwdcode + '\'' +
                    ", currency='" + currency + '\'' +
                    ", date=" + date +
                    ", buy=" + buy +
                    ", price=" + price +
                    ", quantity=" + quantity +
                    ", exchangeRate=" + exchangeRate +
                    '}';
        }
    }

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(new String[]{
                new ClassPathResource("user-import-context.xml", UserDataReader.class).getURL().toString()
        }, false);

        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        if (args.length > 0) {
            ppc.setLocation(new FileSystemResource(args[0]));
        }
        else {
            ppc.setLocation(new ClassPathResource("default.properties", UserDataReader.class));
        }

        ac.addBeanFactoryPostProcessor(ppc);
        ac.refresh();

        final UserDataReader reader = (UserDataReader) ac.getBean("userDataReader");

        ac.close();
    }
}

