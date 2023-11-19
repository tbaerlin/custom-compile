/*
 * FundDataProviderImpl.java
 *
 * Created on 11.08.2006 18:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.bonddata;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.monitor.Resource;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.MasterDataBond;
import de.marketmaker.istar.domain.data.NullMasterDataBond;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BondDataProviderImpl implements BondDataProvider, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Long, MasterDataBond> masterData = new HashMap<>();

    private final Map<String, List<BenchmarkHistoryResponse.BenchmarkHistoryItem>> benchmarkHistory = new HashMap<>();

    private ActiveMonitor activeMonitor;

    private File masterDataFile;

    private File benchmarkHistoryFile;

    public void setMasterDataFile(File masterDataFile) {
        this.masterDataFile = masterDataFile;
    }

    public void setBenchmarkHistoryFile(File benchmarkHistoryFile) {
        this.benchmarkHistoryFile = benchmarkHistoryFile;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void afterPropertiesSet() throws Exception {
        readMasterData();
        readBenchmarkHistory();

        final FileResource masterDataResource = new FileResource(this.masterDataFile);
        masterDataResource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                readMasterData();
            }
        });
        this.activeMonitor.addResource(masterDataResource);

        if (this.benchmarkHistoryFile != null) {
            final FileResource benchmarkHistoryResource = new FileResource(this.benchmarkHistoryFile);
            masterDataResource.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    readBenchmarkHistory();
                }
            });
            this.activeMonitor.addResource(benchmarkHistoryResource);
        }
    }

    private void readMasterData() {
        final TimeTaker tt = new TimeTaker();
        try {
            final BondMasterDataReader ur = new BondMasterDataReader();
            ur.read(this.masterDataFile);

            synchronized (this) {
                this.masterData.clear();
                this.masterData.putAll(ur.getValues());
            }

            this.logger.info("<readMasterData> read " + this.masterDataFile.getName() + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readMasterData> failed", e);
        }
    }

    private void readBenchmarkHistory() {
        if (this.benchmarkHistoryFile == null) {
            return;
        }

        final TimeTaker tt = new TimeTaker();
        try {
            final BenchmarkHistoryReader r = new BenchmarkHistoryReader();
            r.read(this.benchmarkHistoryFile);

            synchronized (this) {
                this.benchmarkHistory.clear();
                this.benchmarkHistory.putAll(r.getValues());
            }

            this.logger.info("<readBenchmarkHistory> read " + this.benchmarkHistoryFile.getName() + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readBenchmarkHistory> failed", e);
        }
    }

    synchronized public MasterDataBond getMasterData(long instrumentid) {
        final MasterDataBond data = this.masterData.get(instrumentid);
        return data != null ? data : NullMasterDataBond.INSTANCE;
    }

    synchronized public BenchmarkHistoryResponse getBenchmarkHistory(
            BenchmarkHistoryRequest request) {
        final String symbol = request.getQuote().getSymbolVwdfeed();
        final List<BenchmarkHistoryResponse.BenchmarkHistoryItem> items = this.benchmarkHistory.get(symbol);

        final BenchmarkHistoryResponse response = new BenchmarkHistoryResponse(symbol, items);
        if (items == null) {
            response.setInvalid();
        }
        return response;
    }

    public static void main(String[] args) throws Exception {
        final BondDataProviderImpl p = new BondDataProviderImpl();
        p.setActiveMonitor(new ActiveMonitor());
        p.setMasterDataFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-wm-bond-masterdata.xml.gz"));
        p.setBenchmarkHistoryFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-benchmarkbonds.xml.gz"));
        p.afterPropertiesSet();

        final MasterDataBond masterData = p.getMasterData(1236L);
        System.out.println(masterData);

        final QuoteDp2 quote = new QuoteDp2(1);
        quote.setSymbol(KeysystemEnum.VWDFEED, "5.BMKAU01Y.TFI");
        final BenchmarkHistoryResponse response = p.getBenchmarkHistory(new BenchmarkHistoryRequest(quote));
        System.out.println(response);
        Thread.sleep(Long.MAX_VALUE);
    }
}
