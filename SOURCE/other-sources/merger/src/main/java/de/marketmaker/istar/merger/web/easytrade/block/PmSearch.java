/*
 * PmSearch.java
 *
 * Created on 18.07.12 07:44
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.IndexConstants;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;

/**
 * Search for quotes for portfolio manager search interface to fill dynamic dp files interface.
 * @author tkiesgen
 */
@MmInternal
public class PmSearch extends EasytradeCommandController {
    private static final List<String> SEARCH_FIELDS = Arrays.asList(
            IndexConstants.FIELDNAME_NAME,
            IndexConstants.FIELDNAME_ALIAS,
            KeysystemEnum.MMNAME.name().toLowerCase(),
            KeysystemEnum.MMWKN.name().toLowerCase(),
            KeysystemEnum.WM_WP_NAME.name().toLowerCase(),
            KeysystemEnum.WM_WP_NAME_KURZ.name().toLowerCase(),
            KeysystemEnum.WM_WP_NAME_ZUSATZ.name().toLowerCase(),
            KeysystemEnum.TICKER.name().toLowerCase(),
            KeysystemEnum.EUREXTICKER.name().toLowerCase(),
            KeysystemEnum.ISIN.name().toLowerCase(),
            KeysystemEnum.WKN.name().toLowerCase(),
            KeysystemEnum.VWDCODE.name().toLowerCase(),
            KeysystemEnum.OEWKN.name().toLowerCase()
    );

    public static class Command extends ListCommand {
        private String searchterm;

        @NotNull
        public String getSearchterm() {
            return searchterm;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setSearchterm(String searchterm) {
            this.searchterm = searchterm;
        }
    }

    private EasytradeInstrumentProvider instrumentProvider;

    public PmSearch() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {

        final Command cmd = (Command) o;

        final SimpleSearchCommand sc = new SimpleSearchCommand(cmd.getSearchterm(),
                SEARCH_FIELDS, null, InstrumentProvider.StrategyEnum.DEFAULT,
                null, null, null, null,
                cmd.getOffset(), cmd.getCount(), 1000, true);
        sc.setFilterOpra(false);

        final SearchResponse sr = this.instrumentProvider.simpleSearch(sc);

        final List<Instrument> instruments = sr.getInstruments();
        final List<Quote> quotes = new ArrayList<>(instruments.size());
        for (final Instrument instrument : instruments) {
            quotes.add(this.instrumentProvider.getQuote(instrument, MarketStrategy.PORTFOLIO_MANAGER));
        }

        final ListResult listResult = ListResult.create(cmd,
                Collections.<String>emptyList(), "relevance", sr.getTotalTypesCount());
        listResult.setCount(quotes.size());

        final Map<String, Object> model = new HashMap<>();
        model.put("listinfo", listResult);
        model.put("quotes", quotes);
        return new ModelAndView("pmsearch", model);
    }
}
