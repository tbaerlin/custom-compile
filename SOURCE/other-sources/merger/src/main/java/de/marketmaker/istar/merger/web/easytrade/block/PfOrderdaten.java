/*
 * PfOrderdaten.java
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
import de.marketmaker.istar.merger.user.NoSuchOrderException;
import de.marketmaker.istar.merger.user.NoSuchPortfolioException;
import de.marketmaker.istar.merger.user.Order;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.PortfolioPosition;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;

/**
 *
 * Returns some detail information about an order.
 * E.g. the price of a buy order
 *
 */
public class PfOrderdaten extends UserHandler {
    public static class Command extends UserCommandImpl {
        private Long portfolioid;
        private Long orderid;

        @NotNull
        public Long getPortfolioid() {
            return portfolioid;
        }

        public void setPortfolioid(Long portfolioid) {
            this.portfolioid = portfolioid;
        }

        @NotNull
        public Long getOrderid() {
            return orderid;
        }

        public void setOrderid(Long orderid) {
            this.orderid = orderid;
        }
    }

    public PfOrderdaten() {
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
        final Order order = portfolio.getOrder(cmd.getOrderid());
        if (order == null) {
            throw new NoSuchOrderException("Invalid orderid: " + cmd.getOrderid(), cmd.getOrderid());
        }

        final PortfolioPosition position = portfolio.getPositionWithOrder(order.getId());

        final Map<String,Object> model = new HashMap<>();
        model.put("order", order);
        model.put("position", position);
        return new ModelAndView("pforderdaten", model);
    }
}