package de.marketmaker.istar.merger.provider.analyzer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.analyses.analyzer.AnalysesCollector;
import de.marketmaker.istar.analyses.analyzer.Index;
import de.marketmaker.istar.analyses.analyzer.ReportContext;
import de.marketmaker.istar.analyses.analyzer.ReportContextImpl;
import de.marketmaker.istar.analyses.analyzer.Security;
import de.marketmaker.istar.analyses.backend.AnalysesDao;
import de.marketmaker.istar.analyses.backend.AnalysesDaoDb;
import de.marketmaker.istar.analyses.backend.AnalysesProvider;
import de.marketmaker.istar.analyses.backend.Protos;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Provider;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.instrument.IndexCompositionProvider;
import de.marketmaker.istar.instrument.IndexMembershipRequest;
import de.marketmaker.istar.instrument.IndexMembershipResponse;
import de.marketmaker.istar.instrument.InstrumentRequest;
import de.marketmaker.istar.instrument.InstrumentRequest.KeyType;
import de.marketmaker.istar.instrument.InstrumentResponse;
import de.marketmaker.istar.instrument.InstrumentServer;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.InstrumentRatios;
import de.marketmaker.istar.ratios.frontend.RatioDataResult;
import de.marketmaker.istar.ratios.frontend.RatioSearchEngine;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * bootstrapping the AnalysesContext and processing incoming analyses
 *
 * this is also a container for dynamic data
 */
