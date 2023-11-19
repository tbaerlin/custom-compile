/*
 * MscInstruments.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Size;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.BaseMultiSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.MultiListSorter;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
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

/**
 * Returns for each symbol in a given list of financial instrument <code>symbol</code>s
 * a list of quotes from markets where the instrument is traded on.
 * <p>
 *     For derivatives, you can also query the underlyings.
 * </p>
 * <p>
 *     The returned list is ordered by the vwd market symbol.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscInstruments extends EasytradeCommandController {
    public static class Command extends BaseMultiSymbolCommand {
        /**
         * @return Identifiers for instruments or quotes that will be interpreted according to
         * the specified <tt>symbolStrategy</tt>. Underlyings of derivatives can be queried with
         * <code>underlying(</code><i>your symbol</i><code>)</code>.
         * @sample 25548.qid
         * @sample underlying(DE000CK247R6);
         * @sample DE000CK247R6
         */
        @NotNull
        @Size(min = 1, max = 500)
        public String[] getSymbol() {
            return super.getSymbol();
        }
    }

    protected EasytradeInstrumentProvider instrumentProvider;

    public MscInstruments() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final String[] symbols = cmd.getSymbol();
        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(Arrays.asList(symbols), cmd.getSymbolStrategy(), new MarketStrategies(cmd));

        final List<List<Quote>> quoteses = new ArrayList<>(symbols.length);

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final MultiListSorter mls = new MultiListSorter(QuoteComparator.BY_VWDFEED_MARKET, false);

        for (final Quote quote : quotes) {
            if (quote == null) {
                quoteses.add(Collections.<Quote>emptyList());
                continue;
            }

            final List<Quote> iquotes = ProfiledInstrument.quotesWithPrices(quote.getInstrument(), profile);
            mls.sort(iquotes);
            quoteses.add(iquotes);
        }


        final Map<String, Object> model = new HashMap<>();
        model.put("symbols", Arrays.asList(symbols));
        model.put("quotes", quotes);
        model.put("quoteses", quoteses);

        return new ModelAndView("mscinstruments", model);
    }
}
