package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class RatiosPropertySupportOPT {
    private static PropertySupport[] qps = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    private static PropertySupport[] ips = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    public static PropertySupport forQuote(int fieldid) {
        return qps[fieldid];
    }

    public static PropertySupport forInstrument(int fieldid) {
        return ips[fieldid];
    }

    static {

        ips[11] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, int localeIndex, String value) {
                ratios.setCurrencyStrike(value);
            }

            public String getString(InstrumentRatiosOPT ratios, int localeIndex) {
                return ratios.getCurrencyStrike();
            }
        };
        ips[14] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, int value) {
                ratios.setExpires(value);
            }

            public int getInt(InstrumentRatiosOPT ratios) {
                return ratios.getExpires();
            }
        };
        ips[139] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, int localeIndex, String value) {
                ratios.setWkn(value);
            }

            public String getString(InstrumentRatiosOPT ratios, int localeIndex) {
                return ratios.getWkn();
            }
        };
        ips[140] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, int localeIndex, String value) {
                ratios.setIsin(value);
            }

            public String getString(InstrumentRatiosOPT ratios, int localeIndex) {
                return ratios.getIsin();
            }
        };
        ips[141] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, int localeIndex, String value) {
                ratios.setName(value);
            }

            public String getString(InstrumentRatiosOPT ratios, int localeIndex) {
                return ratios.getName();
            }
        };
        ips[148] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, long value) {
                ratios.setStrikePrice(value);
            }

            public long getLong(InstrumentRatiosOPT ratios) {
                return ratios.getStrikePrice();
            }
        };
        ips[149] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, int localeIndex, String value) {
                ratios.setOsType(value);
            }

            public String getString(InstrumentRatiosOPT ratios, int localeIndex) {
                return ratios.getOsType();
            }
        };
        ips[150] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, boolean value) {
                ratios.setIsAmerican(value);
            }

            public boolean getBoolean(InstrumentRatiosOPT ratios) {
                return ratios.getIsAmerican();
            }
        };
        ips[152] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, int localeIndex, String value) {
                ratios.setUnderlyingWkn(value);
            }

            public String getString(InstrumentRatiosOPT ratios, int localeIndex) {
                return ratios.getUnderlyingWkn();
            }
        };
        ips[153] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, int localeIndex, String value) {
                ratios.setUnderlyingIsin(value);
            }

            public String getString(InstrumentRatiosOPT ratios, int localeIndex) {
                return ratios.getUnderlyingIsin();
            }
        };
        ips[154] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, int localeIndex, String value) {
                ratios.setUnderlyingName(value);
            }

            public String getString(InstrumentRatiosOPT ratios, int localeIndex) {
                return ratios.getUnderlyingName();
            }
        };
        ips[172] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, int localeIndex, String value) {
                ratios.setExerciseType(value);
            }

            public String getString(InstrumentRatiosOPT ratios, int localeIndex) {
                return ratios.getExerciseType();
            }
        };
        ips[317] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, long value) {
                ratios.setUnderlyingIid(value);
            }

            public long getLong(InstrumentRatiosOPT ratios) {
                return ratios.getUnderlyingIid();
            }
        };
        ips[618] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, int localeIndex, String value) {
                ratios.setOptionCategory(value);
            }

            public String getString(InstrumentRatiosOPT ratios, int localeIndex) {
                return ratios.getOptionCategory();
            }
        };
        ips[698] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, long value) {
                ratios.setContractValue(value);
            }

            public long getLong(InstrumentRatiosOPT ratios) {
                return ratios.getContractValue();
            }
        };
        ips[699] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, long value) {
                ratios.setContractValueCalculated(value);
            }

            public long getLong(InstrumentRatiosOPT ratios) {
                return ratios.getContractValueCalculated();
            }
        };
        ips[701] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, long value) {
                ratios.setContractSize(value);
            }

            public long getLong(InstrumentRatiosOPT ratios) {
                return ratios.getContractSize();
            }
        };
        ips[702] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, long value) {
                ratios.setGenerationNumber(value);
            }

            public long getLong(InstrumentRatiosOPT ratios) {
                return ratios.getGenerationNumber();
            }
        };
        ips[703] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, long value) {
                ratios.setVersionNumber(value);
            }

            public long getLong(InstrumentRatiosOPT ratios) {
                return ratios.getVersionNumber();
            }
        };
        ips[704] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, int value) {
                ratios.setTradingMonth(value);
            }

            public int getInt(InstrumentRatiosOPT ratios) {
                return ratios.getTradingMonth();
            }
        };
        ips[713] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, long value) {
                ratios.setUnderlyingProductIid(value);
            }

            public long getLong(InstrumentRatiosOPT ratios) {
                return ratios.getUnderlyingProductIid();
            }
        };
        ips[756] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, int localeIndex, String value) {
                ratios.setUnderlyingEurexTicker(value);
            }

            public String getString(InstrumentRatiosOPT ratios, int localeIndex) {
                return ratios.getUnderlyingEurexTicker();
            }
        };
        ips[769] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, boolean value) {
                ratios.setWmNotActive(value);
            }

            public boolean getBoolean(InstrumentRatiosOPT ratios) {
                return ratios.getWmNotActive();
            }
        };
        ips[819] = new PropertySupport<InstrumentRatiosOPT>() {
            public void set(InstrumentRatiosOPT ratios, boolean value) {
                ratios.setIsFlex(value);
            }

            public boolean getBoolean(InstrumentRatiosOPT ratios) {
                return ratios.getIsFlex();
            }
        };

        qps[1] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setMostRecentUpdateTimestamp(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getMostRecentUpdateTimestamp();
            }
        };
        qps[4] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setVolatility1m(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getVolatility1m();
            }
        };
        qps[5] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setPerformance1m(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getPerformance1m();
            }
        };
        qps[10] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setReferenceTimestamp(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getReferenceTimestamp();
            }
        };
        qps[43] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setTotalVolume(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getTotalVolume();
            }
        };
        qps[67] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setVolatility3m(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getVolatility3m();
            }
        };
        qps[68] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setPerformance3m(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getPerformance3m();
            }
        };
        qps[69] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setPerformance1y(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getPerformance1y();
            }
        };
        qps[72] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setVolatility10y(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getVolatility10y();
            }
        };
        qps[73] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setVolatility1y(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getVolatility1y();
            }
        };
        qps[95] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setDelta(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getDelta();
            }
        };
        qps[96] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setOmega(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getOmega();
            }
        };
        qps[97] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setGamma(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getGamma();
            }
        };
        qps[98] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setVega(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getVega();
            }
        };
        qps[99] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setTheta(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getTheta();
            }
        };
        qps[100] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setRho(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getRho();
            }
        };
        qps[101] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setImpliedvolatility(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getImpliedvolatility();
            }
        };
        qps[105] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setPerformance10y(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getPerformance10y();
            }
        };
        qps[108] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setVolatility1w(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getVolatility1w();
            }
        };
        qps[127] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setPerformance6m(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getPerformance6m();
            }
        };
        qps[128] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setPerformance3y(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getPerformance3y();
            }
        };
        qps[129] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setPerformance5y(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getPerformance5y();
            }
        };
        qps[130] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setVolatility6m(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getVolatility6m();
            }
        };
        qps[131] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setVolatility5y(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getVolatility5y();
            }
        };
        qps[155] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int localeIndex, String value) {
                ratios.setVwdCode(value);
            }

            public String getString(QuoteRatiosOPT ratios, int localeIndex) {
                return ratios.getVwdCode();
            }
        };
        qps[156] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int localeIndex, String value) {
                ratios.setMmwkn(value);
            }

            public String getString(QuoteRatiosOPT ratios, int localeIndex) {
                return ratios.getMmwkn();
            }
        };
        qps[157] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int localeIndex, String value) {
                ratios.setCurrency(value);
            }

            public String getString(QuoteRatiosOPT ratios, int localeIndex) {
                return ratios.getCurrency();
            }
        };
        qps[167] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setHighAlltime(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getHighAlltime();
            }
        };
        qps[168] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setLowAlltime(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getLowAlltime();
            }
        };
        qps[169] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int value) {
                ratios.setHighAlltimeDate(value);
            }

            public int getInt(QuoteRatiosOPT ratios) {
                return ratios.getHighAlltimeDate();
            }
        };
        qps[171] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int value) {
                ratios.setLowAlltimeDate(value);
            }

            public int getInt(QuoteRatiosOPT ratios) {
                return ratios.getLowAlltimeDate();
            }
        };
        qps[177] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int value) {
                ratios.setHigh1yDate(value);
            }

            public int getInt(QuoteRatiosOPT ratios) {
                return ratios.getHigh1yDate();
            }
        };
        qps[178] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int value) {
                ratios.setLow1yDate(value);
            }

            public int getInt(QuoteRatiosOPT ratios) {
                return ratios.getLow1yDate();
            }
        };
        qps[299] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setVolatility3y(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getVolatility3y();
            }
        };
        qps[300] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setPerformance1w(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getPerformance1w();
            }
        };
        qps[319] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setHigh1y(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getHigh1y();
            }
        };
        qps[320] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setLow1y(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getLow1y();
            }
        };
        qps[332] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int localeIndex, String value) {
                ratios.setMarketmanagerName(value);
            }

            public String getString(QuoteRatiosOPT ratios, int localeIndex) {
                return ratios.getMarketmanagerName();
            }
        };
        qps[342] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setVolatilityCurrentYear(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getVolatilityCurrentYear();
            }
        };
        qps[405] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setReferencePrice(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getReferencePrice();
            }
        };
        qps[414] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int localeIndex, String value) {
                ratios.setVwdMarket(value);
            }

            public String getString(QuoteRatiosOPT ratios, int localeIndex) {
                return ratios.getVwdMarket();
            }
        };
        qps[417] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setClosePreviousYear(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getClosePreviousYear();
            }
        };
        qps[418] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int value) {
                ratios.setClosePreviousYearDate(value);
            }

            public int getInt(QuoteRatiosOPT ratios) {
                return ratios.getClosePreviousYearDate();
            }
        };
        qps[419] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setClosePreviousMonth(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getClosePreviousMonth();
            }
        };
        qps[420] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int value) {
                ratios.setClosePreviousMonthDate(value);
            }

            public int getInt(QuoteRatiosOPT ratios) {
                return ratios.getClosePreviousMonthDate();
            }
        };
        qps[421] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setPerformanceCurrentYear(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getPerformanceCurrentYear();
            }
        };
        qps[422] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setPerformanceCurrentMonth(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getPerformanceCurrentMonth();
            }
        };
        qps[423] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setHighPreviousYear(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getHighPreviousYear();
            }
        };
        qps[424] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int value) {
                ratios.setHighPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosOPT ratios) {
                return ratios.getHighPreviousYearDate();
            }
        };
        qps[425] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setLowPreviousYear(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getLowPreviousYear();
            }
        };
        qps[426] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int value) {
                ratios.setLowPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosOPT ratios) {
                return ratios.getLowPreviousYearDate();
            }
        };
        qps[428] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setChangeNetCurrentYear(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getChangeNetCurrentYear();
            }
        };
        qps[429] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setChangeNetCurrentMonth(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getChangeNetCurrentMonth();
            }
        };
        qps[531] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setQid(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getQid();
            }
        };
        qps[633] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setThetaRelative(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getThetaRelative();
            }
        };
        qps[634] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setTheta1w(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getTheta1w();
            }
        };
        qps[635] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setTheta1wRelative(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getTheta1wRelative();
            }
        };
        qps[636] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setTheta1m(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getTheta1m();
            }
        };
        qps[637] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setTheta1mRelative(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getTheta1mRelative();
            }
        };
        qps[648] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setMaSimple38(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getMaSimple38();
            }
        };
        qps[649] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setMaSimple90(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getMaSimple90();
            }
        };
        qps[650] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setMaSimple200(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getMaSimple200();
            }
        };
        qps[714] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int localeIndex, String value) {
                ratios.setBisKey(value);
            }

            public String getString(QuoteRatiosOPT ratios, int localeIndex) {
                return ratios.getBisKey();
            }
        };
        qps[727] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, int localeIndex, String value) {
                ratios.setVwdsymbol(value);
            }

            public String getString(QuoteRatiosOPT ratios, int localeIndex) {
                return ratios.getVwdsymbol();
            }
        };
        qps[760] = new PropertySupport<QuoteRatiosOPT>() {
            public void set(QuoteRatiosOPT ratios, long value) {
                ratios.setVolatilityAlltime(value);
            }

            public long getLong(QuoteRatiosOPT ratios) {
                return ratios.getVolatilityAlltime();
            }
        };

    }
}