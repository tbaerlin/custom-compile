/*
 * PfMusterdepotAnlegen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.merger.user.AddPortfolioCommand;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates a new portfolio for a given user.
 */

public class PfMusterdepotAnlegen extends UserHandler {
    public static final class Command extends UserCommandImpl {
        private String name = "Testdepot";
        private String currencyCode = "EUR";
        private BigDecimal cash = BigDecimal.ZERO;

        /**
         * @return Name of the new portfolio
         */
        @NotNull
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return Amount of money the user wants to have available in the new portfolio
         */
        public BigDecimal getCash() {
            return cash;
        }

        public void setCash(BigDecimal cash) {
            this.cash = cash;
        }

        public void setLiquiditaet(BigDecimal liquiditaet) {
            this.cash = liquiditaet;
        }

        /**
         * @return Portfolios currency. Every calculation over the portfolios positions is done in that currency.
         * So if there are positions in other currencys available, those values will be converted
         * into the portfolios currency to perform calculations with it (E.g. "currentValue" in PF_Evaluation)
         */
        @RestrictedSet("EUR,USD,GBP,CHF,JPY")
        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }
    }

    public PfMusterdepotAnlegen() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Long localUserId = getLocalUserId(cmd);

        final AddPortfolioCommand apc = new AddPortfolioCommand();
        apc.setUserid(localUserId);
        apc.setName(cmd.getName());
        apc.setWatchlist(false);
        apc.setCash(cmd.getCash());
        apc.setCurrencycode(cmd.getCurrencyCode());

        final Long portfolioid = getUserProvider().addPortfolio(apc);

        final Map model = new HashMap();
        model.put("portfolioid", portfolioid);
        return new ModelAndView("pfmusterdepotanlegen", model);
    }
}
