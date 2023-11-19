/*
 * FundDataProviderImpl.java
 *
 * Created on 11.08.2006 18:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.stockdata;

import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.STK;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.monitor.MeterSupport;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.MasterDataStock;
import de.marketmaker.istar.domain.data.NullMasterDataStock;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.MasterDataStockImpl;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.xml.sax.SAXException;

/**
 * Read and manage Stock static data.
 * Currently there are two sources for this data. WM and GICS. These are in two separate files.
 * If one of these files is updated <b>both</b> files will be read again, data will be merged and
 * the common map will be updated.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author mcoenen
 */
public class StockDataProviderImpl implements StockDataProvider, InitializingBean {
    private final static DateTimeFormatter DTF_DATE = DateTimeFormat.forPattern("dd.MM.yyyy");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile Map<Long, MasterDataStock> masterData = Collections.emptyMap();

    private File wmDataFile;

    private File gicsDataFile;

    private File esmaDataFile;

    private ActiveMonitor activeMonitor;

    private CompanyDataProvider companyDataProvider;

    private MeterRegistry meterRegistry;

    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setWmDataFile(File wmDataFile) {
        this.wmDataFile = wmDataFile;
    }

    public void setGicsDataFile(File gicsDataFile) {
        this.gicsDataFile = gicsDataFile;
    }

    public void setEsmaDataFile(File esmaDataFile) {
        this.esmaDataFile = esmaDataFile;
    }

    public void setCompanyDataProvider(CompanyDataProvider companyDataProvider) {
        this.companyDataProvider = companyDataProvider;
    }

    public void afterPropertiesSet() throws Exception {
        final FileResource wmDataResource = new FileResource(this.wmDataFile);
        wmDataResource.addPropertyChangeListener(evt -> readMasterData());

        final FileResource gicsDataResource = new FileResource(this.gicsDataFile);
        gicsDataResource.addPropertyChangeListener(evt -> readMasterData());

        final FileResource esmaDataResource = new FileResource(this.esmaDataFile);
        esmaDataResource.addPropertyChangeListener(evt -> readMasterData());

        this.activeMonitor.addResource(wmDataResource);
        this.activeMonitor.addResource(gicsDataResource);
        this.activeMonitor.addResource(esmaDataResource);

        readMasterData();
    }

    synchronized void readMasterData() {
        final TimeTaker tt = new TimeTaker();
        try {
            final Map<Long, MasterDataStock> entries = new HashMap<>();
            final MasterDataReader ur = new MasterDataReader(entries);
            ur.setMeterRegistry(this.meterRegistry);
            if (this.wmDataFile.exists()) {
                ur.read(this.wmDataFile);
                this.logger.info("<readMasterData> read " + this.wmDataFile.getName() + ", took " + tt);
                tt.start();
            }
            if (this.gicsDataFile.exists()) {
                ur.read(this.gicsDataFile);
                this.logger.info("<readMasterData> read " + this.gicsDataFile.getName() + ", took " + tt);
            }
            if (this.esmaDataFile.exists()) {
                ur.read(this.esmaDataFile);
                this.logger.info("<readMasterData> read " + this.esmaDataFile.getName() + ", took " + tt);
            }

            this.masterData = entries.isEmpty() ? Collections.emptyMap() : entries;
        }
        catch (Exception e) {
            this.logger.error("<readMasterData> failed", e);
        }
    }

    @Override
    public List<MasterDataStock> getMasterData(List<Long> iids, Profile profile) {
        return MeterSupport.record(this.meterRegistry, "master.data.list",
            () -> getMasterDataInternal(iids, profile), Tags.of("type", STK.name()));
    }

