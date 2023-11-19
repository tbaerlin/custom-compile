package de.marketmaker.istar.analyses.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.nioframework.SelectorThread;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.news.backend.ObjectAdapter;
import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;
import de.marketmaker.istar.news.frontend.NewsRecord;
import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsResponse;
import de.marketmaker.istar.news.frontend.NewsServer;


@ManagedResource
public class AwpImporter extends AnalysesNewsHandler implements InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // only ever accesses in the selector thread so we don't need to synchronize
    private final Set<NewsRecordImpl> incompleteAnalysesCache = new HashSet<>();

    private NewsServer newsServer;

    private SelectorThread selectorThread;

    private File persistenceFile = new File(System.getProperty("user.home"),
            "produktion/var/data/analyses/awpAnalysesPersistence.obj");

    private final ScheduledExecutorService scheduler;

    // Analyse [Rating-ID] wurde ohne korrespondierende Nachricht versandt.
    private String DUMMY_HEADLINE_1 = "Analyse ";
    private String DUMMY_HEADLINE_2 = " wurde ohne korrespondierende Nachricht versandt.";

    private String DUMMY_CONTENT = ""
            + "Zu dieser Analyse wurde keine korrespondierende Nachricht seitens der "
            + "Agentur AWP bereitgestellt. Dieses Update ist daher als Bestätigung einer "
            + "vorherigen Einschätzung des beurteilten Unternehmens durch den Analysten zu "
            + "interpretieren.";

    private int TIMEOUT_HOURS = 25;


    public AwpImporter(AnalysesProviderAwp provider) {
        super(Selector.AWP_ANALYSER, provider);
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public void setNewsServer(NewsServer newsServer) {
        this.newsServer = newsServer;
    }

    public void setPersistenceFile(File persistenceFile) {
        this.persistenceFile = persistenceFile;
    }

    public void setSelectorThread(SelectorThread selectorThread) {
        this.selectorThread = selectorThread;
    }

    @Override
    public void afterPropertiesSet() {
        selectorThread.invokeLater(this::restoreCache);
        // try to assign headline and text to the incoming analyses data
        scheduler.scheduleWithFixedDelay(() -> selectorThread
                .invokeLater(AwpImporter.this::processCache), 2, 5, TimeUnit.MINUTES);
    }

    // creating the protobuf container
    // see: T-45092 for a list of fields
    @Override
    protected void addProviderSpecificFields(NewsRecordImpl nr, Protos.Analysis.Builder builder) {
        // fields values for symbol and iid are added before calling this method

        // previous
        addFields(nr, builder, VwdFieldDescription.NDB_Country, "country");
        addField(nr, builder, VwdFieldDescription.NDB_CompanyName, "company_name");
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Study_Date, "study_date", ObjectAdapter.LONG_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_Ticker, "ticker");
        addField(nr, builder, VwdFieldDescription.NDB_Industry, "industry");
        addFields(nr, builder, VwdFieldDescription.NDB_Analyst, "analyst_name");
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Institute_Name, "source");
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Institute_Symbol, "institute_symbol");
        // normalized ratings
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Recommendation, "rating", ObjectAdapter.AWP_RATING_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Prev_Recommendation, "previous_rating", ObjectAdapter.AWP_RATING_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Price_Target, "target", ObjectAdapter.PRICE_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Prev_Price_Target, "previous_target", ObjectAdapter.PRICE_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Price_Currency, "currency");
        addField(nr, builder, VwdFieldDescription.NDB_RatingID, "rating_id");

        // new
        addField(nr, builder, VwdFieldDescription.NDB_Status, "status");
        addField(nr, builder, VwdFieldDescription.NDB_Company_ID, "company_id");
        addField(nr, builder, VwdFieldDescription.NDB_Financial_Year, "financial_year");
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Recommendation_Norm, "analyst_recomm_norm");
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Prev_Recommendation_Norm, "analyst_prev_recomm_norm");
        addField(nr, builder, VwdFieldDescription.NDB_EPS_FY1, "eps_fy1", ObjectAdapter.PRICE_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_EPS_FY2, "eps_fy2", ObjectAdapter.PRICE_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_EPS_FY3, "eps_fy3", ObjectAdapter.PRICE_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_EPS_Currency, "eps_currency");
        addField(nr, builder, VwdFieldDescription.NDB_Dividend_FY1, "dividend_fy1", ObjectAdapter.PRICE_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_Dividend_FY2, "dividend_fy2", ObjectAdapter.PRICE_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_Dividend_FY3, "dividend_fy3", ObjectAdapter.PRICE_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_Dividend_Currency, "dividend_currency");
        addField(nr, builder, VwdFieldDescription.NDB_PE_FY1, "pe_fy1", ObjectAdapter.PRICE_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_PE_FY2, "pe_fy2", ObjectAdapter.PRICE_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_PE_FY3, "pe_fy3", ObjectAdapter.PRICE_ADAPTER);

        addFields(nr, builder, VwdFieldDescription.NDB_Branch, "branch");
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Recom_Source, "source_type");

        addFields(nr, builder, VwdFieldDescription.NDB_Analyst_Institute_ISIN, "institute_isin");
        addField(nr, builder, VwdFieldDescription.NDB_Time_Frame, "timeframe", ObjectAdapter.TIMEFRAME_ADAPTER);

        // store the raw rating values
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Recommendation, "analyst_recomm");
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Prev_Recommendation, "analyst_prev_recomm");

        // fallback for company name
        if (!builder.hasCompanyName() && nr.getInstruments().size() == 1) {
            builder.setCompanyName(nr.getInstruments().iterator().next().getName());
        }
    }

    /**
     * inbound data from feed,
     * this method is called for each incoming feed record
     * for AWP analyses we have to gather the title and text from the news backend
     *
     * @param newsRecord incoming news/analysis
     */
    @Override
    public void handle(NewsRecordImpl newsRecord) {
        // permission check
        if (!newsRecord.getSelectors().contains(this.selectorStr)) {
            return;
        }
        // this already uses headline and text from the news record
        final Protos.Analysis.Builder builder = buildAnalysis(newsRecord);
        // validation
        if (builder.getRating() == Protos.Analysis.Rating.NONE) {
            final String headline = builder.getHeadline();
            if (!HEARTBEAT_HEADER.equals(headline)) {
                this.logger.debug("<handle> incoming record ignored, reason: without rating: " + builder.getHeadline());
            }
            return;
        }
        if (builder.getIidCount() == 0) {
            this.logger.info("<handle> incoming record ignored, reason: without iid " + builder.getHeadline()
                    + ", " + newsRecord.getAttributes(NewsAttributeEnum.ISIN));
            return;
        }
        if (builder.getIidCount() > 1) {
            this.logger.info("<handle> incoming record ignored, reason: multiple iids " + builder.getHeadline()
                    + ", " + builder.getIidList());
            return;
        }

        this.logger.info("<handle> processing incoming record '" + builder.getHeadline()
                + "' ratingId: " + builder.getRatingId());
        incompleteAnalysesCache.add(newsRecord);
        processCache();
    }


    // must use selectorThread here
    private void processCache() {
        if (incompleteAnalysesCache.size() > 0) {
            logger.info("<processCache> trying to resolve news for ratingIds: " + cacheContent());
            final Iterator<NewsRecordImpl> iter = incompleteAnalysesCache.iterator();
            while (iter.hasNext()) {
                // retrieve
                final NewsRecordImpl entry = iter.next();
                final String entryId = entry.getNdbRatingId();
                final NewsResponse result = newsServer.getNews(createNewsRequest(entryId));
                final List<NewsRecord> records = result.getRecords();
                if (records.size() >= 1) {
                    push2Provider(records.get(0).getHeadline(), records.get(0).getText(), entry);
                    iter.remove();
                    logger.info("<processCache> resolved news for ratingId: " + entry.getNdbRatingId()
                            + " pushing to provider headline was '" + records.get(0).getHeadline() + "'");
                } else if (entry.getTimestamp().plusHours(TIMEOUT_HOURS).isBeforeNow()) {
                    push2Provider(DUMMY_HEADLINE_1 + entryId + DUMMY_HEADLINE_2, DUMMY_CONTENT, entry);
                    iter.remove();
                    logger.info("<processCache> resolving news timed out for for ratingId: " + entry.getNdbRatingId()
                            + " using default header and content ");
                }
            }
        }
        persistCache();
    }

    private void push2Provider(String headline, String content, NewsRecordImpl entry) {
        final Protos.Analysis.Builder builder = buildAnalysis(entry);
        builder.setHeadline(headline);
        builder.setText(0, content);
        this.provider.addAnalysis(builder);
    }

    @ManagedOperation
    public void processCacheInSelectorThread() {
        selectorThread.invokeLater(this::processCache);
    }

    private void persistCache() {
        logger.info("<persistCache> into: " + persistenceFile);
        try (FileOutputStream fileOutputStream = new FileOutputStream(persistenceFile)) {
            logger.info("<persistCache> writing " + cacheContent());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(incompleteAnalysesCache);
            objectOutputStream.close();
        } catch (IOException ex) {
            logger.warn("<persistCache> error while persisting data, ignored, we might lose some analyses", ex);
        }
    }

    private void restoreCache() {
        if (!persistenceFile.exists()) {
            logger.info("<restoreCache> no file found at: " + persistenceFile);
            return;
        }
        logger.info("<restoreCache> from file: " + persistenceFile);
        try (FileInputStream fileInputStream = new FileInputStream(persistenceFile)) {
            // read as hashmap
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Set<NewsRecordImpl> hashSet = new HashSet<>();
            Object deserialized = objectInputStream.readObject();
            hashSet.addAll((HashSet<NewsRecordImpl>) deserialized);
            objectInputStream.close();
            incompleteAnalysesCache.addAll(hashSet);
        } catch (IOException | ClassNotFoundException ex) {
            logger.warn("<restoreCache> error while restoring data, ignored, we might lose some analyses", ex);
        }
        logger.info("<restoreCache> read '" + cacheContent() + "'");
    }

    private String cacheContent() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("unresolved ratingIds in cache: ");
        incompleteAnalysesCache.forEach(newsRecord -> stringBuilder.append(newsRecord.getNdbRatingId() + ", "));
        return stringBuilder.toString();
    }

    private NewsRequest createNewsRequest(String ratingId) {
        final NewsRequest request = new NewsRequest();
        BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term(NewsIndexConstants.FIELD_RATING_ID, ratingId)), BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term(NewsIndexConstants.FIELD_LANGUAGE, "de")), BooleanClause.Occur.MUST);
        request.setLuceneQuery(query);
        request.setProfile(ProfileFactory.valueOf(true));
        return request;
    }

}
