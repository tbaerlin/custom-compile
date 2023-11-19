/*
 * Event.java
 *
 * Created on 08.06.2010 14:19:14
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.companydate;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import net.jcip.annotations.Immutable;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.CompanyDate;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.domainimpl.data.CompanyDateImpl;

/**
 * @author oflege
 */
@Immutable
final class Event {
    private final static Collator GERMAN_COLLATOR;

    static {
        GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);
        // Ignore case
        GERMAN_COLLATOR.setStrength(Collator.SECONDARY);
    }

    private static final Map<QuoteNameStrategy, ByNameComparator> BY_NAME_COMPARATORS = new HashMap<>();

    static class ByNameComparator implements Comparator<Event> {

        private final QuoteNameStrategy quoteNameStrategy;

        private ByNameComparator(QuoteNameStrategy quoteNameStrategy) {
            this.quoteNameStrategy = quoteNameStrategy;
        }

        public int compare(Event e1, Event e2) {
            if (this.quoteNameStrategy != null && e1.getInstrument() != null && e2.getInstrument() != null) {
                Quote quote1 = CompanyDateReader.STRATEGY.getQuote(e1.getInstrument());
                Quote quote2 = CompanyDateReader.STRATEGY.getQuote(e2.getInstrument());
                return GERMAN_COLLATOR.compare(
                        this.quoteNameStrategy.getName(quote1),
                        this.quoteNameStrategy.getName(quote2)
                );
            }
            else {
                return GERMAN_COLLATOR.compare(e1.name, e2.name);
            }
        }
    }

    static class EventComparator implements Comparator<Event> {
        private final Language language;

        EventComparator(Language language) {
            this.language = language;
        }

        public int compare(Event e1, Event e2) {
            return GERMAN_COLLATOR.compare(
                    e1.event.getLocalized(this.language),
                    e2.event.getLocalized(this.language));
        }
    }

    static Comparator<Event> BY_SYMBOL = (e1, e2) -> {
            if (e1.symbol == null) {
                return e2.symbol == null ? 0 : 1;
            }
            if (e2.symbol == null) {
                return -1;
            }
            return e1.symbol.compareTo(e2.symbol);
        };

    static Comparator<Event> BY_WKN = (e1, e2) -> {
        if (e1.instrument == null) {
            return e2.instrument == null ? 0 : 1;
        }
        if (e2.instrument == null) {
            return -1;
        }
        String symbolWkn1 = e1.instrument.getSymbolWkn();
        String symbolWkn2 = e2.instrument.getSymbolWkn();
        if (symbolWkn1 == null) {
            return symbolWkn2 == null ? 0 : 1;
        }
        if (symbolWkn2 == null) {
            return -1;
        }
        return symbolWkn1.compareTo(symbolWkn2);
    };

    static Comparator<Event> BY_DATE = (e1, e2) -> e1.yyyymmdd - e2.yyyymmdd;

    static class ByDateAndNameComparator implements Comparator<Event> {

        private final ByNameComparator byNameComparator;

        ByDateAndNameComparator(QuoteNameStrategy quoteNameStrategy) {
            this.byNameComparator = getByNameComparator(quoteNameStrategy);
        }

        @Override
        public int compare(Event o1, Event o2) {
            final int cmp = BY_DATE.compare(o1, o2);
            return cmp != 0 ? cmp : this.byNameComparator.compare(o1, o2);
        }
    }

    static class ByRelevanceComparator implements Comparator<Event> {

        private final ByNameComparator byNameComparator;

        ByRelevanceComparator(QuoteNameStrategy quoteNameStrategy) {
            this.byNameComparator = getByNameComparator(quoteNameStrategy);
        }

        @Override
        public int compare(Event e1, Event e2) {
            final int cmp = Double.compare(e2.rank * e2.getFactor(), e1.rank * e1.getFactor());
            return cmp != 0 ? cmp : this.byNameComparator.compare(e1, e2);
        }
    }

    public static ByNameComparator getByNameComparator(QuoteNameStrategy quoteNameStrategy) {
        ByNameComparator byNameComparator = BY_NAME_COMPARATORS.get(quoteNameStrategy);
        if (byNameComparator == null) {
            byNameComparator = new ByNameComparator(quoteNameStrategy);
            BY_NAME_COMPARATORS.put(quoteNameStrategy, byNameComparator);
        }

        return byNameComparator;
    }

    private double getFactor() {
        if (symbol != null) {
            if (symbol.startsWith("DE")) {
                return 1000;
            }
            if (symbol.startsWith("US")) {
                return 10;
            }
        }
        return 1;
    }

    static List<CompanyDate> toCompanyDates(List<Event> events) {
        return events
                .stream()
                .map(Event::toCompanyDate)
                .collect(Collectors.toList());
    }

    private final long iid;

    private final LocalizedString event;

    private final String name;

    private final String symbol;

    private final int yyyymmdd;

    private final double rank;

    private transient Instrument instrument;

    Event(long iid, String name, String symbol, LocalizedString event, int yyyymmdd, double rank) {
        this.iid = iid;
        this.name = name;
        this.symbol = symbol;
        this.event = event;
        this.yyyymmdd = yyyymmdd;
        this.rank = rank;
    }

    public long getIid() {
        return this.iid;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    @Override
    public String toString() {
        return new StringBuilder(100).append("Event[").append(this.yyyymmdd)
                .append(", ").append(iid)
                .append(".iid, ").append(this.name)
                .append(", ").append(this.symbol)
                .append(", ").append(this.event)
                .append(", ").append(BigDecimal.valueOf(this.rank).toPlainString())
                .append("]").toString();
    }

    CompanyDate toCompanyDate() {
        return new CompanyDateImpl(this.iid, this.event, DateUtil.yyyyMmDdToYearMonthDay(this.yyyymmdd));
    }

    public int getYyyymmdd() {
        return this.yyyymmdd;
    }

    public LocalizedString getEvent() {
        return this.event;
    }
}
