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
import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domainimpl.data.MasterDataFundImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundDataProviderUnion extends AbstractFundDataProvider {

    public static final MasterDataFund.Source SOURCE = MasterDataFund.Source.UNION;

    @Override
    protected IstarMdpExportReader<Map<Long, MasterDataFund>> createMasterDataReader() {
        return new MasterDataReaderUnion();
    }

    @Override
    protected IstarMdpExportReader<Map<Long, List<InstrumentAllocation>>> createAllocationsReader() {
        return new AllocationsReaderUnion(this.limitedRead);
    }

    private static class MasterDataReaderUnion extends IstarMdpExportReader<Map<Long, MasterDataFund>> {

        private final Map<Long, MasterDataFund> result = new HashMap<>();

        private MasterDataReaderUnion() {
            super("PORTFOLIODATE");
        }

        @Override
        protected void handleRow() {
            final MasterDataFundImpl.Builder builder = new MasterDataFundImpl.Builder();

            final Long iid = getLong("SECURITY");
            if (null == iid) {
                return;
            }
            builder.setInstrumentid(iid);
            builder.setPortfolioDate(getDateTime("PORTFOLIODATE"));
            builder.setSource(SOURCE);

            builder.setWithDefaultBenchmark(false);
            this.result.put(iid, builder.build());
        }

        @Override
        protected Map<Long, MasterDataFund> getResult() {
            return this.result;
        }
    }


    /**
     * @author Oliver Flege
     * @author Thomas Kiesgen
     */
    private static class AllocationsReaderUnion extends AllocationsReaderCommon {

        private static final Map<String, InstrumentAllocation.Type> TYPES = new HashMap<>();

        static {
            TYPES.put("Größte Positionen", InstrumentAllocation.Type.INSTRUMENT);
            TYPES.put("Anlage Typ", InstrumentAllocation.Type.ASSET);
            TYPES.put("Länder", InstrumentAllocation.Type.COUNTRY);
            TYPES.put("Währungen", InstrumentAllocation.Type.CURRENCY);
            TYPES.put("Branchen", InstrumentAllocation.Type.SECTOR);
        }

        public AllocationsReaderUnion(boolean limited) {
            super(limited, "BEZEICHNUNG", "AUFTEILUNGSART_BEZEICHNUNG", SOURCE);
            withShareTag("ANTEIL");
        }

        protected InstrumentAllocation.Type getType(String str) {
            return TYPES.get(str);
        }
    }

    public static void main(String[] args) throws Exception {
        final FundDataProviderUnion p = new FundDataProviderUnion();
        File dir = new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider");
        p.setAllocationsFile(new File(dir, "istar-vwd-breakdown.xml.gz"));
        p.afterPropertiesSet();
        System.out.println(p.getInstrumentAllocations(52337L));
    }
}