package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class QuoteRatiosWNT extends QuoteRatios<InstrumentRatiosWNT> {
    protected static final long serialVersionUID = 1L;

    public QuoteRatiosWNT(long id, InstrumentRatiosWNT instrumentRatios) {
        super(id, instrumentRatios);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportWNT.forQuote(fid);
    }

    public PropertySupport propertySupport4Instrument(int fid) {
        return RatiosPropertySupportWNT.forInstrument(fid);
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

    private long volatility10y = Long.MIN_VALUE;

    private long volatility1y = Long.MIN_VALUE;

    private int dateoflast = Integer.MIN_VALUE;

    private long intrinsicvalue = Long.MIN_VALUE;

    private long intrinsicvaluepercent = Long.MIN_VALUE;

    private long extrinsicvalue = Long.MIN_VALUE;

    private long extrinsicvaluepercent = Long.MIN_VALUE;

    private long optionprice = Long.MIN_VALUE;

    private long optionpriceperyear = Long.MIN_VALUE;

    private long breakeven = Long.MIN_VALUE;

    private long leverage = Long.MIN_VALUE;

    private long fairvalue = Long.MIN_VALUE;

    private long delta = Long.MIN_VALUE;

    private long omega = Long.MIN_VALUE;

    private long gamma = Long.MIN_VALUE;

    private long vega = Long.MIN_VALUE;

    private long theta = Long.MIN_VALUE;

    private long rho = Long.MIN_VALUE;

    private long impliedvolatility = Long.MIN_VALUE;

    private long performance10y = Long.MIN_VALUE;

    private long last = Long.MIN_VALUE;

    private long performance1d = Long.MIN_VALUE;

    private long performance6m = Long.MIN_VALUE;

    private String vwdCode = null;

    private String mmwkn = null;

    private String currency = null;

    private long changeNet = Long.MIN_VALUE;

    private long changePercent = Long.MIN_VALUE;

    private long performance1w = Long.MIN_VALUE;

    private long averagevolume1w = Long.MIN_VALUE;

    private String marketmanagerName = null;

    private long volatilityCurrentYear = Long.MIN_VALUE;

    private long performanceAlltime = Long.MIN_VALUE;

    private long referencePrice = Long.MIN_VALUE;

    private String vwdMarket = null;

    private long performanceCurrentYear = Long.MIN_VALUE;

    private long qid = Long.MIN_VALUE;

    private long volatilityAlltime = Long.MIN_VALUE;


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

    public int getDateoflast() {
        return this.dateoflast;
    }

    public void setDateoflast(int value) {
        this.dateoflast = value;
    }

    public long getIntrinsicvalue() {
        return this.intrinsicvalue;
    }

    public void setIntrinsicvalue(long value) {
        this.intrinsicvalue = value;
    }

    public long getIntrinsicvaluepercent() {
        return this.intrinsicvaluepercent;
    }

    public void setIntrinsicvaluepercent(long value) {
        this.intrinsicvaluepercent = value;
    }

    public long getExtrinsicvalue() {
        return this.extrinsicvalue;
    }

    public void setExtrinsicvalue(long value) {
        this.extrinsicvalue = value;
    }

    public long getExtrinsicvaluepercent() {
        return this.extrinsicvaluepercent;
    }

    public void setExtrinsicvaluepercent(long value) {
        this.extrinsicvaluepercent = value;
    }

    public long getOptionprice() {
        return this.optionprice;
    }

    public void setOptionprice(long value) {
        this.optionprice = value;
    }

    public long getOptionpriceperyear() {
        return this.optionpriceperyear;
    }

    public void setOptionpriceperyear(long value) {
        this.optionpriceperyear = value;
    }

    public long getBreakeven() {
        return this.breakeven;
    }

    public void setBreakeven(long value) {
        this.breakeven = value;
    }

    public long getLeverage() {
        return this.leverage;
    }

    public void setLeverage(long value) {
        this.leverage = value;
    }

    public long getFairvalue() {
        return this.fairvalue;
    }

    public void setFairvalue(long value) {
        this.fairvalue = value;
    }

    public long getDelta() {
        return this.delta;
    }

    public void setDelta(long value) {
        this.delta = value;
    }

    public long getOmega() {
        return this.omega;
    }

    public void setOmega(long value) {
        this.omega = value;
    }

    public long getGamma() {
        return this.gamma;
    }

    public void setGamma(long value) {
        this.gamma = value;
    }

    public long getVega() {
        return this.vega;
    }

    public void setVega(long value) {
        this.vega = value;
    }

    public long getTheta() {
        return this.theta;
    }

    public void setTheta(long value) {
        this.theta = value;
    }

    public long getRho() {
        return this.rho;
    }

    public void setRho(long value) {
        this.rho = value;
    }

    public long getImpliedvolatility() {
        return this.impliedvolatility;
    }

    public void setImpliedvolatility(long value) {
        this.impliedvolatility = value;
    }

    public long getPerformance10y() {
        return this.performance10y;
    }

    public void setPerformance10y(long value) {
        this.performance10y = value;
    }

    public long getLast() {
        return this.last;
    }

    public void setLast(long value) {
        this.last = value;
    }

    public long getPerformance1d() {
        return this.performance1d;
    }

    public void setPerformance1d(long value) {
        this.performance1d = value;
    }

    public long getPerformance6m() {
        return this.performance6m;
    }

    public void setPerformance6m(long value) {
        this.performance6m = value;
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

    public long getPerformance1w() {
        return this.performance1w;
    }

    public void setPerformance1w(long value) {
        this.performance1w = value;
    }

    public long getAveragevolume1w() {
        return this.averagevolume1w;
    }

    public void setAveragevolume1w(long value) {
        this.averagevolume1w = value;
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

    public long getQid() {
        return this.qid;
    }

    public void setQid(long value) {
        this.qid = value;
    }

    public long getVolatilityAlltime() {
        return this.volatilityAlltime;
    }

    public void setVolatilityAlltime(long value) {
        this.volatilityAlltime = value;
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