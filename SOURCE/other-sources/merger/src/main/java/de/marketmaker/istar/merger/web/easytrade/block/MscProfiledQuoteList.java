/*
 * StkKursdaten.java
 *
 * Created on 07.07.2006 10:28:22
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

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.PermissionDeniedException;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

/**
 * Returns instrument/quotedata for those elements in a list of symbols for which the user has
 * at least the permission to access end-of-day prices. For symbols denoting an index, the check
 * can be extended so that an index's instrument/quotedata will only be included in the result
 * if the user has at least end-of-day permissions for all index constituents.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscProfiledQuoteList extends EasytradeCommandController {
    public static class Command {
        private SymbolStrategyEnum symbolStrategy;
        private String[] symbol;
        private String[] marketStrategy;
        private boolean constituents = false;

        /**
         * @return symbols of instruments to be checked
         */
        @NotNull
        public String[] getSymbol() {
            return symbol;
        }

        public void setSymbol(String[] symbol) {
            this.symbol = HttpRequestUtil.filterParametersWithText(symbol);
        }

        /**
         * @return how the symbols are to be interpreted
         */
        public SymbolStrategyEnum getSymbolStrategy() {
            return symbolStrategy;
        }

        public void setSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
            this.symbolStrategy = symbolStrategy;
        }

        /**
         * Names of market strategies that will be used to select the
         * quotes of the respective index, which in turn will be checked for permissions. Will be
         * ignored if <tt>constituents</tt> is false; otherwise, the number of strategies has to
         * be the same as the number of <tt>symbol</tt>s. If <tt>constituents</tt> is true and this
         * parameter is undefined, the original index constituents (e.g., quotes at <tt>ETR</tt>
         * for the DAX) will be used.
         */
        public String[] getMarketStrategy() {
            return marketStrategy;
        }

        public void setMarketStrategy(String[] marketStrategy) {
            this.marketStrategy = marketStrategy;
        }

        /**
         * @return true if the permissions of index constituents should be checked.
         */
        public boolean isConstituents() {
            return constituents;
        }

        public void setConstituents(boolean constituents) {
            this.constituents = constituents;
        }
    }

    private String template = "mscprofiledquotelist";

    private EasytradeInstrumentProvider instrumentProvider;
    private ProfiledIndexCompositionProvider indexCompositionProvider;

    public MscProfiledQuoteList() {
        super(Command.class);
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIndexCompositionProvider(ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;

        if (cmd.isConstituents() && cmd.getMarketStrategy() != null
                && cmd.getMarketStrategy().length != cmd.getSymbol().length) {
            throw new BadRequestException("marketStrategy.length ("
                    + cmd.getMarketStrategy().length + ") != symbol.length ("
                    + cmd.getSymbol().length + ")");
        }

        final List<String> requestSymbols = new ArrayList<>(Arrays.asList(cmd.getSymbol()));
        final List<Quote> quotes
                = this.instrumentProvider.identifyQuotes(Arrays.asList(cmd.getSymbol()),
                cmd.getSymbolStrategy(), null, null);

        filterByProfile(quotes);

        handleConstituents(cmd, quotes);

        CollectionUtils.removeNulls(quotes, requestSymbols);

        final Map<String, Object> model = new HashMap<>();
        model.put("quotes", quotes);
        model.put("requestSymbols", requestSymbols);
        return new ModelAndView(template, model);
    }

    private void filterByProfile(List<Quote> quotes) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            if (quote != null && profile.getPriceQuality(quote) == PriceQuality.NONE) {
                quotes.set(i, null);
            }
        }
    }

    private void handleConstituents(Command cmd, List<Quote> quotes) {
        if (!cmd.isConstituents()) {
            return;
        }

        final String[] strategy = cmd.getMarketStrategy();

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            if (quote == null || quote.getInstrument().getInstrumentType() != InstrumentTypeEnum.IND) {
                continue;
            }

            final String marketStrategy = strategy == null
                    || !StringUtils.hasText(strategy[i]) ? null : strategy[i];

            List<Long> qids = getIndexConstituents(quote);
            if (qids == null) {
                quotes.set(i, null);
                continue;
            }

            final List<Quote> constituents = this.instrumentProvider.identifyQuotes(qids);

            if (marketStrategy == null) {
                if (!isAllowed(profile, constituents)) {
                    quotes.set(i, null);
                }
            }
            else {
                for (final Quote constituent : constituents) {
                    if (constituent == null) {
                        continue;
                    }
                    final Quote cQuote = this.instrumentProvider.getQuote(constituent.getInstrument(), null, marketStrategy);
                    if (cQuote != null && profile.getPriceQuality(cQuote) == PriceQuality.NONE) {
                        quotes.set(i, null);
                        break;
                    }
                }
            }
        }
    }

    private List<Long> getIndexConstituents(Quote quote) {
        final IndexCompositionResponse response;
        try {
            response = this.indexCompositionProvider.getIndexCompositionByQid(quote.getId());
        } catch (PermissionDeniedException e) {
            return null;
        }
        if (!response.isValid()) {
            return null;
        }
        return response.getIndexComposition().getQids();
    }

    private boolean isAllowed(Profile profile, List<Quote> constituents) {
        for (final Quote constituent : constituents) {
            if (constituent != null && profile.getPriceQuality(constituent) == PriceQuality.NONE) {
                return false;
            }
        }
        return true;
    }
}