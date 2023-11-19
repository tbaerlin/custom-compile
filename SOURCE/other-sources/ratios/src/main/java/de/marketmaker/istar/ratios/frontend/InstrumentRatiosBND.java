package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class InstrumentRatiosBND extends InstrumentRatios {
    protected static final long serialVersionUID = 1L;

    public InstrumentRatiosBND(long id) {
        super(id);
    }

    public QuoteRatios createQuoteRatios(long quoteid) {
        return new QuoteRatiosBND(quoteid, this);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportBND.forInstrument(fid);
    }

    private int expires = Integer.MIN_VALUE;

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

    private String ratingFitchShortTerm = null;

    private String ratingMoodysLongTerm = null;

    private long issueVolume = Long.MIN_VALUE;

    private long wmRedemptionPrice = Long.MIN_VALUE;

    private int issueDate = Integer.MIN_VALUE;

    private String ratingFitchLongTerm = null;

    private String[] wmBondtype = null;

    private String[] wmCoupontype = null;

    private long wmNominalInterest = Long.MIN_VALUE;

    private String[] wmInterestPeriod = null;

    private String[] wmCountry = null;

    private String wmissuecurrency = null;

    private String benchmarkName = null;

    private long dzWgzListid = Long.MIN_VALUE;

    private String[] issuerCategory = null;

    private String[] wmSector = null;

    private int wmIssueDate = Integer.MIN_VALUE;

    private boolean dzPib = false;

    private int ratingMoodysLongTermDate = Integer.MIN_VALUE;

    private String ratingMoodysLongTermAction = null;

    private String ratingMoodysShortTerm = null;

    private int ratingMoodysShortTermDate = Integer.MIN_VALUE;

    private String ratingMoodysShortTermAction = null;

    private int ratingFitchShortTermDate = Integer.MIN_VALUE;

    private String ratingFitchShortTermAction = null;

    private int ratingFitchLongTermDate = Integer.MIN_VALUE;

    private String ratingFitchLongTermAction = null;

    private String ratingSnPShortTerm = null;

    private int ratingSnPShortTermDate = Integer.MIN_VALUE;

    private String ratingSnPShortTermAction = null;

    private String ratingSnPLongTerm = null;

    private int ratingSnPLongTermDate = Integer.MIN_VALUE;

    private String ratingSnPLongTermAction = null;

    private String ratingSnPLongTermRegulatoryId = null;

    private String ratingSnPLongTermQualifier = null;

    private String wmIssuerName = null;

    private long wmSmallestTransferableUnit = Long.MIN_VALUE;

    private boolean wmNotActive = false;

    private String wmBondRank = null;

    private boolean isSpecialDismissal = false;

    private String ratingSnPLocalLongTerm = null;

    private int ratingSnPLocalLongTermDate = Integer.MIN_VALUE;

    private String ratingSnPLocalShortTerm = null;

    private int ratingSnPLocalShortTermDate = Integer.MIN_VALUE;

    private String ratingMoodysLongTermSource = null;

    private String ratingMoodysShortTermSource = null;

    private String lei = null;

    private long minAmountOfTransferableUnit = Long.MIN_VALUE;

    private String ratingSnPLongTermSource = null;

    private String ratingSnPShortTermSource = null;

    private String ratingSnPLocalLongTermSource = null;

    private String ratingSnPLocalShortTermSource = null;

    private String ratingSnPLocalLongTermAction = null;

    private String ratingSnPLocalShortTermAction = null;


    public int getExpires() {
        return this.expires;
    }

    public void setExpires(int value) {
        this.expires = value;
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

    public String getRatingFitchShortTerm() {
        return this.ratingFitchShortTerm;
    }

    public void setRatingFitchShortTerm(String value) {
        this.ratingFitchShortTerm = value;
    }

    public String getRatingMoodysLongTerm() {
        return this.ratingMoodysLongTerm;
    }

    public void setRatingMoodysLongTerm(String value) {
        this.ratingMoodysLongTerm = value;
    }

    public long getIssueVolume() {
        return this.issueVolume;
    }

    public void setIssueVolume(long value) {
        this.issueVolume = value;
    }

    public long getWmRedemptionPrice() {
        return this.wmRedemptionPrice;
    }

    public void setWmRedemptionPrice(long value) {
        this.wmRedemptionPrice = value;
    }

    public int getIssueDate() {
        return this.issueDate;
    }

    public void setIssueDate(int value) {
        this.issueDate = value;
    }

    public String getRatingFitchLongTerm() {
        return this.ratingFitchLongTerm;
    }

    public void setRatingFitchLongTerm(String value) {
        this.ratingFitchLongTerm = value;
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

    public String getBenchmarkName() {
        return this.benchmarkName;
    }

    public void setBenchmarkName(String value) {
        this.benchmarkName = value;
    }

    public long getDzWgzListid() {
        return this.dzWgzListid;
    }

    public void setDzWgzListid(long value) {
        this.dzWgzListid = value;
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

    public boolean getDzPib() {
        return this.dzPib;
    }

    public void setDzPib(boolean value) {
        this.dzPib = value;
    }

    public int getRatingMoodysLongTermDate() {
        return this.ratingMoodysLongTermDate;
    }

    public void setRatingMoodysLongTermDate(int value) {
        this.ratingMoodysLongTermDate = value;
    }

    public String getRatingMoodysLongTermAction() {
        return this.ratingMoodysLongTermAction;
    }

    public void setRatingMoodysLongTermAction(String value) {
        this.ratingMoodysLongTermAction = value;
    }

    public String getRatingMoodysShortTerm() {
        return this.ratingMoodysShortTerm;
    }

    public void setRatingMoodysShortTerm(String value) {
        this.ratingMoodysShortTerm = value;
    }

    public int getRatingMoodysShortTermDate() {
        return this.ratingMoodysShortTermDate;
    }

    public void setRatingMoodysShortTermDate(int value) {
        this.ratingMoodysShortTermDate = value;
    }

    public String getRatingMoodysShortTermAction() {
        return this.ratingMoodysShortTermAction;
    }

    public void setRatingMoodysShortTermAction(String value) {
        this.ratingMoodysShortTermAction = value;
    }

    public int getRatingFitchShortTermDate() {
        return this.ratingFitchShortTermDate;
    }

    public void setRatingFitchShortTermDate(int value) {
        this.ratingFitchShortTermDate = value;
    }

    public String getRatingFitchShortTermAction() {
        return this.ratingFitchShortTermAction;
    }

    public void setRatingFitchShortTermAction(String value) {
        this.ratingFitchShortTermAction = value;
    }

    public int getRatingFitchLongTermDate() {
        return this.ratingFitchLongTermDate;
    }

    public void setRatingFitchLongTermDate(int value) {
        this.ratingFitchLongTermDate = value;
    }

    public String getRatingFitchLongTermAction() {
        return this.ratingFitchLongTermAction;
    }

    public void setRatingFitchLongTermAction(String value) {
        this.ratingFitchLongTermAction = value;
    }

    public String getRatingSnPShortTerm() {
        return this.ratingSnPShortTerm;
    }

    public void setRatingSnPShortTerm(String value) {
        this.ratingSnPShortTerm = value;
    }

    public int getRatingSnPShortTermDate() {
        return this.ratingSnPShortTermDate;
    }

    public void setRatingSnPShortTermDate(int value) {
        this.ratingSnPShortTermDate = value;
    }

    public String getRatingSnPShortTermAction() {
        return this.ratingSnPShortTermAction;
    }

    public void setRatingSnPShortTermAction(String value) {
        this.ratingSnPShortTermAction = value;
    }

    public String getRatingSnPLongTerm() {
        return this.ratingSnPLongTerm;
    }

    public void setRatingSnPLongTerm(String value) {
        this.ratingSnPLongTerm = value;
    }

    public int getRatingSnPLongTermDate() {
        return this.ratingSnPLongTermDate;
    }

    public void setRatingSnPLongTermDate(int value) {
        this.ratingSnPLongTermDate = value;
    }

    public String getRatingSnPLongTermAction() {
        return this.ratingSnPLongTermAction;
    }

    public void setRatingSnPLongTermAction(String value) {
        this.ratingSnPLongTermAction = value;
    }

    public String getRatingSnPLongTermRegulatoryId() {
        return this.ratingSnPLongTermRegulatoryId;
    }

    public void setRatingSnPLongTermRegulatoryId(String value) {
        this.ratingSnPLongTermRegulatoryId = value;
    }

    public String getRatingSnPLongTermQualifier() {
        return this.ratingSnPLongTermQualifier;
    }

    public void setRatingSnPLongTermQualifier(String value) {
        this.ratingSnPLongTermQualifier = value;
    }

    public String getWmIssuerName() {
        return this.wmIssuerName;
    }

    public void setWmIssuerName(String value) {
        this.wmIssuerName = value;
    }

    public long getWmSmallestTransferableUnit() {
        return this.wmSmallestTransferableUnit;
    }

    public void setWmSmallestTransferableUnit(long value) {
        this.wmSmallestTransferableUnit = value;
    }

    public boolean getWmNotActive() {
        return this.wmNotActive;
    }

    public void setWmNotActive(boolean value) {
        this.wmNotActive = value;
    }

    public String getWmBondRank() {
        return this.wmBondRank;
    }

    public void setWmBondRank(String value) {
        this.wmBondRank = value;
    }

    public boolean getIsSpecialDismissal() {
        return this.isSpecialDismissal;
    }

    public void setIsSpecialDismissal(boolean value) {
        this.isSpecialDismissal = value;
    }

    public String getRatingSnPLocalLongTerm() {
        return this.ratingSnPLocalLongTerm;
    }

    public void setRatingSnPLocalLongTerm(String value) {
        this.ratingSnPLocalLongTerm = value;
    }

    public int getRatingSnPLocalLongTermDate() {
        return this.ratingSnPLocalLongTermDate;
    }

    public void setRatingSnPLocalLongTermDate(int value) {
        this.ratingSnPLocalLongTermDate = value;
    }

    public String getRatingSnPLocalShortTerm() {
        return this.ratingSnPLocalShortTerm;
    }

    public void setRatingSnPLocalShortTerm(String value) {
        this.ratingSnPLocalShortTerm = value;
    }

    public int getRatingSnPLocalShortTermDate() {
        return this.ratingSnPLocalShortTermDate;
    }

    public void setRatingSnPLocalShortTermDate(int value) {
        this.ratingSnPLocalShortTermDate = value;
    }

    public String getRatingMoodysLongTermSource() {
        return this.ratingMoodysLongTermSource;
    }

    public void setRatingMoodysLongTermSource(String value) {
        this.ratingMoodysLongTermSource = value;
    }

    public String getRatingMoodysShortTermSource() {
        return this.ratingMoodysShortTermSource;
    }

    public void setRatingMoodysShortTermSource(String value) {
        this.ratingMoodysShortTermSource = value;
    }

    public String getLei() {
        return this.lei;
    }

    public void setLei(String value) {
        this.lei = value;
    }

    public long getMinAmountOfTransferableUnit() {
        return this.minAmountOfTransferableUnit;
    }

    public void setMinAmountOfTransferableUnit(long value) {
        this.minAmountOfTransferableUnit = value;
    }

    public String getRatingSnPLongTermSource() {
        return this.ratingSnPLongTermSource;
    }

    public void setRatingSnPLongTermSource(String value) {
        this.ratingSnPLongTermSource = value;
    }

    public String getRatingSnPShortTermSource() {
        return this.ratingSnPShortTermSource;
    }

    public void setRatingSnPShortTermSource(String value) {
        this.ratingSnPShortTermSource = value;
    }

    public String getRatingSnPLocalLongTermSource() {
        return this.ratingSnPLocalLongTermSource;
    }

    public void setRatingSnPLocalLongTermSource(String value) {
        this.ratingSnPLocalLongTermSource = value;
    }

    public String getRatingSnPLocalShortTermSource() {
        return this.ratingSnPLocalShortTermSource;
    }

    public void setRatingSnPLocalShortTermSource(String value) {
        this.ratingSnPLocalShortTermSource = value;
    }

    public String getRatingSnPLocalLongTermAction() {
        return this.ratingSnPLocalLongTermAction;
    }

    public void setRatingSnPLocalLongTermAction(String value) {
        this.ratingSnPLocalLongTermAction = value;
    }

    public String getRatingSnPLocalShortTermAction() {
        return this.ratingSnPLocalShortTermAction;
    }

    public void setRatingSnPLocalShortTermAction(String value) {
        this.ratingSnPLocalShortTermAction = value;
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