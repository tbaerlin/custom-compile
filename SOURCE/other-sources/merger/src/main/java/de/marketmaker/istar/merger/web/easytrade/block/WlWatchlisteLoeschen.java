/*
 * WlWatchlisteLoeschen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 *
  * @author Oliver Flege
  * @author Thomas Kiesgen
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.merger.user.RemovePortfolioCommand;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;

/**
 * Deletes the given watchlist
 */
public class WlWatchlisteLoeschen extends UserHandler {
    @SuppressWarnings("UnusedDeclaration")
    public static final class Command extends UserCommandImpl implements InitializingBean {
        private Long watchlistid;

        private String watchlistName;

        @Override
        public void afterPropertiesSet() throws Exception {
            if (this.watchlistid == null && this.watchlistName == null) {
                throw new BadRequestException("watchlist and watchlistName undefined");
            }
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
    }

    public WlWatchlisteLoeschen() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final UserContext userContext = getUserContext(cmd);
        final User user = userContext.getUser();
        final Long watchlistId = getWatchlistId(user, cmd.getWatchlistName(), cmd.getWatchlistid());

        final RemovePortfolioCommand rwc = new RemovePortfolioCommand();
        rwc.setUserid(user.getId());
        rwc.setPortfolioid(watchlistId);

        getUserProvider().removePortfolio(rwc);

        final Map<String, Object> model = new HashMap<>();
        model.put("watchlistid", watchlistId);
        return new ModelAndView("wlwatchlisteloeschen", model);
    }
}
