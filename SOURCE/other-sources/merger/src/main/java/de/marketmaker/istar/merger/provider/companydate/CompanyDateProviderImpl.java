/*
 * CompanyDateProviderImpl.java
 *
 * Created on 19.07.2008 13:56:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.companydate;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.LocalDate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.PagedResultSorter;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.ratios.frontend.SimpleComparatorChain;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CompanyDateProviderImpl implements CompanyDateProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CompanyDateReader wmProvider;

    private CompanyDateReader convensysProvider;

    public void setWmProvider(CompanyDateReader wmProvider) {
        this.wmProvider = wmProvider;
    }

    public void setConvensysProvider(CompanyDateReader convensysProvider) {
        this.convensysProvider = convensysProvider;
    }

    /**
     * Based on the entitlement of {@link Profile} items from the {@link CompanyDateReader}
     * will be selected by delegating {@link EventSelector}s to the corresponding {@link CompanyDateReader}.
     *
     * <p>
     * Currently WMData and Convensys Data sources are supported.
     * </p>
     */
    @Override
    public CompanyDateResponse getCompanyDates(CompanyDateRequest request) {
        if (!request.isConvensysAllowed() && !request.isWmAllowed()) {
            this.logger.info("<getCompanyDates> neither wm nor convensys allowed for " + request);
            return CompanyDateResponse.getInvalid();
        }

        Profile profile = request.getProfile();
        if (profile != null) {
            RequestContext requestContext = new RequestContext(profile, Collections.emptyMap());
            requestContext.setBaseQuoteFilter(request.getBaseQuoteFilter());
            RequestContextHolder.setRequestContext(requestContext);
        }

        final PagedResultSorter<Event> sorter = doGetCompanyDates(request);

        final CompanyDateResponse response = new CompanyDateResponse();
        response.setDates(Event.toCompanyDates(sorter.getResult()));
        response.setTotalCount(sorter.getTotalCount());
        return response;
    }

    @Override
    public CompanyDateDaysResponse getDaysWithCompanyDates(CompanyDateDaysRequest request) {
        final Set<LocalDate> days = new HashSet<>();
        if (request.isConvensysAllowed()) {
            selectDays(this.convensysProvider, request, days);
        }
        if (request.isWmAllowed()) {
            selectDays(this.wmProvider, request, days);
        }
        return new CompanyDateDaysResponse(new ArrayList<>(days));
    }

    private void selectDays(CompanyDateReader provider, CompanyDateDaysRequest request, Set<LocalDate> days) {
        provider.selectDays(request, days);
    }

    private PagedResultSorter<Event> doGetCompanyDates(CompanyDateRequest request) {
        final PagedResultSorter<Event> result = getResultSorter(request);

        if (request.isConvensysAllowed()) {
            select(this.convensysProvider, request, result);
        }
        if (request.isWmAllowed()) {
            select(this.wmProvider, request, result);
        }
        return result;
    }

    private PagedResultSorter<Event> getResultSorter(CompanyDateRequest request) {
        if (request.getCount() > 0) { // new style
            int num = 0;
            if (request.isConvensysAllowed()) {
                num += this.convensysProvider.getSize();
            }
            if (request.isWmAllowed()) {
                num += this.wmProvider.getSize();
            }
            return new PagedResultSorter<>(request.getOffset(), request.getCount(),
                    num * 2, // *2 as getSize might change until we are sync'ed again on the provider
                    getComparator(request));
        }
        else { // old style
            return new PagedResultSorter<>(0, 200, new Event.ByDateAndNameComparator(request.getQuoteNameStrategy()));
        }
    }

    private Comparator<Event> getComparator(CompanyDateRequest request) {
        final List<ListResult.Sort> sorts = request.getSorts();

        final SimpleComparatorChain<Event> result = new SimpleComparatorChain<>();

        boolean singleDay = isSingleDay(request);
        boolean singleSymbol = isSingleSymbol(request);

        if (!isWithDate(sorts) && !singleDay) {
            result.add(Collections.reverseOrder(Event.BY_DATE));
        }
        if (sorts.isEmpty() && !singleSymbol) {
            result.add(new Event.ByRelevanceComparator(request.getQuoteNameStrategy()));
        }

        for (ListResult.Sort sort : sorts) {
            String sortName = sort.getName();
            if ("date".equals(sortName)) {
                if (!singleDay) {
                    result.add(getComparator(Event.BY_DATE, sort));
                }
            }
            else if ("event".equals(sortName)) {
                if (request.getLanguage() != null) {
                    result.add(getComparator(new Event.EventComparator(request.getLanguage()), sort));
                }
                else if (request.getLocales() != null && !request.getLocales().isEmpty()) {
                    final Language language = Language.valueOf(request.getLocales().get(0));
                    result.add(getComparator(new Event.EventComparator(language), sort));
                }
            }
            else if (!singleSymbol) {
                if ("name".equals(sortName)) {
                    result.add(getComparator(Event.getByNameComparator(request.getQuoteNameStrategy()), sort));
                }
                else if ("relevance".equals(sortName)) {
                    result.add(getComparator(new Event.ByRelevanceComparator(request.getQuoteNameStrategy()), sort));
                }
                else if ("isin".equals(sortName)) {
                    result.add(getComparator(Event.BY_SYMBOL, sort));
                }
                else if ("wkn".equals(sortName)) {
                    result.add(getComparator(Event.BY_WKN, sort));
                }
            }
        }

        if (!singleSymbol && !isWith(sorts, "name")) {
            result.add(Event.getByNameComparator(request.getQuoteNameStrategy()));
        }

        return result;
    }

    private boolean isWithDate(List<ListResult.Sort> sorts) {
        return isWith(sorts, "date");
    }

    private boolean isWith(List<ListResult.Sort> sorts, String sort) {
        return sorts.stream().map(ListResult.Sort::getName).anyMatch(sort::equals);
    }

    private Comparator<Event> getComparator(Comparator<Event> c, ListResult.Sort sort) {
        return sort.isAscending() ? c : Collections.reverseOrder(c);
    }

    private boolean isSingleSymbol(CompanyDateRequest request) {
        return request.getIids() != null && request.getIids().size() == 1;
    }

    private boolean isSingleDay(CompanyDateRequest r) {
        return r.getFrom() != null && r.getTo() != null && r.getFrom().equals(r.getTo());
    }

    private void select(final CompanyDateReader provider, CompanyDateRequest request,
                        PagedResultSorter<Event> sorter) {
        final EventSelector es = new EventSelector (request.getLanguage(),
                request.isWmAllowed() && provider == this.convensysProvider);
        provider.select(request, sorter, es);
    }

    public static void main(String[] args) throws Exception {
        try (AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(CompanyDateReader.AmqpInstrumentAppConfig.class)) {
            InstrumentProvider instrumentProvider = (InstrumentProvider) annotationConfigApplicationContext.getBean("instrumentProvider");
            final CompanyDateProviderImpl cp = createProvider(instrumentProvider);

            TimeTaker tt0 = new TimeTaker();

            final CompanyDateRequest request = new CompanyDateRequest();
            request.setConvensysAllowed(true);
            request.setWmAllowed(true);
//        request.setIids(new HashSet<Long>(Arrays.asList(2036189L)));
            request.setFrom(new LocalDate(2015, 12, 11));
            //request.setTo(new LocalDate(2010, 8, 12));
            request.setSortBy("name, date");
            request.setAscending(true);
            request.setOffset(0);
            request.setCount(10000);
            request.setQuoteNameStrategyName("de.marketmaker.istar.domain.instrument.QuoteNameStrategies#WM_WP_NAME_KURZ");
//        request.setEvents(Collections.singleton("kapitalmaßnahme"));
            //request.setEvents(new HashSet<>(Arrays.asList(Pattern.quote("(foo|"), "dividende")));
//        request.setNonEvents(new HashSet<String>(Arrays.asList("konferenz", "dividende", "hauptversammlung", "(bericht|ergebnis|abschlu)", "aufsicht", "kapitalmaßnahme")));
            PagedResultSorter<Event> response;

            RequestContext requestContext = new RequestContext(ProfileFactory.valueOf(true), Collections.emptyMap());
            requestContext.setBaseQuoteFilter(request.getBaseQuoteFilter());
            RequestContextHolder.setRequestContext(requestContext);

//        do {
            final TimeTaker tt = new TimeTaker();
            response = cp.doGetCompanyDates(request);
            tt.stop();

            List<String> providedNames =
                    response.getResult()
                            .stream()
                            .map(Event::getInstrument)
                            .filter(Objects::nonNull)
                            .map(CompanyDateReader.STRATEGY::getQuote)
                            .map(quote -> request.getQuoteNameStrategy().getName(quote))
                            .collect(Collectors.toList());

            List<String> sortedNames = new ArrayList<>(providedNames);
            Collections.sort(sortedNames, Collator.getInstance(Locale.GERMAN));

            System.out.println("\n\n\n==================================");
            for (int i = 0; i < providedNames.size(); i++) {
                String provided = providedNames.get(i);
                String sorted = sortedNames.get(i);
                if (!provided.equals(sorted)) {
                    System.out.println("misatch: " + provided + " vs. " + sorted);
                }
            }

        /*
        response.getResult().forEach(r -> {
            Instrument instrument = r.getInstrument();
            if (instrument == null) {
                System.out.println("Instrument null: " + r);
            }
            else {
                String symbol = request.getQuoteNameStrategy().getName(CompanyDateReader.STRATEGY.getQuote(instrument));
                System.out.println(symbol);
            }
        });
        */
            System.out.println("==================================\n\n\n");

        /*System.out.println(request.getOffset() + ", #: " + response.getResult().size()
                + " (" + response.getTotalCount() + "), took " + tt);
        request.setOffset(request.getOffset() + request.getCount());
//        } while (request.getOffset() < response.getTotalCount());
//        System.out.println(response);

        System.out.println("Took " + tt0);*/
            annotationConfigApplicationContext.close();
        }
    }

    public static CompanyDateProviderImpl createProvider(InstrumentProvider instrumentProvider) throws Exception {


        final CompanyDateReader convensys = new CompanyDateReader();
        convensys.setActiveMonitor(new ActiveMonitor());
        convensys.setCompanyDateFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-convensys-companydate.xml.gz"));
        convensys.setInstrumentProvider(instrumentProvider);
        convensys.setInstrumentRequestChunkSize(1000);
        convensys.afterPropertiesSet();

        final CompanyDateReader wm = new CompanyDateReader();
        wm.setActiveMonitor(new ActiveMonitor());
        wm.setCompanyDateFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-wm-companydate.xml.gz"));
        wm.setInstrumentProvider(instrumentProvider);
        wm.setInstrumentRequestChunkSize(1000);
        wm.afterPropertiesSet();

        /* enable as soon as we got the data, see: R-78119
        final CompanyDateReader benl = new CompanyDateReader();
        benl.setActiveMonitor(new ActiveMonitor());
        benl.setCompanyDateFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-benl-companydate.xml.gz"));
        benl.afterPropertiesSet();
        */

        final CompanyDateProviderImpl cp = new CompanyDateProviderImpl();
        cp.setWmProvider(wm);
        cp.setConvensysProvider(convensys);
        //cp.setBenlProvider(benl);
        return cp;
    }
}
