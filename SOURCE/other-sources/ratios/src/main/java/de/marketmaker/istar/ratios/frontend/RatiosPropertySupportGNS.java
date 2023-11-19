package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class RatiosPropertySupportGNS {
    private static PropertySupport[] qps = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    private static PropertySupport[] ips = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    public static PropertySupport forQuote(int fieldid) {
        return qps[fieldid];
    }

    public static PropertySupport forInstrument(int fieldid) {
        return ips[fieldid];
    }

    static {

        ips[14] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int value) {
                ratios.setExpires(value);
            }

            public int getInt(InstrumentRatiosGNS ratios) {
                return ratios.getExpires();
            }
        };
        ips[81] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, long value) {
                ratios.setWmIssueVolume(value);
            }

            public long getLong(InstrumentRatiosGNS ratios) {
                return ratios.getWmIssueVolume();
            }
        };
        ips[112] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, long value) {
                ratios.setWmcoupon(value);
            }

            public long getLong(InstrumentRatiosGNS ratios) {
                return ratios.getWmcoupon();
            }
        };
        ips[113] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int value) {
                ratios.setWmexpirationdate(value);
            }

            public int getInt(InstrumentRatiosGNS ratios) {
                return ratios.getWmexpirationdate();
            }
        };
        ips[139] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setWkn(value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getWkn();
            }
        };
        ips[140] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setIsin(value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getIsin();
            }
        };
        ips[141] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setName(value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getName();
            }
        };
        ips[151] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setIssuerName(value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getIssuerName();
            }
        };
        ips[160] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, long value) {
                ratios.setInterest(value);
            }

            public long getLong(InstrumentRatiosGNS ratios) {
                return ratios.getInterest();
            }
        };
        ips[161] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setInterestType(value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getInterestType();
            }
        };
        ips[162] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setCountry(localeIndex, value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getCountry(localeIndex);
            }
        };
        ips[163] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setBondType(value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getBondType();
            }
        };
        ips[185] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, long value) {
                ratios.setIssueVolume(value);
            }

            public long getLong(InstrumentRatiosGNS ratios) {
                return ratios.getIssueVolume();
            }
        };
        ips[202] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int value) {
                ratios.setIssueDate(value);
            }

            public int getInt(InstrumentRatiosGNS ratios) {
                return ratios.getIssueDate();
            }
        };
        ips[301] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setWmBondtype(localeIndex, value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getWmBondtype(localeIndex);
            }
        };
        ips[302] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setWmCoupontype(localeIndex, value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getWmCoupontype(localeIndex);
            }
        };
        ips[304] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, long value) {
                ratios.setWmNominalInterest(value);
            }

            public long getLong(InstrumentRatiosGNS ratios) {
                return ratios.getWmNominalInterest();
            }
        };
        ips[306] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setWmInterestPeriod(localeIndex, value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getWmInterestPeriod(localeIndex);
            }
        };
        ips[353] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setWmCountry(localeIndex, value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getWmCountry(localeIndex);
            }
        };
        ips[354] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setWmissuecurrency(value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getWmissuecurrency();
            }
        };
        ips[489] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, long value) {
                ratios.setWmDividend(value);
            }

            public long getLong(InstrumentRatiosGNS ratios) {
                return ratios.getWmDividend();
            }
        };
        ips[490] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setWmDividendCurrency(value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getWmDividendCurrency();
            }
        };
        ips[492] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setBenchmarkName(value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getBenchmarkName();
            }
        };
        ips[533] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setIssuerCategory(localeIndex, value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getIssuerCategory(localeIndex);
            }
        };
        ips[556] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, long value) {
                ratios.setWmDividendLastYear(value);
            }

            public long getLong(InstrumentRatiosGNS ratios) {
                return ratios.getWmDividendLastYear();
            }
        };
        ips[645] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setWmSector(localeIndex, value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getWmSector(localeIndex);
            }
        };
        ips[646] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int value) {
                ratios.setWmIssueDate(value);
            }

            public int getInt(InstrumentRatiosGNS ratios) {
                return ratios.getWmIssueDate();
            }
        };
        ips[651] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, long value) {
                ratios.setWmNumberOfIssuedEquities(value);
            }

            public long getLong(InstrumentRatiosGNS ratios) {
                return ratios.getWmNumberOfIssuedEquities();
            }
        };
        ips[729] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, int localeIndex, String value) {
                ratios.setWmIssuerName(value);
            }

            public String getString(InstrumentRatiosGNS ratios, int localeIndex) {
                return ratios.getWmIssuerName();
            }
        };
        ips[769] = new PropertySupport<InstrumentRatiosGNS>() {
            public void set(InstrumentRatiosGNS ratios, boolean value) {
                ratios.setWmNotActive(value);
            }

            public boolean getBoolean(InstrumentRatiosGNS ratios) {
                return ratios.getWmNotActive();
            }
        };

        qps[1] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setMostRecentUpdateTimestamp(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getMostRecentUpdateTimestamp();
            }
        };
        qps[4] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setVolatility1m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getVolatility1m();
            }
        };
        qps[5] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformance1m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformance1m();
            }
        };
        qps[6] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setSpread(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getSpread();
            }
        };
        qps[10] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setReferenceTimestamp(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getReferenceTimestamp();
            }
        };
        qps[15] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setSpreadRelative(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getSpreadRelative();
            }
        };
        qps[16] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setBid(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getBid();
            }
        };
        qps[17] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAsk(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAsk();
            }
        };
        qps[18] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setLastPrice(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getLastPrice();
            }
        };
        qps[19] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPreviousClose(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPreviousClose();
            }
        };
        qps[28] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setBidVolume(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getBidVolume();
            }
        };
        qps[29] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAskVolume(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAskVolume();
            }
        };
        qps[34] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setBidAskDate(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getBidAskDate();
            }
        };
        qps[35] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setBidAskTime(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getBidAskTime();
            }
        };
        qps[36] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setTradeVolume(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getTradeVolume();
            }
        };
        qps[37] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setLastDate(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getLastDate();
            }
        };
        qps[38] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setLastTime(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getLastTime();
            }
        };
        qps[39] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setHigh(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getHigh();
            }
        };
        qps[40] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setLow(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getLow();
            }
        };
        qps[41] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setHighYear(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getHighYear();
            }
        };
        qps[42] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setLowYear(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getLowYear();
            }
        };
        qps[43] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setTotalVolume(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getTotalVolume();
            }
        };
        qps[44] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setPreviousDate(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getPreviousDate();
            }
        };
        qps[45] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setOpen(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getOpen();
            }
        };
        qps[60] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setIssuePrice(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getIssuePrice();
            }
        };
        qps[67] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setVolatility3m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getVolatility3m();
            }
        };
        qps[68] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformance3m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformance3m();
            }
        };
        qps[69] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformance1y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformance1y();
            }
        };
        qps[72] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setVolatility10y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getVolatility10y();
            }
        };
        qps[73] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setVolatility1y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getVolatility1y();
            }
        };
        qps[80] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setBeta1y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getBeta1y();
            }
        };
        qps[82] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setCorrelation1y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getCorrelation1y();
            }
        };
        qps[83] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setMarketcapitalization(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getMarketcapitalization();
            }
        };
        qps[105] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformance10y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformance10y();
            }
        };
        qps[108] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setVolatility1w(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getVolatility1w();
            }
        };
        qps[117] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformance1d(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformance1d();
            }
        };
        qps[118] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformancetobenchmark3m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformancetobenchmark3m();
            }
        };
        qps[119] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformancetobenchmark1w(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformancetobenchmark1w();
            }
        };
        qps[120] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformancetobenchmark1m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformancetobenchmark1m();
            }
        };
        qps[121] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformancetobenchmark1y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformancetobenchmark1y();
            }
        };
        qps[122] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragevolume1m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragevolume1m();
            }
        };
        qps[123] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragevolume1y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragevolume1y();
            }
        };
        qps[124] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setBeta1m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getBeta1m();
            }
        };
        qps[125] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setEarning(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getEarning();
            }
        };
        qps[126] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformancetobenchmark6m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformancetobenchmark6m();
            }
        };
        qps[127] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformance6m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformance6m();
            }
        };
        qps[128] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformance3y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformance3y();
            }
        };
        qps[129] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformance5y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformance5y();
            }
        };
        qps[130] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setVolatility6m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getVolatility6m();
            }
        };
        qps[131] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setVolatility5y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getVolatility5y();
            }
        };
        qps[136] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setCorrelation1m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getCorrelation1m();
            }
        };
        qps[155] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int localeIndex, String value) {
                ratios.setVwdCode(value);
            }

            public String getString(QuoteRatiosGNS ratios, int localeIndex) {
                return ratios.getVwdCode();
            }
        };
        qps[156] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int localeIndex, String value) {
                ratios.setMmwkn(value);
            }

            public String getString(QuoteRatiosGNS ratios, int localeIndex) {
                return ratios.getMmwkn();
            }
        };
        qps[157] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int localeIndex, String value) {
                ratios.setCurrency(value);
            }

            public String getString(QuoteRatiosGNS ratios, int localeIndex) {
                return ratios.getCurrency();
            }
        };
        qps[158] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setYieldRelative_mdps(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getYieldRelative_mdps();
            }
        };
        qps[159] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setDuration_mdps(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getDuration_mdps();
            }
        };
        qps[167] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setHighAlltime(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getHighAlltime();
            }
        };
        qps[168] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setLowAlltime(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getLowAlltime();
            }
        };
        qps[169] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setHighAlltimeDate(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getHighAlltimeDate();
            }
        };
        qps[171] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setLowAlltimeDate(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getLowAlltimeDate();
            }
        };
        qps[177] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setHigh1yDate(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getHigh1yDate();
            }
        };
        qps[178] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setLow1yDate(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getLow1yDate();
            }
        };
        qps[179] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragePrice1w(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragePrice1w();
            }
        };
        qps[180] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragePrice1m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragePrice1m();
            }
        };
        qps[181] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragePrice3m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragePrice3m();
            }
        };
        qps[182] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragePrice6m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragePrice6m();
            }
        };
        qps[183] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragePrice1y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragePrice1y();
            }
        };
        qps[184] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragePrice3y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragePrice3y();
            }
        };
        qps[186] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragePrice5y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragePrice5y();
            }
        };
        qps[187] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragePrice10y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragePrice10y();
            }
        };
        qps[188] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setBeta1w(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getBeta1w();
            }
        };
        qps[189] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setBeta3m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getBeta3m();
            }
        };
        qps[190] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setBeta6m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getBeta6m();
            }
        };
        qps[191] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformancetobenchmark3y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformancetobenchmark3y();
            }
        };
        qps[192] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformancetobenchmark5y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformancetobenchmark5y();
            }
        };
        qps[204] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformancetobenchmark10y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformancetobenchmark10y();
            }
        };
        qps[240] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setChangeNet(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getChangeNet();
            }
        };
        qps[241] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setChangePercent(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getChangePercent();
            }
        };
        qps[299] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setVolatility3y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getVolatility3y();
            }
        };
        qps[300] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformance1w(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformance1w();
            }
        };
        qps[307] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setCorrelation1w(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getCorrelation1w();
            }
        };
        qps[308] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setCorrelation3m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getCorrelation3m();
            }
        };
        qps[309] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setCorrelation6m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getCorrelation6m();
            }
        };
        qps[310] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setCorrelation3y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getCorrelation3y();
            }
        };
        qps[311] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setCorrelation5y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getCorrelation5y();
            }
        };
        qps[312] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setCorrelation10y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getCorrelation10y();
            }
        };
        qps[319] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setHigh1y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getHigh1y();
            }
        };
        qps[320] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setLow1y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getLow1y();
            }
        };
        qps[323] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragevolume1w(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragevolume1w();
            }
        };
        qps[324] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragevolume3m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragevolume3m();
            }
        };
        qps[325] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragevolume6m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragevolume6m();
            }
        };
        qps[326] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragevolume3y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragevolume3y();
            }
        };
        qps[327] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragevolume5y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragevolume5y();
            }
        };
        qps[329] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAveragevolume10y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAveragevolume10y();
            }
        };
        qps[332] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int localeIndex, String value) {
                ratios.setMarketmanagerName(value);
            }

            public String getString(QuoteRatiosGNS ratios, int localeIndex) {
                return ratios.getMarketmanagerName();
            }
        };
        qps[342] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setVolatilityCurrentYear(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getVolatilityCurrentYear();
            }
        };
        qps[346] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAlpha1m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAlpha1m();
            }
        };
        qps[347] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAlpha1y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAlpha1y();
            }
        };
        qps[355] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setMdpsbasepointvalue(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getMdpsbasepointvalue();
            }
        };
        qps[357] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setMdpsconvexity(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getMdpsconvexity();
            }
        };
        qps[358] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setMdpsmodifiedduration(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getMdpsmodifiedduration();
            }
        };
        qps[359] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setMdpsbrokenperiodinterest(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getMdpsbrokenperiodinterest();
            }
        };
        qps[366] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setMdpsinterestrateelasticity(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getMdpsinterestrateelasticity();
            }
        };
        qps[371] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setBeta3y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getBeta3y();
            }
        };
        qps[382] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setBeta5y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getBeta5y();
            }
        };
        qps[387] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setBeta10y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getBeta10y();
            }
        };
        qps[392] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformanceAlltime(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformanceAlltime();
            }
        };
        qps[405] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setReferencePrice(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getReferencePrice();
            }
        };
        qps[407] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPari(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPari();
            }
        };
        qps[414] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int localeIndex, String value) {
                ratios.setVwdMarket(value);
            }

            public String getString(QuoteRatiosGNS ratios, int localeIndex) {
                return ratios.getVwdMarket();
            }
        };
        qps[417] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setClosePreviousYear(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getClosePreviousYear();
            }
        };
        qps[418] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setClosePreviousYearDate(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getClosePreviousYearDate();
            }
        };
        qps[419] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setClosePreviousMonth(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getClosePreviousMonth();
            }
        };
        qps[420] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setClosePreviousMonthDate(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getClosePreviousMonthDate();
            }
        };
        qps[421] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformanceCurrentYear(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformanceCurrentYear();
            }
        };
        qps[422] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformanceCurrentMonth(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformanceCurrentMonth();
            }
        };
        qps[423] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setHighPreviousYear(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getHighPreviousYear();
            }
        };
        qps[424] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setHighPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getHighPreviousYearDate();
            }
        };
        qps[425] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setLowPreviousYear(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getLowPreviousYear();
            }
        };
        qps[426] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setLowPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getLowPreviousYearDate();
            }
        };
        qps[428] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setChangeNetCurrentYear(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getChangeNetCurrentYear();
            }
        };
        qps[429] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setChangeNetCurrentMonth(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getChangeNetCurrentMonth();
            }
        };
        qps[441] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAlpha1w(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAlpha1w();
            }
        };
        qps[442] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAlpha3m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAlpha3m();
            }
        };
        qps[443] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAlpha6m(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAlpha6m();
            }
        };
        qps[444] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAlpha3y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAlpha3y();
            }
        };
        qps[445] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAlpha5y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAlpha5y();
            }
        };
        qps[446] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAlpha10y(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAlpha10y();
            }
        };
        qps[491] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setWmDividendYield(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getWmDividendYield();
            }
        };
        qps[531] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setQid(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getQid();
            }
        };
        qps[532] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, int value) {
                ratios.setDateFirstPrice(value);
            }

            public int getInt(QuoteRatiosGNS ratios) {
                return ratios.getDateFirstPrice();
            }
        };
        qps[648] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setMaSimple38(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getMaSimple38();
            }
        };
        qps[649] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setMaSimple90(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getMaSimple90();
            }
        };
        qps[650] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setMaSimple200(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getMaSimple200();
            }
        };
        qps[654] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setMarketCapitalizationPreviousDay(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getMarketCapitalizationPreviousDay();
            }
        };
        qps[692] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setTurnoverDay(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getTurnoverDay();
            }
        };
        qps[721] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setPerformancetobenchmarkcurrentyear(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getPerformancetobenchmarkcurrentyear();
            }
        };
        qps[722] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setCorrelationcurrentyear(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getCorrelationcurrentyear();
            }
        };
        qps[748] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setMarketcapitalizationUSD(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getMarketcapitalizationUSD();
            }
        };
        qps[749] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setMarketcapitalizationEUR(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getMarketcapitalizationEUR();
            }
        };
        qps[760] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setVolatilityAlltime(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getVolatilityAlltime();
            }
        };
        qps[761] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setBetaCurrentYear(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getBetaCurrentYear();
            }
        };
        qps[762] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setBetaAlltime(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getBetaAlltime();
            }
        };
        qps[765] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAlphaCurrentYear(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAlphaCurrentYear();
            }
        };
        qps[766] = new PropertySupport<QuoteRatiosGNS>() {
            public void set(QuoteRatiosGNS ratios, long value) {
                ratios.setAlphaAlltime(value);
            }

            public long getLong(QuoteRatiosGNS ratios) {
                return ratios.getAlphaAlltime();
            }
        };

    }
}