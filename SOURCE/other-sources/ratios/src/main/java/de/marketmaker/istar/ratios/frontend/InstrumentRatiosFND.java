package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class InstrumentRatiosFND extends InstrumentRatios {
    protected static final long serialVersionUID = 1L;

    public InstrumentRatiosFND(long id) {
        super(id);
    }

    public QuoteRatios createQuoteRatios(long quoteid) {
        return new QuoteRatiosFND(quoteid, this);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportFND.forInstrument(fid);
    }

    private int expires = Integer.MIN_VALUE;

    private long attraxriskclass = Long.MIN_VALUE;

    private String wkn = null;

    private String isin = null;

    private String name = null;

    private String issuerName = null;

    private String[] country = null;

    private long issueVolume = Long.MIN_VALUE;

    private int issueDate = Integer.MIN_VALUE;

    private String[] sector = null;

    private long fwwIssueSurcharge = Long.MIN_VALUE;

    private long fwwManagementFee = Long.MIN_VALUE;

    private long fwwRiskclass = Long.MIN_VALUE;

    private long fwwAccountFee = Long.MIN_VALUE;

    private String fwwFundType = null;

    private String fwwSector = null;

    private String fwwKag = null;

    private String ratingFeri = null;

    private long morningstars = Long.MIN_VALUE;

    private boolean isEtf = false;

    private boolean vrIssuer = false;

    private boolean wmVrIssuer = false;

    private String bviKategorieGrob = null;

    private BitSet marketAdmission = RatioEnumSet.unmodifiableBitSet();

    private boolean msVrIssuer = false;

    private String msIssuername = null;

    private String[] msCountry = null;

    private String[] msDistributionStrategy = null;

    private long msFundVolume = Long.MIN_VALUE;

    private int msIssueDate = Integer.MIN_VALUE;

    private long msIssueSurcharge = Long.MIN_VALUE;

    private long msManagementfee = Long.MIN_VALUE;

    private long msAccountfee = Long.MIN_VALUE;

    private long msTer = Long.MIN_VALUE;

    private String[] msInvestmentFocus = null;

    private String msBenchmarkName = null;

    private long vwdBenchmarkQid = Long.MIN_VALUE;

    private long fwwTer = Long.MIN_VALUE;

    private String[] vwdbenlInvestmentFocus = null;

    private long vwdbenlBenchmarkQid = Long.MIN_VALUE;

    private String[] vwdbenlFundType = null;

    private String[] vwdbenlIssuername = null;

    private String[] vwdbenlCountry = null;

    private String[] vwdbenlDistStrategy = null;

    private long vwdbenlFundVolume = Long.MIN_VALUE;

    private int vwdbenlIssueDate = Integer.MIN_VALUE;

    private long vwdbenlIssueSurcharge = Long.MIN_VALUE;

    private long vwdbenlManagementfee = Long.MIN_VALUE;

    private long vwdbenlTer = Long.MIN_VALUE;

    private String vwdbenlBenchmarkName = null;

    private BitSet msMarketAdmission = RatioEnumSet.unmodifiableBitSet();

    private String[] subtype = null;

    private String[] vwditInvestmentFocus = null;

    private String[] vwditFundType = null;

    private String[] vwditIssuername = null;

    private String vwditBenchmarkName = null;

    private int vwditIssueDate = Integer.MIN_VALUE;

    private long vwditTer = Long.MIN_VALUE;

    private String vwditCfsRating = null;

    private String[] ssatFundType = null;

    private String[] ssatIssuername = null;

    private String[] ssatInvestmentFocus = null;

    private String[] ssatCountry = null;

    private String[] ssatDistributionStrategy = null;

    private long ssatFundVolume = Long.MIN_VALUE;

    private int ssatIssueDate = Integer.MIN_VALUE;

    private long ssatIssueSurcharge = Long.MIN_VALUE;

    private long ssatManagementfee = Long.MIN_VALUE;

    private long ssatAccountfee = Long.MIN_VALUE;

    private long ssatTer = Long.MIN_VALUE;

    private String ssatBenchmarkName = null;

    private long ssatBenchmarkQid = Long.MIN_VALUE;

    private BitSet ssatMarketAdmission = RatioEnumSet.unmodifiableBitSet();

    private long msOngoingCharge = Long.MIN_VALUE;

    private int msOngoingChargeDate = Integer.MIN_VALUE;

    private long vwdbenlsrrivalue = Long.MIN_VALUE;

    private int vwdbenlsrrivaluedate = Integer.MIN_VALUE;

    private long vwdbenldiamondrating = Long.MIN_VALUE;

    private int vwdbenldiamondratingdate = Integer.MIN_VALUE;

    private String[] fidaFundType = null;

    private String[] fidaIssuername = null;

    private String[] fidaInvestmentFocus = null;

    private int fidaIssueDate = Integer.MIN_VALUE;

    private long fidaTer = Long.MIN_VALUE;

    private String fidaBenchmarkName = null;

    private String[] fidaCountry = null;

    private String fidaDistributionStrategy = null;

    private long fidaFundVolume = Long.MIN_VALUE;

    private long fidaIssueSurcharge = Long.MIN_VALUE;

    private long fidaManagementfee = Long.MIN_VALUE;

    private long fidaRating = Long.MIN_VALUE;

    private String fidaPermissionType = null;

    private String[] msBroadassetclass = null;

    private boolean wmNotActive = false;

    private String[] vwdFundType = null;

    private String[] vwdIssuername = null;

    private String[] vwdCountry = null;

    private String[] vwdDistributionStrategy = null;

    private long vwdFundVolume = Long.MIN_VALUE;

    private int vwdIssueDate = Integer.MIN_VALUE;

    private long vwdIssueSurcharge = Long.MIN_VALUE;

    private long vwdManagementFee = Long.MIN_VALUE;

    private long vwdTer = Long.MIN_VALUE;

    private String[] vwdBenchmarkName = null;

    private long vwdOnGoingCharge = Long.MIN_VALUE;

    private int vwdOngoingChargeDate = Integer.MIN_VALUE;

    private BitSet vwdMarketAdmission = RatioEnumSet.unmodifiableBitSet();

    private String[] vwdInvestmentFocus = null;

    private long vwdAccountfee = Long.MIN_VALUE;

    private String wmInvestmentAssetPoolClass = null;

    private boolean vwdVrIssuer = false;

    private long morningstarsDZBANK = Long.MIN_VALUE;

    private long vwdsrrivalue = Long.MIN_VALUE;

    private int vwdsrrivaluedate = Integer.MIN_VALUE;

    private long vwddiamondrating = Long.MIN_VALUE;

    private int vwddiamondratingdate = Integer.MIN_VALUE;

    private long fidaRatingROnly = Long.MIN_VALUE;

    private long fidaRatingIOnly = Long.MIN_VALUE;


    public int getExpires() {
        return this.expires;
    }

    public void setExpires(int value) {
        this.expires = value;
    }

    public long getAttraxriskclass() {
        return this.attraxriskclass;
    }

    public void setAttraxriskclass(long value) {
        this.attraxriskclass = value;
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

    public long getFwwIssueSurcharge() {
        return this.fwwIssueSurcharge;
    }

    public void setFwwIssueSurcharge(long value) {
        this.fwwIssueSurcharge = value;
    }

    public long getFwwManagementFee() {
        return this.fwwManagementFee;
    }

    public void setFwwManagementFee(long value) {
        this.fwwManagementFee = value;
    }

    public long getFwwRiskclass() {
        return this.fwwRiskclass;
    }

    public void setFwwRiskclass(long value) {
        this.fwwRiskclass = value;
    }

    public long getFwwAccountFee() {
        return this.fwwAccountFee;
    }

    public void setFwwAccountFee(long value) {
        this.fwwAccountFee = value;
    }

    public String getFwwFundType() {
        return this.fwwFundType;
    }

    public void setFwwFundType(String value) {
        this.fwwFundType = value;
    }

    public String getFwwSector() {
        return this.fwwSector;
    }

    public void setFwwSector(String value) {
        this.fwwSector = value;
    }

    public String getFwwKag() {
        return this.fwwKag;
    }

    public void setFwwKag(String value) {
        this.fwwKag = value;
    }

    public String getRatingFeri() {
        return this.ratingFeri;
    }

    public void setRatingFeri(String value) {
        this.ratingFeri = value;
    }

    public long getMorningstars() {
        return this.morningstars;
    }

    public void setMorningstars(long value) {
        this.morningstars = value;
    }

    public boolean getIsEtf() {
        return this.isEtf;
    }

    public void setIsEtf(boolean value) {
        this.isEtf = value;
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

    public String getBviKategorieGrob() {
        return this.bviKategorieGrob;
    }

    public void setBviKategorieGrob(String value) {
        this.bviKategorieGrob = value;
    }

    public BitSet getMarketAdmission() {
        return this.marketAdmission;
    }

    public void setMarketAdmission(BitSet value) {
        this.marketAdmission = value;
    }

    public boolean getMsVrIssuer() {
        return this.msVrIssuer;
    }

    public void setMsVrIssuer(boolean value) {
        this.msVrIssuer = value;
    }

    public String getMsIssuername() {
        return this.msIssuername;
    }

    public void setMsIssuername(String value) {
        this.msIssuername = value;
    }

    public String getMsCountry(int localeIndex) {
        if (this.msCountry == null) {
            return null;
        }
        return this.msCountry[localeIndex];
    }

    public void setMsCountry(int localeIndex, String value) {
        if (this.msCountry == null) {
            this.msCountry = new String[RatioFieldDescription.msCountry.getLocales().length];
        }
        this.msCountry[localeIndex] = value;
    }

    public String getMsDistributionStrategy(int localeIndex) {
        if (this.msDistributionStrategy == null) {
            return null;
        }
        return this.msDistributionStrategy[localeIndex];
    }

    public void setMsDistributionStrategy(int localeIndex, String value) {
        if (this.msDistributionStrategy == null) {
            this.msDistributionStrategy = new String[RatioFieldDescription.msDistributionStrategy.getLocales().length];
        }
        this.msDistributionStrategy[localeIndex] = value;
    }

    public long getMsFundVolume() {
        return this.msFundVolume;
    }

    public void setMsFundVolume(long value) {
        this.msFundVolume = value;
    }

    public int getMsIssueDate() {
        return this.msIssueDate;
    }

    public void setMsIssueDate(int value) {
        this.msIssueDate = value;
    }

    public long getMsIssueSurcharge() {
        return this.msIssueSurcharge;
    }

    public void setMsIssueSurcharge(long value) {
        this.msIssueSurcharge = value;
    }

    public long getMsManagementfee() {
        return this.msManagementfee;
    }

    public void setMsManagementfee(long value) {
        this.msManagementfee = value;
    }

    public long getMsAccountfee() {
        return this.msAccountfee;
    }

    public void setMsAccountfee(long value) {
        this.msAccountfee = value;
    }

    public long getMsTer() {
        return this.msTer;
    }

    public void setMsTer(long value) {
        this.msTer = value;
    }

    public String getMsInvestmentFocus(int localeIndex) {
        if (this.msInvestmentFocus == null) {
            return null;
        }
        return this.msInvestmentFocus[localeIndex];
    }

    public void setMsInvestmentFocus(int localeIndex, String value) {
        if (this.msInvestmentFocus == null) {
            this.msInvestmentFocus = new String[RatioFieldDescription.msInvestmentFocus.getLocales().length];
        }
        this.msInvestmentFocus[localeIndex] = value;
    }

    public String getMsBenchmarkName() {
        return this.msBenchmarkName;
    }

    public void setMsBenchmarkName(String value) {
        this.msBenchmarkName = value;
    }

    public long getVwdBenchmarkQid() {
        return this.vwdBenchmarkQid;
    }

    public void setVwdBenchmarkQid(long value) {
        this.vwdBenchmarkQid = value;
    }

    public long getFwwTer() {
        return this.fwwTer;
    }

    public void setFwwTer(long value) {
        this.fwwTer = value;
    }

    public String getVwdbenlInvestmentFocus(int localeIndex) {
        if (this.vwdbenlInvestmentFocus == null) {
            return null;
        }
        return this.vwdbenlInvestmentFocus[localeIndex];
    }

    public void setVwdbenlInvestmentFocus(int localeIndex, String value) {
        if (this.vwdbenlInvestmentFocus == null) {
            this.vwdbenlInvestmentFocus = new String[RatioFieldDescription.vwdbenlInvestmentFocus.getLocales().length];
        }
        this.vwdbenlInvestmentFocus[localeIndex] = value;
    }

    public long getVwdbenlBenchmarkQid() {
        return this.vwdbenlBenchmarkQid;
    }

    public void setVwdbenlBenchmarkQid(long value) {
        this.vwdbenlBenchmarkQid = value;
    }

    public String getVwdbenlFundType(int localeIndex) {
        if (this.vwdbenlFundType == null) {
            return null;
        }
        return this.vwdbenlFundType[localeIndex];
    }

    public void setVwdbenlFundType(int localeIndex, String value) {
        if (this.vwdbenlFundType == null) {
            this.vwdbenlFundType = new String[RatioFieldDescription.vwdbenlFundType.getLocales().length];
        }
        this.vwdbenlFundType[localeIndex] = value;
    }

    public String getVwdbenlIssuername(int localeIndex) {
        if (this.vwdbenlIssuername == null) {
            return null;
        }
        return this.vwdbenlIssuername[localeIndex];
    }

    public void setVwdbenlIssuername(int localeIndex, String value) {
        if (this.vwdbenlIssuername == null) {
            this.vwdbenlIssuername = new String[RatioFieldDescription.vwdbenlIssuername.getLocales().length];
        }
        this.vwdbenlIssuername[localeIndex] = value;
    }

    public String getVwdbenlCountry(int localeIndex) {
        if (this.vwdbenlCountry == null) {
            return null;
        }
        return this.vwdbenlCountry[localeIndex];
    }

    public void setVwdbenlCountry(int localeIndex, String value) {
        if (this.vwdbenlCountry == null) {
            this.vwdbenlCountry = new String[RatioFieldDescription.vwdbenlCountry.getLocales().length];
        }
        this.vwdbenlCountry[localeIndex] = value;
    }

    public String getVwdbenlDistStrategy(int localeIndex) {
        if (this.vwdbenlDistStrategy == null) {
            return null;
        }
        return this.vwdbenlDistStrategy[localeIndex];
    }

    public void setVwdbenlDistStrategy(int localeIndex, String value) {
        if (this.vwdbenlDistStrategy == null) {
            this.vwdbenlDistStrategy = new String[RatioFieldDescription.vwdbenlDistStrategy.getLocales().length];
        }
        this.vwdbenlDistStrategy[localeIndex] = value;
    }

    public long getVwdbenlFundVolume() {
        return this.vwdbenlFundVolume;
    }

    public void setVwdbenlFundVolume(long value) {
        this.vwdbenlFundVolume = value;
    }

    public int getVwdbenlIssueDate() {
        return this.vwdbenlIssueDate;
    }

    public void setVwdbenlIssueDate(int value) {
        this.vwdbenlIssueDate = value;
    }

    public long getVwdbenlIssueSurcharge() {
        return this.vwdbenlIssueSurcharge;
    }

    public void setVwdbenlIssueSurcharge(long value) {
        this.vwdbenlIssueSurcharge = value;
    }

    public long getVwdbenlManagementfee() {
        return this.vwdbenlManagementfee;
    }

    public void setVwdbenlManagementfee(long value) {
        this.vwdbenlManagementfee = value;
    }

    public long getVwdbenlTer() {
        return this.vwdbenlTer;
    }

    public void setVwdbenlTer(long value) {
        this.vwdbenlTer = value;
    }

    public String getVwdbenlBenchmarkName() {
        return this.vwdbenlBenchmarkName;
    }

    public void setVwdbenlBenchmarkName(String value) {
        this.vwdbenlBenchmarkName = value;
    }

    public BitSet getMsMarketAdmission() {
        return this.msMarketAdmission;
    }

    public void setMsMarketAdmission(BitSet value) {
        this.msMarketAdmission = value;
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

    public String getVwditInvestmentFocus(int localeIndex) {
        if (this.vwditInvestmentFocus == null) {
            return null;
        }
        return this.vwditInvestmentFocus[localeIndex];
    }

    public void setVwditInvestmentFocus(int localeIndex, String value) {
        if (this.vwditInvestmentFocus == null) {
            this.vwditInvestmentFocus = new String[RatioFieldDescription.vwditInvestmentFocus.getLocales().length];
        }
        this.vwditInvestmentFocus[localeIndex] = value;
    }

    public String getVwditFundType(int localeIndex) {
        if (this.vwditFundType == null) {
            return null;
        }
        return this.vwditFundType[localeIndex];
    }

    public void setVwditFundType(int localeIndex, String value) {
        if (this.vwditFundType == null) {
            this.vwditFundType = new String[RatioFieldDescription.vwditFundType.getLocales().length];
        }
        this.vwditFundType[localeIndex] = value;
    }

    public String getVwditIssuername(int localeIndex) {
        if (this.vwditIssuername == null) {
            return null;
        }
        return this.vwditIssuername[localeIndex];
    }

    public void setVwditIssuername(int localeIndex, String value) {
        if (this.vwditIssuername == null) {
            this.vwditIssuername = new String[RatioFieldDescription.vwditIssuername.getLocales().length];
        }
        this.vwditIssuername[localeIndex] = value;
    }

    public String getVwditBenchmarkName() {
        return this.vwditBenchmarkName;
    }

    public void setVwditBenchmarkName(String value) {
        this.vwditBenchmarkName = value;
    }

    public int getVwditIssueDate() {
        return this.vwditIssueDate;
    }

    public void setVwditIssueDate(int value) {
        this.vwditIssueDate = value;
    }

    public long getVwditTer() {
        return this.vwditTer;
    }

    public void setVwditTer(long value) {
        this.vwditTer = value;
    }

    public String getVwditCfsRating() {
        return this.vwditCfsRating;
    }

    public void setVwditCfsRating(String value) {
        this.vwditCfsRating = value;
    }

    public String getSsatFundType(int localeIndex) {
        if (this.ssatFundType == null) {
            return null;
        }
        return this.ssatFundType[localeIndex];
    }

    public void setSsatFundType(int localeIndex, String value) {
        if (this.ssatFundType == null) {
            this.ssatFundType = new String[RatioFieldDescription.ssatFundType.getLocales().length];
        }
        this.ssatFundType[localeIndex] = value;
    }

    public String getSsatIssuername(int localeIndex) {
        if (this.ssatIssuername == null) {
            return null;
        }
        return this.ssatIssuername[localeIndex];
    }

    public void setSsatIssuername(int localeIndex, String value) {
        if (this.ssatIssuername == null) {
            this.ssatIssuername = new String[RatioFieldDescription.ssatIssuername.getLocales().length];
        }
        this.ssatIssuername[localeIndex] = value;
    }

    public String getSsatInvestmentFocus(int localeIndex) {
        if (this.ssatInvestmentFocus == null) {
            return null;
        }
        return this.ssatInvestmentFocus[localeIndex];
    }

    public void setSsatInvestmentFocus(int localeIndex, String value) {
        if (this.ssatInvestmentFocus == null) {
            this.ssatInvestmentFocus = new String[RatioFieldDescription.ssatInvestmentFocus.getLocales().length];
        }
        this.ssatInvestmentFocus[localeIndex] = value;
    }

    public String getSsatCountry(int localeIndex) {
        if (this.ssatCountry == null) {
            return null;
        }
        return this.ssatCountry[localeIndex];
    }

    public void setSsatCountry(int localeIndex, String value) {
        if (this.ssatCountry == null) {
            this.ssatCountry = new String[RatioFieldDescription.ssatCountry.getLocales().length];
        }
        this.ssatCountry[localeIndex] = value;
    }

    public String getSsatDistributionStrategy(int localeIndex) {
        if (this.ssatDistributionStrategy == null) {
            return null;
        }
        return this.ssatDistributionStrategy[localeIndex];
    }

    public void setSsatDistributionStrategy(int localeIndex, String value) {
        if (this.ssatDistributionStrategy == null) {
            this.ssatDistributionStrategy = new String[RatioFieldDescription.ssatDistributionStrategy.getLocales().length];
        }
        this.ssatDistributionStrategy[localeIndex] = value;
    }

    public long getSsatFundVolume() {
        return this.ssatFundVolume;
    }

    public void setSsatFundVolume(long value) {
        this.ssatFundVolume = value;
    }

    public int getSsatIssueDate() {
        return this.ssatIssueDate;
    }

    public void setSsatIssueDate(int value) {
        this.ssatIssueDate = value;
    }

    public long getSsatIssueSurcharge() {
        return this.ssatIssueSurcharge;
    }

    public void setSsatIssueSurcharge(long value) {
        this.ssatIssueSurcharge = value;
    }

    public long getSsatManagementfee() {
        return this.ssatManagementfee;
    }

    public void setSsatManagementfee(long value) {
        this.ssatManagementfee = value;
    }

    public long getSsatAccountfee() {
        return this.ssatAccountfee;
    }

    public void setSsatAccountfee(long value) {
        this.ssatAccountfee = value;
    }

    public long getSsatTer() {
        return this.ssatTer;
    }

    public void setSsatTer(long value) {
        this.ssatTer = value;
    }

    public String getSsatBenchmarkName() {
        return this.ssatBenchmarkName;
    }

    public void setSsatBenchmarkName(String value) {
        this.ssatBenchmarkName = value;
    }

    public long getSsatBenchmarkQid() {
        return this.ssatBenchmarkQid;
    }

    public void setSsatBenchmarkQid(long value) {
        this.ssatBenchmarkQid = value;
    }

    public BitSet getSsatMarketAdmission() {
        return this.ssatMarketAdmission;
    }

    public void setSsatMarketAdmission(BitSet value) {
        this.ssatMarketAdmission = value;
    }

    public long getMsOngoingCharge() {
        return this.msOngoingCharge;
    }

    public void setMsOngoingCharge(long value) {
        this.msOngoingCharge = value;
    }

    public int getMsOngoingChargeDate() {
        return this.msOngoingChargeDate;
    }

    public void setMsOngoingChargeDate(int value) {
        this.msOngoingChargeDate = value;
    }

    public long getVwdbenlsrrivalue() {
        return this.vwdbenlsrrivalue;
    }

    public void setVwdbenlsrrivalue(long value) {
        this.vwdbenlsrrivalue = value;
    }

    public int getVwdbenlsrrivaluedate() {
        return this.vwdbenlsrrivaluedate;
    }

    public void setVwdbenlsrrivaluedate(int value) {
        this.vwdbenlsrrivaluedate = value;
    }

    public long getVwdbenldiamondrating() {
        return this.vwdbenldiamondrating;
    }

    public void setVwdbenldiamondrating(long value) {
        this.vwdbenldiamondrating = value;
    }

    public int getVwdbenldiamondratingdate() {
        return this.vwdbenldiamondratingdate;
    }

    public void setVwdbenldiamondratingdate(int value) {
        this.vwdbenldiamondratingdate = value;
    }

    public String getFidaFundType(int localeIndex) {
        if (this.fidaFundType == null) {
            return null;
        }
        return this.fidaFundType[localeIndex];
    }

    public void setFidaFundType(int localeIndex, String value) {
        if (this.fidaFundType == null) {
            this.fidaFundType = new String[RatioFieldDescription.fidaFundType.getLocales().length];
        }
        this.fidaFundType[localeIndex] = value;
    }

    public String getFidaIssuername(int localeIndex) {
        if (this.fidaIssuername == null) {
            return null;
        }
        return this.fidaIssuername[localeIndex];
    }

    public void setFidaIssuername(int localeIndex, String value) {
        if (this.fidaIssuername == null) {
            this.fidaIssuername = new String[RatioFieldDescription.fidaIssuername.getLocales().length];
        }
        this.fidaIssuername[localeIndex] = value;
    }

    public String getFidaInvestmentFocus(int localeIndex) {
        if (this.fidaInvestmentFocus == null) {
            return null;
        }
        return this.fidaInvestmentFocus[localeIndex];
    }

    public void setFidaInvestmentFocus(int localeIndex, String value) {
        if (this.fidaInvestmentFocus == null) {
            this.fidaInvestmentFocus = new String[RatioFieldDescription.fidaInvestmentFocus.getLocales().length];
        }
        this.fidaInvestmentFocus[localeIndex] = value;
    }

    public int getFidaIssueDate() {
        return this.fidaIssueDate;
    }

    public void setFidaIssueDate(int value) {
        this.fidaIssueDate = value;
    }

    public long getFidaTer() {
        return this.fidaTer;
    }

    public void setFidaTer(long value) {
        this.fidaTer = value;
    }

    public String getFidaBenchmarkName() {
        return this.fidaBenchmarkName;
    }

    public void setFidaBenchmarkName(String value) {
        this.fidaBenchmarkName = value;
    }

    public String getFidaCountry(int localeIndex) {
        if (this.fidaCountry == null) {
            return null;
        }
        return this.fidaCountry[localeIndex];
    }

    public void setFidaCountry(int localeIndex, String value) {
        if (this.fidaCountry == null) {
            this.fidaCountry = new String[RatioFieldDescription.fidaCountry.getLocales().length];
        }
        this.fidaCountry[localeIndex] = value;
    }

    public String getFidaDistributionStrategy() {
        return this.fidaDistributionStrategy;
    }

    public void setFidaDistributionStrategy(String value) {
        this.fidaDistributionStrategy = value;
    }

    public long getFidaFundVolume() {
        return this.fidaFundVolume;
    }

    public void setFidaFundVolume(long value) {
        this.fidaFundVolume = value;
    }

    public long getFidaIssueSurcharge() {
        return this.fidaIssueSurcharge;
    }

    public void setFidaIssueSurcharge(long value) {
        this.fidaIssueSurcharge = value;
    }

    public long getFidaManagementfee() {
        return this.fidaManagementfee;
    }

    public void setFidaManagementfee(long value) {
        this.fidaManagementfee = value;
    }

    public long getFidaRating() {
        return this.fidaRating;
    }

    public void setFidaRating(long value) {
        this.fidaRating = value;
    }

    public String getFidaPermissionType() {
        return this.fidaPermissionType;
    }

    public void setFidaPermissionType(String value) {
        this.fidaPermissionType = value;
    }

    public String getMsBroadassetclass(int localeIndex) {
        if (this.msBroadassetclass == null) {
            return null;
        }
        return this.msBroadassetclass[localeIndex];
    }

    public void setMsBroadassetclass(int localeIndex, String value) {
        if (this.msBroadassetclass == null) {
            this.msBroadassetclass = new String[RatioFieldDescription.msBroadassetclass.getLocales().length];
        }
        this.msBroadassetclass[localeIndex] = value;
    }

    public boolean getWmNotActive() {
        return this.wmNotActive;
    }

    public void setWmNotActive(boolean value) {
        this.wmNotActive = value;
    }

    public String getVwdFundType(int localeIndex) {
        if (this.vwdFundType == null) {
            return null;
        }
        return this.vwdFundType[localeIndex];
    }

    public void setVwdFundType(int localeIndex, String value) {
        if (this.vwdFundType == null) {
            this.vwdFundType = new String[RatioFieldDescription.vwdFundType.getLocales().length];
        }
        this.vwdFundType[localeIndex] = value;
    }

    public String getVwdIssuername(int localeIndex) {
        if (this.vwdIssuername == null) {
            return null;
        }
        return this.vwdIssuername[localeIndex];
    }

    public void setVwdIssuername(int localeIndex, String value) {
        if (this.vwdIssuername == null) {
            this.vwdIssuername = new String[RatioFieldDescription.vwdIssuername.getLocales().length];
        }
        this.vwdIssuername[localeIndex] = value;
    }

    public String getVwdCountry(int localeIndex) {
        if (this.vwdCountry == null) {
            return null;
        }
        return this.vwdCountry[localeIndex];
    }

    public void setVwdCountry(int localeIndex, String value) {
        if (this.vwdCountry == null) {
            this.vwdCountry = new String[RatioFieldDescription.vwdCountry.getLocales().length];
        }
        this.vwdCountry[localeIndex] = value;
    }

    public String getVwdDistributionStrategy(int localeIndex) {
        if (this.vwdDistributionStrategy == null) {
            return null;
        }
        return this.vwdDistributionStrategy[localeIndex];
    }

    public void setVwdDistributionStrategy(int localeIndex, String value) {
        if (this.vwdDistributionStrategy == null) {
            this.vwdDistributionStrategy = new String[RatioFieldDescription.vwdDistributionStrategy.getLocales().length];
        }
        this.vwdDistributionStrategy[localeIndex] = value;
    }

    public long getVwdFundVolume() {
        return this.vwdFundVolume;
    }

    public void setVwdFundVolume(long value) {
        this.vwdFundVolume = value;
    }

    public int getVwdIssueDate() {
        return this.vwdIssueDate;
    }

    public void setVwdIssueDate(int value) {
        this.vwdIssueDate = value;
    }

    public long getVwdIssueSurcharge() {
        return this.vwdIssueSurcharge;
    }

    public void setVwdIssueSurcharge(long value) {
        this.vwdIssueSurcharge = value;
    }

    public long getVwdManagementFee() {
        return this.vwdManagementFee;
    }

    public void setVwdManagementFee(long value) {
        this.vwdManagementFee = value;
    }

    public long getVwdTer() {
        return this.vwdTer;
    }

    public void setVwdTer(long value) {
        this.vwdTer = value;
    }

    public String getVwdBenchmarkName(int localeIndex) {
        if (this.vwdBenchmarkName == null) {
            return null;
        }
        return this.vwdBenchmarkName[localeIndex];
    }

    public void setVwdBenchmarkName(int localeIndex, String value) {
        if (this.vwdBenchmarkName == null) {
            this.vwdBenchmarkName = new String[RatioFieldDescription.vwdBenchmarkName.getLocales().length];
        }
        this.vwdBenchmarkName[localeIndex] = value;
    }

    public long getVwdOnGoingCharge() {
        return this.vwdOnGoingCharge;
    }

    public void setVwdOnGoingCharge(long value) {
        this.vwdOnGoingCharge = value;
    }

    public int getVwdOngoingChargeDate() {
        return this.vwdOngoingChargeDate;
    }

    public void setVwdOngoingChargeDate(int value) {
        this.vwdOngoingChargeDate = value;
    }

    public BitSet getVwdMarketAdmission() {
        return this.vwdMarketAdmission;
    }

    public void setVwdMarketAdmission(BitSet value) {
        this.vwdMarketAdmission = value;
    }

    public String getVwdInvestmentFocus(int localeIndex) {
        if (this.vwdInvestmentFocus == null) {
            return null;
        }
        return this.vwdInvestmentFocus[localeIndex];
    }

    public void setVwdInvestmentFocus(int localeIndex, String value) {
        if (this.vwdInvestmentFocus == null) {
            this.vwdInvestmentFocus = new String[RatioFieldDescription.vwdInvestmentFocus.getLocales().length];
        }
        this.vwdInvestmentFocus[localeIndex] = value;
    }

    public long getVwdAccountfee() {
        return this.vwdAccountfee;
    }

    public void setVwdAccountfee(long value) {
        this.vwdAccountfee = value;
    }

    public String getWmInvestmentAssetPoolClass() {
        return this.wmInvestmentAssetPoolClass;
    }

    public void setWmInvestmentAssetPoolClass(String value) {
        this.wmInvestmentAssetPoolClass = value;
    }

    public boolean getVwdVrIssuer() {
        return this.vwdVrIssuer;
    }

    public void setVwdVrIssuer(boolean value) {
        this.vwdVrIssuer = value;
    }

    public long getMorningstarsDZBANK() {
        return this.morningstarsDZBANK;
    }

    public void setMorningstarsDZBANK(long value) {
        this.morningstarsDZBANK = value;
    }

    public long getVwdsrrivalue() {
        return this.vwdsrrivalue;
    }

    public void setVwdsrrivalue(long value) {
        this.vwdsrrivalue = value;
    }

    public int getVwdsrrivaluedate() {
        return this.vwdsrrivaluedate;
    }

    public void setVwdsrrivaluedate(int value) {
        this.vwdsrrivaluedate = value;
    }

    public long getVwddiamondrating() {
        return this.vwddiamondrating;
    }

    public void setVwddiamondrating(long value) {
        this.vwddiamondrating = value;
    }

    public int getVwddiamondratingdate() {
        return this.vwddiamondratingdate;
    }

    public void setVwddiamondratingdate(int value) {
        this.vwddiamondratingdate = value;
    }

    public long getFidaRatingROnly() {
        return this.fidaRatingROnly;
    }

    public void setFidaRatingROnly(long value) {
        this.fidaRatingROnly = value;
    }

    public long getFidaRatingIOnly() {
        return this.fidaRatingIOnly;
    }

    public void setFidaRatingIOnly(long value) {
        this.fidaRatingIOnly = value;
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