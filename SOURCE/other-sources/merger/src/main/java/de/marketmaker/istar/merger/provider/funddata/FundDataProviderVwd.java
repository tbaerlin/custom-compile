/*
 * FundDataProviderVwd.java
 *
 * Created on 11/23/15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.funddata;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.monitor.Resource;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.InstrumentAllocations;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.data.NullMasterDataFund;
import de.marketmaker.istar.domainimpl.data.InstrumentAllocationImpl;
import de.marketmaker.istar.domainimpl.data.MasterDataFundImpl;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.ratios.backend.XmlFieldsReader;

import static java.util.stream.Collectors.toList;

/**
 * @author Stefan Willenbrock
 */
public class FundDataProviderVwd implements FundDataProvider, InitializingBean {
    private static final Locale LOCALE_DUTCH = new Locale("nl");

    private static final Locale[] LOCALES = new Locale[]{Locale.GERMAN, Locale.ENGLISH, Locale.FRENCH, LOCALE_DUTCH};

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

//    public static final MasterDataFund.Source SOURCE = MasterDataFund.Source.VWD;
//  set to null since GIS Trader goes mad, mmf doesn't, our xsd is hardcoded in those apps
//  see R-81564 and Mails
    public static final MasterDataFund.Source SOURCE = null;

    public static final String LIST_SEPARATOR = "__,__";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Long, MasterDataFund> masterData = new HashMap<>();

    private final Map<String, Map<Long, List<InstrumentAllocation>>> allocations = new HashMap<>();

    {
        for (final Locale locale : LOCALES) {
            this.allocations.put(locale.getLanguage(), new HashMap<>());
        }
    }

    private ActiveMonitor activeMonitor;

    private File masterDataFile;

    private File allocationsFile;

    private FeriRatingProvider feriRatingProvider;

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setMasterDataFile(File masterDataFile) {
        this.masterDataFile = masterDataFile;
    }

    public void setAllocationsFile(File allocationsFile) {
        this.allocationsFile = allocationsFile;
    }

    public void setFeriRatingProvider(FeriRatingProvider feriRatingProvider) {
        this.feriRatingProvider = feriRatingProvider;
    }

    private synchronized MasterDataFund getMasterData(long instrumentid) {
        final MasterDataFund data = this.masterData.get(instrumentid);
        return data != null ? data : NullMasterDataFund.INSTANCE;
    }

    private synchronized InstrumentAllocations getInstrumentAllocations(long instrumentid,
            List<Locale> locales) {
        final Map<Long, List<InstrumentAllocation>> map = getAllocationsMap(locales);
        return InstrumentAllocations.create(map.get(instrumentid));
    }

    private Map<Long, List<InstrumentAllocation>> getAllocationsMap(List<Locale> locales) {
        if (locales != null) {
            for (final Locale locale : locales) {
                final Map<Long, List<InstrumentAllocation>> map = this.allocations.get(locale.getLanguage());
                if (map != null && !map.isEmpty()) {
                    return map;
                }
            }
        }
        return this.allocations.get(LOCALES[0].getLanguage());
    }


    @Override
    public FundDataResponse getFundData(FundDataRequest request) {
        final FundDataResponse response = new FundDataResponse();
        if (request.isWithAllocations() || request.isWithConsolidatedAllocations()) {
            final List<InstrumentAllocations> list = new ArrayList<>();
            for (final Long iid : request.getInstrumentids()) {
                list.add(getInstrumentAllocations(iid, request.getLocales()));
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

        final boolean morningstarsOnlyDz = !request.isWithMorningstarRating() && request.isWithMorningstarRatingDz();
        if (request.isWithMorningstarRating() || request.isWithMorningstarRatingDz()) {
            response.setMorningstarRatings(request.getInstrumentids().stream()
                    .map(iid -> {
                        final String morningstars = getMasterData(iid).getMorningstarOverallRating();
                        return hasMorningstars(iid, morningstarsOnlyDz) && morningstars != null ? Integer.valueOf(morningstars) : null;
                    }).collect(toList()));
        }

        return response;
    }

    private boolean hasMorningstars(Long iid, boolean morningstarsOnlyDz) {
        LocalizedString issuerName = getMasterData(iid).getIssuerName();
        return !morningstarsOnlyDz || issuerName != null && XmlFieldsReader.checkIssuer(issuerName.getDe(), XmlFieldsReader.UNION_INVEST_KVG_PREFIX);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.activeMonitor != null) {
            final FileResource masterDataResource = new FileResource(this.masterDataFile);
            masterDataResource.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    readMasterDataFund();
                }
            });

