/*
 * AbstractFundDataProvider.java
 *
 * Created on 19.12.13 13:56
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.funddata;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.monitor.Resource;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.InstrumentAllocations;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.data.NullMasterDataFund;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for FundDataProvider implementations
 * @author oflege
 */
abstract public class AbstractFundDataProvider implements FundDataProvider, InitializingBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected Map<Long, MasterDataFund> masterData = Collections.emptyMap();

    protected Map<Long, List<InstrumentAllocation>> allocations = Collections.emptyMap();

    protected ActiveMonitor activeMonitor;

    protected File allocationsFile;

    protected File masterDataFile;

    protected boolean limitedRead = true;

    public void setAllocationsFile(File allocationsFile) {
        this.allocationsFile = allocationsFile;
    }

    public void setLimitedRead(boolean limitedRead) {
        this.limitedRead = limitedRead;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setMasterDataFile(File masterDataFile) {
        this.masterDataFile = masterDataFile;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.activeMonitor != null) {
            ArrayList<Resource> resources = getResourcesToMonitor();
            this.activeMonitor.addResources(resources.toArray(new Resource[resources.size()]));
        }

        if (this.masterDataFile != null) {
            readMasterDataFund();
        }

        if (this.allocationsFile != null) {
            readAllocations();
        }
    }

    protected ArrayList<Resource> getResourcesToMonitor() {
        ArrayList<Resource> result = new ArrayList<>();
        if (this.masterDataFile != null) {
            FileResource fr = new FileResource(this.masterDataFile);
            fr.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    readMasterDataFund();
                }
            });
            result.add(fr);
        }
        if (this.allocationsFile != null) {
            FileResource fr = new FileResource(this.allocationsFile);
            fr.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    readAllocations();
                }
            });
            result.add(fr);
        }
        return result;
    }

    protected abstract IstarMdpExportReader<Map<Long, MasterDataFund>> createMasterDataReader();

    protected abstract IstarMdpExportReader<Map<Long, List<InstrumentAllocation>>> createAllocationsReader();

    protected Map<Long, List<InstrumentAllocation>> doReadAllocations() throws Exception {
        return createAllocationsReader().read(this.allocationsFile);
    }

    protected Map<Long, MasterDataFund> doReadMasterData() throws Exception {
        return createMasterDataReader().read(this.masterDataFile);
    }

    protected void readMasterDataFund() {
        final TimeTaker tt = new TimeTaker();
        try {
            final Map<Long, MasterDataFund> data = doReadMasterData();

            synchronized (this) {
                this.masterData = data;
            }
            this.logger.info("<readMasterDataFund> read from " + this.masterDataFile.getName()
                    + ", took " + tt);

        } catch (Exception e) {
            this.logger.error("<readMasterDataFund> failed for " + this.masterDataFile.getName(), e);
        }
    }

    protected void readAllocations() {
        final TimeTaker tt = new TimeTaker();
        try {
            final Map<Long, List<InstrumentAllocation>> result = doReadAllocations();
            removeEmptyAllocations(result);

            synchronized (this) {
                this.allocations = result;
            }
            this.logger.info("<readAllocations> read allocations from " + this.allocationsFile.getName()
                    + ", took " + tt);

        } catch (Exception e) {
            this.logger.error("<readAllocations> failed for " + this.allocationsFile.getName(), e);
        }
    }

    private void removeEmptyAllocations(Map<Long, List<InstrumentAllocation>> result) {
        for (Iterator<List<InstrumentAllocation>> it = result.values().iterator(); it.hasNext(); ) {
            if (it.next().isEmpty()) {
                it.remove();
            }
        }
    }

    public FundDataResponse getFundData(FundDataRequest req) {
        final FundDataResponse response = new FundDataResponse();
        if (req.isWithAllocations() || req.isWithConsolidatedAllocations()) {
            response.setInstrumentAllocationses(getInstrumentAllocations(req));
        }
        if (req.isWithBenchmarks()) {
            response.setBenchmarksList(getBenchmarksList(req));
        }
        if (req.isWithFeriPerformances()) {
            response.setFeriPerformanceses(getFeriPerformanceses(req));
        }
        if (req.isWithFeriRating()) {
            response.setFeriRatings(getFeriRatings(req));
        }
        if (req.isWithMasterData()) {
            response.setMasterDataFunds(getMasterData(req));
        }
        if (req.isWithMorningstarRating()) {
            response.setMorningstarRatings(getMorningstarRatings(req));
        }
        return response;
    }

    protected List<InstrumentAllocations> getInstrumentAllocations(FundDataRequest req) {
        final List<InstrumentAllocations> result = new ArrayList<>();
        for (final Long iid : req.getInstrumentids()) {
            result.add(getInstrumentAllocations(iid));
        }
        return result;
    }

    protected List<List<InstrumentAllocation>> getBenchmarksList(FundDataRequest req) {
        return Collections.nCopies(req.getInstrumentids().size(),
                Collections.<InstrumentAllocation>emptyList());
    }

    protected List<FeriPerformances> getFeriPerformanceses(FundDataRequest req) {
        return Collections.nCopies(req.getInstrumentids().size(), new FeriPerformances());
    }

    protected List<String> getFeriRatings(FundDataRequest req) {
        return Collections.nCopies(req.getInstrumentids().size(), (String) null);
    }

    protected List<MasterDataFund> getMasterData(FundDataRequest req) {
        final List<MasterDataFund> result = new ArrayList<>();
        for (final Long iid : req.getInstrumentids()) {
            result.add(getMasterData(iid));
        }
        return result;
    }

    protected List<Integer> getMorningstarRatings(FundDataRequest req) {
        return Collections.nCopies(req.getInstrumentids().size(), (Integer) null);
    }

    protected synchronized MasterDataFund getMasterData(long instrumentid) {
        final MasterDataFund data = this.masterData.get(instrumentid);
        return data != null ? data : NullMasterDataFund.INSTANCE;
    }

    protected synchronized InstrumentAllocations getInstrumentAllocations(long instrumentid) {
        final List<InstrumentAllocation> result = this.allocations.get(instrumentid);
        if (result == null) {
            return InstrumentAllocations.NULL;
        }
        final MasterDataFund md = this.masterData.get(instrumentid);
        return (null != md)
                ? InstrumentAllocations.create(md.getMarketAdmission(), result)
                : InstrumentAllocations.create(result);

    }
}
