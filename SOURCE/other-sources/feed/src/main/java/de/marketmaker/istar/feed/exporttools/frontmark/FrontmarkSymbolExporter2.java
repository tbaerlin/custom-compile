/*
 * FrontmarkSymbolExporter2.java
 *
 * Created on 20.05.2015 13:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.exporttools.frontmark;

import de.marketmaker.itools.amqprpc.connections.AmqpRpcConnectionManager;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.feed.exporttools.CommonSymbolExporter;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;
import de.marketmaker.itools.amqprpc.helper.AmqpAppConfig;

/**
 * Similar to {@link FrontmarkSymbolExporter} but adhering to a wishlist provided by customer.
 *
 * <dl>
 *   <dt>T-42850</dt>
 *   <dd>Original task</dd>
 * </dl>
 *
 * @see FrontmarkSymbolExporter
 * @author mwilke
 */
public class FrontmarkSymbolExporter2 extends CommonSymbolExporter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String VWD_ID = "120351";

    private File outputFile;

    private final Set<String> symbols = new HashSet<>();

    public FrontmarkSymbolExporter2(ProfileProvider profileProvider, InstrumentDirDao instrumentDirDao) {
        super(profileProvider, instrumentDirDao);
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
        final Profile profile = getProfile(VWD_ID);

        try (PrintWriter pw = new PrintWriter(this.outputFile)) {
            for (Instrument instrument : this.instrumentDirDao) {
                if ("true".equals(System.getProperty("checkISIN", "true")) && (instrument.getSymbolIsin() == null)) {
                    continue;
                }
                for (Quote quote : instrument.getQuotes()) {

                    if (quote.getSymbolVwdcode() != null && isRelevantQuote(instrument, quote)) {
                        if (profile.getPriceQuality(quote) == PriceQuality.NONE) {
                            this.logger.info("<run> {} wanted but not allowed", quote.getSymbolVwdcode());
                            continue;
                        }

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
        return (symbols.contains(instrument.getSymbolIsin()) ||
                symbols.contains(quote.getSymbolVwdcode()) ||
                symbols.contains(quote.getSymbolVwdfeed()));
    }

    private static String getCurrency(Quote quote) {
        final String s = quote.getCurrency().getSymbolIso();
        return StringUtils.hasText(s) ? s : "";
    }

    @Configuration
    static class AppConfig extends AmqpAppConfig {

        @Value("#{systemProperties['outputFile'] ?: 'frontmark2-symbols.txt'}")
        File outputFile;

        @Value("#{systemProperties['inputFile'] ?: 'frontmark2-INsymbols.txt'}")
        File inputFile;

        @Value("#{systemProperties['instrumentDir']}")
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
        FrontmarkSymbolExporter2 exporter() throws Exception {
            FrontmarkSymbolExporter2 e = new FrontmarkSymbolExporter2(profileProvider(), instrumentDirDao());
            e.setOutputFile(this.outputFile);
            e.setInputFile(this.inputFile);
            return e;
        }
    }

    public static void main(String[] args) throws Exception {
        try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            try (final AmqpRpcConnectionManager connectionManager = context.getBean(AmqpRpcConnectionManager.class)) {
                context.getBean(FrontmarkSymbolExporter2.class).execute();
            }
        }
    }
}