            final FileResource allocationsResource = new FileResource(this.allocationsFile);
            allocationsResource.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    readAllocations();
                }
            });

            this.activeMonitor.addResources(new Resource[]{masterDataResource, allocationsResource});
        }

        readMasterDataFund();
        readAllocations();
    }

    private void readMasterDataFund() {
        try {
            final TimeTaker tt = new TimeTaker();
            final MasterDataReader ur = new MasterDataReader();
            ur.read(this.masterDataFile);

            synchronized (this) {
                this.masterData.clear();
                this.masterData.putAll(ur.getValues());
            }

            this.logger.info("<readMasterDataFund> read " + this.masterDataFile.getName() + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readMasterDataFund> failed", e);
        }
    }

    private void readAllocations() {
        try {
            final TimeTaker tt = new TimeTaker();
            final AllocationsReader ur = new AllocationsReader();
            ur.read(this.allocationsFile);

            synchronized (this) {
                this.allocations.clear();
                this.allocations.put(LOCALES[0].getLanguage(), ur.getValues_DE());
                this.allocations.put(LOCALES[1].getLanguage(), ur.getValues_EN());
                this.allocations.put(LOCALES[2].getLanguage(), ur.getValues_FR());
                this.allocations.put(LOCALES[3].getLanguage(), ur.getValues_NL());
            }

            this.logger.info("<readAllocations> read " + this.allocationsFile.getName() + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readAllocations> failed", e);
        }
    }

    public static class MasterDataReader extends AbstractSaxReader {
        private final Map<Long, MasterDataFund> values = new HashMap<>();

        private MasterDataFundImpl.Builder builder = new MasterDataFundImpl.Builder() {
            @Override
            public MasterDataFundImpl build() {
                setSource(SOURCE);
                return super.build();
            }
        };

        private static final Map<String, BigDecimal> DISTRIBUTION_COUNT = new HashMap<>();

        static {
            DISTRIBUTION_COUNT.put("ADHO", BigDecimal.ZERO);
            DISTRIBUTION_COUNT.put("MNTH", new BigDecimal(12));
            DISTRIBUTION_COUNT.put("QUTR", new BigDecimal(4));
            DISTRIBUTION_COUNT.put("SEMI", new BigDecimal(2));
            DISTRIBUTION_COUNT.put("TOMN", new BigDecimal(6));
            DISTRIBUTION_COUNT.put("WEEK", new BigDecimal(52));
            DISTRIBUTION_COUNT.put("YEAR", BigDecimal.ONE);
        }

        private List<DateTime> getCurrentDates() {
            final List<String> dateStrings = getCurrentStrings();
            final List<DateTime> dates = new ArrayList<>(dateStrings.size());
            for (String d : dateStrings) {
                if (!StringUtils.hasText(d)) {
                    continue;
                }
                dates.add(DTF.parseDateTime(d));
            }
            return dates;
        }

        private List<String> getCurrentStrings() {
            return Arrays.asList(getCurrentString().split(LIST_SEPARATOR));
        }

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if (tagName.equals("ROW")) {
                    // i have finished a new row => process
                    storeFields();
                }
                else if (tagName.equals("IID")) {
                    this.builder.setInstrumentid(getCurrentLong());
                }
                else if (tagName.equals("INSTRUMENTID")) {
                    this.builder.setInstrumentid(getCurrentLong());
                }
                else if (tagName.equals("FUNDTYP_EN")) {
                    this.builder.setFundtype(getCurrentString(), Language.en);
                }
                else if (tagName.equals("FUNDTYP_DE")) {
                    this.builder.setFundtype(getCurrentString(), Language.de);
                }
                else if (tagName.equals("FUNDTYP_FR")) {
                    this.builder.setFundtype(getCurrentString(), Language.fr);
                }
                else if (tagName.equals("FUNDTYP_NL")) {
                    this.builder.setFundtype(getCurrentString(), Language.nl);
                }
                else if (tagName.equals("CURRENCY")) {
                    this.builder.setCurrency(getCurrentString());
                }
                else if (tagName.equals("ANLAGESTRATEGIE_PROSPECT_EN")) {
                    this.builder.setStrategy(getCurrentString(), Language.en);
                }
                else if (tagName.equals("ANLAGESTRATEGIE_PROSPECT_DE")) {
                    this.builder.setStrategy(getCurrentString(), Language.de);
                }
                else if (tagName.equals("ANLAGESTRATEGIE_PROSPECT_FR")) {
                    this.builder.setStrategy(getCurrentString(), Language.fr);
                }
                else if (tagName.equals("ANLAGESTRATEGIE_PROSPECT_NL")) {
                    this.builder.setStrategy(getCurrentString(), Language.nl);
                }
                else if (tagName.equals("ANLAGESCHWERPUNKT_EN")) {
                    this.builder.setInvestmentFocus(getCurrentString(), Language.en);
                }
                else if (tagName.equals("ANLAGESCHWERPUNKT_DE")) {
                    this.builder.setInvestmentFocus(getCurrentString(), Language.de);
                }
                else if (tagName.equals("ANLAGESCHWERPUNKT_FR")) {
                    this.builder.setInvestmentFocus(getCurrentString(), Language.fr);
                }
                else if (tagName.equals("ANLAGESCHWERPUNKT_NL")) {
                    this.builder.setInvestmentFocus(getCurrentString(), Language.nl);
                }
                else if (tagName.equals("KAG")) {
                    this.builder.setIssuerName(getCurrentString(), LocalizedString.DEFAULT_LANGUAGE);
                }
                else if (tagName.equals("FUNDORGANISATION")) {
                    this.builder.setIssuerOrganization(getCurrentString(), LocalizedString.DEFAULT_LANGUAGE);
                }
                else if (tagName.equals("COUNTRY_EN")) {
                    this.builder.setCountry(getCurrentString(), Language.en);
                }
                else if (tagName.equals("COUNTRY_DE")) {
                    this.builder.setCountry(getCurrentString(), Language.de);
                }
                else if (tagName.equals("COUNTRY_FR")) {
                    this.builder.setCountry(getCurrentString(), Language.fr);
                }
                else if (tagName.equals("COUNTRY_NL")) {
                    this.builder.setCountry(getCurrentString(), Language.nl);
                }
                else if (tagName.equals("BENCHMARK_EN")) {
                    this.builder.setBenchmarkName(getCurrentString(), Language.en);
                }
                else if (tagName.equals("BENCHMARK_DE")) {
                    this.builder.setBenchmarkName(getCurrentString(), Language.de);
                }
                else if (tagName.equals("BENCHMARK_FR")) {
                    this.builder.setBenchmarkName(getCurrentString(), Language.fr);
                }
                else if (tagName.equals("BENCHMARK_NL")) {
                    this.builder.setBenchmarkName(getCurrentString(), Language.nl);
                }
                else if (tagName.equals("BENCHMARKQID")) {
                    this.builder.setBenchmarkQid(getCurrentLong());
                }
                else if (tagName.equals("AUFLAGEDATUM")) {
                    this.builder.setIssueDate(getCurrentDateTime(DTF));
                }
                else if (tagName.equals("FUNDCLASSVOLUME")) {
                    this.builder.setFundclassVolume(getCurrentBigDecimal());
                }
                else if (tagName.equals("FUNDCLASSVOLUMECURRENCY")) {
                    this.builder.setFundclassVolumeCurrency(getCurrentString());
                }
                else if (tagName.equals("FUNDCLASSVOLUMEDATE")) {
                    this.builder.setFundclassVolumeDate(getCurrentDateTime(DTF));
                }
                else if (tagName.equals("FONDSMANAGER")) {
                    this.builder.setFundManager(getCurrentString(), Language.de, Language.en, Language.fr, Language.nl);
                }
                else if (tagName.equals("DISTRIBUTIONSTRATEGY__EN")) {
                    final String ds = getCurrentString();
                    if (ds == null || "".equals(ds.trim())) {
                        this.builder.setDistributionStrategy(MasterDataFund.DistributionStrategy.UNKNOWN);
                    }
                    else if ("retaining".equals(ds.trim())) {
                        this.builder.setDistributionStrategy(MasterDataFund.DistributionStrategy.RETAINING);
                    }
                    else if ("distributing".equals(ds.trim())) {
                        this.builder.setDistributionStrategy(MasterDataFund.DistributionStrategy.DISTRIBUTING);
                    }
                    else {
                        this.logger.error("<handleRow> unknown distribution strategy abbreviation: " + ds);
                    }
                }
                else if (tagName.equals("AUSGABEAUFSCHLAG")) {
                    this.builder.setIssueSurcharge(getCurrentPercent());
                }
                else if (tagName.equals("MANAGEMENTFEE")) {
                    this.builder.setManagementFee(getCurrentPercent());
                }
                else if (tagName.equals("PERFORMANCEGEBUEHR")) {
                    this.builder.setPerformanceFee(getCurrentPercent());
                }
                else if (tagName.equals("DEPOTBANKGEBUEHR")) {
                    this.builder.setAccountFee(getCurrentPercent());
                }
                else if (tagName.equals("TER")) {
                    this.builder.setTer(getCurrentPercent());
                }
                else if (tagName.equals("KAGSTRASSE")) {
                    this.builder.setIssuerStreet(getCurrentString());
                }
                else if (tagName.equals("KAGPLZ")) {
                    this.builder.setIssuerPostalcode(getCurrentString());
                }
                else if (tagName.equals("KAGORT")) {
                    this.builder.setIssuerCity(getCurrentString());
                }
                else if (tagName.equals("KAGEMAIL")) {
                    this.builder.setIssuerEmail(getCurrentString());
                }
                else if (tagName.equals("KAGURL")) {
                    this.builder.setIssuerUrl(getCurrentString());
                }
                else if (tagName.equals("KAGPHONE")) {
                    this.builder.setIssuerPhone(getCurrentString());
                }
                else if (tagName.equals("KAGFAX")) {
                    this.builder.setIssuerFax(getCurrentString());
                }
                else if (tagName.equals("LASTDISTRIBUTION")) {
                    this.builder.setLastDistribution(getCurrentBigDecimal());
                }
                else if (tagName.equals("LASTDISTRIBUTIONDATE")) {
                    this.builder.setLastDistributionDate(getCurrentDateTime(DTF));
                }
                else if (tagName.equals("DISTRIBUTIONCURRENCY")) {
                    this.builder.setDistributionCurrency(getCurrentString());
                }
                else if (tagName.equals("DISTRIBUTIONCOUNT")) {
                    this.builder.setDistributionCount(getDistributionCount(getCurrentString()));
                }
                else if (tagName.equals("BVIKATEGORIEGROB")) {
                    this.builder.setFundtypeBviCoarse(getCurrentString());
                }
                else if (tagName.equals("EINMALANLAGE_FR")) {
                    this.builder.setMinimumInvestmentDescription(getCurrentString(), Language.fr);
                }
                else if (tagName.equals("EINMALANLAGE_NL")) {
                    this.builder.setMinimumInvestmentDescription(getCurrentString(), Language.nl);
                }
                else if (tagName.equals("PORTFOLIODATE")) {
                    this.builder.setPortfolioDate(getCurrentDateTime(DTF));
                }
                else if (tagName.equals("REDEMPTIONFEE")) {
                    this.builder.setRedemptionFee(getCurrentPercent());
                }
                else if (tagName.equals("DURATION")) {
                    this.builder.setDuration(getDuration(getCurrentString()));
                }
                else if (tagName.equals("MODIFIEDDURATION")) {
                    this.builder.setModifiedDuration(getDuration(getCurrentString()));
                }
                else if (tagName.equals("MARKETADMISSION")) {
                    this.builder.setMarketAdmission(getCurrentString());
                }
                else if (tagName.equals("ONGOINGCHARGE")) {
                    this.builder.setOngoingCharge(getCurrentPercent());
                }
                else if (tagName.equals("ONGOINGCHARGEDATE")) {
                    this.builder.setOngoingChargeDate(getCurrentDateTime(DTF));
                }
                else if (tagName.equals("SRRIVALUE")) {
                    this.builder.setSrriValue(getCurrentString());
                }
                else if (tagName.equals("SRRIDATE")) {
                    this.builder.setSrriValueDate(getCurrentDateTime(DTF));
                }
                else if (tagName.equals("DIAMONDRATING")) {
                    this.builder.setDiamondRating(getCurrentString());
                }
                else if (tagName.equals("DIAMONDRATINGDATE")) {
                    this.builder.setDiamondRatingDate(getCurrentDateTime(DTF));
                }
                else if (tagName.equals("FONDSVOLUMEN")) {
                    this.builder.setFundVolume(getCurrentBigDecimal());
                }
                else if (tagName.equals("DATUM_FONDSVOLUMEN")) {
                    this.builder.setFundVolumeDate(getCurrentDateTime(DTF));
                }
                else if (tagName.equals("FONDSVOLUMENCURRENCY")) {
                    this.builder.setFundVolumeCurrency(getCurrentString());
                }
                else if (tagName.equals("TAXABLEINCOMEDIVIDEND")) {
                    this.builder.setTaxableIncomeDividend(getCurrentPercent());
                }
                else if (tagName.equals("CAPITALGUARANTEED")) {
                    this.builder.setCapitalGuaranteed(getCurrentBigDecimal());
                }
                else if (tagName.equals("EUROPEANPASSPORT")) {
                    this.builder.setEuroPassport(getCurrentBoolean());
                }
                else if (tagName.equals("ISETCREPLICATION")) {
                    this.builder.setEtcReplication(getCurrentBoolean());
                }
                else if (tagName.equals("ISETPREPLICATION")) {
                    this.builder.setEtpReplication(getCurrentBoolean());
                }
                else if (tagName.equals("ISETNREPLICATION")) {
                    this.builder.setEtnReplication(getCurrentBoolean());
                }
                else if (tagName.equals("ISETFREPLICATION")) {
                    this.builder.setEtfReplication(getCurrentBoolean());
                }
                else if (tagName.equals("ETFREPLICATIONLEVEL_NL")) {
                    this.builder.setEtfReplicationLevel(getCurrentString(), Language.nl);
                }
                else if (tagName.equals("ETFREPLICATIONLEVEL_FR")) {
                    this.builder.setEtfReplicationLevel(getCurrentString(), Language.fr);
                }
                else if (tagName.equals("ETFREPLICATIONLEVEL_EN")) {
                    this.builder.setEtfReplicationLevel(getCurrentString(), Language.en);
                }
                else if (tagName.equals("ETFREPLICATIONLEVEL_DE")) {
                    this.builder.setEtfReplicationLevel(getCurrentString(), Language.de);
                }
                else if (tagName.equals("QUOTEFREQUENCY_NL")) {
                    this.builder.setQuoteFrequency(getCurrentString(), Language.nl);
                }
                else if (tagName.equals("QUOTEFREQUENCY_FR")) {
                    this.builder.setQuoteFrequency(getCurrentString(), Language.fr);
                }
                else if (tagName.equals("QUOTEFREQUENCY_EN")) {
                    this.builder.setQuoteFrequency(getCurrentString(), Language.en);
                }
                else if (tagName.equals("QUOTEFREQUENCY_DE")) {
                    this.builder.setQuoteFrequency(getCurrentString(), Language.de);
                }
                else if (tagName.equals("FONDSMANAGER_STARTDATES")) {
                    this.builder.setFundManagerStartDates(getCurrentDates());
                }
                else if (tagName.equals("FONDSMANAGER_NAMES")) {
                    this.builder.setFundManagerNames(getCurrentStrings());
                }
                else if (tagName.equals("MORNINGSTAROVERALLRATING")) {
                    // FundDataResponse#setMorningstarRatings needs Integer
                    // therefor fail early, on istar-provider start
                    this.builder.setMorningstarOverallRating(String.valueOf(getCurrentInt()));
                }
                else if (tagName.equals("ISETF")) {
                    this.builder.setEtf(getCurrentBoolean());
                }
                else if (tagName.equals("ISETN")) {
                    this.builder.setEtn(getCurrentBoolean());
                }
                else if (tagName.equals("ISETC")) {
                    this.builder.setEtc(getCurrentBoolean());
                }
                else if (tagName.equals("ISETP")) {
                    this.builder.setEtp(getCurrentBoolean());
                }
                else if (tagName.equals("REGIONCODE")) {
                    this.builder.setRegionCode(getCurrentString());
                }
                else if (tagName.equals("REGION_NL")) {
                    this.builder.setRegion(getCurrentString(), Language.nl);
                }
                else if (tagName.equals("REGION_FR")) {
                    this.builder.setRegion(getCurrentString(), Language.fr);
                }
                else if (tagName.equals("REGION_EN")) {
                    this.builder.setRegion(getCurrentString(), Language.en);
                }
                else if (tagName.equals("REGION_DE")) {
                    this.builder.setRegion(getCurrentString(), Language.de);
                }
                else if (tagName.equals("SECTORCODE")) {
                    this.builder.setSectorCode(getCurrentString());
                }
                else if (tagName.equals("SECTOR_NL")) {
                    this.builder.setSector(getCurrentString(), Language.nl);
                }
                else if (tagName.equals("SECTOR_FR")) {
                    this.builder.setSector(getCurrentString(), Language.fr);
                }
                else if (tagName.equals("SECTOR_EN")) {
                    this.builder.setSector(getCurrentString(), Language.en);
                }
                else if (tagName.equals("SECTOR_DE")) {
                    this.builder.setSector(getCurrentString(), Language.de);
                }
                else if (tagName.equals("FULLNAME")) {
                    this.builder.setFullName(getCurrentString(), LocalizedString.DEFAULT_LANGUAGE);
                }
                else if (tagName.equals("SHORTNAME")) {
                    this.builder.setShortName(getCurrentString(), LocalizedString.DEFAULT_LANGUAGE);
                }
                else if (tagName.equals("EFCFCLASSIFICATION")) {
                    this.builder.setEfcfClassification(getCurrentString());
                }
                else if (tagName.equals("LEGALTYPENL")) {
                    this.builder.setLegalType(getCurrentString(), Language.nl);
                }
                else if (tagName.equals("LEGALTYPEFR")) {
                    this.builder.setLegalType(getCurrentString(), Language.fr);
                }
                else if (tagName.equals("LEGALTYPEEN")) {
                    this.builder.setLegalType(getCurrentString(), Language.en);
                }
                else if (tagName.equals("LEGALTYPEDE")) {
                    this.builder.setLegalType(getCurrentString(), Language.de);
                }
                else if (tagName.equals("COUNTRYOFISSUERCODE")) {
                    this.builder.setIssuerCountryCode(getCurrentString());
                }
                else {
                    notParsed(tagName);
                }
            } catch (Exception e) {
                this.logger.error("<endElement> error in " + tagName, e);
                this.errorOccured = true;
            }
        }

        private BigDecimal getDistributionCount(String s) {
            return (s != null) ? DISTRIBUTION_COUNT.get(s) : null;
        }

        BigDecimal getDuration(String str) {
            if (!StringUtils.hasText(str)) {
                return null;
            }
            final BigDecimal value = new BigDecimal(str.substring(0, str.length() - 1));
            char timeUnit = str.charAt(str.length() - 1);
            switch (timeUnit) {
                case 'Y':
                    return value;
                case 'M':
                    return value.divide(new BigDecimal(12), Constants.MC);
                case 'W':
                    return value.divide(new BigDecimal(52), Constants.MC);
                case 'D':
                    return value.divide(new BigDecimal(356), Constants.MC);
                default:
                    return null;
            }
        }

        private void storeFields() {
            this.limiter.ackAction();

            if (this.errorOccured) {
                reset();
                return;
            }

            if (this.builder.isValid()) {
                this.builder.setWithDefaultBenchmark(false);
                final MasterDataFund data_nl = this.builder.build();
                this.values.put(data_nl.getInstrumentid(), data_nl);
            }

            reset();
        }

        protected void reset() {
            this.builder = new MasterDataFundImpl.Builder();
            this.errorOccured = false;
        }

        public Map<Long, MasterDataFund> getValues() {
            return values;
        }
    }

    public static class AllocationsReader extends AbstractSaxReader {
        final Map<Long, List<InstrumentAllocation>> values_de = new HashMap<>();

        final Map<Long, List<InstrumentAllocation>> values_en = new HashMap<>();

        final Map<Long, List<InstrumentAllocation>> values_fr = new HashMap<>();

        final Map<Long, List<InstrumentAllocation>> values_nl = new HashMap<>();

        private long instrumentid = -1;

        private String description_de;

        private String description_en;

        private String description_nl;

        private String description_fr;

        private BigDecimal share;

        private String isin;

        private String elementCode;

        private DateTime lastUpdate;

        private InstrumentAllocation.Type type;

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if (tagName.equals("ROW")) {
                    // if have finished a new row => process
                    storeFields();
                }
                else if (tagName.equals("IID")) {
                    this.instrumentid = getCurrentLong();
                }
                else if (tagName.equals("DESCRIPTION_DE")) {
                    this.description_de = getCurrentString();
                }
                else if (tagName.equals("DESCRIPTION_EN")) {
                    this.description_en = getCurrentString();
                }
                else if (tagName.equals("DESCRIPTION_FR")) {
                    this.description_fr = getCurrentString();
                }
                else if (tagName.equals("DESCRIPTION_NL")) {
                    this.description_nl = getCurrentString();
                }
                else if (tagName.equals("TYPE")) {
                    this.type = convert(getCurrentString(false));
                }
                else if (tagName.equals("SHARE_")) {
                    this.share = getCurrentBigDecimal();
                }
                else if (tagName.equals("ISINCODE")) {
                    this.isin = getCurrentString();
                }
                else if (tagName.equals("ELEMENTCODE")) {
                    this.elementCode = getCurrentString();
                }
                else if (tagName.equals("LASTUPDATE")) {
                    this.lastUpdate = DTF.parseDateTime(getCurrentString());
                }
            } catch (Exception e) {
                this.logger.error("<endElement> error in " + tagName, e);
                this.errorOccured = true;
            }
        }

        private InstrumentAllocation.Type convert(String str) {
            switch (str) {
                case "IP2":
                    return InstrumentAllocation.Type.COUNTRY;
                case "IPH":
                    return InstrumentAllocation.Type.SECTOR;
                case "IPA":
                    return InstrumentAllocation.Type.INSTRUMENT;
                case "IP1":
                    return InstrumentAllocation.Type.ASSET;
                case "IP6":
                    return InstrumentAllocation.Type.CURRENCY;
                case "IP9":
                    return InstrumentAllocation.Type.RATING;
                case "IPJ":
                    return InstrumentAllocation.Type.GEOGRAPHICAL;
                default:
                    return null;
            }
        }

        private void storeFields() {
            this.limiter.ackAction();

            if (this.errorOccured) {
                reset();
                return;
            }

            if (this.instrumentid < 0 || this.type == null ||
                    this.share == null || this.share.compareTo(BigDecimal.ZERO) == 0) {
                reset();
                return;
            }

            add(this.description_de, this.values_de);
            add(this.description_en, this.values_en);
            add(this.description_fr, this.values_fr);
            add(this.description_nl, this.values_nl);

            reset();
        }

        private void add(String description, Map<Long, List<InstrumentAllocation>> values) {
            final InstrumentAllocation ia = new InstrumentAllocationImpl(this.type, this.instrumentid, description, this.share, this.isin, this.elementCode, this.lastUpdate).withSource(SOURCE);
            List<InstrumentAllocation> ias = values.get(this.instrumentid);
            if (ias == null) {
                ias = new ArrayList<>();
                values.put(this.instrumentid, ias);
            }
            ias.add(ia);
        }

        protected void reset() {
            this.instrumentid = -1;
            this.description_de = null;
            this.description_en = null;
            this.description_fr = null;
            this.description_nl = null;
            this.type = null;
            this.share = null;
            this.isin = null;
            this.elementCode = null;
            this.lastUpdate = null;
            this.errorOccured = false;
        }

        public Map<Long, List<InstrumentAllocation>> getValues_DE() {
            return values_de;
        }

        public Map<Long, List<InstrumentAllocation>> getValues_EN() {
            return values_en;
        }

        public Map<Long, List<InstrumentAllocation>> getValues_FR() {
            return values_fr;
        }

        public Map<Long, List<InstrumentAllocation>> getValues_NL() {
            return values_nl;
        }
    }

    public static void main(String[] args) throws Exception {
        final FundDataProviderVwd p = new FundDataProviderVwd();
        File dir = new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider");
        p.setMasterDataFile(new File(dir, "istar-vwd-fund-masterdata.xml.gz"));
        p.setAllocationsFile(new File(dir, "istar-vwd-fund-masterbreakdown.xml.gz"));
        p.afterPropertiesSet();
        final List<Locale> locales = Arrays.asList(LOCALES);
        final MasterDataFund masterData = p.getMasterData(52112);
        System.out.println(masterData);
        System.out.println(p.getInstrumentAllocations(52112, locales));

    }

}
