package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class InstrumentRatiosSTK extends InstrumentRatios {
    protected static final long serialVersionUID = 1L;

    public InstrumentRatiosSTK(long id) {
        super(id);
    }

    public QuoteRatios createQuoteRatios(long quoteid) {
        return new QuoteRatiosSTK(quoteid, this);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupportSTK.forInstrument(fid);
    }

    private long wmIssueVolume = Long.MIN_VALUE;

    private long screenerinterest = Long.MIN_VALUE;

    private String wkn = null;

    private String isin = null;

    private String name = null;

    private String[] country = null;

    private String[] sector = null;

    private long factsetProfit2Y = Long.MIN_VALUE;

    private long factsetEps2Y = Long.MIN_VALUE;

    private long factsetDividend2Y = Long.MIN_VALUE;

    private long factsetDividendyield2Y = Long.MIN_VALUE;

    private long factsetProfit1Y = Long.MIN_VALUE;

    private long factsetEps1Y = Long.MIN_VALUE;

    private long factsetDividend1Y = Long.MIN_VALUE;

    private long factsetDividendyield1Y = Long.MIN_VALUE;

    private long factsetProfit0Y = Long.MIN_VALUE;

    private long factsetEps0Y = Long.MIN_VALUE;

    private long factsetDividend0Y = Long.MIN_VALUE;

    private long factsetDividendyield0Y = Long.MIN_VALUE;

    private long convensysProfit_1Y = Long.MIN_VALUE;

    private long convensysEps_1Y = Long.MIN_VALUE;

    private long convensysDividend_1Y = Long.MIN_VALUE;

    private long convensysDividendyield_1Y = Long.MIN_VALUE;

    private long convensysProfit_2Y = Long.MIN_VALUE;

    private long convensysEps_2Y = Long.MIN_VALUE;

    private long convensysDividend_2Y = Long.MIN_VALUE;

    private long convensysDividendyield_2Y = Long.MIN_VALUE;

    private long factsetPriceEarningRatio0Y = Long.MIN_VALUE;

    private long factsetCashflow0Y = Long.MIN_VALUE;

    private long factsetLongTermGrowth = Long.MIN_VALUE;

    private String[] wmCountry = null;

    private long factsetPriceTarget = Long.MIN_VALUE;

    private long factsetPriceEarningRatio1Y = Long.MIN_VALUE;

    private long factsetCashflow1Y = Long.MIN_VALUE;

    private long factsetEPSAfterGoodwill1Y = Long.MIN_VALUE;

    private long factsetEPSBeforeGoodwill1Y = Long.MIN_VALUE;

    private long factsetFiscalYear = Long.MIN_VALUE;

    private long factsetBookValue1Y = Long.MIN_VALUE;

    private long factsetEbit1Y = Long.MIN_VALUE;

    private long factsetEbitda1Y = Long.MIN_VALUE;

    private long factsetGoodwill1Y = Long.MIN_VALUE;

    private long factsetNetDebt1Y = Long.MIN_VALUE;

    private String factsetCurrency = null;

    private long factsetSales1Y = Long.MIN_VALUE;

    private long factsetPostEventConsensus1Y = Long.MIN_VALUE;

    private long factsetPreTaxProfit1Y = Long.MIN_VALUE;

    private long factsetPriceEarningRatio2Y = Long.MIN_VALUE;

    private long factsetCashflow2Y = Long.MIN_VALUE;

    private long factsetEPSAfterGoodwill2Y = Long.MIN_VALUE;

    private long factsetEPSBeforeGoodwill2Y = Long.MIN_VALUE;

    private long factsetBookValue2Y = Long.MIN_VALUE;

    private long factsetEbit2Y = Long.MIN_VALUE;

    private long factsetEbitda2Y = Long.MIN_VALUE;

    private long factsetGoodwill2Y = Long.MIN_VALUE;

    private long factsetNetDebt2Y = Long.MIN_VALUE;

    private long factsetSales2Y = Long.MIN_VALUE;

    private long factsetPostEventConsensus2Y = Long.MIN_VALUE;

    private long factsetPreTaxProfit2Y = Long.MIN_VALUE;

    private long wmDividend = Long.MIN_VALUE;

    private String wmDividendCurrency = null;

    private String benchmarkName = null;

    private long wmDividendLastYear = Long.MIN_VALUE;

    private long trPriceEarningRatio1Y = Long.MIN_VALUE;

    private long trPriceEarningRatio2Y = Long.MIN_VALUE;

    private long trEps1Y = Long.MIN_VALUE;

    private long trEps2Y = Long.MIN_VALUE;

    private long trDividend1Y = Long.MIN_VALUE;

    private long trDividend2Y = Long.MIN_VALUE;

    private long trDividendyield1Y = Long.MIN_VALUE;

    private long trDividendyield2Y = Long.MIN_VALUE;

    private long trSales1Y = Long.MIN_VALUE;

    private long trSales2Y = Long.MIN_VALUE;

    private long trProfit1Y = Long.MIN_VALUE;

    private long trProfit2Y = Long.MIN_VALUE;

    private long trEbit1Y = Long.MIN_VALUE;

    private long trEbit2Y = Long.MIN_VALUE;

    private long trEbitda1Y = Long.MIN_VALUE;

    private long trEbitda2Y = Long.MIN_VALUE;

    private long trRecommendation = Long.MIN_VALUE;

    private long trFiscalYear = Long.MIN_VALUE;

    private long trBookValue1Y = Long.MIN_VALUE;

    private long trBookValue2Y = Long.MIN_VALUE;

    private long trCashflow1Y = Long.MIN_VALUE;

    private long trCashflow2Y = Long.MIN_VALUE;

    private String trCurrency = null;

    private String[] wmSector = null;

    private long wmNumberOfIssuedEquities = Long.MIN_VALUE;

    private boolean wmNotActive = false;

    private String[] gicsSector = null;

    private String[] gicsIndustryGroup = null;

    private String[] gicsIndustry = null;

    private String[] gicsSubIndustry = null;

    private String gicsSectorKey = null;

    private String gicsIndustryGroupKey = null;

    private String gicsIndustryKey = null;

    private String gicsSubIndustryKey = null;

    private String lei = null;

    private long factsetRecommendation = Long.MIN_VALUE;

    private long factsetPriceEarningRatio3Y = Long.MIN_VALUE;

    private long factsetPriceEarningRatio4Y = Long.MIN_VALUE;

    private long factsetDividendyield3Y = Long.MIN_VALUE;

    private long factsetDividendyield4Y = Long.MIN_VALUE;

    private long factsetSales3Y = Long.MIN_VALUE;

    private long factsetSales4Y = Long.MIN_VALUE;

    private long factsetProfit3Y = Long.MIN_VALUE;

    private long factsetProfit4Y = Long.MIN_VALUE;

    private long factsetEbit3Y = Long.MIN_VALUE;

    private long factsetEbit4Y = Long.MIN_VALUE;

    private long factsetEbitda3Y = Long.MIN_VALUE;

    private long factsetEbitda4Y = Long.MIN_VALUE;


    public long getWmIssueVolume() {
        return this.wmIssueVolume;
    }

    public void setWmIssueVolume(long value) {
        this.wmIssueVolume = value;
    }

    public long getScreenerinterest() {
        return this.screenerinterest;
    }

    public void setScreenerinterest(long value) {
        this.screenerinterest = value;
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

    public long getFactsetProfit2Y() {
        return this.factsetProfit2Y;
    }

    public void setFactsetProfit2Y(long value) {
        this.factsetProfit2Y = value;
    }

    public long getFactsetEps2Y() {
        return this.factsetEps2Y;
    }

    public void setFactsetEps2Y(long value) {
        this.factsetEps2Y = value;
    }

    public long getFactsetDividend2Y() {
        return this.factsetDividend2Y;
    }

    public void setFactsetDividend2Y(long value) {
        this.factsetDividend2Y = value;
    }

    public long getFactsetDividendyield2Y() {
        return this.factsetDividendyield2Y;
    }

    public void setFactsetDividendyield2Y(long value) {
        this.factsetDividendyield2Y = value;
    }

    public long getFactsetProfit1Y() {
        return this.factsetProfit1Y;
    }

    public void setFactsetProfit1Y(long value) {
        this.factsetProfit1Y = value;
    }

    public long getFactsetEps1Y() {
        return this.factsetEps1Y;
    }

    public void setFactsetEps1Y(long value) {
        this.factsetEps1Y = value;
    }

    public long getFactsetDividend1Y() {
        return this.factsetDividend1Y;
    }

    public void setFactsetDividend1Y(long value) {
        this.factsetDividend1Y = value;
    }

    public long getFactsetDividendyield1Y() {
        return this.factsetDividendyield1Y;
    }

    public void setFactsetDividendyield1Y(long value) {
        this.factsetDividendyield1Y = value;
    }

    public long getFactsetProfit0Y() {
        return this.factsetProfit0Y;
    }

    public void setFactsetProfit0Y(long value) {
        this.factsetProfit0Y = value;
    }

    public long getFactsetEps0Y() {
        return this.factsetEps0Y;
    }

    public void setFactsetEps0Y(long value) {
        this.factsetEps0Y = value;
    }

    public long getFactsetDividend0Y() {
        return this.factsetDividend0Y;
    }

    public void setFactsetDividend0Y(long value) {
        this.factsetDividend0Y = value;
    }

    public long getFactsetDividendyield0Y() {
        return this.factsetDividendyield0Y;
    }

    public void setFactsetDividendyield0Y(long value) {
        this.factsetDividendyield0Y = value;
    }

    public long getConvensysProfit_1Y() {
        return this.convensysProfit_1Y;
    }

    public void setConvensysProfit_1Y(long value) {
        this.convensysProfit_1Y = value;
    }

    public long getConvensysEps_1Y() {
        return this.convensysEps_1Y;
    }

    public void setConvensysEps_1Y(long value) {
        this.convensysEps_1Y = value;
    }

    public long getConvensysDividend_1Y() {
        return this.convensysDividend_1Y;
    }

    public void setConvensysDividend_1Y(long value) {
        this.convensysDividend_1Y = value;
    }

    public long getConvensysDividendyield_1Y() {
        return this.convensysDividendyield_1Y;
    }

    public void setConvensysDividendyield_1Y(long value) {
        this.convensysDividendyield_1Y = value;
    }

    public long getConvensysProfit_2Y() {
        return this.convensysProfit_2Y;
    }

    public void setConvensysProfit_2Y(long value) {
        this.convensysProfit_2Y = value;
    }

    public long getConvensysEps_2Y() {
        return this.convensysEps_2Y;
    }

    public void setConvensysEps_2Y(long value) {
        this.convensysEps_2Y = value;
    }

    public long getConvensysDividend_2Y() {
        return this.convensysDividend_2Y;
    }

    public void setConvensysDividend_2Y(long value) {
        this.convensysDividend_2Y = value;
    }

    public long getConvensysDividendyield_2Y() {
        return this.convensysDividendyield_2Y;
    }

    public void setConvensysDividendyield_2Y(long value) {
        this.convensysDividendyield_2Y = value;
    }

    public long getFactsetPriceEarningRatio0Y() {
        return this.factsetPriceEarningRatio0Y;
    }

    public void setFactsetPriceEarningRatio0Y(long value) {
        this.factsetPriceEarningRatio0Y = value;
    }

    public long getFactsetCashflow0Y() {
        return this.factsetCashflow0Y;
    }

    public void setFactsetCashflow0Y(long value) {
        this.factsetCashflow0Y = value;
    }

    public long getFactsetLongTermGrowth() {
        return this.factsetLongTermGrowth;
    }

    public void setFactsetLongTermGrowth(long value) {
        this.factsetLongTermGrowth = value;
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

    public long getFactsetPriceTarget() {
        return this.factsetPriceTarget;
    }

    public void setFactsetPriceTarget(long value) {
        this.factsetPriceTarget = value;
    }

    public long getFactsetPriceEarningRatio1Y() {
        return this.factsetPriceEarningRatio1Y;
    }

    public void setFactsetPriceEarningRatio1Y(long value) {
        this.factsetPriceEarningRatio1Y = value;
    }

    public long getFactsetCashflow1Y() {
        return this.factsetCashflow1Y;
    }

    public void setFactsetCashflow1Y(long value) {
        this.factsetCashflow1Y = value;
    }

    public long getFactsetEPSAfterGoodwill1Y() {
        return this.factsetEPSAfterGoodwill1Y;
    }

    public void setFactsetEPSAfterGoodwill1Y(long value) {
        this.factsetEPSAfterGoodwill1Y = value;
    }

    public long getFactsetEPSBeforeGoodwill1Y() {
        return this.factsetEPSBeforeGoodwill1Y;
    }

    public void setFactsetEPSBeforeGoodwill1Y(long value) {
        this.factsetEPSBeforeGoodwill1Y = value;
    }

    public long getFactsetFiscalYear() {
        return this.factsetFiscalYear;
    }

    public void setFactsetFiscalYear(long value) {
        this.factsetFiscalYear = value;
    }

    public long getFactsetBookValue1Y() {
        return this.factsetBookValue1Y;
    }

    public void setFactsetBookValue1Y(long value) {
        this.factsetBookValue1Y = value;
    }

    public long getFactsetEbit1Y() {
        return this.factsetEbit1Y;
    }

    public void setFactsetEbit1Y(long value) {
        this.factsetEbit1Y = value;
    }

    public long getFactsetEbitda1Y() {
        return this.factsetEbitda1Y;
    }

    public void setFactsetEbitda1Y(long value) {
        this.factsetEbitda1Y = value;
    }

    public long getFactsetGoodwill1Y() {
        return this.factsetGoodwill1Y;
    }

    public void setFactsetGoodwill1Y(long value) {
        this.factsetGoodwill1Y = value;
    }

    public long getFactsetNetDebt1Y() {
        return this.factsetNetDebt1Y;
    }

    public void setFactsetNetDebt1Y(long value) {
        this.factsetNetDebt1Y = value;
    }

    public String getFactsetCurrency() {
        return this.factsetCurrency;
    }

    public void setFactsetCurrency(String value) {
        this.factsetCurrency = value;
    }

    public long getFactsetSales1Y() {
        return this.factsetSales1Y;
    }

    public void setFactsetSales1Y(long value) {
        this.factsetSales1Y = value;
    }

    public long getFactsetPostEventConsensus1Y() {
        return this.factsetPostEventConsensus1Y;
    }

    public void setFactsetPostEventConsensus1Y(long value) {
        this.factsetPostEventConsensus1Y = value;
    }

    public long getFactsetPreTaxProfit1Y() {
        return this.factsetPreTaxProfit1Y;
    }

    public void setFactsetPreTaxProfit1Y(long value) {
        this.factsetPreTaxProfit1Y = value;
    }

    public long getFactsetPriceEarningRatio2Y() {
        return this.factsetPriceEarningRatio2Y;
    }

    public void setFactsetPriceEarningRatio2Y(long value) {
        this.factsetPriceEarningRatio2Y = value;
    }

    public long getFactsetCashflow2Y() {
        return this.factsetCashflow2Y;
    }

    public void setFactsetCashflow2Y(long value) {
        this.factsetCashflow2Y = value;
    }

    public long getFactsetEPSAfterGoodwill2Y() {
        return this.factsetEPSAfterGoodwill2Y;
    }

    public void setFactsetEPSAfterGoodwill2Y(long value) {
        this.factsetEPSAfterGoodwill2Y = value;
    }

    public long getFactsetEPSBeforeGoodwill2Y() {
        return this.factsetEPSBeforeGoodwill2Y;
    }

    public void setFactsetEPSBeforeGoodwill2Y(long value) {
        this.factsetEPSBeforeGoodwill2Y = value;
    }

    public long getFactsetBookValue2Y() {
        return this.factsetBookValue2Y;
    }

    public void setFactsetBookValue2Y(long value) {
        this.factsetBookValue2Y = value;
    }

    public long getFactsetEbit2Y() {
        return this.factsetEbit2Y;
    }

    public void setFactsetEbit2Y(long value) {
        this.factsetEbit2Y = value;
    }

    public long getFactsetEbitda2Y() {
        return this.factsetEbitda2Y;
    }

    public void setFactsetEbitda2Y(long value) {
        this.factsetEbitda2Y = value;
    }

    public long getFactsetGoodwill2Y() {
        return this.factsetGoodwill2Y;
    }

    public void setFactsetGoodwill2Y(long value) {
        this.factsetGoodwill2Y = value;
    }

    public long getFactsetNetDebt2Y() {
        return this.factsetNetDebt2Y;
    }

    public void setFactsetNetDebt2Y(long value) {
        this.factsetNetDebt2Y = value;
    }

    public long getFactsetSales2Y() {
        return this.factsetSales2Y;
    }

    public void setFactsetSales2Y(long value) {
        this.factsetSales2Y = value;
    }

    public long getFactsetPostEventConsensus2Y() {
        return this.factsetPostEventConsensus2Y;
    }

    public void setFactsetPostEventConsensus2Y(long value) {
        this.factsetPostEventConsensus2Y = value;
    }

    public long getFactsetPreTaxProfit2Y() {
        return this.factsetPreTaxProfit2Y;
    }

    public void setFactsetPreTaxProfit2Y(long value) {
        this.factsetPreTaxProfit2Y = value;
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

    public long getWmDividendLastYear() {
        return this.wmDividendLastYear;
    }

    public void setWmDividendLastYear(long value) {
        this.wmDividendLastYear = value;
    }

    public long getTrPriceEarningRatio1Y() {
        return this.trPriceEarningRatio1Y;
    }

    public void setTrPriceEarningRatio1Y(long value) {
        this.trPriceEarningRatio1Y = value;
    }

    public long getTrPriceEarningRatio2Y() {
        return this.trPriceEarningRatio2Y;
    }

    public void setTrPriceEarningRatio2Y(long value) {
        this.trPriceEarningRatio2Y = value;
    }

    public long getTrEps1Y() {
        return this.trEps1Y;
    }

    public void setTrEps1Y(long value) {
        this.trEps1Y = value;
    }

    public long getTrEps2Y() {
        return this.trEps2Y;
    }

    public void setTrEps2Y(long value) {
        this.trEps2Y = value;
    }

    public long getTrDividend1Y() {
        return this.trDividend1Y;
    }

    public void setTrDividend1Y(long value) {
        this.trDividend1Y = value;
    }

    public long getTrDividend2Y() {
        return this.trDividend2Y;
    }

    public void setTrDividend2Y(long value) {
        this.trDividend2Y = value;
    }

    public long getTrDividendyield1Y() {
        return this.trDividendyield1Y;
    }

    public void setTrDividendyield1Y(long value) {
        this.trDividendyield1Y = value;
    }

    public long getTrDividendyield2Y() {
        return this.trDividendyield2Y;
    }

    public void setTrDividendyield2Y(long value) {
        this.trDividendyield2Y = value;
    }

    public long getTrSales1Y() {
        return this.trSales1Y;
    }

    public void setTrSales1Y(long value) {
        this.trSales1Y = value;
    }

    public long getTrSales2Y() {
        return this.trSales2Y;
    }

    public void setTrSales2Y(long value) {
        this.trSales2Y = value;
    }

    public long getTrProfit1Y() {
        return this.trProfit1Y;
    }

    public void setTrProfit1Y(long value) {
        this.trProfit1Y = value;
    }

    public long getTrProfit2Y() {
        return this.trProfit2Y;
    }

    public void setTrProfit2Y(long value) {
        this.trProfit2Y = value;
    }

    public long getTrEbit1Y() {
        return this.trEbit1Y;
    }

    public void setTrEbit1Y(long value) {
        this.trEbit1Y = value;
    }

    public long getTrEbit2Y() {
        return this.trEbit2Y;
    }

    public void setTrEbit2Y(long value) {
        this.trEbit2Y = value;
    }

    public long getTrEbitda1Y() {
        return this.trEbitda1Y;
    }

    public void setTrEbitda1Y(long value) {
        this.trEbitda1Y = value;
    }

    public long getTrEbitda2Y() {
        return this.trEbitda2Y;
    }

    public void setTrEbitda2Y(long value) {
        this.trEbitda2Y = value;
    }

    public long getTrRecommendation() {
        return this.trRecommendation;
    }

    public void setTrRecommendation(long value) {
        this.trRecommendation = value;
    }

    public long getTrFiscalYear() {
        return this.trFiscalYear;
    }

    public void setTrFiscalYear(long value) {
        this.trFiscalYear = value;
    }

    public long getTrBookValue1Y() {
        return this.trBookValue1Y;
    }

    public void setTrBookValue1Y(long value) {
        this.trBookValue1Y = value;
    }

    public long getTrBookValue2Y() {
        return this.trBookValue2Y;
    }

    public void setTrBookValue2Y(long value) {
        this.trBookValue2Y = value;
    }

    public long getTrCashflow1Y() {
        return this.trCashflow1Y;
    }

    public void setTrCashflow1Y(long value) {
        this.trCashflow1Y = value;
    }

    public long getTrCashflow2Y() {
        return this.trCashflow2Y;
    }

    public void setTrCashflow2Y(long value) {
        this.trCashflow2Y = value;
    }

    public String getTrCurrency() {
        return this.trCurrency;
    }

    public void setTrCurrency(String value) {
        this.trCurrency = value;
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

    public long getWmNumberOfIssuedEquities() {
        return this.wmNumberOfIssuedEquities;
    }

    public void setWmNumberOfIssuedEquities(long value) {
        this.wmNumberOfIssuedEquities = value;
    }

    public boolean getWmNotActive() {
        return this.wmNotActive;
    }

    public void setWmNotActive(boolean value) {
        this.wmNotActive = value;
    }

    public String getGicsSector(int localeIndex) {
        if (this.gicsSector == null) {
            return null;
        }
        return this.gicsSector[localeIndex];
    }

    public void setGicsSector(int localeIndex, String value) {
        if (this.gicsSector == null) {
            this.gicsSector = new String[RatioFieldDescription.gicsSector.getLocales().length];
        }
        this.gicsSector[localeIndex] = value;
    }

    public String getGicsIndustryGroup(int localeIndex) {
        if (this.gicsIndustryGroup == null) {
            return null;
        }
        return this.gicsIndustryGroup[localeIndex];
    }

    public void setGicsIndustryGroup(int localeIndex, String value) {
        if (this.gicsIndustryGroup == null) {
            this.gicsIndustryGroup = new String[RatioFieldDescription.gicsIndustryGroup.getLocales().length];
        }
        this.gicsIndustryGroup[localeIndex] = value;
    }

    public String getGicsIndustry(int localeIndex) {
        if (this.gicsIndustry == null) {
            return null;
        }
        return this.gicsIndustry[localeIndex];
    }

    public void setGicsIndustry(int localeIndex, String value) {
        if (this.gicsIndustry == null) {
            this.gicsIndustry = new String[RatioFieldDescription.gicsIndustry.getLocales().length];
        }
        this.gicsIndustry[localeIndex] = value;
    }

    public String getGicsSubIndustry(int localeIndex) {
        if (this.gicsSubIndustry == null) {
            return null;
        }
        return this.gicsSubIndustry[localeIndex];
    }

    public void setGicsSubIndustry(int localeIndex, String value) {
        if (this.gicsSubIndustry == null) {
            this.gicsSubIndustry = new String[RatioFieldDescription.gicsSubIndustry.getLocales().length];
        }
        this.gicsSubIndustry[localeIndex] = value;
    }

    public String getGicsSectorKey() {
        return this.gicsSectorKey;
    }

    public void setGicsSectorKey(String value) {
        this.gicsSectorKey = value;
    }

    public String getGicsIndustryGroupKey() {
        return this.gicsIndustryGroupKey;
    }

    public void setGicsIndustryGroupKey(String value) {
        this.gicsIndustryGroupKey = value;
    }

    public String getGicsIndustryKey() {
        return this.gicsIndustryKey;
    }

    public void setGicsIndustryKey(String value) {
        this.gicsIndustryKey = value;
    }

    public String getGicsSubIndustryKey() {
        return this.gicsSubIndustryKey;
    }

    public void setGicsSubIndustryKey(String value) {
        this.gicsSubIndustryKey = value;
    }

    public String getLei() {
        return this.lei;
    }

    public void setLei(String value) {
        this.lei = value;
    }

    public long getFactsetRecommendation() {
        return this.factsetRecommendation;
    }

    public void setFactsetRecommendation(long value) {
        this.factsetRecommendation = value;
    }

    public long getFactsetPriceEarningRatio3Y() {
        return this.factsetPriceEarningRatio3Y;
    }

    public void setFactsetPriceEarningRatio3Y(long value) {
        this.factsetPriceEarningRatio3Y = value;
    }

    public long getFactsetPriceEarningRatio4Y() {
        return this.factsetPriceEarningRatio4Y;
    }

    public void setFactsetPriceEarningRatio4Y(long value) {
        this.factsetPriceEarningRatio4Y = value;
    }

    public long getFactsetDividendyield3Y() {
        return this.factsetDividendyield3Y;
    }

    public void setFactsetDividendyield3Y(long value) {
        this.factsetDividendyield3Y = value;
    }

    public long getFactsetDividendyield4Y() {
        return this.factsetDividendyield4Y;
    }

    public void setFactsetDividendyield4Y(long value) {
        this.factsetDividendyield4Y = value;
    }

    public long getFactsetSales3Y() {
        return this.factsetSales3Y;
    }

    public void setFactsetSales3Y(long value) {
        this.factsetSales3Y = value;
    }

    public long getFactsetSales4Y() {
        return this.factsetSales4Y;
    }

    public void setFactsetSales4Y(long value) {
        this.factsetSales4Y = value;
    }

    public long getFactsetProfit3Y() {
        return this.factsetProfit3Y;
    }

    public void setFactsetProfit3Y(long value) {
        this.factsetProfit3Y = value;
    }

    public long getFactsetProfit4Y() {
        return this.factsetProfit4Y;
    }

    public void setFactsetProfit4Y(long value) {
        this.factsetProfit4Y = value;
    }

    public long getFactsetEbit3Y() {
        return this.factsetEbit3Y;
    }

    public void setFactsetEbit3Y(long value) {
        this.factsetEbit3Y = value;
    }

    public long getFactsetEbit4Y() {
        return this.factsetEbit4Y;
    }

    public void setFactsetEbit4Y(long value) {
        this.factsetEbit4Y = value;
    }

    public long getFactsetEbitda3Y() {
        return this.factsetEbitda3Y;
    }

    public void setFactsetEbitda3Y(long value) {
        this.factsetEbitda3Y = value;
    }

    public long getFactsetEbitda4Y() {
        return this.factsetEbitda4Y;
    }

    public void setFactsetEbitda4Y(long value) {
        this.factsetEbitda4Y = value;
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