/*
 * AnnualReportDataImpl.java
 *
 * Created on 17.03.2010 15:56:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;

import net.jcip.annotations.Immutable;
import org.joda.time.Interval;

import de.marketmaker.istar.domain.data.AnnualReportAssets;
import de.marketmaker.istar.domain.data.AnnualReportBalanceSheet;
import de.marketmaker.istar.domain.data.AnnualReportData;
import de.marketmaker.istar.domain.data.AnnualReportKeyFigures;
import de.marketmaker.istar.domain.data.AnnualReportLiabilities;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class AnnualReportDataImpl implements AnnualReportData, Serializable {
    protected static final long serialVersionUID = 1L;

    private final long instrumentid;
    private final Interval reference;
    private final AnnualReportAssets assets;
    private final AnnualReportLiabilities liabilities;
    private final AnnualReportBalanceSheet balanceSheet;
    private final AnnualReportKeyFigures keyFigures;

    public AnnualReportDataImpl(long instrumentid, Interval reference, AnnualReportAssets assets,
            AnnualReportLiabilities liabilities, AnnualReportBalanceSheet balanceSheet,
            AnnualReportKeyFigures keyFigures) {
        this.instrumentid = instrumentid;
        this.reference = reference;
        this.assets = assets;
        this.liabilities = liabilities;
        this.balanceSheet = balanceSheet;
        this.keyFigures = keyFigures;
    }

    public long getInstrumentid() {
        return instrumentid;
    }

    public Interval getReference() {
        return reference;
    }

    public AnnualReportAssets getAssets() {
        return assets;
    }

    public AnnualReportLiabilities getLiabilities() {
        return liabilities;
    }

    public AnnualReportBalanceSheet getBalanceSheet() {
        return balanceSheet;
    }

    public AnnualReportKeyFigures getKeyFigures() {
        return keyFigures;
    }

    @Override
    public String toString() {
        return "AnnualReportDataImpl{" +
                "instrumentid=" + instrumentid +
                ", reference=" + reference +
                ", assets=" + assets +
                ", liabilities=" + liabilities +
                ", balanceSheet=" + balanceSheet +
                ", keyFigures=" + keyFigures +
                '}';
    }
}
