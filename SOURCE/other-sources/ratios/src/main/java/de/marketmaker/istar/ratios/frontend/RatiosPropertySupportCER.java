package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class RatiosPropertySupportCER {
    private static PropertySupport[] qps = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    private static PropertySupport[] ips = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    public static PropertySupport forQuote(int fieldid) {
        return qps[fieldid];
    }

    public static PropertySupport forInstrument(int fieldid) {
        return ips[fieldid];
    }

    static {

        ips[3] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setSubscriptionRatio(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getSubscriptionRatio();
            }
        };
        ips[9] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setIssuerProductname(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getIssuerProductname();
            }
        };
        ips[11] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setCurrencyStrike(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getCurrencyStrike();
            }
        };
        ips[13] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setCap(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getCap();
            }
        };
        ips[14] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int value) {
                ratios.setExpires(value);
            }

            public int getInt(InstrumentRatiosCER ratios) {
                return ratios.getExpires();
            }
        };
        ips[139] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setWkn(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getWkn();
            }
        };
        ips[140] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setIsin(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getIsin();
            }
        };
        ips[141] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setName(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getName();
            }
        };
        ips[148] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setStrikePrice(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getStrikePrice();
            }
        };
        ips[149] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setOsType(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getOsType();
            }
        };
        ips[151] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setIssuerName(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getIssuerName();
            }
        };
        ips[152] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setUnderlyingWkn(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getUnderlyingWkn();
            }
        };
        ips[153] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setUnderlyingIsin(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getUnderlyingIsin();
            }
        };
        ips[154] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setUnderlyingName(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getUnderlyingName();
            }
        };
        ips[160] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setInterest(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getInterest();
            }
        };
        ips[166] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setUnderlyingType(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getUnderlyingType();
            }
        };
        ips[170] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, boolean value) {
                ratios.setGatrixxQuanto(value);
            }

            public boolean getBoolean(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxQuanto();
            }
        };
        ips[172] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setExerciseType(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getExerciseType();
            }
        };
        ips[185] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setIssueVolume(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getIssueVolume();
            }
        };
        ips[193] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setLowerKnock(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getLowerKnock();
            }
        };
        ips[194] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setUpperKnock(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getUpperKnock();
            }
        };
        ips[195] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setLowerRange(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getLowerRange();
            }
        };
        ips[196] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setUpperRange(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getUpperRange();
            }
        };
        ips[202] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int value) {
                ratios.setIssueDate(value);
            }

            public int getInt(InstrumentRatiosCER ratios) {
                return ratios.getIssueDate();
            }
        };
        ips[289] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setGatrixxType(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getGatrixxType();
            }
        };
        ips[290] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxStrikePrice(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxStrikePrice();
            }
        };
        ips[292] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxCoupon(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxCoupon();
            }
        };
        ips[293] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxCap(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxCap();
            }
        };
        ips[294] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxKnockin(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxKnockin();
            }
        };
        ips[295] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxBonuslevel(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxBonuslevel();
            }
        };
        ips[296] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxBarrier(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxBarrier();
            }
        };
        ips[297] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setGatrixxGuaranteeType(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getGatrixxGuaranteeType();
            }
        };
        ips[298] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setGatrixxLeverageType(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getGatrixxLeverageType();
            }
        };
        ips[317] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setUnderlyingIid(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getUnderlyingIid();
            }
        };
        ips[408] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, boolean value) {
                ratios.setVrIssuer(value);
            }

            public boolean getBoolean(InstrumentRatiosCER ratios) {
                return ratios.getVrIssuer();
            }
        };
        ips[409] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, boolean value) {
                ratios.setWmVrIssuer(value);
            }

            public boolean getBoolean(InstrumentRatiosCER ratios) {
                return ratios.getWmVrIssuer();
            }
        };
        ips[411] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setDzCategory(localeIndex, value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getDzCategory(localeIndex);
            }
        };
        ips[412] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxprotectlevel(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxprotectlevel();
            }
        };
        ips[413] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setGatrixxtypeftreff(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getGatrixxtypeftreff();
            }
        };
        ips[493] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxParticipationlevel(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxParticipationlevel();
            }
        };
        ips[494] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setGatrixxTypename(localeIndex, value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getGatrixxTypename(localeIndex);
            }
        };
        ips[495] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setGatrixxMultiassetName(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getGatrixxMultiassetName();
            }
        };
        ips[503] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxStoploss(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxStoploss();
            }
        };
        ips[504] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setDzWgzListid(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getDzWgzListid();
            }
        };
        ips[530] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, boolean value) {
                ratios.setGatrixxIsknockout(value);
            }

            public boolean getBoolean(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxIsknockout();
            }
        };
        ips[535] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int value) {
                ratios.setEdgRatingDate(value);
            }

            public int getInt(InstrumentRatiosCER ratios) {
                return ratios.getEdgRatingDate();
            }
        };
        ips[536] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setEdgScore1(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getEdgScore1();
            }
        };
        ips[537] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setEdgScore2(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getEdgScore2();
            }
        };
        ips[538] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setEdgScore3(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getEdgScore3();
            }
        };
        ips[539] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setEdgScore4(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getEdgScore4();
            }
        };
        ips[540] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setEdgScore5(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getEdgScore5();
            }
        };
        ips[541] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setEdgTopScore(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getEdgTopScore();
            }
        };
        ips[542] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setEdgTopClass(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getEdgTopClass();
            }
        };
        ips[543] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxParticipationFactor(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxParticipationFactor();
            }
        };
        ips[544] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int value) {
                ratios.setGatrixxKnockindate(value);
            }

            public int getInt(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxKnockindate();
            }
        };
        ips[546] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxStartvalue(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxStartvalue();
            }
        };
        ips[547] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxStopvalue(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxStopvalue();
            }
        };
        ips[548] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxRefundMaximum(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxRefundMaximum();
            }
        };
        ips[562] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setSmfIssuerProductname(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getSmfIssuerProductname();
            }
        };
        ips[563] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setSmfLeverageType(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getSmfLeverageType();
            }
        };
        ips[564] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, boolean value) {
                ratios.setSmfIsQuanto(value);
            }

            public boolean getBoolean(InstrumentRatiosCER ratios) {
                return ratios.getSmfIsQuanto();
            }
        };
        ips[566] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int value) {
                ratios.setSmfExpires(value);
            }

            public int getInt(InstrumentRatiosCER ratios) {
                return ratios.getSmfExpires();
            }
        };
        ips[567] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setSmfParticipationrate(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getSmfParticipationrate();
            }
        };
        ips[568] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setSmfSubscriptionRatio(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getSmfSubscriptionRatio();
            }
        };
        ips[569] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setSmfCoupon(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getSmfCoupon();
            }
        };
        ips[570] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setSmfMultiassetName(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getSmfMultiassetName();
            }
        };
        ips[582] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setSmfCertificateType(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getSmfCertificateType();
            }
        };
        ips[623] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setType(localeIndex, value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getType(localeIndex);
            }
        };
        ips[624] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setTypeKey(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getTypeKey();
            }
        };
        ips[625] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setSubtype(localeIndex, value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getSubtype(localeIndex);
            }
        };
        ips[626] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setTypeDZ(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getTypeDZ();
            }
        };
        ips[627] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setTypeKeyDZ(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getTypeKeyDZ();
            }
        };
        ips[628] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setSubtypeDZ(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getSubtypeDZ();
            }
        };
        ips[629] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setTypeWGZ(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getTypeWGZ();
            }
        };
        ips[630] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setTypeKeyWGZ(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getTypeKeyWGZ();
            }
        };
        ips[631] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setSubtypeWGZ(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getSubtypeWGZ();
            }
        };
        ips[632] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setGatrixxBonusbufferRelative(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxBonusbufferRelative();
            }
        };
        ips[647] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, boolean value) {
                ratios.setVwdStaticDataAvailable(value);
            }

            public boolean getBoolean(InstrumentRatiosCER ratios) {
                return ratios.getVwdStaticDataAvailable();
            }
        };
        ips[653] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, boolean value) {
                ratios.setDzPib(value);
            }

            public boolean getBoolean(InstrumentRatiosCER ratios) {
                return ratios.getDzPib();
            }
        };
        ips[655] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, boolean value) {
                ratios.setDzIsLeverageProduct(value);
            }

            public boolean getBoolean(InstrumentRatiosCER ratios) {
                return ratios.getDzIsLeverageProduct();
            }
        };
        ips[656] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setSubtypeKey(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getSubtypeKey();
            }
        };
        ips[657] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setSubtypeKeyDZ(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getSubtypeKeyDZ();
            }
        };
        ips[658] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setSubtypeKeyWGZ(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getSubtypeKeyWGZ();
            }
        };
        ips[659] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, boolean value) {
                ratios.setGatrixxIsEndless(value);
            }

            public boolean getBoolean(InstrumentRatiosCER ratios) {
                return ratios.getGatrixxIsEndless();
            }
        };
        ips[750] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setSedexIssuerName(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getSedexIssuerName();
            }
        };
        ips[751] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, long value) {
                ratios.setSedexStrike(value);
            }

            public long getLong(InstrumentRatiosCER ratios) {
                return ratios.getSedexStrike();
            }
        };
        ips[752] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int value) {
                ratios.setSedexIssueDate(value);
            }

            public int getInt(InstrumentRatiosCER ratios) {
                return ratios.getSedexIssueDate();
            }
        };
        ips[753] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int value) {
                ratios.setSedexExpires(value);
            }

            public int getInt(InstrumentRatiosCER ratios) {
                return ratios.getSedexExpires();
            }
        };
        ips[754] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setTypeSEDEX(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getTypeSEDEX();
            }
        };
        ips[755] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setTypeKeySEDEX(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getTypeKeySEDEX();
            }
        };
        ips[756] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, int localeIndex, String value) {
                ratios.setUnderlyingEurexTicker(value);
            }

            public String getString(InstrumentRatiosCER ratios, int localeIndex) {
                return ratios.getUnderlyingEurexTicker();
            }
        };
        ips[769] = new PropertySupport<InstrumentRatiosCER>() {
            public void set(InstrumentRatiosCER ratios, boolean value) {
                ratios.setWmNotActive(value);
            }

            public boolean getBoolean(InstrumentRatiosCER ratios) {
                return ratios.getWmNotActive();
            }
        };

        qps[1] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMostRecentUpdateTimestamp(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMostRecentUpdateTimestamp();
            }
        };
        qps[4] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setVolatility1m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getVolatility1m();
            }
        };
        qps[5] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setPerformance1m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getPerformance1m();
            }
        };
        qps[6] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setSpread(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getSpread();
            }
        };
        qps[10] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setReferenceTimestamp(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getReferenceTimestamp();
            }
        };
        qps[15] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setSpreadRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getSpreadRelative();
            }
        };
        qps[16] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setBid(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getBid();
            }
        };
        qps[17] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAsk(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAsk();
            }
        };
        qps[18] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setLastPrice(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getLastPrice();
            }
        };
        qps[19] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setPreviousClose(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getPreviousClose();
            }
        };
        qps[22] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setDiscountPrice(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getDiscountPrice();
            }
        };
        qps[23] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setDiscountPriceRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getDiscountPriceRelative();
            }
        };
        qps[24] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setDiscountPriceRelativePerYear(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getDiscountPriceRelativePerYear();
            }
        };
        qps[25] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setUnchangedEarning(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getUnchangedEarning();
            }
        };
        qps[26] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setUnchangedEarningRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getUnchangedEarningRelative();
            }
        };
        qps[27] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setUnchangedEarningRelativePerYear(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getUnchangedEarningRelativePerYear();
            }
        };
        qps[28] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setBidVolume(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getBidVolume();
            }
        };
        qps[29] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAskVolume(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAskVolume();
            }
        };
        qps[30] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setCapToUnderlying(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getCapToUnderlying();
            }
        };
        qps[31] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setCapToUnderlyingRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getCapToUnderlyingRelative();
            }
        };
        qps[32] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setUnderlyingToCap(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getUnderlyingToCap();
            }
        };
        qps[33] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setUnderlyingToCapRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getUnderlyingToCapRelative();
            }
        };
        qps[34] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int value) {
                ratios.setBidAskDate(value);
            }

            public int getInt(QuoteRatiosCER ratios) {
                return ratios.getBidAskDate();
            }
        };
        qps[35] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int value) {
                ratios.setBidAskTime(value);
            }

            public int getInt(QuoteRatiosCER ratios) {
                return ratios.getBidAskTime();
            }
        };
        qps[36] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setTradeVolume(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getTradeVolume();
            }
        };
        qps[37] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int value) {
                ratios.setLastDate(value);
            }

            public int getInt(QuoteRatiosCER ratios) {
                return ratios.getLastDate();
            }
        };
        qps[38] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int value) {
                ratios.setLastTime(value);
            }

            public int getInt(QuoteRatiosCER ratios) {
                return ratios.getLastTime();
            }
        };
        qps[39] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setHigh(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getHigh();
            }
        };
        qps[40] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setLow(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getLow();
            }
        };
        qps[41] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setHighYear(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getHighYear();
            }
        };
        qps[42] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setLowYear(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getLowYear();
            }
        };
        qps[43] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setTotalVolume(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getTotalVolume();
            }
        };
        qps[44] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int value) {
                ratios.setPreviousDate(value);
            }

            public int getInt(QuoteRatiosCER ratios) {
                return ratios.getPreviousDate();
            }
        };
        qps[45] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setOpen(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getOpen();
            }
        };
        qps[48] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setUnderlyingLastPrice(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getUnderlyingLastPrice();
            }
        };
        qps[49] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setUnderlyingPreviousClose(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getUnderlyingPreviousClose();
            }
        };
        qps[52] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setUnderlyingTradeVolume(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getUnderlyingTradeVolume();
            }
        };
        qps[53] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int value) {
                ratios.setUnderlyingLastDate(value);
            }

            public int getInt(QuoteRatiosCER ratios) {
                return ratios.getUnderlyingLastDate();
            }
        };
        qps[54] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int value) {
                ratios.setUnderlyingLastTime(value);
            }

            public int getInt(QuoteRatiosCER ratios) {
                return ratios.getUnderlyingLastTime();
            }
        };
        qps[57] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setUnderlyingTotalVolume(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getUnderlyingTotalVolume();
            }
        };
        qps[58] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int value) {
                ratios.setUnderlyingPreviousDate(value);
            }

            public int getInt(QuoteRatiosCER ratios) {
                return ratios.getUnderlyingPreviousDate();
            }
        };
        qps[60] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setIssuePrice(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getIssuePrice();
            }
        };
        qps[67] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setVolatility3m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getVolatility3m();
            }
        };
        qps[68] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setPerformance3m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getPerformance3m();
            }
        };
        qps[69] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setPerformance1y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getPerformance1y();
            }
        };
        qps[70] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setCapLevel(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getCapLevel();
            }
        };
        qps[71] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setYieldRelativePerYear(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getYieldRelativePerYear();
            }
        };
        qps[72] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setVolatility10y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getVolatility10y();
            }
        };
        qps[73] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setVolatility1y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getVolatility1y();
            }
        };
        qps[74] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setSharperatio1y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getSharperatio1y();
            }
        };
        qps[75] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setSharperatio3y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getSharperatio3y();
            }
        };
        qps[92] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setLeverage(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getLeverage();
            }
        };
        qps[105] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setPerformance10y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getPerformance10y();
            }
        };
        qps[117] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setPerformance1d(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getPerformance1d();
            }
        };
        qps[122] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragevolume1m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragevolume1m();
            }
        };
        qps[123] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragevolume1y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragevolume1y();
            }
        };
        qps[127] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setPerformance6m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getPerformance6m();
            }
        };
        qps[128] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setPerformance3y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getPerformance3y();
            }
        };
        qps[129] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setPerformance5y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getPerformance5y();
            }
        };
        qps[130] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setVolatility6m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getVolatility6m();
            }
        };
        qps[131] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setVolatility5y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getVolatility5y();
            }
        };
        qps[132] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setSharperatio5y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getSharperatio5y();
            }
        };
        qps[133] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setSharperatio1m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getSharperatio1m();
            }
        };
        qps[134] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setSharperatio3m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getSharperatio3m();
            }
        };
        qps[135] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setSharperatio6m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getSharperatio6m();
            }
        };
        qps[137] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setUnderlyinglow1y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getUnderlyinglow1y();
            }
        };
        qps[138] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setUnderlyinghigh1y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getUnderlyinghigh1y();
            }
        };
        qps[155] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int localeIndex, String value) {
                ratios.setVwdCode(value);
            }

            public String getString(QuoteRatiosCER ratios, int localeIndex) {
                return ratios.getVwdCode();
            }
        };
        qps[156] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int localeIndex, String value) {
                ratios.setMmwkn(value);
            }

            public String getString(QuoteRatiosCER ratios, int localeIndex) {
                return ratios.getMmwkn();
            }
        };
        qps[157] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int localeIndex, String value) {
                ratios.setCurrency(value);
            }

            public String getString(QuoteRatiosCER ratios, int localeIndex) {
                return ratios.getCurrency();
            }
        };
        qps[167] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setHighAlltime(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getHighAlltime();
            }
        };
        qps[168] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setLowAlltime(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getLowAlltime();
            }
        };
        qps[169] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int value) {
                ratios.setHighAlltimeDate(value);
            }

            public int getInt(QuoteRatiosCER ratios) {
                return ratios.getHighAlltimeDate();
            }
        };
        qps[171] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int value) {
                ratios.setLowAlltimeDate(value);
            }

            public int getInt(QuoteRatiosCER ratios) {
                return ratios.getLowAlltimeDate();
            }
        };
        qps[177] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int value) {
                ratios.setHigh1yDate(value);
            }

            public int getInt(QuoteRatiosCER ratios) {
                return ratios.getHigh1yDate();
            }
        };
        qps[178] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int value) {
                ratios.setLow1yDate(value);
            }

            public int getInt(QuoteRatiosCER ratios) {
                return ratios.getLow1yDate();
            }
        };
        qps[179] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragePrice1w(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragePrice1w();
            }
        };
        qps[180] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragePrice1m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragePrice1m();
            }
        };
        qps[181] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragePrice3m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragePrice3m();
            }
        };
        qps[182] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragePrice6m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragePrice6m();
            }
        };
        qps[183] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragePrice1y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragePrice1y();
            }
        };
        qps[184] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragePrice3y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragePrice3y();
            }
        };
        qps[186] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragePrice5y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragePrice5y();
            }
        };
        qps[187] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragePrice10y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragePrice10y();
            }
        };
        qps[240] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setChangeNet(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getChangeNet();
            }
        };
        qps[241] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setChangePercent(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getChangePercent();
            }
        };
        qps[299] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setVolatility3y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getVolatility3y();
            }
        };
        qps[300] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setPerformance1w(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getPerformance1w();
            }
        };
        qps[319] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setHigh1y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getHigh1y();
            }
        };
        qps[320] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setLow1y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getLow1y();
            }
        };
        qps[323] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragevolume1w(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragevolume1w();
            }
        };
        qps[324] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragevolume3m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragevolume3m();
            }
        };
        qps[325] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragevolume6m(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragevolume6m();
            }
        };
        qps[326] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragevolume3y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragevolume3y();
            }
        };
        qps[327] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragevolume5y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragevolume5y();
            }
        };
        qps[329] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setAveragevolume10y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getAveragevolume10y();
            }
        };
        qps[332] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int localeIndex, String value) {
                ratios.setMarketmanagerName(value);
            }

            public String getString(QuoteRatiosCER ratios, int localeIndex) {
                return ratios.getMarketmanagerName();
            }
        };
        qps[342] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setVolatilityCurrentYear(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getVolatilityCurrentYear();
            }
        };
        qps[359] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsbrokenperiodinterest(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsbrokenperiodinterest();
            }
        };
        qps[392] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setPerformanceAlltime(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getPerformanceAlltime();
            }
        };
        qps[394] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setSharperatio1w(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getSharperatio1w();
            }
        };
        qps[395] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setSharperatio10y(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getSharperatio10y();
            }
        };
        qps[405] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setReferencePrice(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getReferencePrice();
            }
        };
        qps[414] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int localeIndex, String value) {
                ratios.setVwdMarket(value);
            }

            public String getString(QuoteRatiosCER ratios, int localeIndex) {
                return ratios.getVwdMarket();
            }
        };
        qps[421] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setPerformanceCurrentYear(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getPerformanceCurrentYear();
            }
        };
        qps[448] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsDiscount(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsDiscount();
            }
        };
        qps[449] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsDiscountRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsDiscountRelative();
            }
        };
        qps[450] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsYield(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsYield();
            }
        };
        qps[451] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsYieldRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsYieldRelative();
            }
        };
        qps[452] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsYieldRelativePerYear(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsYieldRelativePerYear();
            }
        };
        qps[453] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsGapCap(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsGapCap();
            }
        };
        qps[454] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsGapCapRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsGapCapRelative();
            }
        };
        qps[455] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsMaximumYield(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsMaximumYield();
            }
        };
        qps[456] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsMaximumYieldRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsMaximumYieldRelative();
            }
        };
        qps[457] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsMaximumYieldRelativePerYear(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsMaximumYieldRelativePerYear();
            }
        };
        qps[458] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsUnchangedYieldRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsUnchangedYieldRelative();
            }
        };
        qps[459] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsUnchangedYieldRelativePerYear(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsUnchangedYieldRelativePerYear();
            }
        };
        qps[460] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsGapBonusLevelRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsGapBonusLevelRelative();
            }
        };
        qps[461] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsGapBonusBufferRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsGapBonusBufferRelative();
            }
        };
        qps[462] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsAgioRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsAgioRelative();
            }
        };
        qps[463] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsAgioRelativePerYear(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsAgioRelativePerYear();
            }
        };
        qps[465] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsGapLowerBarrier(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsGapLowerBarrier();
            }
        };
        qps[466] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsGapLowerBarrierRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsGapLowerBarrierRelative();
            }
        };
        qps[467] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsGapUpperBarrier(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsGapUpperBarrier();
            }
        };
        qps[468] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsGapUpperBarrierRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsGapUpperBarrierRelative();
            }
        };
        qps[469] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsUnderlyingToCapRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsUnderlyingToCapRelative();
            }
        };
        qps[470] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsCapToUnderlyingRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsCapToUnderlyingRelative();
            }
        };
        qps[471] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsGapStrikeRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsGapStrikeRelative();
            }
        };
        qps[472] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsGapBonusLevel(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsGapBonusLevel();
            }
        };
        qps[473] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsPerformanceAlltime(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsPerformanceAlltime();
            }
        };
        qps[496] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsAgio(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsAgio();
            }
        };
        qps[497] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsUnchangedYield(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsUnchangedYield();
            }
        };
        qps[498] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsOutperformanceValue(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsOutperformanceValue();
            }
        };
        qps[499] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsGapStrike(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsGapStrike();
            }
        };
        qps[500] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsLeverage(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsLeverage();
            }
        };
        qps[501] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsGapBarrier(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsGapBarrier();
            }
        };
        qps[502] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsGapBarrierRelative(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsGapBarrierRelative();
            }
        };
        qps[531] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setQid(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getQid();
            }
        };
        qps[532] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, int value) {
                ratios.setDateFirstPrice(value);
            }

            public int getInt(QuoteRatiosCER ratios) {
                return ratios.getDateFirstPrice();
            }
        };
        qps[652] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setExternalReferenceTimestamp(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getExternalReferenceTimestamp();
            }
        };
        qps[728] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setMdpsDateBarrierReached(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getMdpsDateBarrierReached();
            }
        };
        qps[760] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setVolatilityAlltime(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getVolatilityAlltime();
            }
        };
        qps[763] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setSharperatioCurrentYear(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getSharperatioCurrentYear();
            }
        };
        qps[764] = new PropertySupport<QuoteRatiosCER>() {
            public void set(QuoteRatiosCER ratios, long value) {
                ratios.setSharperatioAlltime(value);
            }

            public long getLong(QuoteRatiosCER ratios) {
                return ratios.getSharperatioAlltime();
            }
        };

    }
}