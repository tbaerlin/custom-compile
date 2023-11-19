/*
 * InsertPositionCommand.java
 *
 * Created on 04.08.2006 10:28:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AddPositionCommand {
    private Long userid;
    private Long portfolioid;
    private Quote quote;

    public Quote getQuote() {
        return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public Long getPortfolioid() {
        return portfolioid;
    }

    public void setPortfolioid(Long portfolioid) {
        this.portfolioid = portfolioid;
    }

    public Long getInstrumentid() {
        return this.quote.getInstrument().getId();
    }

    public Long getQuoteid() {
        return this.quote.getId();
    }

    public InstrumentTypeEnum getInstrumentType() {
        return this.quote.getInstrument().getInstrumentType();
    }

    public boolean isQuotedPerPercent() {
        return this.quote.getMinimumQuotationSize().isUnitPercent();
    }
}
