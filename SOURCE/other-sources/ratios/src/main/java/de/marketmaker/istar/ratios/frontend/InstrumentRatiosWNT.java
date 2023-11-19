package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class InstrumentRatiosWNT extends InstrumentRatios implements InstrumentRatiosDerivative {
    protected static final long serialVersionUID = 1L;

    public InstrumentRatiosWNT(long id) {
        super(id);
    }

    public QuoteRatios createQuoteRatios(long quoteid) {
        return new QuoteRatiosWNT(quoteid, this);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportWNT.forInstrument(fid);
    }

    private long subscriptionRatio = Long.MIN_VALUE;

    private String currencyStrike = null;

    private int expires = Integer.MIN_VALUE;

    private String wkn = null;

    private String isin = null;

    private String name = null;

    private long strikePrice = Long.MIN_VALUE;

    private String osType = null;

    private boolean isAmerican = false;

    private String issuerName = null;

    private String underlyingWkn = null;

    private String underlyingIsin = null;

    private String underlyingName = null;

    private String underlyingType = null;

    private int issueDate = Integer.MIN_VALUE;

    private long underlyingIid = Long.MIN_VALUE;

    private boolean vrIssuer = false;

    private boolean wmVrIssuer = false;

    private int edgRatingDate = Integer.MIN_VALUE;

    private long edgScore1 = Long.MIN_VALUE;

    private long edgScore2 = Long.MIN_VALUE;

    private long edgScore3 = Long.MIN_VALUE;

    private long edgScore4 = Long.MIN_VALUE;

    private long edgScore5 = Long.MIN_VALUE;

    private long edgTopScore = Long.MIN_VALUE;

    private long edgTopClass = Long.MIN_VALUE;

    private String smfLeverageType = null;

    private boolean smfIsAmerican = false;

    private int smfExpires = Integer.MIN_VALUE;

    private long smfSubscriptionRatio = Long.MIN_VALUE;

    private long smfStrike = Long.MIN_VALUE;

    private boolean vwdStaticDataAvailable = false;

    private String sedexIssuerName = null;

    private long sedexStrike = Long.MIN_VALUE;

    private int sedexIssueDate = Integer.MIN_VALUE;

    private int sedexExpires = Integer.MIN_VALUE;

    private String underlyingEurexTicker = null;

    private boolean wmNotActive = false;


    public long getSubscriptionRatio() {
        return this.subscriptionRatio;
    }

    public void setSubscriptionRatio(long value) {
        this.subscriptionRatio = value;
    }

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

    public String getIssuerName() {
        return this.issuerName;
    }

    public void setIssuerName(String value) {
        this.issuerName = value;
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

    public String getUnderlyingType() {
        return this.underlyingType;
    }

    public void setUnderlyingType(String value) {
        this.underlyingType = value;
    }

    public int getIssueDate() {
        return this.issueDate;
    }

    public void setIssueDate(int value) {
        this.issueDate = value;
    }

    public long getUnderlyingIid() {
        return this.underlyingIid;
    }

    public void setUnderlyingIid(long value) {
        this.underlyingIid = value;
    }

    public boolean getVrIssuer() {
        return this.vrIssuer;
    }

    public void setVrIssuer(boolean value) {
        this.vrIssuer = value;
    }

    public boolean getWmVrIssuer() {
        return this.wmVrIssuer;
    }

    public void setWmVrIssuer(boolean value) {
        this.wmVrIssuer = value;
    }

    public int getEdgRatingDate() {
        return this.edgRatingDate;
    }

    public void setEdgRatingDate(int value) {
        this.edgRatingDate = value;
    }

    public long getEdgScore1() {
        return this.edgScore1;
    }

    public void setEdgScore1(long value) {
        this.edgScore1 = value;
    }

    public long getEdgScore2() {
        return this.edgScore2;
    }

    public void setEdgScore2(long value) {
        this.edgScore2 = value;
    }

    public long getEdgScore3() {
        return this.edgScore3;
    }

    public void setEdgScore3(long value) {
        this.edgScore3 = value;
    }

    public long getEdgScore4() {
        return this.edgScore4;
    }

    public void setEdgScore4(long value) {
        this.edgScore4 = value;
    }

    public long getEdgScore5() {
        return this.edgScore5;
    }

    public void setEdgScore5(long value) {
        this.edgScore5 = value;
    }

    public long getEdgTopScore() {
        return this.edgTopScore;
    }

    public void setEdgTopScore(long value) {
        this.edgTopScore = value;
    }

    public long getEdgTopClass() {
        return this.edgTopClass;
    }

    public void setEdgTopClass(long value) {
        this.edgTopClass = value;
    }

    public String getSmfLeverageType() {
        return this.smfLeverageType;
    }

    public void setSmfLeverageType(String value) {
        this.smfLeverageType = value;
    }

    public boolean getSmfIsAmerican() {
        return this.smfIsAmerican;
    }

    public void setSmfIsAmerican(boolean value) {
        this.smfIsAmerican = value;
    }

    public int getSmfExpires() {
        return this.smfExpires;
    }

    public void setSmfExpires(int value) {
        this.smfExpires = value;
    }

    public long getSmfSubscriptionRatio() {
        return this.smfSubscriptionRatio;
    }

    public void setSmfSubscriptionRatio(long value) {
        this.smfSubscriptionRatio = value;
    }

    public long getSmfStrike() {
        return this.smfStrike;
    }

    public void setSmfStrike(long value) {
        this.smfStrike = value;
    }

    public boolean getVwdStaticDataAvailable() {
        return this.vwdStaticDataAvailable;
    }

    public void setVwdStaticDataAvailable(boolean value) {
        this.vwdStaticDataAvailable = value;
    }

    public String getSedexIssuerName() {
        return this.sedexIssuerName;
    }

    public void setSedexIssuerName(String value) {
        this.sedexIssuerName = value;
    }

    public long getSedexStrike() {
        return this.sedexStrike;
    }

    public void setSedexStrike(long value) {
        this.sedexStrike = value;
    }

    public int getSedexIssueDate() {
        return this.sedexIssueDate;
    }

    public void setSedexIssueDate(int value) {
        this.sedexIssueDate = value;
    }

    public int getSedexExpires() {
        return this.sedexExpires;
    }

    public void setSedexExpires(int value) {
        this.sedexExpires = value;
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