    private List<MasterDataStock> getMasterDataInternal(List<Long> iids, Profile profile) {
        return iids.stream()
            .map(iid -> iid == null ? NullMasterDataStock.INSTANCE
                : getMasterData(iid, profile))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public MasterDataStock getMasterData(long iid, Profile profile) {
        final boolean hasGicAccess = profile.isAllowed(Selector.SP_GICS_DIRECT);
        return MeterSupport.record(this.meterRegistry, "master.data.get",
            () -> getMasterDataInternal(iid, hasGicAccess), Tags.of("type", STK.name()));
    }

    private MasterDataStock getMasterDataInternal(long iid, boolean hasGicAccess) {
        MasterDataStock masterData =
            this.masterData.getOrDefault(iid, NullMasterDataStock.INSTANCE);
        if (masterData != NullMasterDataStock.INSTANCE && !hasGicAccess){
            masterData = filterGicFields(masterData);
        }
        return masterData;
    }

    private MasterDataStock filterGicFields(MasterDataStock masterData) {
        return new MasterDataStockImpl(
            masterData.getInstrumentid(),
            masterData.getDividend(),
            masterData.getDividendLastYear(),
            masterData.getDividendCurrency(),
            masterData.getDividendExDay(),
            masterData.getSector(),
            null,
            null,
            null,
            null,
            LocalizedString.NULL_LOCALIZED_STRING,
            LocalizedString.NULL_LOCALIZED_STRING,
            LocalizedString.NULL_LOCALIZED_STRING,
            LocalizedString.NULL_LOCALIZED_STRING,
            masterData.getMostLiquidMarket()
        );
    }

    public class MasterDataReader extends AbstractSaxReader {

        private final Map<Long, MasterDataStock> entries;

        private long instrumentid = -1L;

        // WM Data
        private BigDecimal dividend;
        private BigDecimal dividendLastYear;
        private String dividendCurrency;
        private LocalDate dividendExDay;
        private final LocalizedString.Builder sector = new LocalizedString.Builder();

        // GICS Data
        private String gicsSectorKey;
        private String gicsIndustryGroupKey;
        private String gicsIndustryKey;
        private String gicsSubIndustryKey;
        private final LocalizedString.Builder gicsSector = new LocalizedString.Builder();
        private final LocalizedString.Builder gicsIndustryGroup = new LocalizedString.Builder();
        private final LocalizedString.Builder gicsIndustry = new LocalizedString.Builder();
        private final LocalizedString.Builder gicsSubIndustry = new LocalizedString.Builder();

        // ESMA data
        private String mostLiquidMarket;

        public MasterDataReader(
                Map<Long, MasterDataStock> entries) {
            this.entries = entries;
        }

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if ("ROW".equalsIgnoreCase(tagName)) {
                    storeFields();
                }
                else if ("IID".equalsIgnoreCase(tagName)) {
                    this.instrumentid = getCurrentLong();
                }
                else if ("ROWS".equalsIgnoreCase(tagName)) {
                    // ignored
                }
                else if (!handleAsWMTag(tagName) && !handleAsGICSTag(tagName) && !handleAsESMATag(tagName)) {
                    notParsed(tagName);
                }
            }
            catch (Exception e) {
                this.logger.error("<endElement> error in " + tagName, e);
                this.errorOccured = true;
            }
        }

        private boolean handleAsWMTag(String tagName) {
            switch (tagName) {
                case "DIVIDEND":
                    this.dividend = getCurrentBigDecimal();
                    return true;
                case "DIVIDENDLASTYEAR":
                    this.dividendLastYear = getCurrentBigDecimal();
                    return true;
                case "DIVIDENDCURRENCY":
                    this.dividendCurrency = getCurrentString();
                    return true;
                case "DIVIDENDEXDAY":
                    this.dividendExDay = DTF_DATE.parseDateTime(getCurrentString(false)).toLocalDate();
                    return true;
                case "SECTOR":
                case "SECTOR__DE":
                    this.sector.add(getCurrentString(), Language.de);
                    return true;
                case "SECTOR_EN":
                case "SECTOR__EN":
                    this.sector.add(getCurrentString(), Language.en);
                    return true;
                default:
                    return false;
            }
        }

        private boolean handleAsGICSTag(String tagName) {

            switch (tagName) {
                case "GICSSECTORKEY":
                    this.gicsSectorKey = getCurrentString();
                    return true;
                case "GICSINDUSTRYGROUPKEY":
                    this.gicsIndustryGroupKey = getCurrentString();
                    return true;
                case "GICSINDUSTRYKEY":
                    this.gicsIndustryKey = getCurrentString();
                    return true;
                case "GICSSUBINDUSTRYKEY":
                    this.gicsSubIndustryKey = getCurrentString();
                    return true;

                case "GICSSECTOR__DE":
                    this.gicsSector.add(getCurrentString(), Language.de);
                    return true;
                case "GICSINDUSTRYGROUP__DE":
                    this.gicsIndustryGroup.add(getCurrentString(), Language.de);
                    return true;
                case "GICSINDUSTRY__DE":
                    this.gicsIndustry.add(getCurrentString(), Language.de);
                    return true;
                case "GICSSUBINDUSTRY__DE":
                    this.gicsSubIndustry.add(getCurrentString(), Language.de);
                    return true;

                case "GICSSECTOR__EN":
                    this.gicsSector.add(getCurrentString(), Language.en);
                    return true;
                case "GICSINDUSTRYGROUP__EN":
                    this.gicsIndustryGroup.add(getCurrentString(), Language.en);
                    return true;
                case "GICSINDUSTRY__EN":
                    this.gicsIndustry.add(getCurrentString(), Language.en);
                    return true;
                case "GICSSUBINDUSTRY__EN":
                    this.gicsSubIndustry.add(getCurrentString(), Language.en);
                    return true;

                case "GICSSECTOR__FR":
                    this.gicsSector.add(getCurrentString(), Language.fr);
                    return true;
                case "GICSINDUSTRYGROUP__FR":
                    this.gicsIndustryGroup.add(getCurrentString(), Language.fr);
                    return true;
                case "GICSINDUSTRY__FR":
                    this.gicsIndustry.add(getCurrentString(), Language.fr);
                    return true;
                case "GICSSUBINDUSTRY__FR":
                    this.gicsSubIndustry.add(getCurrentString(), Language.fr);
                    return true;

                case "GICSSECTOR__IT":
                    this.gicsSector.add(getCurrentString(), Language.it);
                    return true;
                case "GICSINDUSTRYGROUP__IT":
                    this.gicsIndustryGroup.add(getCurrentString(), Language.it);
                    return true;
                case "GICSINDUSTRY__IT":
                    this.gicsIndustry.add(getCurrentString(), Language.it);
                    return true;
                case "GICSSUBINDUSTRY__IT":
                    this.gicsSubIndustry.add(getCurrentString(), Language.it);
                    return true;
                default:
                    return false;
            }
        }

