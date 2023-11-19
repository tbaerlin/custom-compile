/*
 * FundDataProviderImpl.java
 *
 * Created on 11.08.2006 18:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.funddata;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.ratios.backend.XmlFieldsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.monitor.Resource;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.InstrumentAllocations;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.data.NullMasterDataFund;

import static java.util.stream.Collectors.toCollection;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundDataProviderMorningstar implements FundDataProvider, InitializingBean {
    public static final MasterDataFund.Source SOURCE = MasterDataFund.Source.MORNINGSTAR;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Long, MasterDataFund> masterData = new HashMap<>();

    private ActiveMonitor activeMonitor;

    private File masterDataFile;

    private AllocationsProviderMorningstar allocationsProvider;

    private AllocationsProviderMorningstar consolidatedAllocationsProvider;

    private FeriRatingProvider feriRatingProvider;

    public void setFeriRatingProvider(FeriRatingProvider feriRatingProvider) {
        this.feriRatingProvider = feriRatingProvider;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setMasterDataFile(File masterDataFile) {
        this.masterDataFile = masterDataFile;
    }

    public void setAllocationsProvider(AllocationsProviderMorningstar allocationsProvider) {
        this.allocationsProvider = allocationsProvider;
    }

    public void setConsolidatedAllocationsProvider(
            AllocationsProviderMorningstar consolidatedAllocationsProvider) {
        this.consolidatedAllocationsProvider = consolidatedAllocationsProvider;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.activeMonitor != null) {
            final FileResource masterDataResource = new FileResource(this.masterDataFile);
            masterDataResource.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    readMasterDataFund();
                }
            });

            this.activeMonitor.addResources(new Resource[]{masterDataResource});
        }

        readMasterDataFund();
    }

    private void readMasterDataFund() {
        final TimeTaker tt = new TimeTaker();
        try {
            final Map<Long, MasterDataFund> data =
                    new MasterDataReaderMorningstar().read(this.masterDataFile);

            synchronized (this) {
                this.masterData.clear();
                this.masterData.putAll(data);
            }

            this.logger.info("<readMasterDataFund> read from " + this.masterDataFile.getName()
                    + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readMasterDataFund> failed", e);
        }
    }

    private synchronized MasterDataFund getMasterData(long instrumentid) {
        final MasterDataFund data = this.masterData.get(instrumentid);
        return data != null ? data : NullMasterDataFund.INSTANCE;
    }

    synchronized public InstrumentAllocations getInstrumentAllocations(long instrumentid,
            boolean withConsolidatedAllocations) {
        final InstrumentAllocations result = withConsolidatedAllocations
                ? this.consolidatedAllocationsProvider.getAllocations(instrumentid)
                : this.allocationsProvider.getAllocations(instrumentid);
        return result != null ? result : InstrumentAllocations.NULL;
    }

    public FundDataResponse getFundData(FundDataRequest request) {
        // TODO refactor after general update
        final FundDataResponse response = new FundDataResponse();
        if (request.isWithAllocations() || request.isWithConsolidatedAllocations()) {
            final List<InstrumentAllocations> list = new ArrayList<>();
            for (final Long iid : request.getInstrumentids()) {
                list.add(getInstrumentAllocations(iid, request.isWithConsolidatedAllocations()));
            }
            response.setInstrumentAllocationses(list);
        }
        if (request.isWithBenchmarks()) {
            response.setBenchmarksList(Collections.nCopies(request.getInstrumentids().size(),
                    Collections.<InstrumentAllocation>emptyList()));
        }
        if (request.isWithFeriPerformances()) {
            response.setFeriPerformanceses(Collections.nCopies(request.getInstrumentids().size(),
                    new FeriPerformances()));
        }
        if (request.isWithFeriRating()) {
            final List<String> ratings = new ArrayList<>(request.getInstrumentids().size());
            for (final Long iid : request.getInstrumentids()) {
                ratings.add(this.feriRatingProvider.getFeriRating(iid));
            }
            response.setFeriRatings(ratings);
        }
        if (request.isWithMasterData()) {
            final List<MasterDataFund> list = new ArrayList<>();
            for (final Long iid : request.getInstrumentids()) {
                list.add(getMasterData(iid));
            }
            response.setMasterDataFunds(list);
        }
        addMSFromMasterData(request, response);

        return response;
    }

    private void addMSFromMasterData(FundDataRequest request, FundDataResponse response) {
        final boolean morningstarsOnlyDz = !request.isWithMorningstarRating() && request.isWithMorningstarRatingDz();

        if (request.isWithMorningstarRating() || request.isWithMorningstarRatingDz()) {
            response.setMorningstarRatings(
                    request.getInstrumentids().stream().map(this::getMasterData).map(mdf -> getMSRating(mdf, morningstarsOnlyDz))
                            .collect(toCollection(ArrayList::new))
            );
        }
    }

    private Integer getMSRating(MasterDataFund data, boolean morningstarsOnlyDz) {
        final String ratingStr = data.getMorningstarOverallRating();
        if (ratingStr == null) {
            return null;
        }

        final boolean vrIssuer = data.getIssuerName() != null && XmlFieldsReader.checkIssuer(data.getIssuerName().getDe(), XmlFieldsReader.UNION_INVEST_KVG_PREFIX);
        return !morningstarsOnlyDz || vrIssuer ? Integer.parseInt(ratingStr) : null;
    }
}
