/*
 * StockAnalysisSummaryImpl.java
 *
 * Created on 10.08.2006 16:15:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.StockAnalysisSummary;

import java.io.Serializable;
import java.math.BigDecimal;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StockAnalysisSummaryImpl implements Serializable, StockAnalysisSummary {
    protected static final long serialVersionUID = 1L;

    private final DateTime date;
    private final int numberOfBuys;
    private final int numberOfStrongBuys;
    private final int numberOfHolds;
    private final int numberOfSells;
    private final int numberOfStrongSells;

    public StockAnalysisSummaryImpl(DateTime date, int numberOfBuys, int numberOfStrongBuys, int numberOfHolds, int numberOfSells, int numberOfStrongSells) {
        this.date = date;
        this.numberOfBuys = numberOfBuys;
        this.numberOfStrongBuys = numberOfStrongBuys;
        this.numberOfHolds = numberOfHolds;
        this.numberOfSells = numberOfSells;
        this.numberOfStrongSells = numberOfStrongSells;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public DateTime getDate() {
        return date;
    }

    public int getNumberOfBuys() {
        return numberOfBuys;
    }

    public int getNumberOfStrongBuys() {
        return numberOfStrongBuys;
    }

    public int getNumberOfHolds() {
        return numberOfHolds;
    }

    public int getNumberOfSells() {
        return numberOfSells;
    }

    public int getNumberOfStrongSells() {
        return numberOfStrongSells;
    }

    public int getNumberOfAnalyses() {
        return this.numberOfBuys + this.numberOfStrongBuys + this.numberOfHolds
                + this.numberOfSells + this.numberOfStrongSells;
    }

    public BigDecimal getNormalizedRecommendation(boolean strongEqualsNormal) {
        final int numberOfAnalyses = getNumberOfAnalyses();
        if(numberOfAnalyses==0) {
            return null;
        }

        final double sum = this.numberOfStrongBuys * (strongEqualsNormal ? 1 : 2)
                + this.numberOfBuys
                - this.numberOfSells
                - this.numberOfStrongSells * (strongEqualsNormal ? 1 : 2);

        return BigDecimal.valueOf(sum/numberOfAnalyses);
    }
}
