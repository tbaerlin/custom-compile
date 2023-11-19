package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class QuoteRatiosIND extends QuoteRatios<InstrumentRatiosIND> {
    protected static final long serialVersionUID = 1L;

    public QuoteRatiosIND(long id, InstrumentRatiosIND instrumentRatios) {
        super(id, instrumentRatios);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportIND.forQuote(fid);
    }

    public PropertySupport propertySupport4Instrument(int fid) {
        return RatiosPropertySupportIND.forInstrument(fid);
    }

    private long mostRecentUpdateTimestamp = Long.MIN_VALUE;

    private long volatility1m = Long.MIN_VALUE;

    private long performance1m = Long.MIN_VALUE;

    private long referenceTimestamp = Long.MIN_VALUE;

    private long volatility3m = Long.MIN_VALUE;

    private long performance3m = Long.MIN_VALUE;

    private long performance1y = Long.MIN_VALUE;

    private long volatility10y = Long.MIN_VALUE;

    private long volatility1y = Long.MIN_VALUE;

    private long sharperatio1y = Long.MIN_VALUE;

    private long sharperatio3y = Long.MIN_VALUE;

    private long performance10y = Long.MIN_VALUE;

    private long volatility1w = Long.MIN_VALUE;

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

    private long volatility3y = Long.MIN_VALUE;

    private long performance1w = Long.MIN_VALUE;

    private long maximumLoss1y = Long.MIN_VALUE;

    private long high1y = Long.MIN_VALUE;

    private long low1y = Long.MIN_VALUE;

    private long maximumLoss6m = Long.MIN_VALUE;

    private long averagevolume1w = Long.MIN_VALUE;

    private long averagevolume3m = Long.MIN_VALUE;

    private long averagevolume6m = Long.MIN_VALUE;

    private long averagevolume3y = Long.MIN_VALUE;

    private long averagevolume5y = Long.MIN_VALUE;

    private long averagevolume10y = Long.MIN_VALUE;

    private String marketmanagerName = null;

    private long maximumLoss3y = Long.MIN_VALUE;

    private long volatilityCurrentYear = Long.MIN_VALUE;

    private long performanceAlltime = Long.MIN_VALUE;

    private long sharperatio1w = Long.MIN_VALUE;

    private long sharperatio10y = Long.MIN_VALUE;

    private long referencePrice = Long.MIN_VALUE;

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

    private long qid = Long.MIN_VALUE;

    private int dateFirstPrice = Integer.MIN_VALUE;

    private long rsi7d = Long.MIN_VALUE;

    private long rsi9d = Long.MIN_VALUE;

    private long rsi14d = Long.MIN_VALUE;

    private long rsi25d = Long.MIN_VALUE;

    private long rsi90d = Long.MIN_VALUE;

    private long rsi450d = Long.MIN_VALUE;

    private long maSimple38 = Long.MIN_VALUE;

    private long maSimple90 = Long.MIN_VALUE;

    private long maSimple200 = Long.MIN_VALUE;

    private long rsi130d = Long.MIN_VALUE;

    private long volatilityAlltime = Long.MIN_VALUE;

    private long sharperatioCurrentYear = Long.MIN_VALUE;

    private long sharperatioAlltime = Long.MIN_VALUE;

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

    public long getPerformance1m() {
        return this.performance1m;
    }

    public void setPerformance1m(long value) {
        this.performance1m = value;
    }

    public long getReferenceTimestamp() {
        return this.referenceTimestamp;
    }

    public void setReferenceTimestamp(long value) {
        this.referenceTimestamp = value;
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

    public long getMaximumLoss1y() {
        return this.maximumLoss1y;
    }

    public void setMaximumLoss1y(long value) {
        this.maximumLoss1y = value;
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

    public long getRsi7d() {
        return this.rsi7d;
    }

    public void setRsi7d(long value) {
        this.rsi7d = value;
    }

    public long getRsi9d() {
        return this.rsi9d;
    }

    public void setRsi9d(long value) {
        this.rsi9d = value;
    }

    public long getRsi14d() {
        return this.rsi14d;
    }

    public void setRsi14d(long value) {
        this.rsi14d = value;
    }

    public long getRsi25d() {
        return this.rsi25d;
    }

    public void setRsi25d(long value) {
        this.rsi25d = value;
    }

    public long getRsi90d() {
        return this.rsi90d;
    }

    public void setRsi90d(long value) {
        this.rsi90d = value;
    }

    public long getRsi450d() {
        return this.rsi450d;
    }

    public void setRsi450d(long value) {
        this.rsi450d = value;
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

    public long getRsi130d() {
        return this.rsi130d;
    }

    public void setRsi130d(long value) {
        this.rsi130d = value;
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