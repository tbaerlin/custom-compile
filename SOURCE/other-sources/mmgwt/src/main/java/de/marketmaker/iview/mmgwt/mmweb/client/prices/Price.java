/*
 * Price.java
 *
 * Created on 02.02.2009 12:42:03
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.prices;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.*;
import de.marketmaker.iview.mmgwt.mmweb.client.data.PriceWithSupplement;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushPrice;

/**
 * Combines attributes of PriceData, FundPriceData, and ContractPriceData to simplify
 * handling of those objects. Only {@link de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceStore}
 * may create prices, all other objects just retrieve the prices created by that instance.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Price implements Pushable {
    public static final String UPDATED_BUT_UNCHANGED = "_"; // $NON-NLS-0$

    public static final Price NULL_PRICE = new Price();

    public static final PriceData NULL_PRICE_DATA = new PriceData();

    public static final FundPriceData NULL_FUND_PRICE_DATA = new FundPriceData();

    public static final ContractPriceData NULL_CONTRACT_PRICE_DATA = new ContractPriceData();

    public static final PriceDataExtended NULL_PRICE_DATA_EXTENDED = new PriceDataExtended();

    public static final LMEPriceData NULL_LME_PRICE_DATA = new LMEPriceData();


    public static PriceData nonNullPriceData(PriceData pd) {
        return pd != null ? pd : NULL_PRICE_DATA;
    }

    public static FundPriceData nonNullFundPriceData(FundPriceData pd) {
        return pd != null ? pd : NULL_FUND_PRICE_DATA;
    }

    public static ContractPriceData nonNullContractPriceData(ContractPriceData pd) {
        return pd != null ? pd : NULL_CONTRACT_PRICE_DATA;
    }

    public static PriceDataExtended nonNullPriceDataExtended(PriceDataExtended pd) {
        return pd != null ? pd : NULL_PRICE_DATA_EXTENDED;
    }

    public static LMEPriceData nonNullLMEPriceData(LMEPriceData pd) {
        return pd != null ? pd : NULL_LME_PRICE_DATA;
    }

    public static Price create(HasPricedata e) {
        return getFromStore(e.getQuotedata());
    }

    public static Price create(IMGResult e) {
        return getFromStore(e.getQuotedata());
    }

    static Price doCreate(HasPricedata e) {
        return create(e.getPricedatatype(), e.getPricedata(), e.getFundpricedata(), e.getContractpricedata(), e.getLmepricedata());
    }

    static Price doCreate(IMGResult e) {
        return create("standard", e.getPricedata(), null, null, null); // $NON-NLS-0$
    }

    private static Price getFromStore(QuoteData data) {
        final Price result = PriceStore.INSTANCE.getPrice(data);
        return result != null ? result : NULL_PRICE;
    }

    private static Price create(String priceDataType, PriceData priceData,
                                FundPriceData fundPriceData, ContractPriceData contractPriceData, LMEPriceData lmePriceData) {
        switch (PriceDataType.fromDmxml(priceDataType)) {
            case FUND_OTC:
                return fundPriceData != null ? new Price(fundPriceData) : NULL_PRICE;
            case CONTRACT_EXCHANGE:
                return contractPriceData != null ? new Price(contractPriceData) : NULL_PRICE;
            case LME:
                return lmePriceData != null ? new Price(lmePriceData) : NULL_PRICE;
            default:
                return priceData != null ? new Price(priceData) : NULL_PRICE;
        }
    }

    private int generation;

    private Price previous;

    private final PriceDataType type;

    private String ask;

    private Long askVolume;

    private String bid;

    private Long bidVolume;

    private String bidAskDate;

    private String changeNet;

    private String changePercent;

    private String date;

    private String high;

    private String high52W;

    private String highYear;

    private String low;

    private String low52W;

    private String lowYear;

    private String open;

    private String close;

    private Long numTrades;

    private PriceWithSupplement previousPrice;

    private String quality;

    private PriceWithSupplement lastPrice;

    private String turnoverDay;

    private Long volume;

    private Long volumePrice;

    private String previousCloseDate;

    private String interpolatedClosing;

    private String provisionalEvaluation;

    private String officialAsk;

    private String officialBid;

    private String unofficialAsk;

    private String unofficialBid;

    private String previousOfficialAsk;

    private String previousOfficialBid;

    private String previousUnofficialAsk;

    private String previousUnofficialBid;

    private Price() {
        this.previousPrice = PriceWithSupplement.NULL;
        this.lastPrice = PriceWithSupplement.NULL;
        this.type = PriceDataType.INVALID;
    }

    public Price(PriceData data) {
        this.type = PriceDataType.STANDARD;
        ask = data.getAsk();
        askVolume = data.getAskVolume();
        bid = data.getBid();
        bidVolume = data.getBidVolume();
        bidAskDate = data.getBidAskDate();
        changeNet = data.getChangeNet();
        changePercent = data.getChangePercent();
        date = data.getDate();
        high = data.getHighDay();
        high52W = data.getHigh1Y();
        highYear = data.getHighYear();
        low = data.getLowDay();
        low52W = data.getLow1Y();
        lowYear = data.getLowYear();
        open = data.getOpen();
        close = data.getClose();
        numTrades = data.getNumberOfTrades();
        previousPrice = new PriceWithSupplement(data.getPreviousClose(), data.getPreviousCloseSupplement());
        lastPrice = new PriceWithSupplement(data.getPrice(), data.getSupplement());
        turnoverDay = data.getTurnoverDay();
        volume = data.getVolumeDay();
        quality = data.getPriceQuality();
        volumePrice = data.getVolumePrice();
        previousCloseDate = data.getPreviousCloseDate();
        interpolatedClosing = null;
        provisionalEvaluation = null;
        officialAsk = null;
        officialBid = null;
        unofficialAsk = null;
        unofficialBid = null;
        previousOfficialAsk = null;
        previousOfficialBid = null;
        previousUnofficialAsk = null;
        previousUnofficialBid = null;
    }

    public Price(FundPriceData data) {
        this.type = PriceDataType.FUND_OTC;
        changeNet = data.getChangeNet();
        changePercent = data.getChangePercent();
        date = data.getDate();
        high52W = data.getHigh1Y();
        highYear = data.getHighYear();
        low52W = data.getLow1Y();
        lowYear = data.getLowYear();

        open = null;
        close = null;
        numTrades = null;
        previousPrice = new PriceWithSupplement(data.getPreviousRepurchasingprice(), null);
        lastPrice = new PriceWithSupplement(data.getPrice(), null);
        turnoverDay = null;
        volume = null;
        quality = data.getPriceQuality();
        volumePrice = null;
        interpolatedClosing = null;
        provisionalEvaluation = null;
        officialAsk = null;
        officialBid = null;
        unofficialAsk = null;
        unofficialBid = null;
        previousOfficialAsk = null;
        previousOfficialBid = null;
        previousUnofficialAsk = null;
        previousUnofficialBid = null;
    }

    public Price(ContractPriceData data) {
        final boolean lmeDataInstance = data instanceof LMEPriceData;

        this.type = lmeDataInstance ? PriceDataType.LME : PriceDataType.CONTRACT_EXCHANGE;
        ask = data.getAsk();
        askVolume = data.getAskVolume();
        bid = data.getBid();
        bidVolume = data.getBidVolume();
        bidAskDate = data.getBidAskDate();
        changeNet = data.getChangeNet();
        changePercent = data.getChangePercent();
        date = data.getDate();
        high = data.getHighDay();
        high52W = data.getHigh1Y();
        highYear = data.getHighYear();
        low = data.getLowDay();
        low52W = data.getLow1Y();
        lowYear = data.getLowYear();
        open = data.getOpen();
        close = data.getClose();
        numTrades = data.getNumberOfTrades();
        previousPrice = new PriceWithSupplement(data.getPreviousClose(), data.getPreviousCloseSupplement());
        lastPrice = new PriceWithSupplement(data.getPrice(), data.getSupplement());
        turnoverDay = data.getTurnoverDay();
        volume = data.getVolumeDay();
        quality = data.getPriceQuality();
        volumePrice = data.getVolumePrice();

        if (lmeDataInstance) {
            final LMEPriceData lmeData = (LMEPriceData) data;
            interpolatedClosing = lmeData.getInterpolatedClosing();
            provisionalEvaluation = lmeData.getProvisionalEvaluation();
            officialAsk = lmeData.getOfficialAsk();
            officialBid = lmeData.getOfficialBid();
            unofficialAsk = lmeData.getUnofficialAsk();
            unofficialBid = lmeData.getUnofficialBid();
            previousOfficialAsk = lmeData.getPreviousOfficialAsk();
            previousOfficialBid = lmeData.getPreviousOfficialBid();
            previousUnofficialAsk = lmeData.getPreviousUnofficialAsk();
            previousUnofficialBid = lmeData.getPreviousUnofficialBid();
        }
        else {
            interpolatedClosing = null;
            provisionalEvaluation = null;
            officialAsk = null;
            officialBid = null;
            unofficialAsk = null;
            unofficialBid = null;
            previousOfficialAsk = null;
            previousOfficialBid = null;
            previousUnofficialAsk = null;
            previousUnofficialBid = null;
        }
    }

    /**
     * copy fields from pp and save the data we need for diff rendering in previous price
     *
     * @param pp pushed price update
     */
    public void copyFrom(PushPrice pp) {
        resetPrevious();
        //override with push values that are NOT NULL
        if (pp.getDate() != null) {
            this.date = pp.getDate();
        }
        if (pp.getPrice() != null) {
            this.previous.lastPrice.setPrice(this.lastPrice.getPrice());
            if (!UPDATED_BUT_UNCHANGED.equals(pp.getPrice())) {
                this.lastPrice.setPrice(pp.getPrice());
            }
        }
        if (pp.getSupplement() != null) {
            this.lastPrice.setSupplement(pp.getSupplement());
        }
        if (pp.getVolumePrice() != null) {
            this.volumePrice = pp.getVolumePrice();
        }
        if (pp.getChangeNet() != null) {
            this.previous.changeNet = this.changeNet;
            if (!UPDATED_BUT_UNCHANGED.equals(pp.getChangeNet())) {
                this.changeNet = pp.getChangeNet();
            }
        }
        if (pp.getChangePercent() != null) {
            this.previous.changePercent = this.changePercent;
            if (!UPDATED_BUT_UNCHANGED.equals(pp.getChangePercent())) {
                this.changePercent = pp.getChangePercent();
            }
        }
        if (pp.getBidAskDate() != null) {
            this.bidAskDate = pp.getBidAskDate();
        }
        if (pp.getAsk() != null) {
            this.previous.ask = this.ask;
            if (!UPDATED_BUT_UNCHANGED.equals(pp.getAsk())) {
                this.ask = pp.getAsk();
            }
        }
        if (pp.getBid() != null) {
            this.previous.bid = this.bid;
            if (!UPDATED_BUT_UNCHANGED.equals(pp.getBid())) {
                this.bid = pp.getBid();
            }
        }
        if (pp.getAskVolume() != null) {
            this.askVolume = pp.getAskVolume();
        }
        if (pp.getBidVolume() != null) {
            this.bidVolume = pp.getBidVolume();
        }
        if (pp.getLow() != null) {
            this.previous.low = this.low;
            if (!UPDATED_BUT_UNCHANGED.equals(pp.getLow())) {
                this.low = pp.getLow();
            }
        }
        if (pp.getLowYear() != null) {
            this.lowYear = pp.getLowYear();
        }
        // TODO: low52W not yet pp
        if (pp.getHigh() != null) {
            this.previous.high = this.high;
            if (!UPDATED_BUT_UNCHANGED.equals(pp.getHigh())) {
                this.high = pp.getHigh();
            }
        }
        if (pp.getHighYear() != null) {
            this.highYear = pp.getHighYear();
        }
        // TODO: high52W not in pp
        if (pp.getVolumeDay() != null) {
            this.volume = pp.getVolumeDay();
        }
        if (pp.getTurnoverDay() != null) {
            this.turnoverDay = pp.getTurnoverDay();
        }
        if (pp.getNumberOfTrades() != null) {
            this.numTrades = pp.getNumberOfTrades();
        }
        if (pp.getOpen() != null) {
            this.open = pp.getOpen();
        }
        if (pp.getClose() != null) {
            this.close = pp.getClose();
        }
        if (pp.getPreviousClose() != null || pp.getPreviousCloseSupplement() != null) {
            this.previousPrice.setPrice(pp.getPreviousClose());
        }
        if (pp.getPreviousCloseSupplement() != null) {
            this.previousPrice.setSupplement(pp.getPreviousCloseSupplement());
        }
        if (pp.getPreviousCloseDate() != null) {
            this.previousCloseDate = pp.getPreviousCloseDate();
        }

        if (pp.getInterpolatedClosing() != null) {
            this.interpolatedClosing = pp.getInterpolatedClosing();
        }

        if (pp.getProvisionalEvaluation() != null) {
            this.provisionalEvaluation = pp.getProvisionalEvaluation();
        }

        if (pp.getOfficialAsk() != null) {
            this.officialAsk = pp.getOfficialAsk();
        }

        if (pp.getOfficialBid() != null) {
            this.officialBid = pp.getOfficialBid();
        }

        if (pp.getUnofficialAsk() != null) {
            this.unofficialAsk = pp.getUnofficialAsk();
        }

        if (pp.getUnofficialBid() != null) {
            this.unofficialBid = pp.getUnofficialBid();
        }

        if (pp.getPreviousOfficialAsk() != null) {
            this.previousOfficialAsk = pp.getPreviousOfficialAsk();
        }

        if (pp.getPreviousOfficialBid() != null) {
            this.previousOfficialBid = pp.getPreviousOfficialBid();
        }

        if (pp.getPreviousUnofficialAsk() != null) {
            this.previousUnofficialAsk = pp.getPreviousUnofficialAsk();
        }

        if (pp.getPreviousUnofficialBid() != null) {
            this.previousUnofficialBid = pp.getPreviousUnofficialBid();
        }

        //noinspection StatementWithEmptyBody
        if (pp.getIssueprice() != null) {
            // todo                            
        }
        //noinspection StatementWithEmptyBody
        if (pp.getSettlement() != null) {
            // todo                            
        }
        //noinspection StatementWithEmptyBody
        if (pp.getOpenInterest() != null) {
            // todo                            
        }
    }

    private void resetPrevious() {
        if (this.previous == null) {
            this.previous = new Price(this.type);
        }
        this.previous.copyFrom(NULL_PRICE);
    }

    private Price(PriceDataType type) {
        this.type = type;
    }

    public void copyFrom(Price price) {
        this.previous = null;
        this.ask = price.getAsk();
        this.askVolume = price.getAskVolume();
        this.bid = price.getBid();
        this.bidVolume = price.getBidVolume();
        this.bidAskDate = price.getBidAskDate();
        this.changeNet = price.getChangeNet();
        this.changePercent = price.getChangePercent();
        this.date = price.getDate();
        this.high = price.getHigh();
        this.high52W = price.getHigh52W();
        this.highYear = price.getHighYear();
        this.low = price.getLow();
        this.low52W = price.getLow52W();
        this.lowYear = price.getLowYear();
        this.open = price.getOpen();
        this.close = price.getClose();
        this.numTrades = price.getNumTrades();
        this.previousPrice = copy(price.previousPrice, this.previousPrice);
        this.lastPrice = copy(price.lastPrice, this.lastPrice);
        this.turnoverDay = price.getTurnoverDay();
        this.volume = price.getVolume();
        this.quality = price.getQuality();
        this.volumePrice = price.getVolumePrice();
        this.previousCloseDate = price.getPreviousCloseDate();
        this.interpolatedClosing = assign(price, price.getInterpolatedClosing(), this.interpolatedClosing);
        this.provisionalEvaluation = assign(price, price.getProvisionalEvaluation(), this.provisionalEvaluation);
        this.officialAsk = assign(price, price.getOfficialAsk(), this.officialAsk);
        this.officialBid = assign(price, price.getOfficialBid(), this.officialBid);
        this.unofficialAsk = assign(price, price.getUnofficialAsk(), this.unofficialAsk);
        this.unofficialBid = assign(price, price.getUnofficialBid(), this.unofficialBid);
        this.previousOfficialAsk = assign(price, price.getPreviousOfficialAsk(), this.previousOfficialAsk);
        this.previousOfficialBid = assign(price, price.getPreviousOfficialBid(), this.previousOfficialBid);
        this.previousUnofficialAsk = assign(price, price.getPreviousUnofficialAsk(), this.previousUnofficialAsk);
        this.previousUnofficialBid = assign(price, price.getPreviousUnofficialBid(), this.previousUnofficialBid);
    }

    private PriceWithSupplement copy(PriceWithSupplement source, PriceWithSupplement target) {
        if (source == null) {
            return null;
        }
        if (target == null) {
            return new PriceWithSupplement(source.getPrice(), source.getSupplement());
        }
        target.setPrice(source.getPrice());
        target.setSupplement(source.getSupplement());
        return target;
    }

    /**
     * Assigns a value to a price field, but does not overwrite a value if the new value is null
     * and the the price type of the new value differs from that of the old value. An exception
     * from this rule is the explicit {@linkplain #NULL_PRICE}. This avoids, e.g., that LME field
     * previousOfficialAsk can be overwritten by a standard price type which will never have a
     * non-null previousOfficialAsk field. This is a common problem if prices are updated with
     * prices from {@linkplain IMGPriceResult}, cf. {@linkplain #doCreate(IMGResult)}.
     * @param newPrice the price instance of the price where the new value comes from
     * @param newValue the new value
     * @param oldValue the old value
     * @return the value
     */
    private String assign(Price newPrice, String newValue, String oldValue) {
        if(newValue != null) {
            return newValue;
        }
        if(newPrice.type == this.type || NULL_PRICE.equals(newPrice)) {
            return null;
        }
        return oldValue;
    }

    // TODO: should be re-implemented so that it is possible to represent the inheritance structure:
    // LME prices are contract prices but type enum differs. There should be a method that explicitly
    // checks this.
    public PriceDataType getType() {
        return this.type;
    }

    public boolean isPushable() {
        return this.quality != null && this.quality.endsWith("+"); // $NON-NLS-0$
    }

    public int getGeneration() {
        return generation;
    }

    void setGeneration(int generation) {
        this.generation = generation;
    }

    public Price getPrevious() {
        return previous;
    }

    public void setPrevious(Price previous) {
        this.previous = previous;
    }

    public boolean isPushPrice() {
        return this.previous != null;
    }

    @Override
    public String toString() {
        return getLastPrice().toString() + "@" + getDate(); // $NON-NLS-0$
    }

    public String getAsk() {
        return ask;
    }

    public Long getAskVolume() {
        return askVolume;
    }

    public String getBid() {
        return bid;
    }

    public Long getBidVolume() {
        return bidVolume;
    }

    public String getBidAskDate() {
        return bidAskDate;
    }

    public String getChangeNet() {
        return changeNet;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public String getDate() {
        return date;
    }

    public String getHigh() {
        return high;
    }

    public String getHighYear() {
        return highYear;
    }

    public String getHigh52W() {
        return high52W;
    }

    public String getLow() {
        return low;
    }

    public String getLow52W() {
        return low52W;
    }

    public String getLowYear() {
        return lowYear;
    }

    public String getOpen() {
        return open;
    }

    public String getClose() {
        return close;
    }

    public Long getNumTrades() {
        return numTrades;
    }

    public PriceWithSupplement getPreviousPrice() {
        return previousPrice;
    }

    public PriceWithSupplement getLastPrice() {
        return lastPrice;
    }

    public String getTurnoverDay() {
        return turnoverDay;
    }

    public Long getVolume() {
        return volume;
    }

    public String getQuality() {
        return this.quality;
    }

    public Long getVolumePrice() {
        return volumePrice;
    }

    public String getPreviousCloseDate() {
        return previousCloseDate;
    }

    public String getInterpolatedClosing() {
        return interpolatedClosing;
    }

    public String getProvisionalEvaluation() {
        return provisionalEvaluation;
    }

    public String getOfficialAsk() {
        return officialAsk;
    }

    public String getOfficialBid() {
        return officialBid;
    }

    public String getUnofficialAsk() {
        return unofficialAsk;
    }

    public String getUnofficialBid() {
        return unofficialBid;
    }

    public String getPreviousOfficialAsk() {
        return previousOfficialAsk;
    }

    public String getPreviousOfficialBid() {
        return previousOfficialBid;
    }

    public String getPreviousUnofficialAsk() {
        return previousUnofficialAsk;
    }

    public String getPreviousUnofficialBid() {
        return previousUnofficialBid;
    }

    private int compareStringsAsDoubles(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return 0;
        }
        if (s1 == null || s2 == null) {
            final String error = ".compareStringsAsDoubles: params must not be null!"; // $NON-NLS-0$
            Firebug.log(getClass().getName() + error);
            throw new IllegalArgumentException(getClass().getName() + error);
        }
        Double d1 = Double.valueOf(s1);
        Double d2 = Double.valueOf(s2);
        return d1.compareTo(d2);
    }

    public int compareLastPrice(Price price) {
        return compareStringsAsDoubles(getLastPrice().getPrice(), price.getLastPrice().getPrice());
    }

    public int compareBid(Price price) {
        return compareStringsAsDoubles(getBid(), price.getBid());
    }

    public int compareAsk(Price price) {
        return compareStringsAsDoubles(getAsk(), price.getAsk());
    }

    public int compareLow(Price price) {
        return compareStringsAsDoubles(getLow(), price.getLow());
    }

    public int compareHigh(Price price) {
        return compareStringsAsDoubles(getHigh(), price.getHigh());
    }

    public int compareChangeNet(Price price) {
        return compareStringsAsDoubles(getChangeNet(), price.getChangeNet());
    }

    public int compareChangePercent(Price price) {
        return compareStringsAsDoubles(getChangePercent(), price.getChangePercent());
    }

    public int compareInterpolatedClosing(Price price) {
        return compareStringsAsDoubles(getInterpolatedClosing(), price.getInterpolatedClosing());
    }

    public int compareProvisionalEvaluation(Price price) {
        return compareStringsAsDoubles(getProvisionalEvaluation(), price.getProvisionalEvaluation());
    }

    public int compareOfficialAsk(Price price) {
        return compareStringsAsDoubles(getOfficialAsk(), price.getOfficialAsk());
    }

    public int compareOfficialBid(Price price) {
        return compareStringsAsDoubles(getOfficialBid(), price.getOfficialBid());
    }

    public int compareUnofficialAsk(Price price) {
        return compareStringsAsDoubles(getUnofficialAsk(), price.getUnofficialAsk());
    }

    public int compareUnofficialBid(Price price) {
        return compareStringsAsDoubles(getUnofficialBid(), price.getUnofficialBid());
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Price)) return false;

        Price price = (Price) o;

        if (generation != price.generation) return false;
        if (previous != null ? !previous.equals(price.previous) : price.previous != null)
            return false;
        if (type != price.type) return false;
        if (ask != null ? !ask.equals(price.ask) : price.ask != null) return false;
        if (askVolume != null ? !askVolume.equals(price.askVolume) : price.askVolume != null)
            return false;
        if (bid != null ? !bid.equals(price.bid) : price.bid != null) return false;
        if (bidVolume != null ? !bidVolume.equals(price.bidVolume) : price.bidVolume != null)
            return false;
        if (bidAskDate != null ? !bidAskDate.equals(price.bidAskDate) : price.bidAskDate != null)
            return false;
        if (changeNet != null ? !changeNet.equals(price.changeNet) : price.changeNet != null)
            return false;
        if (changePercent != null ? !changePercent.equals(price.changePercent) : price.changePercent != null)
            return false;
        if (date != null ? !date.equals(price.date) : price.date != null) return false;
        if (high != null ? !high.equals(price.high) : price.high != null) return false;
        if (high52W != null ? !high52W.equals(price.high52W) : price.high52W != null) return false;
        if (highYear != null ? !highYear.equals(price.highYear) : price.highYear != null)
            return false;
        if (low != null ? !low.equals(price.low) : price.low != null) return false;
        if (low52W != null ? !low52W.equals(price.low52W) : price.low52W != null) return false;
        if (lowYear != null ? !lowYear.equals(price.lowYear) : price.lowYear != null) return false;
        if (open != null ? !open.equals(price.open) : price.open != null) return false;
        if (close != null ? !close.equals(price.close) : price.close != null) return false;
        if (numTrades != null ? !numTrades.equals(price.numTrades) : price.numTrades != null)
            return false;
        if (previousPrice != null ? !previousPrice.equals(price.previousPrice) : price.previousPrice != null)
            return false;
        if (quality != null ? !quality.equals(price.quality) : price.quality != null) return false;
        if (lastPrice != null ? !lastPrice.equals(price.lastPrice) : price.lastPrice != null)
            return false;
        if (turnoverDay != null ? !turnoverDay.equals(price.turnoverDay) : price.turnoverDay != null)
            return false;
        if (volume != null ? !volume.equals(price.volume) : price.volume != null) return false;
        if (volumePrice != null ? !volumePrice.equals(price.volumePrice) : price.volumePrice != null)
            return false;
        if (previousCloseDate != null ? !previousCloseDate.equals(price.previousCloseDate) : price.previousCloseDate != null)
            return false;
        if (interpolatedClosing != null ? !interpolatedClosing.equals(price.interpolatedClosing) : price.interpolatedClosing != null)
            return false;
        if (provisionalEvaluation != null ? !provisionalEvaluation.equals(price.provisionalEvaluation) : price.provisionalEvaluation != null)
            return false;
        if (officialAsk != null ? !officialAsk.equals(price.officialAsk) : price.officialAsk != null)
            return false;
        if (officialBid != null ? !officialBid.equals(price.officialBid) : price.officialBid != null)
            return false;
        if (unofficialAsk != null ? !unofficialAsk.equals(price.unofficialAsk) : price.unofficialAsk != null)
            return false;
        if (unofficialBid != null ? !unofficialBid.equals(price.unofficialBid) : price.unofficialBid != null)
            return false;
        if (previousOfficialAsk != null ? !previousOfficialAsk.equals(price.previousOfficialAsk) : price.previousOfficialAsk != null)
            return false;
        if (previousOfficialBid != null ? !previousOfficialBid.equals(price.previousOfficialBid) : price.previousOfficialBid != null)
            return false;
        if (previousUnofficialAsk != null ? !previousUnofficialAsk.equals(price.previousUnofficialAsk) : price.previousUnofficialAsk != null)
            return false;
        return previousUnofficialBid != null ? previousUnofficialBid.equals(price.previousUnofficialBid) : price.previousUnofficialBid == null;

    }

    @Override
    public int hashCode() {
        int result = generation;
        result = 31 * result + (previous != null ? previous.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (ask != null ? ask.hashCode() : 0);
        result = 31 * result + (askVolume != null ? askVolume.hashCode() : 0);
        result = 31 * result + (bid != null ? bid.hashCode() : 0);
        result = 31 * result + (bidVolume != null ? bidVolume.hashCode() : 0);
        result = 31 * result + (bidAskDate != null ? bidAskDate.hashCode() : 0);
        result = 31 * result + (changeNet != null ? changeNet.hashCode() : 0);
        result = 31 * result + (changePercent != null ? changePercent.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (high != null ? high.hashCode() : 0);
        result = 31 * result + (high52W != null ? high52W.hashCode() : 0);
        result = 31 * result + (highYear != null ? highYear.hashCode() : 0);
        result = 31 * result + (low != null ? low.hashCode() : 0);
        result = 31 * result + (low52W != null ? low52W.hashCode() : 0);
        result = 31 * result + (lowYear != null ? lowYear.hashCode() : 0);
        result = 31 * result + (open != null ? open.hashCode() : 0);
        result = 31 * result + (close != null ? close.hashCode() : 0);
        result = 31 * result + (numTrades != null ? numTrades.hashCode() : 0);
        result = 31 * result + (previousPrice != null ? previousPrice.hashCode() : 0);
        result = 31 * result + (quality != null ? quality.hashCode() : 0);
        result = 31 * result + (lastPrice != null ? lastPrice.hashCode() : 0);
        result = 31 * result + (turnoverDay != null ? turnoverDay.hashCode() : 0);
        result = 31 * result + (volume != null ? volume.hashCode() : 0);
        result = 31 * result + (volumePrice != null ? volumePrice.hashCode() : 0);
        result = 31 * result + (previousCloseDate != null ? previousCloseDate.hashCode() : 0);
        result = 31 * result + (interpolatedClosing != null ? interpolatedClosing.hashCode() : 0);
        result = 31 * result + (provisionalEvaluation != null ? provisionalEvaluation.hashCode() : 0);
        result = 31 * result + (officialAsk != null ? officialAsk.hashCode() : 0);
        result = 31 * result + (officialBid != null ? officialBid.hashCode() : 0);
        result = 31 * result + (unofficialAsk != null ? unofficialAsk.hashCode() : 0);
        result = 31 * result + (unofficialBid != null ? unofficialBid.hashCode() : 0);
        result = 31 * result + (previousOfficialAsk != null ? previousOfficialAsk.hashCode() : 0);
        result = 31 * result + (previousOfficialBid != null ? previousOfficialBid.hashCode() : 0);
        result = 31 * result + (previousUnofficialAsk != null ? previousUnofficialAsk.hashCode() : 0);
        result = 31 * result + (previousUnofficialBid != null ? previousUnofficialBid.hashCode() : 0);
        return result;
    }
}
