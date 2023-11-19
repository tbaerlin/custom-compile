/*
 * PortfolioVaRLightRequest.java
 *
 * Created on 07.09.2010 17:07:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortfolioVaRLightRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private final LocalDate date;

    private final String currency;

    private final List<Position> positions = new ArrayList<>();

    public PortfolioVaRLightRequest(LocalDate date, String currency) {
        this.date = date;
        this.currency = currency;
    }

    public void addPosition(String symbol, Quote quote, BigDecimal quantity) {
        addPosition(symbol, quote, quantity, null);
    }

    public void addPosition(String symbol, Quote quote, BigDecimal quantity, BigDecimal purchasePrice) {
        this.positions.add(new Position(symbol, SymbolQuote.create(quote), quantity, purchasePrice));
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCurrency() {
        return currency;
    }

    public List<Position> getPositions() {
        return positions;
    }

}
