/*
 * WlWatchlistAnlegen.java
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
import de.marketmaker.istar.merger.user.AddPortfolioCommand;

/**
 * Creates a new watchlist for the given user with the given name
 */
public class WlWatchlistAnlegen extends UserHandler {
    public static final class Command extends UserCommandImpl {
        private String name;

        /**
         * @return new watchlists name
         */
        @NotNull
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public WlWatchlistAnlegen() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Long localUserId = getLocalUserId(cmd);

        final Long watchlistid = createWatchlist(localUserId, cmd.getName());

        final Map<String, Object> model = new HashMap<>();
        model.put("watchlistid", watchlistid);
        return new ModelAndView("wlwatchlistanlegen", model);
    }
}
