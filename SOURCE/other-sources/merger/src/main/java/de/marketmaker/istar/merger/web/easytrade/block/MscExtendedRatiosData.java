/*
 * StkKennzahlenBenchmark.java
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

import org.joda.time.Interval;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.ExtendedHistoricRatios;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.Period;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscExtendedRatiosData extends EasytradeCommandController {
    private final static long DAX_QUOTEID = 106547L;

    private HistoricRatiosProvider historicRatiosProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    protected ProfiledIndexCompositionProvider indexCompositionProvider;

    private String templateName = "extendedratiosdata";

    public MscExtendedRatiosData() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public void setIndexCompositionProvider(ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public static class Command extends DefaultSymbolCommand {
        private String[] period;

        @NotNull
        @Period
        public String[] getPeriod() {
            return ArraysUtil.copyOf(period);
        }

        public void setPeriod(String[] period) {
            this.period = ArraysUtil.copyOf(period);
        }
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final Long benchmarkQuoteId = this.indexCompositionProvider.getBenchmarkId(quote.getInstrument());
        final Quote quoteBenchmark = this.instrumentProvider.identifyQuotes(Arrays.asList(benchmarkQuoteId == null ? DAX_QUOTEID : benchmarkQuoteId)).get(0);

        final List<Interval> intervals = new ArrayList<>(cmd.getPeriod().length);
        for (final String p : cmd.getPeriod()) {
            intervals.add(DateUtil.getInterval(p));
        }

        final List<ExtendedHistoricRatios> ratios =
                this.historicRatiosProvider.getExtendedHistoricRatios(SymbolQuote.create(quote),
                        SymbolQuote.create(quoteBenchmark), intervals);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("benchmark", quoteBenchmark);
        model.put("intervals", intervals);
        model.put("ratios", ratios);
        return new ModelAndView(this.templateName, model);
    }
}