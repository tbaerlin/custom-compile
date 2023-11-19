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
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.KeysystemEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CountryDp2 extends ItemWithNamesDp2 implements Country, Serializable {
    static final long serialVersionUID = -7L;

    private String name;

    private Currency currency;

    public CountryDp2() {
    }

    public CountryDp2(long id, String name) {
        super(id);

        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getSymbolIso() {
        return getSymbol(KeysystemEnum.ISO);
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
