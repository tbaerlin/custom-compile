/*
 * HuestSymbolExporter.java
 *
 * Created on 10.06.14 13:51
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.exporttools.boersestg;

import de.marketmaker.itools.amqprpc.connections.AmqpRpcConnectionManager;
import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
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
 * Creates a file that is used as input for {@link HuestExporterBoerseStg}; the file contains lines in the format
 * <pre>vwdcode;isin;currency</pre>
 * for all markets used by <code>HuestExporterBoerseStg</code>.
 * @author oflege
 * @author tkiesgen
 */
class HuestSymbolExporter extends CommonSymbolExporter {

    private File outputFile;

    private File isinFile;

    public HuestSymbolExporter(ProfileProvider profileProvider, InstrumentDirDao instrumentDirDao) {
        super(profileProvider, instrumentDirDao);
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public void setIsinFile(File isinFile) {
        this.isinFile = isinFile;
    }

    public void execute() throws Exception {
        final Profile profile = getProfile("120286");

        final Set<String> isins = this.isinFile != null
                ? new HashSet<>(FileUtils.readLines(this.isinFile))
                : Collections.<String>emptySet();

        System.out.println("isins: " + isins + ", #" + isins.size());

        try (PrintWriter pw = new PrintWriter(this.outputFile)) {
            for (Instrument instrument : this.instrumentDirDao) {
                if (instrument.getSymbolIsin() == null) {
                    continue;
                }
                for (Quote quote : instrument.getQuotes()) {
                    if (profile.getPriceQuality(quote) == PriceQuality.NONE) {
                        continue;
                    }
                    if (quote.getSymbolVwdcode() != null && isRelevantQuote(isins, quote)) {
                        pw.append(quote.getSymbolVwdcode())
                                .append(";").append(instrument.getSymbolIsin())
                                .append(";").append(getCurrency(quote))
                                .println();
                    }
                }
            }
        }
    }

    private static boolean isRelevantQuote(Set<String> isins, Quote quote) {
        return (isins.contains(quote.getInstrument().getSymbolIsin())
                && HuestExporterBoerseStg.MARKETS.contains(quote.getSymbolVwdfeedMarket()));
//                || "QTX".equals(quote.getSymbolVwdfeedMarket());
    }

    private static String getCurrency(Quote quote) {
        final String s = quote.getCurrency().getSymbolIso();
        return StringUtils.hasText(s) ? s : "";
    }

    @Configuration
    static class AppConfig extends AmqpAppConfig {

        @Value("#{systemProperties['outputFile'] ?: 'STG-huest-symbols.txt'}")
        File outputFile;

        @Value("#{systemProperties['instrumentDir']}")
        File instrumentDir;

        @Value("#{systemProperties['isinFile'] ?: 'REQUEST_VWD_INSTRUMENTS.csv'}")
        File isinFile;

        @Bean
        public static PropertySourcesPlaceholderConfigurer ppc() {
            PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
            ppc.setLocation(new ClassPathResource("HuestSymbolExporter.properties"));
            ppc.setIgnoreResourceNotFound(true);
            return ppc;
        }

        @Bean
        ProfileProvider profileProvider() throws Exception {
            return proxy(ProfileProvider.class);
        }

        @Bean
        InstrumentDirDao instrumentDirDao() throws Exception {
            return new InstrumentDirDao(this.instrumentDir);
        }

        @Bean
        HuestSymbolExporter exporter() throws Exception {
            HuestSymbolExporter hse = new HuestSymbolExporter(profileProvider(), instrumentDirDao());
            hse.setOutputFile(this.outputFile);
            hse.setIsinFile(this.isinFile);
            return hse;
        }
    }

    public static void main(String[] args) throws Exception {
        try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            try (final AmqpRpcConnectionManager connectionManager = context.getBean(AmqpRpcConnectionManager.class)) {
                context.getBean(HuestSymbolExporter.class).execute();
            }
        }
    }
}
