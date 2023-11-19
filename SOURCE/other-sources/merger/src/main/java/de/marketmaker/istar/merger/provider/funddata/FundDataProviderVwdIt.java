/*
 * FundDataProviderImpl.java
 *
 * Created on 11.08.2006 18:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.funddata;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domainimpl.data.MasterDataFundImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundDataProviderVwdIt extends AbstractFundDataProvider {

    public static final MasterDataFund.Source SOURCE = MasterDataFund.Source.VWDIT;

    @Override
    protected IstarMdpExportReader<Map<Long, MasterDataFund>> createMasterDataReader() {
        return new MasterDataReader();
    }

    @Override
    protected IstarMdpExportReader<Map<Long, List<InstrumentAllocation>>> createAllocationsReader() {
        return new AllocationsReaderCommon(true, "DESCRIPTION", "TYPE", SOURCE).withInstrumentIdTag("IID");
    }

    private static class MasterDataReader extends IstarMdpExportReader<Map<Long, MasterDataFund>> {

        private final Map<Long, MasterDataFund> result = new HashMap<>();

        private MasterDataReader() {
            super(
                    "ANLAGESTRATEGIE",
                    "ANLAGESCHWERPUNKT",
                    "CURRENCY",
                    "FONDSTYP",
                    "KAG",
                    "BENCHMARKNAME"
            );
        }

        @Override
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
            builder.setPortfolioDate(getDateTime("PORTFOLIODATE"));
            builder.setTer(getBigDecimal("TER"));
            builder.setSource(SOURCE);

            builder.setWithDefaultBenchmark(false);
            this.result.put(iid, builder.build());
        }

        @Override
        protected Map<Long, MasterDataFund> getResult() {
            return this.result;
        }
    }

    public static void main(String[] args) throws Exception {
        final FundDataProviderVwdIt p = new FundDataProviderVwdIt();
        File dir = new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider");
        p.setMasterDataFile(new File(dir, "istar-vwdit-fund-masterdata.xml.gz"));
        p.setAllocationsFile(new File(dir, "istar-vwdit-fund-breakdown.xml.gz"));
        p.readMasterDataFund();
        p.readAllocations();
        System.out.println(p.getMasterData(132750089));
        System.out.println(p.getInstrumentAllocations(1233784));
    }
}