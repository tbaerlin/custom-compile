/*
 * BewSymbol.java
 *
 * Created on 19.05.2010 15:22:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.math.BigDecimal;

import org.joda.time.DateTimeZone;

import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.NullPrice;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.PriceRecordFactory;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayData;

/**
 * Represents a row in the result file.
 * @author oflege
 */
class ResultItem {
    private final RequestItem request;

    private final Quote quote;

    private IntradayData intradayData;

    private PriceRecord priceRecord;

    private final BigDecimal priceFactor;

    private Price lastMonthsUltimo;

    private Price lastYearsUltimo;

    private boolean failed = false;

    private Price valuationPrice = NullPrice.INSTANCE;

    private Price previousValuationPrice = NullPrice.INSTANCE;

    private String valuationPriceSource;

    private DateTimeZone timeZone;

    ResultItem(RequestItem request, Quote quote) {
        this.request = request;
        this.quote = quote;
        this.priceFactor = getPriceFactor(quote);
    }

    private BigDecimal getPriceFactor(Quote quote) {
        // currency id = 69: ZAR
        // currency id = 1762: ZAX, ZAC == ZAR in cent

        // LON returns GBX prices, LOG returns GBP prices
        // JNB returns ZAX/ZAC prices, JNR returns ZAR prices
        if (quote != null && !quote.getMinimumQuotationSize().isUnitPercent()
                && ("LON".equals(this.request.getExchange()) && "GBP".equals(quote.getCurrency().getSymbolIso())
                || ("JNB".equals(this.request.getExchange()) && quote.getCurrency().getId() == 69))) {
            return Constants.ONE_HUNDRED;
        }
        if (quote != null && !quote.getMinimumQuotationSize().isUnitPercent()
                && ("LOG".equals(this.request.getExchange()) && "GBX".equals(quote.getCurrency().getSymbolIso())
                || ("JNR".equals(this.request.getExchange()) && quote.getCurrency().getId() == 1762))) {
            return Constants.ONE_PERCENT;
        }
        return BigDecimal.ONE;
    }

    public String toString() {
        return "ResultItem[" + this.request.toString() + "]";
    }

    public void setFailed() {
        this.failed = true;
    }

    public boolean isFailed() {
        return this.failed;
    }

    String toStringInUnknownFile(boolean debug) {
        final StringBuilder sb = new StringBuilder(80);
        sb.append(getSymbol());
        if (getExchange() != null) {
            sb.append(";").append(getExchange());
        }
        if (debug) {
            sb.append(" -- ");
            request.appendTo(sb);
        }
        return sb.toString();
    }

    RequestItem getRequest() {
        return this.request;
    }

    String getSymbol() {
        return this.request.getSymbolAsRequested();
    }

    public String getExternalFields() {
        return this.request.getExternalFields();
    }

    String getClientsideMapping() {
        return this.request.getClientsideMapping();
    }

    String getExchange() {
        final String e = this.request.getExchangeAsRequested();
        return (e != null || this.quote == null) ? e : MarketMapping.getBewOldMarketCode(this.quote);
    }

    Quote getQuote() {
        return quote;
    }

    void setIntradayData(IntradayData intradayData) {
        this.intradayData = intradayData;
        if (this.intradayData != null) {
            initPriceRecord();
        }
        calcValuationPrice();
    }

    void setTimeZone(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public DateTimeZone getTimeZone() {
        return timeZone;
    }

    void setLastMonthsUltimo(Price lastMonthsUltimo) {
        this.lastMonthsUltimo = lastMonthsUltimo;
    }

    public Price getLastMonthsUltimo() {
        return lastMonthsUltimo;
    }

    public void setLastYearsUltimo(Price lastYearsUltimo) {
        this.lastYearsUltimo = lastYearsUltimo;
    }

    public Price getLastYearsUltimo() {
        return lastYearsUltimo;
    }

    private void initPriceRecord() {
        if (this.quote == null) {
            this.priceRecord = NullPriceRecord.INSTANCE;
            return;
        }

        if (this.quote instanceof OpraQuote) {
            this.priceRecord = ((OpraQuote) this.quote).getPriceRecord();
        }
        else {
            this.priceRecord = computePriceRecord();
        }
    }

    BigDecimal getPriceFactor() {
        return this.priceFactor;
    }

    IntradayData getIntradayData() {
        return this.intradayData;
    }

    PriceRecord getPriceRecord() {
        return this.priceRecord;
    }

    private PriceRecord computePriceRecord() {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final SnapRecord snap = this.intradayData.getSnap();
        return PriceRecordFactory.create(this.quote, snap,
                profile.getPriceQuality(this.quote), false);
    }

    int getValuationPriceField() {
        return this.request.getServersideMapping() != null
                ? this.request.getServersideMapping().getField() : -1;
    }

    public Price getValuationPrice() {
        return this.valuationPrice;
    }

    public String getValuationPriceSource() {
        return valuationPriceSource;
    }

    public Price getPreviousValuationPrice() {
        return this.previousValuationPrice;
    }

    public Price getBid() {
        return ValuationPrice.getBid(this.priceRecord);
    }

    public Price getAsk() {
        return ValuationPrice.getAsk(this.priceRecord);
    }

    private void calcValuationPrice() {
        if (this.quote == null || this.priceRecord == null) {
            return;
        }

        if (this.quote instanceof OpraQuote) {
            this.valuationPrice = this.priceRecord.getSettlement();
            this.valuationPriceSource = "settlement";
            return;
        }

        final ValuationPrice vp = new ValuationPrice(this.quote, this.intradayData.getSnap(),
                this.priceRecord, getValuationPriceField(), null);

        this.valuationPrice = vp.getValuationPrice();
        this.previousValuationPrice = vp.getPreviousValuationPrice();

        this.valuationPriceSource = vp.getSource();
    }

    void setValuationPrice(Price price, String valuationPriceSource) {
        this.valuationPrice = price;
        this.valuationPriceSource = valuationPriceSource;
    }
}