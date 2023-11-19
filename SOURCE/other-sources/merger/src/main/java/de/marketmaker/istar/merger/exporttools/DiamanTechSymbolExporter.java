/*
 * DiamanTechSymbolExporter.java
 *
 * Created on 3/13/17
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.exporttools;

import de.marketmaker.istar.instrument.export.InstrumentDirDao;
import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.feed.api.FeedConnector;
import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.feed.exporttools.CommonSymbolExporter;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcConnectionManager;
import de.marketmaker.itools.amqprpc.helper.AmqpAppConfig;

/**
 * Exports snap fields for diamantech.
 *
 * @author Stefan Willenbrock
 * @see #SNAP_FIELDS
 */
public class DiamanTechSymbolExporter extends CommonSymbolExporter {

    // Old spec:
    // "ADF_Rendite_ISMA", "ADF_Duration", "ADF_Time_to_maturity", "ADF_Ref_Kurs"

    private static final List<String> SNAP_FIELDS = Arrays.asList("ADF_Berechnungsdatum", "ADF_Ref_Kurs", "ADF_Time_to_maturity", "ADF_Duration");

    private static final List<String> COLUMN_HEADER = Arrays.asList("ISIN", "VWD_CODE", "DATE_TRADING_DAY", "REFERENCE_PRICE", "YIELD_TO_MATURITY", "DURATION");

    private final Logger logger = LoggerFactory.getLogger(DiamanTechSymbolExporter.class);

    private final FeedConnector intradayServer;

    private File outputFile;

    private Set<String> symbols = new HashSet<>();

    public DiamanTechSymbolExporter(ProfileProvider profileProvider, InstrumentDirDao instrumentDirDao, FeedConnector intradayServer) {
        super(profileProvider, instrumentDirDao);
        this.intradayServer = intradayServer;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public void setInputFile(File inputFile) {
        try (Scanner scanner = new Scanner(inputFile)) {
            while (scanner.hasNextLine()) {
                symbols.add(scanner.nextLine().trim());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void execute() throws Exception {
        // vwd ID: 120476
        // vwd Product: vwd data manager XML
        // Production: 23.03.2017
        // Entitlement: data marked green (CQ) + EuroTLX
        final Profile profile = getProfile("120476");

        try (PrintWriter pw = new PrintWriter(this.outputFile)) {
            printHeader(pw);
            for (Instrument instrument : this.instrumentDirDao) {
                if ("true".equals(System.getProperty("checkISIN", "true")) && (instrument.getSymbolIsin() == null)) {
                    this.logger.info("<afterPropertiesSet> {} has no ISIN", instrument.getName());
                    continue;
                }
                printQuotes(pw, profile, instrument);
            }
        }
    }

    private void printHeader(PrintWriter pw) {
        pw.append(COLUMN_HEADER.get(0));
        for (int i = 1; i < COLUMN_HEADER.size(); i++) {
            pw.append(';').append(COLUMN_HEADER.get(i));
        }
        pw.println();
    }

    private void printQuotes(PrintWriter pw, Profile profile, Instrument instrument) {
        for (Quote quote : instrument.getQuotes()) {
            if (quote.getSymbolVwdcode() != null && isRelevantQuote(instrument, quote)) {
                if (profile.getPriceQuality(quote) == PriceQuality.NONE) {
                    this.logger.debug("<printQuotes> {} wanted but not allowed", quote.getSymbolVwdcode());
                    continue;
                }
                final IntradayRequest ir = new IntradayRequest();
                final String vendorKey = quote.getSymbolVwdfeed();
                final IntradayRequest.Item ri = new IntradayRequest.Item(vendorKey, true);
                ir.add(ri);

                final IntradayResponse.Item item = this.intradayServer.getIntradayData(ir).getItem(vendorKey);
                if (item == null) {
                    continue;
                }
                final SnapRecord snapRecord = item.getPriceSnapRecord();

                pw.append(instrument.getSymbolIsin()).append(";").append(quote.getSymbolVwdcode());
                SNAP_FIELDS.stream().forEach(key -> pw.append(";").append(readSnapValue(snapRecord.getField(key))));
                pw.println();
            }
        }
    }

    private String readSnapValue(SnapField snapField) {
        Object value = "";

        if (snapField.isDefined()) {
            switch (snapField.getType()) {
                case PRICE:
                    value = SnapRecordUtils.getPrice(snapField);
                    break;
                case TIME:
                case NUMBER:
                    value = SnapRecordUtils.getInt(snapField);
                    break;
                case STRING:
                    value = SnapRecordUtils.getString(snapField);
                    break;
                case DATE: {
                    final int date = SnapRecordUtils.getInt(snapField);
                    return date > 0 ? DateUtil.yyyyMmDdToLocalDate(date).toString() : "";
                }
                default:
                    value = snapField.getValue();
            }
        }
        return String.valueOf(value);
    }

    private boolean isRelevantQuote(Instrument instrument, Quote quote) {
        return (symbols.contains(instrument.getSymbolIsin()) ||
                symbols.contains(quote.getSymbolVwdcode()) ||
                symbols.contains(quote.getSymbolVwdfeed()));
    }

    @Configuration
    static class AppConfig extends AmqpAppConfig {

        @Value("${outputFile}")
        File outputFile;

        @Value("${inputFile}")
        File inputFile;

        @Value("${instrumentDir}")
        File instrumentDir;

        @Bean
        ProfileProvider profileProvider() throws Exception {
            return proxy(ProfileProvider.class);
        }

        @Bean
        InstrumentDirDao instrumentDirDao() throws Exception {
            return new InstrumentDirDao(this.instrumentDir);
        }

        @Bean
        FeedConnector intradayServer() throws Exception {
            return proxy(FeedConnector.class);
        }

        @Bean
        DiamanTechSymbolExporter exporter() throws Exception {
            DiamanTechSymbolExporter e = new DiamanTechSymbolExporter(profileProvider(), instrumentDirDao(), intradayServer());
            e.setOutputFile(this.outputFile);
            e.setInputFile(this.inputFile);
            return e;
        }
    }

    public static void main(String[] args) throws Exception {
        try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            try (final AmqpRpcConnectionManager connectionManager = context.getBean(AmqpRpcConnectionManager.class)) {
                context.getBean(DiamanTechSymbolExporter.class).execute();
            }
        }
    }
}
