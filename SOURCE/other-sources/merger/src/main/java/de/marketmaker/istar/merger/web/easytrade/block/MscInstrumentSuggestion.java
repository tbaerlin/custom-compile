/*
 * MscInstrumentSuggestion.java
 *
 * Created on 17.06.2009 11:18:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.HasText;
import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.domain.data.SuggestedInstrument;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;

/**
 * Suggests instruments based on a few characters a user has typed in a search box. Results can
 * be used to display suggestions while the user still enters more characters. Limited to finding
 * stocks, funds, and indexes. Results are ordered based on significance.
 * <p>
 * <em>Implementation Note:</em> We would suggest that clients cache a small number of results
 * to achieve maximum performance. If, for example, the most recent 10 results are cached, there
 * is no need to issue a new request when the user deletes the last char.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscInstrumentSuggestion extends EasytradeCommandController {
    public final static class Command {

        private static final int DEFAULT_LIMIT = 10;

        private int limit = DEFAULT_LIMIT;
        private String query;
        private String strategy;

        /**
         * @return Number of suggestions to be returned, default is {@value #DEFAULT_LIMIT}.
         */
        @Range(min = 1, max = 20)
        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        /**
         * @return initial letters of a security name, a wkn or an isin.
         */
        @HasText
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        @MmInternal
        public String getStrategy() {
            return this.strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }
    }

    private InstrumentProvider instrumentProvider;

    public void setInstrumentProvider(InstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public MscInstrumentSuggestion() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command c = (Command) o;

        final List<SuggestedInstrument> result
                = this.instrumentProvider.getSuggestions(c.getQuery(), c.getLimit(), c.getStrategy());

        return new ModelAndView("mscinstrumentsuggest", "suggestions", result);
    }
}
