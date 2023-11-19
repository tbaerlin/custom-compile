/*
 * KurseDde.java
 *
 * Created on 27.10.2008 10:12:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.webxl;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.merger.web.DataBinderUtils;

import java.util.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MusterdepotDde extends AbstractDde {
    private static final DataBinderUtils.Mapping MAPPING =
            new DataBinderUtils.Mapping().add("Xun", "xun").add("lattribs").add("d");

    protected DataBinderUtils.Mapping getParameterMapping() {
        return MAPPING;
    }

    public static class Command extends AbstractDde.Command {
        private Long d;
        private String lattribs;

        @NotNull
        public Long getD() {
            return d;
        }

        public void setD(Long d) {
            this.d = d;
        }

        @NotNull
        public String getLattribs() {
            return lattribs;
        }

        public void setLattribs(String lattribs) {
            this.lattribs = lattribs;
        }
    }

    public MusterdepotDde() {
        super(Command.class);
    }

    protected String getContent(Object o) {
        final Command cmd = (Command) o;

        final UserContext userContext = getUserContext(getUserCommand());
        final User user = userContext.getUser();
        final Portfolio p = user.getPortfolioOrWatchlist(cmd.getD());

        final Set<Long> qids = p != null ? p.getQuoteIds() : Collections.<Long>emptySet();
        final Map<String, Quote> symbolToQuote = new LinkedHashMap<>(qids.size());
        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(qids);
        CollectionUtils.removeNulls(quotes);
        for (final Quote quote : quotes) {
            symbolToQuote.put(quote.getSymbolVwdfeed(), quote);
        }

        return getQuoteContent(cmd.getLattribs(), symbolToQuote);
    }
}
