/*
 * GisApoOrder.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.RatioFormatter;
import de.marketmaker.istar.domain.data.CorporateAction;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@MmInternal
public class GisApoOrder extends EasytradeCommandController {
    private static final String VIEW_NAME = "gisapoorder";

    public static class Command {
        private String[] isin;
        private String[] wkn;
        private String[] name;

        public String[] getIsin() {
            return isin;
        }

        public void setIsin(String[] isin) {
            this.isin = isin;
        }

        public String[] getName() {
            return name;
        }

        public void setName(String[] name) {
            this.name = name;
        }

        public String[] getWkn() {
            return wkn;
        }

        public void setWkn(String[] wkn) {
            this.wkn = wkn;
        }
    }

    private EasytradeInstrumentProvider instrumentProvider;
    private IntradayProvider intradayProvider;
    private HighLowProvider highLowProvider;
    private HistoricRatiosProvider historicRatiosProvider;
    private final RatioFormatter ratioFormatter = new RatioFormatter();


    public GisApoOrder() {
        super(Command.class);
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;
        final List<Quote> quotes = new ArrayList<>();
        final List<String> errorSymbols = new ArrayList<>();
        final List<String> errorDescriptions = new ArrayList<>();


        if (cmd.getIsin() != null) {
            for (String isin : cmd.getIsin()) {
                try {
                    final String[] tokens = isin.trim().split("\\@");
                    if (tokens.length != 2) {

                        quotes.add(this.instrumentProvider.identifyQuote(tokens[0], SymbolStrategyEnum.ISIN, null, null));
                        continue;
                    }
                    quotes.add(this.instrumentProvider.identifyQuote(tokens[0], SymbolStrategyEnum.ISIN, null, "market:" + tokens[1]));

                } catch (UnknownSymbolException e) {
                    errorSymbols.add(isin);
                    errorDescriptions.add("Isin unbekannt");
                }
            }
        }

        if (cmd.getWkn() != null) {
            for (String wkn : cmd.getWkn()) {
                try {
                    final String[] tokens = wkn.trim().split("\\@");

                    if (tokens.length != 2) {
                        quotes.add(this.instrumentProvider.identifyQuote(tokens[0], SymbolStrategyEnum.WKN, null, "market:FFMST,FFM,DDF,BLN,STG,HBG,MCH,ETR,EUS,EEU,FXVWD,FONDS,HNV,STG2"));
                        continue;
                    }
                    quotes.add(this.instrumentProvider.identifyQuote(tokens[0], SymbolStrategyEnum.WKN, null, "market:" + tokens[1]));

                } catch (UnknownSymbolException e) {
                    errorSymbols.add(wkn);
                    errorDescriptions.add("Wkn unbekannt");
                }
            }
        }


        if (cmd.getName() != null) {
            for (String name : cmd.getName()) {
                final SimpleSearchCommand command = new SimpleSearchCommand(name, 0, 5, 5, false);
                final SearchResponse searchResponse = this.instrumentProvider.simpleSearch(command);
                if (searchResponse.isValid() && searchResponse.getQuotes().size() > 0) {
                    quotes.addAll(searchResponse.getQuotes());
                } else {
                    errorSymbols.add(name);
                    errorDescriptions.add("Name unbekannt");
                }

            }

        }
        final List<List<String>> markets = new ArrayList<>();
        final List<List<DateTime>> splitDates = new ArrayList<>();
        final List<List<String>> splitRatios = new ArrayList<>();
        final List<BigDecimal> dividends = new ArrayList<>();

        for (Quote quote : quotes) {

            BigDecimal dividend = null;
            DateTime dividendDate = null;

            final List<CorporateAction> list
                    = this.historicRatiosProvider.getCorporateActions(SymbolQuote.create(quote),
                    DateUtil.getInterval("P10Y"), false);
            // splitDates and ratios per element
            final List<DateTime> spDates = new ArrayList<>();
            final List<String> spRatios = new ArrayList<>();
            for (Iterator<CorporateAction> corporateActionIterator = list.iterator(); corporateActionIterator.hasNext();) {
                final CorporateAction corporateAction = corporateActionIterator.next();

                if (corporateAction.getType() != CorporateAction.Type.FACTOR) {

                    if (dividendDate == null) {
                        dividendDate = corporateAction.getDate();
                    }
                    // get the latest dividend
                    if (corporateAction.getDate().isAfter(dividendDate)) {
                        dividend = corporateAction.getFactor();
                        dividendDate = corporateAction.getDate();
                    }
                    corporateActionIterator.remove();
                    continue;
                }
                final String ratio = ratioFormatter.format(BigDecimal.ONE.divideToIntegralValue(corporateAction.getFactor()));
                spDates.add(corporateAction.getDate());
                spRatios.add(ratio);

            }
            dividends.add(dividend);
            splitDates.add(spDates);
            splitRatios.add(spRatios);

            final List<Quote> quoteList = ProfiledInstrument.quotesWithPrices(quote.getInstrument(), RequestContextHolder.getRequestContext().getProfile());
            final List<String> ms = new ArrayList<>();
            for (Quote q : quoteList) {
                ms.add(q.getSymbolVwdfeedMarket());
            }
            markets.add(ms);
        }

        final ModelAndView result = new ModelAndView(VIEW_NAME);
        final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(quotes);
        final List<HighLow> highLows = this.highLowProvider.getHighLows52W(quotes, priceRecords);

        result.addObject("quotes", quotes);
        result.addObject("priceRecords", priceRecords);
        result.addObject("highLows", highLows);
        result.addObject("markets", markets);
        result.addObject("errorSymbols", errorSymbols);
        result.addObject("errorDescriptions", errorDescriptions);
        result.addObject("dividends", dividends);
        result.addObject("splitDates", splitDates);
        result.addObject("splitRatios", splitRatios);
        return result;
    }


}