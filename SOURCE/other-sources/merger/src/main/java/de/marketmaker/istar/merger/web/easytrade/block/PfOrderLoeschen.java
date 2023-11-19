/*
 * PfOrderLoeschen.java
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

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.user.RemoveOrderCommand;

/**
 * Delete an existing order
 */
public class PfOrderLoeschen extends UserHandler {
    public static class Command extends UserCommandImpl {
        private Long portfolioid;
        private Long orderid;

        /**
         * @return identifies the affected portfolio
         */
        @NotNull
        public Long getPortfolioid() {
            return portfolioid;
        }

        public void setPortfolioid(Long portfolioid) {
            this.portfolioid = portfolioid;
        }

        /**
         * @return identifies the order to delete
         */
        @NotNull
        public Long getOrderid() {
            return orderid;
        }

        public void setOrderid(Long orderid) {
            this.orderid = orderid;
        }
    }

    public PfOrderLoeschen() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Long localUserId = getLocalUserId(cmd);

        final RemoveOrderCommand roc = new RemoveOrderCommand();
        roc.setUserid(localUserId);
        roc.setPortfolioid(cmd.getPortfolioid());
        roc.setOrderid(cmd.getOrderid());

        getUserProvider().removeOrder(roc);

        final Map<String, Object> model = new HashMap<>();
        model.put("portfolioid", cmd.getPortfolioid());
        return new ModelAndView("pforderloeschen", model);
    }
}
