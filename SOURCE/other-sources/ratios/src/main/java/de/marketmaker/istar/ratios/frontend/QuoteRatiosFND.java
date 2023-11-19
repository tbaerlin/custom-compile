package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class QuoteRatiosFND extends QuoteRatios<InstrumentRatiosFND> {
    protected static final long serialVersionUID = 1L;

    public QuoteRatiosFND(long id, InstrumentRatiosFND instrumentRatios) {
        super(id, instrumentRatios);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportFND.forQuote(fid);
    }

    public PropertySupport propertySupport4Instrument(int fid) {
        return RatiosPropertySupportFND.forInstrument(fid);
    }

    private long mostRecentUpdateTimestamp = Long.MIN_VALUE;

    private long volatility1m = Long.MIN_VALUE;

    private long spread = Long.MIN_VALUE;

    private long referenceTimestamp = Long.MIN_VALUE;

    private long spreadRelative = Long.MIN_VALUE;

    private long bid = Long.MIN_VALUE;

    private long ask = Long.MIN_VALUE;

    private long lastPrice = Long.MIN_VALUE;

    private long previousClose = Long.MIN_VALUE;

    private long bidVolume = Long.MIN_VALUE;

    private long askVolume = Long.MIN_VALUE;

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

    private long issuePrice = Long.MIN_VALUE;

    private long interimProfit = Long.MIN_VALUE;

    private int interimProfitDate = Integer.MIN_VALUE;

    private long estateProfit = Long.MIN_VALUE;

    private long stockProfit = Long.MIN_VALUE;

    private int stockProfitDate = Integer.MIN_VALUE;

    private long volatility3m = Long.MIN_VALUE;

    private long volatility10y = Long.MIN_VALUE;

    private long volatility1y = Long.MIN_VALUE;

    private long sharperatio1y = Long.MIN_VALUE;

    private long sharperatio3y = Long.MIN_VALUE;

    private long bviperformance1y = Long.MIN_VALUE;

    private long bviperformance3y = Long.MIN_VALUE;

    private long treynor1y = Long.MIN_VALUE;

    private long treynor3y = Long.MIN_VALUE;

    private long beta1y = Long.MIN_VALUE;

    private long correlation1y = Long.MIN_VALUE;

    private int dateoflast = Integer.MIN_VALUE;

    private long volatility1w = Long.MIN_VALUE;

    private long mmnetassetvalue = Long.MIN_VALUE;

    private long mmissueprice = Long.MIN_VALUE;

    private long performance1d = Long.MIN_VALUE;

    private long performancetobenchmark3m = Long.MIN_VALUE;

    private long performancetobenchmark1w = Long.MIN_VALUE;

    private long performancetobenchmark1m = Long.MIN_VALUE;

    private long performancetobenchmark1y = Long.MIN_VALUE;

    private long averagevolume1m = Long.MIN_VALUE;

    private long averagevolume1y = Long.MIN_VALUE;

    private long beta1m = Long.MIN_VALUE;

    private long performancetobenchmark6m = Long.MIN_VALUE;

    private long volatility6m = Long.MIN_VALUE;

    private long volatility5y = Long.MIN_VALUE;

    private long sharperatio5y = Long.MIN_VALUE;

    private long sharperatio1m = Long.MIN_VALUE;

    private long sharperatio3m = Long.MIN_VALUE;

    private long sharperatio6m = Long.MIN_VALUE;

    private long correlation1m = Long.MIN_VALUE;

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

    private long beta1w = Long.MIN_VALUE;

    private long beta3m = Long.MIN_VALUE;

    private long beta6m = Long.MIN_VALUE;

    private long performancetobenchmark3y = Long.MIN_VALUE;

    private long performancetobenchmark5y = Long.MIN_VALUE;

    private long performancetobenchmark10y = Long.MIN_VALUE;

    private long changeNet = Long.MIN_VALUE;

    private long changePercent = Long.MIN_VALUE;

    private long bviperformance1m = Long.MIN_VALUE;

    private long bviperformance3m = Long.MIN_VALUE;

    private long bviperformance6m = Long.MIN_VALUE;

    private long bviperformance5y = Long.MIN_VALUE;

    private long bviperformance10y = Long.MIN_VALUE;

    private long negativeMonthsPercent1m = Long.MIN_VALUE;

    private long negativeMonthsPercent3m = Long.MIN_VALUE;

    private long negativeMonthsPercent6m = Long.MIN_VALUE;

    private long negativeMonthsPercent1y = Long.MIN_VALUE;

    private long negativeMonthsPercent3y = Long.MIN_VALUE;

    private long negativeMonthsPercent5y = Long.MIN_VALUE;

    private long negativeMonthsPercent10y = Long.MIN_VALUE;

    private long volatility3y = Long.MIN_VALUE;

    private long correlation1w = Long.MIN_VALUE;

    private long correlation3m = Long.MIN_VALUE;

    private long correlation6m = Long.MIN_VALUE;

    private long correlation3y = Long.MIN_VALUE;

    private long correlation5y = Long.MIN_VALUE;

    private long correlation10y = Long.MIN_VALUE;

    private long maximumLoss1y = Long.MIN_VALUE;

    private long maximumLossDays1y = Long.MIN_VALUE;

    private long high1y = Long.MIN_VALUE;

    private long low1y = Long.MIN_VALUE;

    private long maximumLoss6m = Long.MIN_VALUE;

    private long maximumLossMonths3y = Long.MIN_VALUE;

    private long averagevolume1w = Long.MIN_VALUE;

    private long averagevolume3m = Long.MIN_VALUE;

    private long averagevolume6m = Long.MIN_VALUE;

    private long averagevolume3y = Long.MIN_VALUE;

    private long averagevolume5y = Long.MIN_VALUE;

    private long averagevolume10y = Long.MIN_VALUE;

    private String marketmanagerName = null;

    private long bviperformancecurrentyear = Long.MIN_VALUE;

    private long maximumLoss3y = Long.MIN_VALUE;

    private long volatilityCurrentYear = Long.MIN_VALUE;

    private long alpha1m = Long.MIN_VALUE;

    private long alpha1y = Long.MIN_VALUE;

    private long beta3y = Long.MIN_VALUE;

    private long beta5y = Long.MIN_VALUE;

    private long beta10y = Long.MIN_VALUE;

    private long bviperformance1w = Long.MIN_VALUE;

    private long sharperatio1w = Long.MIN_VALUE;

    private long sharperatio10y = Long.MIN_VALUE;

    private long treynor1w = Long.MIN_VALUE;

    private long treynor1m = Long.MIN_VALUE;

    private long treynor3m = Long.MIN_VALUE;

    private long treynor6m = Long.MIN_VALUE;

    private long treynor5y = Long.MIN_VALUE;

    private long treynor10y = Long.MIN_VALUE;

    private long maximumLossMonths1y = Long.MIN_VALUE;

    private long maximumLossMonths5y = Long.MIN_VALUE;

    private long maximumLossMonths10y = Long.MIN_VALUE;

    private long referencePrice = Long.MIN_VALUE;

    private long bviperformanceAlltime = Long.MIN_VALUE;

    private String vwdMarket = null;

    private long closePreviousYear = Long.MIN_VALUE;

    private int closePreviousYearDate = Integer.MIN_VALUE;

    private long closePreviousMonth = Long.MIN_VALUE;

    private int closePreviousMonthDate = Integer.MIN_VALUE;

    private long highPreviousYear = Long.MIN_VALUE;

    private int highPreviousYearDate = Integer.MIN_VALUE;

    private long lowPreviousYear = Long.MIN_VALUE;

    private int lowPreviousYearDate = Integer.MIN_VALUE;

    private long bviperformancecurrentmonth = Long.MIN_VALUE;

    private long changeNetCurrentYear = Long.MIN_VALUE;

    private long changeNetCurrentMonth = Long.MIN_VALUE;

    private long alpha1w = Long.MIN_VALUE;

    private long alpha3m = Long.MIN_VALUE;

    private long alpha6m = Long.MIN_VALUE;

    private long alpha3y = Long.MIN_VALUE;

    private long alpha5y = Long.MIN_VALUE;

    private long alpha10y = Long.MIN_VALUE;

    private long trackingError1w = Long.MIN_VALUE;

    private long trackingError1m = Long.MIN_VALUE;

    private long trackingError3m = Long.MIN_VALUE;

    private long trackingError6m = Long.MIN_VALUE;

    private long trackingError1y = Long.MIN_VALUE;

    private long trackingError3y = Long.MIN_VALUE;

    private long trackingError5y = Long.MIN_VALUE;

    private long trackingError10y = Long.MIN_VALUE;

    private long informationRatio1w = Long.MIN_VALUE;

    private long informationRatio1m = Long.MIN_VALUE;

    private long informationRatio3m = Long.MIN_VALUE;

    private long informationRatio6m = Long.MIN_VALUE;

    private long informationRatio1y = Long.MIN_VALUE;

    private long informationRatio3y = Long.MIN_VALUE;

    private long informationRatio5y = Long.MIN_VALUE;

    private long informationRatio10y = Long.MIN_VALUE;

    private long sterlingRatio1w = Long.MIN_VALUE;

    private long sterlingRatio1m = Long.MIN_VALUE;

    private long sterlingRatio3m = Long.MIN_VALUE;

    private long sterlingRatio6m = Long.MIN_VALUE;

    private long sterlingRatio1y = Long.MIN_VALUE;

    private long sterlingRatio3y = Long.MIN_VALUE;

    private long sterlingRatio5y = Long.MIN_VALUE;

    private long sterlingRatio10y = Long.MIN_VALUE;

    private long qid = Long.MIN_VALUE;

    private int dateFirstPrice = Integer.MIN_VALUE;

    private long probabilityofoutperformance3y = Long.MIN_VALUE;

    private long positiveregression3y = Long.MIN_VALUE;

    private long negativeregression3y = Long.MIN_VALUE;

    private long bviperformancelastyear = Long.MIN_VALUE;

    private long bviperformanceyearbeforelastyear = Long.MIN_VALUE;

    private long performanceSinceFundIssueDate = Long.MIN_VALUE;

    private long bviperformance2_1y = Long.MIN_VALUE;

    private long bviperformance3_2y = Long.MIN_VALUE;

    private long bviperformance4_3y = Long.MIN_VALUE;

    private long bviperformance5_4y = Long.MIN_VALUE;

    private long maSimple38 = Long.MIN_VALUE;

    private long maSimple90 = Long.MIN_VALUE;

    private long maSimple200 = Long.MIN_VALUE;

    private int reference1w = Integer.MIN_VALUE;

    private int reference1m = Integer.MIN_VALUE;

    private int reference3m = Integer.MIN_VALUE;

    private int reference6m = Integer.MIN_VALUE;

    private int reference1y = Integer.MIN_VALUE;

    private int reference3y = Integer.MIN_VALUE;

    private int reference5y = Integer.MIN_VALUE;

    private int reference10y = Integer.MIN_VALUE;

    private int referenceAlltime = Integer.MIN_VALUE;

    private long performancetobenchmarkcurrentyear = Long.MIN_VALUE;

    private long correlationcurrentyear = Long.MIN_VALUE;

    private long volatilityAlltime = Long.MIN_VALUE;

    private long betaCurrentYear = Long.MIN_VALUE;

    private long betaAlltime = Long.MIN_VALUE;

    private long sharperatioCurrentYear = Long.MIN_VALUE;

    private long sharperatioAlltime = Long.MIN_VALUE;

    private long alphaCurrentYear = Long.MIN_VALUE;

    private long alphaAlltime = Long.MIN_VALUE;

    private long treynorCurrentYear = Long.MIN_VALUE;

    private long treynorAlltime = Long.MIN_VALUE;

    private long maximumLoss5y = Long.MIN_VALUE;

    private long maximumLoss10y = Long.MIN_VALUE;

    private long maximumLossAlltime = Long.MIN_VALUE;


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

    public long getIssuePrice() {
        return this.issuePrice;
    }

    public void setIssuePrice(long value) {
        this.issuePrice = value;
    }

    public long getInterimProfit() {
        return this.interimProfit;
    }

    public void setInterimProfit(long value) {
        this.interimProfit = value;
    }

    public int getInterimProfitDate() {
        return this.interimProfitDate;
    }

    public void setInterimProfitDate(int value) {
        this.interimProfitDate = value;
    }

    public long getEstateProfit() {
        return this.estateProfit;
    }

    public void setEstateProfit(long value) {
        this.estateProfit = value;
    }

    public long getStockProfit() {
        return this.stockProfit;
    }

    public void setStockProfit(long value) {
        this.stockProfit = value;
    }

    public int getStockProfitDate() {
        return this.stockProfitDate;
    }

    public void setStockProfitDate(int value) {
        this.stockProfitDate = value;
    }

    public long getVolatility3m() {
        return this.volatility3m;
    }

    public void setVolatility3m(long value) {
        this.volatility3m = value;
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

    public long getBviperformance1y() {
        return this.bviperformance1y;
    }

    public void setBviperformance1y(long value) {
        this.bviperformance1y = value;
    }

    public long getBviperformance3y() {
        return this.bviperformance3y;
    }

    public void setBviperformance3y(long value) {
        this.bviperformance3y = value;
    }

    public long getTreynor1y() {
        return this.treynor1y;
    }

    public void setTreynor1y(long value) {
        this.treynor1y = value;
    }

    public long getTreynor3y() {
        return this.treynor3y;
    }

    public void setTreynor3y(long value) {
        this.treynor3y = value;
    }

    public long getBeta1y() {
        return this.beta1y;
    }

    public void setBeta1y(long value) {
        this.beta1y = value;
    }

    public long getCorrelation1y() {
        return this.correlation1y;
    }

    public void setCorrelation1y(long value) {
        this.correlation1y = value;
    }

    public int getDateoflast() {
        return this.dateoflast;
    }

    public void setDateoflast(int value) {
        this.dateoflast = value;
    }

    public long getVolatility1w() {
        return this.volatility1w;
    }

    public void setVolatility1w(long value) {
        this.volatility1w = value;
    }

    public long getMmnetassetvalue() {
        return this.mmnetassetvalue;
    }

    public void setMmnetassetvalue(long value) {
        this.mmnetassetvalue = value;
    }

    public long getMmissueprice() {
        return this.mmissueprice;
    }

    public void setMmissueprice(long value) {
        this.mmissueprice = value;
    }

    public long getPerformance1d() {
        return this.performance1d;
    }

    public void setPerformance1d(long value) {
        this.performance1d = value;
    }

    public long getPerformancetobenchmark3m() {
        return this.performancetobenchmark3m;
    }

    public void setPerformancetobenchmark3m(long value) {
        this.performancetobenchmark3m = value;
    }

    public long getPerformancetobenchmark1w() {
        return this.performancetobenchmark1w;
    }

    public void setPerformancetobenchmark1w(long value) {
        this.performancetobenchmark1w = value;
    }

    public long getPerformancetobenchmark1m() {
        return this.performancetobenchmark1m;
    }

    public void setPerformancetobenchmark1m(long value) {
        this.performancetobenchmark1m = value;
    }

    public long getPerformancetobenchmark1y() {
        return this.performancetobenchmark1y;
    }

    public void setPerformancetobenchmark1y(long value) {
        this.performancetobenchmark1y = value;
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

    public long getBeta1m() {
        return this.beta1m;
    }

    public void setBeta1m(long value) {
        this.beta1m = value;
    }

    public long getPerformancetobenchmark6m() {
        return this.performancetobenchmark6m;
    }

    public void setPerformancetobenchmark6m(long value) {
        this.performancetobenchmark6m = value;
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

    public long getCorrelation1m() {
        return this.correlation1m;
    }

    public void setCorrelation1m(long value) {
        this.correlation1m = value;
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

    public long getBeta1w() {
        return this.beta1w;
    }

    public void setBeta1w(long value) {
        this.beta1w = value;
    }

    public long getBeta3m() {
        return this.beta3m;
    }

    public void setBeta3m(long value) {
        this.beta3m = value;
    }

    public long getBeta6m() {
        return this.beta6m;
    }

    public void setBeta6m(long value) {
        this.beta6m = value;
    }

    public long getPerformancetobenchmark3y() {
        return this.performancetobenchmark3y;
    }

    public void setPerformancetobenchmark3y(long value) {
        this.performancetobenchmark3y = value;
    }

    public long getPerformancetobenchmark5y() {
        return this.performancetobenchmark5y;
    }

    public void setPerformancetobenchmark5y(long value) {
        this.performancetobenchmark5y = value;
    }

    public long getPerformancetobenchmark10y() {
        return this.performancetobenchmark10y;
    }

    public void setPerformancetobenchmark10y(long value) {
        this.performancetobenchmark10y = value;
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

    public long getBviperformance1m() {
        return this.bviperformance1m;
    }

    public void setBviperformance1m(long value) {
        this.bviperformance1m = value;
    }

    public long getBviperformance3m() {
        return this.bviperformance3m;
    }

    public void setBviperformance3m(long value) {
        this.bviperformance3m = value;
    }

    public long getBviperformance6m() {
        return this.bviperformance6m;
    }

    public void setBviperformance6m(long value) {
        this.bviperformance6m = value;
    }

    public long getBviperformance5y() {
        return this.bviperformance5y;
    }

    public void setBviperformance5y(long value) {
        this.bviperformance5y = value;
    }

    public long getBviperformance10y() {
        return this.bviperformance10y;
    }

    public void setBviperformance10y(long value) {
        this.bviperformance10y = value;
    }

    public long getNegativeMonthsPercent1m() {
        return this.negativeMonthsPercent1m;
    }

    public void setNegativeMonthsPercent1m(long value) {
        this.negativeMonthsPercent1m = value;
    }

    public long getNegativeMonthsPercent3m() {
        return this.negativeMonthsPercent3m;
    }

    public void setNegativeMonthsPercent3m(long value) {
        this.negativeMonthsPercent3m = value;
    }

    public long getNegativeMonthsPercent6m() {
        return this.negativeMonthsPercent6m;
    }

    public void setNegativeMonthsPercent6m(long value) {
        this.negativeMonthsPercent6m = value;
    }

    public long getNegativeMonthsPercent1y() {
        return this.negativeMonthsPercent1y;
    }

    public void setNegativeMonthsPercent1y(long value) {
        this.negativeMonthsPercent1y = value;
    }

    public long getNegativeMonthsPercent3y() {
        return this.negativeMonthsPercent3y;
    }

    public void setNegativeMonthsPercent3y(long value) {
        this.negativeMonthsPercent3y = value;
    }

    public long getNegativeMonthsPercent5y() {
        return this.negativeMonthsPercent5y;
    }

    public void setNegativeMonthsPercent5y(long value) {
        this.negativeMonthsPercent5y = value;
    }

    public long getNegativeMonthsPercent10y() {
        return this.negativeMonthsPercent10y;
    }

    public void setNegativeMonthsPercent10y(long value) {
        this.negativeMonthsPercent10y = value;
    }

    public long getVolatility3y() {
        return this.volatility3y;
    }

    public void setVolatility3y(long value) {
        this.volatility3y = value;
    }

    public long getCorrelation1w() {
        return this.correlation1w;
    }

    public void setCorrelation1w(long value) {
        this.correlation1w = value;
    }

    public long getCorrelation3m() {
        return this.correlation3m;
    }

    public void setCorrelation3m(long value) {
        this.correlation3m = value;
    }

    public long getCorrelation6m() {
        return this.correlation6m;
    }

    public void setCorrelation6m(long value) {
        this.correlation6m = value;
    }

    public long getCorrelation3y() {
        return this.correlation3y;
    }

    public void setCorrelation3y(long value) {
        this.correlation3y = value;
    }

    public long getCorrelation5y() {
        return this.correlation5y;
    }

    public void setCorrelation5y(long value) {
        this.correlation5y = value;
    }

    public long getCorrelation10y() {
        return this.correlation10y;
    }

    public void setCorrelation10y(long value) {
        this.correlation10y = value;
    }

    public long getMaximumLoss1y() {
        return this.maximumLoss1y;
    }

    public void setMaximumLoss1y(long value) {
        this.maximumLoss1y = value;
    }

    public long getMaximumLossDays1y() {
        return this.maximumLossDays1y;
    }

    public void setMaximumLossDays1y(long value) {
        this.maximumLossDays1y = value;
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

    public long getMaximumLoss6m() {
        return this.maximumLoss6m;
    }

    public void setMaximumLoss6m(long value) {
        this.maximumLoss6m = value;
    }

    public long getMaximumLossMonths3y() {
        return this.maximumLossMonths3y;
    }

    public void setMaximumLossMonths3y(long value) {
        this.maximumLossMonths3y = value;
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

    public long getBviperformancecurrentyear() {
        return this.bviperformancecurrentyear;
    }

    public void setBviperformancecurrentyear(long value) {
        this.bviperformancecurrentyear = value;
    }

    public long getMaximumLoss3y() {
        return this.maximumLoss3y;
    }

    public void setMaximumLoss3y(long value) {
        this.maximumLoss3y = value;
    }

    public long getVolatilityCurrentYear() {
        return this.volatilityCurrentYear;
    }

    public void setVolatilityCurrentYear(long value) {
        this.volatilityCurrentYear = value;
    }

    public long getAlpha1m() {
        return this.alpha1m;
    }

    public void setAlpha1m(long value) {
        this.alpha1m = value;
    }

    public long getAlpha1y() {
        return this.alpha1y;
    }

    public void setAlpha1y(long value) {
        this.alpha1y = value;
    }

    public long getBeta3y() {
        return this.beta3y;
    }

    public void setBeta3y(long value) {
        this.beta3y = value;
    }

    public long getBeta5y() {
        return this.beta5y;
    }

    public void setBeta5y(long value) {
        this.beta5y = value;
    }

    public long getBeta10y() {
        return this.beta10y;
    }

    public void setBeta10y(long value) {
        this.beta10y = value;
    }

    public long getBviperformance1w() {
        return this.bviperformance1w;
    }

    public void setBviperformance1w(long value) {
        this.bviperformance1w = value;
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

    public long getTreynor1w() {
        return this.treynor1w;
    }

    public void setTreynor1w(long value) {
        this.treynor1w = value;
    }

    public long getTreynor1m() {
        return this.treynor1m;
    }

    public void setTreynor1m(long value) {
        this.treynor1m = value;
    }

    public long getTreynor3m() {
        return this.treynor3m;
    }

    public void setTreynor3m(long value) {
        this.treynor3m = value;
    }

    public long getTreynor6m() {
        return this.treynor6m;
    }

    public void setTreynor6m(long value) {
        this.treynor6m = value;
    }

    public long getTreynor5y() {
        return this.treynor5y;
    }

    public void setTreynor5y(long value) {
        this.treynor5y = value;
    }

    public long getTreynor10y() {
        return this.treynor10y;
    }

    public void setTreynor10y(long value) {
        this.treynor10y = value;
    }

    public long getMaximumLossMonths1y() {
        return this.maximumLossMonths1y;
    }

    public void setMaximumLossMonths1y(long value) {
        this.maximumLossMonths1y = value;
    }

    public long getMaximumLossMonths5y() {
        return this.maximumLossMonths5y;
    }

    public void setMaximumLossMonths5y(long value) {
        this.maximumLossMonths5y = value;
    }

    public long getMaximumLossMonths10y() {
        return this.maximumLossMonths10y;
    }

    public void setMaximumLossMonths10y(long value) {
        this.maximumLossMonths10y = value;
    }

    public long getReferencePrice() {
        return this.referencePrice;
    }

    public void setReferencePrice(long value) {
        this.referencePrice = value;
    }

    public long getBviperformanceAlltime() {
        return this.bviperformanceAlltime;
    }

    public void setBviperformanceAlltime(long value) {
        this.bviperformanceAlltime = value;
    }

    public String getVwdMarket() {
        return this.vwdMarket;
    }

    public void setVwdMarket(String value) {
        this.vwdMarket = value;
    }

    public long getClosePreviousYear() {
        return this.closePreviousYear;
    }

    public void setClosePreviousYear(long value) {
        this.closePreviousYear = value;
    }

    public int getClosePreviousYearDate() {
        return this.closePreviousYearDate;
    }

    public void setClosePreviousYearDate(int value) {
        this.closePreviousYearDate = value;
    }

    public long getClosePreviousMonth() {
        return this.closePreviousMonth;
    }

    public void setClosePreviousMonth(long value) {
        this.closePreviousMonth = value;
    }

    public int getClosePreviousMonthDate() {
        return this.closePreviousMonthDate;
    }

    public void setClosePreviousMonthDate(int value) {
        this.closePreviousMonthDate = value;
    }

    public long getHighPreviousYear() {
        return this.highPreviousYear;
    }

    public void setHighPreviousYear(long value) {
        this.highPreviousYear = value;
    }

    public int getHighPreviousYearDate() {
        return this.highPreviousYearDate;
    }

    public void setHighPreviousYearDate(int value) {
        this.highPreviousYearDate = value;
    }

    public long getLowPreviousYear() {
        return this.lowPreviousYear;
    }

    public void setLowPreviousYear(long value) {
        this.lowPreviousYear = value;
    }

    public int getLowPreviousYearDate() {
        return this.lowPreviousYearDate;
    }

    public void setLowPreviousYearDate(int value) {
        this.lowPreviousYearDate = value;
    }

    public long getBviperformancecurrentmonth() {
        return this.bviperformancecurrentmonth;
    }

    public void setBviperformancecurrentmonth(long value) {
        this.bviperformancecurrentmonth = value;
    }

    public long getChangeNetCurrentYear() {
        return this.changeNetCurrentYear;
    }

    public void setChangeNetCurrentYear(long value) {
        this.changeNetCurrentYear = value;
    }

    public long getChangeNetCurrentMonth() {
        return this.changeNetCurrentMonth;
    }

    public void setChangeNetCurrentMonth(long value) {
        this.changeNetCurrentMonth = value;
    }

    public long getAlpha1w() {
        return this.alpha1w;
    }

    public void setAlpha1w(long value) {
        this.alpha1w = value;
    }

    public long getAlpha3m() {
        return this.alpha3m;
    }

    public void setAlpha3m(long value) {
        this.alpha3m = value;
    }

    public long getAlpha6m() {
        return this.alpha6m;
    }

    public void setAlpha6m(long value) {
        this.alpha6m = value;
    }

    public long getAlpha3y() {
        return this.alpha3y;
    }

    public void setAlpha3y(long value) {
        this.alpha3y = value;
    }

    public long getAlpha5y() {
        return this.alpha5y;
    }

    public void setAlpha5y(long value) {
        this.alpha5y = value;
    }

    public long getAlpha10y() {
        return this.alpha10y;
    }

    public void setAlpha10y(long value) {
        this.alpha10y = value;
    }

    public long getTrackingError1w() {
        return this.trackingError1w;
    }

    public void setTrackingError1w(long value) {
        this.trackingError1w = value;
    }

    public long getTrackingError1m() {
        return this.trackingError1m;
    }

    public void setTrackingError1m(long value) {
        this.trackingError1m = value;
    }

    public long getTrackingError3m() {
        return this.trackingError3m;
    }

    public void setTrackingError3m(long value) {
        this.trackingError3m = value;
    }

    public long getTrackingError6m() {
        return this.trackingError6m;
    }

    public void setTrackingError6m(long value) {
        this.trackingError6m = value;
    }

    public long getTrackingError1y() {
        return this.trackingError1y;
    }

    public void setTrackingError1y(long value) {
        this.trackingError1y = value;
    }

    public long getTrackingError3y() {
        return this.trackingError3y;
    }

    public void setTrackingError3y(long value) {
        this.trackingError3y = value;
    }

    public long getTrackingError5y() {
        return this.trackingError5y;
    }

    public void setTrackingError5y(long value) {
        this.trackingError5y = value;
    }

    public long getTrackingError10y() {
        return this.trackingError10y;
    }

    public void setTrackingError10y(long value) {
        this.trackingError10y = value;
    }

    public long getInformationRatio1w() {
        return this.informationRatio1w;
    }

    public void setInformationRatio1w(long value) {
        this.informationRatio1w = value;
    }

    public long getInformationRatio1m() {
        return this.informationRatio1m;
    }

    public void setInformationRatio1m(long value) {
        this.informationRatio1m = value;
    }

    public long getInformationRatio3m() {
        return this.informationRatio3m;
    }

    public void setInformationRatio3m(long value) {
        this.informationRatio3m = value;
    }

    public long getInformationRatio6m() {
        return this.informationRatio6m;
    }

    public void setInformationRatio6m(long value) {
        this.informationRatio6m = value;
    }

    public long getInformationRatio1y() {
        return this.informationRatio1y;
    }

    public void setInformationRatio1y(long value) {
        this.informationRatio1y = value;
    }

    public long getInformationRatio3y() {
        return this.informationRatio3y;
    }

    public void setInformationRatio3y(long value) {
        this.informationRatio3y = value;
    }

    public long getInformationRatio5y() {
        return this.informationRatio5y;
    }

    public void setInformationRatio5y(long value) {
        this.informationRatio5y = value;
    }

    public long getInformationRatio10y() {
        return this.informationRatio10y;
    }

    public void setInformationRatio10y(long value) {
        this.informationRatio10y = value;
    }

    public long getSterlingRatio1w() {
        return this.sterlingRatio1w;
    }

    public void setSterlingRatio1w(long value) {
        this.sterlingRatio1w = value;
    }

    public long getSterlingRatio1m() {
        return this.sterlingRatio1m;
    }

    public void setSterlingRatio1m(long value) {
        this.sterlingRatio1m = value;
    }

    public long getSterlingRatio3m() {
        return this.sterlingRatio3m;
    }

    public void setSterlingRatio3m(long value) {
        this.sterlingRatio3m = value;
    }

    public long getSterlingRatio6m() {
        return this.sterlingRatio6m;
    }

    public void setSterlingRatio6m(long value) {
        this.sterlingRatio6m = value;
    }

    public long getSterlingRatio1y() {
        return this.sterlingRatio1y;
    }

    public void setSterlingRatio1y(long value) {
        this.sterlingRatio1y = value;
    }

    public long getSterlingRatio3y() {
        return this.sterlingRatio3y;
    }

    public void setSterlingRatio3y(long value) {
        this.sterlingRatio3y = value;
    }

    public long getSterlingRatio5y() {
        return this.sterlingRatio5y;
    }

    public void setSterlingRatio5y(long value) {
        this.sterlingRatio5y = value;
    }

    public long getSterlingRatio10y() {
        return this.sterlingRatio10y;
    }

    public void setSterlingRatio10y(long value) {
        this.sterlingRatio10y = value;
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

    public long getProbabilityofoutperformance3y() {
        return this.probabilityofoutperformance3y;
    }

    public void setProbabilityofoutperformance3y(long value) {
        this.probabilityofoutperformance3y = value;
    }

    public long getPositiveregression3y() {
        return this.positiveregression3y;
    }

    public void setPositiveregression3y(long value) {
        this.positiveregression3y = value;
    }

    public long getNegativeregression3y() {
        return this.negativeregression3y;
    }

    public void setNegativeregression3y(long value) {
        this.negativeregression3y = value;
    }

    public long getBviperformancelastyear() {
        return this.bviperformancelastyear;
    }

    public void setBviperformancelastyear(long value) {
        this.bviperformancelastyear = value;
    }

    public long getBviperformanceyearbeforelastyear() {
        return this.bviperformanceyearbeforelastyear;
    }

    public void setBviperformanceyearbeforelastyear(long value) {
        this.bviperformanceyearbeforelastyear = value;
    }

    public long getPerformanceSinceFundIssueDate() {
        return this.performanceSinceFundIssueDate;
    }

    public void setPerformanceSinceFundIssueDate(long value) {
        this.performanceSinceFundIssueDate = value;
    }

    public long getBviperformance2_1y() {
        return this.bviperformance2_1y;
    }

    public void setBviperformance2_1y(long value) {
        this.bviperformance2_1y = value;
    }

    public long getBviperformance3_2y() {
        return this.bviperformance3_2y;
    }

    public void setBviperformance3_2y(long value) {
        this.bviperformance3_2y = value;
    }

    public long getBviperformance4_3y() {
        return this.bviperformance4_3y;
    }

    public void setBviperformance4_3y(long value) {
        this.bviperformance4_3y = value;
    }

    public long getBviperformance5_4y() {
        return this.bviperformance5_4y;
    }

    public void setBviperformance5_4y(long value) {
        this.bviperformance5_4y = value;
    }

    public long getMaSimple38() {
        return this.maSimple38;
    }

    public void setMaSimple38(long value) {
        this.maSimple38 = value;
    }

    public long getMaSimple90() {
        return this.maSimple90;
    }

    public void setMaSimple90(long value) {
        this.maSimple90 = value;
    }

    public long getMaSimple200() {
        return this.maSimple200;
    }

    public void setMaSimple200(long value) {
        this.maSimple200 = value;
    }

    public int getReference1w() {
        return this.reference1w;
    }

    public void setReference1w(int value) {
        this.reference1w = value;
    }

    public int getReference1m() {
        return this.reference1m;
    }

    public void setReference1m(int value) {
        this.reference1m = value;
    }

    public int getReference3m() {
        return this.reference3m;
    }

    public void setReference3m(int value) {
        this.reference3m = value;
    }

    public int getReference6m() {
        return this.reference6m;
    }

    public void setReference6m(int value) {
        this.reference6m = value;
    }

    public int getReference1y() {
        return this.reference1y;
    }

    public void setReference1y(int value) {
        this.reference1y = value;
    }

    public int getReference3y() {
        return this.reference3y;
    }

    public void setReference3y(int value) {
        this.reference3y = value;
    }

    public int getReference5y() {
        return this.reference5y;
    }

    public void setReference5y(int value) {
        this.reference5y = value;
    }

    public int getReference10y() {
        return this.reference10y;
    }

    public void setReference10y(int value) {
        this.reference10y = value;
    }

    public int getReferenceAlltime() {
        return this.referenceAlltime;
    }

    public void setReferenceAlltime(int value) {
        this.referenceAlltime = value;
    }

    public long getPerformancetobenchmarkcurrentyear() {
        return this.performancetobenchmarkcurrentyear;
    }

    public void setPerformancetobenchmarkcurrentyear(long value) {
        this.performancetobenchmarkcurrentyear = value;
    }

    public long getCorrelationcurrentyear() {
        return this.correlationcurrentyear;
    }

    public void setCorrelationcurrentyear(long value) {
        this.correlationcurrentyear = value;
    }

    public long getVolatilityAlltime() {
        return this.volatilityAlltime;
    }

    public void setVolatilityAlltime(long value) {
        this.volatilityAlltime = value;
    }

    public long getBetaCurrentYear() {
        return this.betaCurrentYear;
    }

    public void setBetaCurrentYear(long value) {
        this.betaCurrentYear = value;
    }

    public long getBetaAlltime() {
        return this.betaAlltime;
    }

    public void setBetaAlltime(long value) {
        this.betaAlltime = value;
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

    public long getAlphaCurrentYear() {
        return this.alphaCurrentYear;
    }

    public void setAlphaCurrentYear(long value) {
        this.alphaCurrentYear = value;
    }

    public long getAlphaAlltime() {
        return this.alphaAlltime;
    }

    public void setAlphaAlltime(long value) {
        this.alphaAlltime = value;
    }

    public long getTreynorCurrentYear() {
        return this.treynorCurrentYear;
    }

    public void setTreynorCurrentYear(long value) {
        this.treynorCurrentYear = value;
    }

    public long getTreynorAlltime() {
        return this.treynorAlltime;
    }

    public void setTreynorAlltime(long value) {
        this.treynorAlltime = value;
    }

    public long getMaximumLoss5y() {
        return this.maximumLoss5y;
    }

    public void setMaximumLoss5y(long value) {
        this.maximumLoss5y = value;
    }

    public long getMaximumLoss10y() {
        return this.maximumLoss10y;
    }

    public void setMaximumLoss10y(long value) {
        this.maximumLoss10y = value;
    }

    public long getMaximumLossAlltime() {
        return this.maximumLossAlltime;
    }

    public void setMaximumLossAlltime(long value) {
        this.maximumLossAlltime = value;
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