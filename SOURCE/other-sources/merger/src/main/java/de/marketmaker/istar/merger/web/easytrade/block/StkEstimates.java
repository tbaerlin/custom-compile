/*
 * StkAnalystenschaetzung.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.StockRevenueSummary;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.estimates.EstimatesProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Returns estimates of the economic development of a given stock corporation for the given fiscal years, depending on the availability of data.
 * <p>
 * Data may be available for last year (<code>fy0</code>), this year (<code>fy1</code>), and next year (<code>fy2</code>).
 * </p>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkEstimates extends EasytradeCommandController {
    private EstimatesProvider estimatesProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public StkEstimates() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setEstimatesProvider(EstimatesProvider estimatesProvider) {
        this.estimatesProvider = estimatesProvider;
    }

    public static class Command extends DefaultSymbolCommand {
        private String[] year;

        /**
         * @return The desired fiscal years. The fiscal year is a single-digit number prefixed with <code>fy</code>.
         * @sample fy1, fy2
         */
        public String[] getYear() {
            return year;
        }

        public void setYear(String[] year) {
            this.year = year;
        }
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final List<StockRevenueSummary> estimates = getEstimates(quote);

        final Map<String, Object> model = new HashMap<>();

        final Set<String> years = getYears(cmd);
        final List<String> resultyears = new ArrayList<>();

        for (int index = 0; index < estimates.size(); index++) {
            final StockRevenueSummary e = estimates.get(index);

            final String year = "fy" + index;

            if (years.contains(year)) {
                model.put(year, e);
                resultyears.add(year);
            }
        }

        final StockRevenueSummary defaultEstimate = estimates.isEmpty() ? null : estimates.get(0);
        model.put("default", defaultEstimate);
        model.put("years", resultyears);
        model.put("referencequote", quote);

        return new ModelAndView("stkestimates", model);
    }

    private List<StockRevenueSummary> getEstimates(Quote quote) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        return this.estimatesProvider.getEstimates(profile, quote.getInstrument().getId());
    }

    private Set<String> getYears(Command cmd) {
        if (cmd.getYear() != null) {
            return new HashSet<>(Arrays.asList(cmd.getYear()));
        }
        return Collections.emptySet();
    }
}
