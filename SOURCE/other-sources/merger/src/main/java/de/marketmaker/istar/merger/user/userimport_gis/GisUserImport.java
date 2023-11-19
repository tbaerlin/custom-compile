/*
 * GisUserImport.java
 *
 * Created on 23.10.2008 11:16:39
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user.userimport_gis;

import de.marketmaker.istar.common.spring.ApplicationObjectSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.util.IsinUtil;
import de.marketmaker.istar.domainimpl.CurrencyDp2;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import de.marketmaker.istar.domainimpl.instrument.MinimumQuotationSizeDp2;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataProvider;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataRequest;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataResponse;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import static de.marketmaker.istar.merger.Constants.MC;
import static de.marketmaker.istar.merger.Constants.ONE_HUNDRED;
import de.marketmaker.istar.merger.alert.Alert;
import de.marketmaker.istar.merger.alert.AlertProvider;
import de.marketmaker.istar.merger.alert.DeleteAlertUserRequest;
import de.marketmaker.istar.merger.alert.DeleteAlertUserResponse;
import de.marketmaker.istar.merger.alert.RetrieveAlertUserRequest;
import de.marketmaker.istar.merger.alert.RetrieveAlertUserResponse;
import de.marketmaker.istar.merger.alert.UpdateAlertRequest;
import de.marketmaker.istar.merger.alert.UpdateAlertResponse;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.UserProvider;
import de.marketmaker.istar.merger.user.AddOrderCommand;
import de.marketmaker.istar.merger.user.AddPortfolioCommand;
import de.marketmaker.istar.merger.user.AddPositionCommand;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class GisUserImport extends ApplicationObjectSupport implements InitializingBean, Lifecycle {

    private UserProvider userProvider;

    private ExecutorService executorService;

    private int numImported = 0;

    private List<File> files;

    private File instrumentFile;

    private EasytradeInstrumentProvider easytradeInstrumentProvider;

    private AlertProvider alertProvider;

    private UserMasterDataProvider userMasterDataProvider;

    private int companyid = 7;

    private Map<String, InstrumentStub> instruments = new HashMap<>();

    private Map<String, Counter> invalidSymbols = new HashMap<>();

    private Map<String, String> exchanges = new HashMap<>();

    private int numIdentified = 0;

    private int numNotIdentified = 0;

    private File auxIdentifications;

    private Future<Object> future;

    private boolean dryRun = true;

    private Set<String> usersToImport = new HashSet<>();

    private Set<String> usersNotToImport = new HashSet<>();

    private File usersToImportFile = null;

    private File usersNotToImportFile = null;

    private Map<String, Alert> alerts = new HashMap<>();

    protected String xun;

    private static class Counter {
        int count = 1;

        void inc() {
            count++;
        }

        public int getCount() {
            return count;
        }
    }

    {
        exchanges.put("AFF", "IT");
        exchanges.put("ASX", "NL");
        exchanges.put("AUS", "AU");
        exchanges.put("BER", "BLN");
        exchanges.put("BRU", "BL");
        exchanges.put("CSE", "DK");
        exchanges.put("C48", "FFMST");
        exchanges.put("DFK", "FONDS");
        exchanges.put("DUS", "DDF");
        exchanges.put("EAI", "WIEN");
        exchanges.put("EAV", "WIEN");
        exchanges.put("ETR", "ETR,EUS,EEU");
        exchanges.put("EWX", "EUWAX");
        exchanges.put("FSE", "FFMST,FFM,FFMFO");
        exchanges.put("FX1", "FXVWD");
        exchanges.put("HAM", "HBG");
        exchanges.put("HAN", "HNV");
        exchanges.put("HSE", "FI");
        exchanges.put("LIS", "PT");
        exchanges.put("LSE", "UK,UKINT");
        exchanges.put("ISE", "UK,UKINT");
        exchanges.put("LUX", "LU");
        exchanges.put("MIX", "IT");
        exchanges.put("MUN", "MCH");
        exchanges.put("NAS", "Q,IQ");
        exchanges.put("NYS", "N,DJNCB,GMN");
        exchanges.put("PSE", "FR");
        exchanges.put("SCM", "ES");
        exchanges.put("SIX", "ES");
        exchanges.put("SSE", "SW");
        exchanges.put("STU", "STG,EUWAX");
        exchanges.put("TOR", "TO");
        exchanges.put("TSE", "TK");
        exchanges.put("ZRH", "CH");
    }

    public void setFile(File file) {
        if (file.isDirectory()) {
            setFiles(Arrays.asList(file.listFiles()));
        }
        else {
            this.files = Collections.singletonList(file);
        }
    }

    public void setUsersToImportFile(File usersToImportFile) {
        this.usersToImportFile = usersToImportFile;
    }

    public void setUsersNotToImportFile(File usersNotToImportFile) {
        this.usersNotToImportFile = usersNotToImportFile;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public void setInstrumentFile(File instrumentFile) {
        this.instrumentFile = instrumentFile;
    }

    public void setAuxIdentifications(File auxIdentifications) {
        this.auxIdentifications = auxIdentifications;
    }

    public void setAlertProvider(AlertProvider alertProvider) {
        this.alertProvider = alertProvider;
    }

    public void setUserMasterDataProvider(UserMasterDataProvider userMasterDataProvider) {
        this.userMasterDataProvider = userMasterDataProvider;
    }

    public void setEasytradeInstrumentProvider(
            EasytradeInstrumentProvider easytradeInstrumentProvider) {
        this.easytradeInstrumentProvider = easytradeInstrumentProvider;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.usersNotToImportFile != null) {
            readUsersNotToImport();
        }
        if (this.usersToImportFile != null) {
            readUsersToImport();
        }
        if (instrumentFile != null && instrumentFile.exists()) {
            final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(this.instrumentFile));
            this.instruments = (Map<String, InstrumentStub>) ois.readObject();
            ois.close();
            this.logger.info("<afterPropertiesSet> read " + this.instruments.size() + " instruments");
        }

        if (this.auxIdentifications != null) {
            Properties p = new Properties();
            final FileInputStream fis = new FileInputStream(this.auxIdentifications);
            p.load(fis);
            fis.close();
            Map<String, InstrumentStub> aux = new HashMap<>();
            for (Object o : p.keySet()) {
                final String key = (String) o;
                if (this.instruments.containsKey(key)) {
                    continue;
                }
                final String qid = (String) p.get(key);

                final InstrumentStub stub = identify(qid, SymbolStrategyEnum.QID, null, null);
                aux.put(key, stub);
            }
            this.instruments.putAll(aux);
            this.logger.info("<afterPropertiesSet> added " + aux);
        }
    }

    private void readUsersNotToImport() throws Exception {
        readUsers(this.usersNotToImportFile, this.usersNotToImport);
        this.logger.info("<readUsersNotToImport> " + this.usersNotToImport);
    }

    private void readUsersToImport() throws Exception {
        readUsers(this.usersToImportFile, this.usersToImport);
        this.logger.info("<readUsersToImport> " + this.usersToImport);
    }

    private void readUsers(File f, Set<String> set) throws Exception {
        final Scanner s = new Scanner(f, "UTF8");
        while (s.hasNextLine()) {
            final String uid = s.nextLine().trim().toUpperCase();
            if (uid.length() > 0) {
                set.add(uid);
            }
        }
        s.close();
    }

    @Override
    public boolean isRunning() {
        return this.executorService != null;
    }

    public void start() {
        this.executorService = Executors.newSingleThreadExecutor();
        this.future = this.executorService.submit(new Callable<Object>() {
            public Object call() throws Exception {
                try {
                    importUsers();
                    return null;
                } catch (Exception e) {
                    logger.error("<call> failed", e);
                    throw e;
                }
                finally {
                    new Thread(new Runnable() {
                        public void run() {
                            ((ConfigurableApplicationContext) getApplicationContext()).close();
                        }
                    }).start();
                }
            }
        });
    }

    public void stop() {
        this.logger.info("<stop> ...");
        try {
            this.future.get();
        } catch (Exception e) {
            this.logger.error("<stop> execution failed", e);
        }
        this.executorService.shutdown();
        if (instrumentFile != null) {
            writeInstruments();
        }
        this.logger.info("<stop> Identified " + this.numIdentified + " of " + (this.numNotIdentified + this.numIdentified));
        final File file = exportUnknown();
        this.logger.info("<stop> wrote unknown files to " + file.getAbsolutePath());
    }

    protected void writeInstruments() {
        try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.instrumentFile))) {
            oos.writeObject(this.instruments);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected File exportUnknown() {
        final File file = new File(System.getProperty("java.io.tmpdir"), "unknown.txt");
        try (PrintWriter pw = new PrintWriter(file)) {
            for (Map.Entry<String, Counter> entry : invalidSymbols.entrySet()) {
                pw.println(entry.getValue().count + "\t" + entry.getKey());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    String getChildTextTrim(Element e, String s) {
        return getChildTextTrim(e, s, null);
    }

    String getChildTextTrim(Element e, String s, String defaultValue) {
        final String result = e.getChildTextTrim(s);
        return result != null ? result : defaultValue;
    }

    private void getPosition(Portfolio pf, Element position) {
        final String anzahl = getChildTextTrim(position, "N_Anzahl");
        final String isin = getChildTextTrim(position, "isin");
        final String symbol = getChildTextTrim(position, "N_Symbol");

        final InstrumentStub stub = identify(isin, symbol);
        if (stub == InstrumentStub.NULL || stub == null) {
            return;
        }

        getLimits(stub, position);

        Position pos = new Position();
        pos.stub = stub;

        pf.positions.add(pos);

        final Element histElement = position.getChild("C_Historie");
        if (histElement == null) return;

        final List<Element> hists = histElement.getChildren();
        for (Element hist : hists) {
            final String num = getChildTextTrim(hist, "N_Anzahl");
            if ("0".equals(num)) {
                continue;
            }
            final String note = getChildTextTrim(hist, "N_Bemerkung");
            String datum = getChildTextTrim(hist, "N_Datum");
            final String kurs = getChildTextTrim(hist, "N_Kurs");
            final String typ = getChildTextTrim(hist, "N_Typ");

            if (!StringUtils.hasText(kurs) || !StringUtils.hasText(typ)) {
                continue;
            }

            if (!StringUtils.hasText(datum)) {
                // tag is <C_date>
                datum = hist.getName().substring(2);
            }

            Order o = new Order();
            o.buy = "Kauf".equals(typ);
            o.anzahl = new BigDecimal(num);
            o.date = new DateTime(Long.parseLong(datum) * 1000L);
            o.note = note;
            o.price = new BigDecimal(kurs);

            if (stub.currency == null) {
                this.logger.info("<getPosition> --------- EXCH FROM null " + stub.qid);
            }
            else if (!stub.currency.equals("EUR")) {
                this.logger.info("<getPosition> --------- EXCH FROM " + stub.currency);
                // get exchange rate...
            }

            pos.orders.add(o);
        }
    }

    private void getLimits(InstrumentStub stub, Element position) {
        BigDecimal min = parseLimit(position.getChildTextTrim("N_LimitMinus"));
        BigDecimal minPct = parseLimit(position.getChildTextTrim("N_LimitMinusPer"));
        BigDecimal max = parseLimit(position.getChildTextTrim("N_LimitPlus"));
        BigDecimal maxPct = parseLimit(position.getChildTextTrim("N_LimitPlusPer"));
        if (min == null && minPct == null && max == null && maxPct == null) {
            return;
        }

        Alert alert = this.alerts.get(stub.vwdcode);
        BigDecimal lower = null;
        BigDecimal lowerPct = null;
        BigDecimal upper = null;
        BigDecimal upperPct = null;
        BigDecimal referenceValue = null;
        if (min != null) {
            if (minPct != null) {
                lowerPct = minPct.multiply(ONE_HUNDRED, MC);
                referenceValue = min.add(minPct.multiply(min), MC);
            }
            else {
                lower = min;
                referenceValue = min;
            }
        }
        if (max != null) {
            if (maxPct != null) {
                upperPct = maxPct.multiply(ONE_HUNDRED, MC);
                referenceValue = max.subtract(maxPct.multiply(max), MC);
            }
            else {
                upper = max;
                referenceValue = max;
            }
        }

        if (alert == null) {
            alert = new Alert();
            this.alerts.put(stub.vwdcode, alert);
            alert.setVwdCode(stub.vwdcode);
            alert.setName("Limit " + this.alerts.size());
        }
        else {
            this.logger.info("<getLimits> overwriting for " + this.xun + ": " + alert);
        }
        // we allow only one limit per vwdcode, if we import multiple the last one wins...
        alert.setLowerBoundary(lower);
        alert.setLowerBoundaryPercent(lowerPct);
        alert.setUpperBoundary(upper);
        alert.setUpperBoundaryPercent(upperPct);
        alert.setReferenceValue(referenceValue);
    }

    private BigDecimal parseLimit(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        BigDecimal result = new BigDecimal(s);
        return result.compareTo(BigDecimal.ZERO) != 0 ? result : null;
    }

    private InstrumentStub identify(String isin, String tickerWithExch) {
        final InstrumentStub result = doIdentify(isin, tickerWithExch);
        if (result != InstrumentStub.NULL) {
            this.numIdentified++;
        }
        else {
            this.numNotIdentified++;
        }
        return result;
    }

    private InstrumentStub doIdentify(String isin, String tickerWithExch) {
        final String key = isin + "|" + tickerWithExch;
        final Counter counter = this.invalidSymbols.get(key);
        if (counter != null) {
            counter.inc();
            return InstrumentStub.NULL;
        }

        boolean isIsin = IsinUtil.isIsin(isin);
        if (!isIsin && !StringUtils.hasText(tickerWithExch)) {
            ackNoHit(key);
            return InstrumentStub.NULL;
        }

        final InstrumentStub stub = this.instruments.get(key);
        if (stub != null) return stub;


        final int n = tickerWithExch.indexOf('.');
        if (n == -1 && !isIsin) {
            ackNoHit(key);
            return InstrumentStub.NULL;
        }
        final String ticker = tickerWithExch.substring(0, n);
        final String market = tickerWithExch.substring(n + 1);
        if (isin == null && IsinUtil.isIsin(ticker)) {
            isin = ticker;
            isIsin = true;
        }

        try {
            String symbol = isIsin ? isin : ticker;
            final SymbolStrategyEnum strategy = symbol == isin
                    ? SymbolStrategyEnum.ISIN : SymbolStrategyEnum.WKN;
            final String mappings = this.exchanges.get(market);
            final String marketStrategy = mappings != null ? "market:" + mappings : null;

            final InstrumentStub result = identify(symbol, strategy, market, marketStrategy);
            this.instruments.put(key, result);
            return result;

        } catch (UnknownSymbolException use) {
            ackNoHit(key);
            return InstrumentStub.NULL;
        } catch (Exception e) {
            this.logger.error("<identify> failed", e);
            return InstrumentStub.NULL;
        }
    }

    private InstrumentStub identify(String symbol, SymbolStrategyEnum strategy,
            String market, String marketStrategy) {
        final Quote q = this.easytradeInstrumentProvider.identifyQuote(symbol,
                strategy, market, marketStrategy);

        final InstrumentStub result = new InstrumentStub(q);
        result.quotedPerPercent = q.getMinimumQuotationSize().isUnitPercent();
        return result;
    }

    private void ackNoHit(String key) {
        this.logger.warn("no hit for: '" + key + "'");
        this.invalidSymbols.put(key, new Counter());
    }

    private void importUser(Element userElement) {
        this.alerts.clear();

        String n_xun = getChildTextTrim(userElement, "N_Xun");
        final String vorname = getChildTextTrim(userElement, "N_Xvorname");
        final String name = getChildTextTrim(userElement, "N_Xname");
        if (!StringUtils.hasText(n_xun)) {
            this.logger.warn("<importUser> No xun for " + vorname + " " + name);
            return;
        }
        this.xun = n_xun.toUpperCase();
        if (!this.usersToImport.isEmpty() && !this.usersToImport.contains(this.xun)) {
            return;
        }
        if (this.usersNotToImport.contains(this.xun)) {
            return;
        }
        this.logger.info("<importUser> xun = " + this.xun + ", " + vorname + " " + name);

        final Element depotsElement = userElement.getChild("C_Depot");
        if (depotsElement == null) {
            this.logger.info("<importUser> no depots for " + this.xun);
            return;
        }

        List<Portfolio> pfs = getPortfolios(depotsElement);
        if (pfs.isEmpty()) {
            this.logger.info("<importUser> no portfolios for " + this.xun);
            // no user will be created, no pf and no wl
            return;
        }

        if (this.dryRun) {
            return;
        }

        UserContext uc = this.userProvider.getUserContext(this.xun.toUpperCase(), this.companyid);
        if (!uc.getUser().getWatchlists().isEmpty() || !uc.getUser().getPortfolios().isEmpty()) {
            this.userProvider.removeUser(uc.getUser().getId());
            uc = this.userProvider.getUserContext(this.xun.toUpperCase(), this.companyid);
        }
        long uid = uc.getUser().getId();

        final int numWls = importPortfolios(uid, pfs, true);
        final int numPfs = importPortfolios(uid, pfs, false);
        if (numPfs == 0) {
            createPortfolio(uid, "Portfolio 1", false);
        }

        importAlerts();
        this.logger.info("<importUser> imported " + this.xun + ", " + ++numImported);
    }

    private void importAlerts() {
        if (this.alerts.isEmpty() || this.alertProvider == null) {
            return;
        }
        UserMasterDataResponse response =
                this.userMasterDataProvider.getUserMasterData(UserMasterDataRequest.forLogin(this.xun, "10"));
        if (!response.isValid()) {
            this.logger.warn("<importAlerts> failed getting master data for " + this.xun);
            return;
        }
        String vwdId = response.getMasterData().getVwdId();

        // bug in deleteAlertUser causes an exception for non-existent user
        if (!ensureAlertUserExists(vwdId)) {
            return;
        }
        DeleteAlertUserResponse deleteUserResponse
                = this.alertProvider.deleteAlertUser(new DeleteAlertUserRequest(vwdId));
        if (!deleteUserResponse.isValid()) {
            this.logger.warn("<importAlerts> failed to delete user " + this.xun + " / " + vwdId);
            return;
        }
        if (!ensureAlertUserExists(vwdId)) {
            return;
        }
        for (Alert alert : alerts.values()) {
            alert.setFieldId(guessFieldId(alert.getVwdCode()));
            UpdateAlertRequest req = new UpdateAlertRequest("7", vwdId);
            req.setAlert(alert);
            UpdateAlertResponse updateAlertResponse = this.alertProvider.updateAlert(req);
            if (!updateAlertResponse.isValid()) {
                this.logger.warn("<importAlerts> failed to insert " + alert + "for " + this.xun + " / " + vwdId);
            }
        }
    }

    private boolean ensureAlertUserExists(String vwdId) {
        RetrieveAlertUserResponse retrieveAlertUserResponse
                = this.alertProvider.retrieveAlertUser(new RetrieveAlertUserRequest(vwdId, Locale.GERMANY));
        if (!retrieveAlertUserResponse.isValid()) {
            this.logger.warn("<ensureAlertUserExists> failed " + this.xun + " / " + vwdId);
            return false;
        }
        return true;
    }

    private int guessFieldId(String code) {
        if (code.contains(".FONDS")) {
            return VwdFieldDescription.ADF_Ruecknahme.id();
        }
        return VwdFieldDescription.ADF_Bezahlt.id();
    }


    private Long createPortfolio(long uid, String name, boolean watchlist) {
        final AddPortfolioCommand awc = new AddPortfolioCommand();
        awc.setWatchlist(watchlist);
        awc.setCurrencycode("EUR");
        awc.setName(name);
        awc.setUserid(uid);
        return this.userProvider.addPortfolio(awc);
    }

    private int importPortfolios(long uid, List<Portfolio> pfs, boolean asWatchlists) {
        int result = 0;
        for (Portfolio pf : pfs) {
            if (!asWatchlists && !pf.withOrders()) {
                continue;
            }
            result++;

            String name = pf.name;
            if (!StringUtils.hasText(name)) {
                name = asWatchlists ? "Watchlist" : "Portfolio";
            }

            final Long pfid = createPortfolio(uid, name, asWatchlists);

            for (Position position : pf.positions) {
                insertPosition(uid, pfid, position);

                if (!asWatchlists) {
                    for (Order order : position.orders) {
                        insertOrder(uid, pfid, position, order);
                    }
                }
            }
        }
        return result;
    }

    private void insertOrder(long uid, Long pfid, Position position,
            Order order) {
        final AddOrderCommand aoc = new AddOrderCommand();
        aoc.setBuy(order.buy);
        aoc.setDate(order.date);
        //                    aoc.setExchangeRate();
        aoc.setQuote(position.stub.toQuote());
        aoc.setPortfolioid(pfid);
        aoc.setPrice(order.price);
        aoc.setUserid(uid);
        aoc.setVolume(order.anzahl);
        this.userProvider.addOrder(aoc);
    }

    private void insertPosition(long uid, Long pfid, Position position) {
        final AddPositionCommand apc = new AddPositionCommand();
        apc.setUserid(uid);
        apc.setPortfolioid(pfid);
        apc.setQuote(position.stub.toQuote());
        this.userProvider.insertPosition(apc);
    }

    private List<Portfolio> getPortfolios(Element depotsElement) {
        List<Portfolio> result = new ArrayList<>();

        final List<Element> depotElements = depotsElement.getChildren();
        for (Element depotElement : depotElements) {
            if (!depotElement.getName().startsWith("C_")) {
                continue;
            }

            final Portfolio pf = new Portfolio();
            pf.name = getChildTextTrim(depotElement, "N_depotname");
            if (!StringUtils.hasText(pf.name)) {
                pf.name = getChildTextTrim(depotElement, "N_Listenname");
            }

            final List<Element> positionElements = depotElement.getChildren();
            for (Element positionElement : positionElements) {
                if (!positionElement.getName().startsWith("C_")) {
                    continue;
                }
                getPosition(pf, positionElement);
            }

            if (!pf.positions.isEmpty()) {
                result.add(pf);
            }
        }

        return result;
    }

    private void importUsers() {
        final Profile profile = ProfileFactory.valueOf(true);
        RequestContextHolder.setRequestContext(new RequestContext(profile, LbbwMarketStrategy.INSTANCE));

        for (File file : files) {
            importUsers(file);
        }
    }

    private void importUsers(File file) {
        final SAXBuilder saxBuilder = new SAXBuilder();
        final Document document;
        try {
            document = saxBuilder.build(new FileInputStream(file));
        } catch (Exception e) {
            this.logger.error("<importUsers> failed for " + file.getAbsolutePath(), e);
            return;
        }

        this.logger.info("<importUsers> from " + file.getName());
        final List<Element> nodes = document.getRootElement().getChildren("user");
        for (Element user : nodes) {
            importUser(user);
        }
    }

    public void setUserProvider(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    public void setCompanyid(int companyid) {
        this.companyid = companyid;
    }

    private static class Portfolio {
        String name;

        List<Position> positions = new ArrayList<>();

        boolean withOrders() {
            for (Position position : positions) {
                if (position.withOrders()) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class Position {
        InstrumentStub stub;

        String note;

        List<Order> orders = new ArrayList<>();

        void add(Order o) {
            this.orders.add(o);
        }

        boolean withOrders() {
            return !this.orders.isEmpty();
        }
    }

    private static class Order {
        BigDecimal anzahl;

        boolean buy;

        BigDecimal price;

        DateTime date;

        String note;
    }

    private static class InstrumentStub implements Serializable {
        protected static final long serialVersionUID = -8515660087962985196L;

        long iid;

        long qid;

        String currency;

        boolean quotedPerPercent = false;

        InstrumentTypeEnum type;

        String vwdcode;

        final static InstrumentStub NULL = new InstrumentStub();

        private InstrumentStub() {
        }

        public InstrumentStub(Quote q) {
            this(q.getInstrument().getId(), q.getId(), q.getCurrency().getSymbolIso(),
                    q.getInstrument().getInstrumentType(), q.getSymbolVwdcode());
        }

        public InstrumentStub(long iid, long qid, String currency,
                InstrumentTypeEnum type, String vwdcode) {
            this.iid = iid;
            this.qid = qid;
            this.currency = currency;
            this.type = type;
            this.vwdcode = vwdcode;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InstrumentStub that = (InstrumentStub) o;
            return this.qid == that.qid;
        }

        public int hashCode() {
            return (int) this.qid;
        }

        public String toString() {
            return "[" + this.iid + ".iid, "
                    + this.qid + ".qid, "
                    + this.currency + ", %=" + this.quotedPerPercent
                    + "]";
        }

        public Quote toQuote() {
            QuoteDp2 result = new QuoteDp2(this.qid) {
                @Override
                public MinimumQuotationSize getMinimumQuotationSize() {
                    return new MinimumQuotationSizeDp2(BigDecimal.ONE,
                            quotedPerPercent ? MinimumQuotationSize.Unit.PERCENT : MinimumQuotationSize.Unit.POINT,
                            new CurrencyDp2(0L, ""));
                }
            };
            result.setInstrument(new InstrumentDp2(this.iid) {
                @Override
                public InstrumentTypeEnum getInstrumentType() {
                    return InstrumentTypeEnum.NON; // TODO
                }
            });
            ((InstrumentDp2) (result.getInstrument())).addQuote(result);
            return result;
        }
    }
}
