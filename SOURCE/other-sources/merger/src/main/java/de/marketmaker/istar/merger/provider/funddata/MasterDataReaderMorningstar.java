/*
 * MasterDataReaderMorningstar.java
 *
 * Created on 08.10.2010 12:57:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.funddata;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domainimpl.data.MasterDataFundImpl;
import de.marketmaker.istar.ratios.backend.MarketAdmissionUtil;

/**
 * @author oflege
 */
class MasterDataReaderMorningstar extends IstarMdpExportReader<Map<Long, MasterDataFund>> {

    private static final String SONSTIGE = "Sonstige";

    private static final String OTHER = "Other";

    private final Map<Long, MasterDataFund> values = new HashMap<>();

    /**
     * For each key that describes the number of distributrions per year in Morningstar data,
     * this map contains that number as a BigDecimal
     */
    private static final Map<String, BigDecimal> DISTRIBUTION_COUNT = new HashMap<>();

    static {
        DISTRIBUTION_COUNT.put("A$", BigDecimal.ONE);
        DISTRIBUTION_COUNT.put("a$", new BigDecimal(2));
        DISTRIBUTION_COUNT.put("B$", new BigDecimal(6));
        DISTRIBUTION_COUNT.put("D$", new BigDecimal(365));
        DISTRIBUTION_COUNT.put("M$", new BigDecimal(12));
        DISTRIBUTION_COUNT.put("m$", new BigDecimal(26));
        DISTRIBUTION_COUNT.put("N$", BigDecimal.ZERO);
        DISTRIBUTION_COUNT.put("Q$", new BigDecimal(4));
        DISTRIBUTION_COUNT.put("W$", new BigDecimal(52));
    }

    MasterDataReaderMorningstar() {
        super("CURRENCY", "ANLAGESTRATEGIE", "ANLAGESTRATEGIE_EN", "ANLAGESCHWERPUNKT", "ANLAGESCHWERPUNKT_EN",
                "KAG", "LAND", "LAND_EN", "BROADASSETCLASS__DE", "BROADASSETCLASS__EN",
                "KAGORT", "KAGPLZ", "KAGSTRASSE", "KAGPHONE", "KAGFAX", "KAGEMAIL", "KAGURL",
                "MORNINGSTAROVERALLRATING", "BENCHMARKNAME", "DISTRIBUTIONCURRENCY", "BVIKATEGORIEGROB",
                "FONDSVOLUMENCURRENCY", "FUNDCLASSVOLUMECURRENCY");
    }

    protected Map<Long, MasterDataFund> getResult() {
        return this.values;
    }

    protected void handleRow() {
        final MasterDataFundImpl.Builder builder = new MasterDataFundImpl.Builder();

        builder.setInstrumentid(getLong("INSTRUMENTID", "IID"));
        builder.setCurrency(get("CURRENCY"));
        builder.setBroadassetclass(get("BROADASSETCLASS__DE"), Language.de);
        builder.setBroadassetclass(get("BROADASSETCLASS__EN"), Language.en);
        builder.setStrategy(get("ANLAGESTRATEGIE"), Language.de);
        builder.setStrategy(get("ANLAGESTRATEGIE_EN"), Language.en);
        builder.setInvestmentFocus(get("ANLAGESCHWERPUNKT"), Language.de);
        builder.setInvestmentFocus(get("ANLAGESCHWERPUNKT_EN"), Language.en);
        builder.setIssuerName(get("KAG"), Language.de, Language.en);
        builder.setCountry(get("LAND"), Language.de);
        builder.setCountry(get("LAND_EN"), Language.en);
        builder.setIssuerCity(get("KAGORT"));
        builder.setIssuerPostalcode(get("KAGPLZ"));
        builder.setIssuerStreet(get("KAGSTRASSE"));
        builder.setIssuerEmail(get("KAGEMAIL"));
        builder.setIssuerUrl(get("KAGURL"));
        builder.setIssuerPhone(get("KAGPHONE"));
        builder.setIssuerPhone(get("KAGFAX"));
        builder.setMorningstarOverallRating(get("MORNINGSTAROVERALLRATING"));
        builder.setMorningstarRatingDate(getDateTime("MORNINGSTARRATINGDATE"));
        builder.setBenchmarkName(get("BENCHMARKNAME"), Language.de, Language.en);
        builder.setBenchmarkQid(getLong("BENCHMARKQID"));
        builder.setFundManager(get("FONDSMANAGER"), Language.de, Language.en);
        builder.setIssueDate(getDateTime("AUFLAGEDATUM"));
        builder.setFundVolume(getBigDecimal("FONDSVOLUMEN"));
        builder.setFundVolumeCurrency(get("FONDSVOLUMENCURRENCY"));
        builder.setFundVolumeDate(getDateTime("DATUM_FONDSVOLUMEN"));
        builder.setIssueSurcharge(getPercent("AUSGABEAUFSCHLAG"));
        builder.setManagementFee(getPercent("MANAGEMENTFEE"));
        builder.setAccountFee(getPercent("DEPOTBANKGEBUEHR"));
        builder.setPerformanceFee(getPercent("PERFORMANCEGEBUEHR"));
        builder.setTer(getPercent("TER"));
        builder.setDistributionCount(getDistributionCount(get("DISTRIBUTIONCOUNT")));
        builder.setLastDistribution(getBigDecimal("LASTDISTRIBUTION"));
        builder.setDistributionCurrency(get("DISTRIBUTIONCURRENCY"));
        builder.setMinimumInvestment(getBigDecimal("EINMALANLAGE"));
        builder.setLastDistributionDate(getDateTime("LASTDISTRIBUTIONDATE"));
        builder.setFundtypeBviCoarse(get("BVIKATEGORIEGROB"));
        builder.setPortfolioDate(getDateTime("PORTFOLIODATE"));
        builder.setDuration(getBigDecimal("DURATION_"));
        builder.setModifiedDuration(getBigDecimal("MODIFIEDDURATION"));
        builder.setOngoingCharge(getPercent("ONGOINGCHARGE"));
        builder.setOngoingChargeDate(getDateTime("ONGOINGCHARGEDATE"));
        builder.setFundclassVolume(getBigDecimal("FUNDCLASSVOLUME"));
        builder.setFundclassVolumeCurrency(get("FUNDCLASSVOLUMECURRENCY"));
        builder.setFundclassVolumeDate(getDateTime("FUNDCLASSVOLUMEDATE"));
        builder.setMarketAdmission(getMarketAdmission());
        final String ds = get("AUSCHUETTUNGSART");
        if (ds != null) {
            builder.setDistributionStrategy("true".equals(ds)
                    ? MasterDataFund.DistributionStrategy.DISTRIBUTING
                    : MasterDataFund.DistributionStrategy.RETAINING);
        }

        builder.setFundtype(getFundType_de(), Language.de);
        builder.setFundtype(getFundType_en(), Language.en);
        builder.setSource(FundDataProviderMorningstar.SOURCE);
        final MasterDataFund data = builder.build();
        this.values.put(data.getInstrumentid(), data);
    }

