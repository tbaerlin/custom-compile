/*
 * PfMusterdepotLoeschen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */

package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.user.RemovePortfolioCommand;

/**
 * Deletes a portfolio
 */

public class PfMusterdepotLoeschen extends UserHandler {
    public static final class Command extends UserCommandImpl {
        private Long portfolioid;

        @NotNull
        public Long getPortfolioid() {
            return portfolioid;
        }

        public void setPortfolioid(Long portfolioid) {
            this.portfolioid = portfolioid;
        }
    }


    public PfMusterdepotLoeschen() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;
        final Long localUserId = getLocalUserId(cmd);

        final RemovePortfolioCommand rwc = new RemovePortfolioCommand();
        rwc.setUserid(localUserId);
        rwc.setPortfolioid(cmd.getPortfolioid());

        getUserProvider().removePortfolio(rwc);

        final Map model = new HashMap();
        model.put("portfolioid", cmd.getPortfolioid());
        return new ModelAndView("pfmusterdepotloeschen", model);
    }
}
