/*
 * StkKennzahlenBenchmark.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.BasicHistoricRatios;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.ISOPeriodFormat;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscHistoricalRatios extends EasytradeCommandController {
    private final static long DAX_QUOTEID = 106547L;

    private HistoricRatiosProvider historicRatiosProvider;

    protected IntradayProvider intradayProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    protected ProfiledIndexCompositionProvider indexCompositionProvider;

    private String templateName = "mschistoricalratios";


    public MscHistoricalRatios() {
        super(Command.class);
    }

    public void setIndexCompositionProvider(ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
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

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public static class Command extends DefaultSymbolCommand {
        private LocalDate from;
        private LocalDate to;

        @NotNull
        public LocalDate getFrom() {
            return from;
        }

        public void setFrom(LocalDate from) {
            this.from = from;
        }

        @NotNull
        public LocalDate getTo() {
            return to;
        }

        public void setTo(LocalDate to) {
            this.to = to;
        }
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final Long benchmarkQuoteId = this.indexCompositionProvider.getBenchmarkId(quote.getInstrument());
        final Quote quoteBenchmark = this.instrumentProvider.identifyQuotes(Arrays.asList(benchmarkQuoteId == null ? DAX_QUOTEID : benchmarkQuoteId)).get(0);
        final Interval interval = new Interval(cmd.getFrom().toDateTimeAtStartOfDay(), cmd.getTo().toDateTimeAtStartOfDay());
        final BasicHistoricRatios historicRatios =
                this.historicRatiosProvider.getBasicHistoricRatios(SymbolQuote.create(quote), SymbolQuote.create(quoteBenchmark), Collections.singletonList(interval)).get(0);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("benchmark", quoteBenchmark);
        model.put("from", cmd.getFrom());
        model.put("to", cmd.getTo());
        model.put("interval", ISOPeriodFormat.standard().print(interval.toPeriod()));

        final BasicHistoricRatios result;
        if (cmd.getTo().equals(new LocalDate())) {
            final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(Arrays.asList(quote, quoteBenchmark));
            final PriceRecord current = priceRecords.get(0);
            final PriceRecord currentBenchmark = priceRecords.get(1);
            result = historicRatios.copy(current, currentBenchmark);
        } else {
            result = historicRatios;
        }

        model.put("ratios", result);
        return new ModelAndView(this.templateName, model);
    }
}