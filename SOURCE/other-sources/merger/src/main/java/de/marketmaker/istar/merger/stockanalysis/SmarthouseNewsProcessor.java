/*
 * AnalysisNewsProcessor.java
 *
 * Created on 06.10.2009 10:15:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.stockanalysis;

import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;
import de.marketmaker.istar.news.backend.NewsRecordHandler;
import de.marketmaker.istar.news.backend.NewsSymbolIdentifier;
import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;
import de.marketmaker.istar.news.frontend.NewsRecord;
import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsResponse;
import de.marketmaker.istar.news.frontend.NewsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class SmarthouseNewsProcessor implements InitializingBean, NewsRecordHandler {

    public static void main(String[] args) throws Exception {
        final SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setPassword("aktiencheck");
        ds.setUsername("aktiencheck");
        ds.setUrl("jdbc:mysql://proton/aktiencheck");

        StockAnalysisDaoDb dao = new StockAnalysisDaoDb();
        dao.setDataSource(ds);
        dao.afterPropertiesSet();

        final SmarthouseNewsProcessor p = new SmarthouseNewsProcessor();
        p.setRatingPatternsFile(new File("d:/temp/ratingPatterns.prop"));
        p.setDao(dao);
        p.afterPropertiesSet();

        InstrumentDirDao idf = new InstrumentDirDao(new File("d:/produktion/var/data/instrument/work0/data/instruments/"));

        final GZIPInputStream gis = new GZIPInputStream(new FileInputStream(new File("d:/temp/analyses.gz")));
        final ObjectInputStream ois =
                new ObjectInputStream(gis);
        int n = 0;
        try {
            while (true) {
                NewsRecordImpl nr = (NewsRecordImpl) ois.readObject();
                final Set<String> iids = nr.getAttributes(NewsAttributeEnum.IID);
                if (iids.isEmpty() || iids.size() > 1) {
                    continue;
                }
                final Instrument instrument = idf.getInstrument(Long.parseLong(iids.iterator().next()));
                if (instrument == null) {
                    continue;
                }
                n++;
                nr.setInstruments(Collections.singletonList(instrument));
                p.handle(nr);
            }
        } catch (Exception e) {
            // return;
        } finally {
            idf.close();
            System.out.println("#Analyses = " + n);
        }

    }

    private String selector = "3004";

    private StockAnalysisDao dao;

    private NewsSymbolIdentifier symbolIdentifier;

    private NewsServer newsServer;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EnumMap<Rating, Pattern> ratingPatterns =
            new EnumMap<>(Rating.class);

    private AtomicInteger numNewsProcessed = new AtomicInteger();

    private File ratingPatternsFile;

    public void setNewsServer(NewsServer newsServer) {
        this.newsServer = newsServer;
    }

    public void setSymbolIdentifier(NewsSymbolIdentifier symbolIdentifier) {
        this.symbolIdentifier = symbolIdentifier;
    }

    public void setRatingPatternsFile(File ratingPatternsFile) {
        this.ratingPatternsFile = ratingPatternsFile;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.ratingPatternsFile == null) {
            throw new IllegalStateException("ratingPatternsFile is null");
        }
        readRatingPatterns();
    }

    @ManagedOperation
    public void readRatingPatterns() {
        final Properties properties;
        try {
            properties = PropertiesLoader.load(this.ratingPatternsFile);
        } catch (IOException e) {
            this.logger.error("<readRatingPatterns> failed to read "
                    + this.ratingPatternsFile.getAbsolutePath(), e);
            return;
        }
        synchronized (this.ratingPatterns) {
            for (final String name : properties.stringPropertyNames()) {
                try {
                    final Rating rating = Rating.valueOf(name);
                    final String value = properties.getProperty(name);
                    if (StringUtils.hasText(value)) {
                        this.ratingPatterns.put(rating, Pattern.compile(value, Pattern.CASE_INSENSITIVE));
                    }
                    else {
                        this.ratingPatterns.remove(rating);
                    }
                } catch (Exception e) {
                    this.logger.error("<readRatingPatterns> failed for '" + name
                            + "'='" + properties.getProperty(name) + "'", e);
                }
            }
        }
    }

    public void handle(NewsRecordImpl newsRecord) {
        if (this.numNewsProcessed.getAndIncrement() == 0 && this.newsServer != null) {
            addMissing(new LocalDate());
        }

        if (!newsRecord.getSelectors().contains(this.selector)) {
            return;
        }
        final SnapField field = newsRecord.getField(VwdFieldDescription.NDB_Analyst.id());
        if (!field.isDefined()) {
            this.logger.warn("<handle> no analyst in " + newsRecord.getId());
            return;
        }
        final String analyst = field.getValue().toString().replace("&amp;", "&");

        final Set<Instrument> instruments = newsRecord.getInstruments();
        if (instruments == null || instruments.isEmpty()) {
            this.logger.warn("<handle> no instrument for analysis " + newsRecord.getId());
            return;
        }
        if (instruments.size() > 1) {
            this.logger.warn("<handle> multiple instruments for analysis " + newsRecord.getId() + ": " + instruments);
            return;
        }
        final Rating rating = getRating(newsRecord);
        final Instrument instrument = instruments.iterator().next();

        final Long id = Long.parseLong(newsRecord.getId(), Character.MAX_RADIX);

        final int numInserted = this.dao.storeAnalysis(id, analyst, instrument, rating, newsRecord.getTimestamp(),
                "Analyse", null, newsRecord.getHeadline(), newsRecord.getText(),
                null, null, null);

        if (numInserted == 1 && this.logger.isDebugEnabled()) {
           this.logger.debug("<handle> inserted " + newsRecord.getId() + ": " + newsRecord.getHeadline());
        }
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "from", description = "from-date (yyyy-MM-dd)")
    })
    public void addMissing(final String from) {
        // if still 0, we don't want to trigger addMissing called from handle
        this.numNewsProcessed.compareAndSet(0, 1);
        try {
            addMissing(parseLocalDate(from));
        } catch (Throwable t) {
            this.logger.error("<addMissing> failed", t);
        }
    }

    private LocalDate parseLocalDate(String to) {
        return DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(to).toLocalDate();
    }

    private void addMissing(final LocalDate from) {
        int offset = 0;
        while (true) {
            final NewsRequest request = createRequest(from, offset);
            final NewsResponse newsResponse = this.newsServer.getNews(request);
            for (NewsRecord record : newsResponse.getRecords()) {
                final NewsRecordImpl impl = (NewsRecordImpl) record;
                if (this.symbolIdentifier != null) {
                    this.symbolIdentifier.assignInstrumentsTo(impl);
                }
                handle(impl);
            }
            offset += request.getCount();
            if (newsResponse.getHitCount() < offset) {
                break;
            }
        }
    }

    private NewsRequest createRequest(LocalDate from, int offset) {
        final NewsRequest result = new NewsRequest();
        result.setFrom(from.toDateTimeAtStartOfDay());
        result.setCount(50);
        result.setOffset(offset);
        result.setWithHitCount(true);
        result.setProfile(ProfileFactory.valueOf(true));
        result.setLuceneQuery(createQuery());
        return result;
    }

    private TermQuery createQuery() {
        return new TermQuery(new Term(NewsIndexConstants.FIELD_SELECTOR, this.selector));
    }

    private Rating getRating(NewsRecordImpl newsRecord) {
        final SnapField rating = newsRecord.getField(VwdFieldDescription.NDB_AnalystRating.id());
        if (!rating.isDefined()) {
            return Rating.NO_RATING;
        }
        final String value = rating.getValue().toString();

        for (Map.Entry<Rating, Pattern> e : this.ratingPatterns.entrySet()) {
            if (matches(e.getValue(), value)) {
                return e.getKey();
            }
        }
        this.logger.warn("<getRating> uncategorized value: '" + value + "'");
        return Rating.NO_RATING;
    }

    private boolean matches(Pattern p, String value) {
        return p.matcher(value).matches();
    }

    public void setDao(StockAnalysisDao dao) {
        this.dao = dao;
    }
}
