package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class RatiosPropertySupportMER {
    private static PropertySupport[] qps = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    private static PropertySupport[] ips = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    public static PropertySupport forQuote(int fieldid) {
        return qps[fieldid];
    }

    public static PropertySupport forInstrument(int fieldid) {
        return ips[fieldid];
    }

    static {

        ips[139] = new PropertySupport<InstrumentRatiosMER>() {
            public void set(InstrumentRatiosMER ratios, int localeIndex, String value) {
                ratios.setWkn(value);
            }

            public String getString(InstrumentRatiosMER ratios, int localeIndex) {
                return ratios.getWkn();
            }
        };
        ips[140] = new PropertySupport<InstrumentRatiosMER>() {
            public void set(InstrumentRatiosMER ratios, int localeIndex, String value) {
                ratios.setIsin(value);
            }

            public String getString(InstrumentRatiosMER ratios, int localeIndex) {
                return ratios.getIsin();
            }
        };
        ips[141] = new PropertySupport<InstrumentRatiosMER>() {
            public void set(InstrumentRatiosMER ratios, int localeIndex, String value) {
                ratios.setName(value);
            }

            public String getString(InstrumentRatiosMER ratios, int localeIndex) {
                return ratios.getName();
            }
        };
        ips[769] = new PropertySupport<InstrumentRatiosMER>() {
            public void set(InstrumentRatiosMER ratios, boolean value) {
                ratios.setWmNotActive(value);
            }

            public boolean getBoolean(InstrumentRatiosMER ratios) {
                return ratios.getWmNotActive();
            }
        };
        ips[790] = new PropertySupport<InstrumentRatiosMER>() {
            public void set(InstrumentRatiosMER ratios, int localeIndex, String value) {
                ratios.setLmeMetalCode(value);
            }

            public String getString(InstrumentRatiosMER ratios, int localeIndex) {
                return ratios.getLmeMetalCode();
            }
        };

        qps[1] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setMostRecentUpdateTimestamp(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getMostRecentUpdateTimestamp();
            }
        };
        qps[4] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setVolatility1m(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getVolatility1m();
            }
        };
        qps[5] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setPerformance1m(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getPerformance1m();
            }
        };
        qps[10] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setReferenceTimestamp(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getReferenceTimestamp();
            }
        };
        qps[67] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setVolatility3m(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getVolatility3m();
            }
        };
        qps[68] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setPerformance3m(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getPerformance3m();
            }
        };
        qps[69] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setPerformance1y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getPerformance1y();
            }
        };
        qps[72] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setVolatility10y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getVolatility10y();
            }
        };
        qps[73] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setVolatility1y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getVolatility1y();
            }
        };
        qps[105] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setPerformance10y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getPerformance10y();
            }
        };
        qps[108] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setVolatility1w(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getVolatility1w();
            }
        };
        qps[127] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setPerformance6m(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getPerformance6m();
            }
        };
        qps[128] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setPerformance3y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getPerformance3y();
            }
        };
        qps[129] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setPerformance5y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getPerformance5y();
            }
        };
        qps[130] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setVolatility6m(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getVolatility6m();
            }
        };
        qps[131] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setVolatility5y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getVolatility5y();
            }
        };
        qps[155] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int localeIndex, String value) {
                ratios.setVwdCode(value);
            }

            public String getString(QuoteRatiosMER ratios, int localeIndex) {
                return ratios.getVwdCode();
            }
        };
        qps[156] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int localeIndex, String value) {
                ratios.setMmwkn(value);
            }

            public String getString(QuoteRatiosMER ratios, int localeIndex) {
                return ratios.getMmwkn();
            }
        };
        qps[157] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int localeIndex, String value) {
                ratios.setCurrency(value);
            }

            public String getString(QuoteRatiosMER ratios, int localeIndex) {
                return ratios.getCurrency();
            }
        };
        qps[167] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setHighAlltime(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getHighAlltime();
            }
        };
        qps[168] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setLowAlltime(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getLowAlltime();
            }
        };
        qps[169] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int value) {
                ratios.setHighAlltimeDate(value);
            }

            public int getInt(QuoteRatiosMER ratios) {
                return ratios.getHighAlltimeDate();
            }
        };
        qps[171] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int value) {
                ratios.setLowAlltimeDate(value);
            }

            public int getInt(QuoteRatiosMER ratios) {
                return ratios.getLowAlltimeDate();
            }
        };
        qps[177] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int value) {
                ratios.setHigh1yDate(value);
            }

            public int getInt(QuoteRatiosMER ratios) {
                return ratios.getHigh1yDate();
            }
        };
        qps[178] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int value) {
                ratios.setLow1yDate(value);
            }

            public int getInt(QuoteRatiosMER ratios) {
                return ratios.getLow1yDate();
            }
        };
        qps[179] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setAveragePrice1w(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getAveragePrice1w();
            }
        };
        qps[180] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setAveragePrice1m(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getAveragePrice1m();
            }
        };
        qps[181] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setAveragePrice3m(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getAveragePrice3m();
            }
        };
        qps[182] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setAveragePrice6m(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getAveragePrice6m();
            }
        };
        qps[183] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setAveragePrice1y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getAveragePrice1y();
            }
        };
        qps[184] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setAveragePrice3y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getAveragePrice3y();
            }
        };
        qps[186] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setAveragePrice5y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getAveragePrice5y();
            }
        };
        qps[187] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setAveragePrice10y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getAveragePrice10y();
            }
        };
        qps[299] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setVolatility3y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getVolatility3y();
            }
        };
        qps[300] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setPerformance1w(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getPerformance1w();
            }
        };
        qps[319] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setHigh1y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getHigh1y();
            }
        };
        qps[320] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setLow1y(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getLow1y();
            }
        };
        qps[332] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int localeIndex, String value) {
                ratios.setMarketmanagerName(value);
            }

            public String getString(QuoteRatiosMER ratios, int localeIndex) {
                return ratios.getMarketmanagerName();
            }
        };
        qps[342] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setVolatilityCurrentYear(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getVolatilityCurrentYear();
            }
        };
        qps[392] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setPerformanceAlltime(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getPerformanceAlltime();
            }
        };
        qps[405] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setReferencePrice(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getReferencePrice();
            }
        };
        qps[414] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int localeIndex, String value) {
                ratios.setVwdMarket(value);
            }

            public String getString(QuoteRatiosMER ratios, int localeIndex) {
                return ratios.getVwdMarket();
            }
        };
        qps[417] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setClosePreviousYear(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getClosePreviousYear();
            }
        };
        qps[418] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int value) {
                ratios.setClosePreviousYearDate(value);
            }

            public int getInt(QuoteRatiosMER ratios) {
                return ratios.getClosePreviousYearDate();
            }
        };
        qps[419] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setClosePreviousMonth(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getClosePreviousMonth();
            }
        };
        qps[420] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int value) {
                ratios.setClosePreviousMonthDate(value);
            }

            public int getInt(QuoteRatiosMER ratios) {
                return ratios.getClosePreviousMonthDate();
            }
        };
        qps[421] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setPerformanceCurrentYear(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getPerformanceCurrentYear();
            }
        };
        qps[422] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setPerformanceCurrentMonth(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getPerformanceCurrentMonth();
            }
        };
        qps[423] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setHighPreviousYear(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getHighPreviousYear();
            }
        };
        qps[424] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int value) {
                ratios.setHighPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosMER ratios) {
                return ratios.getHighPreviousYearDate();
            }
        };
        qps[425] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setLowPreviousYear(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getLowPreviousYear();
            }
        };
        qps[426] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int value) {
                ratios.setLowPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosMER ratios) {
                return ratios.getLowPreviousYearDate();
            }
        };
        qps[428] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setChangeNetCurrentYear(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getChangeNetCurrentYear();
            }
        };
        qps[429] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setChangeNetCurrentMonth(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getChangeNetCurrentMonth();
            }
        };
        qps[531] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setQid(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getQid();
            }
        };
        qps[648] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setMaSimple38(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getMaSimple38();
            }
        };
        qps[649] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setMaSimple90(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getMaSimple90();
            }
        };
        qps[650] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setMaSimple200(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getMaSimple200();
            }
        };
        qps[760] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, long value) {
                ratios.setVolatilityAlltime(value);
            }

            public long getLong(QuoteRatiosMER ratios) {
                return ratios.getVolatilityAlltime();
            }
        };
        qps[791] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int value) {
                ratios.setLmeExpirationDate(value);
            }

            public int getInt(QuoteRatiosMER ratios) {
                return ratios.getLmeExpirationDate();
            }
        };
        qps[800] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, boolean value) {
                ratios.setLmeComposite(value);
            }

            public boolean getBoolean(QuoteRatiosMER ratios) {
                return ratios.getLmeComposite();
            }
        };
        qps[811] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int localeIndex, String value) {
                ratios.setMerInstrumentenTyp(value);
            }

            public String getString(QuoteRatiosMER ratios, int localeIndex) {
                return ratios.getMerInstrumentenTyp();
            }
        };
        qps[812] = new PropertySupport<QuoteRatiosMER>() {
            public void set(QuoteRatiosMER ratios, int localeIndex, String value) {
                ratios.setMerHandelsmonat(value);
            }

            public String getString(QuoteRatiosMER ratios, int localeIndex) {
                return ratios.getMerHandelsmonat();
            }
        };

    }
}