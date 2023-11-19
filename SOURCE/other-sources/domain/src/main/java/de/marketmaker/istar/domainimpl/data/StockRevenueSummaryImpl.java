/*
 * BasicBalanceFiguresImpl.java
 *
 * Created on 09.08.2006 12:13:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.marketmaker.istar.domain.data.StockRevenueSummary;
import lombok.ToString;
import net.jcip.annotations.Immutable;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
@ToString
public class StockRevenueSummaryImpl implements StockRevenueSummary, Serializable {
    protected static final long serialVersionUID = 2L;

    public static final Field EMPTY_FIELD = Field.create(null, null, null, null, null);

    private static final MathContext MC = new MathContext(7, RoundingMode.HALF_UP);


    private final Interval reference;

    private final Integer fiscalYear;

    private final String currency;

    private final List<String> brokerNames;

    private final LocalDate referenceDate;

    private final BigDecimal price;

    private final BigDecimal recommendation;

    private final Integer numBuy;

    private final Integer numOverweight;

    private final Integer numHold;

    private final Integer numUnderweight;

    private final Integer numSell;

    private final Integer numTotal;

    private final Field earningPerShare;

    private final Field priceEarningRatio;

    private final Field cashflow;

    private final Field dividend;

    private final Field dividendYield;

    private final Field netProfit;

    private final Field preTaxProfit;

    private final Field earningPerShareAfterGoodwill;

    private final Field earningPerShareBeforeGoodwill;

    private final Field longTermGrowth;

    private final Field bookValue;

    private final Field bookValueAdjusted;

    private final Field ebit;

    private final Field ebitda;

    private final Field goodwill;

    private final Field netDebt;

    private final Field priceTarget;

    private final Field sales;

    private final Field postEventConsensus;

    private final Field epsStdDev;

    private final Field naps;

    private final Field epsGrowth;

    private final Field numberOfShares;

    private final DateTime dmlDate;

    private final Field freeCashFlow;

    private final Field returnOnEquity;

    private final Field returnOnInvestedCapital;

    public StockRevenueSummaryImpl(Interval reference, String currency, Integer fiscalYear,
            BigDecimal price, BigDecimal recommendation,
            Integer numBuy, Integer numOverweight, Integer numHold,
            Integer numUnderweight, Integer numSell, Integer numTotal,
            List<String> brokerNames, LocalDate referenceDate, DateTime dmlDate,
            Field earningPerShare,   // 15
            Field priceEarningRatio,
            Field cashflow, Field dividend, Field dividendYield,
            Field netProfit, Field preTaxProfit, Field earningPerShareAfterGoodwill,
            Field earningPerShareBeforeGoodwill, Field longTermGrowth,
            Field bookValue, Field bookValueAdjusted, Field ebit,
            Field ebitda, Field goodwill, Field netDebt,
            Field priceTarget, Field sales, Field postEventConsensus,
            Field epsStdDev, Field naps, Field epsGrowth, Field numberOfShares,
            Field freeCashFlow, Field returnOnEquity, Field returnOnInvestedCapital) {
        this.reference = reference;
        this.currency = currency;
        this.price = price;
        this.recommendation = recommendation;
        this.numBuy = numBuy;
        this.numOverweight = numOverweight;
        this.numHold = numHold;
        this.numUnderweight = numUnderweight;
        this.numSell = numSell;
        this.numTotal = numTotal;
        this.brokerNames = brokerNames;
        this.referenceDate = referenceDate;
        this.dmlDate = dmlDate;
        this.fiscalYear = fiscalYear;
        this.earningPerShare = earningPerShare;
        this.priceEarningRatio = priceEarningRatio;
        this.cashflow = cashflow;
        this.dividend = dividend;
        this.dividendYield = dividendYield;
        this.netProfit = netProfit;
        this.preTaxProfit = preTaxProfit;
        this.earningPerShareAfterGoodwill = earningPerShareAfterGoodwill;
        this.earningPerShareBeforeGoodwill = earningPerShareBeforeGoodwill;
        this.longTermGrowth = longTermGrowth;
        this.bookValue = bookValue;
        this.bookValueAdjusted = bookValueAdjusted;
        this.ebit = ebit;
        this.ebitda = ebitda;
        this.goodwill = goodwill;
        this.netDebt = netDebt;
        this.priceTarget = priceTarget;
        this.sales = sales;
        this.postEventConsensus = postEventConsensus;
        this.epsStdDev = epsStdDev;
        this.naps = naps;
        if (longTermGrowth != null) {
            this.epsGrowth = new Field(  // HACK! see: CORE-13559
                    longTermGrowth.value != null
                            ? longTermGrowth.value.divide(new BigDecimal("100"), MC)
                            : null
                    ,
                    longTermGrowth.numberOfAnalysts,
                    longTermGrowth.upRevisions,
                    longTermGrowth.downRevisions,
                    longTermGrowth.unchangedRevisions);
        } else {
            this.epsGrowth = null;
        }
        this.numberOfShares = numberOfShares;
        this.freeCashFlow = freeCashFlow;
        this.returnOnEquity = returnOnEquity;
        this.returnOnInvestedCapital = returnOnInvestedCapital;
    }

    public Interval getReference() {
        return reference;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getFiscalYear() {
        return fiscalYear;
    }

    public List<String> getBrokerNames() {
        return brokerNames;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public DateTime getDmlDate() {
        return dmlDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getRecommendation() {
        return recommendation;
    }

    public Integer getNumBuy() {
        return numBuy;
    }

    public Integer getNumOverweight() {
        return numOverweight;
    }

    public Integer getNumHold() {
        return numHold;
    }

    public Integer getNumUnderweight() {
        return numUnderweight;
    }

    public Integer getNumSell() {
        return numSell;
    }

    public Integer getNumTotal() {
        return numTotal;
    }

    public Field getEarningPerShare() {
        return earningPerShare;
    }

    public Field getPriceEarningRatio() {
        return priceEarningRatio;
    }

    public Field getCashflow() {
        return cashflow;
    }

    public Field getDividend() {
        return dividend;
    }

    public Field getDividendYield() {
        return dividendYield;
    }

    public Field getNetProfit() {
        return netProfit;
    }

    public Field getPreTaxProfit() {
        return preTaxProfit;
    }

    public Field getEarningPerShareAfterGoodwill() {
        return earningPerShareAfterGoodwill;
    }

    public Field getEarningPerShareBeforeGoodwill() {
        return earningPerShareBeforeGoodwill;
    }

    public Field getLongTermGrowth() {
        return longTermGrowth;
    }

    public Field getBookValue() {
        return bookValue;
    }

    public Field getBookValueAdjusted() {
        return bookValueAdjusted;
    }

    public Field getEBIT() {
        return ebit;
    }

    public Field getEBITDA() {
        return ebitda;
    }

    public Field getGoodwill() {
        return goodwill;
    }

    public Field getNetDebt() {
        return netDebt;
    }

    public Field getPriceTarget() {
        return priceTarget;
    }

    public Field getSales() {
        return sales;
    }

    public Field getPostEventConsensus() {
        return postEventConsensus;
    }

    public Field getEpsStdDev() {
        return epsStdDev;
    }

    public Field getNaps() {
        return naps;
    }

    public Field getEpsGrowth() {
        return epsGrowth;
    }

    @Override
    public Field getNumberOfShares() {
        return numberOfShares;
    }

    @Override
    public Field getFreeCashFlow() {
        return freeCashFlow;
    }

    @Override
    public Field getReturnOnEquity() {
        return returnOnEquity;
    }

    @Override
    public Field getReturnOnInvestedCapital() {
        return returnOnInvestedCapital;
    }

    @Immutable
    public static class Field implements Serializable {
        protected static final long serialVersionUID = 1L;

        private static ConcurrentMap<Integer, Integer> cache
                = new ConcurrentHashMap<>();

        private final BigDecimal value;

        private final Integer numberOfAnalysts;

        private final Integer upRevisions;

        private final Integer downRevisions;

        private final Integer unchangedRevisions;

        private Field(BigDecimal value, Integer numberOfAnalysts, Integer upRevisions,
                Integer downRevisions, Integer unchangedRevisions) {
            this.value = value;
            this.numberOfAnalysts = numberOfAnalysts;
            this.upRevisions = upRevisions;
            this.downRevisions = downRevisions;
            this.unchangedRevisions = unchangedRevisions;
        }

        public static Field create(BigDecimal value, Integer numberOfAnalysts,
                Integer upRevisions, Integer downRevisions, Integer unchangedRevisions) {
            return new Field(value, intern(numberOfAnalysts), intern(upRevisions),
                    intern(downRevisions), intern(unchangedRevisions));
        }

        private static Integer intern(Integer x) {
            if (x == null) {
                return x;
            }
            final Integer existing = cache.putIfAbsent(x, x);
            return (existing != null) ? existing : x;
        }

        public BigDecimal getValue() {
            return value;
        }

        public Integer getNumberOfAnalysts() {
            return numberOfAnalysts;
        }

        public Integer getUpRevisions() {
            return upRevisions;
        }

        public Integer getDownRevisions() {
            return downRevisions;
        }

        public Integer getUnchangedRevisions() {
            return unchangedRevisions;
        }

        public String toString() {
            return "Field[value=" + value
                    + ", #:" + numberOfAnalysts
                    + ", +:" + upRevisions
                    + ", -:" + downRevisions
                    + ", =:" + unchangedRevisions
                    + "]";
        }
    }
}
