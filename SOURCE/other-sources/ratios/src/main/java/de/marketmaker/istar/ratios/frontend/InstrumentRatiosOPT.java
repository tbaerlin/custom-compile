package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class InstrumentRatiosOPT extends InstrumentRatios implements InstrumentRatiosDerivative {
    protected static final long serialVersionUID = 1L;

    public InstrumentRatiosOPT(long id) {
        super(id);
    }

    public QuoteRatios createQuoteRatios(long quoteid) {
        return new QuoteRatiosOPT(quoteid, this);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportOPT.forInstrument(fid);
    }

    private String currencyStrike = null;

    private int expires = Integer.MIN_VALUE;

    private String wkn = null;

    private String isin = null;

    private String name = null;

    private long strikePrice = Long.MIN_VALUE;

    private String osType = null;

    private boolean isAmerican = false;

    private String underlyingWkn = null;

    private String underlyingIsin = null;

    private String underlyingName = null;

    private String exerciseType = null;

    private long underlyingIid = Long.MIN_VALUE;

    private String optionCategory = null;

    private long contractValue = Long.MIN_VALUE;

    private long contractValueCalculated = Long.MIN_VALUE;

    private long contractSize = Long.MIN_VALUE;

    private long generationNumber = Long.MIN_VALUE;

    private long versionNumber = Long.MIN_VALUE;

    private int tradingMonth = Integer.MIN_VALUE;

    private long underlyingProductIid = Long.MIN_VALUE;

    private String underlyingEurexTicker = null;

    private boolean wmNotActive = false;

    private boolean isFlex = false;


    public String getCurrencyStrike() {
        return this.currencyStrike;
    }

    public void setCurrencyStrike(String value) {
        this.currencyStrike = value;
    }

    public int getExpires() {
        return this.expires;
    }

    public void setExpires(int value) {
        this.expires = value;
    }

    public String getWkn() {
        return this.wkn;
    }

    public void setWkn(String value) {
        this.wkn = value;
    }

    public String getIsin() {
        return this.isin;
    }

    public void setIsin(String value) {
        this.isin = value;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public long getStrikePrice() {
        return this.strikePrice;
    }

    public void setStrikePrice(long value) {
        this.strikePrice = value;
    }

    public String getOsType() {
        return this.osType;
    }

    public void setOsType(String value) {
        this.osType = value;
    }

    public boolean getIsAmerican() {
        return this.isAmerican;
    }

    public void setIsAmerican(boolean value) {
        this.isAmerican = value;
    }

    public String getUnderlyingWkn() {
        return this.underlyingWkn;
    }

    public void setUnderlyingWkn(String value) {
        this.underlyingWkn = value;
    }

    public String getUnderlyingIsin() {
        return this.underlyingIsin;
    }

    public void setUnderlyingIsin(String value) {
        this.underlyingIsin = value;
    }

    public String getUnderlyingName() {
        return this.underlyingName;
    }

    public void setUnderlyingName(String value) {
        this.underlyingName = value;
    }

    public String getExerciseType() {
        return this.exerciseType;
    }

    public void setExerciseType(String value) {
        this.exerciseType = value;
    }

    public long getUnderlyingIid() {
        return this.underlyingIid;
    }

    public void setUnderlyingIid(long value) {
        this.underlyingIid = value;
    }

    public String getOptionCategory() {
        return this.optionCategory;
    }

    public void setOptionCategory(String value) {
        this.optionCategory = value;
    }

    public long getContractValue() {
        return this.contractValue;
    }

    public void setContractValue(long value) {
        this.contractValue = value;
    }

    public long getContractValueCalculated() {
        return this.contractValueCalculated;
    }

    public void setContractValueCalculated(long value) {
        this.contractValueCalculated = value;
    }

    public long getContractSize() {
        return this.contractSize;
    }

    public void setContractSize(long value) {
        this.contractSize = value;
    }

    public long getGenerationNumber() {
        return this.generationNumber;
    }

    public void setGenerationNumber(long value) {
        this.generationNumber = value;
    }

    public long getVersionNumber() {
        return this.versionNumber;
    }

    public void setVersionNumber(long value) {
        this.versionNumber = value;
    }

    public int getTradingMonth() {
        return this.tradingMonth;
    }

    public void setTradingMonth(int value) {
        this.tradingMonth = value;
    }

    public long getUnderlyingProductIid() {
        return this.underlyingProductIid;
    }

    public void setUnderlyingProductIid(long value) {
        this.underlyingProductIid = value;
    }

    public String getUnderlyingEurexTicker() {
        return this.underlyingEurexTicker;
    }

    public void setUnderlyingEurexTicker(String value) {
        this.underlyingEurexTicker = value;
    }

    public boolean getWmNotActive() {
        return this.wmNotActive;
    }

    public void setWmNotActive(boolean value) {
        this.wmNotActive = value;
    }

    public boolean getIsFlex() {
        return this.isFlex;
    }

    public void setIsFlex(boolean value) {
        this.isFlex = value;
    }


    // Selectable Interface ----------------------------------------

    public String getString(int fieldid) {
        final PropertySupport ps = propertySupport(fieldid);
        return ps != null ? ps.getString(this) : null;
    }

    public String getString(int fieldid, int localeIndex) {
        final PropertySupport ps = propertySupport(fieldid);
        return ps != null ? ps.getString(this, localeIndex) : null;
    }

    public Boolean getBoolean(int fieldid) {
        final PropertySupport ps = propertySupport(fieldid);
        return ps != null && ps.getBoolean(this);
    }

    public Long getLong(int fieldid) {
        final PropertySupport ps = propertySupport(fieldid);
        final long result = ps != null ? ps.getLong(this) : Long.MIN_VALUE;
        return result == Long.MIN_VALUE ? null : result;
    }

    @Override
    public BitSet getBitSet(int fieldid) {
        final PropertySupport ps = propertySupport(fieldid);
        return ps != null ? ps.getBitSet(this) : null;
    }

    public Integer getInt(int fieldid) {
        final PropertySupport ps = propertySupport(fieldid);
        final int result = ps != null ? ps.getInt(this) : Integer.MIN_VALUE;
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
        return field != null ? getLong(field.id()) : null;
    }

    public BitSet getBitSet(String fieldname) {
        final RatioFieldDescription.Field field = getFieldByName(fieldname);
        return field != null ? getBitSet(field.id()) : null;
    }

    public Integer getInt(String fieldname) {
        final RatioFieldDescription.Field field = getFieldByName(fieldname);
        return field != null ? getInt(field.id()) : null;
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