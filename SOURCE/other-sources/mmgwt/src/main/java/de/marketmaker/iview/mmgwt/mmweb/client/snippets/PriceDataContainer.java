/*
 * PriceDataContainer.java
 *
 * Created on 29.02.12 11:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.FundPriceData;
import de.marketmaker.iview.dmxml.LMEPriceData;
import de.marketmaker.iview.dmxml.PriceData;
import de.marketmaker.iview.dmxml.PriceDataExtended;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;

/**
 * @author oflege
 */
class PriceDataContainer {
    private final QuoteData quoteData;
    private final PriceData priceData;
    private final FundPriceData fundpriceData;
    private final PriceDataExtended pricedataExtended;
    private final LMEPriceData lmePriceData;
    private final Price price;

    // TODO: remove this constructor when LMEPriceData is completely integrated
    public PriceDataContainer(
            QuoteData quotedata, PriceData priceData, FundPriceData fundpriceData,
            PriceDataExtended pricedataExtended, Price p) {
        this(quotedata, priceData, fundpriceData, pricedataExtended, null, p);
    }

    public PriceDataContainer(
            QuoteData quoteData, PriceData priceData, FundPriceData fundpriceData,
            PriceDataExtended pricedataExtended, LMEPriceData lmePriceData, Price price) {
        this.quoteData = quoteData;
        this.priceData = priceData;
        this.fundpriceData = fundpriceData;
        this.pricedataExtended = pricedataExtended;
        this.lmePriceData = lmePriceData;
        this.price = price;
    }

    public QuoteData getQuoteData() {
        return quoteData;
    }

    public PriceData getPriceData() {
        return priceData;
    }

    public FundPriceData getFundpriceData() {
        return fundpriceData;
    }

    public PriceDataExtended getPricedataExtended() {
        return pricedataExtended;
    }

    public LMEPriceData getLMEPriceData() {
        return lmePriceData;
    }

    public Price getPrice() {
        return price;
    }

}
