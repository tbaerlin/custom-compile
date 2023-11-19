/*
 * FrontmarkSymbolexporter.java
 *
 * Created on 28.01.2015 10:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.exporttools.frontmark;

import de.marketmaker.istar.domain.data.NullSnapField;
import de.marketmaker.istar.domain.data.NullSnapRecord;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.IntradayServer;
import de.marketmaker.istar.feed.api.FeedConnector;
import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcConnectionManager;
import java.io.File;
import java.io.PrintWriter;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.instrument.BondDp2;
import de.marketmaker.istar.domainimpl.instrument.FundDp2;
import de.marketmaker.istar.domainimpl.instrument.ParticipationCertificateDp2;
import de.marketmaker.istar.domainimpl.instrument.StockDp2;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.feed.exporttools.CommonSymbolExporter;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;
import de.marketmaker.itools.amqprpc.helper.AmqpAppConfig;

/**
 * CSV Exporter for frontmark GmbH. All Quotes by vwdcode of instrument type BND, FND, GNS and STK of market STG.
 *
 * <dl>
 *     <dt>T-41129</dt>
 *     <dd>Original task</dd>
 *
 *     <dt>R-80584</dt>
 *     <dd>Added GNS</dd>
 *
 *     <dt>CORE-13948</dt>
 *     <dd>Exclude EUWAX symbols after the merger between markets EUWAX and STG</dd>
 * </dl>
 *
 * @author mwilke
 */
public class FrontmarkSymbolExporter extends CommonSymbolExporter {

    private static final String VWD_ID = "120322";

    private static final int TRADING_SEGMENT_FID = 742;

    private static final String SELECTED_MARKET = "STG";

    private static final Optional<String> TRADING_SEGMENT_TO_IGNORE = Optional.of("EUWAX");

    private final FeedConnector intradayServer;

    private File outputFile;

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public FrontmarkSymbolExporter(ProfileProvider profileProvider, InstrumentDirDao instrumentDirDao, FeedConnector intradayServer) {
        super(profileProvider, instrumentDirDao);
        this.intradayServer = intradayServer;
    }

    public void execute() throws Exception {
        final Profile profile = getProfile(VWD_ID);

        try (PrintWriter pw = new PrintWriter(this.outputFile)) {
            for (Instrument instrument : this.instrumentDirDao) {
                if (instrument.getSymbolIsin() == null) {
                    continue;
                }
                for (Quote quote : instrument.getQuotes()) {
                    if (profile.getPriceQuality(quote) == PriceQuality.NONE) {
                        continue;
                    }

                    if (quote.getSymbolVwdcode() != null && isRelevantQuote(instrument, quote)) {
                        pw.append(quote.getSymbolVwdcode())
                                .append(";").append(instrument.getSymbolIsin())

                                .append(";").append(instrument.getSymbolWkn())
                                .append(";").append(instrument.getName())
                                .append(";").append(instrument.getInstrumentType().getDescription())

                                .append(";").append(getCurrency(quote))
                                .println();
                    }
                }
            }
        }
    }

    private boolean isRelevantQuote(Instrument instrument, Quote quote) {
        return ((instrument instanceof StockDp2
                || instrument instanceof FundDp2
                || instrument instanceof BondDp2
                || instrument instanceof ParticipationCertificateDp2
        ) && isSelectedMarketOnly(quote));
    }

    private boolean isSelectedMarketOnly(Quote quote) {
        return SELECTED_MARKET.equals(quote.getSymbolVwdfeedMarket()) && !TRADING_SEGMENT_TO_IGNORE
                .equals(getTradingSegment(quote));
    }

    /**
     * Request {@link SnapField} value of FID {@value #TRADING_SEGMENT_FID} from {@link IntradayServer}.
     *
     * @param quote for which the {@link SnapField} is requested
     */
    private Optional<String> getTradingSegment(Quote quote) {
        final String vwdFeedSymbol = quote.getSymbolVwdfeed();
        if (vwdFeedSymbol == null)
            return Optional.empty();

        final IntradayRequest request = new IntradayRequest();
        request.add(new IntradayRequest.Item(vwdFeedSymbol));

        final IntradayResponse.Item item = this.intradayServer.getIntradayData(request)
                .getItem(vwdFeedSymbol);
        if (item == null)
            return Optional.empty();

        final SnapRecord snapRecord = item.getPriceSnapRecord();
        if (snapRecord == NullSnapRecord.INSTANCE)
            return Optional.empty();

        final SnapField snapField = snapRecord.getField(TRADING_SEGMENT_FID);
        return snapField == NullSnapField.INSTANCE ? Optional.empty()
                : Optional.ofNullable((String) snapField.getValue());
    }

    private static String getCurrency(Quote quote) {
        final String s = quote.getCurrency().getSymbolIso();
        return StringUtils.hasText(s) ? s : "";
    }

    @Configuration
    static class AppConfig extends AmqpAppConfig {

        @Value("#{systemProperties['outputFile'] ?: 'frontmark-symbols.txt'}")
        File outputFile;

        @Value("#{systemProperties['instrumentDir']}")
        File instrumentDir;

        @Bean
        ProfileProvider profileProvider() throws Exception {
            return proxy(ProfileProvider.class);
        }

        @Bean
        FeedConnector intradayServer() throws Exception {
            return proxy(FeedConnector.class);
        }

        @Bean
        InstrumentDirDao instrumentDirDao() throws Exception {
            return new InstrumentDirDao(this.instrumentDir);
        }

        @Bean
        FrontmarkSymbolExporter exporter() throws Exception {
            FrontmarkSymbolExporter e = new FrontmarkSymbolExporter(profileProvider(), instrumentDirDao(), intradayServer());
            e.setOutputFile(this.outputFile);
            return e;
        }
    }

    public static void main(String[] args) throws Exception {
        try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            try (final AmqpRpcConnectionManager connectionManager = context.getBean(AmqpRpcConnectionManager.class)) {
                context.getBean(FrontmarkSymbolExporter.class).execute();
            }
        }
    }
}