        private boolean handleAsESMATag(String tagName) {
            switch (tagName) {
                case "MOSTLIQUIDMARKET":
                    this.mostLiquidMarket = getCurrentString();
                    return true;
                default:
                    return false;
            }
        }

        private void storeFields() {
            this.limiter.ackAction();

            if (this.errorOccured) {
                reset();
                return;
            }

            if (this.instrumentid < 0L) {
                reset();
                return;
            }

            final MasterDataStock data =
                    new MasterDataStockImpl(this.instrumentid,
                            this.dividend,
                            this.dividendLastYear,
                            this.dividendCurrency,
                            this.dividendExDay,
                            this.sector.build(),
                            this.gicsSectorKey,
                            this.gicsIndustryGroupKey,
                            this.gicsIndustryKey,
                            this.gicsSubIndustryKey,
                            this.gicsSector.build(),
                            this.gicsIndustryGroup.build(),
                            this.gicsIndustry.build(),
                            this.gicsSubIndustry.build(),
                            this.mostLiquidMarket);

            this.entries.merge(this.instrumentid, data, MasterDataStock::merge);

            reset();
        }

        protected void reset() {
            this.instrumentid = -1L;
            this.dividend = null;
            this.dividendLastYear = null;
            this.dividendCurrency = null;
            this.dividendExDay = null;
            this.sector.reset();
            this.gicsSectorKey = null;
            this.gicsIndustryGroupKey = null;
            this.gicsIndustryKey = null;
            this.gicsSubIndustryKey = null;
            this.gicsSector.reset();
            this.gicsIndustryGroup.reset();
            this.gicsIndustry.reset();
            this.gicsSubIndustry.reset();
            this.mostLiquidMarket = null;
            this.errorOccured = false;
        }
    }

    public StockDataResponse getStockData(StockDataRequest request) {
        final StockDataResponse response = new StockDataResponse();
        if (request.isWithAnnualReportData()) {
            response.setAnnualReportsDatas(
                    request.getInstrumentids()
                            .stream()
                            .map(iid -> this.companyDataProvider.getAnnualReportData(new CompanyDataRequest(iid, request.getProfile(), request.getLocales())))
                            .collect(Collectors.toCollection(ArrayList::new))
            );
        }
        if (request.isWithCompanyProfile()) {
            response.setCompanyProfiles(
                    request.getInstrumentids()
                            .stream()
                            .map(iid -> this.companyDataProvider.getCompanyProfile(new CompanyDataRequest(iid, request.getProfile(), request.getLocales())))
                            .collect(Collectors.toCollection(ArrayList::new))
            );
        }
        return response;
    }

    public static void main(String[] args) throws Exception {
        final StockDataProviderImpl p = new StockDataProviderImpl();
        p.setWmDataFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-wm-stock-masterdata.xml.gz"));
        p.setGicsDataFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-gics-stock-masterdata.xml.gz"));
        p.setActiveMonitor(new ActiveMonitor());
        p.afterPropertiesSet();
        System.out.println(
            p.getMasterData(Arrays.asList(20665L, 12527L, 1946810L), ProfileFactory.valueOf(true)));
        System.out.println(Runtime.getRuntime().freeMemory() / (1024 * 1024) + "/" + Runtime.getRuntime().maxMemory() / (1024 * 1024));
        System.gc();
        System.out.println(Runtime.getRuntime().freeMemory() / (1024 * 1024) + "/" + Runtime.getRuntime().maxMemory() / (1024 * 1024));
        p.readMasterData();
        System.out.println(Runtime.getRuntime().freeMemory() / (1024 * 1024) + "/" + Runtime.getRuntime().maxMemory() / (1024 * 1024));
        System.gc();
        System.out.println(Runtime.getRuntime().freeMemory() / (1024 * 1024) + "/" + Runtime.getRuntime().maxMemory() / (1024 * 1024));
        Thread.sleep(Long.MAX_VALUE);
    }
}
