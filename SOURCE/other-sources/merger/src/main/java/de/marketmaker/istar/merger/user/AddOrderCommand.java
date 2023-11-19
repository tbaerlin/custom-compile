/*
 * AddOrderCommand.java
 *
 * Created on 07.08.2006 15:16:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.math.BigDecimal;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AddOrderCommand {
    private Long userid;
    private Long portfolioid;
    private Quote quote;
    private BigDecimal charge = BigDecimal.ZERO;
    private BigDecimal exchangeRate = BigDecimal.ONE;
    private BigDecimal volume;
    private BigDecimal price;
    private DateTime date;
    private boolean buy;
    private boolean onlyWithPosition;

    public Quote getQuote() {
        return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public boolean isQuotedPerPercent() {
        return this.quote.getMinimumQuotationSize().isUnitPercent();
    }

    public InstrumentTypeEnum getInstrumentType() {
        return this.quote.getInstrument().getInstrumentType();
    }

    public Long getIid() {
        return this.quote.getInstrument().getId();
    }

    public Long getQid() {
        return this.quote.getId();
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
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

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public boolean isBuy() {
        return buy;
    }

    public void setBuy(boolean buy) {
        this.buy = buy;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCharge() {
        return charge;
    }

    public void setCharge(BigDecimal charge) {
        this.charge = charge;
    }

    public boolean isOnlyWithPosition() {
        return onlyWithPosition;
    }

    public void setOnlyWithPosition(boolean onlyWithPosition) {
        this.onlyWithPosition = onlyWithPosition;
    }

    public AddOrderCommand deepCopy() {
        AddOrderCommand newCommand = new AddOrderCommand();
        newCommand.userid = this.userid;
        newCommand.portfolioid = this.portfolioid;
        newCommand.quote = this.quote;
        newCommand.charge = this.charge;
        newCommand.exchangeRate = this.exchangeRate;
        newCommand.volume = this.volume;
        newCommand.price = this.price;
        newCommand.date = this.date;
        newCommand.buy = this.buy;
        newCommand.onlyWithPosition = this.onlyWithPosition;

        return newCommand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddOrderCommand that = (AddOrderCommand) o;

        if (buy != that.buy) return false;
        if (onlyWithPosition != that.onlyWithPosition) return false;
        if (charge != null ? !charge.equals(that.charge) : that.charge != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (exchangeRate != null ? !exchangeRate.equals(that.exchangeRate) : that.exchangeRate != null)
            return false;
        if (portfolioid != null ? !portfolioid.equals(that.portfolioid) : that.portfolioid != null)
            return false;
        if (price != null ? !price.equals(that.price) : that.price != null) return false;
        if (quote != null ? !quote.equals(that.quote) : that.quote != null) return false;
        if (userid != null ? !userid.equals(that.userid) : that.userid != null) return false;
        if (volume != null ? !volume.equals(that.volume) : that.volume != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userid != null ? userid.hashCode() : 0;
        result = 31 * result + (portfolioid != null ? portfolioid.hashCode() : 0);
        result = 31 * result + (quote != null ? quote.hashCode() : 0);
        result = 31 * result + (charge != null ? charge.hashCode() : 0);
        result = 31 * result + (exchangeRate != null ? exchangeRate.hashCode() : 0);
        result = 31 * result + (volume != null ? volume.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (buy ? 1 : 0);
        result = 31 * result + (onlyWithPosition ? 1 : 0);
        return result;
    }
}
