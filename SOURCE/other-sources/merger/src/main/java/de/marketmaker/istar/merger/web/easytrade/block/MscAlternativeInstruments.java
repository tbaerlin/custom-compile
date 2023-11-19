/*
 * PfAuswertung.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.user.AlternativeIid;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.web.easytrade.ListCommandForUser;
import de.marketmaker.istar.merger.web.easytrade.ListHelper;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Lösch
 */

public class MscAlternativeInstruments extends UserHandler {

    private final static int MAX_INSTRUMENTS = 20;

    private EasytradeInstrumentProvider instrumentProvider;

    public static class Command extends ListCommandForUser {
        private String symbol;

        /**
         * @sample 4229.qid
         */
        @NotNull
        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public boolean isAscending() {
            return false;
        }

        @Override
        public String getUserid() {
            return super.getUserid();
        }
    }

    public MscAlternativeInstruments() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    private AlternativeIid findById(List<AlternativeIid> iids, Quote quote) {
        for (AlternativeIid iid : iids) {
            if (iid.getIid() == quote.getInstrument().getId()) {
                return iid;
            }
        }
        throw new IllegalStateException("could not find iid " + quote.getInstrument().getId());
    }

    private List<Quote> filterInstrumentsWithPrices(List<Quote> quotes) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final List<Quote> result = new ArrayList<>();
        for (final Quote quote : quotes) {
            if (quote != null && !ProfiledInstrument.quotesWithPrices(quote.getInstrument(), profile).isEmpty()) {
                result.add(quote);
                if (result.size() == MAX_INSTRUMENTS) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * iid-list includes possibly obsolete ids, ids of instruments without prices or wrong type.
     * to avoid bulky calls to instrumentProvider, the iid-list is shortened to
     * a (probably) reasonable size.
     * @param iids list of alternative instrument Ids
     */
    private void shorteningAlternativeInstrumentList(List<AlternativeIid> iids) {
        if (iids.size() > MAX_INSTRUMENTS * 2) {
            iids.subList(MAX_INSTRUMENTS * 2, iids.size()).clear();
        }
    }


    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;
        Instrument instrument =
                this.instrumentProvider.identifyInstrument(cmd.getSymbol(), SymbolStrategyEnum.AUTO);
        final User user;
        if (cmd.getUserid() != null) {
            user = getUserContext(cmd).getUser();
        }
        else {
            user = null;
        }

        // sorted by weight and instrumentid
        List<AlternativeIid> iids = getUserProvider().getAlternativeIids(instrument.getId(), user);
        shorteningAlternativeInstrumentList(iids);

        final List<Quote> rawQuotes = this.instrumentProvider.identifyQuotes(
                AlternativeIid.getIids(iids),
                SymbolStrategyEnum.IID,
                new MarketStrategies((String) null)
        );

        final List<Quote> quotes = filterInstrumentsWithPrices(rawQuotes);

        final ListResult listResult =
                ListResult.create(cmd, Collections.singletonList("weight"), "weight", quotes.size());
        ListHelper.clipPage(cmd, quotes);
        listResult.setCount(quotes.size());

        final List<Float> weights = new ArrayList<>();
        for (final Quote quote : quotes) {
            weights.add(findById(iids, quote).getWeight());
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("listinfo", listResult);
        model.put("quotes", quotes);
        model.put("weights", weights);

        return new ModelAndView("mscalternativeinstruments", model);
    }
}
