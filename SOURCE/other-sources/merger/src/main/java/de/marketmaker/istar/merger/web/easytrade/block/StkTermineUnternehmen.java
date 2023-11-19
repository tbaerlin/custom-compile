/*
 * StkTermineUnternehmen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.CompanyDate;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.Stock;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.CompanyDateImpl;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateProvider;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateRequest;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateResponse;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.Period;

/**
 * Provides event dates and titles of a company that issued the given instrument.
 * <p>
 * The events are restricted by a period determined by:
 * <ul>
 * <li>from a date in the past, this date is calculated based on the current date and a given period.</li>
 * <li>to a date in the future, one year from now on.</li>
 * </ul>
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample symbol 20665.iid
 */
public class StkTermineUnternehmen extends EasytradeCommandController {
    private static final LocalizedString ANNUAL_MEETING
            = LocalizedString.createDefault("Hauptversammlung")
            .add(Language.en, "Annual General Meeting")
            .add(Language.it, "Assemblea generale");  //from www.pons.eu

    private CompanyDateProvider companyDateProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setCompanyDateProvider(CompanyDateProvider companyDateProvider) {
        this.companyDateProvider = companyDateProvider;
    }

    public static final class Command extends DefaultSymbolCommand {

        private static final String DEFAULT_PERIOD = "6M";

        private String period = DEFAULT_PERIOD;

        /**
         * @return a period that will give the from-date in the past starting from current date.
         *         Default is {@value #DEFAULT_PERIOD}.
         */
        @Period
        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }
    }

    public StkTermineUnternehmen() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Map<String, Object> model = new HashMap<>();

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        model.put("quote", quote);

        if (quote != null) {
            final List<CompanyDate> companyDates = getCompanyDates(cmd, quote.getInstrument());
            model.put("companyDates", companyDates);
        }
        return new ModelAndView("stktermineunternehmen", model);
    }

    private List<CompanyDate> getCompanyDates(Command cmd, Instrument instrument) {
        final boolean postbankRequest = isPostbankRequest();
        final boolean convensysAllowed = isConvensysAllowed(postbankRequest);

        if (convensysAllowed) {
            return getConvensysCompanyDates(cmd, instrument, !postbankRequest);
        }

        if (isFacundaCalendarAllowed()) {
            return getConvensysCompanyDates(cmd, instrument, false);
        }

        final CompanyDate meetingDate = getGeneralMeetingDate(instrument);
        if (meetingDate != null) {
            return Collections.singletonList(meetingDate);
        }

        return Collections.emptyList();
    }

    private CompanyDate getGeneralMeetingDate(Instrument instrument) {
        if (instrument instanceof Stock) {
            final int date = ((Stock) instrument).getGeneralMeetingDate();
            if (date > 0) {
                return new CompanyDateImpl(instrument.getId(),
                        ANNUAL_MEETING, DateUtil.yyyyMmDdToYearMonthDay(date));
            }
        }
        return null;
    }

    private List<CompanyDate> getConvensysCompanyDates(Command cmd,
            Instrument instrument, boolean wmAllowed) {
        final CompanyDateRequest request =
                createCompanyDateRequest(cmd, instrument, wmAllowed);
        final CompanyDateResponse response = this.companyDateProvider.getCompanyDates(request);
        return response.getDates();
    }

    private CompanyDateRequest createCompanyDateRequest(Command cmd,
            Instrument instrument, boolean wmAllowed) {
        final CompanyDateRequest result = new CompanyDateRequest();
        result.setIids(Collections.singleton(instrument.getId()));
        if (cmd.getPeriod() != null) {
            final Interval interval = DateUtil.getInterval("P" + cmd.getPeriod().toUpperCase());
            result.setFrom(interval.getStart().toLocalDate());
            result.setTo(new LocalDate().plusYears(1));
        }
        result.setConvensysAllowed(true);
        result.setWmAllowed(wmAllowed);
        result.setCount(200);
        return result;
    }

    private boolean isConvensysAllowed(boolean postbankRequest) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        return profile.isAllowed(Selector.CONVENSYS_I)
                || profile.isAllowed(Selector.CONVENSYS_II)
                || postbankRequest;
    }

    private boolean isPostbankRequest() {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        return profile.getName().indexOf("easytrade") > 0
                || profile.getName().indexOf("direktportal") > 0;
    }

    private boolean isFacundaCalendarAllowed() {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        return profile.isAllowed(Selector.FACUNDA_COMPANY_CALENDAR);
    }
}
