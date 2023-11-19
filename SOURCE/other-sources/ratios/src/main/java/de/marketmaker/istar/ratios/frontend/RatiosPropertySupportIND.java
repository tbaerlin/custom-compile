package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class RatiosPropertySupportIND {
    private static PropertySupport[] qps = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    private static PropertySupport[] ips = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    public static PropertySupport forQuote(int fieldid) {
        return qps[fieldid];
    }

    public static PropertySupport forInstrument(int fieldid) {
        return ips[fieldid];
    }

    static {

        ips[139] = new PropertySupport<InstrumentRatiosIND>() {
            public void set(InstrumentRatiosIND ratios, int localeIndex, String value) {
                ratios.setWkn(value);
            }

            public String getString(InstrumentRatiosIND ratios, int localeIndex) {
                return ratios.getWkn();
            }
        };
        ips[140] = new PropertySupport<InstrumentRatiosIND>() {
            public void set(InstrumentRatiosIND ratios, int localeIndex, String value) {
                ratios.setIsin(value);
            }

            public String getString(InstrumentRatiosIND ratios, int localeIndex) {
                return ratios.getIsin();
            }
        };
        ips[141] = new PropertySupport<InstrumentRatiosIND>() {
            public void set(InstrumentRatiosIND ratios, int localeIndex, String value) {
                ratios.setName(value);
            }

            public String getString(InstrumentRatiosIND ratios, int localeIndex) {
                return ratios.getName();
            }
        };
        ips[769] = new PropertySupport<InstrumentRatiosIND>() {
            public void set(InstrumentRatiosIND ratios, boolean value) {
                ratios.setWmNotActive(value);
            }

            public boolean getBoolean(InstrumentRatiosIND ratios) {
                return ratios.getWmNotActive();
            }
        };

        qps[1] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setMostRecentUpdateTimestamp(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getMostRecentUpdateTimestamp();
            }
        };
        qps[4] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setVolatility1m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getVolatility1m();
            }
        };
        qps[5] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setPerformance1m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getPerformance1m();
            }
        };
        qps[10] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setReferenceTimestamp(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getReferenceTimestamp();
            }
        };
        qps[67] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setVolatility3m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getVolatility3m();
            }
        };
        qps[68] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setPerformance3m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getPerformance3m();
            }
        };
        qps[69] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setPerformance1y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getPerformance1y();
            }
        };
        qps[72] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setVolatility10y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getVolatility10y();
            }
        };
        qps[73] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setVolatility1y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getVolatility1y();
            }
        };
        qps[74] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setSharperatio1y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getSharperatio1y();
            }
        };
        qps[75] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setSharperatio3y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getSharperatio3y();
            }
        };
        qps[105] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setPerformance10y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getPerformance10y();
            }
        };
        qps[108] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setVolatility1w(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getVolatility1w();
            }
        };
        qps[122] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragevolume1m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragevolume1m();
            }
        };
        qps[123] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragevolume1y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragevolume1y();
            }
        };
        qps[127] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setPerformance6m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getPerformance6m();
            }
        };
        qps[128] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setPerformance3y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getPerformance3y();
            }
        };
        qps[129] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setPerformance5y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getPerformance5y();
            }
        };
        qps[130] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setVolatility6m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getVolatility6m();
            }
        };
        qps[131] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setVolatility5y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getVolatility5y();
            }
        };
        qps[132] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setSharperatio5y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getSharperatio5y();
            }
        };
        qps[133] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setSharperatio1m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getSharperatio1m();
            }
        };
        qps[134] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setSharperatio3m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getSharperatio3m();
            }
        };
        qps[135] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setSharperatio6m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getSharperatio6m();
            }
        };
        qps[155] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int localeIndex, String value) {
                ratios.setVwdCode(value);
            }

            public String getString(QuoteRatiosIND ratios, int localeIndex) {
                return ratios.getVwdCode();
            }
        };
        qps[156] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int localeIndex, String value) {
                ratios.setMmwkn(value);
            }

            public String getString(QuoteRatiosIND ratios, int localeIndex) {
                return ratios.getMmwkn();
            }
        };
        qps[157] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int localeIndex, String value) {
                ratios.setCurrency(value);
            }

            public String getString(QuoteRatiosIND ratios, int localeIndex) {
                return ratios.getCurrency();
            }
        };
        qps[167] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setHighAlltime(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getHighAlltime();
            }
        };
        qps[168] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setLowAlltime(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getLowAlltime();
            }
        };
        qps[169] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int value) {
                ratios.setHighAlltimeDate(value);
            }

            public int getInt(QuoteRatiosIND ratios) {
                return ratios.getHighAlltimeDate();
            }
        };
        qps[171] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int value) {
                ratios.setLowAlltimeDate(value);
            }

            public int getInt(QuoteRatiosIND ratios) {
                return ratios.getLowAlltimeDate();
            }
        };
        qps[177] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int value) {
                ratios.setHigh1yDate(value);
            }

            public int getInt(QuoteRatiosIND ratios) {
                return ratios.getHigh1yDate();
            }
        };
        qps[178] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int value) {
                ratios.setLow1yDate(value);
            }

            public int getInt(QuoteRatiosIND ratios) {
                return ratios.getLow1yDate();
            }
        };
        qps[179] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragePrice1w(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragePrice1w();
            }
        };
        qps[180] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragePrice1m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragePrice1m();
            }
        };
        qps[181] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragePrice3m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragePrice3m();
            }
        };
        qps[182] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragePrice6m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragePrice6m();
            }
        };
        qps[183] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragePrice1y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragePrice1y();
            }
        };
        qps[184] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragePrice3y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragePrice3y();
            }
        };
        qps[186] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragePrice5y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragePrice5y();
            }
        };
        qps[187] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragePrice10y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragePrice10y();
            }
        };
        qps[299] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setVolatility3y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getVolatility3y();
            }
        };
        qps[300] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setPerformance1w(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getPerformance1w();
            }
        };
        qps[315] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setMaximumLoss1y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getMaximumLoss1y();
            }
        };
        qps[319] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setHigh1y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getHigh1y();
            }
        };
        qps[320] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setLow1y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getLow1y();
            }
        };
        qps[321] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setMaximumLoss6m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getMaximumLoss6m();
            }
        };
        qps[323] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragevolume1w(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragevolume1w();
            }
        };
        qps[324] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragevolume3m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragevolume3m();
            }
        };
        qps[325] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragevolume6m(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragevolume6m();
            }
        };
        qps[326] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragevolume3y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragevolume3y();
            }
        };
        qps[327] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragevolume5y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragevolume5y();
            }
        };
        qps[329] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setAveragevolume10y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getAveragevolume10y();
            }
        };
        qps[332] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int localeIndex, String value) {
                ratios.setMarketmanagerName(value);
            }

            public String getString(QuoteRatiosIND ratios, int localeIndex) {
                return ratios.getMarketmanagerName();
            }
        };
        qps[334] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setMaximumLoss3y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getMaximumLoss3y();
            }
        };
        qps[342] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setVolatilityCurrentYear(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getVolatilityCurrentYear();
            }
        };
        qps[392] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setPerformanceAlltime(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getPerformanceAlltime();
            }
        };
        qps[394] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setSharperatio1w(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getSharperatio1w();
            }
        };
        qps[395] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setSharperatio10y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getSharperatio10y();
            }
        };
        qps[405] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setReferencePrice(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getReferencePrice();
            }
        };
        qps[414] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int localeIndex, String value) {
                ratios.setVwdMarket(value);
            }

            public String getString(QuoteRatiosIND ratios, int localeIndex) {
                return ratios.getVwdMarket();
            }
        };
        qps[417] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setClosePreviousYear(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getClosePreviousYear();
            }
        };
        qps[418] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int value) {
                ratios.setClosePreviousYearDate(value);
            }

            public int getInt(QuoteRatiosIND ratios) {
                return ratios.getClosePreviousYearDate();
            }
        };
        qps[419] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setClosePreviousMonth(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getClosePreviousMonth();
            }
        };
        qps[420] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int value) {
                ratios.setClosePreviousMonthDate(value);
            }

            public int getInt(QuoteRatiosIND ratios) {
                return ratios.getClosePreviousMonthDate();
            }
        };
        qps[421] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setPerformanceCurrentYear(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getPerformanceCurrentYear();
            }
        };
        qps[422] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setPerformanceCurrentMonth(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getPerformanceCurrentMonth();
            }
        };
        qps[423] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setHighPreviousYear(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getHighPreviousYear();
            }
        };
        qps[424] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int value) {
                ratios.setHighPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosIND ratios) {
                return ratios.getHighPreviousYearDate();
            }
        };
        qps[425] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setLowPreviousYear(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getLowPreviousYear();
            }
        };
        qps[426] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int value) {
                ratios.setLowPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosIND ratios) {
                return ratios.getLowPreviousYearDate();
            }
        };
        qps[428] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setChangeNetCurrentYear(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getChangeNetCurrentYear();
            }
        };
        qps[429] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setChangeNetCurrentMonth(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getChangeNetCurrentMonth();
            }
        };
        qps[531] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setQid(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getQid();
            }
        };
        qps[532] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, int value) {
                ratios.setDateFirstPrice(value);
            }

            public int getInt(QuoteRatiosIND ratios) {
                return ratios.getDateFirstPrice();
            }
        };
        qps[557] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setRsi7d(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getRsi7d();
            }
        };
        qps[558] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setRsi9d(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getRsi9d();
            }
        };
        qps[559] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setRsi14d(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getRsi14d();
            }
        };
        qps[560] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setRsi25d(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getRsi25d();
            }
        };
        qps[585] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setRsi90d(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getRsi90d();
            }
        };
        qps[586] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setRsi450d(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getRsi450d();
            }
        };
        qps[648] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setMaSimple38(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getMaSimple38();
            }
        };
        qps[649] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setMaSimple90(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getMaSimple90();
            }
        };
        qps[650] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setMaSimple200(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getMaSimple200();
            }
        };
        qps[747] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setRsi130d(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getRsi130d();
            }
        };
        qps[760] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setVolatilityAlltime(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getVolatilityAlltime();
            }
        };
        qps[763] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setSharperatioCurrentYear(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getSharperatioCurrentYear();
            }
        };
        qps[764] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setSharperatioAlltime(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getSharperatioAlltime();
            }
        };
        qps[770] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setMaximumLoss5y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getMaximumLoss5y();
            }
        };
        qps[771] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setMaximumLoss10y(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getMaximumLoss10y();
            }
        };
        qps[772] = new PropertySupport<QuoteRatiosIND>() {
            public void set(QuoteRatiosIND ratios, long value) {
                ratios.setMaximumLossAlltime(value);
            }

            public long getLong(QuoteRatiosIND ratios) {
                return ratios.getMaximumLossAlltime();
            }
        };

    }
}