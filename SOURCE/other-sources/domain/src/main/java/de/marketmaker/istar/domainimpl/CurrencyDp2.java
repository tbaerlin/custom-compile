/*
 * CurrencyDp2.java
 *
 * Created on 20.12.2004 11:02:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.KeysystemEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CurrencyDp2 extends ItemWithNamesDp2 implements Currency, Serializable {
    static final long serialVersionUID = -8L;

    private static final Map<String, String> CENTS = new HashMap<>();

    static {
        CENTS.put("GBX", "GBP");
        CENTS.put("ZAX", "ZAR");
        CENTS.put("USX", "USD");
    }

    public static boolean isCent(String symbol) {
        return CENTS.containsKey(symbol);
    }

    public static String getBaseCurrencyIso(String symbol) {
        final String base = CENTS.get(symbol);
        return (base != null) ? base : symbol;
    }

    private String name;

    public CurrencyDp2() {
    }

    public CurrencyDp2(long id, String name) {
        super(id);

        this.name = name;
    }

    public boolean isCent() {
        return isCent(getSymbolIso());
    }

    public String getBaseCurrencyIso() {
        return getBaseCurrencyIso(getSymbolIso());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getSymbolIso() {
        return getSymbol(KeysystemEnum.ISO);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
