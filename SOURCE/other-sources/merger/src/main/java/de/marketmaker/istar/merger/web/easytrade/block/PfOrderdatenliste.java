/*
 * PfOrderdatenliste.java
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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.user.NoSuchPortfolioException;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.PortfolioPosition;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;

/**
 * Lists all orders of a given portfolio
 */
public class PfOrderdatenliste extends UserHandler {
    public static class Command extends UserCommandImpl {
        private Long portfolioid;

        /**
         * @return the portfolio id
         */
        @NotNull
        public Long getPortfolioid() {
            return portfolioid;
        }

        public void setPortfolioid(Long portfolioid) {
            this.portfolioid = portfolioid;
        }
    }

    public PfOrderdatenliste() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;

        final UserContext uc = getUserContext(cmd);
        final User user = uc.getUser();
        final Portfolio portfolio = user.getPortfolio(cmd.getPortfolioid());
        if (portfolio == null) {
            throw new NoSuchPortfolioException(cmd.getPortfolioid());
        }

        final Map model = new HashMap();
        final List<PortfolioPosition> positions = portfolio.getPositions();
        model.put("positions", positions);
        return new ModelAndView("pforderdatenliste", model);
    }
}
