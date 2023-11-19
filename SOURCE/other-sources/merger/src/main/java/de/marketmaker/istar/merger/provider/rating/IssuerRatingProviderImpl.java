/*
 * IssuerRatingProviderImpl.java
 *
 * Created on 07.05.12 11:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.rating.Rating;
import de.marketmaker.istar.domain.rating.RatingSystem;
import de.marketmaker.istar.domainimpl.rating.RatingSystemProvider;
import de.marketmaker.istar.domainimpl.rating.RatingSystemProviderImpl;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaItem;
import de.marketmaker.istar.merger.web.finder.Query2Term;
import de.marketmaker.istar.merger.web.finder.Term;

/**
 * //TODO might need to support search on other fields and sort on other fields
 *
 * @author zzhao
 */
public class IssuerRatingProviderImpl implements IssuerRatingProvider, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor monitor;

    private File issuerRatingFile;

    private Executor executor;

    private AtomicReference<Map<IssuerRatingMetaDataKey, List<Object>>> metaDataRef = new AtomicReference<>();

    private AtomicReference<List<IssuerRatingImpl>> ratingsRef = new AtomicReference<>();

    private RatingSystemProvider ratingSystemProvider;

    public void setRatingSystemProvider(RatingSystemProvider ratingSystemProvider) {
        this.ratingSystemProvider = ratingSystemProvider;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.isTrue(null != this.issuerRatingFile
                && this.issuerRatingFile.isFile()
                && this.issuerRatingFile.canRead(),
                "invalid issuer rating file: " + this.issuerRatingFile);
        Assert.notNull(this.ratingSystemProvider, "issuer rating system provider required");
        Assert.notNull(this.monitor, "active monitor required for issuer rating file");
        Assert.notNull(this.executor, "an executor is required for loading issuer ratings");
        reloadIssuerRating();
        final FileResource fr = new FileResource(this.issuerRatingFile);
        fr.addPropertyChangeListener(evt -> executor.execute(this::reloadIssuerRating));
        this.monitor.addResource(fr);
    }

    void reloadIssuerRating() {
        final TimeTaker tt = new TimeTaker();
        InputStream is = null;
        try {
            is = getInputStream();
            final IssuerRatingReader reader = new IssuerRatingReader();
            reader.parse(is);
            this.metaDataRef.set(reader.getMetaData());
            this.ratingsRef.set(reader.getIssuerRatings());
        } catch (Exception e) {
            this.logger.error("<reloadIssuerRating> failed reading issuer ratings", e);
            throw new IllegalStateException("failed reading issuer ratings", e);
        } finally {
            IoUtils.close(is);
            if (null != this.ratingsRef.get()) {
                this.logger.info("<reloadIssuerRating> took: " + tt + " for "
                        + this.ratingsRef.get().size() + " ratings");
            }
        }
    }

    protected InputStream getInputStream() throws IOException {
        final FileInputStream is = new FileInputStream(this.issuerRatingFile);
        if (this.issuerRatingFile.getName().endsWith(".gz")) {
            return new GZIPInputStream(is);
        }
        return is;
    }

    public void setMonitor(ActiveMonitor monitor) {
        this.monitor = monitor;
    }

    public void setIssuerRatingFile(File issuerRatingFile) {
        this.issuerRatingFile = issuerRatingFile;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public Map<IssuerRatingMetaDataKey, List<Object>> getMetaData(boolean withDetailedSymbol) {
        final Map<IssuerRatingMetaDataKey, List<Object>> mdm = this.metaDataRef.get();
        if (null == mdm) {
            return Collections.emptyMap();
        }

        final Map<IssuerRatingMetaDataKey, List<Object>> ret = new HashMap<>();
        for (Map.Entry<IssuerRatingMetaDataKey, List<Object>> entry : mdm.entrySet()) {
            final RatingSystem rs = getRatingSystem(entry.getKey().getDesc());
            if (null != rs) {
                final List<Object> list = entry.getValue();
                final TreeSet<Object> values = new TreeSet<>(new RatingComparator(rs, false));
                for (Object val : list) {
                    values.add(withDetailedSymbol ? rs.getRating((String) val).getFullSymbol() :
                            rs.getRating((String) val).getSymbol());
                }
                ret.put(entry.getKey(), new ArrayList<>(values));
            }
            else {
                ret.put(entry.getKey(), entry.getValue());
            }
        }

        return ret;
    }

    private static class RatingComparator implements Comparator<Object> {

        private final RatingSystem ratingSystem;

        private final boolean ascending;

        private RatingComparator(RatingSystem ratingSystem, boolean ascending) {
            this.ratingSystem = ratingSystem;
            this.ascending = ascending;
        }

        @Override
        public int compare(Object o1, Object o2) {
            final Rating ratingA = this.ratingSystem.getRating((String) o1);
            final Rating ratingB = this.ratingSystem.getRating((String) o2);

            final int result = ratingA.compareTo(ratingB);
            return this.ascending ? -result : result;
        }
    }

    private RatingSystem getRatingSystem(IssuerRatingDescriptor desc) {
        return desc.isRating() ? this.ratingSystemProvider.getRatingSystem(desc.name()) : null;
    }

    @Override
    public IssuerRatingSearchResponse search(IssuerRatingSearchRequest req) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<search> " + req);
        }
        final TimeTaker tt = new TimeTaker();
        try {
            return searchIntern(req);
        } finally {
            this.logger.info("<search> took: " + tt);
        }
    }

    private IssuerRatingSearchResponse searchIntern(IssuerRatingSearchRequest req) {
        IssuerRatingFilter filter;
        try {
            filter = getFilter(req.getQuery());
        } catch (Exception e) {
            this.logger.warn("<searchIntern> failed creating filter for: " + req.getQuery(), e);
            final String message = e.getMessage();
            return new IssuerRatingSearchResponse(null == message ? e.toString() : message);
        }

        try {
            final List<IssuerRatingImpl> ratings = this.ratingsRef.get();
            final List<IssuerRatingImpl> result = new ArrayList<>();
            for (IssuerRatingImpl rating : ratings) {
                if (filter.evaluate(rating, req.isWithDetailedSymbol())) {
                    result.add(rating);
                }
            }
            return createResponse(req, result);
        } catch (Exception e) {
            this.logger.error("<searchIntern> failed searching for: " + req, e);
            return IssuerRatingSearchResponse.INVALID;
        }
    }

    private IssuerRatingSearchResponse createResponse(IssuerRatingSearchRequest req,
            List<IssuerRatingImpl> issuerRatings) {
        if (issuerRatings.isEmpty()) {
            return IssuerRatingSearchResponse.EMPTY;
        }
        issuerRatings.sort(new MyComparator(req, this.ratingSystemProvider));
        final Map<IssuerRatingMetaDataKey, Map<Object, MutableInt>> mm = new TreeMap<>();
        final ArrayList<IssuerRating> list = new ArrayList<>(req.getCount());
        for (int i = 0; i < issuerRatings.size(); i++) {
            final IssuerRatingImpl ir = issuerRatings.get(i);
            for (IssuerRatingMetaDataKey mdk : req.getMetaDataKeys()) {
                final Object prop = ir.getProperty(mdk.getDesc());
                if (null != prop) {
                    countMetaData(req.isAscending(), mm, mdk, prop, req.isWithDetailedSymbol());
                }
            }
            if (i >= req.getOffset() && list.size() < req.getCount()) {
                list.add(ir);
            }
        }

        final Map<IssuerRatingMetaDataKey, List<FinderMetaItem>> md =
                new TreeMap<>();
        for (Map.Entry<IssuerRatingMetaDataKey, Map<Object, MutableInt>> entry : mm.entrySet()) {
            md.put(entry.getKey(), createFinderMetaItemList(entry.getValue()));
        }

        return new IssuerRatingSearchResponse(issuerRatings.size(), list, md);
    }

    private void countMetaData(boolean ascending,
            Map<IssuerRatingMetaDataKey, Map<Object, MutableInt>> mm,
            IssuerRatingMetaDataKey mdk, Object prop, boolean withDetailedSymbol) {
        if (!mm.containsKey(mdk)) {
            // one meta data element for each meta data key
            if (null != getRatingSystem(mdk.getDesc())) {
                mm.put(mdk, new TreeMap<Object, MutableInt>(
                        new RatingComparator(getRatingSystem(mdk.getDesc()), ascending)));
            }
            else {
                mm.put(mdk, new TreeMap<Object, MutableInt>());
            }
        }

        final Map<Object, MutableInt> map = mm.get(mdk);
        final Object key = getMetaDataKey(mdk, prop, withDetailedSymbol);
        if (!map.containsKey(key)) {
            map.put(key, new MutableInt(0));
        }

        map.get(key).increment();
    }

    private Object getMetaDataKey(IssuerRatingMetaDataKey mdk, Object prop,
            boolean withDetailedSymbol) {
        if (!withDetailedSymbol && mdk.getDesc().isRating()) {
            final RatingSystem rs = getRatingSystem(mdk.getDesc());
            if (null != rs) {
                return rs.getRating((String) prop).getSymbol();
            }
        }

        return prop;
    }

    private List<FinderMetaItem> createFinderMetaItemList(Map<Object, MutableInt> map) {
        final ArrayList<FinderMetaItem> mi = new ArrayList<>(map.size());
        for (Map.Entry<Object, MutableInt> entry : map.entrySet()) {
            if (entry.getKey() instanceof RatingSource) {
                final RatingSource source = (RatingSource) entry.getKey();
                mi.add(new FinderMetaItem(source.name(), source.getFullName(), entry.getValue().intValue()));
            }
            else {
                final String key = String.valueOf(entry.getKey());
                mi.add(new FinderMetaItem(key, key, entry.getValue().intValue()));
            }
        }

        return mi;
    }

    private static class MyComparator implements Comparator<IssuerRatingImpl> {

        private final IssuerRatingDescriptor sortBy;

        private final boolean ascending;

        private final RatingSystem ratingSystem;

        private final boolean withDetailedSymbol;

        MyComparator(IssuerRatingSearchRequest req, RatingSystemProvider provider) {
            this.sortBy = IssuerRatingDescriptor.fromValue(req.getSortBy());
            this.ratingSystem = this.sortBy.isRating() ? provider.getRatingSystem(this.sortBy.name()) : null;
            this.ascending = req.isAscending();
            this.withDetailedSymbol = req.isWithDetailedSymbol();
        }

        @Override
        public int compare(IssuerRatingImpl o1, IssuerRatingImpl o2) {
            int result;

            final Comparable val1 = o1.getValue(this.sortBy);
            final Comparable val2 = o2.getValue(this.sortBy);

            if (null == val1 && null == val2) {
                result = 0;
            }
            else if (null == val1) {
                result = -1;
            }
            else if (null == val2) {
                result = 1;
            }
            else {
                if (null != this.ratingSystem) {
                    final Rating ratingA = this.ratingSystem.getRating((String) val1);
                    final Rating ratingB = this.ratingSystem.getRating((String) val2);
                    result = 0 - ratingA.compareTo(ratingB);
                }
                else {
                    result = val1.compareTo(val2);
                }
            }

            return this.ascending ? result : -result;
        }
    }

    private IssuerRatingFilter getFilter(String query) throws Exception {
        if (StringUtils.hasText(query)) {
            final Term term = Query2Term.toTerm(query);
            final IssuerRatingTermVisitor visitor = new IssuerRatingTermVisitor(this.ratingSystemProvider);
            term.accept(visitor);

            return visitor.getFilter();
        }

        return IssuerRatingFilter.TRUE;
    }

    public static void main(String[] args) throws Exception {
        emitUsedMem();
        final File file = new File(LocalConfigProvider.getProductionBaseDir(),
                "var/data/provider/istar-issuer-rating-data.xml.gz");
        final IssuerRatingProviderImpl provider = new IssuerRatingProviderImpl();
        provider.setIssuerRatingFile(file);
        final RatingSystemProviderImpl ranking = new RatingSystemProviderImpl();
        ranking.setResource(new FileSystemResource(new File(LocalConfigProvider.getIstarSrcDir(),
                "merger/src/conf/issuer_ratings.properties")));
        ranking.initialize();
        provider.setRatingSystemProvider(ranking);
        provider.reloadIssuerRating();

        Runtime.getRuntime().gc();
        emitUsedMem();
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String query;
        do {
            System.out.println();
            emitUsedMem();
            System.out.print("Query: ");
            query = br.readLine();
            if (StringUtils.hasText(query)) {
                final String[] parts = query.split("\\|");
                try {
                    final IssuerRatingSearchRequest req =
                            new IssuerRatingSearchRequest(0, 3, "IssuerName", true, parts[0],
                                    parts.length == 2 && Boolean.parseBoolean(parts[1]),
                                    IssuerRatingMetaDataKey.ALL_KEYS);
                    final IssuerRatingSearchResponse resp = provider.search(req);
                    emitResp(resp);
                } catch (Exception e) {
                    System.out.println(e.getLocalizedMessage());
                }
            }
        } while (StringUtils.hasText(query));
        System.out.println("quit query");
    }

    private static final double MB = 1024d * 1024d;

    private static void emitUsedMem() {
        final Runtime runtime = Runtime.getRuntime();
        final long used = runtime.totalMemory() - runtime.freeMemory();
        final double usedMB = used / MB;
        System.out.println("Used Mem: " + usedMB + " M");
    }

    private static void emitResp(IssuerRatingSearchResponse resp) {
        if (!resp.isValid()) {
            System.out.println("Invalid response: " + resp.getMessage());
        }
        else {
            System.out.println("Total Count: " + resp.getTotalCount());
            if (resp.getTotalCount() > 0) {
                final List<IssuerRating> issuerRatings = resp.getIssuerRatings();
                for (IssuerRating issuerRating : issuerRatings) {
                    System.out.println(issuerRating);
                }
                for (Map.Entry<IssuerRatingMetaDataKey, List<FinderMetaItem>> entry : resp.getMetaData().entrySet()) {
                    if (!entry.getValue().isEmpty()) {
                        System.out.println();
                        System.out.println(entry.getKey());
                        for (FinderMetaItem item : entry.getValue()) {
                            System.out.print(" " + item.getName() + ": " + item.getCount());
                        }
                    }
                }
            }
        }
    }
}
