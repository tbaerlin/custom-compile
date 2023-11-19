/*
 * UserDataReader.java
 *
 * Created on 26.03.13 07:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user.userexport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.VwdProfileFactory;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.PortfolioEvaluationProvider;
import de.marketmaker.istar.merger.provider.UserProvider;
import de.marketmaker.istar.merger.user.EvaluatedPortfolio;
import de.marketmaker.istar.merger.user.EvaluatedPosition;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.PortfolioPosition;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy;

/**
 * Exporter for dm[xml] user data. Current usage: LBBW portfolio and watchlist export.
 * @author tkiesgen
 */
public class UserDataWriter implements InitializingBean {
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private UserProvider userProvider;

    private PortfolioEvaluationProvider evaluationProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private File baseDir;

    private int istarCompanyId;

    private File profileFile;

    @SuppressWarnings("UnusedDeclaration")
    public void setProfileFile(File profileFile) {
        this.profileFile = profileFile;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setEvaluationProvider(PortfolioEvaluationProvider evaluationProvider) {
        this.evaluationProvider = evaluationProvider;
    }

    public void setIstarCompanyId(int istarCompanyId) {
        this.istarCompanyId = istarCompanyId;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setUserProvider(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    public void afterPropertiesSet() throws Exception {
        this.df.applyLocalizedPattern("0.######");

        final Profile profile = getProfile();
        RequestContext rc = new RequestContext(profile, LbbwMarketStrategy.INSTANCE);
        rc.disableSharedIntradayMap();
        RequestContextHolder.setRequestContext(rc);

        final List<Long> userIds = this.userProvider.getUserIds(this.istarCompanyId);
        this.logger.info("<afterPropertiesSet> #users: " + userIds.size());

        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(this.baseDir, "lbbw-export.csv")), "UTF-8"));

        writer.println("#login;portfolio/watchlist;listId;listname;portfolioCurrency;cash;initialInvestment;positionId;isin;vwdCode;vwdMarket;name;currency;"
                + "currentValue;currentDate;quantity;currentValue;currentValueInPortfolioCurrency;lastOrderDate;avgOrderPrice;avgOrderPriceInPortfolioCurrency;"
                + "orderValue;orderValueInPortfolioCurrency");

        int count = 0;
        for (final Long userId : userIds) {
            exportUser(userId, writer);
            count++;

            if (count % 1000 == 0) {
                this.logger.info("<afterPropertiesSet> " + count + "/" + userIds.size());
            }
        }

        writer.close();
    }

    private void exportUser(Long userId, PrintWriter writer) {
        final UserContext uc = this.userProvider.getUserContext(userId);
        final User user = uc.getUser();

        for (final Portfolio portfolio : user.getPortfolios()) {
            final EvaluatedPortfolio evaluatedPortfolio = this.evaluationProvider.evaluate(portfolio, false);

            for (EvaluatedPosition ep : evaluatedPortfolio.getNonEmptyPositions()) {
                writer.println(formatString(user.getLogin()) + ";"
                        + "portfolio" + ";"
                        + portfolio.getId() + ";"
                        + formatString(portfolio.getName()) + ";"
                        + evaluatedPortfolio.getPortfolioCurrency() + ";"
                        + formatPrice(evaluatedPortfolio.getCash()) + ";"
                        + formatPrice(evaluatedPortfolio.getInitialInvestment()) + ";"
                        /*+ ep.getPosition().getId() + ";"*/
                        + formatString(ep.getQuote().getInstrument().getSymbolIsin()) + ";"
                        + formatString(ep.getQuote().getSymbolVwdcode()) + ";"
                        + formatString(ep.getQuote().getSymbolVwdfeedMarket()) + ";"
                        + formatString(ep.getQuote().getInstrument().getName()) + ";"
                        + formatString(ep.getQuote().getCurrency().getSymbolIso()) + ";"
                        + formatPrice(ep.getCurrentPrice().getPrice().getValue()) + ";"
                        + formatDate(ep.getCurrentPrice().getPrice().getDate()) + ";"
                        + formatPrice(ep.getTotalVolume()) + ";"
                        + formatPrice(ep.getCurrentValue()) + ";"
                        + formatPrice(ep.getCurrentValueInPortfolioCurrency()) + ";"
                        + formatDate(ep.getLastOrderDate()) + ";"
                        + formatPrice(ep.getAverageOrderPrice()) + ";"
                        + formatPrice(ep.getAverageOrderPriceInPortfolioCurrency()) + ";"
                        + formatPrice(ep.getOrderValue()) + ";"
                        + formatPrice(ep.getOrderValueInPortfolioCurrency())
                );
            }
        }

        for (final Portfolio watchlist : user.getWatchlists()) {
            final List<PortfolioPosition> positions = new ArrayList<>(watchlist.getPositions());
            final List<Quote> quotes = getQuotes(positions);
            if (positions.isEmpty()) {
                continue;
            }

            for (int i = 0; i < positions.size(); i++) {
                final PortfolioPosition position = positions.get(i);
                final Quote quote = quotes.get(i);

                writer.println(formatString(user.getLogin()) + ";"
                        + "watchlist" + ";"
                        + watchlist.getId() + ";"
                        + formatString(watchlist.getName()) + ";"
                        + ";;;"
                        + position.getId() + ";"
                        + formatString(quote.getInstrument().getSymbolIsin()) + ";"
                        + formatString(quote.getSymbolVwdcode()) + ";"
                        + formatString(quote.getSymbolVwdfeedMarket()) + ";"
                        + formatString(quote.getInstrument().getName()) + ";"
                        + formatString(quote.getCurrency().getSymbolIso())
                );
            }
        }
    }

    private List<Quote> getQuotes(List<PortfolioPosition> positions) {
        final List<Long> qids = new ArrayList<>(positions.size());
        for (final PortfolioPosition position : positions) {
            qids.add(position.getQid());
        }

        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(qids);

        CollectionUtils.removeNulls(quotes, positions);
        return quotes;
    }

    private String formatString(String s) {
        if (s == null) {
            return "";
        }
        if (s.contains(";")) {
            return s.replace(";", "");
        }
        return s;
    }

    private String formatPrice(final BigDecimal price) {
        if (price == null) {
            this.logger.warn("<formatPrice> no price => return empty string");
            return "";
        }
        return this.df.format(price);
    }

    private String formatDate(final DateTime date) {
        return DTF.print(date);
    }

    private Profile getProfile() throws Exception {
        if (this.profileFile != null) {
            return new VwdProfileFactory().read(new FileInputStream(this.profileFile));
        }
        return ProfileFactory.valueOf(true);
    }

    public static void main(String[] args) throws Exception {
        final String value = new ClassPathResource("logback-userdatawriter.xml", UserDataWriter.class).getURL().toString();
        System.setProperty("logback.configurationFile", value);

        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(new String[]{
                new ClassPathResource("user-export-context.xml", UserDataWriter.class).getURL().toString()
        }, false);

        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        if (args.length > 0) {
            ppc.setLocation(new FileSystemResource(args[0]));
        }
        else {
            ppc.setLocation(new ClassPathResource("default.properties", UserDataWriter.class));
        }

        ac.addBeanFactoryPostProcessor(ppc);
        ac.refresh();
        ac.getBean("userDataWriter");
        ac.close();
    }
}

