/*
 * Position.java
 *
 * Created on 05.08.15 17:27
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author tkiesgen
 */
public class Position implements Serializable {
    static final long serialVersionUID = 1L;

    private final String symbol;

    private final SymbolQuote quote;

    private final BigDecimal quantity;

    private final BigDecimal purchasePrice;

    public Position(String symbol, SymbolQuote quote, BigDecimal quantity,
            BigDecimal buyingRate) {
        this.symbol = symbol;
        this.quote = quote;
        this.quantity = quantity;
        this.purchasePrice = buyingRate;
    }

    public String getSymbol() {
        return symbol;
    }

    public SymbolQuote getQuote() {
        return quote;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }
}
