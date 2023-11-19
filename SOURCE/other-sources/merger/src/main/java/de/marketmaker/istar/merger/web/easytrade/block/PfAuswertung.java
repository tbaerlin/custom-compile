/*
 * PfAuswertung.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 *
 */

package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.PortfolioEvaluationProvider;
import de.marketmaker.istar.merger.user.EvaluatedPortfolio;
import de.marketmaker.istar.merger.user.EvaluatedPosition;
import de.marketmaker.istar.merger.user.EvaluatedPositionComparator;
import de.marketmaker.istar.merger.user.EvaluatedPositionGrouparator;
import de.marketmaker.istar.merger.user.NoSuchPortfolioException;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.merger.web.easytrade.ListCommandForUser;
import de.marketmaker.istar.merger.web.easytrade.ListResult;


/**
 * Shows detailed information about a users portfolio (identified by parameter portfolioid)
 * and a list of all existing portfolios of that user.
 */

public class PfAuswertung extends UserHandler {
    private static final String DEFAULT_SORT_BY = "boersenplatz";

    private static final Callable<Comparator<EvaluatedPosition>> BY_NAME
            = new Callable<Comparator<EvaluatedPosition>>() {
        @Override
        public Comparator<EvaluatedPosition> call() throws Exception {
            return new Comparator<EvaluatedPosition>() {
                private final Comparator<Quote> cq = SortSupport.QUOTE_NAME.call();

                @Override
                public int compare(EvaluatedPosition o1, EvaluatedPosition o2) {
                    return this.cq.compare(o1.getQuote(), o2.getQuote());
                }
            };
        }
    };

    private static final SortSupport<EvaluatedPosition> SORT_SUPPORT
            = SortSupport.createBuilder(DEFAULT_SORT_BY, EvaluatedPositionComparator.BY_VWDFEED_MARKET)
            .add("name", BY_NAME) // use the Callable which supports localized names
            .add("waehrung", EvaluatedPositionComparator.BY_CURRENCY)
            .add("differenzRelativ", EvaluatedPositionComparator.BY_CURRENT_CHANGE_PERCENT)
            .add("datum", EvaluatedPositionComparator.BY_CURRENT_LASTDATE)
            .build();

    private static Map<String, String> SORT_DMXML_CONVERSIONS = new HashMap<>();

    private boolean withEmptyPositions = false;

    static {
        SORT_DMXML_CONVERSIONS.put("boersenplatz", "marketVwd");
        SORT_DMXML_CONVERSIONS.put("name", "name");
        SORT_DMXML_CONVERSIONS.put("waehrung", "currencyIso");
        SORT_DMXML_CONVERSIONS.put("differenzRelativ", "changePercentPeriod");
        SORT_DMXML_CONVERSIONS.put("datum", "date");
    }

    private static final List<String> SORT_FIELDS = SortSupport.getSortFields(SORT_SUPPORT);

    private static final List<String> SORT_FIELDS_DMXML = Collections.unmodifiableList(
            new ArrayList<>(SORT_DMXML_CONVERSIONS.values()));

    private PortfolioEvaluationProvider evaluationProvider;

    private HighLowProvider highLowProvider;

    public void setWithEmptyPositions(boolean withEmptyPositions) {
        this.withEmptyPositions = withEmptyPositions;
    }

    public void setEvaluationProvider(PortfolioEvaluationProvider evaluationProvider) {
        this.evaluationProvider = evaluationProvider;
    }

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    public static class Command extends ListCommandForUser {
        private Long portfolioid;

        private boolean extendedPriceData = false;

        private String groupBy;

        public Long getPortfolioid() {
            return portfolioid;
        }

        public void setPortfolioid(Long portfolioid) {
            this.portfolioid = portfolioid;
        }

        /**
         * If <code>true</code> return {@link de.marketmaker.iview.dmxml.PriceDataExtended} for each element.
         */
        public boolean isExtendedPriceData() {
            return extendedPriceData;
        }

        public void setExtendedPriceData(boolean extendedPriceData) {
            this.extendedPriceData = extendedPriceData;
        }

        public String getGroupBy() {
            return groupBy;
        }

        public void setGroupBy(String groupBy) {
            this.groupBy = groupBy;
        }
    }

