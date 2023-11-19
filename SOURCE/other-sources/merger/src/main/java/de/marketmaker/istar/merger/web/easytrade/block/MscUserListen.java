/*
 * WlWatchlistAnzeigen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.merger.user.*;
import de.marketmaker.istar.domain.instrument.Quote;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Returns information about a user's watchlists and portfolios, information about the maximum
 * number of watchlists/portfolios for this user, and the maximum number
 * of positions per watchlist/portfolio.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscUserListen extends UserHandler {
    private static final String VIEW = "mscuserlisten";

    public static class Command extends UserCommandImpl {
        private boolean withPositions;

        /**
         * Set to <tt>true</tt> to request information about the positions in each watchlist
         * or portfolio; default value is <tt>false</tt>
         */
        public boolean isWithPositions() {
            return withPositions;
        }

        public void setWithPositions(boolean withPositions) {
            this.withPositions = withPositions;
        }
    }

    private EasytradeInstrumentProvider instrumentProvider;

    public MscUserListen() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;

        final UserContext userContext = getUserContext(cmd);

        final User user = userContext.getUser();
        final Company c = userContext.getCompany();

        final Map<String, Object> model = new HashMap<>();
        model.put("withPositions", cmd.isWithPositions());
        model.put("watchlists", getLists(cmd.isWithPositions(), user.getWatchlists(), true));
        model.put("portfolios", getLists(cmd.isWithPositions(), user.getPortfolios(), false));
        model.put("company", c);
        return new ModelAndView(VIEW, model);
    }

    private List<ListWithPositions> getLists(boolean withPositions, List<Portfolio> portfolios, boolean isWatchlist) {
        final List<ListWithPositions> lists = new ArrayList<>(portfolios.size());
        for (final Portfolio wl : portfolios) {
            final List<Quote> quotes = withPositions ? getQuotes(wl, isWatchlist) : null;
            lists.add(new ListWithPositions(wl, quotes));
        }
        return lists;
    }

    private List<Quote> getQuotes(Portfolio portfolio, boolean isWatchlist) {
        final List<PortfolioPosition> positions = portfolio.getPositions(isWatchlist);
        final List<Long> qids = new ArrayList<>(positions.size());
        for (final PortfolioPosition position : positions) {
            qids.add(position.getQid());
        }

        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(qids);
        final List<Quote> result = new ArrayList<>(qids.size());
        for (int i = 0; i < qids.size(); i++) {
            if (quotes.get(i) == null) {
                this.logger.warn("<getQuotes> no quote for: " + qids.get(i) + ".qid");
                continue;
            }
            result.add(quotes.get(i));
        }
        return result;
    }

    public static class ListWithPositions {
        private final Portfolio portfolio;
        private final List<Quote> positions;

        public ListWithPositions(Portfolio portfolio, List<Quote> positions) {
            this.portfolio = portfolio;
            this.positions = positions;
        }

        public Portfolio getPortfolio() {
            return portfolio;
        }

        public List<Quote> getPositions() {
            return positions;
        }
    }
}
