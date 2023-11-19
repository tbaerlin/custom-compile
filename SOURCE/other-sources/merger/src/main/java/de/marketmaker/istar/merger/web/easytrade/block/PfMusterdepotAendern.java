/*
 * PfMusterdepotAendern.java
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

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.provider.PortfolioEvaluationProvider;
import de.marketmaker.istar.merger.user.EvaluatedPortfolio;
import de.marketmaker.istar.merger.user.NoSuchPortfolioException;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.UpdatePortfolioCommand;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * Modify the attributes of an existing portfolio
 */
public class PfMusterdepotAendern extends UserHandler {
    private PortfolioEvaluationProvider evaluationProvider;

    public void setEvaluationProvider(PortfolioEvaluationProvider evaluationProvider) {
        this.evaluationProvider = evaluationProvider;
    }

    public static final class Command extends UserCommandImpl {
        private Long portfolioid;

        private String name;

        private BigDecimal liquiditaet = BigDecimal.ZERO;

        private BigDecimal initialInvestment;

        /**
         * @return Name of the portfolio
         */
        @NotNull
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return Unique portfolio id
         */
        @NotNull
        public Long getPortfolioid() {
            return portfolioid;
        }

        public void setPortfolioid(Long portfolioid) {
            this.portfolioid = portfolioid;
        }

        /**
         * @return Amount of money the user wants to have available in the portfolio
         */
        public BigDecimal getLiquiditaet() {
            return liquiditaet;
        }

        public void setLiquiditaet(BigDecimal liquiditaet) {
            this.liquiditaet = liquiditaet;
        }

        public void setCash(BigDecimal cash) {
            setLiquiditaet(cash);
        }

        /**
         * @return Amount of money the user had available in the portfolio when it had been created
         */
        public BigDecimal getInitialInvestment() {
            return initialInvestment;
        }

        public void setInitialInvestment(BigDecimal initialInvestment) {
            this.initialInvestment = initialInvestment;
        }
    }

    public PfMusterdepotAendern() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        if (cmd.getInitialInvestment() == null && cmd.getLiquiditaet() == null) {
            errors.reject("validator.notNull", "initialInvestment and cash are undefined");
            return null;
        }

        final Long localUserId = getLocalUserId(cmd);

        final UpdatePortfolioCommand uwc = new UpdatePortfolioCommand();
        uwc.setUserid(localUserId);
        uwc.setPortfolioid(cmd.getPortfolioid());
        uwc.setName(cmd.getName());
        uwc.setCash(getCashValue(cmd));

        getUserProvider().updatePortfolio(uwc);

        final Map<String, Object> model = new HashMap<>();
        model.put("portfolioid", cmd.getPortfolioid());
        return new ModelAndView("pfmusterdepotaendern", model);
    }

    private BigDecimal getCashValue(Command cmd) {
        if (cmd.getInitialInvestment() == null) {
            return cmd.getLiquiditaet();
        }

        final UserContext uc = getUserContext(cmd);
        final User user = uc.getUser();

        final Portfolio portfolio = user.getPortfolio(cmd.getPortfolioid());
        if (portfolio == null) {
            throw new NoSuchPortfolioException(cmd.getPortfolioid());
        }

        final EvaluatedPortfolio evaluatedPortfolio
                = this.evaluationProvider.evaluate(portfolio, true);

        final BigDecimal diff = cmd.getInitialInvestment().subtract(evaluatedPortfolio.getInitialInvestment());
        return portfolio.getCash().add(diff);
    }
}
