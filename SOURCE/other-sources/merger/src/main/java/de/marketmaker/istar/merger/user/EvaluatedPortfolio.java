/*
 * EvaluatedPortfolio.java
 *
 * Created on 07.08.2006 15:52:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;
import de.marketmaker.istar.domainimpl.data.ZeroPriceRecord;
import de.marketmaker.istar.merger.Constants;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EvaluatedPortfolio {
    private static final Logger logger = LoggerFactory.getLogger(EvaluatedPortfolio.class);

    private final List<EvaluatedPosition> positions;

    private final Portfolio portfolio;

    public EvaluatedPortfolio(Portfolio portfolio, Map<Long, Quote> quoteById,
                              Map<Long, PriceRecord> priceById,
                              Map<String, BigDecimal> exchangerateByCode) {
        this.portfolio = portfolio;
        final List<PortfolioPosition> positions = portfolio.getPositions();
        this.positions = new ArrayList<>(positions.size());

        for (PortfolioPosition pp : positions) {
            final long qid = pp.getQid();
            final Quote quote = quoteById.get(qid);
            if (quote == null) {
                // TODO: can we do any better?
                continue;
            }

            final BigDecimal exchangerate = getExchangeRate(quote, portfolio, exchangerateByCode);
            final PriceRecord price = getPrice(quote, priceById);

            final EvaluatedPosition ep = new SingleEvaluatedPosition(pp, quote, price, exchangerate);
            this.positions.add(ep);
        }
    }

    private PriceRecord getPrice(Quote quote, Map<Long, PriceRecord> priceById) {
        final PriceRecord result = priceById.get(quote.getId());
        if (result == null || result instanceof NullPriceRecord) {
            return ZeroPriceRecord.INSTANCE;
        }
        return result;
    }

    private BigDecimal getExchangeRate(Quote quote, Portfolio portfolio,
            Map<String, BigDecimal> exchangerateByCode) {
        final String currencyCode = quote.getCurrency().getSymbolIso();
        if (currencyCode == null) {
            logger.warn("<init> no IsoCurrencyCode for " + quote.getId() + ".qid");
            return BigDecimal.ONE;
        }
        if (currencyCode.equals(portfolio.getCurrencyCode())) {
            return BigDecimal.ONE;
        }
        final BigDecimal result = exchangerateByCode.get(currencyCode);
        if (result == null) {
            // TODO: can we indicate somehow that the exchangerate is missing?
            // TODO: would return BigDecimal.ZERO work?
            return BigDecimal.ONE;
        }
        return result;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public List<EvaluatedPosition> getPositions() {
        return new ArrayList<>(this.positions);
    }

    public List<EvaluatedPosition> getNonEmptyPositions() {
        final List<EvaluatedPosition> result = new ArrayList<>(this.positions.size());

        for (EvaluatedPosition position : this.positions) {
            if (position.getTotalVolume().compareTo(BigDecimal.ZERO) != 0) {
                result.add(position);
            }
        }
        return result;
    }

    public BigDecimal getCash() {
        return this.portfolio.getCash();
    }

    public BigDecimal getInitialInvestment() {
        return this.portfolio.getCash().add(getOrderValue()).subtract(getRealizedGainNet());
    }

    public BigDecimal getCurrentValue() {
        BigDecimal result = BigDecimal.ZERO;
        for (EvaluatedPosition ep : positions) {
            result = result.add(ep.getCurrentValueInPortfolioCurrency());
        }
        return result;
    }

    public BigDecimal getOrderValue() {
        BigDecimal result = BigDecimal.ZERO;
        for (EvaluatedPosition ep : positions) {
            result = result.add(ep.getOrderValueInPortfolioCurrency());
        }
        return result;
    }

    public BigDecimal getPreviousCloseValue() {
        BigDecimal result = BigDecimal.ZERO;
        for (EvaluatedPosition ep : positions) {
            result = result.add(ep.getPreviousCloseValueInPortfolioCurrency());
        }
        return result;
    }

    public BigDecimal getChangeNet() {
        final BigDecimal currentValue = getCurrentValue();
        final BigDecimal orderValue = getOrderValue();
        return currentValue.subtract(orderValue, Constants.MC);
    }

    public BigDecimal getChangePercent() {
        final BigDecimal orderValue = getOrderValue();
        if (orderValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getChangeNet().divide(orderValue, Constants.MC);
    }

    public BigDecimal getDailyChangeNet() {
        final BigDecimal currentValue = getCurrentValue();
        final BigDecimal previousCloseValue = getPreviousCloseValue();
        return currentValue.subtract(previousCloseValue, Constants.MC);
    }

    public BigDecimal getDailyChangePercent() {
        final BigDecimal previousCloseValue = getPreviousCloseValue();
        if (previousCloseValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getDailyChangeNet().divide(previousCloseValue, Constants.MC);
    }

    /**
     * @return sum of realized costs for all positions (in portfolio currency)
     * @see EvaluatedPosition#getRealizedCost()
     */
    public BigDecimal getRealizedCost() {
        BigDecimal result = BigDecimal.ZERO;
        for (EvaluatedPosition ep : positions) {
            result = result.add(ep.getRealizedCost());
        }
        return result;
    }

    /**
     * @return sum of realized gain for all positions (in portfolio currency)
     * @see EvaluatedPosition#getRealizedGain()
     */
    public BigDecimal getRealizedGain() {
        BigDecimal result = BigDecimal.ZERO;
        for (EvaluatedPosition ep : positions) {
            result = result.add(ep.getRealizedGain());
        }
        return result;
    }

    /**
     * @return realized gain minus realized costs
     */
    public BigDecimal getRealizedGainNet() {
        final BigDecimal gain = getRealizedGain();
        final BigDecimal cost = getRealizedCost();
        return gain.subtract(cost, Constants.MC);
    }

    /**
     * @return net realized gain in percent
     */
    public BigDecimal getRealizedGainPercent() {
        final BigDecimal cost = getRealizedCost();
        if (cost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getRealizedGainNet().divide(cost, Constants.MC);
    }

    public String getPortfolioCurrency() {
        return this.portfolio.getCurrencyCode();
    }
}
