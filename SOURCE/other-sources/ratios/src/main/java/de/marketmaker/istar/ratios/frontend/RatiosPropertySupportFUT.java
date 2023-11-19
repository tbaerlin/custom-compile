package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class RatiosPropertySupportFUT {
    private static PropertySupport[] qps = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    private static PropertySupport[] ips = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    public static PropertySupport forQuote(int fieldid) {
        return qps[fieldid];
    }

    public static PropertySupport forInstrument(int fieldid) {
        return ips[fieldid];
    }

    static {

        ips[14] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, int value) {
                ratios.setExpires(value);
            }

            public int getInt(InstrumentRatiosFUT ratios) {
                return ratios.getExpires();
            }
        };
        ips[139] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, int localeIndex, String value) {
                ratios.setWkn(value);
            }

            public String getString(InstrumentRatiosFUT ratios, int localeIndex) {
                return ratios.getWkn();
            }
        };
        ips[140] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, int localeIndex, String value) {
                ratios.setIsin(value);
            }

            public String getString(InstrumentRatiosFUT ratios, int localeIndex) {
                return ratios.getIsin();
            }
        };
        ips[141] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, int localeIndex, String value) {
                ratios.setName(value);
            }

            public String getString(InstrumentRatiosFUT ratios, int localeIndex) {
                return ratios.getName();
            }
        };
        ips[152] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, int localeIndex, String value) {
                ratios.setUnderlyingWkn(value);
            }

            public String getString(InstrumentRatiosFUT ratios, int localeIndex) {
                return ratios.getUnderlyingWkn();
            }
        };
        ips[153] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, int localeIndex, String value) {
                ratios.setUnderlyingIsin(value);
            }

            public String getString(InstrumentRatiosFUT ratios, int localeIndex) {
                return ratios.getUnderlyingIsin();
            }
        };
        ips[154] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, int localeIndex, String value) {
                ratios.setUnderlyingName(value);
            }

            public String getString(InstrumentRatiosFUT ratios, int localeIndex) {
                return ratios.getUnderlyingName();
            }
        };
        ips[317] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, long value) {
                ratios.setUnderlyingIid(value);
            }

            public long getLong(InstrumentRatiosFUT ratios) {
                return ratios.getUnderlyingIid();
            }
        };
        ips[695] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, long value) {
                ratios.setTickSize(value);
            }

            public long getLong(InstrumentRatiosFUT ratios) {
                return ratios.getTickSize();
            }
        };
        ips[696] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, long value) {
                ratios.setTickValue(value);
            }

            public long getLong(InstrumentRatiosFUT ratios) {
                return ratios.getTickValue();
            }
        };
        ips[697] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, int localeIndex, String value) {
                ratios.setTickCurrency(value);
            }

            public String getString(InstrumentRatiosFUT ratios, int localeIndex) {
                return ratios.getTickCurrency();
            }
        };
        ips[698] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, long value) {
                ratios.setContractValue(value);
            }

            public long getLong(InstrumentRatiosFUT ratios) {
                return ratios.getContractValue();
            }
        };
        ips[699] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, long value) {
                ratios.setContractValueCalculated(value);
            }

            public long getLong(InstrumentRatiosFUT ratios) {
                return ratios.getContractValueCalculated();
            }
        };
        ips[700] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, int localeIndex, String value) {
                ratios.setContractCurrency(value);
            }

            public String getString(InstrumentRatiosFUT ratios, int localeIndex) {
                return ratios.getContractCurrency();
            }
        };
        ips[713] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, long value) {
                ratios.setUnderlyingProductIid(value);
            }

            public long getLong(InstrumentRatiosFUT ratios) {
                return ratios.getUnderlyingProductIid();
            }
        };
        ips[756] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, int localeIndex, String value) {
                ratios.setUnderlyingEurexTicker(value);
            }

            public String getString(InstrumentRatiosFUT ratios, int localeIndex) {
                return ratios.getUnderlyingEurexTicker();
            }
        };
        ips[769] = new PropertySupport<InstrumentRatiosFUT>() {
            public void set(InstrumentRatiosFUT ratios, boolean value) {
                ratios.setWmNotActive(value);
            }

            public boolean getBoolean(InstrumentRatiosFUT ratios) {
                return ratios.getWmNotActive();
            }
        };

        qps[1] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setMostRecentUpdateTimestamp(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getMostRecentUpdateTimestamp();
            }
        };
        qps[4] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setVolatility1m(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getVolatility1m();
            }
        };
        qps[5] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setPerformance1m(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getPerformance1m();
            }
        };
        qps[10] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setReferenceTimestamp(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getReferenceTimestamp();
            }
        };
        qps[43] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setTotalVolume(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getTotalVolume();
            }
        };
        qps[67] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setVolatility3m(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getVolatility3m();
            }
        };
        qps[68] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setPerformance3m(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getPerformance3m();
            }
        };
        qps[69] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setPerformance1y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getPerformance1y();
            }
        };
        qps[72] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setVolatility10y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getVolatility10y();
            }
        };
        qps[73] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setVolatility1y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getVolatility1y();
            }
        };
        qps[105] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setPerformance10y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getPerformance10y();
            }
        };
        qps[108] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setVolatility1w(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getVolatility1w();
            }
        };
        qps[122] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragevolume1m(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragevolume1m();
            }
        };
        qps[123] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragevolume1y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragevolume1y();
            }
        };
        qps[127] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setPerformance6m(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getPerformance6m();
            }
        };
        qps[128] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setPerformance3y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getPerformance3y();
            }
        };
        qps[129] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setPerformance5y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getPerformance5y();
            }
        };
        qps[130] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setVolatility6m(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getVolatility6m();
            }
        };
        qps[131] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setVolatility5y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getVolatility5y();
            }
        };
        qps[155] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int localeIndex, String value) {
                ratios.setVwdCode(value);
            }

            public String getString(QuoteRatiosFUT ratios, int localeIndex) {
                return ratios.getVwdCode();
            }
        };
        qps[156] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int localeIndex, String value) {
                ratios.setMmwkn(value);
            }

            public String getString(QuoteRatiosFUT ratios, int localeIndex) {
                return ratios.getMmwkn();
            }
        };
        qps[157] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int localeIndex, String value) {
                ratios.setCurrency(value);
            }

            public String getString(QuoteRatiosFUT ratios, int localeIndex) {
                return ratios.getCurrency();
            }
        };
        qps[167] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setHighAlltime(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getHighAlltime();
            }
        };
        qps[168] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setLowAlltime(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getLowAlltime();
            }
        };
        qps[169] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int value) {
                ratios.setHighAlltimeDate(value);
            }

            public int getInt(QuoteRatiosFUT ratios) {
                return ratios.getHighAlltimeDate();
            }
        };
        qps[171] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int value) {
                ratios.setLowAlltimeDate(value);
            }

            public int getInt(QuoteRatiosFUT ratios) {
                return ratios.getLowAlltimeDate();
            }
        };
        qps[177] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int value) {
                ratios.setHigh1yDate(value);
            }

            public int getInt(QuoteRatiosFUT ratios) {
                return ratios.getHigh1yDate();
            }
        };
        qps[178] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int value) {
                ratios.setLow1yDate(value);
            }

            public int getInt(QuoteRatiosFUT ratios) {
                return ratios.getLow1yDate();
            }
        };
        qps[179] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragePrice1w(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragePrice1w();
            }
        };
        qps[180] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragePrice1m(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragePrice1m();
            }
        };
        qps[181] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragePrice3m(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragePrice3m();
            }
        };
        qps[182] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragePrice6m(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragePrice6m();
            }
        };
        qps[183] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragePrice1y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragePrice1y();
            }
        };
        qps[184] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragePrice3y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragePrice3y();
            }
        };
        qps[186] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragePrice5y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragePrice5y();
            }
        };
        qps[187] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragePrice10y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragePrice10y();
            }
        };
        qps[299] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setVolatility3y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getVolatility3y();
            }
        };
        qps[300] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setPerformance1w(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getPerformance1w();
            }
        };
        qps[319] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setHigh1y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getHigh1y();
            }
        };
        qps[320] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setLow1y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getLow1y();
            }
        };
        qps[323] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragevolume1w(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragevolume1w();
            }
        };
        qps[324] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragevolume3m(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragevolume3m();
            }
        };
        qps[325] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragevolume6m(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragevolume6m();
            }
        };
        qps[326] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragevolume3y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragevolume3y();
            }
        };
        qps[327] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragevolume5y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragevolume5y();
            }
        };
        qps[329] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setAveragevolume10y(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getAveragevolume10y();
            }
        };
        qps[332] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int localeIndex, String value) {
                ratios.setMarketmanagerName(value);
            }

            public String getString(QuoteRatiosFUT ratios, int localeIndex) {
                return ratios.getMarketmanagerName();
            }
        };
        qps[342] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setVolatilityCurrentYear(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getVolatilityCurrentYear();
            }
        };
        qps[405] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setReferencePrice(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getReferencePrice();
            }
        };
        qps[414] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int localeIndex, String value) {
                ratios.setVwdMarket(value);
            }

            public String getString(QuoteRatiosFUT ratios, int localeIndex) {
                return ratios.getVwdMarket();
            }
        };
        qps[417] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setClosePreviousYear(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getClosePreviousYear();
            }
        };
        qps[418] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int value) {
                ratios.setClosePreviousYearDate(value);
            }

            public int getInt(QuoteRatiosFUT ratios) {
                return ratios.getClosePreviousYearDate();
            }
        };
        qps[419] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setClosePreviousMonth(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getClosePreviousMonth();
            }
        };
        qps[420] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int value) {
                ratios.setClosePreviousMonthDate(value);
            }

            public int getInt(QuoteRatiosFUT ratios) {
                return ratios.getClosePreviousMonthDate();
            }
        };
        qps[421] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setPerformanceCurrentYear(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getPerformanceCurrentYear();
            }
        };
        qps[422] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setPerformanceCurrentMonth(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getPerformanceCurrentMonth();
            }
        };
        qps[423] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setHighPreviousYear(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getHighPreviousYear();
            }
        };
        qps[424] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int value) {
                ratios.setHighPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosFUT ratios) {
                return ratios.getHighPreviousYearDate();
            }
        };
        qps[425] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setLowPreviousYear(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getLowPreviousYear();
            }
        };
        qps[426] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int value) {
                ratios.setLowPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosFUT ratios) {
                return ratios.getLowPreviousYearDate();
            }
        };
        qps[428] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setChangeNetCurrentYear(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getChangeNetCurrentYear();
            }
        };
        qps[429] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setChangeNetCurrentMonth(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getChangeNetCurrentMonth();
            }
        };
        qps[531] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setQid(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getQid();
            }
        };
        qps[648] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setMaSimple38(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getMaSimple38();
            }
        };
        qps[649] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setMaSimple90(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getMaSimple90();
            }
        };
        qps[650] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setMaSimple200(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getMaSimple200();
            }
        };
        qps[714] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, int localeIndex, String value) {
                ratios.setBisKey(value);
            }

            public String getString(QuoteRatiosFUT ratios, int localeIndex) {
                return ratios.getBisKey();
            }
        };
        qps[760] = new PropertySupport<QuoteRatiosFUT>() {
            public void set(QuoteRatiosFUT ratios, long value) {
                ratios.setVolatilityAlltime(value);
            }

            public long getLong(QuoteRatiosFUT ratios) {
                return ratios.getVolatilityAlltime();
            }
        };

    }
}