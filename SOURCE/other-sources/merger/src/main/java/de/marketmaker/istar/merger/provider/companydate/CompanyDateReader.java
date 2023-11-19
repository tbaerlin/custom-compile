/*
 * CompanyDateProviderConvensys.java
 *
 * Created on 03.07.2008 13:56:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.companydate;

import de.marketmaker.itools.amqprpc.connections.AmqpRpcConnectionManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.PagedResultSorter;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.QuoteOrder;
import de.marketmaker.istar.instrument.InstrumentServer;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.merger.provider.InstrumentProviderImpl;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilters;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteSelectors;
import de.marketmaker.itools.amqprpc.helper.AmqpAppConfig;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class CompanyDateReader implements InitializingBean {
    private static final int MIN_FROM = 19700101;

    private static final int MAX_TO = 21000101;

    public static class MyReader extends IstarMdpExportReader<Map<Long, List<Event>>> {
        private final Map<Long, List<Event>> events = new HashMap<>(1 << 15);

        private int numEvents;

        public MyReader() {
            super(false, "NAME", "SYMBOL", "EVENT", "EVENT_EN", "EVENT_IT");
        }

        protected Map<Long, List<Event>> getResult() {
            return this.events;
        }

        protected void endDocument() {
            for (List<Event> list : this.events.values()) {
                list.sort(new Event.ByDateAndNameComparator(null));
            }
            logger.info("<endDocument> read " + this.numEvents
                    + " events for " + this.events.size() + " iids");
        }

        protected void handleRow() {
            final Long iid = getLong("IID");
            if (iid == null) {
                return;
            }
            final String name = get("NAME");
            if (name == null) {
                return;
            }
            final String symbol = get("SYMBOL");

            final LocalizedString defEvent = LocalizedString.createDefault(get("EVENT"));
            if (defEvent == null) {
                return;
            }
            final LocalizedString event = defEvent
                    .add(Language.en, get("EVENT_EN"))
                    .add(Language.it, get("EVENT_IT"));

            if (!StringUtils.hasText(event.getDe())) {
                return;
            }
            final DateTime dt = getDateTime("DATE_");
            if (dt == null) {
                return;
            }
            final int yyyymmdd = DateUtil.toYyyyMmDd(dt);
            final double rank = getDouble("RANK", 0d);

            final List<Event> list = getEventList(iid);
            list.add(new Event(iid, name, symbol, event, yyyymmdd, rank));
            this.numEvents++;
        }

        private List<Event> getEventList(Long key) {
            final List<Event> existing = this.events.get(key);
            if (existing != null) {
                return existing;
            }
            final List<Event> result = new ArrayList<>();
            this.events.put(key, result);
            return result;
        }
    }


    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private File companyDateFile;

    private InstrumentProvider instrumentProvider;

    private int instrumentRequestChunkSize;

    @GuardedBy("this")
    private Map<Long, List<Event>> events = Collections.emptyMap();

    @GuardedBy("this")
    private int size;

    static final MarketStrategy STRATEGY = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    new QuoteSelectors.ByOrder(QuoteOrder.VOLUME),
                    QuoteSelectors.HOME_EXCHANGE,
                    QuoteSelectors.SYMBOL_RELEVANCE
            )).build();

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setCompanyDateFile(File companyDateFile) {
        this.companyDateFile = companyDateFile;
    }

    public void setInstrumentProvider(InstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setInstrumentRequestChunkSize(int instrumentRequestChunkSize) {
        this.instrumentRequestChunkSize = instrumentRequestChunkSize;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.instrumentRequestChunkSize <= 0) {
            throw new IllegalArgumentException("instrumentRequestChunkSize must be larger than 0");
        }
        final FileResource companyDate = new FileResource(this.companyDateFile);
        companyDate.addPropertyChangeListener(evt -> readCompanyDates());
        this.activeMonitor.addResource(companyDate);

        readCompanyDates();
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "REC", justification = "catch all exceptions")
    private void readCompanyDates() {
        try {
            setEvents(new MyReader().read(this.companyDateFile));
        } catch (Exception e) {
            this.logger.error("<readCompanyDate> failed for " + this.companyDateFile.getAbsolutePath(), e);
        }
    }

    private void setEvents(final Map<Long, List<Event>> events) {
        if (this.instrumentProvider != null) {
            try {
                Collection<Long> invalidIids
                        = this.instrumentProvider.validate(events.keySet());
                invalidIids.forEach(events::remove);
                this.logger.info("<setEvents> removed " + invalidIids.size() + " invalid iids");

                attachInstruments(events);
            } catch (Exception e) {
                this.logger.warn("<setEvents> Could not connect to instrumentServer");
            }
        }

        doSetEvents(events);
    }

    private void attachInstruments(Map<Long, List<Event>> events) {
        final List<Long> instrumentIds = new ArrayList<>(events.keySet());

        for (int fromIndex = 0, size = instrumentIds.size(); fromIndex < size; fromIndex += this.instrumentRequestChunkSize) {
            TimeTaker tt = new TimeTaker();
            List<Long> instrumentIdsChunk = instrumentIds.subList(fromIndex, Math.min(fromIndex + this.instrumentRequestChunkSize, size));
            this.instrumentProvider.identifyInstruments(instrumentIdsChunk)
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(
                            instrument ->
                                    events.getOrDefault(instrument.getId(), Collections.emptyList())
                                            .forEach(event -> event.setInstrument(instrument))
                    );
            this.logger.info("<attachInstruments> Fetching " + instrumentIdsChunk.size() + " instrumentIds took " + tt);
        }
    }

    private synchronized void doSetEvents(final Map<Long, List<Event>> events) {
        this.events = events;
        this.size =
                events.values()
                        .stream()
                        .mapToInt(List::size)
                        .sum();
    }

    private Collection<LocalDate> getDaysWithCompanyDates(LocalDate from, LocalDate to) {
        final int min = DateUtil.toYyyyMmDd(from);
        final int max = DateUtil.toYyyyMmDd(to);

        return this.events.values()
                        .stream()
                        .flatMap(Collection::stream)
                        .filter(event -> event.getYyyymmdd() >= min && event.getYyyymmdd() <= max)
                        .map(event -> DateUtil.yyyyMmDdToLocalDate(event.getYyyymmdd()))
                        .collect(Collectors.toSet());
    }

    public void selectDays(CompanyDateDaysRequest request, Set<LocalDate> days) {
        days.addAll(getDaysWithCompanyDates(request.getFrom(), request.getTo()));
    }

    synchronized int getSize() {
        return this.size;
    }

    synchronized void select(CompanyDateRequest request, PagedResultSorter<Event> sorter,
            EventSelector es) {

        final int min = (request.getFrom() != null) ? DateUtil.toYyyyMmDd(request.getFrom()) : MIN_FROM;
        final int max = (request.getTo() != null) ? DateUtil.toYyyyMmDd(request.getTo()) : MAX_TO;

        final Map<Long, List<Event>> map = getEvents(request.getIids());

        final Pattern ep = toPattern(request.getEvents());
        final Pattern nep = toPattern(request.getNonEvents());
        final boolean hasProfile = request.getProfile() != null;

        map.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(e -> e.getYyyymmdd() >= min && e.getYyyymmdd() <= max && es.select(e) && matches(e, ep, nep))
                .filter(e -> {
                    if (!hasProfile) {
                        return true;
                    }
                    try {
                        Instrument instrument = e.getInstrument();
                        return instrument != null && STRATEGY.getQuote(instrument) != null;
                    } catch (Exception ignore) {
                    }
                    return false;
                })
                .forEach(sorter::add);
    }

    private Pattern toPattern(Set<String> terms) {
        if (terms == null || terms.isEmpty()) {
            return null;
        }
        if (terms.size() == 1) {
            return Pattern.compile(terms.iterator().next(), Pattern.CASE_INSENSITIVE);
        }
        return Pattern.compile("(" + StringUtils.collectionToDelimitedString(terms, "|") + ")",
                Pattern.CASE_INSENSITIVE);
    }

    private boolean matches(Event e, Pattern eventPattern, Pattern nonEventPattern) {
        return !isNonEvent(e, nonEventPattern) && isEvent(e, eventPattern);
    }

    private boolean isEvent(Event e, Pattern eventPattern) {
        return eventPattern == null || matches(e, eventPattern);
    }

    private boolean isNonEvent(Event e, Pattern nonEventPattern) {
        return nonEventPattern != null && matches(e, nonEventPattern);
    }

    private boolean matches(Event e, Pattern p) {
        return p.matcher(e.getEvent().getDe()).find();
    }

    private Map<Long, List<Event>> getEvents(Set<Long> iids) {
        if (iids == null) {
            return this.events;
        }
        final Map<Long, List<Event>> result = new HashMap<>();
        for (Long iid : iids) {
            final List<Event> list = this.events.get(iid);
            if (list != null) {
                result.put(iid, list);
            }
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        try (AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(AmqpInstrumentAppConfig.class)) {
            try (final AmqpRpcConnectionManager closeable = annotationConfigApplicationContext.getBean(AmqpRpcConnectionManager.class)) {
                InstrumentProvider instrumentProvider = (InstrumentProvider) annotationConfigApplicationContext.getBean("instrumentProvider");

                final CompanyDateReader convensys = new CompanyDateReader();
                convensys.setActiveMonitor(new ActiveMonitor());
                convensys.setCompanyDateFile(new File(LocalConfigProvider.getProductionBaseDir(),
                    "var/data/provider/istar-convensys-companydate.xml.gz"));
                convensys.setInstrumentProvider(instrumentProvider);
                convensys.setInstrumentRequestChunkSize(1000);
                convensys.afterPropertiesSet();

                final CompanyDateReader wm = new CompanyDateReader();
                wm.setActiveMonitor(new ActiveMonitor());
                wm.setCompanyDateFile(new File(LocalConfigProvider.getProductionBaseDir(),
                    "var/data/provider/istar-wm-companydate.xml.gz"));
                wm.setInstrumentProvider(instrumentProvider);
                wm.setInstrumentRequestChunkSize(1000);
                wm.afterPropertiesSet();

                Set<Long> keys1 = convensys.events.keySet();
                Set<Long> keys2 = wm.events.keySet();

                System.out.println("Duplicates: " + keys1.stream().map(keys2::remove).filter(b -> b).count());
            }
        }
    }

    @Configuration
    static class AmqpInstrumentAppConfig extends AmqpAppConfig {

        @Bean
        InstrumentProvider instrumentProvider() throws Exception {
            InstrumentProviderImpl instrumentProvider = new InstrumentProviderImpl();
            instrumentProvider.setInstrumentServer(proxy(InstrumentServer.class));
            return instrumentProvider;
        }
    }
}
