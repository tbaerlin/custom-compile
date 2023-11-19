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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.monitor.Resource;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.InstrumentAllocations;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.data.NullMasterDataFund;
import de.marketmaker.istar.domainimpl.data.InstrumentAllocationImpl;
import de.marketmaker.istar.domainimpl.data.MasterDataFundImpl;
import de.marketmaker.istar.merger.provider.ReportProviderImpl;
import de.marketmaker.istar.ratios.backend.XmlFieldsReader;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundDataProviderFww implements FundDataProvider, InitializingBean {

    public static final MasterDataFund.Source SOURCE = MasterDataFund.Source.FWW;

    private static class MasterDataReader extends IstarMdpExportReader<Map<Long, MasterDataFund>> {
        private static final BigDecimal MILLION = BigDecimal.valueOf(1000000);

        private final Map<Long, MasterDataFund> result = new HashMap<>();

        public MasterDataReader() {
            super(true, "ANLAGESCHWERPUNKT", "FUNDTYPE", "KAG", "BENCHMARKNAME",
                    "SEKTORNAME", "DISTRIBUTIONCURRENCY", "CURRENCY", "COUNTRY");
        }

        protected Map<Long, MasterDataFund> getResult() {
            return this.result;
        }

        protected void handleRow() {
            final MasterDataFundImpl.Builder builder = new MasterDataFundImpl.Builder();

            final Long iid = getLong("INSTRUMENTID", "IID");
            if (iid == null) {
                return;
            }
            builder.setInstrumentid(iid);
            builder.setStrategy(get("ANLAGESTRATEGIE"), Language.de);
            builder.setInvestmentFocus(get("ANLAGESCHWERPUNKT"), Language.de);
            builder.setFundtype(get("FUNDTYPE"), Language.de);
            builder.setIssuerName(get("KAG"), Language.de);
            builder.setBenchmarkName(get("BENCHMARKNAME"), Language.de);
            builder.setFundManager(get("FONDSMANAGER"), Language.de);
            builder.setSector(get("SEKTORNAME"), Language.de);
            builder.setIssueDate(getDateTime("AUFLAGEDATUM"));
            builder.setReportDate(getDateTime("REPORTDATE"));
            final BigDecimal vol = getBigDecimal("FONDSVOLUMEN");
            if (vol != null) {
                builder.setFundVolume(vol.multiply(MILLION));
            }
            builder.setIssueSurcharge(getBigDecimal("AUSGABEAUFSCHLAG"));
            final Integer aatyp = getInt("AUSGABEAUFSCHLAGTYP");
            if (aatyp != null) {
                builder.setIssueSurchargeType(getSurchargeType(aatyp));
            }
            builder.setIssueSurchargeNet(getBigDecimal("AUSGABEAUFSCHLAGNETTO"));
            builder.setIssueSurchargeGross(getBigDecimal("AUSGABEAUFSCHLAGBRUTTO"));
            builder.setManagementFee(getBigDecimal("MANAGEMENTFEE"));
            builder.setAccountFee(getBigDecimal("DEPOTBANKGEBUEHR"));
            builder.setAllinFee(getBigDecimal("ALLINFEE"));
            builder.setTer(getBigDecimal("TER"));
            final String art = get("AUSCHUETTUNGSART");
            if (art != null) {
                builder.setDistributionStrategy("1".equals(art)
                        ? MasterDataFund.DistributionStrategy.RETAINING
                        : MasterDataFund.DistributionStrategy.DISTRIBUTING);
            }
            builder.setLastDistribution(getBigDecimal("LASTDISTRIBUTION"));
            builder.setDistributionCurrency(get("DISTRIBUTIONCURRENCY"));
            builder.setLastDistributionDate(getDateTime("LASTDISTRIBUTIONDATE"));
            builder.setFwwRiskclass(getInt("FWWRISKCLASS"));
            builder.setCustomer(get("CUSTOMER"));
            builder.setCurrency(get("CURRENCY"));
            builder.setIssuerPhone(get("PHONE"));
            builder.setCountry(get("COUNTRY"), Language.de);
            builder.setSource(SOURCE);

            if (!builder.isValid()) {
                return;
            }

            final MasterDataFund data = builder.build();
            this.result.put(data.getInstrumentid(), data);
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
    }

    private static class BenchmarkReader extends
            IstarMdpExportReader<Map<Long, List<InstrumentAllocation>>> {
        final Map<Long, List<InstrumentAllocation>> result = new HashMap<>();

        protected Map<Long, List<InstrumentAllocation>> getResult() {
            return this.result;
        }

        protected void handleRow() {
            final Long fondsiid = getLong("FONDSIID");
            final Long benchmarkiid = getLong("BENCHMARKIID");

            if (fondsiid == null || benchmarkiid == null) {
                return;
            }

            final BigDecimal share = getPercent("SHARE_");

            final InstrumentAllocation ia
                    = new InstrumentAllocationImpl(InstrumentAllocation.Type.INSTRUMENT, benchmarkiid, null, share);
            List<InstrumentAllocation> ias = this.result.get(fondsiid);
            if (ias == null) {
                ias = new ArrayList<>();
                this.result.put(fondsiid, ias);
            }
            ias.add(ia);
        }
    }

    private static class TopholdingsReader extends
            IstarMdpExportReader<Map<Long, List<InstrumentAllocation>>> {
        final Map<Long, List<InstrumentAllocation>> values = new HashMap<>();

        public TopholdingsReader() {
            super(true, "INVESTMENTNAME");
        }

        protected Map<Long, List<InstrumentAllocation>> getResult() {
            return this.values;
        }

        protected void handleRow() {
            final Long instrumentid = getLong("FUNDINSTRUMENTID");
            final Long investmentinstrumentid = getLong("INVESTMENTINSTRUMENTID");
            final String category = get("INVESTMENTNAME");
            final BigDecimal share = getPercent("SHARE_");

            if (instrumentid == null) {
                return;
            }

            final InstrumentAllocation ia = new InstrumentAllocationImpl(InstrumentAllocation.Type.INSTRUMENT,
                    investmentinstrumentid, category, share);
            List<InstrumentAllocation> ias = this.values.get(instrumentid);
            if (ias == null) {
                ias = new ArrayList<>();
                this.values.put(instrumentid, ias);
            }
            ias.add(ia);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Long, MasterDataFund> masterData = new HashMap<>();

    private final Map<Long, List<InstrumentAllocation>> allocations = new HashMap<>();

    private final Map<Long, List<InstrumentAllocation>> benchmarks = new HashMap<>();

    private ActiveMonitor activeMonitor;

    private File masterDataFile;

    private File allocationsFile;

    private File topholdingsFile;

    private File benchmarkFile;

    private MorningstarRatingProvider morningstarRatingProvider;

    public void setMorningstarRatingProvider(MorningstarRatingProvider morningstarRatingProvider) {
        this.morningstarRatingProvider = morningstarRatingProvider;
    }

    public void setBenchmarkFile(File benchmarkFile) {
        this.benchmarkFile = benchmarkFile;
    }

    public void setMasterDataFile(File masterDataFile) {
        this.masterDataFile = masterDataFile;
    }

    public void setAllocationsFile(File allocationsFile) {
        this.allocationsFile = allocationsFile;
    }

    public void setTopholdingsFile(File topholdingsFile) {
        this.topholdingsFile = topholdingsFile;
    }

    public void setActiveMonitor(ActiveMonitor monitor) {
        this.activeMonitor = monitor;
    }

    public void afterPropertiesSet() throws Exception {
        readBenchmarks();
        readMasterDataFund();
        readAllocations();
        readTopholdings();

        final FileResource masterDataResource = new FileResource(this.masterDataFile);
        masterDataResource.addPropertyChangeListener(evt -> readMasterDataFund());

        final FileResource allocationsResource = new FileResource(this.allocationsFile);
        masterDataResource.addPropertyChangeListener(evt -> readAllocations());

        final FileResource topholdindsResource = new FileResource(this.topholdingsFile);
        masterDataResource.addPropertyChangeListener(evt -> readTopholdings());


        final FileResource benchmarkResource = new FileResource(this.benchmarkFile);
        benchmarkResource.addPropertyChangeListener(evt -> readBenchmarks());

        this.activeMonitor.addResources(new Resource[]{
                masterDataResource, allocationsResource, topholdindsResource, benchmarkResource
        });
    }

    void readMasterDataFund() {
        final TimeTaker tt = new TimeTaker();
        try {
            final Map<Long, MasterDataFund> map = new MasterDataReader().read(this.masterDataFile);
            synchronized (this) {
                this.masterData.clear();
                this.masterData.putAll(map);
            }

            this.logger.info("<readMasterDataFund> read " + this.masterDataFile.getName() + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readMasterDataFund> failed", e);
        }
    }

    void readBenchmarks() {
        final TimeTaker tt = new TimeTaker();
        try {
            final Map<Long, List<InstrumentAllocation>> values
                    = new BenchmarkReader().read(this.benchmarkFile);

            synchronized (this) {
                this.benchmarks.clear();
                this.benchmarks.putAll(values);
            }

            this.logger.info("<readBenchmarks> read " + this.benchmarkFile.getName() + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readBenchmarks> failed", e);
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "REC", justification = "catch all exceptions")
    void readAllocations() {
        final TimeTaker tt = new TimeTaker();
        try {
            final Map<Long, List<InstrumentAllocation>> values
                    = new AllocationsReaderCommon(true, "CATEGORY", "TYPE", SOURCE)
                    .withInstrumentIdTag("INSTRUMENTID").read(this.allocationsFile);
            synchronized (this) {
                for (final Map.Entry<Long, List<InstrumentAllocation>> entry : this.allocations.entrySet()) {
                    // remove all types of this update
                    for (final Iterator<InstrumentAllocation> it = entry.getValue().iterator(); it.hasNext(); ) {
                        final InstrumentAllocation ia = it.next();
                        if (ia.getType() == InstrumentAllocation.Type.INSTRUMENT) {
                            continue;
                        }
                        it.remove();
                    }

                    // add all new types
                    final List<InstrumentAllocation> ias = values.remove(entry.getKey());
                    if (ias != null) {
                        entry.getValue().addAll(ias);
                    }
                }

                // above usage of 'remove' instead of 'get' leaves only the new ones in the values map
                // => just add them all
                this.allocations.putAll(values);

                for (final Iterator<Map.Entry<Long, List<InstrumentAllocation>>> it = this.allocations.entrySet().iterator(); it.hasNext(); ) {
                    final Map.Entry<Long, List<InstrumentAllocation>> entry = it.next();
                    if (entry.getValue().isEmpty()) {
                        it.remove();
                    }
                }
            }

            this.logger.info("<readAllocations> read " + this.allocationsFile.getName() + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readAllocations> failed", e);
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "REC", justification = "catch all exceptions")
    void readTopholdings() {
        final TimeTaker tt = new TimeTaker();
        try {
            final Map<Long, List<InstrumentAllocation>> values
                    = new TopholdingsReader().read(this.topholdingsFile);

            synchronized (this) {
                for (final Map.Entry<Long, List<InstrumentAllocation>> entry : this.allocations.entrySet()) {
                    // remove all types of this update
                    for (final Iterator<InstrumentAllocation> it = entry.getValue().iterator(); it.hasNext(); ) {
                        final InstrumentAllocation ia = it.next();
                        if (ia.getType() != InstrumentAllocation.Type.INSTRUMENT) {
                            continue;
                        }
                        it.remove();
                    }

                    // add all new types
                    final List<InstrumentAllocation> ias = values.remove(entry.getKey());
                    if (ias != null) {
                        entry.getValue().addAll(ias);
                    }
                }

                // above usage of 'remove' instead of 'get' leaves only the new ones in the values map
                // => just add them all
                this.allocations.putAll(values);

                for (final Iterator<Map.Entry<Long, List<InstrumentAllocation>>> it = this.allocations.entrySet().iterator(); it.hasNext(); ) {
                    final Map.Entry<Long, List<InstrumentAllocation>> entry = it.next();
                    if (entry.getValue().isEmpty()) {
                        it.remove();
                    }
                }
            }

            this.logger.info("<readTopholdings> read " + this.topholdingsFile.getName() + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readTopholdings> failed", e);
        }
    }

    synchronized MasterDataFund getMasterData(long instrumentid) {
        final MasterDataFund data = this.masterData.get(instrumentid);
        return data != null ? data : NullMasterDataFund.INSTANCE;
    }

    synchronized public InstrumentAllocations getInstrumentAllocations(long instrumentid) {
        final List<InstrumentAllocation> ias = this.allocations.get(instrumentid);
        return InstrumentAllocations.create(ias);
    }

    synchronized public List<InstrumentAllocation> getBenchmarks(long instrumentid) {
        final List<InstrumentAllocation> ias = this.benchmarks.get(instrumentid);
        if (ias == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(ias);
    }

    public Integer getMorningstarRating(long instrumentid) {
        return this.morningstarRatingProvider.getMorningstarRating(instrumentid);
    }

    public FundDataResponse getFundData(FundDataRequest request) {
        // TODO refactor after general update
        final FundDataResponse response = new FundDataResponse();
        if (request.isWithAllocations() || request.isWithConsolidatedAllocations()) {
            response.setInstrumentAllocationses(request.getInstrumentids().stream().map(this::getInstrumentAllocations).collect(Collectors.toList()));
        }
        if (request.isWithBenchmarks()) {
            response.setBenchmarksList(request.getInstrumentids().stream().map(this::getBenchmarks).collect(Collectors.toList()));
        }
        if (request.isWithFeriPerformances()) {
            final List<FeriPerformances> list = new ArrayList<>();
            for (final Long iid : request.getInstrumentids()) {
                list.add(new FeriPerformances());
            }
            response.setFeriPerformanceses(list);
        }
        if (request.isWithFeriRating()) {
            final List<String> list = new ArrayList<>();
            for (final Long iid : request.getInstrumentids()) {
                list.add(null);
            }
            response.setFeriRatings(list);
        }
        if (request.isWithMasterData()) {
            response.setMasterDataFunds(request.getInstrumentids().stream().map(this::getMasterData).collect(Collectors.toList()));
        }

        final boolean ratingsOnlyDz = !request.isWithMorningstarRating() && request.isWithMorningstarRatingDz();
        if (request.isWithMorningstarRating() || request.isWithMorningstarRatingDz()) {
            final List<Integer> list = new ArrayList<>();
            for (final Long iid : request.getInstrumentids()) {
                final MasterDataFund data = getMasterData(iid);

                final boolean vrIssuer = data.getIssuerName() != null && XmlFieldsReader.checkIssuer(data.getIssuerName().getDe(), XmlFieldsReader.UNION_INVEST_KVG_PREFIX);
                final Integer rating = !ratingsOnlyDz || vrIssuer ? getMorningstarRating(iid) : null;

                list.add(rating);
            }
            response.setMorningstarRatings(list);
        }

        return response;
    }

    public static void main(String[] args) throws Exception {
        final FundDataProviderFww p = new FundDataProviderFww();
        File dir = new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider");
        p.setMasterDataFile(new File(dir, "istar-fww-fund-masterdata.xml.gz"));
        p.setAllocationsFile(new File(dir, "istar-fww-fund-allocations.xml.gz"));
        p.setTopholdingsFile(new File(dir, "istar-fww-fund-topholdings.xml.gz"));
        p.setBenchmarkFile(new File(dir, "istar-fww-fund-benchmark.xml.gz"));
        final ReportProviderImpl rp = new ReportProviderImpl();
        rp.setFile(new File(dir, "istar-fww-fund-reports.xml.gz"));
        rp.afterPropertiesSet();
        final ActiveMonitor monitor = new ActiveMonitor();
        monitor.start();
        p.setActiveMonitor(monitor);
        p.afterPropertiesSet();
        final MasterDataFund masterData = p.getMasterData(52112);
        System.out.println(masterData);
        final InstrumentAllocations ias = p.getInstrumentAllocations(100594);
        System.out.println(ias);
        System.out.println("benchmarks:" + p.getBenchmarks(52112L));

        System.out.println("INSTRUMENT");
        final List<InstrumentAllocation> i = ias.getAllocations(InstrumentAllocation.Type.INSTRUMENT);
        for (final InstrumentAllocation instrumentAllocation : i) {
            System.out.println(instrumentAllocation.getCategory() + ": " + instrumentAllocation.getShare());
        }

        System.out.println("SEKTOR");
        final List<InstrumentAllocation> s = ias.getAllocations(InstrumentAllocation.Type.SECTOR);
        for (final InstrumentAllocation instrumentAllocation : s) {
            System.out.println(instrumentAllocation.getCategory() + ": " + instrumentAllocation.getShare());
        }

        System.out.println("ASSET");
        final List<InstrumentAllocation> pb = ias.getAllocations(InstrumentAllocation.Type.ASSET);
        for (final InstrumentAllocation instrumentAllocation : pb) {
            System.out.println(instrumentAllocation.getCategory() + ": " + instrumentAllocation.getShare());
        }

        Thread.sleep(Long.MAX_VALUE);
    }
}
