package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class QuoteRatiosCER extends QuoteRatios<InstrumentRatiosCER> {
    protected static final long serialVersionUID = 1L;

    public QuoteRatiosCER(long id, InstrumentRatiosCER instrumentRatios) {
        super(id, instrumentRatios);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportCER.forQuote(fid);
    }

    public PropertySupport propertySupport4Instrument(int fid) {
        return RatiosPropertySupportCER.forInstrument(fid);
    }

    private long mostRecentUpdateTimestamp = Long.MIN_VALUE;

    private long volatility1m = Long.MIN_VALUE;

    private long performance1m = Long.MIN_VALUE;

    private long spread = Long.MIN_VALUE;

    private long referenceTimestamp = Long.MIN_VALUE;

    private long spreadRelative = Long.MIN_VALUE;

    private long bid = Long.MIN_VALUE;

    private long ask = Long.MIN_VALUE;

    private long lastPrice = Long.MIN_VALUE;

    private long previousClose = Long.MIN_VALUE;

    private long discountPrice = Long.MIN_VALUE;

    private long discountPriceRelative = Long.MIN_VALUE;

    private long discountPriceRelativePerYear = Long.MIN_VALUE;

    private long unchangedEarning = Long.MIN_VALUE;

    private long unchangedEarningRelative = Long.MIN_VALUE;

    private long unchangedEarningRelativePerYear = Long.MIN_VALUE;

    private long bidVolume = Long.MIN_VALUE;

    private long askVolume = Long.MIN_VALUE;

    private long capToUnderlying = Long.MIN_VALUE;

    private long capToUnderlyingRelative = Long.MIN_VALUE;

    private long underlyingToCap = Long.MIN_VALUE;

    private long underlyingToCapRelative = Long.MIN_VALUE;

    private int bidAskDate = Integer.MIN_VALUE;

    private int bidAskTime = Integer.MIN_VALUE;

    private long tradeVolume = Long.MIN_VALUE;

    private int lastDate = Integer.MIN_VALUE;

    private int lastTime = Integer.MIN_VALUE;

    private long high = Long.MIN_VALUE;

    private long low = Long.MIN_VALUE;

    private long highYear = Long.MIN_VALUE;

    private long lowYear = Long.MIN_VALUE;

    private long totalVolume = Long.MIN_VALUE;

    private int previousDate = Integer.MIN_VALUE;

    private long open = Long.MIN_VALUE;

    private long underlyingLastPrice = Long.MIN_VALUE;

    private long underlyingPreviousClose = Long.MIN_VALUE;

    private long underlyingTradeVolume = Long.MIN_VALUE;

    private int underlyingLastDate = Integer.MIN_VALUE;

    private int underlyingLastTime = Integer.MIN_VALUE;

    private long underlyingTotalVolume = Long.MIN_VALUE;

    private int underlyingPreviousDate = Integer.MIN_VALUE;

    private long issuePrice = Long.MIN_VALUE;

    private long volatility3m = Long.MIN_VALUE;

    private long performance3m = Long.MIN_VALUE;

    private long performance1y = Long.MIN_VALUE;

    private long capLevel = Long.MIN_VALUE;

    private long yieldRelativePerYear = Long.MIN_VALUE;

    private long volatility10y = Long.MIN_VALUE;

    private long volatility1y = Long.MIN_VALUE;

    private long sharperatio1y = Long.MIN_VALUE;

    private long sharperatio3y = Long.MIN_VALUE;

    private long leverage = Long.MIN_VALUE;

    private long performance10y = Long.MIN_VALUE;

    private long performance1d = Long.MIN_VALUE;

    private long averagevolume1m = Long.MIN_VALUE;

    private long averagevolume1y = Long.MIN_VALUE;

    private long performance6m = Long.MIN_VALUE;

    private long performance3y = Long.MIN_VALUE;

    private long performance5y = Long.MIN_VALUE;

    private long volatility6m = Long.MIN_VALUE;

    private long volatility5y = Long.MIN_VALUE;

    private long sharperatio5y = Long.MIN_VALUE;

    private long sharperatio1m = Long.MIN_VALUE;

    private long sharperatio3m = Long.MIN_VALUE;

    private long sharperatio6m = Long.MIN_VALUE;

    private long underlyinglow1y = Long.MIN_VALUE;

    private long underlyinghigh1y = Long.MIN_VALUE;

    private String vwdCode = null;

    private String mmwkn = null;

    private String currency = null;

    private long highAlltime = Long.MIN_VALUE;

    private long lowAlltime = Long.MIN_VALUE;

    private int highAlltimeDate = Integer.MIN_VALUE;

    private int lowAlltimeDate = Integer.MIN_VALUE;

    private int high1yDate = Integer.MIN_VALUE;

    private int low1yDate = Integer.MIN_VALUE;

    private long averagePrice1w = Long.MIN_VALUE;

    private long averagePrice1m = Long.MIN_VALUE;

    private long averagePrice3m = Long.MIN_VALUE;

    private long averagePrice6m = Long.MIN_VALUE;

    private long averagePrice1y = Long.MIN_VALUE;

    private long averagePrice3y = Long.MIN_VALUE;

    private long averagePrice5y = Long.MIN_VALUE;

    private long averagePrice10y = Long.MIN_VALUE;

    private long changeNet = Long.MIN_VALUE;

    private long changePercent = Long.MIN_VALUE;

    private long volatility3y = Long.MIN_VALUE;

    private long performance1w = Long.MIN_VALUE;

    private long high1y = Long.MIN_VALUE;

    private long low1y = Long.MIN_VALUE;

    private long averagevolume1w = Long.MIN_VALUE;

    private long averagevolume3m = Long.MIN_VALUE;

    private long averagevolume6m = Long.MIN_VALUE;

    private long averagevolume3y = Long.MIN_VALUE;

    private long averagevolume5y = Long.MIN_VALUE;

    private long averagevolume10y = Long.MIN_VALUE;

    private String marketmanagerName = null;

    private long volatilityCurrentYear = Long.MIN_VALUE;

    private long mdpsbrokenperiodinterest = Long.MIN_VALUE;

    private long performanceAlltime = Long.MIN_VALUE;

    private long sharperatio1w = Long.MIN_VALUE;

    private long sharperatio10y = Long.MIN_VALUE;

    private long referencePrice = Long.MIN_VALUE;

    private String vwdMarket = null;

    private long performanceCurrentYear = Long.MIN_VALUE;

    private long mdpsDiscount = Long.MIN_VALUE;

    private long mdpsDiscountRelative = Long.MIN_VALUE;

    private long mdpsYield = Long.MIN_VALUE;

    private long mdpsYieldRelative = Long.MIN_VALUE;

    private long mdpsYieldRelativePerYear = Long.MIN_VALUE;

    private long mdpsGapCap = Long.MIN_VALUE;

    private long mdpsGapCapRelative = Long.MIN_VALUE;

    private long mdpsMaximumYield = Long.MIN_VALUE;

    private long mdpsMaximumYieldRelative = Long.MIN_VALUE;

    private long mdpsMaximumYieldRelativePerYear = Long.MIN_VALUE;

    private long mdpsUnchangedYieldRelative = Long.MIN_VALUE;

    private long mdpsUnchangedYieldRelativePerYear = Long.MIN_VALUE;

    private long mdpsGapBonusLevelRelative = Long.MIN_VALUE;

    private long mdpsGapBonusBufferRelative = Long.MIN_VALUE;

    private long mdpsAgioRelative = Long.MIN_VALUE;

    private long mdpsAgioRelativePerYear = Long.MIN_VALUE;

    private long mdpsGapLowerBarrier = Long.MIN_VALUE;

    private long mdpsGapLowerBarrierRelative = Long.MIN_VALUE;

    private long mdpsGapUpperBarrier = Long.MIN_VALUE;

    private long mdpsGapUpperBarrierRelative = Long.MIN_VALUE;

    private long mdpsUnderlyingToCapRelative = Long.MIN_VALUE;

    private long mdpsCapToUnderlyingRelative = Long.MIN_VALUE;

    private long mdpsGapStrikeRelative = Long.MIN_VALUE;

    private long mdpsGapBonusLevel = Long.MIN_VALUE;

    private long mdpsPerformanceAlltime = Long.MIN_VALUE;

    private long mdpsAgio = Long.MIN_VALUE;

    private long mdpsUnchangedYield = Long.MIN_VALUE;

    private long mdpsOutperformanceValue = Long.MIN_VALUE;

    private long mdpsGapStrike = Long.MIN_VALUE;

    private long mdpsLeverage = Long.MIN_VALUE;

    private long mdpsGapBarrier = Long.MIN_VALUE;

    private long mdpsGapBarrierRelative = Long.MIN_VALUE;

    private long qid = Long.MIN_VALUE;

    private int dateFirstPrice = Integer.MIN_VALUE;

    private long externalReferenceTimestamp = Long.MIN_VALUE;

    private long mdpsDateBarrierReached = Long.MIN_VALUE;

    private long volatilityAlltime = Long.MIN_VALUE;

    private long sharperatioCurrentYear = Long.MIN_VALUE;

    private long sharperatioAlltime = Long.MIN_VALUE;


    public long getMostRecentUpdateTimestamp() {
        return this.mostRecentUpdateTimestamp;
    }

    public void setMostRecentUpdateTimestamp(long value) {
        this.mostRecentUpdateTimestamp = value;
    }

    public long getVolatility1m() {
        return this.volatility1m;
    }

    public void setVolatility1m(long value) {
        this.volatility1m = value;
    }

    public long getPerformance1m() {
        return this.performance1m;
    }

    public void setPerformance1m(long value) {
        this.performance1m = value;
    }

    public long getSpread() {
        return this.spread;
    }

    public void setSpread(long value) {
        this.spread = value;
    }

    public long getReferenceTimestamp() {
        return this.referenceTimestamp;
    }

    public void setReferenceTimestamp(long value) {
        this.referenceTimestamp = value;
    }

    public long getSpreadRelative() {
        return this.spreadRelative;
    }

    public void setSpreadRelative(long value) {
        this.spreadRelative = value;
    }

    public long getBid() {
        return this.bid;
    }

    public void setBid(long value) {
        this.bid = value;
    }

    public long getAsk() {
        return this.ask;
    }

    public void setAsk(long value) {
        this.ask = value;
    }

    public long getLastPrice() {
        return this.lastPrice;
    }

    public void setLastPrice(long value) {
        this.lastPrice = value;
    }

    public long getPreviousClose() {
        return this.previousClose;
    }

    public void setPreviousClose(long value) {
        this.previousClose = value;
    }

    public long getDiscountPrice() {
        return this.discountPrice;
    }

    public void setDiscountPrice(long value) {
        this.discountPrice = value;
    }

    public long getDiscountPriceRelative() {
        return this.discountPriceRelative;
    }

    public void setDiscountPriceRelative(long value) {
        this.discountPriceRelative = value;
    }

    public long getDiscountPriceRelativePerYear() {
        return this.discountPriceRelativePerYear;
    }

    public void setDiscountPriceRelativePerYear(long value) {
        this.discountPriceRelativePerYear = value;
    }

    public long getUnchangedEarning() {
        return this.unchangedEarning;
    }

    public void setUnchangedEarning(long value) {
        this.unchangedEarning = value;
    }

    public long getUnchangedEarningRelative() {
        return this.unchangedEarningRelative;
    }

    public void setUnchangedEarningRelative(long value) {
        this.unchangedEarningRelative = value;
    }

    public long getUnchangedEarningRelativePerYear() {
        return this.unchangedEarningRelativePerYear;
    }

    public void setUnchangedEarningRelativePerYear(long value) {
        this.unchangedEarningRelativePerYear = value;
    }

    public long getBidVolume() {
        return this.bidVolume;
    }

    public void setBidVolume(long value) {
        this.bidVolume = value;
    }

    public long getAskVolume() {
        return this.askVolume;
    }

    public void setAskVolume(long value) {
        this.askVolume = value;
    }

    public long getCapToUnderlying() {
        return this.capToUnderlying;
    }

    public void setCapToUnderlying(long value) {
        this.capToUnderlying = value;
    }

    public long getCapToUnderlyingRelative() {
        return this.capToUnderlyingRelative;
    }

    public void setCapToUnderlyingRelative(long value) {
        this.capToUnderlyingRelative = value;
    }

    public long getUnderlyingToCap() {
        return this.underlyingToCap;
    }

    public void setUnderlyingToCap(long value) {
        this.underlyingToCap = value;
    }

    public long getUnderlyingToCapRelative() {
        return this.underlyingToCapRelative;
    }

    public void setUnderlyingToCapRelative(long value) {
        this.underlyingToCapRelative = value;
    }

    public int getBidAskDate() {
        return this.bidAskDate;
    }

    public void setBidAskDate(int value) {
        this.bidAskDate = value;
    }

    public int getBidAskTime() {
        return this.bidAskTime;
    }

    public void setBidAskTime(int value) {
        this.bidAskTime = value;
    }

    public long getTradeVolume() {
        return this.tradeVolume;
    }

    public void setTradeVolume(long value) {
        this.tradeVolume = value;
    }

    public int getLastDate() {
        return this.lastDate;
    }

    public void setLastDate(int value) {
        this.lastDate = value;
    }

    public int getLastTime() {
        return this.lastTime;
    }

    public void setLastTime(int value) {
        this.lastTime = value;
    }

    public long getHigh() {
        return this.high;
    }

    public void setHigh(long value) {
        this.high = value;
    }

    public long getLow() {
        return this.low;
    }

    public void setLow(long value) {
        this.low = value;
    }

    public long getHighYear() {
        return this.highYear;
    }

    public void setHighYear(long value) {
        this.highYear = value;
    }

    public long getLowYear() {
        return this.lowYear;
    }

    public void setLowYear(long value) {
        this.lowYear = value;
    }

    public long getTotalVolume() {
        return this.totalVolume;
    }

    public void setTotalVolume(long value) {
        this.totalVolume = value;
    }

    public int getPreviousDate() {
        return this.previousDate;
    }

    public void setPreviousDate(int value) {
        this.previousDate = value;
    }

    public long getOpen() {
        return this.open;
    }

    public void setOpen(long value) {
        this.open = value;
    }

    public long getUnderlyingLastPrice() {
        return this.underlyingLastPrice;
    }

    public void setUnderlyingLastPrice(long value) {
        this.underlyingLastPrice = value;
    }

    public long getUnderlyingPreviousClose() {
        return this.underlyingPreviousClose;
    }

    public void setUnderlyingPreviousClose(long value) {
        this.underlyingPreviousClose = value;
    }

    public long getUnderlyingTradeVolume() {
        return this.underlyingTradeVolume;
    }

    public void setUnderlyingTradeVolume(long value) {
        this.underlyingTradeVolume = value;
    }

    public int getUnderlyingLastDate() {
        return this.underlyingLastDate;
    }

    public void setUnderlyingLastDate(int value) {
        this.underlyingLastDate = value;
    }

    public int getUnderlyingLastTime() {
        return this.underlyingLastTime;
    }

    public void setUnderlyingLastTime(int value) {
        this.underlyingLastTime = value;
    }

    public long getUnderlyingTotalVolume() {
        return this.underlyingTotalVolume;
    }

    public void setUnderlyingTotalVolume(long value) {
        this.underlyingTotalVolume = value;
    }

    public int getUnderlyingPreviousDate() {
        return this.underlyingPreviousDate;
    }

    public void setUnderlyingPreviousDate(int value) {
        this.underlyingPreviousDate = value;
    }

    public long getIssuePrice() {
        return this.issuePrice;
    }

    public void setIssuePrice(long value) {
        this.issuePrice = value;
    }

    public long getVolatility3m() {
        return this.volatility3m;
    }

    public void setVolatility3m(long value) {
        this.volatility3m = value;
    }

    public long getPerformance3m() {
        return this.performance3m;
    }

    public void setPerformance3m(long value) {
        this.performance3m = value;
    }

    public long getPerformance1y() {
        return this.performance1y;
    }

    public void setPerformance1y(long value) {
        this.performance1y = value;
    }

    public long getCapLevel() {
        return this.capLevel;
    }

    public void setCapLevel(long value) {
        this.capLevel = value;
    }

    public long getYieldRelativePerYear() {
        return this.yieldRelativePerYear;
    }

    public void setYieldRelativePerYear(long value) {
        this.yieldRelativePerYear = value;
    }

    public long getVolatility10y() {
        return this.volatility10y;
    }

    public void setVolatility10y(long value) {
        this.volatility10y = value;
    }

    public long getVolatility1y() {
        return this.volatility1y;
    }

    public void setVolatility1y(long value) {
        this.volatility1y = value;
    }

    public long getSharperatio1y() {
        return this.sharperatio1y;
    }

    public void setSharperatio1y(long value) {
        this.sharperatio1y = value;
    }

    public long getSharperatio3y() {
        return this.sharperatio3y;
    }

    public void setSharperatio3y(long value) {
        this.sharperatio3y = value;
    }

    public long getLeverage() {
        return this.leverage;
    }

    public void setLeverage(long value) {
        this.leverage = value;
    }

    public long getPerformance10y() {
        return this.performance10y;
    }

    public void setPerformance10y(long value) {
        this.performance10y = value;
    }

    public long getPerformance1d() {
        return this.performance1d;
    }

    public void setPerformance1d(long value) {
        this.performance1d = value;
    }

    public long getAveragevolume1m() {
        return this.averagevolume1m;
    }

    public void setAveragevolume1m(long value) {
        this.averagevolume1m = value;
    }

    public long getAveragevolume1y() {
        return this.averagevolume1y;
    }

    public void setAveragevolume1y(long value) {
        this.averagevolume1y = value;
    }

    public long getPerformance6m() {
        return this.performance6m;
    }

    public void setPerformance6m(long value) {
        this.performance6m = value;
    }

    public long getPerformance3y() {
        return this.performance3y;
    }

    public void setPerformance3y(long value) {
        this.performance3y = value;
    }

    public long getPerformance5y() {
        return this.performance5y;
    }

    public void setPerformance5y(long value) {
        this.performance5y = value;
    }

    public long getVolatility6m() {
        return this.volatility6m;
    }

    public void setVolatility6m(long value) {
        this.volatility6m = value;
    }

    public long getVolatility5y() {
        return this.volatility5y;
    }

    public void setVolatility5y(long value) {
        this.volatility5y = value;
    }

    public long getSharperatio5y() {
        return this.sharperatio5y;
    }

    public void setSharperatio5y(long value) {
        this.sharperatio5y = value;
    }

    public long getSharperatio1m() {
        return this.sharperatio1m;
    }

    public void setSharperatio1m(long value) {
        this.sharperatio1m = value;
    }

    public long getSharperatio3m() {
        return this.sharperatio3m;
    }

    public void setSharperatio3m(long value) {
        this.sharperatio3m = value;
    }

    public long getSharperatio6m() {
        return this.sharperatio6m;
    }

    public void setSharperatio6m(long value) {
        this.sharperatio6m = value;
    }

    public long getUnderlyinglow1y() {
        return this.underlyinglow1y;
    }

    public void setUnderlyinglow1y(long value) {
        this.underlyinglow1y = value;
    }

    public long getUnderlyinghigh1y() {
        return this.underlyinghigh1y;
    }

    public void setUnderlyinghigh1y(long value) {
        this.underlyinghigh1y = value;
    }

    public String getVwdCode() {
        return this.vwdCode;
    }

    public void setVwdCode(String value) {
        this.vwdCode = value;
    }

    public String getMmwkn() {
        return this.mmwkn;
    }

    public void setMmwkn(String value) {
        this.mmwkn = value;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String value) {
        this.currency = value;
    }

    public long getHighAlltime() {
        return this.highAlltime;
    }

    public void setHighAlltime(long value) {
        this.highAlltime = value;
    }

    public long getLowAlltime() {
        return this.lowAlltime;
    }

    public void setLowAlltime(long value) {
        this.lowAlltime = value;
    }

    public int getHighAlltimeDate() {
        return this.highAlltimeDate;
    }

    public void setHighAlltimeDate(int value) {
        this.highAlltimeDate = value;
    }

    public int getLowAlltimeDate() {
        return this.lowAlltimeDate;
    }

    public void setLowAlltimeDate(int value) {
        this.lowAlltimeDate = value;
    }

    public int getHigh1yDate() {
        return this.high1yDate;
    }

    public void setHigh1yDate(int value) {
        this.high1yDate = value;
    }

    public int getLow1yDate() {
        return this.low1yDate;
    }

    public void setLow1yDate(int value) {
        this.low1yDate = value;
    }

    public long getAveragePrice1w() {
        return this.averagePrice1w;
    }

    public void setAveragePrice1w(long value) {
        this.averagePrice1w = value;
    }

    public long getAveragePrice1m() {
        return this.averagePrice1m;
    }

    public void setAveragePrice1m(long value) {
        this.averagePrice1m = value;
    }

    public long getAveragePrice3m() {
        return this.averagePrice3m;
    }

    public void setAveragePrice3m(long value) {
        this.averagePrice3m = value;
    }

    public long getAveragePrice6m() {
        return this.averagePrice6m;
    }

    public void setAveragePrice6m(long value) {
        this.averagePrice6m = value;
    }

    public long getAveragePrice1y() {
        return this.averagePrice1y;
    }

    public void setAveragePrice1y(long value) {
        this.averagePrice1y = value;
    }

    public long getAveragePrice3y() {
        return this.averagePrice3y;
    }

    public void setAveragePrice3y(long value) {
        this.averagePrice3y = value;
    }

    public long getAveragePrice5y() {
        return this.averagePrice5y;
    }

    public void setAveragePrice5y(long value) {
        this.averagePrice5y = value;
    }

    public long getAveragePrice10y() {
        return this.averagePrice10y;
    }

    public void setAveragePrice10y(long value) {
        this.averagePrice10y = value;
    }

    public long getChangeNet() {
        return this.changeNet;
    }

    public void setChangeNet(long value) {
        this.changeNet = value;
    }

    public long getChangePercent() {
        return this.changePercent;
    }

    public void setChangePercent(long value) {
        this.changePercent = value;
    }

    public long getVolatility3y() {
        return this.volatility3y;
    }

    public void setVolatility3y(long value) {
        this.volatility3y = value;
    }

    public long getPerformance1w() {
        return this.performance1w;
    }

    public void setPerformance1w(long value) {
        this.performance1w = value;
    }

    public long getHigh1y() {
        return this.high1y;
    }

    public void setHigh1y(long value) {
        this.high1y = value;
    }

    public long getLow1y() {
        return this.low1y;
    }

    public void setLow1y(long value) {
        this.low1y = value;
    }

    public long getAveragevolume1w() {
        return this.averagevolume1w;
    }

    public void setAveragevolume1w(long value) {
        this.averagevolume1w = value;
    }

    public long getAveragevolume3m() {
        return this.averagevolume3m;
    }

    public void setAveragevolume3m(long value) {
        this.averagevolume3m = value;
    }

    public long getAveragevolume6m() {
        return this.averagevolume6m;
    }

    public void setAveragevolume6m(long value) {
        this.averagevolume6m = value;
    }

    public long getAveragevolume3y() {
        return this.averagevolume3y;
    }

    public void setAveragevolume3y(long value) {
        this.averagevolume3y = value;
    }

    public long getAveragevolume5y() {
        return this.averagevolume5y;
    }

    public void setAveragevolume5y(long value) {
        this.averagevolume5y = value;
    }

    public long getAveragevolume10y() {
        return this.averagevolume10y;
    }

    public void setAveragevolume10y(long value) {
        this.averagevolume10y = value;
    }

    public String getMarketmanagerName() {
        return this.marketmanagerName;
    }

    public void setMarketmanagerName(String value) {
        this.marketmanagerName = value;
    }

    public long getVolatilityCurrentYear() {
        return this.volatilityCurrentYear;
    }

    public void setVolatilityCurrentYear(long value) {
        this.volatilityCurrentYear = value;
    }

    public long getMdpsbrokenperiodinterest() {
        return this.mdpsbrokenperiodinterest;
    }

    public void setMdpsbrokenperiodinterest(long value) {
        this.mdpsbrokenperiodinterest = value;
    }

    public long getPerformanceAlltime() {
        return this.performanceAlltime;
    }

    public void setPerformanceAlltime(long value) {
        this.performanceAlltime = value;
    }

    public long getSharperatio1w() {
        return this.sharperatio1w;
    }

    public void setSharperatio1w(long value) {
        this.sharperatio1w = value;
    }

    public long getSharperatio10y() {
        return this.sharperatio10y;
    }

    public void setSharperatio10y(long value) {
        this.sharperatio10y = value;
    }

    public long getReferencePrice() {
        return this.referencePrice;
    }

    public void setReferencePrice(long value) {
        this.referencePrice = value;
    }

    public String getVwdMarket() {
        return this.vwdMarket;
    }

    public void setVwdMarket(String value) {
        this.vwdMarket = value;
    }

    public long getPerformanceCurrentYear() {
        return this.performanceCurrentYear;
    }

    public void setPerformanceCurrentYear(long value) {
        this.performanceCurrentYear = value;
    }

    public long getMdpsDiscount() {
        return this.mdpsDiscount;
    }

    public void setMdpsDiscount(long value) {
        this.mdpsDiscount = value;
    }

    public long getMdpsDiscountRelative() {
        return this.mdpsDiscountRelative;
    }

    public void setMdpsDiscountRelative(long value) {
        this.mdpsDiscountRelative = value;
    }

    public long getMdpsYield() {
        return this.mdpsYield;
    }

    public void setMdpsYield(long value) {
        this.mdpsYield = value;
    }

    public long getMdpsYieldRelative() {
        return this.mdpsYieldRelative;
    }

    public void setMdpsYieldRelative(long value) {
        this.mdpsYieldRelative = value;
    }

    public long getMdpsYieldRelativePerYear() {
        return this.mdpsYieldRelativePerYear;
    }

    public void setMdpsYieldRelativePerYear(long value) {
        this.mdpsYieldRelativePerYear = value;
    }

    public long getMdpsGapCap() {
        return this.mdpsGapCap;
    }

    public void setMdpsGapCap(long value) {
        this.mdpsGapCap = value;
    }

    public long getMdpsGapCapRelative() {
        return this.mdpsGapCapRelative;
    }

    public void setMdpsGapCapRelative(long value) {
        this.mdpsGapCapRelative = value;
    }

    public long getMdpsMaximumYield() {
        return this.mdpsMaximumYield;
    }

    public void setMdpsMaximumYield(long value) {
        this.mdpsMaximumYield = value;
    }

    public long getMdpsMaximumYieldRelative() {
        return this.mdpsMaximumYieldRelative;
    }

    public void setMdpsMaximumYieldRelative(long value) {
        this.mdpsMaximumYieldRelative = value;
    }

    public long getMdpsMaximumYieldRelativePerYear() {
        return this.mdpsMaximumYieldRelativePerYear;
    }

    public void setMdpsMaximumYieldRelativePerYear(long value) {
        this.mdpsMaximumYieldRelativePerYear = value;
    }

    public long getMdpsUnchangedYieldRelative() {
        return this.mdpsUnchangedYieldRelative;
    }

    public void setMdpsUnchangedYieldRelative(long value) {
        this.mdpsUnchangedYieldRelative = value;
    }

    public long getMdpsUnchangedYieldRelativePerYear() {
        return this.mdpsUnchangedYieldRelativePerYear;
    }

    public void setMdpsUnchangedYieldRelativePerYear(long value) {
        this.mdpsUnchangedYieldRelativePerYear = value;
    }

    public long getMdpsGapBonusLevelRelative() {
        return this.mdpsGapBonusLevelRelative;
    }

    public void setMdpsGapBonusLevelRelative(long value) {
        this.mdpsGapBonusLevelRelative = value;
    }

    public long getMdpsGapBonusBufferRelative() {
        return this.mdpsGapBonusBufferRelative;
    }

    public void setMdpsGapBonusBufferRelative(long value) {
        this.mdpsGapBonusBufferRelative = value;
    }

    public long getMdpsAgioRelative() {
        return this.mdpsAgioRelative;
    }

    public void setMdpsAgioRelative(long value) {
        this.mdpsAgioRelative = value;
    }

    public long getMdpsAgioRelativePerYear() {
        return this.mdpsAgioRelativePerYear;
    }

    public void setMdpsAgioRelativePerYear(long value) {
        this.mdpsAgioRelativePerYear = value;
    }

    public long getMdpsGapLowerBarrier() {
        return this.mdpsGapLowerBarrier;
    }

    public void setMdpsGapLowerBarrier(long value) {
        this.mdpsGapLowerBarrier = value;
    }

    public long getMdpsGapLowerBarrierRelative() {
        return this.mdpsGapLowerBarrierRelative;
    }

    public void setMdpsGapLowerBarrierRelative(long value) {
        this.mdpsGapLowerBarrierRelative = value;
    }

    public long getMdpsGapUpperBarrier() {
        return this.mdpsGapUpperBarrier;
    }

    public void setMdpsGapUpperBarrier(long value) {
        this.mdpsGapUpperBarrier = value;
    }

    public long getMdpsGapUpperBarrierRelative() {
        return this.mdpsGapUpperBarrierRelative;
    }

    public void setMdpsGapUpperBarrierRelative(long value) {
        this.mdpsGapUpperBarrierRelative = value;
    }

    public long getMdpsUnderlyingToCapRelative() {
        return this.mdpsUnderlyingToCapRelative;
    }

    public void setMdpsUnderlyingToCapRelative(long value) {
        this.mdpsUnderlyingToCapRelative = value;
    }

    public long getMdpsCapToUnderlyingRelative() {
        return this.mdpsCapToUnderlyingRelative;
    }

    public void setMdpsCapToUnderlyingRelative(long value) {
        this.mdpsCapToUnderlyingRelative = value;
    }

    public long getMdpsGapStrikeRelative() {
        return this.mdpsGapStrikeRelative;
    }

    public void setMdpsGapStrikeRelative(long value) {
        this.mdpsGapStrikeRelative = value;
    }

    public long getMdpsGapBonusLevel() {
        return this.mdpsGapBonusLevel;
    }

    public void setMdpsGapBonusLevel(long value) {
        this.mdpsGapBonusLevel = value;
    }

    public long getMdpsPerformanceAlltime() {
        return this.mdpsPerformanceAlltime;
    }

    public void setMdpsPerformanceAlltime(long value) {
        this.mdpsPerformanceAlltime = value;
    }

    public long getMdpsAgio() {
        return this.mdpsAgio;
    }

    public void setMdpsAgio(long value) {
        this.mdpsAgio = value;
    }

    public long getMdpsUnchangedYield() {
        return this.mdpsUnchangedYield;
    }

    public void setMdpsUnchangedYield(long value) {
        this.mdpsUnchangedYield = value;
    }

    public long getMdpsOutperformanceValue() {
        return this.mdpsOutperformanceValue;
    }

    public void setMdpsOutperformanceValue(long value) {
        this.mdpsOutperformanceValue = value;
    }

    public long getMdpsGapStrike() {
        return this.mdpsGapStrike;
    }

    public void setMdpsGapStrike(long value) {
        this.mdpsGapStrike = value;
    }

    public long getMdpsLeverage() {
        return this.mdpsLeverage;
    }

    public void setMdpsLeverage(long value) {
        this.mdpsLeverage = value;
    }

    public long getMdpsGapBarrier() {
        return this.mdpsGapBarrier;
    }

    public void setMdpsGapBarrier(long value) {
        this.mdpsGapBarrier = value;
    }

    public long getMdpsGapBarrierRelative() {
        return this.mdpsGapBarrierRelative;
    }

    public void setMdpsGapBarrierRelative(long value) {
        this.mdpsGapBarrierRelative = value;
    }

    public long getQid() {
        return this.qid;
    }

    public void setQid(long value) {
        this.qid = value;
    }

    public int getDateFirstPrice() {
        return this.dateFirstPrice;
    }

    public void setDateFirstPrice(int value) {
        this.dateFirstPrice = value;
    }

    public long getExternalReferenceTimestamp() {
        return this.externalReferenceTimestamp;
    }

    public void setExternalReferenceTimestamp(long value) {
        this.externalReferenceTimestamp = value;
    }

    public long getMdpsDateBarrierReached() {
        return this.mdpsDateBarrierReached;
    }

    public void setMdpsDateBarrierReached(long value) {
        this.mdpsDateBarrierReached = value;
    }

    public long getVolatilityAlltime() {
        return this.volatilityAlltime;
    }

    public void setVolatilityAlltime(long value) {
        this.volatilityAlltime = value;
    }

    public long getSharperatioCurrentYear() {
        return this.sharperatioCurrentYear;
    }

    public void setSharperatioCurrentYear(long value) {
        this.sharperatioCurrentYear = value;
    }

    public long getSharperatioAlltime() {
        return this.sharperatioAlltime;
    }

    public void setSharperatioAlltime(long value) {
        this.sharperatioAlltime = value;
    }


    // Selectable Interface ----------------------------------------

    public String getString(int fieldid) {
        PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            return ps.getString(this);
        }
        ps = propertySupport4Instrument(fieldid);
        return ps != null ? ps.getString(this.getInstrumentRatios()) : null;
    }

    public String getString(int fieldid, int localeIndex) {
        PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            return ps.getString(this, localeIndex);
        }
        ps = propertySupport4Instrument(fieldid);
        return ps != null ? ps.getString(this.getInstrumentRatios(), localeIndex) : null;
    }

    public Boolean getBoolean(int fieldid) {
        PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            return ps.getBoolean(this);
        }
        ps = propertySupport4Instrument(fieldid);
        return ps != null && ps.getBoolean(this.getInstrumentRatios());
    }

    public Long getLong(int fieldid) {
        PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            final long result = ps.getLong(this);
            return result == Long.MIN_VALUE ? null : result;
        }
        ps = propertySupport4Instrument(fieldid);
        final long result = ps != null ? ps.getLong(this.getInstrumentRatios()) : Long.MIN_VALUE;
        return result == Long.MIN_VALUE ? null : result;
    }

    @Override
    public BitSet getBitSet(int fieldid) {
        PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            final BitSet result = ps.getBitSet(this);
            return result.isEmpty() ? null : result;
        }
        ps = propertySupport4Instrument(fieldid);
        return ps != null ? ps.getBitSet(this.getInstrumentRatios()) : null;
    }

    public Integer getInt(int fieldid) {
        PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            final int result = ps.getInt(this);
            return result == Integer.MIN_VALUE ? null : result;
        }
        ps = propertySupport4Instrument(fieldid);
        final int result = ps != null ? ps.getInt(this.getInstrumentRatios()) : Integer.MIN_VALUE;
        return result == Integer.MIN_VALUE ? null : result;
    }

    // methods for access by fieldname (velocity) ----------------------------------------

    public String getString(String fieldname) {
        final RatioFieldDescription.Field field = getFieldByName(fieldname);
        if (field == null) {
            return null;
        }

        if (!field.isLocalized()) {
            return getString(field.id());
        }

        // TODO: use RequestContextHolder for locale (instead of null), problem is ratios package
        final int localeIndex = RatioFieldDescription.getLocaleIndex(field, null);
        return getString(field.id(), localeIndex);
    }

    public Boolean getBoolean(String fieldname) {
        final RatioFieldDescription.Field field = getFieldByName(fieldname);
        return field != null && getBoolean(field.id());
    }

    public Long getLong(String fieldname) {
        final RatioFieldDescription.Field field = getFieldByName(fieldname);
        return (field != null) ? getLong(field.id()) : null;
    }

    public BitSet getBitSet(String fieldname) {
        final RatioFieldDescription.Field field = getFieldByName(fieldname);
        return field != null ? getBitSet(field.id()) : null;
    }

    public Integer getInt(String fieldname) {
        final RatioFieldDescription.Field field = getFieldByName(fieldname);
        return (field != null) ? getInt(field.id()) : null;
    }

    // RatioUpdatable Interface ----------------------------------------

    public void set(int fieldid, int localeIndex, String value) {
        final PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            ps.set(this, localeIndex, value);
        }
    }

    public void set(int fieldid, boolean value) {
        final PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            ps.set(this, value);
        }
    }

    public void set(int fieldid, long value) {
        final PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            ps.set(this, value);
        }
    }

    @Override
    public void set(int fieldid, BitSet value) {
        final PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            ps.set(this, value);
        }
    }

    public void set(int fieldid, int value) {
        final PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            ps.set(this, value);
        }
    }
}