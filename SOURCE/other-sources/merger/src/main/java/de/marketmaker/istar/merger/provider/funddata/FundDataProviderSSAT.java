/*
 * FundDataProviderImpl.java
 *
 * Created on 11.08.2006 18:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.funddata;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domainimpl.data.MasterDataFundImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundDataProviderSSAT extends AbstractFundDataProvider {

    public static final MasterDataFund.Source SOURCE = MasterDataFund.Source.SSAT;

    @Override
    protected IstarMdpExportReader<Map<Long, MasterDataFund>> createMasterDataReader() {
        return new MasterDataReader();
    }

    @Override
    protected IstarMdpExportReader<Map<Long, List<InstrumentAllocation>>> createAllocationsReader() {
        return new AllocationsReaderCommon(this.limitedRead, SOURCE);
    }

    /**
     * @author oflege
     */
    static class MasterDataReader extends IstarMdpExportReader<Map<Long, MasterDataFund>> {

        private static final Logger log = LoggerFactory.getLogger(MasterDataReader.class);

        private static final Map<String, BigDecimal> DC_MAP;

        static {
            DC_MAP = new HashMap<>(8);
            DC_MAP.put("1", BigDecimal.ONE);
            DC_MAP.put("2", BigDecimal.valueOf(2));
            DC_MAP.put("4", BigDecimal.valueOf(4));
            DC_MAP.put("12", BigDecimal.valueOf(12));
        }

        private final Map<Long, MasterDataFund> values = new HashMap<>();

        MasterDataReader() {
            super(
                    "CURRENCY",
                    "ANLAGESTRATEGIE",
                    "ANLAGESCHWERPUNKT",
                    "KAG",
                    "LAND",
                    "KAGORT",
                    "KAGPLZ",
                    "KAGSTRASSE",
                    "KAGPHONE",
                    "KAGFAX",
                    "KAGEMAIL",
                    "KAGURL",
                    "BENCHMARKNAME",
                    "DISTRIBUTIONCURRENCY",
                    "VERTRIEBSZULASSUNG"
            );
        }

        protected Map<Long, MasterDataFund> getResult() {
            return this.values;
        }

        protected void handleRow() {
            final MasterDataFundImpl.Builder builder = new MasterDataFundImpl.Builder();

            final Long iid = getLong("INSTRUMENTID", "IID");
            if (null == iid) {
                return;
            }
            builder.setFundtype(get("FONDSTYP"), Language.de);
            builder.setInstrumentid(iid);
            builder.setCurrency(get("CURRENCY"));
            builder.setStrategy(get("ANLAGESTRATEGIE"), Language.de);
            builder.setInvestmentFocus(get("ANLAGESCHWERPUNKT"), Language.de);
            builder.setIssuerName(get("KAG"), Language.de, Language.en);
            builder.setCountry(get("LAND"), Language.de);
            builder.setIssuerCity(get("KAGORT"));
            builder.setIssuerPostalcode(get("KAGPLZ"));
            builder.setIssuerStreet(get("KAGSTRASSE"));
            builder.setIssuerEmail(get("KAGEMAIL"));
            builder.setIssuerUrl(get("KAGURL"));
            builder.setIssuerPhone(get("KAGPHONE"));
            builder.setIssuerFax(get("KAGFAX"));
            builder.setBenchmarkName(get("BENCHMARKNAME"), Language.de, Language.en);
            builder.setBenchmarkQid(getLong("BENCHMARKQID"));
            builder.setFundManager(get("FONDSMANAGER"), Language.de, Language.en);
            builder.setIssueDate(getDateTime("AUFLAGEDATUM"));
            builder.setFundVolume(getBigDecimal("FONDSVOLUMEN"));
            builder.setFundVolumeDate(getDateTime("DATUM_FONDSVOLUMEN"));
            builder.setIssueSurcharge(getPercent("AUSGABEAUFSCHLAG"));
            final Integer aatyp = getInt("AUSGABEAUFSCHLAGTYP");
            if (aatyp != null) {
                builder.setIssueSurchargeType(getSurchargeType(aatyp));
            }
            builder.setIssueSurchargeNet(getPercent("AUSGABEAUFSCHLAGNETTO"));
            builder.setIssueSurchargeGross(getPercent("AUSGABEAUFSCHLAGBRUTTO"));
            builder.setManagementFee(getPercent("MANAGEMENTFEE"));
            builder.setAccountFee(getPercent("DEPOTBANKGEBUEHR"));
            builder.setTer(getPercent("TER"));
            builder.setDistributionCount(getDistributionCount(get("DISTRIBUTIONCOUNT")));
            builder.setLastDistribution(getBigDecimal("LASTDISTRIBUTION"));
            builder.setLastDistributionDate(getDateTime("LASTDISTRIBUTIONDATE"));
            builder.setDistributionCurrency(get("DISTRIBUTIONCURRENCY"));
            builder.setPortfolioDate(getDateTime("PORTFOLIODATE"));
            builder.setMinimumInvestment(getBigDecimal("EINMALANLAGE"));
            builder.setDuration(getBigDecimal("DURATION_"));
            builder.setModifiedDuration(getBigDecimal("MODIFIEDDURATION"));
            final String ds = get("DISTRIBUTIONSTRATEGY");
            if (ds != null) {
                builder.setDistributionStrategy(ds.startsWith("Aus")
                        ? MasterDataFund.DistributionStrategy.DISTRIBUTING
                        : MasterDataFund.DistributionStrategy.RETAINING);
            }
            builder.setFundOfFunds(getBoolean("FUNDOFFUNDS"));
            builder.setMarketAdmission(get("VERTRIEBSZULASSUNG"));
            builder.setSrriValue(get("SRRIVALUE"));
            builder.setSource(SOURCE);

            final MasterDataFund data = builder.build();
            this.values.put(data.getInstrumentid(), data);
        }

        private MasterDataFundImpl.IssueSurchargeType getSurchargeType(Integer aatyp) {
            switch (aatyp) {
                case 1:
                    return MasterDataFundImpl.IssueSurchargeType.NET;
                case 2:
                    return MasterDataFundImpl.IssueSurchargeType.GROSS;
                default:
                    return MasterDataFundImpl.IssueSurchargeType.UNKNOWN;
            }
        }

        private BigDecimal getDistributionCount(String str) {
            if (null == str) {
                return null;
            }
            if (DC_MAP.containsKey(str)) {
                return DC_MAP.get(str);
            }
            else {
                log.error("<getDistributionCount> unknown distribution count: " + str);
                return null;
            }
        }
    }

    public static void main(String[] args) {
        final TimeTaker tt = new TimeTaker();
        final FundDataProviderSSAT provider = new FundDataProviderSSAT();
        File dir = new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider");
        provider.setMasterDataFile(new File(dir, "istar-ssat-fund-masterdata.xml.gz"));
        provider.setAllocationsFile(new File(dir, "istar-ssat-fund-breakdown.xml.gz"));
        provider.readMasterDataFund();
        provider.readAllocations();
        System.out.println("prepared: " + tt);

        final long[] iids = {
                408902,
                98904806
        };

        for (long iid : iids) {
            System.out.println(provider.getMasterData(iid));
            System.out.println(provider.getInstrumentAllocations(iid));
        }
        System.out.println("finished: " + tt);
    }

}
