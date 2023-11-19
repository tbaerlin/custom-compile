/*
 * WlWatchlistAendern.java
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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.user.UpdatePortfolioCommand;

/**
 * Changes the name of an existing watchlist
 */
public class WlWatchlistAendern extends UserHandler {
    @SuppressWarnings("UnusedDeclaration")
    public static final class Command extends UserCommandImpl {
        private Long watchlistid;
        private String name;

        /**
         * @return watchlists new name
         */
        @NotNull
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @NotNull
        public Long getWatchlistid() {
            return watchlistid;
        }

        public void setWatchlistid(Long watchlistid) {
            this.watchlistid = watchlistid;
        }
    }

    public WlWatchlistAendern() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;
        final Long localUserId = getLocalUserId(cmd);

        final UpdatePortfolioCommand uwc = new UpdatePortfolioCommand();
        uwc.setUserid(localUserId);
        uwc.setName(cmd.getName());
        uwc.setPortfolioid(cmd.getWatchlistid());

        getUserProvider().updatePortfolio(uwc);

        final Map<String, Object> model = new HashMap<>();
        model.put("watchlistid", cmd.getWatchlistid());
        return new ModelAndView("wlwatchlistaendern", model);
    }
}
