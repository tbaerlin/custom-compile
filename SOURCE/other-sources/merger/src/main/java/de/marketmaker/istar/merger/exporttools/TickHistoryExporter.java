package de.marketmaker.istar.merger.exporttools;

import com.rabbitmq.client.ConnectionFactory;
import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PriceFormatter;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.TickEvent;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.IntradayServer;
import de.marketmaker.istar.feed.api.FeedConnector;
import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayRequest.Item;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.feed.api.IstarFeedConnector;
import de.marketmaker.istar.feed.api.TickConnector;
import de.marketmaker.istar.feed.tick.RawTick;
import de.marketmaker.istar.feed.tick.TickServer;
import de.marketmaker.istar.instrument.InstrumentRequest;
import de.marketmaker.istar.instrument.InstrumentRequest.KeyType;
import de.marketmaker.istar.instrument.InstrumentResponse;
import de.marketmaker.istar.instrument.InstrumentServer;
import de.marketmaker.itools.amqprpc.AmqpProxyFactoryBean;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddressImpl;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcConnectionManager;
import de.marketmaker.itools.amqprpc.supervising.PeriodicSupervisor;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.ReadableInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Tick History CSV Exporter based on {@link TickServer} and {@link IntradayServer}.
 *
 * <p>Consider the test environment does not deliver tick history for much more than one week (due to disk space).</p>
 *
 * <p>Copyright (c) vwd AG. All Rights Reserved.</p>
 */
public class TickHistoryExporter {

    @Configuration
    static class SpringConfig {

        private final String AMQP_EXCHANGE = "istar.rpc";

        private final String tickPeriod = "P10Y";

        @Bean
        public ConnectionFactory connectionFactory() {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("msgsrv");
            connectionFactory.setUsername("merger");
            connectionFactory.setPassword("merger");
            connectionFactory.setPort(5672);
            connectionFactory.setVirtualHost("istar");
            connectionFactory.setRequestedHeartbeat(15);
            return connectionFactory;
        }

        @Bean(initMethod = "afterPropertiesSet")
        public AmqpRpcConnectionManager amqpRpcConnectionManager() throws Exception {
            AmqpRpcConnectionManager amqpRpcConnectionManager = new AmqpRpcConnectionManager();
            amqpRpcConnectionManager.setConnectionFactory(connectionFactory());
            return amqpRpcConnectionManager;
        }

        private <T> AmqpRpcAddress createAmqpRpcAddress(Class<T> clazz) {
            String queueName = clazz.getAnnotation(AmqpAddress.class).queue();
            AmqpRpcAddressImpl amqpRpcAddress = new AmqpRpcAddressImpl();
            amqpRpcAddress.setExchange(AMQP_EXCHANGE);
            amqpRpcAddress.setRequestQueue(queueName);
            return amqpRpcAddress;
        }

        private AmqpProxyFactoryBean createAmqpProxyFactoryBean(Class clazz) throws Exception {
            AmqpProxyFactoryBean proxyFactoryBean = new AmqpProxyFactoryBean();
            proxyFactoryBean.setConnectionManager(amqpRpcConnectionManager());
            proxyFactoryBean.setSupervisor(new PeriodicSupervisor());
            proxyFactoryBean.setAddress(createAmqpRpcAddress(clazz));
            proxyFactoryBean.setServiceInterface(clazz);
            proxyFactoryBean.afterPropertiesSet();
            return proxyFactoryBean;
        }

        @Bean
        public FeedConnector intradayServer() throws Exception {
            return (FeedConnector) createAmqpProxyFactoryBean(FeedConnector.class).getObject();
        }

        @Bean
        public TickConnector tickServer() throws Exception {
            return (TickConnector) createAmqpProxyFactoryBean(TickConnector.class).getObject();
        }

        @Bean(initMethod = "afterPropertiesSet")
        public IstarFeedConnector istarFeedConnector() throws Exception {
            final IstarFeedConnector istarFeedConnector = new IstarFeedConnector();
            istarFeedConnector.setChicagoServer(intradayServer());
            istarFeedConnector.setTickServer(tickServer());
            return istarFeedConnector;
        }

