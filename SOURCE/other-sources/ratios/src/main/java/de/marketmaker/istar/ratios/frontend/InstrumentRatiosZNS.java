package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class InstrumentRatiosZNS extends InstrumentRatios {
    protected static final long serialVersionUID = 1L;

    public InstrumentRatiosZNS(long id) {
        super(id);
    }

    public QuoteRatios createQuoteRatios(long quoteid) {
        return new QuoteRatiosZNS(quoteid, this);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportZNS.forInstrument(fid);
    }

    private String wkn = null;

    private String isin = null;

    private String name = null;

    private String issuerName = null;

    private String[] country = null;

    private String[] sector = null;

    private String znsCategory = null;

    private String maturity = null;

    private String debtRanking = null;

    private String issuerType = null;

    private String restructuringRule = null;

    private String source = null;

    private boolean wmNotActive = false;


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

    public String getIssuerName() {
        return this.issuerName;
    }

    public void setIssuerName(String value) {
        this.issuerName = value;
    }

    public String getCountry(int localeIndex) {
        if (this.country == null) {
            return null;
        }
        return this.country[localeIndex];
    }

    public void setCountry(int localeIndex, String value) {
        if (this.country == null) {
            this.country = new String[RatioFieldDescription.country.getLocales().length];
        }
        this.country[localeIndex] = value;
    }

    public String getSector(int localeIndex) {
        if (this.sector == null) {
            return null;
        }
        return this.sector[localeIndex];
    }

    public void setSector(int localeIndex, String value) {
        if (this.sector == null) {
            this.sector = new String[RatioFieldDescription.sector.getLocales().length];
        }
        this.sector[localeIndex] = value;
    }

    public String getZnsCategory() {
        return this.znsCategory;
    }

    public void setZnsCategory(String value) {
        this.znsCategory = value;
    }

    public String getMaturity() {
        return this.maturity;
    }

    public void setMaturity(String value) {
        this.maturity = value;
    }

    public String getDebtRanking() {
        return this.debtRanking;
    }

    public void setDebtRanking(String value) {
        this.debtRanking = value;
    }

    public String getIssuerType() {
        return this.issuerType;
    }

    public void setIssuerType(String value) {
        this.issuerType = value;
    }

    public String getRestructuringRule() {
        return this.restructuringRule;
    }

    public void setRestructuringRule(String value) {
        this.restructuringRule = value;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String value) {
        this.source = value;
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