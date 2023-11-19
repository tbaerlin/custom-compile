/*
 * StkKursdaten.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.common.validator.Max;
import de.marketmaker.istar.common.validator.Min;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.Tick;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.timeseries.Timeseries;
import de.marketmaker.istar.merger.provider.IntradayData;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.NoDataException;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscReferenceTickData extends EasytradeCommandController {
    protected IntradayProvider intradayProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public static class Command extends DefaultSymbolCommand {
        private DateTime date;

        private int numTrades;

        @NotNull
        public DateTime getDate() {
            return date;
        }

        public void setDate(DateTime date) {
            this.date = date;
        }

        @Min(value = 1)
        @Max(value = 100)
        public int getNumTrades() {
            return numTrades;
        }

        public void setNumTrades(int numTrades) {
            this.numTrades = numTrades;
        }
    }

    public MscReferenceTickData() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {

        final Command cmd = (Command) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);

        final Interval interval = cmd.getDate().toLocalDate().toInterval();
        final IntradayData id = this.intradayProvider.getIntradayData(quote, interval);
        final TickRecord tr = id.getTicks();
        if (tr == null) {
            throw new NoDataException("no data available for " + cmd.getDate().toLocalDate());
        }
        final Timeseries<Tick> timeseries = tr.getTimeseries(interval, TickType.TRADE);

        model.put("trades", getTrades(timeseries, cmd.getDate(), cmd.getNumTrades()));

        return new ModelAndView("mscreferencetickdata", model);
    }

    private List<TickImpl> getTrades(Timeseries<Tick> timeseries, DateTime date, int numTicks) {
        int count = 0;
        int index = 0;
        for (final DataWithInterval<Tick> dwi : timeseries) {
            if (dwi.getData().getType() != TickType.TRADE) {
                continue;
            }

            if (!dwi.getInterval().getStart().isAfter(date)) {
                index = count;
            }
            count++;
        }

        final List<TickImpl> trades = new ArrayList<>(numTicks * 2);
        final int start = Math.max(0, index - numTicks);
        final int end = Math.min(count, index + numTicks);

        count = 0;
        for (final DataWithInterval<Tick> dwi : timeseries) {
            if (dwi.getData().getType() != TickType.TRADE) {
                continue;
            }

            if (count >= start && count <= end) {
                final Tick t = dwi.getData();
                trades.add(new TickImpl(t.getInstant().toDateTime(), PriceCoder.decode(t.getPrice()),
                        t.getVolume(), t.getSupplement(), t.getTradeIdentifier(), TickImpl.Type.valueOf(t.getType())));
            }

            count++;
        }
        return trades;
    }
}
