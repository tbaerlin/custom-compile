/*
 * CurDevisenrechner.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import static de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum.VWDCODE;

import de.marketmaker.istar.common.validator.Pattern;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.web.easytrade.HasQuote;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Provides cross rates for given currencies.
 * <p>
 * Cross rates are retrieved depending on how the currencies are given:
 * <dl>
 * <dt>as an array of currency ISO codes</dt>
 * <dd>cross rates for all kinds of pair combinations as source to target</dd>
 * <dt>as an array of source to target currency ISO pairs: <code>XXX/YYY</code></dt>
 * <dd>cross rates for only the given source and target currency pairs</dd>
 * </dl>
 * </p>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CurCrossRateTable extends EasytradeCommandController {
    public static class Command {
        private String[] isocode;

        private String[] isocodeSourceTarget;

        /**
         * @return an array of 3-character ISO currency codes.
         * @sample EUR, USD, CHF
         */
        @Pattern(regex = "[A-Z]{3}")
        public String[] getIsocode() {
            return isocode;
        }

        public void setIsocode(String[] isocode) {
            this.isocode = isocode;
        }

        /**
         * @return an array of source to target currency pairs. Currency symbols in 3-character ISO
         * format. A pair is given in format: <code>XXX/YYY</code>, e.g. separated with slash.
         * Ignored if {@link #getIsocode()} is defined.
         */
        @Pattern(regex = "[A-Z]{3}/[A-Z]{3}")
        public String[] getIsocodeSourceTarget() {
            return isocodeSourceTarget;
        }

        public void setIsocodeSourceTarget(String[] isocodeSourceTarget) {
            this.isocodeSourceTarget = isocodeSourceTarget;
        }
    }

    public CurCrossRateTable() {
        super(Command.class);
    }

    private IsoCurrencyConversionProvider isoCurrencyConversionProvider;

    private IntradayProvider intradayProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public void setIsoCurrencyConversionProvider(
            IsoCurrencyConversionProvider isoCurrencyConversionProvider) {
        this.isoCurrencyConversionProvider = isoCurrencyConversionProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        // TODO: add dependency of XRATE OR function selector (to be defined)
        return RequestContextHolder.callWith(ProfileFactory.valueOf(true),
                new Callable<ModelAndView>() {
                    public ModelAndView call() throws Exception {
                        return handle(cmd);
                    }
                });
    }

    private ModelAndView handle(Command cmd) {
        List<CrossRateCell> cells = getCells(cmd);

        final List<String> vwdcodes = new ArrayList<>();
        for (Iterator iterator = cells.iterator(); iterator.hasNext(); ) {
            CrossRateCell cell = (CrossRateCell) iterator.next();
            String vkey = this.isoCurrencyConversionProvider.getCrossRateSymbol(cell.from, cell.to);
            if (vkey != null) {
                String vwdcode = vkey.substring(vkey.indexOf(".") + 1);
                vwdcodes.add(vwdcode);
                cell.symbol = vwdcode;
            }
            else {
                iterator.remove();
            }
        }

        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(vwdcodes, VWDCODE, null, null);
        final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(quotes);

        for (int i = 0; i < cells.size(); i++) {
            final CrossRateCell cell = cells.get(i);
            cell.priceRecord = priceRecords.get(i);
            cell.quote = quotes.get(i);
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("cells", cells);
        return new ModelAndView("curcrossratetable", model);
    }

    private List<CrossRateCell> getCells(Command cmd) {
        LinkedHashSet<CrossRateCell> result = new LinkedHashSet<>();
        if (cmd.getIsocode() != null) {
            for (final String from : cmd.getIsocode()) {
                for (final String to : cmd.getIsocode()) {
                    if (!from.equals(to)) {
                        result.add(new CrossRateCell(from, to));
                    }
                }
            }
        }
        else {
            for (final String s2t : cmd.getIsocodeSourceTarget()) {
                final String[] codes = s2t.split("/");
                if (!codes[0].equals(codes[1])) {
                    result.add(new CrossRateCell(codes[0], codes[1]));
                }
            }
        }
        return new ArrayList<>(result);
    }

    public static class CrossRateCell implements HasQuote {
        private final String from;

        private final String to;

        private String symbol;

        private PriceRecord priceRecord;

        private Quote quote;

        public CrossRateCell(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getSymbol() {
            return symbol;
        }

        public PriceRecord getPriceRecord() {
            return priceRecord;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CrossRateCell that = (CrossRateCell) o;
            return from.equals(that.from) && to.equals(that.to);
        }

        @Override
        public int hashCode() {
            return 31 * from.hashCode() + to.hashCode();
        }

        @Override
        public Quote getQuote() {
            return this.quote;
        }
    }
}