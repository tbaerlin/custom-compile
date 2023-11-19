/*
 * MscTermine.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.validator.DateFormat;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.CompanyDate;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateProvider;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateRequest;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateResponse;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;

/**
 * Queries company events for a given date.
 * <p>
 * Company events on the given date is returned, along with the associated instrument and quote
 * data.
 * </p>
 * <p>
 * The returned company events can be sorted on different fields, which can be obtained by each
 * response.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscTermine extends EasytradeCommandController {
    private final static DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final String DEFAULT_SORTBY = "relevance";

    public MscTermine() {
        super(Command.class);
    }

    private CompanyDateProvider companyDateProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setCompanyDateProvider(CompanyDateProvider companyDateProvider) {
        this.companyDateProvider = companyDateProvider;
    }

    public static class Command extends ListCommand {
        private String date;

        @MmInternal
        public String getDatum() {
            return getDate();
        }

        public void setDatum(String datum) {
            setDate(datum);
        }

        /**
         * @return the date for which company events are queried.
         */
        @DateFormat(format = "yyyy-MM-dd")
        public String getDate() {
            return this.date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        @MmInternal
        YearMonthDay getDay() {
            if (this.date == null) {
                return new YearMonthDay();
            }
            return DTF.parseDateTime(this.date).toYearMonthDay();
        }
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final CompanyDateRequest cdr = new CompanyDateRequest();
        cdr.setConvensysAllowed(true);
        cdr.setFrom(cmd.getDay().toLocalDate());
        cdr.setTo(cdr.getFrom());
        cdr.setCount(cmd.getCount());
        cdr.setOffset(cmd.getOffset());
        cdr.setSortBy(cmd.getSortBy());
        cdr.setAscending(cmd.isAscending());
        cdr.setLocales(RequestContextHolder.getRequestContext().getLocales());
        cdr.setLanguage(getLanguage());
        final CompanyDateResponse r = this.companyDateProvider.getCompanyDates(cdr);

        final List<CompanyDate> companyDates = r.getDates();
        final Set<Long> iids = new HashSet<>(companyDates.size());
        for (final CompanyDate cd : companyDates) {
            iids.add(cd.getInstrumentid());
        }
        final List<Instrument> rawInstruments = this.instrumentProvider.identifyInstruments(new ArrayList<>(iids));
        CollectionUtils.removeNulls(rawInstruments);

        final Map<Long, Quote> iidToQuote = new HashMap<>();

        for (final Quote quote : getQuotes(rawInstruments)) {
            iidToQuote.put(quote.getInstrument().getId(), quote);
        }

        final List<Instrument> instruments = new ArrayList<>(iidToQuote.size());
        final List<Quote> quotes = new ArrayList<>(iidToQuote.size());
        for (final Iterator<CompanyDate> it = companyDates.iterator(); it.hasNext(); ) {
            final Quote quote = iidToQuote.get(it.next().getInstrumentid());
            if (quote == null) {
                it.remove();
                continue;
            }

            instruments.add(quote.getInstrument());
            quotes.add(quote);
        }

        final ListResult listResult = ListResult.create(cmd,
                new ArrayList<>(CompanyDateRequest.SORTFIELDS), DEFAULT_SORTBY, companyDates.size());
        listResult.setCount(companyDates.size());

        final Map<String, Object> model = new HashMap<>();
        model.put("companyDates", companyDates);
        model.put("instruments", instruments);
        model.put("quotes", quotes);
        model.put("listinfo", listResult);
        return new ModelAndView("msctermine", model);
    }

    private Language getLanguage() {
        final List<Locale> locales = RequestContextHolder.getRequestContext().getLocales();
        return Language.valueOf(locales.get(0));
    }

    private List<Quote> getQuotes(List<Instrument> instruments) {
        final List<Quote> result = new ArrayList<>(instruments.size());
        final MarketStrategy strategy = MarketStrategyFactory.defaultStrategy();
        for (final Instrument instrument : instruments) {
            try {
                result.add(strategy.getQuote(instrument));
            } catch (Exception e) {
                this.logger.debug("<getQuotes> no quote allowed for " + instrument);
            }
        }
        return result;
    }

}
