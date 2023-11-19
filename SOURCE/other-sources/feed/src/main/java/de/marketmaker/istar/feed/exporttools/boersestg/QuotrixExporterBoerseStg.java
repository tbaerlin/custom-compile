/*
 * HuestExporterBoerseStg.java
 *
 * Created on 10.06.14 14:08
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.exporttools.boersestg;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.exporttools.ExporterBaseWriter;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.tick.DefaultIndexHandler;
import de.marketmaker.istar.feed.ordered.tick.FileTickStore;
import de.marketmaker.istar.feed.ordered.tick.OrderedTickData;
import de.marketmaker.istar.feed.ordered.tick.TickDecompressor;
import de.marketmaker.istar.feed.ordered.tick.TickFile;
import de.marketmaker.istar.feed.ordered.tick.TickFileIndexReader;
import de.marketmaker.istar.feed.ordered.tick.TickFileProcessor;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.instrument.Controller;
import de.marketmaker.istar.instrument.InstrumentServer;
import de.marketmaker.istar.instrument.InstrumentServerImpl;
import de.marketmaker.istar.instrument.search.SearchRequestStringBased;
import de.marketmaker.istar.instrument.search.SearchResponse;

import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICK3;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICKZ;
import static de.marketmaker.istar.feed.tick.TickFiles.getMarketName;

/**
 * Tick file exporter which processes a TD3 and TDZ file of a particular market and writes it as CSV.
 *
 * <ul>
 *     <li>{@code -Dmarket="QTX"}</li>
 *     <li>{@code -DinputDir="file:$HOME/produktion/var/data/chicago/$DATE"}</li>
 *     <li>{@code -DoutputDir="file:$PWD/out/"}</li>
 *     <li>{@code -DfilePrefix="quotrix-export_"}</li>
 *     <li>{@code -DinstrumentDir="file:$HOME/produktion/var/data/instrument"}</li>
 *     <li>{@code -DexportType="MARKET_STATISTICS"}</li>
 * </ul>
 *
 * @author tkiesgen
 */
public class QuotrixExporterBoerseStg extends TickFileProcessor {

    public enum ExportType {
        MARKET_STATISTICS,
        BEST_EXECUTION // since 2020-09-29
    }

    @Configuration
    static class AppConfig {

        @Value("${exportType:MARKET_STATISTICS}")
        ExportType exportType;

        @Bean
        InstrumentServerImpl instrumentServer() {
            return new InstrumentServerImpl();
        }

        @Bean(initMethod = "initialize", destroyMethod = "dispose")
        Controller controller(@Value("${instrumentDir}") File instrumentDir) {
            Controller controller = new Controller();
            controller.setMaxBps(4194304);
            controller.setBaseDir(instrumentDir);
            controller.setMaxArchiveFiles(1);
            controller.setMaxUpdateArchiveFiles(1);
            controller.setInstrumentServer(instrumentServer());

            return controller;
        }

        @Bean
        QuotrixExporterBoerseStg exporterBoerseStg(@Value("${market}") String market,
                                                   @Value("${inputDir}") File inputDir,
                                                   @Value("${outputDir}") File outputDir,
                                                   @Value("${filePrefix}") String filePrefix,
                                                   Controller controller) {
            return new QuotrixExporterBoerseStg(market, inputDir, outputDir, filePrefix, controller, instrumentServer(), exportType);
        }
    }

    private final String market;

    private final File inputDir;

    private final File outputDir;

    private final String filePrefix;

    private final InstrumentServer instrumentServer;

    private final ExportType exportType;

    public QuotrixExporterBoerseStg(String market, File inputDir, File outputDir, String filePrefix,
                                    Controller controller, InstrumentServer instrumentServer, ExportType exportType) {
        this.market = market;
        this.inputDir = inputDir;
        this.outputDir = outputDir;
        this.filePrefix = filePrefix;
        this.numThreads = 4;
        this.instrumentServer = instrumentServer;
        this.exportType = exportType;
        controller.checkIncoming();
    }

    public void execute() throws Exception {
        process(this.inputDir.listFiles(f -> f.isFile() && f.length() > 8
                && this.market.equals(getMarketName(f))
                && FileTickStore.canHandle(f)));
    }

    @Override
    protected Task createTask() {
        return new MyTask();
    }

    private class MyTask extends Task {
        @Override
        protected void process(File f, File out) throws IOException {
            new MyWriter(f).process(out);
        }

        @Override
        protected File getOutFile(File f) {
            return new File(outputDir, f.getName()
                    .replace(".tdz", ".csv.gz")
                    .replace(".td3", ".csv.gz")
                    .replace(market + "-", filePrefix));
        }
    }

    private class MyWriter extends ExporterBaseWriter {

        private final AbstractTickRecord.TickItem.Encoding encoding;

        private final DefaultIndexHandler handler;

        private final ArrayList<FeedData> items = new ArrayList<>(8192);

        private final int date;

        private final StringBuilder line = new StringBuilder();

        private final long[] data = new long[6];

        private final String[] supplements = new String[3];

