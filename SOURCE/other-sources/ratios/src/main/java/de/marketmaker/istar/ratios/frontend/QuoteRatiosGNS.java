package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class QuoteRatiosGNS extends QuoteRatios<InstrumentRatiosGNS> {
    protected static final long serialVersionUID = 1L;

    public QuoteRatiosGNS(long id, InstrumentRatiosGNS instrumentRatios) {
        super(id, instrumentRatios);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportGNS.forQuote(fid);
    }

    public PropertySupport propertySupport4Instrument(int fid) {
        return RatiosPropertySupportGNS.forInstrument(fid);
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

    private long volatility3m = Long.MIN_VALUE;

    private long performance3m = Long.MIN_VALUE;

    private long performance1y = Long.MIN_VALUE;

    private long volatility10y = Long.MIN_VALUE;

    private long volatility1y = Long.MIN_VALUE;

    private long beta1y = Long.MIN_VALUE;

    private long correlation1y = Long.MIN_VALUE;

    private long marketcapitalization = Long.MIN_VALUE;

    private long performance10y = Long.MIN_VALUE;

    private long volatility1w = Long.MIN_VALUE;

    private long performance1d = Long.MIN_VALUE;

    private long performancetobenchmark3m = Long.MIN_VALUE;

    private long performancetobenchmark1w = Long.MIN_VALUE;

    private long performancetobenchmark1m = Long.MIN_VALUE;

    private long performancetobenchmark1y = Long.MIN_VALUE;

    private long averagevolume1m = Long.MIN_VALUE;

    private long averagevolume1y = Long.MIN_VALUE;

    private long beta1m = Long.MIN_VALUE;

    private long earning = Long.MIN_VALUE;

    private long performancetobenchmark6m = Long.MIN_VALUE;

    private long performance6m = Long.MIN_VALUE;

    private long performance3y = Long.MIN_VALUE;

    private long performance5y = Long.MIN_VALUE;

    private long volatility6m = Long.MIN_VALUE;

    private long volatility5y = Long.MIN_VALUE;

    private long correlation1m = Long.MIN_VALUE;

    private String vwdCode = null;

    private String mmwkn = null;

    private String currency = null;

    private long yieldRelative_mdps = Long.MIN_VALUE;

    private long duration_mdps = Long.MIN_VALUE;

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

    private long volatility3y = Long.MIN_VALUE;

    private long performance1w = Long.MIN_VALUE;

    private long correlation1w = Long.MIN_VALUE;

    private long correlation3m = Long.MIN_VALUE;

    private long correlation6m = Long.MIN_VALUE;

    private long correlation3y = Long.MIN_VALUE;

    private long correlation5y = Long.MIN_VALUE;

    private long correlation10y = Long.MIN_VALUE;

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

    private long alpha1m = Long.MIN_VALUE;

    private long alpha1y = Long.MIN_VALUE;

    private long mdpsbasepointvalue = Long.MIN_VALUE;

    private long mdpsconvexity = Long.MIN_VALUE;

    private long mdpsmodifiedduration = Long.MIN_VALUE;

    private long mdpsbrokenperiodinterest = Long.MIN_VALUE;

    private long mdpsinterestrateelasticity = Long.MIN_VALUE;

    private long beta3y = Long.MIN_VALUE;

    private long beta5y = Long.MIN_VALUE;

    private long beta10y = Long.MIN_VALUE;

    private long performanceAlltime = Long.MIN_VALUE;

    private long referencePrice = Long.MIN_VALUE;

    private long pari = Long.MIN_VALUE;

    private String vwdMarket = null;

    private long closePreviousYear = Long.MIN_VALUE;

    private int closePreviousYearDate = Integer.MIN_VALUE;

    private long closePreviousMonth = Long.MIN_VALUE;

    private int closePreviousMonthDate = Integer.MIN_VALUE;

    private long performanceCurrentYear = Long.MIN_VALUE;

    private long performanceCurrentMonth = Long.MIN_VALUE;

    private long highPreviousYear = Long.MIN_VALUE;

    private int highPreviousYearDate = Integer.MIN_VALUE;

    private long lowPreviousYear = Long.MIN_VALUE;

    private int lowPreviousYearDate = Integer.MIN_VALUE;

    private long changeNetCurrentYear = Long.MIN_VALUE;

    private long changeNetCurrentMonth = Long.MIN_VALUE;

    private long alpha1w = Long.MIN_VALUE;

    private long alpha3m = Long.MIN_VALUE;

    private long alpha6m = Long.MIN_VALUE;

    private long alpha3y = Long.MIN_VALUE;

    private long alpha5y = Long.MIN_VALUE;

    private long alpha10y = Long.MIN_VALUE;

    private long wmDividendYield = Long.MIN_VALUE;

    private long qid = Long.MIN_VALUE;

    private int dateFirstPrice = Integer.MIN_VALUE;

    private long maSimple38 = Long.MIN_VALUE;

    private long maSimple90 = Long.MIN_VALUE;

    private long maSimple200 = Long.MIN_VALUE;

    private long marketCapitalizationPreviousDay = Long.MIN_VALUE;

    private long turnoverDay = Long.MIN_VALUE;

    private long performancetobenchmarkcurrentyear = Long.MIN_VALUE;

    private long correlationcurrentyear = Long.MIN_VALUE;

    private long marketcapitalizationUSD = Long.MIN_VALUE;

    private long marketcapitalizationEUR = Long.MIN_VALUE;

    private long volatilityAlltime = Long.MIN_VALUE;

    private long betaCurrentYear = Long.MIN_VALUE;

    private long betaAlltime = Long.MIN_VALUE;

    private long alphaCurrentYear = Long.MIN_VALUE;

    private long alphaAlltime = Long.MIN_VALUE;


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

    public long getMarketcapitalization() {
        return this.marketcapitalization;
    }

    public void setMarketcapitalization(long value) {
        this.marketcapitalization = value;
    }

    public long getPerformance10y() {
        return this.performance10y;
    }

    public void setPerformance10y(long value) {
        this.performance10y = value;
    }

    public long getVolatility1w() {
        return this.volatility1w;
    }

    public void setVolatility1w(long value) {
        this.volatility1w = value;
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

    public long getEarning() {
        return this.earning;
    }

    public void setEarning(long value) {
        this.earning = value;
    }

    public long getPerformancetobenchmark6m() {
        return this.performancetobenchmark6m;
    }

    public void setPerformancetobenchmark6m(long value) {
        this.performancetobenchmark6m = value;
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

    public long getYieldRelative_mdps() {
        return this.yieldRelative_mdps;
    }

    public void setYieldRelative_mdps(long value) {
        this.yieldRelative_mdps = value;
    }

    public long getDuration_mdps() {
        return this.duration_mdps;
    }

    public void setDuration_mdps(long value) {
        this.duration_mdps = value;
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

    public long getMdpsbasepointvalue() {
        return this.mdpsbasepointvalue;
    }

    public void setMdpsbasepointvalue(long value) {
        this.mdpsbasepointvalue = value;
    }

    public long getMdpsconvexity() {
        return this.mdpsconvexity;
    }

    public void setMdpsconvexity(long value) {
        this.mdpsconvexity = value;
    }

    public long getMdpsmodifiedduration() {
        return this.mdpsmodifiedduration;
    }

    public void setMdpsmodifiedduration(long value) {
        this.mdpsmodifiedduration = value;
    }

    public long getMdpsbrokenperiodinterest() {
        return this.mdpsbrokenperiodinterest;
    }

    public void setMdpsbrokenperiodinterest(long value) {
        this.mdpsbrokenperiodinterest = value;
    }

    public long getMdpsinterestrateelasticity() {
        return this.mdpsinterestrateelasticity;
    }

    public void setMdpsinterestrateelasticity(long value) {
        this.mdpsinterestrateelasticity = value;
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

    public long getPerformanceAlltime() {
        return this.performanceAlltime;
    }

    public void setPerformanceAlltime(long value) {
        this.performanceAlltime = value;
    }

    public long getReferencePrice() {
        return this.referencePrice;
    }

    public void setReferencePrice(long value) {
        this.referencePrice = value;
    }

    public long getPari() {
        return this.pari;
    }

    public void setPari(long value) {
        this.pari = value;
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

    public long getPerformanceCurrentYear() {
        return this.performanceCurrentYear;
    }

    public void setPerformanceCurrentYear(long value) {
        this.performanceCurrentYear = value;
    }

    public long getPerformanceCurrentMonth() {
        return this.performanceCurrentMonth;
    }

    public void setPerformanceCurrentMonth(long value) {
        this.performanceCurrentMonth = value;
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

    public long getWmDividendYield() {
        return this.wmDividendYield;
    }

    public void setWmDividendYield(long value) {
        this.wmDividendYield = value;
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

    public long getMarketCapitalizationPreviousDay() {
        return this.marketCapitalizationPreviousDay;
    }

    public void setMarketCapitalizationPreviousDay(long value) {
        this.marketCapitalizationPreviousDay = value;
    }

    public long getTurnoverDay() {
        return this.turnoverDay;
    }

    public void setTurnoverDay(long value) {
        this.turnoverDay = value;
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

    public long getMarketcapitalizationUSD() {
        return this.marketcapitalizationUSD;
    }

    public void setMarketcapitalizationUSD(long value) {
        this.marketcapitalizationUSD = value;
    }

    public long getMarketcapitalizationEUR() {
        return this.marketcapitalizationEUR;
    }

    public void setMarketcapitalizationEUR(long value) {
        this.marketcapitalizationEUR = value;
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