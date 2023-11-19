package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class QuoteRatiosZNS extends QuoteRatios<InstrumentRatiosZNS> {
    protected static final long serialVersionUID = 1L;

    public QuoteRatiosZNS(long id, InstrumentRatiosZNS instrumentRatios) {
        super(id, instrumentRatios);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportZNS.forQuote(fid);
    }

    public PropertySupport propertySupport4Instrument(int fid) {
        return RatiosPropertySupportZNS.forInstrument(fid);
    }

    private long mostRecentUpdateTimestamp = Long.MIN_VALUE;

    private long volatility1m = Long.MIN_VALUE;

    private long performance1m = Long.MIN_VALUE;

    private long referenceTimestamp = Long.MIN_VALUE;

    private long lastPrice = Long.MIN_VALUE;

    private long volatility3m = Long.MIN_VALUE;

    private long performance3m = Long.MIN_VALUE;

    private long performance1y = Long.MIN_VALUE;

    private long volatility10y = Long.MIN_VALUE;

    private long volatility1y = Long.MIN_VALUE;

    private long performance10y = Long.MIN_VALUE;

    private long volatility1w = Long.MIN_VALUE;

    private long performance6m = Long.MIN_VALUE;

    private long performance3y = Long.MIN_VALUE;

    private long performance5y = Long.MIN_VALUE;

    private long volatility6m = Long.MIN_VALUE;

    private long volatility5y = Long.MIN_VALUE;

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

    private long high1y = Long.MIN_VALUE;

    private long low1y = Long.MIN_VALUE;

    private String marketmanagerName = null;

    private long volatilityCurrentYear = Long.MIN_VALUE;

    private long performanceAlltime = Long.MIN_VALUE;

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

    private long maSimple38 = Long.MIN_VALUE;

    private long maSimple90 = Long.MIN_VALUE;

    private long maSimple200 = Long.MIN_VALUE;

    private long volatilityAlltime = Long.MIN_VALUE;

    private long volatility = Long.MIN_VALUE;

    private String rating = null;

    private String standard = null;


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

    public long getLastPrice() {
        return this.lastPrice;
    }

    public void setLastPrice(long value) {
        this.lastPrice = value;
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

    public long getVolatilityAlltime() {
        return this.volatilityAlltime;
    }

    public void setVolatilityAlltime(long value) {
        this.volatilityAlltime = value;
    }

    public long getVolatility() {
        return this.volatility;
    }

    public void setVolatility(long value) {
        this.volatility = value;
    }

    public String getRating() {
        return this.rating;
    }

    public void setRating(String value) {
        this.rating = value;
    }

    public String getStandard() {
        return this.standard;
    }

    public void setStandard(String value) {
        this.standard = value;
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