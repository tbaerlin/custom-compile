/*
 * BondRatiosImpl.java
 *
 * Created on 28.07.2006 07:45:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.math.BigDecimal;
import java.io.Serializable;

import de.marketmaker.istar.domain.data.WarrantRatios;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WarrantRatiosImpl implements Serializable, WarrantRatios {
    protected static final long serialVersionUID = 1L;

    private final BigDecimal leverage;
    private final BigDecimal contango;
    private final BigDecimal contangoPerYear;
    private final BigDecimal intrinsicValue;
    private final BigDecimal extrinsicValue;
    private final BigDecimal breakeven;
    private final BigDecimal impliedVolatility;
    private final BigDecimal delta;
    private final BigDecimal fairPrice;
    private final BigDecimal parity;
    private final BigDecimal omega;
    private final BigDecimal gamma;
    private final BigDecimal vega;
    private final BigDecimal rho;
    private final BigDecimal moneyness;
    private final BigDecimal moneynessRelative;
    private final BigDecimal theta;
    private final BigDecimal thetaRelative;
    private final BigDecimal theta1w;
    private final BigDecimal theta1wRelative;
    private final BigDecimal theta1m;
    private final BigDecimal theta1mRelative;

    public WarrantRatiosImpl(BigDecimal leverage, BigDecimal contango, BigDecimal contangoPerYear,
                             BigDecimal intrinsicValue, BigDecimal extrinsicValue, BigDecimal breakeven,
                             BigDecimal impliedVolatility, BigDecimal delta, BigDecimal fairPrice,
                             BigDecimal parity, BigDecimal omega, BigDecimal gamma, BigDecimal vega,
                             BigDecimal rho, BigDecimal moneyness, BigDecimal moneynessRelative,
                             BigDecimal theta, BigDecimal thetaRelative,
                             BigDecimal theta1w, BigDecimal theta1wRelative,
                             BigDecimal theta1m, BigDecimal theta1mRelative) {
        this.leverage = leverage;
        this.contango = contango;
        this.contangoPerYear = contangoPerYear;
        this.intrinsicValue = intrinsicValue;
        this.extrinsicValue = extrinsicValue;
        this.breakeven = breakeven;
        this.impliedVolatility = impliedVolatility;
        this.delta = delta;
        this.fairPrice = fairPrice;
        this.parity = parity;
        this.omega = omega;
        this.gamma = gamma;
        this.vega = vega;
        this.rho = rho;
        this.moneyness = moneyness;
        this.moneynessRelative = moneynessRelative;
        this.theta = theta;
        this.thetaRelative = thetaRelative;
        this.theta1w = theta1w;
        this.theta1wRelative = theta1wRelative;
        this.theta1m = theta1m;
        this.theta1mRelative = theta1mRelative;
    }

    public BigDecimal getLeverage() {
        return leverage;
    }

    public BigDecimal getContango() {
        return contango;
    }

    public BigDecimal getContangoPerYear() {
        return contangoPerYear;
    }

    public BigDecimal getIntrinsicValue() {
        return intrinsicValue;
    }

    public BigDecimal getExtrinsicValue() {
        return extrinsicValue;
    }

    public BigDecimal getBreakeven() {
        return breakeven;
    }

    public BigDecimal getImpliedVolatility() {
        return impliedVolatility;
    }

    public BigDecimal getDelta() {
        return delta;
    }

    public BigDecimal getFairPrice() {
        return fairPrice;
    }

    public BigDecimal getParity() {
        return parity;
    }

    public BigDecimal getOmega() {
        return omega;
    }

    public BigDecimal getGamma() {
        return gamma;
    }

    public BigDecimal getVega() {
        return vega;
    }

    public BigDecimal getRho() {
        return rho;
    }

    public BigDecimal getMoneyness() {
        return moneyness;
    }

    public BigDecimal getMoneynessRelative() {
        return moneynessRelative;
    }

    public BigDecimal getTheta() {
        return theta;
    }

    public BigDecimal getThetaRelative() {
        return thetaRelative;
    }

    public BigDecimal getTheta1w() {
        return theta1w;
    }

    public BigDecimal getTheta1wRelative() {
        return theta1wRelative;
    }

    public BigDecimal getTheta1m() {
        return theta1m;
    }

    public BigDecimal getTheta1mRelative() {
        return theta1mRelative;
    }

    public String toString() {
        return "WarrantRatiosImpl[leverage=" + leverage
                + ", contango=" + contango
                + ", contangoPerYear=" + contangoPerYear
                + ", intrinsicValue=" + intrinsicValue
                + ", extrinsicValue=" + extrinsicValue
                + ", breakeven=" + breakeven
                + ", impliedVolatility=" + impliedVolatility
                + ", delta=" + delta
                + ", fairPrice=" + fairPrice
                + ", parity=" + parity
                + ", omega=" + omega
                + ", gamma=" + gamma
                + ", vega=" + vega
                + ", rho=" + rho
                + ", moneyness=" + moneyness
                + ", moneynessRelative=" + moneynessRelative
                + ", theta=" + theta
                + ", thetaRelative=" + thetaRelative
                + ", theta1w=" + theta1w
                + ", theta1wRelative=" + theta1wRelative
                + ", theta1m=" + theta1m
                + ", theta1mRelative=" + theta1mRelative
                + "]";
    }
}
