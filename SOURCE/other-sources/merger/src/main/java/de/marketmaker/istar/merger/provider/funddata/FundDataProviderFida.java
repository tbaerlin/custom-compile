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

import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.InstrumentAllocations;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domainimpl.data.MasterDataFundImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundDataProviderFida extends AbstractFundDataProvider {

    public static final MasterDataFund.Source SOURCE = MasterDataFund.Source.FIDA;

    private static final Map<String, BigDecimal> DISTRIBUTION_COUNT = new HashMap<>();

    static {
        DISTRIBUTION_COUNT.put("Annuale", BigDecimal.ONE);
        DISTRIBUTION_COUNT.put("Giornaliero", new BigDecimal(365));
        DISTRIBUTION_COUNT.put("Irregolare", BigDecimal.ZERO);
        DISTRIBUTION_COUNT.put("Mensile", new BigDecimal(12));
        DISTRIBUTION_COUNT.put("Semestrale", new BigDecimal(2));
        DISTRIBUTION_COUNT.put("Trimestrale", new BigDecimal(4));
    }

    @Override
    protected IstarMdpExportReader<Map<Long, MasterDataFund>> createMasterDataReader() {
        return new MasterDataReaderFida();
    }

    @Override
    protected IstarMdpExportReader<Map<Long, List<InstrumentAllocation>>> createAllocationsReader() {
        return new AllocationsReaderCommon(this.limitedRead, "DESCRIPTION", "TYPE", SOURCE);
    }

    protected synchronized InstrumentAllocations getInstrumentAllocations(long instrumentid) {
        final List<InstrumentAllocation> result = this.allocations.get(instrumentid);
        if (result == null) {
            return InstrumentAllocations.NULL;
        }
        final MasterDataFund md = this.masterData.get(instrumentid);

        if (md == null) {
            return InstrumentAllocations.NULL;
        }

        return InstrumentAllocations.create(result, md.getPermissionType());
    }

    public static void main(String[] args) {
        final TimeTaker tt = new TimeTaker();
        final FundDataProviderFida provider = new FundDataProviderFida();
        File dir = new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider");
        provider.setMasterDataFile(new File(dir, "istar-fida-fund-masterdata.xml.gz"));
        provider.setAllocationsFile(new File(dir, "istar-fida-fund-breakdown.xml.gz"));
        provider.readMasterDataFund();
        provider.readAllocations();
        System.out.println("prepared: " + tt);

        final long[] iids = {
                462411,
                36626130
        };

        for (long iid : iids) {
            System.out.println(provider.getMasterData(iid));
            System.out.println(provider.getInstrumentAllocations(iid));
        }
        System.out.println("finished: " + tt);
    }

    /**
     * @author oflege
     */
    static class MasterDataReaderFida extends IstarMdpExportReader<Map<Long, MasterDataFund>> {

        private final Map<Long, MasterDataFund> values = new HashMap<>();

        MasterDataReaderFida() {
            super(
                    "ANLAGESCHWERPUNKT",
                    "ANLAGESTRATEGIE",
                    "BENCHMARKNAME",
                    "CURRENCY",
                    "FIDARATING",
                    "FONDSTYP",
                    "KAG",
                    "COUNTRY",
                    "FUNDVOLUMECURRENCY"
            );
        }

        protected Map<Long, MasterDataFund> getResult() {
            return this.values;
        }

        private BigDecimal getDistributionCount(String str) {
            return (str != null) ? DISTRIBUTION_COUNT.get(str) : null;
        }

        protected void handleRow() {
            final MasterDataFundImpl.Builder builder = new MasterDataFundImpl.Builder();

            final Long iid = getLong("INSTRUMENTID", "IID");
            if (null == iid) {
                return;
            }
            builder.setInstrumentid(iid);
            builder.setFundtype(get("FONDSTYP"), Language.it);
            builder.setCurrency(get("CURRENCY"));
            builder.setStrategy(get("ANLAGESTRATEGIE"), Language.it);
            builder.setInvestmentFocus(get("ANLAGESCHWERPUNKT"), Language.it);
            builder.setIssuerName(get("KAG"), Language.it);
            builder.setBenchmarkName(get("BENCHMARKNAME"), Language.it);
            builder.setIssueDate(getDateTime("AUFLAGEDATUM"));
            builder.setTer(getPercent("TER"));
            builder.setPortfolioDate(getDateTime("PORTFOLIODATE"));
            builder.setCountry(get("COUNTRY"), Language.it);
            final String ds = get("DISTRIBUTIONSTRATEGY");
            if (StringUtils.hasText(ds)) {
                switch (ds) {
                    case "T":
                        builder.setDistributionStrategy(MasterDataFund.DistributionStrategy.RETAINING);
                        break;
                    case "A":
                        builder.setDistributionStrategy(MasterDataFund.DistributionStrategy.DISTRIBUTING);
                        break;
                    default:
                        this.logger.error("<handleRow> unknown distribution strategy abbreviation: " + ds);
                        break;
                }
            }
            builder.setDistributionCount(getDistributionCount(get("DISTRIBUTIONCOUNT")));
            builder.setLastDistributionDate(getDateTime("LASTDISTRIBUTIONDATE"));
            builder.setLastDistribution(getBigDecimal("LASTDISTRIBUTION"));
            builder.setIssueSurcharge(getPercent("ISSUESURCHARGE"));
            builder.setManagementFee(getPercent("MANAGEMENTFEE"));
            builder.setRedemptionFee(getPercent("REDEMPTIONFEE"));
            builder.setFundVolume(getBigDecimal("FUNDVOLUME"));
            builder.setFundVolumeCurrency(get("FUNDVOLUMECURRENCY"));
            builder.setPermissionType(get("PERMISSIONTYPE"));
            builder.setFidaRating(get("FIDARATING"));
            builder.setOngoingCharge(getPercent("OGC"));
            builder.setSource(SOURCE);

            builder.setWithDefaultBenchmark(false); // as in VwdIt - todo: keep it?

            final MasterDataFund data = builder.build();
            this.values.put(data.getInstrumentid(), data);
        }
    }
}