        public MyWriter(File file) {
            super(ByteOrder.LITTLE_ENDIAN, file);
            this.encoding = file.getName().endsWith(".td3") ? TICK3 : TICKZ;
            this.handler = new DefaultIndexHandler(file) {
                @Override
                protected void doHandle(ByteString vwdcode, long position, int length) {
                    ExporterHelper.addItem(items, vwdcode, position, length, date);
                }
            };
            this.date = TickFile.getDay(this.file);
        }

        protected void process(FileChannel ch, PrintWriter pw) throws IOException {
            TickFileIndexReader.readEntries(ch, this.handler);

            if (exportType == ExportType.BEST_EXECUTION) {
                pw.println("#WKN;ISIN;market;currency;bid;bidVolume;bisSupplement;ask;askVolume;askSupplement;trade;tradeVolume;tradeSupplement;time");
            } else {
                pw.println("#ISIN;market;trade;tradeVolume;tradeSupplement;time");
            }

            mapFile(ch);

            for (FeedData item : items) {
                OrderedTickData td = ((OrderedFeedData) item).getOrderedTickData();
                try {
                    byte[] ticks = addTicks(td);
                    write(item, ticks, pw);
                } catch (Throwable e) {
                    this.logger.error(file.getName() + " process failed for " + item.getVwdcode(), e);
                }
            }
        }

        private Quote getFirstQuote(String vwdcode, String vwdFeedMarket) {
            SearchRequestStringBased sr = new SearchRequestStringBased();
            String searchKey = KeysystemEnum.VWDCODE.name().toLowerCase();
            sr.setSearchExpression(searchKey + ':' + vwdcode);
            sr.setDefaultFields(Collections.singletonList(searchKey));
            sr.setMaxNumResults(1);
            SearchResponse response = QuotrixExporterBoerseStg.this.instrumentServer.search(sr);
            return response.getInstruments().stream()
                    .flatMap(i -> i.getQuotes().stream())
                    .filter(q -> vwdFeedMarket.equals(q.getSymbolVwdfeedMarket()))
                    .findFirst()
                    .orElse(null);
        }

        private void write(FeedData item, byte[] ticks, PrintWriter pw) {
            this.line.setLength(0);
            final String vwdcode = item.getVendorkey().toVwdcode().toString();
            final int startIndex = vwdcode.indexOf('.') + 1;
            final Quote quote = startIndex > 0 ? getFirstQuote(vwdcode, vwdcode.substring(startIndex)) : null;
            if (Objects.nonNull(quote)) {
                if (exportType == ExportType.BEST_EXECUTION) {
                    // wkn
                    String wkn = quote.getInstrument().getSymbolWkn();
                    this.line.append(Objects.nonNull(wkn) ? wkn : "").append(";");
                }
                // isin
                String isin = quote.getInstrument().getSymbolIsin();
                this.line.append(Objects.nonNull(isin) ? isin : "").append(";");
                // market
                this.line.append(quote.getSymbolVwdfeedMarket()).append(";");
                if (exportType == ExportType.BEST_EXECUTION) {
                    // currency
                    String isoCurrency = Objects.nonNull(quote.getCurrency()) ? quote.getCurrency().getSymbolIso() : "";
                    this.line.append(Objects.nonNull(isoCurrency) ? isoCurrency : "").append(";");
                }
            } else {
                this.logger.warn("<write> no instrument for {}", vwdcode);
                if (exportType == ExportType.BEST_EXECUTION) {
                    // wkn
                    this.line.append(";");
                }
                // isin + market
                this.line.append(vwdcode.replace('.', ';')).append(";");
                if (exportType == ExportType.BEST_EXECUTION) {
                    // currency
                    this.line.append(";");
                }
            }
            int mark = this.line.length();

            if (!hasTrade(ticks)) {
                return;
            }

            TickDecompressor td = new TickDecompressor(ticks, this.encoding);
            for (TickDecompressor.Element e : td) {
                if (!e.hasFlag(FeedUpdateFlags.FLAG_WITH_TRADE)) {
                    continue;
                }
                BufferFieldData fd = e.getData();
                final int mdpsTime = fd.getInt();

                ExporterHelper.collectData(fd, data, supplements);
                this.line.setLength(mark);

                if (ExportType.BEST_EXECUTION == exportType) {
                    // bid
                    ExporterHelper.append(line, data[0], data[1], supplements[0]);
                    // ask
                    ExporterHelper.append(line, data[2], data[3], supplements[1]);
                }
                // trade
                ExporterHelper.append(line, data[4], data[5], supplements[2]);
                this.line.append(MdpsFeedUtils.decodeLocalTime(mdpsTime));
                pw.println(this.line);
            }
        }

        private boolean hasTrade(byte[] ticks) {
            TickDecompressor td = new TickDecompressor(ticks, this.encoding);
            for (TickDecompressor.Element e : td) {
                if (e.hasFlag(FeedUpdateFlags.FLAG_WITH_TRADE)) {
                    return true;
                }
            }
            return false;
        }

        private byte[] addTicks(OrderedTickData td) {
            byte[] ticks = new byte[td.getLength()];
            fillFileTickStoreTicks(td.getStoreAddress(), ticks);
            return ticks;
        }
    }

    public static void main(String[] args) throws Exception {
        try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            context.getBean(QuotrixExporterBoerseStg.class).execute();
        }
    }
}
