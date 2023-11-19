/*
 * StkUnternehmensportrait.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.report.ReportConstraint;
import de.marketmaker.istar.merger.provider.report.ReportServiceDelegate;
import de.marketmaker.istar.merger.web.easytrade.BaseMultiSymbolCommand;

/**
 * Checks if there are any reports available for the given instruments.
 * <p>
 * Each client is configured with access to one or more report providers. This service will tell
 * if there are any reports available for some instruments for the querying client from its
 * configured providers. Such query can be further refined with language and country specifications.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscReportsAvailability extends EasytradeCommandController {
    public static class Command extends BaseMultiSymbolCommand {
        private String language;

        private String country;

        private String filterStrategy;

        private DateTime date;

        private String type;

        @NotNull
        public String[] getSymbol() {
            return super.getSymbol();
        }

        /**
         * @return 2 character language symbol according to ISO 639-1.
         */
        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        /**
         * @return 2 character country symbol according to ISO 3166-1.
         */
        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        /**
         * @return apply special filter strategy.
         */
        @RestrictedSet("DZBANK-PIB")
        public String getFilterStrategy() {
            return filterStrategy;
        }

        public void setFilterStrategy(String filterStrategy) {
            this.filterStrategy = filterStrategy;
        }

        /**
         * @return date of requested reports
         */
        public DateTime getDate() {
            return date;
        }

        public void setDate(DateTime date) {
            this.date = date;
        }

        /**
         * @return type of requested reports
         */
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    private ReportServiceDelegate reportServiceDelegate;

    private EasytradeInstrumentProvider instrumentProvider;

    private String template = "mscreportsavailability";

    private boolean uniqueTypes = false;

    public MscReportsAvailability() {
        super(Command.class);
    }

    public void setReportServiceDelegate(ReportServiceDelegate reportServiceDelegate) {
        this.reportServiceDelegate = reportServiceDelegate;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setUniqueTypes(boolean uniqueTypes) {
        this.uniqueTypes = uniqueTypes;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final List<Quote> quotes
                = this.instrumentProvider.identifyQuotes(Arrays.asList(cmd.getSymbol()),
                cmd.getSymbolStrategy(), new MarketStrategies(cmd));

        final List<Boolean> availables = new ArrayList<>();

        final ReportConstraint constraint =
                new ReportConstraint(getLanguage(cmd), cmd.getCountry(), cmd.getFilterStrategy(),
                        this.uniqueTypes, cmd.getDate(), cmd.getType());
        for (final Quote quote : quotes) {
            if (quote == null) {
                availables.add(false);
                continue;
            }

            availables.add(this.reportServiceDelegate.isReportsAvailable(
                    quote.getInstrument().getId(), quote.getInstrument().getInstrumentType(),
                    constraint));
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("quotes", quotes);
        model.put("availables", availables);
        return new ModelAndView(this.template, model);
    }

    private String getLanguage(Command cmd) {
        return null == cmd.getLanguage() ?
                RequestContextHolder.getRequestContext().getLocale().getLanguage() :
                cmd.getLanguage();
    }
}
