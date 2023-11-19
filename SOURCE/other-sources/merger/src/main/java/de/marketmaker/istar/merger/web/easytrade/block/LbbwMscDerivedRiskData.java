/*
 * StkKennzahlenBenchmark.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.BasicHistoricRatios;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;

/**
 * Provides subset of ratio data for a given instrument.
 * <p>
 * The subset of ratio data returned depends on the block using this service.
 * <table border="1">
 * <tr><th>Block Name</th><th>Subset of ratio data</th></tr>
 * <tr><td>MSC_BasicRatios</td><td>Basic ratio data related to the given instrument itself or its
 * benchmark</td></tr>
 * <tr><td>MSC_DerivedRiskData</td><td>Ratio data related to the given instrument and derived
 * ratio data from the instrument and its benchmark.</td></tr>
 * </table>
 * </p>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample symbol 25548.qid
 */
public class LbbwMscDerivedRiskData extends EasytradeCommandController {
    public static class Command extends DefaultSymbolCommand {
        private String[] period;

        private Period aggregation;

        private LocalDate referenceDate;

        /**
         * @return time span for desired ratio data.
         * @sample P1M, P1Y
         */
        @NotNull
        @de.marketmaker.istar.merger.web.easytrade.Period
        public String[] getPeriod() {
            return ArraysUtil.copyOf(this.period);
        }

        public void setPeriod(String[] period) {
            this.period = ArraysUtil.copyOf(period);
        }

        /**
         * @return time span to aggregate price data used for calculating ratio data.
         */
        public Period getAggregation() {
            return aggregation;
        }

        public void setAggregation(Period aggregation) {
            this.aggregation = aggregation;
        }

        /**
         * @return reference to use - if null, date of last price will be used.
         */
        public LocalDate getReferenceDate() {
            return referenceDate;
        }

        public void setReferenceDate(LocalDate referenceDate) {
            this.referenceDate = referenceDate;
        }
    }

    private HistoricRatiosProvider historicRatiosProvider;

    protected IntradayProvider intradayProvider;

    private FundDataProvider fundDataProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    protected ProfiledIndexCompositionProvider indexCompositionProvider;

    private String templateName = "lbbwmscderivedriskdata";

    public LbbwMscDerivedRiskData() {
        super(Command.class);
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public void setIndexCompositionProvider(
            ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final MscBasicRatiosMethod method = new MscBasicRatiosMethod(cmd, cmd.getReferenceDate(),
                cmd.getPeriod(), cmd.getAggregation(), this.instrumentProvider,
                this.indexCompositionProvider, this.fundDataProvider, this.intradayProvider,
                this.historicRatiosProvider);
        final List<BasicHistoricRatios> ratios = method.invoke();

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", method.getQuote());
        model.put("benchmark", method.getQuoteBenchmark());
        model.put("intervals", MscBasicRatiosMethod.getOutputPeriods(cmd.getPeriod()));
        model.put("ratios", ratios);
        return new ModelAndView(this.templateName, model);
    }
}