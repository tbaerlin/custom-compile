package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class InstrumentRatiosGNS extends InstrumentRatios {
    protected static final long serialVersionUID = 1L;

    public InstrumentRatiosGNS(long id) {
        super(id);
    }

    public QuoteRatios createQuoteRatios(long quoteid) {
        return new QuoteRatiosGNS(quoteid, this);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportGNS.forInstrument(fid);
    }

    private int expires = Integer.MIN_VALUE;

    private long wmIssueVolume = Long.MIN_VALUE;

    private long wmcoupon = Long.MIN_VALUE;

    private int wmexpirationdate = Integer.MIN_VALUE;

    private String wkn = null;

    private String isin = null;

    private String name = null;

    private String issuerName = null;

    private long interest = Long.MIN_VALUE;

    private String interestType = null;

    private String[] country = null;

    private String bondType = null;

    private long issueVolume = Long.MIN_VALUE;

    private int issueDate = Integer.MIN_VALUE;

    private String[] wmBondtype = null;

    private String[] wmCoupontype = null;

    private long wmNominalInterest = Long.MIN_VALUE;

    private String[] wmInterestPeriod = null;

    private String[] wmCountry = null;

    private String wmissuecurrency = null;

    private long wmDividend = Long.MIN_VALUE;

    private String wmDividendCurrency = null;

    private String benchmarkName = null;

    private String[] issuerCategory = null;

    private long wmDividendLastYear = Long.MIN_VALUE;

    private String[] wmSector = null;

    private int wmIssueDate = Integer.MIN_VALUE;

    private long wmNumberOfIssuedEquities = Long.MIN_VALUE;

    private String wmIssuerName = null;

    private boolean wmNotActive = false;


    public int getExpires() {
        return this.expires;
    }

    public void setExpires(int value) {
        this.expires = value;
    }

    public long getWmIssueVolume() {
        return this.wmIssueVolume;
    }

    public void setWmIssueVolume(long value) {
        this.wmIssueVolume = value;
    }

    public long getWmcoupon() {
        return this.wmcoupon;
    }

    public void setWmcoupon(long value) {
        this.wmcoupon = value;
    }

    public int getWmexpirationdate() {
        return this.wmexpirationdate;
    }

    public void setWmexpirationdate(int value) {
        this.wmexpirationdate = value;
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

    public String getIssuerName() {
        return this.issuerName;
    }

    public void setIssuerName(String value) {
        this.issuerName = value;
    }

    public long getInterest() {
        return this.interest;
    }

    public void setInterest(long value) {
        this.interest = value;
    }

    public String getInterestType() {
        return this.interestType;
    }

    public void setInterestType(String value) {
        this.interestType = value;
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

    public String getBondType() {
        return this.bondType;
    }

    public void setBondType(String value) {
        this.bondType = value;
    }

    public long getIssueVolume() {
        return this.issueVolume;
    }

    public void setIssueVolume(long value) {
        this.issueVolume = value;
    }

    public int getIssueDate() {
        return this.issueDate;
    }

    public void setIssueDate(int value) {
        this.issueDate = value;
    }

    public String getWmBondtype(int localeIndex) {
        if (this.wmBondtype == null) {
            return null;
        }
        return this.wmBondtype[localeIndex];
    }

    public void setWmBondtype(int localeIndex, String value) {
        if (this.wmBondtype == null) {
            this.wmBondtype = new String[RatioFieldDescription.wmBondtype.getLocales().length];
        }
        this.wmBondtype[localeIndex] = value;
    }

    public String getWmCoupontype(int localeIndex) {
        if (this.wmCoupontype == null) {
            return null;
        }
        return this.wmCoupontype[localeIndex];
    }

    public void setWmCoupontype(int localeIndex, String value) {
        if (this.wmCoupontype == null) {
            this.wmCoupontype = new String[RatioFieldDescription.wmCoupontype.getLocales().length];
        }
        this.wmCoupontype[localeIndex] = value;
    }

    public long getWmNominalInterest() {
        return this.wmNominalInterest;
    }

    public void setWmNominalInterest(long value) {
        this.wmNominalInterest = value;
    }

    public String getWmInterestPeriod(int localeIndex) {
        if (this.wmInterestPeriod == null) {
            return null;
        }
        return this.wmInterestPeriod[localeIndex];
    }

    public void setWmInterestPeriod(int localeIndex, String value) {
        if (this.wmInterestPeriod == null) {
            this.wmInterestPeriod = new String[RatioFieldDescription.wmInterestPeriod.getLocales().length];
        }
        this.wmInterestPeriod[localeIndex] = value;
    }

    public String getWmCountry(int localeIndex) {
        if (this.wmCountry == null) {
            return null;
        }
        return this.wmCountry[localeIndex];
    }

    public void setWmCountry(int localeIndex, String value) {
        if (this.wmCountry == null) {
            this.wmCountry = new String[RatioFieldDescription.wmCountry.getLocales().length];
        }
        this.wmCountry[localeIndex] = value;
    }

    public String getWmissuecurrency() {
        return this.wmissuecurrency;
    }

    public void setWmissuecurrency(String value) {
        this.wmissuecurrency = value;
    }

    public long getWmDividend() {
        return this.wmDividend;
    }

    public void setWmDividend(long value) {
        this.wmDividend = value;
    }

    public String getWmDividendCurrency() {
        return this.wmDividendCurrency;
    }

    public void setWmDividendCurrency(String value) {
        this.wmDividendCurrency = value;
    }

    public String getBenchmarkName() {
        return this.benchmarkName;
    }

    public void setBenchmarkName(String value) {
        this.benchmarkName = value;
    }

    public String getIssuerCategory(int localeIndex) {
        if (this.issuerCategory == null) {
            return null;
        }
        return this.issuerCategory[localeIndex];
    }

    public void setIssuerCategory(int localeIndex, String value) {
        if (this.issuerCategory == null) {
            this.issuerCategory = new String[RatioFieldDescription.issuerCategory.getLocales().length];
        }
        this.issuerCategory[localeIndex] = value;
    }

    public long getWmDividendLastYear() {
        return this.wmDividendLastYear;
    }

    public void setWmDividendLastYear(long value) {
        this.wmDividendLastYear = value;
    }

    public String getWmSector(int localeIndex) {
        if (this.wmSector == null) {
            return null;
        }
        return this.wmSector[localeIndex];
    }

    public void setWmSector(int localeIndex, String value) {
        if (this.wmSector == null) {
            this.wmSector = new String[RatioFieldDescription.wmSector.getLocales().length];
        }
        this.wmSector[localeIndex] = value;
    }

    public int getWmIssueDate() {
        return this.wmIssueDate;
    }

    public void setWmIssueDate(int value) {
        this.wmIssueDate = value;
    }

    public long getWmNumberOfIssuedEquities() {
        return this.wmNumberOfIssuedEquities;
    }

    public void setWmNumberOfIssuedEquities(long value) {
        this.wmNumberOfIssuedEquities = value;
    }

    public String getWmIssuerName() {
        return this.wmIssuerName;
    }

    public void setWmIssuerName(String value) {
        this.wmIssuerName = value;
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