/*
 * MMPriceUpdate.java
 *
 * Created on 21.02.12 09:51
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mm;

import org.joda.time.LocalDate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author tkiesgen
 */
public class MMPriceUpdate implements Serializable {
    protected static final long serialVersionUID = 1L;

    private final String mmwkn;
    private final boolean fund;
    private final boolean cent;
    private final LocalDate date;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final BigDecimal kassa;
    private final BigDecimal volume;
    private final BigDecimal contracts;
    private final BigDecimal openinterest;

    public MMPriceUpdate(String mmwkn, boolean fund, boolean cent, LocalDate date, BigDecimal open,
            BigDecimal high,
            BigDecimal low, BigDecimal close, BigDecimal kassa, BigDecimal volume,
            BigDecimal contracts, BigDecimal openinterest) {
        if (mmwkn == null) {
            throw new IllegalArgumentException("mmwkn is null");
        }
        if (date == null) {
            throw new IllegalArgumentException("date is null");
        }
        this.mmwkn = mmwkn;
        this.fund = fund;
        this.cent = cent;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.kassa = kassa;
        this.volume = volume;
        this.contracts = contracts;
        this.openinterest = openinterest;
    }

    public String getMmwkn() {
        return mmwkn;
    }

    public boolean isFund() {
        return fund;
    }

    public boolean isCent() {
        return cent;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public BigDecimal getKassa() {
        return kassa;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public BigDecimal getContracts() {
        return contracts;
    }

    public BigDecimal getOpeninterest() {
        return openinterest;
    }
}
