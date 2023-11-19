package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class RatiosPropertySupportWNT {
    private static PropertySupport[] qps = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    private static PropertySupport[] ips = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    public static PropertySupport forQuote(int fieldid) {
        return qps[fieldid];
    }

    public static PropertySupport forInstrument(int fieldid) {
        return ips[fieldid];
    }

    static {

        ips[3] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, long value) {
                ratios.setSubscriptionRatio(value);
            }

            public long getLong(InstrumentRatiosWNT ratios) {
                return ratios.getSubscriptionRatio();
            }
        };
        ips[11] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int localeIndex, String value) {
                ratios.setCurrencyStrike(value);
            }

            public String getString(InstrumentRatiosWNT ratios, int localeIndex) {
                return ratios.getCurrencyStrike();
            }
        };
        ips[14] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int value) {
                ratios.setExpires(value);
            }

            public int getInt(InstrumentRatiosWNT ratios) {
                return ratios.getExpires();
            }
        };
        ips[139] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int localeIndex, String value) {
                ratios.setWkn(value);
            }

            public String getString(InstrumentRatiosWNT ratios, int localeIndex) {
                return ratios.getWkn();
            }
        };
        ips[140] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int localeIndex, String value) {
                ratios.setIsin(value);
            }

            public String getString(InstrumentRatiosWNT ratios, int localeIndex) {
                return ratios.getIsin();
            }
        };
        ips[141] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int localeIndex, String value) {
                ratios.setName(value);
            }

            public String getString(InstrumentRatiosWNT ratios, int localeIndex) {
                return ratios.getName();
            }
        };
        ips[148] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, long value) {
                ratios.setStrikePrice(value);
            }

            public long getLong(InstrumentRatiosWNT ratios) {
                return ratios.getStrikePrice();
            }
        };
        ips[149] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int localeIndex, String value) {
                ratios.setOsType(value);
            }

            public String getString(InstrumentRatiosWNT ratios, int localeIndex) {
                return ratios.getOsType();
            }
        };
        ips[150] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, boolean value) {
                ratios.setIsAmerican(value);
            }

            public boolean getBoolean(InstrumentRatiosWNT ratios) {
                return ratios.getIsAmerican();
            }
        };
        ips[151] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int localeIndex, String value) {
                ratios.setIssuerName(value);
            }

            public String getString(InstrumentRatiosWNT ratios, int localeIndex) {
                return ratios.getIssuerName();
            }
        };
        ips[152] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int localeIndex, String value) {
                ratios.setUnderlyingWkn(value);
            }

            public String getString(InstrumentRatiosWNT ratios, int localeIndex) {
                return ratios.getUnderlyingWkn();
            }
        };
        ips[153] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int localeIndex, String value) {
                ratios.setUnderlyingIsin(value);
            }

            public String getString(InstrumentRatiosWNT ratios, int localeIndex) {
                return ratios.getUnderlyingIsin();
            }
        };
        ips[154] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int localeIndex, String value) {
                ratios.setUnderlyingName(value);
            }

            public String getString(InstrumentRatiosWNT ratios, int localeIndex) {
                return ratios.getUnderlyingName();
            }
        };
        ips[166] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int localeIndex, String value) {
                ratios.setUnderlyingType(value);
            }

            public String getString(InstrumentRatiosWNT ratios, int localeIndex) {
                return ratios.getUnderlyingType();
            }
        };
        ips[202] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int value) {
                ratios.setIssueDate(value);
            }

            public int getInt(InstrumentRatiosWNT ratios) {
                return ratios.getIssueDate();
            }
        };
        ips[317] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, long value) {
                ratios.setUnderlyingIid(value);
            }

            public long getLong(InstrumentRatiosWNT ratios) {
                return ratios.getUnderlyingIid();
            }
        };
        ips[408] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, boolean value) {
                ratios.setVrIssuer(value);
            }

            public boolean getBoolean(InstrumentRatiosWNT ratios) {
                return ratios.getVrIssuer();
            }
        };
        ips[409] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, boolean value) {
                ratios.setWmVrIssuer(value);
            }

            public boolean getBoolean(InstrumentRatiosWNT ratios) {
                return ratios.getWmVrIssuer();
            }
        };
        ips[535] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int value) {
                ratios.setEdgRatingDate(value);
            }

            public int getInt(InstrumentRatiosWNT ratios) {
                return ratios.getEdgRatingDate();
            }
        };
        ips[536] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, long value) {
                ratios.setEdgScore1(value);
            }

            public long getLong(InstrumentRatiosWNT ratios) {
                return ratios.getEdgScore1();
            }
        };
        ips[537] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, long value) {
                ratios.setEdgScore2(value);
            }

            public long getLong(InstrumentRatiosWNT ratios) {
                return ratios.getEdgScore2();
            }
        };
        ips[538] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, long value) {
                ratios.setEdgScore3(value);
            }

            public long getLong(InstrumentRatiosWNT ratios) {
                return ratios.getEdgScore3();
            }
        };
        ips[539] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, long value) {
                ratios.setEdgScore4(value);
            }

            public long getLong(InstrumentRatiosWNT ratios) {
                return ratios.getEdgScore4();
            }
        };
        ips[540] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, long value) {
                ratios.setEdgScore5(value);
            }

            public long getLong(InstrumentRatiosWNT ratios) {
                return ratios.getEdgScore5();
            }
        };
        ips[541] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, long value) {
                ratios.setEdgTopScore(value);
            }

            public long getLong(InstrumentRatiosWNT ratios) {
                return ratios.getEdgTopScore();
            }
        };
        ips[542] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, long value) {
                ratios.setEdgTopClass(value);
            }

            public long getLong(InstrumentRatiosWNT ratios) {
                return ratios.getEdgTopClass();
            }
        };
        ips[563] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int localeIndex, String value) {
                ratios.setSmfLeverageType(value);
            }

            public String getString(InstrumentRatiosWNT ratios, int localeIndex) {
                return ratios.getSmfLeverageType();
            }
        };
        ips[565] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, boolean value) {
                ratios.setSmfIsAmerican(value);
            }

            public boolean getBoolean(InstrumentRatiosWNT ratios) {
                return ratios.getSmfIsAmerican();
            }
        };
        ips[566] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int value) {
                ratios.setSmfExpires(value);
            }

            public int getInt(InstrumentRatiosWNT ratios) {
                return ratios.getSmfExpires();
            }
        };
        ips[568] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, long value) {
                ratios.setSmfSubscriptionRatio(value);
            }

            public long getLong(InstrumentRatiosWNT ratios) {
                return ratios.getSmfSubscriptionRatio();
            }
        };
        ips[584] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, long value) {
                ratios.setSmfStrike(value);
            }

            public long getLong(InstrumentRatiosWNT ratios) {
                return ratios.getSmfStrike();
            }
        };
        ips[647] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, boolean value) {
                ratios.setVwdStaticDataAvailable(value);
            }

            public boolean getBoolean(InstrumentRatiosWNT ratios) {
                return ratios.getVwdStaticDataAvailable();
            }
        };
        ips[750] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int localeIndex, String value) {
                ratios.setSedexIssuerName(value);
            }

            public String getString(InstrumentRatiosWNT ratios, int localeIndex) {
                return ratios.getSedexIssuerName();
            }
        };
        ips[751] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, long value) {
                ratios.setSedexStrike(value);
            }

            public long getLong(InstrumentRatiosWNT ratios) {
                return ratios.getSedexStrike();
            }
        };
        ips[752] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int value) {
                ratios.setSedexIssueDate(value);
            }

            public int getInt(InstrumentRatiosWNT ratios) {
                return ratios.getSedexIssueDate();
            }
        };
        ips[753] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int value) {
                ratios.setSedexExpires(value);
            }

            public int getInt(InstrumentRatiosWNT ratios) {
                return ratios.getSedexExpires();
            }
        };
        ips[756] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, int localeIndex, String value) {
                ratios.setUnderlyingEurexTicker(value);
            }

            public String getString(InstrumentRatiosWNT ratios, int localeIndex) {
                return ratios.getUnderlyingEurexTicker();
            }
        };
        ips[769] = new PropertySupport<InstrumentRatiosWNT>() {
            public void set(InstrumentRatiosWNT ratios, boolean value) {
                ratios.setWmNotActive(value);
            }

            public boolean getBoolean(InstrumentRatiosWNT ratios) {
                return ratios.getWmNotActive();
            }
        };

        qps[1] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setMostRecentUpdateTimestamp(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getMostRecentUpdateTimestamp();
            }
        };
        qps[4] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setVolatility1m(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getVolatility1m();
            }
        };
        qps[5] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setPerformance1m(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getPerformance1m();
            }
        };
        qps[6] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setSpread(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getSpread();
            }
        };
        qps[10] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setReferenceTimestamp(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getReferenceTimestamp();
            }
        };
        qps[15] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setSpreadRelative(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getSpreadRelative();
            }
        };
        qps[16] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setBid(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getBid();
            }
        };
        qps[17] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setAsk(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getAsk();
            }
        };
        qps[18] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setLastPrice(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getLastPrice();
            }
        };
        qps[19] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setPreviousClose(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getPreviousClose();
            }
        };
        qps[28] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setBidVolume(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getBidVolume();
            }
        };
        qps[29] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setAskVolume(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getAskVolume();
            }
        };
        qps[34] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int value) {
                ratios.setBidAskDate(value);
            }

            public int getInt(QuoteRatiosWNT ratios) {
                return ratios.getBidAskDate();
            }
        };
        qps[35] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int value) {
                ratios.setBidAskTime(value);
            }

            public int getInt(QuoteRatiosWNT ratios) {
                return ratios.getBidAskTime();
            }
        };
        qps[36] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setTradeVolume(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getTradeVolume();
            }
        };
        qps[37] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int value) {
                ratios.setLastDate(value);
            }

            public int getInt(QuoteRatiosWNT ratios) {
                return ratios.getLastDate();
            }
        };
        qps[38] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int value) {
                ratios.setLastTime(value);
            }

            public int getInt(QuoteRatiosWNT ratios) {
                return ratios.getLastTime();
            }
        };
        qps[39] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setHigh(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getHigh();
            }
        };
        qps[40] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setLow(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getLow();
            }
        };
        qps[41] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setHighYear(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getHighYear();
            }
        };
        qps[42] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setLowYear(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getLowYear();
            }
        };
        qps[43] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setTotalVolume(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getTotalVolume();
            }
        };
        qps[44] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int value) {
                ratios.setPreviousDate(value);
            }

            public int getInt(QuoteRatiosWNT ratios) {
                return ratios.getPreviousDate();
            }
        };
        qps[45] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setOpen(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getOpen();
            }
        };
        qps[48] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setUnderlyingLastPrice(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getUnderlyingLastPrice();
            }
        };
        qps[49] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setUnderlyingPreviousClose(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getUnderlyingPreviousClose();
            }
        };
        qps[52] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setUnderlyingTradeVolume(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getUnderlyingTradeVolume();
            }
        };
        qps[53] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int value) {
                ratios.setUnderlyingLastDate(value);
            }

            public int getInt(QuoteRatiosWNT ratios) {
                return ratios.getUnderlyingLastDate();
            }
        };
        qps[54] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int value) {
                ratios.setUnderlyingLastTime(value);
            }

            public int getInt(QuoteRatiosWNT ratios) {
                return ratios.getUnderlyingLastTime();
            }
        };
        qps[57] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setUnderlyingTotalVolume(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getUnderlyingTotalVolume();
            }
        };
        qps[58] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int value) {
                ratios.setUnderlyingPreviousDate(value);
            }

            public int getInt(QuoteRatiosWNT ratios) {
                return ratios.getUnderlyingPreviousDate();
            }
        };
        qps[60] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setIssuePrice(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getIssuePrice();
            }
        };
        qps[67] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setVolatility3m(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getVolatility3m();
            }
        };
        qps[68] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setPerformance3m(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getPerformance3m();
            }
        };
        qps[69] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setPerformance1y(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getPerformance1y();
            }
        };
        qps[72] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setVolatility10y(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getVolatility10y();
            }
        };
        qps[73] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setVolatility1y(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getVolatility1y();
            }
        };
        qps[84] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int value) {
                ratios.setDateoflast(value);
            }

            public int getInt(QuoteRatiosWNT ratios) {
                return ratios.getDateoflast();
            }
        };
        qps[85] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setIntrinsicvalue(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getIntrinsicvalue();
            }
        };
        qps[86] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setIntrinsicvaluepercent(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getIntrinsicvaluepercent();
            }
        };
        qps[87] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setExtrinsicvalue(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getExtrinsicvalue();
            }
        };
        qps[88] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setExtrinsicvaluepercent(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getExtrinsicvaluepercent();
            }
        };
        qps[89] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setOptionprice(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getOptionprice();
            }
        };
        qps[90] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setOptionpriceperyear(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getOptionpriceperyear();
            }
        };
        qps[91] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setBreakeven(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getBreakeven();
            }
        };
        qps[92] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setLeverage(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getLeverage();
            }
        };
        qps[94] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setFairvalue(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getFairvalue();
            }
        };
        qps[95] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setDelta(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getDelta();
            }
        };
        qps[96] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setOmega(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getOmega();
            }
        };
        qps[97] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setGamma(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getGamma();
            }
        };
        qps[98] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setVega(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getVega();
            }
        };
        qps[99] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setTheta(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getTheta();
            }
        };
        qps[100] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setRho(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getRho();
            }
        };
        qps[101] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setImpliedvolatility(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getImpliedvolatility();
            }
        };
        qps[105] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setPerformance10y(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getPerformance10y();
            }
        };
        qps[114] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setLast(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getLast();
            }
        };
        qps[117] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setPerformance1d(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getPerformance1d();
            }
        };
        qps[127] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setPerformance6m(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getPerformance6m();
            }
        };
        qps[155] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int localeIndex, String value) {
                ratios.setVwdCode(value);
            }

            public String getString(QuoteRatiosWNT ratios, int localeIndex) {
                return ratios.getVwdCode();
            }
        };
        qps[156] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int localeIndex, String value) {
                ratios.setMmwkn(value);
            }

            public String getString(QuoteRatiosWNT ratios, int localeIndex) {
                return ratios.getMmwkn();
            }
        };
        qps[157] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int localeIndex, String value) {
                ratios.setCurrency(value);
            }

            public String getString(QuoteRatiosWNT ratios, int localeIndex) {
                return ratios.getCurrency();
            }
        };
        qps[240] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setChangeNet(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getChangeNet();
            }
        };
        qps[241] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setChangePercent(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getChangePercent();
            }
        };
        qps[300] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setPerformance1w(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getPerformance1w();
            }
        };
        qps[323] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setAveragevolume1w(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getAveragevolume1w();
            }
        };
        qps[332] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int localeIndex, String value) {
                ratios.setMarketmanagerName(value);
            }

            public String getString(QuoteRatiosWNT ratios, int localeIndex) {
                return ratios.getMarketmanagerName();
            }
        };
        qps[342] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setVolatilityCurrentYear(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getVolatilityCurrentYear();
            }
        };
        qps[392] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setPerformanceAlltime(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getPerformanceAlltime();
            }
        };
        qps[405] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setReferencePrice(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getReferencePrice();
            }
        };
        qps[414] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, int localeIndex, String value) {
                ratios.setVwdMarket(value);
            }

            public String getString(QuoteRatiosWNT ratios, int localeIndex) {
                return ratios.getVwdMarket();
            }
        };
        qps[421] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setPerformanceCurrentYear(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getPerformanceCurrentYear();
            }
        };
        qps[531] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setQid(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getQid();
            }
        };
        qps[760] = new PropertySupport<QuoteRatiosWNT>() {
            public void set(QuoteRatiosWNT ratios, long value) {
                ratios.setVolatilityAlltime(value);
            }

            public long getLong(QuoteRatiosWNT ratios) {
                return ratios.getVolatilityAlltime();
            }
        };

    }
}