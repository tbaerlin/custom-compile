package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class InstrumentRatiosCER extends InstrumentRatios implements InstrumentRatiosDerivative {
    protected static final long serialVersionUID = 1L;

    public InstrumentRatiosCER(long id) {
        super(id);
    }

    public QuoteRatios createQuoteRatios(long quoteid) {
        return new QuoteRatiosCER(quoteid, this);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportCER.forInstrument(fid);
    }

    private long subscriptionRatio = Long.MIN_VALUE;

    private String issuerProductname = null;

    private String currencyStrike = null;

    private long cap = Long.MIN_VALUE;

    private int expires = Integer.MIN_VALUE;

    private String wkn = null;

    private String isin = null;

    private String name = null;

    private long strikePrice = Long.MIN_VALUE;

    private String osType = null;

    private String issuerName = null;

    private String underlyingWkn = null;

    private String underlyingIsin = null;

    private String underlyingName = null;

    private long interest = Long.MIN_VALUE;

    private String underlyingType = null;

    private boolean gatrixxQuanto = false;

    private String exerciseType = null;

    private long issueVolume = Long.MIN_VALUE;

    private long lowerKnock = Long.MIN_VALUE;

    private long upperKnock = Long.MIN_VALUE;

    private long lowerRange = Long.MIN_VALUE;

    private long upperRange = Long.MIN_VALUE;

    private int issueDate = Integer.MIN_VALUE;

    private String gatrixxType = null;

    private long gatrixxStrikePrice = Long.MIN_VALUE;

    private long gatrixxCoupon = Long.MIN_VALUE;

    private long gatrixxCap = Long.MIN_VALUE;

    private long gatrixxKnockin = Long.MIN_VALUE;

    private long gatrixxBonuslevel = Long.MIN_VALUE;

    private long gatrixxBarrier = Long.MIN_VALUE;

    private String gatrixxGuaranteeType = null;

    private String gatrixxLeverageType = null;

    private long underlyingIid = Long.MIN_VALUE;

    private boolean vrIssuer = false;

    private boolean wmVrIssuer = false;

    private String[] dzCategory = null;

    private long gatrixxprotectlevel = Long.MIN_VALUE;

    private String gatrixxtypeftreff = null;

    private long gatrixxParticipationlevel = Long.MIN_VALUE;

    private String[] gatrixxTypename = null;

    private String gatrixxMultiassetName = null;

    private long gatrixxStoploss = Long.MIN_VALUE;

    private long dzWgzListid = Long.MIN_VALUE;

    private boolean gatrixxIsknockout = false;

    private int edgRatingDate = Integer.MIN_VALUE;

    private long edgScore1 = Long.MIN_VALUE;

    private long edgScore2 = Long.MIN_VALUE;

    private long edgScore3 = Long.MIN_VALUE;

    private long edgScore4 = Long.MIN_VALUE;

    private long edgScore5 = Long.MIN_VALUE;

    private long edgTopScore = Long.MIN_VALUE;

    private long edgTopClass = Long.MIN_VALUE;

    private long gatrixxParticipationFactor = Long.MIN_VALUE;

    private int gatrixxKnockindate = Integer.MIN_VALUE;

    private long gatrixxStartvalue = Long.MIN_VALUE;

    private long gatrixxStopvalue = Long.MIN_VALUE;

    private long gatrixxRefundMaximum = Long.MIN_VALUE;

    private String smfIssuerProductname = null;

    private String smfLeverageType = null;

    private boolean smfIsQuanto = false;

    private int smfExpires = Integer.MIN_VALUE;

    private long smfParticipationrate = Long.MIN_VALUE;

    private long smfSubscriptionRatio = Long.MIN_VALUE;

    private long smfCoupon = Long.MIN_VALUE;

    private String smfMultiassetName = null;

    private String smfCertificateType = null;

    private String[] type = null;

    private String typeKey = null;

    private String[] subtype = null;

    private String typeDZ = null;

    private String typeKeyDZ = null;

    private String subtypeDZ = null;

    private String typeWGZ = null;

    private String typeKeyWGZ = null;

    private String subtypeWGZ = null;

    private long gatrixxBonusbufferRelative = Long.MIN_VALUE;

    private boolean vwdStaticDataAvailable = false;

    private boolean dzPib = false;

    private boolean dzIsLeverageProduct = false;

    private String subtypeKey = null;

    private String subtypeKeyDZ = null;

    private String subtypeKeyWGZ = null;

    private boolean gatrixxIsEndless = false;

    private String sedexIssuerName = null;

    private long sedexStrike = Long.MIN_VALUE;

    private int sedexIssueDate = Integer.MIN_VALUE;

    private int sedexExpires = Integer.MIN_VALUE;

    private String typeSEDEX = null;

    private String typeKeySEDEX = null;

    private String underlyingEurexTicker = null;

    private boolean wmNotActive = false;


    public long getSubscriptionRatio() {
        return this.subscriptionRatio;
    }

    public void setSubscriptionRatio(long value) {
        this.subscriptionRatio = value;
    }

    public String getIssuerProductname() {
        return this.issuerProductname;
    }

    public void setIssuerProductname(String value) {
        this.issuerProductname = value;
    }

    public String getCurrencyStrike() {
        return this.currencyStrike;
    }

    public void setCurrencyStrike(String value) {
        this.currencyStrike = value;
    }

    public long getCap() {
        return this.cap;
    }

    public void setCap(long value) {
        this.cap = value;
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

    public long getInterest() {
        return this.interest;
    }

    public void setInterest(long value) {
        this.interest = value;
    }

    public String getUnderlyingType() {
        return this.underlyingType;
    }

    public void setUnderlyingType(String value) {
        this.underlyingType = value;
    }

    public boolean getGatrixxQuanto() {
        return this.gatrixxQuanto;
    }

    public void setGatrixxQuanto(boolean value) {
        this.gatrixxQuanto = value;
    }

    public String getExerciseType() {
        return this.exerciseType;
    }

    public void setExerciseType(String value) {
        this.exerciseType = value;
    }

    public long getIssueVolume() {
        return this.issueVolume;
    }

    public void setIssueVolume(long value) {
        this.issueVolume = value;
    }

    public long getLowerKnock() {
        return this.lowerKnock;
    }

    public void setLowerKnock(long value) {
        this.lowerKnock = value;
    }

    public long getUpperKnock() {
        return this.upperKnock;
    }

    public void setUpperKnock(long value) {
        this.upperKnock = value;
    }

    public long getLowerRange() {
        return this.lowerRange;
    }

    public void setLowerRange(long value) {
        this.lowerRange = value;
    }

    public long getUpperRange() {
        return this.upperRange;
    }

    public void setUpperRange(long value) {
        this.upperRange = value;
    }

    public int getIssueDate() {
        return this.issueDate;
    }

    public void setIssueDate(int value) {
        this.issueDate = value;
    }

    public String getGatrixxType() {
        return this.gatrixxType;
    }

    public void setGatrixxType(String value) {
        this.gatrixxType = value;
    }

    public long getGatrixxStrikePrice() {
        return this.gatrixxStrikePrice;
    }

    public void setGatrixxStrikePrice(long value) {
        this.gatrixxStrikePrice = value;
    }

    public long getGatrixxCoupon() {
        return this.gatrixxCoupon;
    }

    public void setGatrixxCoupon(long value) {
        this.gatrixxCoupon = value;
    }

    public long getGatrixxCap() {
        return this.gatrixxCap;
    }

    public void setGatrixxCap(long value) {
        this.gatrixxCap = value;
    }

    public long getGatrixxKnockin() {
        return this.gatrixxKnockin;
    }

    public void setGatrixxKnockin(long value) {
        this.gatrixxKnockin = value;
    }

    public long getGatrixxBonuslevel() {
        return this.gatrixxBonuslevel;
    }

    public void setGatrixxBonuslevel(long value) {
        this.gatrixxBonuslevel = value;
    }

    public long getGatrixxBarrier() {
        return this.gatrixxBarrier;
    }

    public void setGatrixxBarrier(long value) {
        this.gatrixxBarrier = value;
    }

    public String getGatrixxGuaranteeType() {
        return this.gatrixxGuaranteeType;
    }

    public void setGatrixxGuaranteeType(String value) {
        this.gatrixxGuaranteeType = value;
    }

    public String getGatrixxLeverageType() {
        return this.gatrixxLeverageType;
    }

    public void setGatrixxLeverageType(String value) {
        this.gatrixxLeverageType = value;
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

    public String getDzCategory(int localeIndex) {
        if (this.dzCategory == null) {
            return null;
        }
        return this.dzCategory[localeIndex];
    }

    public void setDzCategory(int localeIndex, String value) {
        if (this.dzCategory == null) {
            this.dzCategory = new String[RatioFieldDescription.dzCategory.getLocales().length];
        }
        this.dzCategory[localeIndex] = value;
    }

    public long getGatrixxprotectlevel() {
        return this.gatrixxprotectlevel;
    }

    public void setGatrixxprotectlevel(long value) {
        this.gatrixxprotectlevel = value;
    }

    public String getGatrixxtypeftreff() {
        return this.gatrixxtypeftreff;
    }

    public void setGatrixxtypeftreff(String value) {
        this.gatrixxtypeftreff = value;
    }

    public long getGatrixxParticipationlevel() {
        return this.gatrixxParticipationlevel;
    }

    public void setGatrixxParticipationlevel(long value) {
        this.gatrixxParticipationlevel = value;
    }

    public String getGatrixxTypename(int localeIndex) {
        if (this.gatrixxTypename == null) {
            return null;
        }
        return this.gatrixxTypename[localeIndex];
    }

    public void setGatrixxTypename(int localeIndex, String value) {
        if (this.gatrixxTypename == null) {
            this.gatrixxTypename = new String[RatioFieldDescription.gatrixxTypename.getLocales().length];
        }
        this.gatrixxTypename[localeIndex] = value;
    }

    public String getGatrixxMultiassetName() {
        return this.gatrixxMultiassetName;
    }

    public void setGatrixxMultiassetName(String value) {
        this.gatrixxMultiassetName = value;
    }

    public long getGatrixxStoploss() {
        return this.gatrixxStoploss;
    }

    public void setGatrixxStoploss(long value) {
        this.gatrixxStoploss = value;
    }

    public long getDzWgzListid() {
        return this.dzWgzListid;
    }

    public void setDzWgzListid(long value) {
        this.dzWgzListid = value;
    }

    public boolean getGatrixxIsknockout() {
        return this.gatrixxIsknockout;
    }

    public void setGatrixxIsknockout(boolean value) {
        this.gatrixxIsknockout = value;
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

    public long getGatrixxParticipationFactor() {
        return this.gatrixxParticipationFactor;
    }

    public void setGatrixxParticipationFactor(long value) {
        this.gatrixxParticipationFactor = value;
    }

    public int getGatrixxKnockindate() {
        return this.gatrixxKnockindate;
    }

    public void setGatrixxKnockindate(int value) {
        this.gatrixxKnockindate = value;
    }

    public long getGatrixxStartvalue() {
        return this.gatrixxStartvalue;
    }

    public void setGatrixxStartvalue(long value) {
        this.gatrixxStartvalue = value;
    }

    public long getGatrixxStopvalue() {
        return this.gatrixxStopvalue;
    }

    public void setGatrixxStopvalue(long value) {
        this.gatrixxStopvalue = value;
    }

    public long getGatrixxRefundMaximum() {
        return this.gatrixxRefundMaximum;
    }

    public void setGatrixxRefundMaximum(long value) {
        this.gatrixxRefundMaximum = value;
    }

    public String getSmfIssuerProductname() {
        return this.smfIssuerProductname;
    }

    public void setSmfIssuerProductname(String value) {
        this.smfIssuerProductname = value;
    }

    public String getSmfLeverageType() {
        return this.smfLeverageType;
    }

    public void setSmfLeverageType(String value) {
        this.smfLeverageType = value;
    }

    public boolean getSmfIsQuanto() {
        return this.smfIsQuanto;
    }

    public void setSmfIsQuanto(boolean value) {
        this.smfIsQuanto = value;
    }

    public int getSmfExpires() {
        return this.smfExpires;
    }

    public void setSmfExpires(int value) {
        this.smfExpires = value;
    }

    public long getSmfParticipationrate() {
        return this.smfParticipationrate;
    }

    public void setSmfParticipationrate(long value) {
        this.smfParticipationrate = value;
    }

    public long getSmfSubscriptionRatio() {
        return this.smfSubscriptionRatio;
    }

    public void setSmfSubscriptionRatio(long value) {
        this.smfSubscriptionRatio = value;
    }

    public long getSmfCoupon() {
        return this.smfCoupon;
    }

    public void setSmfCoupon(long value) {
        this.smfCoupon = value;
    }

    public String getSmfMultiassetName() {
        return this.smfMultiassetName;
    }

    public void setSmfMultiassetName(String value) {
        this.smfMultiassetName = value;
    }

    public String getSmfCertificateType() {
        return this.smfCertificateType;
    }

    public void setSmfCertificateType(String value) {
        this.smfCertificateType = value;
    }

    public String getType(int localeIndex) {
        if (this.type == null) {
            return null;
        }
        return this.type[localeIndex];
    }

    public void setType(int localeIndex, String value) {
        if (this.type == null) {
            this.type = new String[RatioFieldDescription.type.getLocales().length];
        }
        this.type[localeIndex] = value;
    }

    public String getTypeKey() {
        return this.typeKey;
    }

    public void setTypeKey(String value) {
        this.typeKey = value;
    }

    public String getSubtype(int localeIndex) {
        if (this.subtype == null) {
            return null;
        }
        return this.subtype[localeIndex];
    }

    public void setSubtype(int localeIndex, String value) {
        if (this.subtype == null) {
            this.subtype = new String[RatioFieldDescription.subtype.getLocales().length];
        }
        this.subtype[localeIndex] = value;
    }

    public String getTypeDZ() {
        return this.typeDZ;
    }

    public void setTypeDZ(String value) {
        this.typeDZ = value;
    }

    public String getTypeKeyDZ() {
        return this.typeKeyDZ;
    }

    public void setTypeKeyDZ(String value) {
        this.typeKeyDZ = value;
    }

    public String getSubtypeDZ() {
        return this.subtypeDZ;
    }

    public void setSubtypeDZ(String value) {
        this.subtypeDZ = value;
    }

    public String getTypeWGZ() {
        return this.typeWGZ;
    }

    public void setTypeWGZ(String value) {
        this.typeWGZ = value;
    }

    public String getTypeKeyWGZ() {
        return this.typeKeyWGZ;
    }

    public void setTypeKeyWGZ(String value) {
        this.typeKeyWGZ = value;
    }

    public String getSubtypeWGZ() {
        return this.subtypeWGZ;
    }

    public void setSubtypeWGZ(String value) {
        this.subtypeWGZ = value;
    }

    public long getGatrixxBonusbufferRelative() {
        return this.gatrixxBonusbufferRelative;
    }

    public void setGatrixxBonusbufferRelative(long value) {
        this.gatrixxBonusbufferRelative = value;
    }

    public boolean getVwdStaticDataAvailable() {
        return this.vwdStaticDataAvailable;
    }

    public void setVwdStaticDataAvailable(boolean value) {
        this.vwdStaticDataAvailable = value;
    }

    public boolean getDzPib() {
        return this.dzPib;
    }

    public void setDzPib(boolean value) {
        this.dzPib = value;
    }

    public boolean getDzIsLeverageProduct() {
        return this.dzIsLeverageProduct;
    }

    public void setDzIsLeverageProduct(boolean value) {
        this.dzIsLeverageProduct = value;
    }

    public String getSubtypeKey() {
        return this.subtypeKey;
    }

    public void setSubtypeKey(String value) {
        this.subtypeKey = value;
    }

    public String getSubtypeKeyDZ() {
        return this.subtypeKeyDZ;
    }

    public void setSubtypeKeyDZ(String value) {
        this.subtypeKeyDZ = value;
    }

    public String getSubtypeKeyWGZ() {
        return this.subtypeKeyWGZ;
    }

    public void setSubtypeKeyWGZ(String value) {
        this.subtypeKeyWGZ = value;
    }

    public boolean getGatrixxIsEndless() {
        return this.gatrixxIsEndless;
    }

    public void setGatrixxIsEndless(boolean value) {
        this.gatrixxIsEndless = value;
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

    public String getTypeSEDEX() {
        return this.typeSEDEX;
    }

    public void setTypeSEDEX(String value) {
        this.typeSEDEX = value;
    }

    public String getTypeKeySEDEX() {
        return this.typeKeySEDEX;
    }

    public void setTypeKeySEDEX(String value) {
        this.typeKeySEDEX = value;
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