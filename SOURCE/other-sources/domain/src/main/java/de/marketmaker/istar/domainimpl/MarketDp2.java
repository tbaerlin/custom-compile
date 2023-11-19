/*
 * CurrencyDp2.java
 *
 * Created on 20.12.2004 11:02:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.MarketcategoryEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MarketDp2 extends ItemWithNamesDp2 implements Market, Serializable {
    static final long serialVersionUID = -11L;

    private String name;

    private Country country;

    private MarketcategoryEnum marketcategory;

    public MarketDp2() {
    }

    public MarketDp2(long id, String name) {
        super(id);
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Country getCountry() {
        return country;
    }

    public String getSymbolDpTeam() {
        return getSymbol(KeysystemEnum.DP_TEAM);
    }

    public String getSymbolIso() {
        return getSymbol(KeysystemEnum.ISO);
    }

    public String getSymbolMm() {
        return getSymbol(KeysystemEnum.MM);
    }

    public String getSymbolVwdfeed() {
        return getSymbol(KeysystemEnum.VWDFEED);
    }

    public String getSymbolWm() {
        return getSymbol(KeysystemEnum.WM);
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public MarketcategoryEnum getMarketcategory() {
        return marketcategory;
    }

    public void setMarketcategory(MarketcategoryEnum marketcategory) {
        this.marketcategory = marketcategory;
    }

    @Override
    public String getSymbolMicSegment() {
        return getSymbol(KeysystemEnum.MIC);
    }

    @Override
    public String getSymbolMicOperating() {
        return getSymbol(KeysystemEnum.OPERATING_MIC);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
