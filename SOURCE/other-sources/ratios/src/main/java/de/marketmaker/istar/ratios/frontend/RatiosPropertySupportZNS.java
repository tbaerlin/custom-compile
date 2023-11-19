package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class RatiosPropertySupportZNS {
    private static PropertySupport[] qps = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    private static PropertySupport[] ips = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    public static PropertySupport forQuote(int fieldid) {
        return qps[fieldid];
    }

    public static PropertySupport forInstrument(int fieldid) {
        return ips[fieldid];
    }

    static {

        ips[139] = new PropertySupport<InstrumentRatiosZNS>() {
            public void set(InstrumentRatiosZNS ratios, int localeIndex, String value) {
                ratios.setWkn(value);
            }

            public String getString(InstrumentRatiosZNS ratios, int localeIndex) {
                return ratios.getWkn();
            }
        };
        ips[140] = new PropertySupport<InstrumentRatiosZNS>() {
            public void set(InstrumentRatiosZNS ratios, int localeIndex, String value) {
                ratios.setIsin(value);
            }

            public String getString(InstrumentRatiosZNS ratios, int localeIndex) {
                return ratios.getIsin();
            }
        };
        ips[141] = new PropertySupport<InstrumentRatiosZNS>() {
            public void set(InstrumentRatiosZNS ratios, int localeIndex, String value) {
                ratios.setName(value);
            }

            public String getString(InstrumentRatiosZNS ratios, int localeIndex) {
                return ratios.getName();
            }
        };
        ips[151] = new PropertySupport<InstrumentRatiosZNS>() {
            public void set(InstrumentRatiosZNS ratios, int localeIndex, String value) {
                ratios.setIssuerName(value);
            }

            public String getString(InstrumentRatiosZNS ratios, int localeIndex) {
                return ratios.getIssuerName();
            }
        };
        ips[162] = new PropertySupport<InstrumentRatiosZNS>() {
            public void set(InstrumentRatiosZNS ratios, int localeIndex, String value) {
                ratios.setCountry(localeIndex, value);
            }

            public String getString(InstrumentRatiosZNS ratios, int localeIndex) {
                return ratios.getCountry(localeIndex);
            }
        };
        ips[203] = new PropertySupport<InstrumentRatiosZNS>() {
            public void set(InstrumentRatiosZNS ratios, int localeIndex, String value) {
                ratios.setSector(localeIndex, value);
            }

            public String getString(InstrumentRatiosZNS ratios, int localeIndex) {
                return ratios.getSector(localeIndex);
            }
        };
        ips[715] = new PropertySupport<InstrumentRatiosZNS>() {
            public void set(InstrumentRatiosZNS ratios, int localeIndex, String value) {
                ratios.setZnsCategory(value);
            }

            public String getString(InstrumentRatiosZNS ratios, int localeIndex) {
                return ratios.getZnsCategory();
            }
        };
        ips[716] = new PropertySupport<InstrumentRatiosZNS>() {
            public void set(InstrumentRatiosZNS ratios, int localeIndex, String value) {
                ratios.setMaturity(value);
            }

            public String getString(InstrumentRatiosZNS ratios, int localeIndex) {
                return ratios.getMaturity();
            }
        };
        ips[717] = new PropertySupport<InstrumentRatiosZNS>() {
            public void set(InstrumentRatiosZNS ratios, int localeIndex, String value) {
                ratios.setDebtRanking(value);
            }

            public String getString(InstrumentRatiosZNS ratios, int localeIndex) {
                return ratios.getDebtRanking();
            }
        };
        ips[718] = new PropertySupport<InstrumentRatiosZNS>() {
            public void set(InstrumentRatiosZNS ratios, int localeIndex, String value) {
                ratios.setIssuerType(value);
            }

            public String getString(InstrumentRatiosZNS ratios, int localeIndex) {
                return ratios.getIssuerType();
            }
        };
        ips[719] = new PropertySupport<InstrumentRatiosZNS>() {
            public void set(InstrumentRatiosZNS ratios, int localeIndex, String value) {
                ratios.setRestructuringRule(value);
            }

            public String getString(InstrumentRatiosZNS ratios, int localeIndex) {
                return ratios.getRestructuringRule();
            }
        };
        ips[720] = new PropertySupport<InstrumentRatiosZNS>() {
            public void set(InstrumentRatiosZNS ratios, int localeIndex, String value) {
                ratios.setSource(value);
            }

            public String getString(InstrumentRatiosZNS ratios, int localeIndex) {
                return ratios.getSource();
            }
        };
        ips[769] = new PropertySupport<InstrumentRatiosZNS>() {
            public void set(InstrumentRatiosZNS ratios, boolean value) {
                ratios.setWmNotActive(value);
            }

            public boolean getBoolean(InstrumentRatiosZNS ratios) {
                return ratios.getWmNotActive();
            }
        };

        qps[1] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setMostRecentUpdateTimestamp(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getMostRecentUpdateTimestamp();
            }
        };
        qps[4] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setVolatility1m(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getVolatility1m();
            }
        };
        qps[5] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setPerformance1m(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getPerformance1m();
            }
        };
        qps[10] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setReferenceTimestamp(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getReferenceTimestamp();
            }
        };
        qps[18] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setLastPrice(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getLastPrice();
            }
        };
        qps[67] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setVolatility3m(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getVolatility3m();
            }
        };
        qps[68] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setPerformance3m(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getPerformance3m();
            }
        };
        qps[69] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setPerformance1y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getPerformance1y();
            }
        };
        qps[72] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setVolatility10y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getVolatility10y();
            }
        };
        qps[73] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setVolatility1y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getVolatility1y();
            }
        };
        qps[105] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setPerformance10y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getPerformance10y();
            }
        };
        qps[108] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setVolatility1w(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getVolatility1w();
            }
        };
        qps[127] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setPerformance6m(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getPerformance6m();
            }
        };
        qps[128] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setPerformance3y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getPerformance3y();
            }
        };
        qps[129] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setPerformance5y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getPerformance5y();
            }
        };
        qps[130] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setVolatility6m(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getVolatility6m();
            }
        };
        qps[131] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setVolatility5y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getVolatility5y();
            }
        };
        qps[155] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int localeIndex, String value) {
                ratios.setVwdCode(value);
            }

            public String getString(QuoteRatiosZNS ratios, int localeIndex) {
                return ratios.getVwdCode();
            }
        };
        qps[156] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int localeIndex, String value) {
                ratios.setMmwkn(value);
            }

            public String getString(QuoteRatiosZNS ratios, int localeIndex) {
                return ratios.getMmwkn();
            }
        };
        qps[157] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int localeIndex, String value) {
                ratios.setCurrency(value);
            }

            public String getString(QuoteRatiosZNS ratios, int localeIndex) {
                return ratios.getCurrency();
            }
        };
        qps[167] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setHighAlltime(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getHighAlltime();
            }
        };
        qps[168] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setLowAlltime(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getLowAlltime();
            }
        };
        qps[169] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int value) {
                ratios.setHighAlltimeDate(value);
            }

            public int getInt(QuoteRatiosZNS ratios) {
                return ratios.getHighAlltimeDate();
            }
        };
        qps[171] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int value) {
                ratios.setLowAlltimeDate(value);
            }

            public int getInt(QuoteRatiosZNS ratios) {
                return ratios.getLowAlltimeDate();
            }
        };
        qps[177] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int value) {
                ratios.setHigh1yDate(value);
            }

            public int getInt(QuoteRatiosZNS ratios) {
                return ratios.getHigh1yDate();
            }
        };
        qps[178] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int value) {
                ratios.setLow1yDate(value);
            }

            public int getInt(QuoteRatiosZNS ratios) {
                return ratios.getLow1yDate();
            }
        };
        qps[179] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setAveragePrice1w(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getAveragePrice1w();
            }
        };
        qps[180] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setAveragePrice1m(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getAveragePrice1m();
            }
        };
        qps[181] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setAveragePrice3m(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getAveragePrice3m();
            }
        };
        qps[182] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setAveragePrice6m(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getAveragePrice6m();
            }
        };
        qps[183] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setAveragePrice1y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getAveragePrice1y();
            }
        };
        qps[184] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setAveragePrice3y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getAveragePrice3y();
            }
        };
        qps[186] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setAveragePrice5y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getAveragePrice5y();
            }
        };
        qps[187] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setAveragePrice10y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getAveragePrice10y();
            }
        };
        qps[299] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setVolatility3y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getVolatility3y();
            }
        };
        qps[300] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setPerformance1w(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getPerformance1w();
            }
        };
        qps[319] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setHigh1y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getHigh1y();
            }
        };
        qps[320] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setLow1y(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getLow1y();
            }
        };
        qps[332] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int localeIndex, String value) {
                ratios.setMarketmanagerName(value);
            }

            public String getString(QuoteRatiosZNS ratios, int localeIndex) {
                return ratios.getMarketmanagerName();
            }
        };
        qps[342] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setVolatilityCurrentYear(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getVolatilityCurrentYear();
            }
        };
        qps[392] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setPerformanceAlltime(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getPerformanceAlltime();
            }
        };
        qps[405] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setReferencePrice(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getReferencePrice();
            }
        };
        qps[414] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int localeIndex, String value) {
                ratios.setVwdMarket(value);
            }

            public String getString(QuoteRatiosZNS ratios, int localeIndex) {
                return ratios.getVwdMarket();
            }
        };
        qps[417] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setClosePreviousYear(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getClosePreviousYear();
            }
        };
        qps[418] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int value) {
                ratios.setClosePreviousYearDate(value);
            }

            public int getInt(QuoteRatiosZNS ratios) {
                return ratios.getClosePreviousYearDate();
            }
        };
        qps[419] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setClosePreviousMonth(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getClosePreviousMonth();
            }
        };
        qps[420] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int value) {
                ratios.setClosePreviousMonthDate(value);
            }

            public int getInt(QuoteRatiosZNS ratios) {
                return ratios.getClosePreviousMonthDate();
            }
        };
        qps[421] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setPerformanceCurrentYear(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getPerformanceCurrentYear();
            }
        };
        qps[422] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setPerformanceCurrentMonth(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getPerformanceCurrentMonth();
            }
        };
        qps[423] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setHighPreviousYear(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getHighPreviousYear();
            }
        };
        qps[424] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int value) {
                ratios.setHighPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosZNS ratios) {
                return ratios.getHighPreviousYearDate();
            }
        };
        qps[425] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setLowPreviousYear(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getLowPreviousYear();
            }
        };
        qps[426] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int value) {
                ratios.setLowPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosZNS ratios) {
                return ratios.getLowPreviousYearDate();
            }
        };
        qps[428] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setChangeNetCurrentYear(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getChangeNetCurrentYear();
            }
        };
        qps[429] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setChangeNetCurrentMonth(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getChangeNetCurrentMonth();
            }
        };
        qps[531] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setQid(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getQid();
            }
        };
        qps[648] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setMaSimple38(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getMaSimple38();
            }
        };
        qps[649] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setMaSimple90(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getMaSimple90();
            }
        };
        qps[650] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setMaSimple200(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getMaSimple200();
            }
        };
        qps[760] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setVolatilityAlltime(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getVolatilityAlltime();
            }
        };
        qps[814] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, long value) {
                ratios.setVolatility(value);
            }

            public long getLong(QuoteRatiosZNS ratios) {
                return ratios.getVolatility();
            }
        };
        qps[815] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int localeIndex, String value) {
                ratios.setRating(value);
            }

            public String getString(QuoteRatiosZNS ratios, int localeIndex) {
                return ratios.getRating();
            }
        };
        qps[816] = new PropertySupport<QuoteRatiosZNS>() {
            public void set(QuoteRatiosZNS ratios, int localeIndex, String value) {
                ratios.setStandard(value);
            }

            public String getString(QuoteRatiosZNS ratios, int localeIndex) {
                return ratios.getStandard();
            }
        };

    }
}