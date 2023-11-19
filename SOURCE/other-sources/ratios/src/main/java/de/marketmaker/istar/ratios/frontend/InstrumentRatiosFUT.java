package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class InstrumentRatiosFUT extends InstrumentRatios implements InstrumentRatiosDerivative {
    protected static final long serialVersionUID = 1L;

    public InstrumentRatiosFUT(long id) {
        super(id);
    }

    public QuoteRatios createQuoteRatios(long quoteid) {
        return new QuoteRatiosFUT(quoteid, this);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportFUT.forInstrument(fid);
    }

    private int expires = Integer.MIN_VALUE;

    private String wkn = null;

    private String isin = null;

    private String name = null;

    private String underlyingWkn = null;

    private String underlyingIsin = null;

    private String underlyingName = null;

    private long underlyingIid = Long.MIN_VALUE;

    private long tickSize = Long.MIN_VALUE;

    private long tickValue = Long.MIN_VALUE;

    private String tickCurrency = null;

    private long contractValue = Long.MIN_VALUE;

    private long contractValueCalculated = Long.MIN_VALUE;

    private String contractCurrency = null;

    private long underlyingProductIid = Long.MIN_VALUE;

    private String underlyingEurexTicker = null;

    private boolean wmNotActive = false;


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

    public long getUnderlyingIid() {
        return this.underlyingIid;
    }

    public void setUnderlyingIid(long value) {
        this.underlyingIid = value;
    }

    public long getTickSize() {
        return this.tickSize;
    }

    public void setTickSize(long value) {
        this.tickSize = value;
    }

    public long getTickValue() {
        return this.tickValue;
    }

    public void setTickValue(long value) {
        this.tickValue = value;
    }

    public String getTickCurrency() {
        return this.tickCurrency;
    }

    public void setTickCurrency(String value) {
        this.tickCurrency = value;
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

    public String getContractCurrency() {
        return this.contractCurrency;
    }

    public void setContractCurrency(String value) {
        this.contractCurrency = value;
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