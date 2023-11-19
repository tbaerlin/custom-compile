/*
 * WlPositionAnlegen.java
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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.user.AddPositionCommand;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.PortfolioPosition;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.ErrorUtils;

/**
 * Creates a new Position of the given Symbol in the given Watchlist
 */

public class WlPositionAnlegen extends UserHandler {
    private EasytradeInstrumentProvider instrumentProvider;

    @SuppressWarnings("UnusedDeclaration")
    public static final class Command extends DefaultSymbolCommand
            implements InitializingBean, UserCommand {
        private String userid;

        private Long watchlistid;

        private Long companyid;

        private String watchlistName;

        private boolean distinctIid = false;

        @Override
        public void afterPropertiesSet() throws Exception {
            if (this.watchlistid == null && this.watchlistName == null) {
                throw new BadRequestException("watchlist and watchlistName undefined");
            }
        }

        @NotNull
        public Long getCompanyid() {
            return companyid;
        }

        public void setCompanyid(Long companyid) {
            this.companyid = companyid;
        }


        @NotNull
        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public Long getWatchlistid() {
            return watchlistid;
        }

        public void setWatchlistid(Long watchlistid) {
            this.watchlistid = watchlistid;
        }

        @MmInternal
        public String getWatchlistName() {
            return watchlistName;
        }

        public void setWatchlistName(String watchlistName) {
            this.watchlistName = watchlistName;
        }

        @MmInternal
        public boolean isDistinctIid() {
            return distinctIid;
        }

        public void setDistinctIid(boolean distinctIid) {
            this.distinctIid = distinctIid;
        }
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public WlPositionAnlegen() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        if (quote == null) {
            ErrorUtils.rejectSymbol(cmd.getSymbol(), errors);
            return null;
        }

        final UserContext userContext = getUserContext(cmd);
        final User user = userContext.getUser();
        final Long watchlistId = getWatchlistId(user, cmd.getWatchlistName(), cmd.getWatchlistid());

        if (isToInsert(cmd, quote, watchlistId, cmd.isDistinctIid())) {
            final AddPositionCommand command = new AddPositionCommand();
            command.setUserid(user.getId());
            command.setPortfolioid(watchlistId);
            command.setQuote(quote);

            getUserProvider().insertPosition(command);
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("watchlistid", watchlistId);
        return new ModelAndView("wlpositionanlegen", model);
    }

    private boolean isToInsert(Command cmd, Quote quote, Long watchlistId,
            boolean distinctIid) {
        if (!distinctIid) {
            return true;
        }

        // need to get user again as getWatchlist might have added a new watchlist, which
        // would not be reflected in the user object above
        final UserContext userContext = getUserContext(cmd);
        final User user = userContext.getUser();
        final Portfolio watchlist = user.getWatchlist(watchlistId);
        for (final PortfolioPosition p : watchlist.getPositions()) {
            if (p.getIid() == quote.getInstrument().getId()) {
                return false;
            }
        }

        return true;
    }
}