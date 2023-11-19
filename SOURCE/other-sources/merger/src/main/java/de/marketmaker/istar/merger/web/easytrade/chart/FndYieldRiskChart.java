/*
 * FndYieldRiskChart.java
 *
 * Created on 10.12.2007 13:17:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.util.FndBenchmarkRatios;
import de.marketmaker.istar.merger.web.easytrade.util.FndRatios;
import de.marketmaker.istar.merger.web.easytrade.util.FndSectorPerformance;
import de.marketmaker.istar.merger.web.easytrade.util.FndSectorVola;
import de.marketmaker.istar.ratios.frontend.MinMaxAvgRatioSearchResponse;
import de.marketmaker.istar.chart.ChartModelAndView;
import de.marketmaker.istar.chart.data.SimpleXYDataItem;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndYieldRiskChart extends AbstractImgChart {
    private HistoricRatiosProvider historicRatiosProvider;

    private FundDataProvider fundDataProvider;

    private RatiosProvider ratiosProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private static final BigDecimal MIN = BigDecimal.valueOf(-1, -6); // -1Mio

    private static final BigDecimal MAX = BigDecimal.valueOf(1, -6);// 1Mio

    public FndYieldRiskChart() {
        super(BaseImgSymbolCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    protected ChartModelAndView createChartModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object object,
            BindException bindException) throws Exception {
        final BaseImgSymbolCommand cmd = (BaseImgSymbolCommand) object;

        final ChartModelAndView result = createChartModelAndView(cmd);
        final Map<String, Object> model = result.getModel();

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        if (quote == null || quote.getInstrument().getInstrumentType() != InstrumentTypeEnum.FND) {
            return null;
        }

        final Map<String,Object> fr = new FndRatios(this.ratiosProvider, quote, bindException).compute();
        if (fr == null) {
            return null;
        }

        final RatioDataRecord fundRatios = (RatioDataRecord) fr.get(FndRatios.FUND_RATIOS_KEY);

        final BigDecimal risk = fundRatios.getVolatility1y();
        final BigDecimal yield = fundRatios.getBVIPerformance1Year();
        if (!isRiskOk(risk) || !isRiskOk(yield)) {
            return null;
        }

        final QuoteNameStrategy qns
                = RequestContextHolder.getRequestContext().getQuoteNameStrategy();

        model.put("fund", new SimpleXYDataItem("fund", qns.getName(quote),
                toPercent(risk), toPercent(yield)));

        final Map<String, Object> bratios = new FndBenchmarkRatios(this.instrumentProvider,
                this.fundDataProvider, this.historicRatiosProvider, quote).compute();
        final BigDecimal brisk = (BigDecimal) bratios.get(FndBenchmarkRatios.VOLA_P1Y);
        final BigDecimal byield = (BigDecimal) bratios.get(FndBenchmarkRatios.PERF_P1Y);
        if (isRiskOk(brisk) && isRiskOk(byield)) {
            model.put("benchmark", new SimpleXYDataItem("benchmark", "Benchmark",
                    toPercent(brisk), toPercent(byield)));
        }

        final Map<String, Object> syields
                = new FndSectorPerformance(this.ratiosProvider, fundRatios).compute();
        final MinMaxAvgRatioSearchResponse.MinMaxAvg syield
                = (MinMaxAvgRatioSearchResponse.MinMaxAvg) syields.get("mma1Year");
        if (syield != null && isRiskOk(syield.getAvg())) {
            final Map<String, Object> srisks
                    = new FndSectorVola(this.ratiosProvider, fundRatios).compute();
            final MinMaxAvgRatioSearchResponse.MinMaxAvg srisk
                            = (MinMaxAvgRatioSearchResponse.MinMaxAvg) srisks.get("mma1Year");
            if (srisk != null && isRiskOk(srisk.getAvg())) {
                model.put("sector", new SimpleXYDataItem("sector", "Sector",
                        toPercent(srisk.getAvg()), toPercent(syield.getAvg())));
            }
        }

        return result;
    }

    private BigDecimal toPercent(BigDecimal bd) {
        return bd.multiply(ONE_HUNDRED, Constants.MC);
    }

    private boolean isRiskOk(BigDecimal bd) {
        return bd != null && MIN.compareTo(bd) == -1 && MAX.compareTo(bd) == 1;
    }
}
