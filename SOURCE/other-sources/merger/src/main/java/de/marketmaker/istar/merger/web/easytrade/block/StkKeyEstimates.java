/*
 * StkAnalystenschaetzung.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 *
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.StockRevenueSummary;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.StockRevenueSummaryImpl;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.estimates.EstimatesProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.merger.Constants;

import org.joda.time.DateTime;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.math.BigDecimal;


/**
 *
 * Returns the earning and cashflow estimation of a stock
 *
 */

public class StkKeyEstimates extends EasytradeCommandController {
    private EstimatesProvider estimatesProvider;
    private EasytradeInstrumentProvider instrumentProvider;

    public StkKeyEstimates() {
        super(DefaultSymbolCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setEstimatesProvider(EstimatesProvider estimatesProvider) {
        this.estimatesProvider = estimatesProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final SymbolCommand cmd = (SymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final List<StockRevenueSummary> estimates
                = this.estimatesProvider.getEstimates(profile, quote.getInstrument().getId());

        final Map<String, Object> model = new HashMap<>();

        final DateTime today = new DateTime();
        for (final StockRevenueSummary s : estimates) {
            if (s == null || s.getReference() == null) {
                continue;
            }
            final DateTime fiscalYearEnd = s.getReference().getEnd();
            if (today.isAfter(fiscalYearEnd)) {
                continue;
            }

            final BigDecimal per = valueOrNull(s.getPriceEarningRatio());

            if (per != null) {
                model.put("per", new Estimate(per, fiscalYearEnd));

                final BigDecimal eps = valueOrNull(s.getEarningPerShare());
                final BigDecimal cps = valueOrNull(s.getCashflow());
                if (eps != null && cps != null && cps.signum() != 0) {
                    model.put("pcr", new Estimate(per.multiply(eps, Constants.MC)
                            .divide(cps, Constants.MC), fiscalYearEnd));
                }

                break;
            }
        }

        model.put("quote", quote);

        return new ModelAndView("stkkeyestimates", model);
    }

    private BigDecimal valueOrNull(final StockRevenueSummaryImpl.Field f) {
        return (f != null) ? f.getValue() : null;
    }

    public static class Estimate {
        private final BigDecimal value;
        private final DateTime fiscalYearEnd;

        public Estimate(BigDecimal value, DateTime fiscalYearEnd) {
            this.value = value;
            this.fiscalYearEnd = fiscalYearEnd;
        }

        public BigDecimal getValue() {
            return value;
        }

        public DateTime getFiscalYearEnd() {
            return fiscalYearEnd;
        }
    }
}