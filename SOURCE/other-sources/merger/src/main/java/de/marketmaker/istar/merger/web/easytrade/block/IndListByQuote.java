/*
 * StkRelPerformanceVergleichsindex.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.comparator.CompoundComparator;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.instrument.IndexMembershipRequest;
import de.marketmaker.istar.instrument.IndexPrioritySupport;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;

/**
 * Queries all indices in which the given symbol is listed.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IndListByQuote extends EasytradeCommandController {

    public static class Command extends DefaultSymbolCommand {
        private boolean forInstrument = false;

        private boolean sortByDescendingPriority = false;

        /**
         * If <code>false</code> (default) the given symbol
         * evaluates to a quote and all indices containing exactly that quote are returned.
         * <p>
         * If <code>true</code>, the given symbol evaluates to an instrument.
         * All indices containing one of the quotes of that instrument are returned.
         */
        public boolean isForInstrument() {
            return forInstrument;
        }

        public void setForInstrument(boolean forInstrument) {
            this.forInstrument = forInstrument;
        }

        /**
         * If true, the resulting quotes will be sorted by descending priority first and then
         * by name; if false, sorting will be by name only. Default is false.
         */
        public boolean isSortByDescendingPriority() {
            return sortByDescendingPriority;
        }

        public void setSortByDescendingPriority(boolean sortByDescendingPriority) {
            this.sortByDescendingPriority = sortByDescendingPriority;
        }
    }

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public IndListByQuote() {
        super(Command.class);
    }

    public void setIndexCompositionProvider(ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final Set<Long> indexQuoteids = getIndexQuoteIds(cmd, quote);
        final List<Quote> indexQuotes = this.instrumentProvider.identifyQuotes(indexQuoteids);
        CollectionUtils.removeNulls(indexQuotes);

        Comparator<Quote> cmp = getComparator(cmd);

        indexQuotes.sort(cmp);

        final List<Integer> priorities = getIndexPriorities(indexQuotes);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("indices", indexQuotes);
        model.put("priorities", priorities);
        return new ModelAndView("indlistbyquote", model);
    }

    private Set<Long> getIndexQuoteIds(Command cmd, Quote quote) {
        final IndexMembershipRequest request = cmd.isForInstrument()
                ? new IndexMembershipRequest(quote.getInstrument())
                : new IndexMembershipRequest(quote);
        return this.indexCompositionProvider.getIndexMembership(request).getIndexQuoteIds();
    }

    private List<Integer> getIndexPriorities(List<Quote> indexQuotes) {
        final List<Integer> result = new ArrayList<>(indexQuotes.size());
        for (final Quote quote : indexQuotes) {
            result.add(IndexPrioritySupport.getPriority(quote.getId()));
        }
        return result;
    }

    private Comparator<Quote> getComparator(Command cmd) {
        final Comparator<Quote> byName
                = QuoteComparator.byName(RequestContextHolder.getRequestContext().getQuoteNameStrategy());
        if (cmd.isSortByDescendingPriority()) {
            return new CompoundComparator<>(IndexPrioritySupport.DESCENDING_PRIORITY, byName);
        }
        return byName;
    }
}