    public PfAuswertung() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        checkSortBy(cmd);

        final UserContext uc = getUserContext(cmd);
        final User user = uc.getUser();

        final Map<String, Object> model = new HashMap<>();
        model.put("extendedPriceData", cmd.isExtendedPriceData());
        model.put("dmxmlfields", SORT_FIELDS_DMXML);

        if (user.getPortfolios().isEmpty()) {
            final ListResult emptyListResult = ListResult.create(cmd, SORT_FIELDS, DEFAULT_SORT_BY, 0);
            model.put("listinfo", emptyListResult);
            model.put("dmxmlsortedby", SORT_DMXML_CONVERSIONS.get(emptyListResult.getSortedBy()));
            return new ModelAndView("pfauswertung", model);
        }

        final Long pfid = cmd.getPortfolioid();
        final Portfolio portfolio;
        if (pfid == null) {
            portfolio = user.getPortfolios().get(0);
        }
        else {
            portfolio = user.getPortfolio(pfid);
            if (portfolio == null) {
                errors.rejectValue("portfolioid", NoSuchPortfolioException.USER_PORTFOLIOID_INVALID,
                        new Object[]{pfid}, "Invalid portfolioid: " + pfid);
                return null;
            }
        }

        final EvaluatedPortfolio evaluatedPortfolio
                = this.evaluationProvider.evaluate(portfolio, true);

        model.put("portfolios", user.getPortfolios());
        model.put("evaluation", evaluatedPortfolio);

        // note: above eval must always be performed for all (means: also empty) positions
        // to correctly calculate realized gains,cost,etc.
        // => re-eval portfolio if it should contain only non-empty positions in the output
        final List<EvaluatedPosition> positions = this.withEmptyPositions
                ? evaluatedPortfolio.getPositions()
                : this.evaluationProvider.evaluate(portfolio, false).getNonEmptyPositions();

        String groupByStr = cmd.getGroupBy();
        EvaluatedPositionGrouparator.GroupBy groupBy = EvaluatedPositionGrouparator.getGroupBy(groupByStr);
        List<EvaluatedPosition> groupedPositions = EvaluatedPositionGrouparator.createGroupedPositions(positions, groupBy, portfolio.getNotes());

        final ListResult listResult
                = ListResult.create(cmd, SORT_FIELDS, DEFAULT_SORT_BY, groupedPositions.size());

        SORT_SUPPORT.apply(listResult.getSortedBy(), cmd, groupedPositions);
        listResult.setCount(groupedPositions.size());

        final List<Quote> quotes = new ArrayList<>(groupedPositions.size());
        final List<HighLow> highLows = new ArrayList<>(quotes.size());
        for (EvaluatedPosition position : groupedPositions) {
            final Quote quote = position.getQuote();
            quotes.add(quote);
            final PriceRecord pr = position.getCurrentPrice();
            final HighLow hl = this.highLowProvider.getHighLow52W(quote, pr);
            highLows.add(hl);
        }
        model.put("highLows", highLows);

        model.put("listinfo", listResult);
        model.put("positions", groupedPositions);
        model.put("dmxmlsortedby", SORT_DMXML_CONVERSIONS.get(listResult.getSortedBy()));
        model.put("groupedBy", groupBy.name());
        model.put("grouptypes", EvaluatedPositionGrouparator.GroupBy.values());
        return new ModelAndView("pfauswertung", model);
    }

    /**
     * helper for converting possibly english sorting column names (from dmxml) to postbank slang german column names.
     */
    private void checkSortBy(Command cmd) {
        if (SORT_DMXML_CONVERSIONS.containsKey(cmd.getSortBy())) {
            return;
        }

        for (final Map.Entry<String, String> entry : SORT_DMXML_CONVERSIONS.entrySet()) {
            if (entry.getValue().equals(cmd.getSortBy())) {
                cmd.setSortBy(entry.getKey());
                return;
            }
        }
    }
}