    static int m;

    protected BigDecimal getPercent(String name) {
        m++;
        return super.getPercent(name);
    }

    private String getMarketAdmission() {
        final String s = get("MSMARKETADMISSION");
        return (s != null) ? MarketAdmissionUtil.iso3166Alpha3To2(s) : null;
    }

    private BigDecimal getDistributionCount(String str) {
        return (str != null) ? DISTRIBUTION_COUNT.get(str) : null;
    }

    private String getFundType_de() {
        if (getBoolean("ASFUND")) {
            return "AS-Fonds";
        }
        if (getBoolean("FUNDOFFUNDS")) {
            return "Dachfonds";
        }

        final String str = get("FONDSTYP");

        if (str == null) {
            return SONSTIGE;
        }

        final int index = str.lastIndexOf("$");
        if (index < 0) {
            return SONSTIGE;
        }
        final String type = str.substring(index + 1);
        if ("CT".equals(type) || "FI".equals(type)) {
            return "Rentenfonds";
        }
        else if ("FW".equals(type) || "WA".equals(type)) {
            return "Derivatefonds";
        }
        else if ("DG".equals(type)) {
            return "Geldmarktfonds";
        }
        else if ("EF".equals(type)) {
            return "Laufzeitfonds";
        }
        else if ("EG".equals(type)) {
            return "Aktienfonds";
        }
        else if ("HD".equals(type)) {
            return "Hedgefonds";
        }
        else if ("MI".equals(type)) {
            return "Mischfonds";
        }
        else if ("RE".equals(type)) {
            return "Immobilienfonds";
        }
        else if ("BB".equals(type)) {
            return SONSTIGE;
        }

        return SONSTIGE;
    }

    private String getFundType_en() {
        if (getBoolean("ASFUND")) {
            return "AS (Altersvorsorge-Sondervermögen) funds";
        }
        if (getBoolean("FUNDOFFUNDS")) {
            return "Fund of funds";
        }

        final String str = get("FONDSTYP");

        if (str == null) {
            return OTHER;
        }

        final int index = str.lastIndexOf("$");
        if (index < 0) {
            return OTHER;
        }
        final String type = str.substring(index + 1);
        if ("CT".equals(type) || "FI".equals(type)) {
            return "Bond fund";
        }
        else if ("FW".equals(type) || "WA".equals(type)) {
            return "Derivative fund";
        }
        else if ("DG".equals(type)) {
            return "Money market fund";
        }
        else if ("EF".equals(type)) {
            return "Fixed-term fund";
        }
        else if ("EG".equals(type)) {
            return "Equity fund";
        }
        else if ("HD".equals(type)) {
            return "Hedge fund";
        }
        else if ("MI".equals(type)) {
            return "Mixed fund";
        }
        else if ("RE".equals(type)) {
            return "Property fund";
        }
        else if ("BB".equals(type)) {
            return OTHER;
        }

        return OTHER;
    }

    public static void main(String[] args) throws Exception {
        read();
        int k = 1;
        System.gc();
        System.err.println(Runtime.getRuntime().freeMemory());
    }

    private static void read() throws Exception {
        TimeTaker tt = new TimeTaker();
        final File f = new File("d:/produktion/var/data/provider/istar-morningstar-fund-masterdata.xml.gz");
        final MasterDataReaderMorningstar reader = new MasterDataReaderMorningstar();
        final Map<Long, MasterDataFund> map = reader.read(f);
        System.out.println(map.size() + ", took: " + tt);
        System.out.println(map.get(52112L));
        System.out.println("m = " + m);
    }
}
