package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class RatiosPropertySupportFND {
    private static PropertySupport[] qps = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    private static PropertySupport[] ips = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    public static PropertySupport forQuote(int fieldid) {
        return qps[fieldid];
    }

    public static PropertySupport forInstrument(int fieldid) {
        return ips[fieldid];
    }

    static {

        ips[14] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setExpires(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getExpires();
            }
        };
        ips[111] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setAttraxriskclass(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getAttraxriskclass();
            }
        };
        ips[139] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setWkn(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getWkn();
            }
        };
        ips[140] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setIsin(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getIsin();
            }
        };
        ips[141] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setName(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getName();
            }
        };
        ips[151] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setIssuerName(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getIssuerName();
            }
        };
        ips[162] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setCountry(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getCountry(localeIndex);
            }
        };
        ips[185] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setIssueVolume(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getIssueVolume();
            }
        };
        ips[202] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setIssueDate(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getIssueDate();
            }
        };
        ips[203] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setSector(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getSector(localeIndex);
            }
        };
        ips[215] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setFwwIssueSurcharge(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getFwwIssueSurcharge();
            }
        };
        ips[216] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setFwwManagementFee(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getFwwManagementFee();
            }
        };
        ips[217] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setFwwRiskclass(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getFwwRiskclass();
            }
        };
        ips[245] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setFwwAccountFee(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getFwwAccountFee();
            }
        };
        ips[250] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setFwwFundType(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getFwwFundType();
            }
        };
        ips[251] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setFwwSector(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getFwwSector();
            }
        };
        ips[313] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setFwwKag(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getFwwKag();
            }
        };
        ips[314] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setRatingFeri(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getRatingFeri();
            }
        };
        ips[343] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setMorningstars(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getMorningstars();
            }
        };
        ips[380] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, boolean value) {
                ratios.setIsEtf(value);
            }

            public boolean getBoolean(InstrumentRatiosFND ratios) {
                return ratios.getIsEtf();
            }
        };
        ips[408] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, boolean value) {
                ratios.setVrIssuer(value);
            }

            public boolean getBoolean(InstrumentRatiosFND ratios) {
                return ratios.getVrIssuer();
            }
        };
        ips[409] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, boolean value) {
                ratios.setWmVrIssuer(value);
            }

            public boolean getBoolean(InstrumentRatiosFND ratios) {
                return ratios.getWmVrIssuer();
            }
        };
        ips[415] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setBviKategorieGrob(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getBviKategorieGrob();
            }
        };
        ips[416] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, BitSet value) {
                ratios.setMarketAdmission(value);
            }

            public BitSet getBitSet(InstrumentRatiosFND ratios) {
                return ratios.getMarketAdmission();
            }
        };
        ips[430] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, boolean value) {
                ratios.setMsVrIssuer(value);
            }

            public boolean getBoolean(InstrumentRatiosFND ratios) {
                return ratios.getMsVrIssuer();
            }
        };
        ips[432] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setMsIssuername(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getMsIssuername();
            }
        };
        ips[433] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setMsCountry(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getMsCountry(localeIndex);
            }
        };
        ips[434] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setMsDistributionStrategy(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getMsDistributionStrategy(localeIndex);
            }
        };
        ips[435] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setMsFundVolume(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getMsFundVolume();
            }
        };
        ips[436] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setMsIssueDate(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getMsIssueDate();
            }
        };
        ips[437] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setMsIssueSurcharge(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getMsIssueSurcharge();
            }
        };
        ips[438] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setMsManagementfee(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getMsManagementfee();
            }
        };
        ips[439] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setMsAccountfee(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getMsAccountfee();
            }
        };
        ips[440] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setMsTer(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getMsTer();
            }
        };
        ips[447] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setMsInvestmentFocus(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getMsInvestmentFocus(localeIndex);
            }
        };
        ips[488] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setMsBenchmarkName(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getMsBenchmarkName();
            }
        };
        ips[505] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdBenchmarkQid(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdBenchmarkQid();
            }
        };
        ips[545] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setFwwTer(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getFwwTer();
            }
        };
        ips[561] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdbenlInvestmentFocus(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwdbenlInvestmentFocus(localeIndex);
            }
        };
        ips[571] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdbenlBenchmarkQid(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdbenlBenchmarkQid();
            }
        };
        ips[572] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdbenlFundType(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwdbenlFundType(localeIndex);
            }
        };
        ips[573] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdbenlIssuername(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwdbenlIssuername(localeIndex);
            }
        };
        ips[574] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdbenlCountry(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwdbenlCountry(localeIndex);
            }
        };
        ips[575] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdbenlDistStrategy(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwdbenlDistStrategy(localeIndex);
            }
        };
        ips[576] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdbenlFundVolume(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdbenlFundVolume();
            }
        };
        ips[577] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setVwdbenlIssueDate(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getVwdbenlIssueDate();
            }
        };
        ips[578] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdbenlIssueSurcharge(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdbenlIssueSurcharge();
            }
        };
        ips[579] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdbenlManagementfee(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdbenlManagementfee();
            }
        };
        ips[580] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdbenlTer(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdbenlTer();
            }
        };
        ips[581] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdbenlBenchmarkName(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwdbenlBenchmarkName();
            }
        };
        ips[583] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, BitSet value) {
                ratios.setMsMarketAdmission(value);
            }

            public BitSet getBitSet(InstrumentRatiosFND ratios) {
                return ratios.getMsMarketAdmission();
            }
        };
        ips[625] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setSubtype(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getSubtype(localeIndex);
            }
        };
        ips[638] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwditInvestmentFocus(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwditInvestmentFocus(localeIndex);
            }
        };
        ips[639] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwditFundType(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwditFundType(localeIndex);
            }
        };
        ips[640] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwditIssuername(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwditIssuername(localeIndex);
            }
        };
        ips[641] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwditBenchmarkName(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwditBenchmarkName();
            }
        };
        ips[642] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setVwditIssueDate(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getVwditIssueDate();
            }
        };
        ips[643] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwditTer(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwditTer();
            }
        };
        ips[644] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwditCfsRating(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwditCfsRating();
            }
        };
        ips[678] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setSsatFundType(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getSsatFundType(localeIndex);
            }
        };
        ips[679] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setSsatIssuername(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getSsatIssuername(localeIndex);
            }
        };
        ips[680] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setSsatInvestmentFocus(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getSsatInvestmentFocus(localeIndex);
            }
        };
        ips[681] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setSsatCountry(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getSsatCountry(localeIndex);
            }
        };
        ips[682] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setSsatDistributionStrategy(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getSsatDistributionStrategy(localeIndex);
            }
        };
        ips[683] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setSsatFundVolume(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getSsatFundVolume();
            }
        };
        ips[684] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setSsatIssueDate(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getSsatIssueDate();
            }
        };
        ips[685] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setSsatIssueSurcharge(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getSsatIssueSurcharge();
            }
        };
        ips[686] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setSsatManagementfee(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getSsatManagementfee();
            }
        };
        ips[687] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setSsatAccountfee(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getSsatAccountfee();
            }
        };
        ips[688] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setSsatTer(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getSsatTer();
            }
        };
        ips[689] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setSsatBenchmarkName(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getSsatBenchmarkName();
            }
        };
        ips[690] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setSsatBenchmarkQid(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getSsatBenchmarkQid();
            }
        };
        ips[691] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, BitSet value) {
                ratios.setSsatMarketAdmission(value);
            }

            public BitSet getBitSet(InstrumentRatiosFND ratios) {
                return ratios.getSsatMarketAdmission();
            }
        };
        ips[693] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setMsOngoingCharge(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getMsOngoingCharge();
            }
        };
        ips[694] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setMsOngoingChargeDate(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getMsOngoingChargeDate();
            }
        };
        ips[723] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdbenlsrrivalue(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdbenlsrrivalue();
            }
        };
        ips[724] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setVwdbenlsrrivaluedate(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getVwdbenlsrrivaluedate();
            }
        };
        ips[725] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdbenldiamondrating(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdbenldiamondrating();
            }
        };
        ips[726] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setVwdbenldiamondratingdate(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getVwdbenldiamondratingdate();
            }
        };
        ips[730] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setFidaFundType(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getFidaFundType(localeIndex);
            }
        };
        ips[731] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setFidaIssuername(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getFidaIssuername(localeIndex);
            }
        };
        ips[732] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setFidaInvestmentFocus(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getFidaInvestmentFocus(localeIndex);
            }
        };
        ips[733] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setFidaIssueDate(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getFidaIssueDate();
            }
        };
        ips[734] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setFidaTer(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getFidaTer();
            }
        };
        ips[735] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setFidaBenchmarkName(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getFidaBenchmarkName();
            }
        };
        ips[736] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setFidaCountry(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getFidaCountry(localeIndex);
            }
        };
        ips[737] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setFidaDistributionStrategy(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getFidaDistributionStrategy();
            }
        };
        ips[738] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setFidaFundVolume(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getFidaFundVolume();
            }
        };
        ips[739] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setFidaIssueSurcharge(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getFidaIssueSurcharge();
            }
        };
        ips[740] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setFidaManagementfee(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getFidaManagementfee();
            }
        };
        ips[746] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setFidaRating(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getFidaRating();
            }
        };
        ips[757] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setFidaPermissionType(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getFidaPermissionType();
            }
        };
        ips[759] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setMsBroadassetclass(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getMsBroadassetclass(localeIndex);
            }
        };
        ips[769] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, boolean value) {
                ratios.setWmNotActive(value);
            }

            public boolean getBoolean(InstrumentRatiosFND ratios) {
                return ratios.getWmNotActive();
            }
        };
        ips[773] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdFundType(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwdFundType(localeIndex);
            }
        };
        ips[774] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdIssuername(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwdIssuername(localeIndex);
            }
        };
        ips[775] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdCountry(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwdCountry(localeIndex);
            }
        };
        ips[776] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdDistributionStrategy(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwdDistributionStrategy(localeIndex);
            }
        };
        ips[777] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdFundVolume(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdFundVolume();
            }
        };
        ips[778] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setVwdIssueDate(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getVwdIssueDate();
            }
        };
        ips[779] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdIssueSurcharge(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdIssueSurcharge();
            }
        };
        ips[780] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdManagementFee(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdManagementFee();
            }
        };
        ips[781] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdTer(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdTer();
            }
        };
        ips[782] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdBenchmarkName(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwdBenchmarkName(localeIndex);
            }
        };
        ips[783] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdOnGoingCharge(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdOnGoingCharge();
            }
        };
        ips[784] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setVwdOngoingChargeDate(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getVwdOngoingChargeDate();
            }
        };
        ips[785] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, BitSet value) {
                ratios.setVwdMarketAdmission(value);
            }

            public BitSet getBitSet(InstrumentRatiosFND ratios) {
                return ratios.getVwdMarketAdmission();
            }
        };
        ips[787] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdInvestmentFocus(localeIndex, value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getVwdInvestmentFocus(localeIndex);
            }
        };
        ips[788] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdAccountfee(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdAccountfee();
            }
        };
        ips[792] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int localeIndex, String value) {
                ratios.setWmInvestmentAssetPoolClass(value);
            }

            public String getString(InstrumentRatiosFND ratios, int localeIndex) {
                return ratios.getWmInvestmentAssetPoolClass();
            }
        };
        ips[794] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, boolean value) {
                ratios.setVwdVrIssuer(value);
            }

            public boolean getBoolean(InstrumentRatiosFND ratios) {
                return ratios.getVwdVrIssuer();
            }
        };
        ips[795] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setMorningstarsDZBANK(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getMorningstarsDZBANK();
            }
        };
        ips[817] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwdsrrivalue(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwdsrrivalue();
            }
        };
        ips[818] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setVwdsrrivaluedate(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getVwdsrrivaluedate();
            }
        };
        ips[820] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setVwddiamondrating(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getVwddiamondrating();
            }
        };
        ips[821] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, int value) {
                ratios.setVwddiamondratingdate(value);
            }

            public int getInt(InstrumentRatiosFND ratios) {
                return ratios.getVwddiamondratingdate();
            }
        };
        ips[822] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setFidaRatingROnly(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getFidaRatingROnly();
            }
        };
        ips[823] = new PropertySupport<InstrumentRatiosFND>() {
            public void set(InstrumentRatiosFND ratios, long value) {
                ratios.setFidaRatingIOnly(value);
            }

            public long getLong(InstrumentRatiosFND ratios) {
                return ratios.getFidaRatingIOnly();
            }
        };

        qps[1] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMostRecentUpdateTimestamp(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMostRecentUpdateTimestamp();
            }
        };
        qps[4] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setVolatility1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getVolatility1m();
            }
        };
        qps[6] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSpread(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSpread();
            }
        };
        qps[10] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setReferenceTimestamp(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getReferenceTimestamp();
            }
        };
        qps[15] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSpreadRelative(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSpreadRelative();
            }
        };
        qps[16] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBid(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBid();
            }
        };
        qps[17] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAsk(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAsk();
            }
        };
        qps[18] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setLastPrice(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getLastPrice();
            }
        };
        qps[19] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setPreviousClose(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getPreviousClose();
            }
        };
        qps[28] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBidVolume(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBidVolume();
            }
        };
        qps[29] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAskVolume(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAskVolume();
            }
        };
        qps[34] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setBidAskDate(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getBidAskDate();
            }
        };
        qps[35] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setBidAskTime(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getBidAskTime();
            }
        };
        qps[36] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTradeVolume(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTradeVolume();
            }
        };
        qps[37] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setLastDate(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getLastDate();
            }
        };
        qps[38] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setLastTime(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getLastTime();
            }
        };
        qps[39] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setHigh(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getHigh();
            }
        };
        qps[40] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setLow(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getLow();
            }
        };
        qps[41] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setHighYear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getHighYear();
            }
        };
        qps[42] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setLowYear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getLowYear();
            }
        };
        qps[43] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTotalVolume(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTotalVolume();
            }
        };
        qps[44] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setPreviousDate(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getPreviousDate();
            }
        };
        qps[45] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setOpen(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getOpen();
            }
        };
        qps[60] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setIssuePrice(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getIssuePrice();
            }
        };
        qps[61] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setInterimProfit(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getInterimProfit();
            }
        };
        qps[62] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setInterimProfitDate(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getInterimProfitDate();
            }
        };
        qps[63] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setEstateProfit(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getEstateProfit();
            }
        };
        qps[64] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setStockProfit(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getStockProfit();
            }
        };
        qps[65] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setStockProfitDate(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getStockProfitDate();
            }
        };
        qps[67] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setVolatility3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getVolatility3m();
            }
        };
        qps[72] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setVolatility10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getVolatility10y();
            }
        };
        qps[73] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setVolatility1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getVolatility1y();
            }
        };
        qps[74] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSharperatio1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSharperatio1y();
            }
        };
        qps[75] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSharperatio3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSharperatio3y();
            }
        };
        qps[76] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformance1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformance1y();
            }
        };
        qps[77] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformance3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformance3y();
            }
        };
        qps[78] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTreynor1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTreynor1y();
            }
        };
        qps[79] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTreynor3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTreynor3y();
            }
        };
        qps[80] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBeta1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBeta1y();
            }
        };
        qps[82] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setCorrelation1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getCorrelation1y();
            }
        };
        qps[84] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setDateoflast(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getDateoflast();
            }
        };
        qps[108] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setVolatility1w(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getVolatility1w();
            }
        };
        qps[115] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMmnetassetvalue(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMmnetassetvalue();
            }
        };
        qps[116] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMmissueprice(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMmissueprice();
            }
        };
        qps[117] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setPerformance1d(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getPerformance1d();
            }
        };
        qps[118] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setPerformancetobenchmark3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getPerformancetobenchmark3m();
            }
        };
        qps[119] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setPerformancetobenchmark1w(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getPerformancetobenchmark1w();
            }
        };
        qps[120] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setPerformancetobenchmark1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getPerformancetobenchmark1m();
            }
        };
        qps[121] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setPerformancetobenchmark1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getPerformancetobenchmark1y();
            }
        };
        qps[122] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragevolume1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragevolume1m();
            }
        };
        qps[123] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragevolume1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragevolume1y();
            }
        };
        qps[124] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBeta1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBeta1m();
            }
        };
        qps[126] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setPerformancetobenchmark6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getPerformancetobenchmark6m();
            }
        };
        qps[130] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setVolatility6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getVolatility6m();
            }
        };
        qps[131] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setVolatility5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getVolatility5y();
            }
        };
        qps[132] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSharperatio5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSharperatio5y();
            }
        };
        qps[133] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSharperatio1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSharperatio1m();
            }
        };
        qps[134] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSharperatio3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSharperatio3m();
            }
        };
        qps[135] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSharperatio6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSharperatio6m();
            }
        };
        qps[136] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setCorrelation1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getCorrelation1m();
            }
        };
        qps[155] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdCode(value);
            }

            public String getString(QuoteRatiosFND ratios, int localeIndex) {
                return ratios.getVwdCode();
            }
        };
        qps[156] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int localeIndex, String value) {
                ratios.setMmwkn(value);
            }

            public String getString(QuoteRatiosFND ratios, int localeIndex) {
                return ratios.getMmwkn();
            }
        };
        qps[157] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int localeIndex, String value) {
                ratios.setCurrency(value);
            }

            public String getString(QuoteRatiosFND ratios, int localeIndex) {
                return ratios.getCurrency();
            }
        };
        qps[167] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setHighAlltime(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getHighAlltime();
            }
        };
        qps[168] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setLowAlltime(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getLowAlltime();
            }
        };
        qps[169] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setHighAlltimeDate(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getHighAlltimeDate();
            }
        };
        qps[171] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setLowAlltimeDate(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getLowAlltimeDate();
            }
        };
        qps[177] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setHigh1yDate(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getHigh1yDate();
            }
        };
        qps[178] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setLow1yDate(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getLow1yDate();
            }
        };
        qps[179] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragePrice1w(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragePrice1w();
            }
        };
        qps[180] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragePrice1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragePrice1m();
            }
        };
        qps[181] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragePrice3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragePrice3m();
            }
        };
        qps[182] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragePrice6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragePrice6m();
            }
        };
        qps[183] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragePrice1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragePrice1y();
            }
        };
        qps[184] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragePrice3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragePrice3y();
            }
        };
        qps[186] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragePrice5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragePrice5y();
            }
        };
        qps[187] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragePrice10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragePrice10y();
            }
        };
        qps[188] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBeta1w(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBeta1w();
            }
        };
        qps[189] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBeta3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBeta3m();
            }
        };
        qps[190] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBeta6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBeta6m();
            }
        };
        qps[191] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setPerformancetobenchmark3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getPerformancetobenchmark3y();
            }
        };
        qps[192] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setPerformancetobenchmark5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getPerformancetobenchmark5y();
            }
        };
        qps[204] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setPerformancetobenchmark10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getPerformancetobenchmark10y();
            }
        };
        qps[240] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setChangeNet(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getChangeNet();
            }
        };
        qps[241] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setChangePercent(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getChangePercent();
            }
        };
        qps[243] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformance1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformance1m();
            }
        };
        qps[244] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformance3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformance3m();
            }
        };
        qps[252] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformance6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformance6m();
            }
        };
        qps[253] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformance5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformance5y();
            }
        };
        qps[254] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformance10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformance10y();
            }
        };
        qps[255] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setNegativeMonthsPercent1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getNegativeMonthsPercent1m();
            }
        };
        qps[256] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setNegativeMonthsPercent3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getNegativeMonthsPercent3m();
            }
        };
        qps[257] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setNegativeMonthsPercent6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getNegativeMonthsPercent6m();
            }
        };
        qps[258] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setNegativeMonthsPercent1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getNegativeMonthsPercent1y();
            }
        };
        qps[259] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setNegativeMonthsPercent3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getNegativeMonthsPercent3y();
            }
        };
        qps[260] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setNegativeMonthsPercent5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getNegativeMonthsPercent5y();
            }
        };
        qps[261] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setNegativeMonthsPercent10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getNegativeMonthsPercent10y();
            }
        };
        qps[299] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setVolatility3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getVolatility3y();
            }
        };
        qps[307] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setCorrelation1w(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getCorrelation1w();
            }
        };
        qps[308] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setCorrelation3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getCorrelation3m();
            }
        };
        qps[309] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setCorrelation6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getCorrelation6m();
            }
        };
        qps[310] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setCorrelation3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getCorrelation3y();
            }
        };
        qps[311] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setCorrelation5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getCorrelation5y();
            }
        };
        qps[312] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setCorrelation10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getCorrelation10y();
            }
        };
        qps[315] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaximumLoss1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaximumLoss1y();
            }
        };
        qps[316] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaximumLossDays1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaximumLossDays1y();
            }
        };
        qps[319] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setHigh1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getHigh1y();
            }
        };
        qps[320] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setLow1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getLow1y();
            }
        };
        qps[321] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaximumLoss6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaximumLoss6m();
            }
        };
        qps[322] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaximumLossMonths3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaximumLossMonths3y();
            }
        };
        qps[323] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragevolume1w(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragevolume1w();
            }
        };
        qps[324] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragevolume3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragevolume3m();
            }
        };
        qps[325] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragevolume6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragevolume6m();
            }
        };
        qps[326] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragevolume3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragevolume3y();
            }
        };
        qps[327] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragevolume5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragevolume5y();
            }
        };
        qps[329] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAveragevolume10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAveragevolume10y();
            }
        };
        qps[332] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int localeIndex, String value) {
                ratios.setMarketmanagerName(value);
            }

            public String getString(QuoteRatiosFND ratios, int localeIndex) {
                return ratios.getMarketmanagerName();
            }
        };
        qps[333] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformancecurrentyear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformancecurrentyear();
            }
        };
        qps[334] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaximumLoss3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaximumLoss3y();
            }
        };
        qps[342] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setVolatilityCurrentYear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getVolatilityCurrentYear();
            }
        };
        qps[346] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAlpha1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAlpha1m();
            }
        };
        qps[347] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAlpha1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAlpha1y();
            }
        };
        qps[371] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBeta3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBeta3y();
            }
        };
        qps[382] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBeta5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBeta5y();
            }
        };
        qps[387] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBeta10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBeta10y();
            }
        };
        qps[393] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformance1w(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformance1w();
            }
        };
        qps[394] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSharperatio1w(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSharperatio1w();
            }
        };
        qps[395] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSharperatio10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSharperatio10y();
            }
        };
        qps[396] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTreynor1w(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTreynor1w();
            }
        };
        qps[397] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTreynor1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTreynor1m();
            }
        };
        qps[398] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTreynor3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTreynor3m();
            }
        };
        qps[399] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTreynor6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTreynor6m();
            }
        };
        qps[400] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTreynor5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTreynor5y();
            }
        };
        qps[401] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTreynor10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTreynor10y();
            }
        };
        qps[402] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaximumLossMonths1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaximumLossMonths1y();
            }
        };
        qps[403] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaximumLossMonths5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaximumLossMonths5y();
            }
        };
        qps[404] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaximumLossMonths10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaximumLossMonths10y();
            }
        };
        qps[405] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setReferencePrice(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getReferencePrice();
            }
        };
        qps[406] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformanceAlltime(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformanceAlltime();
            }
        };
        qps[414] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int localeIndex, String value) {
                ratios.setVwdMarket(value);
            }

            public String getString(QuoteRatiosFND ratios, int localeIndex) {
                return ratios.getVwdMarket();
            }
        };
        qps[417] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setClosePreviousYear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getClosePreviousYear();
            }
        };
        qps[418] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setClosePreviousYearDate(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getClosePreviousYearDate();
            }
        };
        qps[419] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setClosePreviousMonth(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getClosePreviousMonth();
            }
        };
        qps[420] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setClosePreviousMonthDate(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getClosePreviousMonthDate();
            }
        };
        qps[423] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setHighPreviousYear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getHighPreviousYear();
            }
        };
        qps[424] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setHighPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getHighPreviousYearDate();
            }
        };
        qps[425] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setLowPreviousYear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getLowPreviousYear();
            }
        };
        qps[426] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setLowPreviousYearDate(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getLowPreviousYearDate();
            }
        };
        qps[427] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformancecurrentmonth(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformancecurrentmonth();
            }
        };
        qps[428] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setChangeNetCurrentYear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getChangeNetCurrentYear();
            }
        };
        qps[429] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setChangeNetCurrentMonth(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getChangeNetCurrentMonth();
            }
        };
        qps[441] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAlpha1w(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAlpha1w();
            }
        };
        qps[442] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAlpha3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAlpha3m();
            }
        };
        qps[443] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAlpha6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAlpha6m();
            }
        };
        qps[444] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAlpha3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAlpha3y();
            }
        };
        qps[445] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAlpha5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAlpha5y();
            }
        };
        qps[446] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAlpha10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAlpha10y();
            }
        };
        qps[506] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTrackingError1w(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTrackingError1w();
            }
        };
        qps[507] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTrackingError1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTrackingError1m();
            }
        };
        qps[508] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTrackingError3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTrackingError3m();
            }
        };
        qps[509] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTrackingError6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTrackingError6m();
            }
        };
        qps[510] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTrackingError1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTrackingError1y();
            }
        };
        qps[511] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTrackingError3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTrackingError3y();
            }
        };
        qps[512] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTrackingError5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTrackingError5y();
            }
        };
        qps[513] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTrackingError10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTrackingError10y();
            }
        };
        qps[514] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setInformationRatio1w(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getInformationRatio1w();
            }
        };
        qps[515] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setInformationRatio1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getInformationRatio1m();
            }
        };
        qps[516] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setInformationRatio3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getInformationRatio3m();
            }
        };
        qps[517] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setInformationRatio6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getInformationRatio6m();
            }
        };
        qps[518] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setInformationRatio1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getInformationRatio1y();
            }
        };
        qps[519] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setInformationRatio3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getInformationRatio3y();
            }
        };
        qps[520] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setInformationRatio5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getInformationRatio5y();
            }
        };
        qps[521] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setInformationRatio10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getInformationRatio10y();
            }
        };
        qps[522] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSterlingRatio1w(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSterlingRatio1w();
            }
        };
        qps[523] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSterlingRatio1m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSterlingRatio1m();
            }
        };
        qps[524] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSterlingRatio3m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSterlingRatio3m();
            }
        };
        qps[525] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSterlingRatio6m(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSterlingRatio6m();
            }
        };
        qps[526] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSterlingRatio1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSterlingRatio1y();
            }
        };
        qps[527] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSterlingRatio3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSterlingRatio3y();
            }
        };
        qps[528] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSterlingRatio5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSterlingRatio5y();
            }
        };
        qps[529] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSterlingRatio10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSterlingRatio10y();
            }
        };
        qps[531] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setQid(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getQid();
            }
        };
        qps[532] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setDateFirstPrice(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getDateFirstPrice();
            }
        };
        qps[550] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setProbabilityofoutperformance3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getProbabilityofoutperformance3y();
            }
        };
        qps[551] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setPositiveregression3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getPositiveregression3y();
            }
        };
        qps[552] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setNegativeregression3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getNegativeregression3y();
            }
        };
        qps[553] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformancelastyear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformancelastyear();
            }
        };
        qps[554] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformanceyearbeforelastyear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformanceyearbeforelastyear();
            }
        };
        qps[555] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setPerformanceSinceFundIssueDate(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getPerformanceSinceFundIssueDate();
            }
        };
        qps[619] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformance2_1y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformance2_1y();
            }
        };
        qps[620] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformance3_2y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformance3_2y();
            }
        };
        qps[621] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformance4_3y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformance4_3y();
            }
        };
        qps[622] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBviperformance5_4y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBviperformance5_4y();
            }
        };
        qps[648] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaSimple38(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaSimple38();
            }
        };
        qps[649] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaSimple90(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaSimple90();
            }
        };
        qps[650] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaSimple200(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaSimple200();
            }
        };
        qps[669] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setReference1w(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getReference1w();
            }
        };
        qps[670] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setReference1m(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getReference1m();
            }
        };
        qps[671] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setReference3m(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getReference3m();
            }
        };
        qps[672] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setReference6m(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getReference6m();
            }
        };
        qps[673] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setReference1y(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getReference1y();
            }
        };
        qps[674] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setReference3y(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getReference3y();
            }
        };
        qps[675] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setReference5y(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getReference5y();
            }
        };
        qps[676] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setReference10y(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getReference10y();
            }
        };
        qps[677] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, int value) {
                ratios.setReferenceAlltime(value);
            }

            public int getInt(QuoteRatiosFND ratios) {
                return ratios.getReferenceAlltime();
            }
        };
        qps[721] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setPerformancetobenchmarkcurrentyear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getPerformancetobenchmarkcurrentyear();
            }
        };
        qps[722] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setCorrelationcurrentyear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getCorrelationcurrentyear();
            }
        };
        qps[760] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setVolatilityAlltime(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getVolatilityAlltime();
            }
        };
        qps[761] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBetaCurrentYear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBetaCurrentYear();
            }
        };
        qps[762] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setBetaAlltime(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getBetaAlltime();
            }
        };
        qps[763] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSharperatioCurrentYear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSharperatioCurrentYear();
            }
        };
        qps[764] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setSharperatioAlltime(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getSharperatioAlltime();
            }
        };
        qps[765] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAlphaCurrentYear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAlphaCurrentYear();
            }
        };
        qps[766] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setAlphaAlltime(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getAlphaAlltime();
            }
        };
        qps[767] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTreynorCurrentYear(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTreynorCurrentYear();
            }
        };
        qps[768] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setTreynorAlltime(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getTreynorAlltime();
            }
        };
        qps[770] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaximumLoss5y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaximumLoss5y();
            }
        };
        qps[771] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaximumLoss10y(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaximumLoss10y();
            }
        };
        qps[772] = new PropertySupport<QuoteRatiosFND>() {
            public void set(QuoteRatiosFND ratios, long value) {
                ratios.setMaximumLossAlltime(value);
            }

            public long getLong(QuoteRatiosFND ratios) {
                return ratios.getMaximumLossAlltime();
            }
        };

    }
}