@ManagedResource
public class CollectorImpl implements AnalysesCollector, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // analysis database backend for startup polling all DB content
    private AnalysesDao analysesDao;


    // finding indices for a n instrument
    private IndexCompositionProvider indexCompositionProvider;

    // finding home quotes
    private InstrumentServer instrumentServer;

    // retrieving gics data
    protected RatioSearchEngine ratiosServer;

    // precomputed news-provider specific evaluation data
    private ReportContextStore reportContextStore;

    // retrieving analysis data from the backend on startup
    public void setDao(AnalysesDaoDb analysesDao) {
        this.analysesDao = analysesDao;
    }

    // processing new/incoming analysis data
    public void setAnalysesProvider(Collection<AnalysesProvider> analysesProvider) {
        analysesProvider.forEach(provider -> provider.setCollector(CollectorImpl.this));
    }

    // data storage
    public void setReportContextStore(ReportContextStore reportContextStore) {
        this.reportContextStore = reportContextStore;
    }

    // -- providers

    public void setInstrumentServer(InstrumentServer instrumentServer) {
        this.instrumentServer = instrumentServer;
    }

    public void setIndexCompositionProvider(IndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setRatiosProvider(RatioSearchEngine ratiosServer) {
        this.ratiosServer = ratiosServer;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        fetchAllFromDatabase();
    }

    @ManagedOperation
    public void fetchAllFromDatabase() {
        // split into 2 actions so we don't lock the DB
        reportContextStore.getSources().forEach(this::fetchFromDatabase);  // usually takes 2-3 sec
        // query the providers for additional data
        reportContextStore.getSources().forEach(this::updateStaticData);
    }

    /**
     * add additional data from various providers
     */
    private void updateStaticData(Provider provider) {
        // fetch additional security data like
        // homeQuote, indices containing the instrument, ratiodata... ReportContext also contains the price data
        final ReportContext container = reportContextStore.getReportContext(provider);
        setHomeQuote(container.getSecurities());
        setIndices((ReportContextImpl)container, container.getSecurities());
        setRatioData(container.getSecurities());
        // container for dynamic price data
        updatePriceData((ReportContextImpl)container, container.getSecurities());
    }

    private void fetchFromDatabase(Provider provider, List<Long> idList) {
        analysesDao.getItems(provider, idList).forEach(analysis -> {
            if (analysis != null && analysis.getRawAnalysis() != null) {
                reportContextStore.getReportContext(provider).addAnalyses(analysis.getRawAnalysis());
            }
        });
    }

    // read database content for a single provider
    private void fetchFromDatabase(Provider provider) {
        // processing all data for a single provider, this locks the database
        analysesDao.getAllItems(provider, reportContextStore.getReportContext(provider)::addAnalyses);
    }

    /**
     * implementation of the AnalysesCollector interface this method is called for incoming analysis data
     */
    @Override
    public void addAnalysis(Provider provider, Protos.Analysis analysis) {
        long analysisId = analysis.getId();
        logger.info("<addAnalysis> new analysis, provider is '" + provider.name()
                + "' analysisId is " + analysisId + "/" + Long.toString(analysisId, Character.MAX_RADIX));
        reportContextStore.getReportContext(provider).addAnalyses(analysis);
    }

    //
    private void setHomeQuote(Collection<Security> securities) {
        // find a quote for each security
        securities.stream()
                .filter(security -> security.getQid() == null) // no home quote yet
                .forEach(this::findHomeQuote); // also sets the qid
    }

    private void setIndices(ReportContextImpl dataContainer, Collection<Security> securities) {
        securities.stream()
                // qid is set to NULL_QID if no matching security can be found
                .filter(security -> security.getQid() != null && security.getQid() != NULL_QID)
                .forEach(security -> findIndices(dataContainer, security));
    }

    private void updatePriceData(ReportContextImpl dataContainer, Collection<Security> securities) {
        securities.stream()
                .filter(security -> security.getQid() != null && security.getQid() != NULL_QID)
                .forEach(security -> updatePriceHistory((PriceHistoryCacheImpl)dataContainer.getPriceCache(), security));
    }

    private void updatePriceHistory(PriceHistoryCacheImpl priceHistoryCache, Security security) {
        priceHistoryCache.initializeTimeseries(security);
    }

    private void setRatioData(Collection<Security> securities) {
        securities.stream()
                .filter(security -> !NULL_RATIO_STRING.equals(security.getSector()))
                .forEach(this::findRatioData);
    }

    // setting qid, vwdCode, type
    // for whatever reason there are some iids in the analyses data that are not available from the instrument server:
    // 3267193.iid/IE00B1GKF381, 214324878.iid/FR0010220475, 189936002.iid/US38259P7069
    // 33672.iid/DE0006083439, 14915.iid/CH0012214059, 46753.iid/ES0148396015, 681757.iid/US38259P5089
    // 27485.iid/DE0005072300, 194124815.iid/DE0005772206
    private void findHomeQuote(Security security) {
        final InstrumentRequest request = new InstrumentRequest();
        request.addItem(Long.toString(security.getIid()), KeyType.IID);
        final InstrumentResponse result = instrumentServer.identify(request);
        final Instrument instrument = result.getInstruments().get(0);
        if (instrument != null) {
            final Market homeExchange = instrument.getHomeExchange();
            final InstrumentTypeEnum instrumentType = instrument.getInstrumentType();
            final Optional<Quote> optionalQuote = instrument.getQuotes().stream()
                    .filter(quote -> quote.getMarket().equals(homeExchange))
                    .findAny();
            if (optionalQuote.isPresent()) {
                Quote quote = optionalQuote.get();
                security.setQid(quote.getId());
                security.setVwdCode(quote.getSymbolVwdfeed());
                security.setCurrency(quote.getCurrency().getSymbolIso());
                security.setType(instrumentType);
                return;
            }
        }
        // fallback in case we don't get the data...
        logger.warn("<findHomeQuote> was unable to find instrument or homeQuote for '" + security.getIid() + ".iid/"
                + security.getSymbol() + "' setting qid to " + NULL_QID);
        security.setQid(NULL_QID);
        security.setVwdCode(NULL_VWDCODE);
        security.setCurrency(NULL_CURRENCY);
        security.setType(InstrumentTypeEnum.NON);

    }

    // find and store all indices for a new security
    private void findIndices(ReportContextImpl dataContainer, Security security) {
        assert security.getQid() != null : "qid must not be null";
        final IndexMembershipResponse response = indexCompositionProvider
                .getIndexMembership(new IndexMembershipRequest(new QuoteDp2(security.getQid())));
        response.getItems().stream().forEach(item -> {
            if (!dataContainer.getIndex(item.indexQid).isPresent()) {
                final Optional<Index> index = findIndex(item.indexQid);
                if (!index.isPresent()) {
                    return; // indexQid was invalid
                } else {
                    dataContainer.addIndex(index.get());
                }
            }
            final Index index = dataContainer.getIndex(item.indexQid).get();
            // there is a n-n relationship between index and security,
            // we need to update both sides
            index.put(security);
            security.put(index);
        });
    }

    private void findRatioData(Security security) {
        final RatioSearchRequest ratiosRequest = new RatioSearchRequest(ProfileFactory.valueOf(true));
        //ratiosRequest.addParameter("vwdCode", security.getVwdCode());
        ratiosRequest.addParameter("id", Long.toString(security.getIid()));
        ratiosRequest.setType(security.getType());
        ratiosRequest.setWithDetailedSymbol(true);
        final RatioSearchResponse result = ratiosServer.search(ratiosRequest);
        if (result != null && result.isValid()) {
            final DefaultRatioSearchResponse ratiosResult = (DefaultRatioSearchResponse)result;
            if (ratiosResult.getElements() != null && ratiosResult.getElements().size() >= 1) {
                final RatioDataResult ratioDataResult = ratiosResult.getElements().get(0);
                final InstrumentRatios instrumentRatios = ratioDataResult.getInstrumentRatios();

                security.setIndustry(instrumentRatios.getString(RatioFieldDescription.gicsIndustry.id()));
                security.setSector(instrumentRatios.getString(RatioFieldDescription.gicsSector.id()));
                security.setCountry(instrumentRatios.getString(RatioFieldDescription.country.id()));
                security.setName(instrumentRatios.getString(RatioFieldDescription.name.id()));
                security.setIndustryGroup(instrumentRatios.getString(RatioFieldDescription.gicsIndustryGroup.id()));
                security.setSubIndustry(instrumentRatios.getString(RatioFieldDescription.gicsSubIndustry.id()));
                return;
            }
        }
        logger.warn("<findRatioData> no ratio data for '" + security.getIid() + ".iid/" + security.getSymbol() + "'");
        security.setIndustry(NULL_RATIO_STRING);
        security.setSector(NULL_RATIO_STRING);
        security.setCountry(NULL_RATIO_STRING);
        security.setName(NULL_RATIO_STRING);
        security.setIndustryGroup(NULL_RATIO_STRING);
        security.setSubIndustry(NULL_RATIO_STRING);
    }

    // create index object for a qid or return empty if the qid is not valid
    private Optional<Index> findIndex(long qid) {
        final InstrumentRequest request = new InstrumentRequest();
        request.addItem(Long.toString(qid), KeyType.QID);
        InstrumentResponse response = instrumentServer.identify(request);
        Instrument instrument = response.getInstruments().get(0);
        if (instrument == null) {
            return Optional.empty();
        }
        return Optional.of(new Index(qid, instrument.getName()));
    }

}
