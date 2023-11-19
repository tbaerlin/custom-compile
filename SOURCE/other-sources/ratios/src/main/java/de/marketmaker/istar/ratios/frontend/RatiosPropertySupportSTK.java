package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class RatiosPropertySupportSTK {
    private static PropertySupport[] qps = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    private static PropertySupport[] ips = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    public static PropertySupport forQuote(int fieldid) {
        return qps[fieldid];
    }

    public static PropertySupport forInstrument(int fieldid) {
        return ips[fieldid];
    }

    static {

        ips[81] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setWmIssueVolume(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getWmIssueVolume();
            }
        };
        ips[106] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setScreenerinterest(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getScreenerinterest();
            }
        };
        ips[139] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setWkn(value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getWkn();
            }
        };
        ips[140] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setIsin(value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getIsin();
            }
        };
        ips[141] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setName(value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getName();
            }
        };
        ips[162] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setCountry(localeIndex, value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getCountry(localeIndex);
            }
        };
        ips[203] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setSector(localeIndex, value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getSector(localeIndex);
            }
        };
        ips[269] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetProfit2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetProfit2Y();
            }
        };
        ips[270] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEps2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEps2Y();
            }
        };
        ips[271] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetDividend2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetDividend2Y();
            }
        };
        ips[272] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetDividendyield2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetDividendyield2Y();
            }
        };
        ips[273] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetProfit1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetProfit1Y();
            }
        };
        ips[274] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEps1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEps1Y();
            }
        };
        ips[275] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetDividend1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetDividend1Y();
            }
        };
        ips[276] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetDividendyield1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetDividendyield1Y();
            }
        };
        ips[277] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetProfit0Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetProfit0Y();
            }
        };
        ips[278] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEps0Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEps0Y();
            }
        };
        ips[279] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetDividend0Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetDividend0Y();
            }
        };
        ips[280] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetDividendyield0Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetDividendyield0Y();
            }
        };
        ips[281] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setConvensysProfit_1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getConvensysProfit_1Y();
            }
        };
        ips[282] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setConvensysEps_1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getConvensysEps_1Y();
            }
        };
        ips[283] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setConvensysDividend_1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getConvensysDividend_1Y();
            }
        };
        ips[284] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setConvensysDividendyield_1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getConvensysDividendyield_1Y();
            }
        };
        ips[285] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setConvensysProfit_2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getConvensysProfit_2Y();
            }
        };
        ips[286] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setConvensysEps_2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getConvensysEps_2Y();
            }
        };
        ips[287] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setConvensysDividend_2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getConvensysDividend_2Y();
            }
        };
        ips[288] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setConvensysDividendyield_2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getConvensysDividendyield_2Y();
            }
        };
        ips[344] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetPriceEarningRatio0Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetPriceEarningRatio0Y();
            }
        };
        ips[345] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetCashflow0Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetCashflow0Y();
            }
        };
        ips[348] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetLongTermGrowth(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetLongTermGrowth();
            }
        };
        ips[353] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setWmCountry(localeIndex, value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getWmCountry(localeIndex);
            }
        };
        ips[356] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetPriceTarget(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetPriceTarget();
            }
        };
        ips[360] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetPriceEarningRatio1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetPriceEarningRatio1Y();
            }
        };
        ips[361] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetCashflow1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetCashflow1Y();
            }
        };
        ips[362] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEPSAfterGoodwill1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEPSAfterGoodwill1Y();
            }
        };
        ips[363] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEPSBeforeGoodwill1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEPSBeforeGoodwill1Y();
            }
        };
        ips[364] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetFiscalYear(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetFiscalYear();
            }
        };
        ips[365] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetBookValue1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetBookValue1Y();
            }
        };
        ips[367] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEbit1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEbit1Y();
            }
        };
        ips[368] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEbitda1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEbitda1Y();
            }
        };
        ips[369] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetGoodwill1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetGoodwill1Y();
            }
        };
        ips[370] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetNetDebt1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetNetDebt1Y();
            }
        };
        ips[372] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setFactsetCurrency(value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getFactsetCurrency();
            }
        };
        ips[373] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetSales1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetSales1Y();
            }
        };
        ips[374] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetPostEventConsensus1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetPostEventConsensus1Y();
            }
        };
        ips[375] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetPreTaxProfit1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetPreTaxProfit1Y();
            }
        };
        ips[376] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetPriceEarningRatio2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetPriceEarningRatio2Y();
            }
        };
        ips[377] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetCashflow2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetCashflow2Y();
            }
        };
        ips[378] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEPSAfterGoodwill2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEPSAfterGoodwill2Y();
            }
        };
        ips[379] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEPSBeforeGoodwill2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEPSBeforeGoodwill2Y();
            }
        };
        ips[381] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetBookValue2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetBookValue2Y();
            }
        };
        ips[383] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEbit2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEbit2Y();
            }
        };
        ips[384] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEbitda2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEbitda2Y();
            }
        };
        ips[385] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetGoodwill2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetGoodwill2Y();
            }
        };
        ips[386] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetNetDebt2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetNetDebt2Y();
            }
        };
        ips[389] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetSales2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetSales2Y();
            }
        };
        ips[390] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetPostEventConsensus2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetPostEventConsensus2Y();
            }
        };
        ips[391] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetPreTaxProfit2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetPreTaxProfit2Y();
            }
        };
        ips[489] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setWmDividend(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getWmDividend();
            }
        };
        ips[490] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setWmDividendCurrency(value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getWmDividendCurrency();
            }
        };
        ips[492] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setBenchmarkName(value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getBenchmarkName();
            }
        };
        ips[556] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setWmDividendLastYear(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getWmDividendLastYear();
            }
        };
        ips[589] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrPriceEarningRatio1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrPriceEarningRatio1Y();
            }
        };
        ips[590] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrPriceEarningRatio2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrPriceEarningRatio2Y();
            }
        };
        ips[595] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrEps1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrEps1Y();
            }
        };
        ips[596] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrEps2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrEps2Y();
            }
        };
        ips[597] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrDividend1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrDividend1Y();
            }
        };
        ips[598] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrDividend2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrDividend2Y();
            }
        };
        ips[599] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrDividendyield1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrDividendyield1Y();
            }
        };
        ips[600] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrDividendyield2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrDividendyield2Y();
            }
        };
        ips[601] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrSales1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrSales1Y();
            }
        };
        ips[602] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrSales2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrSales2Y();
            }
        };
        ips[603] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrProfit1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrProfit1Y();
            }
        };
        ips[604] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrProfit2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrProfit2Y();
            }
        };
        ips[605] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrEbit1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrEbit1Y();
            }
        };
        ips[606] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrEbit2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrEbit2Y();
            }
        };
        ips[607] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrEbitda1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrEbitda1Y();
            }
        };
        ips[608] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrEbitda2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrEbitda2Y();
            }
        };
        ips[609] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrRecommendation(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrRecommendation();
            }
        };
        ips[610] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrFiscalYear(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrFiscalYear();
            }
        };
        ips[611] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrBookValue1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrBookValue1Y();
            }
        };
        ips[612] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrBookValue2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrBookValue2Y();
            }
        };
        ips[613] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrCashflow1Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrCashflow1Y();
            }
        };
        ips[614] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setTrCashflow2Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getTrCashflow2Y();
            }
        };
        ips[615] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setTrCurrency(value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getTrCurrency();
            }
        };
        ips[645] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setWmSector(localeIndex, value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getWmSector(localeIndex);
            }
        };
        ips[651] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setWmNumberOfIssuedEquities(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getWmNumberOfIssuedEquities();
            }
        };
        ips[769] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, boolean value) {
                ratios.setWmNotActive(value);
            }

            public boolean getBoolean(InstrumentRatiosSTK ratios) {
                return ratios.getWmNotActive();
            }
        };
        ips[796] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setGicsSector(localeIndex, value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getGicsSector(localeIndex);
            }
        };
        ips[797] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setGicsIndustryGroup(localeIndex, value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getGicsIndustryGroup(localeIndex);
            }
        };
        ips[798] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setGicsIndustry(localeIndex, value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getGicsIndustry(localeIndex);
            }
        };
        ips[799] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setGicsSubIndustry(localeIndex, value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getGicsSubIndustry(localeIndex);
            }
        };
        ips[807] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setGicsSectorKey(value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getGicsSectorKey();
            }
        };
        ips[808] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setGicsIndustryGroupKey(value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getGicsIndustryGroupKey();
            }
        };
        ips[809] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setGicsIndustryKey(value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getGicsIndustryKey();
            }
        };
        ips[810] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setGicsSubIndustryKey(value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getGicsSubIndustryKey();
            }
        };
        ips[813] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, int localeIndex, String value) {
                ratios.setLei(value);
            }

            public String getString(InstrumentRatiosSTK ratios, int localeIndex) {
                return ratios.getLei();
            }
        };
        ips[824] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetRecommendation(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetRecommendation();
            }
        };
        ips[827] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetPriceEarningRatio3Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetPriceEarningRatio3Y();
            }
        };
        ips[828] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetPriceEarningRatio4Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetPriceEarningRatio4Y();
            }
        };
        ips[831] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetDividendyield3Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetDividendyield3Y();
            }
        };
        ips[832] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetDividendyield4Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetDividendyield4Y();
            }
        };
        ips[833] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetSales3Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetSales3Y();
            }
        };
        ips[834] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetSales4Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetSales4Y();
            }
        };
        ips[835] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetProfit3Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetProfit3Y();
            }
        };
        ips[836] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetProfit4Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetProfit4Y();
            }
        };
        ips[837] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEbit3Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEbit3Y();
            }
        };
        ips[838] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEbit4Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEbit4Y();
            }
        };
        ips[839] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEbitda3Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEbitda3Y();
            }
        };
        ips[840] = new PropertySupport<InstrumentRatiosSTK>() {
            public void set(InstrumentRatiosSTK ratios, long value) {
                ratios.setFactsetEbitda4Y(value);
            }

            public long getLong(InstrumentRatiosSTK ratios) {
                return ratios.getFactsetEbitda4Y();
            }
        };

        qps[1] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMostRecentUpdateTimestamp(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMostRecentUpdateTimestamp();
            }
        };
        qps[4] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setVolatility1m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getVolatility1m();
            }
        };
        qps[5] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformance1m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformance1m();
            }
        };
        qps[6] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setSpread(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getSpread();
            }
        };
        qps[10] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setReferenceTimestamp(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getReferenceTimestamp();
            }
        };
        qps[15] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setSpreadRelative(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getSpreadRelative();
            }
        };
        qps[16] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setBid(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getBid();
            }
        };
        qps[17] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAsk(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAsk();
            }
        };
        qps[18] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setLastPrice(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getLastPrice();
            }
        };
        qps[19] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPreviousClose(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPreviousClose();
            }
        };
        qps[28] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setBidVolume(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getBidVolume();
            }
        };
        qps[29] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAskVolume(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAskVolume();
            }
        };
        qps[34] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setBidAskDate(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getBidAskDate();
            }
        };
        qps[35] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setBidAskTime(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getBidAskTime();
            }
        };
        qps[36] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setTradeVolume(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getTradeVolume();
            }
        };
        qps[37] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setLastDate(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getLastDate();
            }
        };
        qps[38] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setLastTime(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getLastTime();
            }
        };
        qps[39] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setHigh(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getHigh();
            }
        };
        qps[40] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setLow(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getLow();
            }
        };
        qps[41] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setHighYear(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getHighYear();
            }
        };
        qps[42] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setLowYear(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getLowYear();
            }
        };
        qps[43] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setTotalVolume(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getTotalVolume();
            }
        };
        qps[44] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setPreviousDate(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getPreviousDate();
            }
        };
        qps[45] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setOpen(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getOpen();
            }
        };
        qps[60] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setIssuePrice(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getIssuePrice();
            }
        };
        qps[67] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setVolatility3m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getVolatility3m();
            }
        };
        qps[68] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformance3m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformance3m();
            }
        };
        qps[69] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformance1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformance1y();
            }
        };
        qps[72] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setVolatility10y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getVolatility10y();
            }
        };
        qps[73] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setVolatility1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getVolatility1y();
            }
        };
        qps[80] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setBeta1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getBeta1y();
            }
        };
        qps[82] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setCorrelation1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getCorrelation1y();
            }
        };
        qps[83] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMarketcapitalization(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMarketcapitalization();
            }
        };
        qps[105] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformance10y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformance10y();
            }
        };
        qps[107] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setChangepercentalltimehigh(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getChangepercentalltimehigh();
            }
        };
        qps[108] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setVolatility1w(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getVolatility1w();
            }
        };
        qps[117] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformance1d(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformance1d();
            }
        };
        qps[118] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformancetobenchmark3m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformancetobenchmark3m();
            }
        };
        qps[119] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformancetobenchmark1w(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformancetobenchmark1w();
            }
        };
        qps[120] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformancetobenchmark1m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformancetobenchmark1m();
            }
        };
        qps[121] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformancetobenchmark1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformancetobenchmark1y();
            }
        };
        qps[122] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragevolume1m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragevolume1m();
            }
        };
        qps[123] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragevolume1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragevolume1y();
            }
        };
        qps[124] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setBeta1m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getBeta1m();
            }
        };
        qps[126] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformancetobenchmark6m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformancetobenchmark6m();
            }
        };
        qps[127] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformance6m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformance6m();
            }
        };
        qps[128] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformance3y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformance3y();
            }
        };
        qps[129] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformance5y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformance5y();
            }
        };
        qps[130] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setVolatility6m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getVolatility6m();
            }
        };
        qps[131] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setVolatility5y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getVolatility5y();
            }
        };
        qps[136] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setCorrelation1m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getCorrelation1m();
            }
        };
        qps[155] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int localeIndex, String value) {
                ratios.setVwdCode(value);
            }

            public String getString(QuoteRatiosSTK ratios, int localeIndex) {
                return ratios.getVwdCode();
            }
        };
        qps[156] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int localeIndex, String value) {
                ratios.setMmwkn(value);
            }

            public String getString(QuoteRatiosSTK ratios, int localeIndex) {
                return ratios.getMmwkn();
            }
        };
        qps[157] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int localeIndex, String value) {
                ratios.setCurrency(value);
            }

            public String getString(QuoteRatiosSTK ratios, int localeIndex) {
                return ratios.getCurrency();
            }
        };
        qps[167] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setHighAlltime(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getHighAlltime();
            }
        };
        qps[168] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setLowAlltime(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getLowAlltime();
            }
        };
        qps[169] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setHighAlltimeDate(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getHighAlltimeDate();
            }
        };
        qps[171] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setLowAlltimeDate(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getLowAlltimeDate();
            }
        };
        qps[175] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setChangepercenthigh52weeks(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getChangepercenthigh52weeks();
            }
        };
        qps[176] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setChangepercentlow52weeks(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getChangepercentlow52weeks();
            }
        };
        qps[177] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setHigh1yDate(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getHigh1yDate();
            }
        };
        qps[178] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setLow1yDate(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getLow1yDate();
            }
        };
        qps[179] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragePrice1w(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragePrice1w();
            }
        };
        qps[180] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragePrice1m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragePrice1m();
            }
        };
        qps[181] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragePrice3m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragePrice3m();
            }
        };
        qps[182] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragePrice6m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragePrice6m();
            }
        };
        qps[183] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragePrice1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragePrice1y();
            }
        };
        qps[184] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragePrice3y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragePrice3y();
            }
        };
        qps[186] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragePrice5y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragePrice5y();
            }
        };
        qps[187] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragePrice10y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragePrice10y();
            }
        };
        qps[188] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setBeta1w(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getBeta1w();
            }
        };
        qps[189] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setBeta3m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getBeta3m();
            }
        };
        qps[190] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setBeta6m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getBeta6m();
            }
        };
        qps[191] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformancetobenchmark3y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformancetobenchmark3y();
            }
        };
        qps[192] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformancetobenchmark5y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformancetobenchmark5y();
            }
        };
        qps[204] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformancetobenchmark10y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformancetobenchmark10y();
            }
        };
        qps[232] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setFactsetCurrentPriceSalesRatio1Y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getFactsetCurrentPriceSalesRatio1Y();
            }
        };
        qps[233] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setFactsetcurrentpricesalesratio2y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getFactsetcurrentpricesalesratio2y();
            }
        };
        qps[234] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformancetobenchmark1d(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformancetobenchmark1d();
            }
        };
        qps[240] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setChangeNet(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getChangeNet();
            }
        };
        qps[241] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setChangePercent(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getChangePercent();
            }
        };
        qps[299] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setVolatility3y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getVolatility3y();
            }
        };
        qps[300] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformance1w(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformance1w();
            }
        };
        qps[307] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setCorrelation1w(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getCorrelation1w();
            }
        };
        qps[308] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setCorrelation3m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getCorrelation3m();
            }
        };
        qps[309] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setCorrelation6m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getCorrelation6m();
            }
        };
        qps[310] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setCorrelation3y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getCorrelation3y();
            }
        };
        qps[311] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setCorrelation5y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getCorrelation5y();
            }
        };
        qps[312] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setCorrelation10y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getCorrelation10y();
            }
        };
        qps[315] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMaximumLoss1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMaximumLoss1y();
            }
        };
        qps[319] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setHigh1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getHigh1y();
            }
        };
        qps[320] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setLow1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getLow1y();
            }
        };
        qps[321] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMaximumLoss6m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMaximumLoss6m();
            }
        };
        qps[323] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragevolume1w(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragevolume1w();
            }
        };
        qps[324] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragevolume3m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragevolume3m();
            }
        };
        qps[325] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragevolume6m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragevolume6m();
            }
        };
        qps[326] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragevolume3y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragevolume3y();
            }
        };
        qps[327] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragevolume5y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragevolume5y();
            }
        };
        qps[329] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAveragevolume10y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAveragevolume10y();
            }
        };
        qps[330] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setFactsetcurrentpriceearningratio1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getFactsetcurrentpriceearningratio1y();
            }
        };
        qps[331] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setFactsetcurrentpriceearningratio2y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getFactsetcurrentpriceearningratio2y();
            }
        };
        qps[332] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int localeIndex, String value) {
                ratios.setMarketmanagerName(value);
            }

            public String getString(QuoteRatiosSTK ratios, int localeIndex) {
                return ratios.getMarketmanagerName();
            }
        };
        qps[334] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMaximumLoss3y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMaximumLoss3y();
            }
        };
        qps[342] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setVolatilityCurrentYear(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getVolatilityCurrentYear();
            }
        };
        qps[346] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAlpha1m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAlpha1m();
            }
        };
        qps[347] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAlpha1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAlpha1y();
            }
        };
        qps[349] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setFactsetcurrentpricecashflowratio1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getFactsetcurrentpricecashflowratio1y();
            }
        };
        qps[350] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setFactsetcurrentpricecashflowratio2y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getFactsetcurrentpricecashflowratio2y();
            }
        };
        qps[351] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setFactsetcurrentpricebookvalueratio1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getFactsetcurrentpricebookvalueratio1y();
            }
        };
        qps[352] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setFactsetcurrentpricebookvalueratio2y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getFactsetcurrentpricebookvalueratio2y();
            }
        };
        qps[371] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setBeta3y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getBeta3y();
            }
        };
        qps[382] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setBeta5y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getBeta5y();
            }
        };
        qps[387] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setBeta10y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getBeta10y();
            }
        };
        qps[392] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformanceAlltime(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformanceAlltime();
            }
        };
        qps[405] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setReferencePrice(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getReferencePrice();
            }
        };
        qps[414] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int localeIndex, String value) {
                ratios.setVwdMarket(value);
            }

            public String getString(QuoteRatiosSTK ratios, int localeIndex) {
                return ratios.getVwdMarket();
            }
        };
        qps[417] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setClosePreviousYear(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getClosePreviousYear();
            }
        };
        qps[418] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setClosePreviousYearDate(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getClosePreviousYearDate();
            }
        };
        qps[419] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setClosePreviousMonth(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getClosePreviousMonth();
            }
        };
        qps[420] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setClosePreviousMonthDate(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getClosePreviousMonthDate();
            }
        };
        qps[421] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformanceCurrentYear(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformanceCurrentYear();
            }
        };
        qps[422] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformanceCurrentMonth(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformanceCurrentMonth();
            }
        };
        qps[423] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setHighPreviousYear(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getHighPreviousYear();
            }
        };
        qps[424] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setHighPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getHighPreviousYearDate();
            }
        };
        qps[425] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setLowPreviousYear(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getLowPreviousYear();
            }
        };
        qps[426] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setLowPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getLowPreviousYearDate();
            }
        };
        qps[428] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setChangeNetCurrentYear(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getChangeNetCurrentYear();
            }
        };
        qps[429] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setChangeNetCurrentMonth(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getChangeNetCurrentMonth();
            }
        };
        qps[441] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAlpha1w(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAlpha1w();
            }
        };
        qps[442] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAlpha3m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAlpha3m();
            }
        };
        qps[443] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAlpha6m(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAlpha6m();
            }
        };
        qps[444] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAlpha3y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAlpha3y();
            }
        };
        qps[445] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAlpha5y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAlpha5y();
            }
        };
        qps[446] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAlpha10y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAlpha10y();
            }
        };
        qps[491] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setWmDividendYield(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getWmDividendYield();
            }
        };
        qps[531] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setQid(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getQid();
            }
        };
        qps[532] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, int value) {
                ratios.setDateFirstPrice(value);
            }

            public int getInt(QuoteRatiosSTK ratios) {
                return ratios.getDateFirstPrice();
            }
        };
        qps[557] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setRsi7d(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getRsi7d();
            }
        };
        qps[558] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setRsi9d(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getRsi9d();
            }
        };
        qps[559] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setRsi14d(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getRsi14d();
            }
        };
        qps[560] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setRsi25d(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getRsi25d();
            }
        };
        qps[585] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setRsi90d(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getRsi90d();
            }
        };
        qps[586] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setRsi450d(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getRsi450d();
            }
        };
        qps[587] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setTrCurrentPriceSalesRatio1Y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getTrCurrentPriceSalesRatio1Y();
            }
        };
        qps[588] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setTrcurrentpricesalesratio2y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getTrcurrentpricesalesratio2y();
            }
        };
        qps[591] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setTrcurrentpricecashflowratio1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getTrcurrentpricecashflowratio1y();
            }
        };
        qps[592] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setTrcurrentpricecashflowratio2y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getTrcurrentpricecashflowratio2y();
            }
        };
        qps[593] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setTrcurrentpricebookvalueratio1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getTrcurrentpricebookvalueratio1y();
            }
        };
        qps[594] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setTrcurrentpricebookvalueratio2y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getTrcurrentpricebookvalueratio2y();
            }
        };
        qps[616] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setTrcurrentpriceearningratio1y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getTrcurrentpriceearningratio1y();
            }
        };
        qps[617] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setTrcurrentpriceearningratio2y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getTrcurrentpriceearningratio2y();
            }
        };
        qps[648] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMaSimple38(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMaSimple38();
            }
        };
        qps[649] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMaSimple90(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMaSimple90();
            }
        };
        qps[650] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMaSimple200(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMaSimple200();
            }
        };
        qps[654] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMarketCapitalizationPreviousDay(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMarketCapitalizationPreviousDay();
            }
        };
        qps[692] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setTurnoverDay(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getTurnoverDay();
            }
        };
        qps[721] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setPerformancetobenchmarkcurrentyear(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getPerformancetobenchmarkcurrentyear();
            }
        };
        qps[722] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setCorrelationcurrentyear(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getCorrelationcurrentyear();
            }
        };
        qps[747] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setRsi130d(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getRsi130d();
            }
        };
        qps[748] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMarketcapitalizationUSD(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMarketcapitalizationUSD();
            }
        };
        qps[749] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMarketcapitalizationEUR(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMarketcapitalizationEUR();
            }
        };
        qps[760] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setVolatilityAlltime(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getVolatilityAlltime();
            }
        };
        qps[761] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setBetaCurrentYear(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getBetaCurrentYear();
            }
        };
        qps[762] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setBetaAlltime(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getBetaAlltime();
            }
        };
        qps[765] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAlphaCurrentYear(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAlphaCurrentYear();
            }
        };
        qps[766] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setAlphaAlltime(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getAlphaAlltime();
            }
        };
        qps[770] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMaximumLoss5y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMaximumLoss5y();
            }
        };
        qps[771] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMaximumLoss10y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMaximumLoss10y();
            }
        };
        qps[772] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setMaximumLossAlltime(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getMaximumLossAlltime();
            }
        };
        qps[825] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setFactsetcurrentpricesalesratio3y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getFactsetcurrentpricesalesratio3y();
            }
        };
        qps[826] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setFactsetcurrentpricesalesratio4y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getFactsetcurrentpricesalesratio4y();
            }
        };
        qps[829] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setFactsetcurrentpricecashflowratio3y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getFactsetcurrentpricecashflowratio3y();
            }
        };
        qps[830] = new PropertySupport<QuoteRatiosSTK>() {
            public void set(QuoteRatiosSTK ratios, long value) {
                ratios.setFactsetcurrentpricecashflowratio4y(value);
            }

            public long getLong(QuoteRatiosSTK ratios) {
                return ratios.getFactsetcurrentpricecashflowratio4y();
            }
        };

    }
}