        @Bean
        public InstrumentServer instrumentServer() throws Exception {
            return (InstrumentServer) createAmqpProxyFactoryBean(InstrumentServer.class).getObject();
        }

        @Bean
        public TickHistoryExporter tickHistoryExporter() throws Exception {
            final List<String> columnHeaders = Arrays
                    .asList("Symbol", "Date", "Time", "Unix_Time_Milliseconds", "ADF_Bezahlt",
                            "ADF_Bezahlt_Umsatz", "ADF_Geld", "ADF_Geld_Umsatz", "ADF_Brief",
                            "ADF_Brief_Umsatz");
            return new TickHistoryExporter(istarFeedConnector(), instrumentServer(),
                    KeyType.VWDCODE, this.tickPeriod, columnHeaders);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(TickHistoryExporter.class);

    private final IstarFeedConnector istarFeedConnector;

    private final InstrumentServer instrumentServer;

    private final KeyType keyType;

    private final Period period;

    private final List<String> outputColumnHeaders;

    private final String valueDelimiter = ";";

    private final String lineSeparator = System.getProperty("line.separator");

    private final PriceFormatter priceFormatter = new PriceFormatter(5, 8);

    private int exportedEventCount = 0;

    private int notExportedEventCount = 0;

    public TickHistoryExporter(IstarFeedConnector istarFeedConnector, InstrumentServer instrumentServer,
            KeyType symbolKeyType,
            String tickPeriod, List<String> columnHeaders) {
        this.istarFeedConnector = istarFeedConnector;
        this.instrumentServer = instrumentServer;
        this.keyType = symbolKeyType;
        this.period = Period.parse(tickPeriod);
        this.outputColumnHeaders = columnHeaders;
    }

    private Optional<InstrumentResponse> getInstrumentResponse(List<String> symbols) {
        final InstrumentRequest instrumentRequest = new InstrumentRequest();
        if (!symbols.isEmpty()) {
            instrumentRequest.addItems(symbols, this.keyType);
        }
        final InstrumentResponse response = this.instrumentServer.identify(instrumentRequest);
        if (!response.isValid()) {
            logger.info("<getInstrumentResponse> Invalid instrument response");
            return Optional.empty();
        }
        return Optional.of(response);
    }

    private List<String> readSymbols(Path symbolFile) {
        final List<String> symbols = new ArrayList<>();
        try (Scanner scanner = new Scanner(symbolFile)) {
            while (scanner.hasNextLine()) {
                final String symbol = StringUtils.trimWhitespace(scanner.nextLine());
                if (!symbol.isEmpty()) {
                    symbols.add(symbol);
                }
            }
        } catch (IOException e) {
            logger.error("<readSymbols> Failed to read file", e);
        }
        return symbols;
    }

    private Optional<IntradayRequest> createIntradayRequest(Quote quote, Interval interval) {
        final String vendorkey = quote.getSymbolVwdfeed();
        if (vendorkey == null) {
            return Optional.empty();
        }

        final LocalDate from = interval.getStart().toLocalDate();
        LocalDate to = interval.getEnd().toLocalDate();
        to = to.minusDays(1);

        final IntradayRequest result = new IntradayRequest();
        result.setTickDataFullAccess(true);
        final IntradayRequest.Item item = new Item(vendorkey, true);
        item.setRetrieveTicks(DateUtil.toYyyyMmDd(from), DateUtil.toYyyyMmDd(to));
        result.add(item);

        return Optional.of(result);
    }

    private long formatUnixTimeMilliseconds(LocalDate startDate, TickEvent event) {
        final DateTime startTime = startDate.toDateTime(LocalTime.MIDNIGHT);
        final long millisInDay = event.getTime() * 1000 + (event instanceof RawTick ? ((RawTick) event).getMillis() : 0);
        final DateTime eventTime = startTime.plus(millisInDay);
        return eventTime.getMillis();
    }

    private String formatPrice(long price) {
        return isNa(price) ? "" : this.priceFormatter.formatPrice(price);
    }

    private String formatVolume(long volume) {
        return volume == Long.MIN_VALUE ? "" : String.valueOf(volume);
    }

    /**
     * Duplicated code from: {@link PriceFormatter#isNa(Number)}.
     *
     * TODO: Make this public within <code>istar/common</code>.
     */
    private boolean isNa(Number price) {
        return price == null
                || price.longValue() == Long.MAX_VALUE
                || price.longValue() == Long.MIN_VALUE
                || price.intValue() == Integer.MAX_VALUE
                || price.intValue() == Integer.MIN_VALUE;
    }

    private boolean isExportedTick(TickEvent tickEvent) {
        return tickEvent.isAsk() && !isNa(tickEvent.getAskPrice()) || tickEvent.isBid() && !isNa(
                tickEvent.getBidPrice()) || tickEvent.isTrade() && !isNa(tickEvent.getPrice());
    }

    private void print(Writer out, Quote quote, IntradayRequest request, IntradayResponse intradayResponse)
            throws IOException {
        for (IntradayResponse.Item item : intradayResponse) {
            final TickRecord tickRecord = item.getTickRecord();
            final Item onlyItem = getOnlyItem(request);
            if (tickRecord == null || tickRecord.getInterval() == null) {
                logger.info("<print> No ticks for {} from {} to {}", onlyItem.getVendorkey(), onlyItem.getTicksFrom(), onlyItem.getTicksTo());
                continue;
            }
            logger.info("<print> Writing ticks for {} from {} to {}", onlyItem.getVendorkey(), onlyItem.getTicksFrom(), onlyItem.getTicksTo());
            de.marketmaker.istar.domain.timeseries.Timeseries<TickEvent> timeseries = tickRecord
                    .getTimeseries(tickRecord.getInterval());
            for (DataWithInterval<TickEvent> event : timeseries){
                final ReadableInterval eventInterval = event.getInterval();
                final TickEvent tickEvent = event.getData();

                final long unixTimeMillis = formatUnixTimeMilliseconds(eventInterval.getStart().toLocalDate(), tickEvent);
                if (isExportedTick(tickEvent)) {
                    final List<Object> values = Arrays.asList(
                            quote.getSymbolVwdcode(), // Symbol
                            eventInterval.getStart().toLocalDate(), // Date
                            new LocalTime(unixTimeMillis), // Time
                            unixTimeMillis, // Unix_Time_Milliseconds
                            formatPrice(tickEvent.getPrice()), // ADF_Bezahlt
                            formatVolume(tickEvent.getVolume()), // ADF_Bezahl_Umsatz
                            formatPrice(tickEvent.getBidPrice()), // ADF_Geld
                            formatVolume(tickEvent.getBidVolume()), // ADF_Geld_Umsatz
                            formatPrice(tickEvent.getAskPrice()), // ADF_Brief
                            formatVolume(tickEvent.getAskVolume()) // ADF_Brief_Umsatz
                    );
                    out.write(values.stream().map(it -> it.toString()).collect(Collectors.joining(this.valueDelimiter)));
                    out.write(this.lineSeparator);

                    this.exportedEventCount++;
                } else {
                    this.notExportedEventCount++;
                }
            }
        }
    }

    private Item getOnlyItem(IntradayRequest intradayRequest) {
        return intradayRequest.getItems().get(0);
    }

    private Optional<IntradayResponse> getIntradayResponse(IntradayRequest request) {
        final IntradayResponse response = this.istarFeedConnector.getIntradayData(request);
        if (!response.isValid()) {
            logger.warn("<getIntradayResponse> Invalid response");
            return Optional.empty();
        }
        return Optional.of(response);
    }

    private void writeTicksForQuote(Quote quote, Path basePath) {
        final String fileName = String.format("%s-%s.gz", quote.getSymbolVwdcode(), this.period);
        final Path tickFile = basePath.resolve(fileName);

        try (OutputStreamWriter os = new OutputStreamWriter(new GZIPOutputStream(
                Files.newOutputStream(tickFile, StandardOpenOption.CREATE_NEW)))) {
            os.write(String.join(this.valueDelimiter, this.outputColumnHeaders));
            os.write(this.lineSeparator);

            final DateTime today = LocalDate.now().toDateTimeAtStartOfDay();
            final List<Interval> tickIntervals = DateUtil.getDailyIntervals(today.minus(this.period), today);

            for (Interval interval : tickIntervals) {
                Optional<IntradayRequest> request = createIntradayRequest(quote, interval);
                if (!request.isPresent()) {
                    logger.warn("<writeTicksForQuote> No intraday request for {}", quote);
                    continue;
                }

                logger.info("<writeTicksForQuote> Request ticks from {} to {}",
                        getOnlyItem(request.get()).getTicksFrom(),
                        getOnlyItem(request.get()).getTicksTo());

                Optional<IntradayResponse> response = getIntradayResponse(request.get());
                if (response.isPresent()) {
                    logger.debug("<writeTicksForQuote> Response size: {}", response.get().size());
                    print(os, quote, request.get(), response.get());
                }
            }

            logger.info("<writeTicksForQuote> Wrote {} events to {}", this.exportedEventCount, tickFile);
            logger.info("<writeTicksForQuote> Omitted {} events", this.notExportedEventCount);

            this.exportedEventCount = this.notExportedEventCount = 0;
        } catch (FileAlreadyExistsException e) {
            logger.warn("<writeTicksForQuote> Already exists: {}", e.getFile());
        } catch (IOException e) {
            logger.error("<writeTicksForQuote> Export failed", e);
        }
    }

    public void export(Path symbolFile) {
        final List<String> symbols = readSymbols(symbolFile);
        logger.info("<export> Read {} symbol(s)", symbols.size());
        if (symbols.isEmpty()) {
            return;
        }

        final InstrumentResponse instrumentResponse = getInstrumentResponse(symbols).orElse(null);
        if (instrumentResponse == null) {
            return;
        }

        assert (symbols.size() == instrumentResponse.getInstruments().size());
        final Iterator<String> symbolIterator = symbols.iterator();
        final Iterator<Instrument> instrumentIterator = instrumentResponse.getInstruments().iterator();

        while (symbolIterator.hasNext()) {
            final String symbol = symbolIterator.next();
            final Instrument instrument = instrumentIterator.next();

            final Quote quote = instrument.getQuotes().stream()
                    .filter(q -> symbol.equals(q.getSymbolVwdcode()))
                    .findFirst().orElse(null);
            if (quote == null) {
                logger.warn("<export> No quote for {}", instrument);
                continue;
            }

            writeTicksForQuote(quote, symbolFile.getParent());
        }

        logger.info("<export> Quit");
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.printf("usage: %s symbol_file%n", TickHistoryExporter.class.getSimpleName());
            System.out.printf("symbol_file: list of quote symbols%n");
            return;
        }

        Path symbolFile = null;
        try {
            symbolFile = Paths.get(args[0]);
            symbolFile = symbolFile.isAbsolute() ? symbolFile : Paths.get(System.getProperty("user.dir")).resolve(symbolFile);
        } catch (InvalidPathException e) {
            System.err.printf("invalid path %s%n", e.getInput());
            System.exit(1);
        }

        if (!Files.isReadable(symbolFile)) {
            System.err.printf("not readable: %s%n", symbolFile);
            System.exit(1);
        }

        try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class)) {
            try {
                final TickHistoryExporter tickHistoryExporter = context.getBean(TickHistoryExporter.class);
                tickHistoryExporter.export(symbolFile);
            } finally {
                final AmqpRpcConnectionManager amqpRpcConnectionManager = context.getBean(AmqpRpcConnectionManager.class);
                if (amqpRpcConnectionManager != null && amqpRpcConnectionManager.isConnectionOpen()) {
                    amqpRpcConnectionManager.destroy();
                }
            }
        }
    }
}
