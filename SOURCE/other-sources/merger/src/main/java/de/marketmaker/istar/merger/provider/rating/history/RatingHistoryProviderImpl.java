/*
 * RatingHistoryProviderImpl.java
 *
 * Created on 11.09.12 14:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating.history;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.concurrent.ExecutorServiceUtil;
import de.marketmaker.istar.domain.rating.Rating;
import de.marketmaker.istar.domain.rating.RatingSystem;
import de.marketmaker.istar.domainimpl.rating.RatingSystemProvider;
import de.marketmaker.istar.domainimpl.rating.RatingSystemProviderImpl;
import de.marketmaker.istar.merger.provider.protobuf.ProtobufDataReader;
import de.marketmaker.istar.merger.provider.protobuf.RatingHistoryProtos;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaItem;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author zzhao
 */
public class RatingHistoryProviderImpl extends ProtobufDataReader implements RatingHistoryProvider {

    private AtomicReference<Map<String, List<FinderMetaItem>>> metaRef
            = new AtomicReference<>(Collections.emptyMap());

    private final ExecutorService es;

    private RatingSystemProvider ratingSystemProvider;

    public RatingHistoryProviderImpl() {
        super(RatingHistoryProtos.RatingHistory.getDescriptor());
        this.es = Executors.newFixedThreadPool(1);
    }

    public void setRatingSystemProvider(RatingSystemProvider ratingSystemProvider) {
        this.ratingSystemProvider = ratingSystemProvider;
    }

    @Override
    public void destroy() throws Exception {
        ExecutorServiceUtil.shutdownAndAwaitTermination(this.es, 5);
        super.destroy();
    }

    @Override
    public RatingHistoryResponse getRatingHistory(RatingHistoryRequest req) {
        final RatingHistoryProtos.RatingHistory.Builder builder
                = RatingHistoryProtos.RatingHistory.newBuilder();
        try {
            if (build(req.getIid(), builder) && builder.isInitialized()) {
                return fromProtobuf(req.getIid(), builder.build());
            }
            else {
                return new RatingHistoryResponse(true, req.getIid());
            }
        } catch (InvalidProtocolBufferException e) {
            this.logger.error("<getRatingHistory> failed to deserialize rating history data for "
                    + req, e);
        }
        return new RatingHistoryResponse(false, req.getIid());
    }

    private RatingHistoryResponse fromProtobuf(long iid, RatingHistoryProtos.RatingHistory rh) {
        final HashMap<RatioFieldDescription.Field, RatingHistory> map
                = new HashMap<>();
        if (rh.getRatingsCount() > 0) {
            for (RatingHistoryProtos.RatingItem ri : rh.getRatingsList()) {
                final RatioFieldDescription.Field field = getField(ri.getRatingType());
                if (!map.containsKey(field)) {
                    map.put(field, new RatingHistory(field));
                }
                map.get(field).addRatingItem(new RatingHistory.RatingItem(
                        DateUtil.yyyyMmDdToLocalDate(ri.getRatingDate()), ri.getRatingValue()));
            }
        }

        return new RatingHistoryResponse(iid, new ArrayList<>(map.values()));
    }

    private RatioFieldDescription.Field getField(String type) {
        if (!StringUtils.hasText(type)) {
            throw new IllegalArgumentException("type required");
        }
        return RatioFieldDescription.getFieldByName(type);
    }

    @Override
    protected void onUpdate(File f) {
        super.onUpdate(f);

        this.es.execute(() -> {
            try {
                final HashMap<String, HashMap<String, MutableInt>> map = new HashMap<>();
                final long[][] kao = keysAndOffsets;
                for (long key : kao[0]) {
                    final RatingHistoryProtos.RatingHistory.Builder builder = RatingHistoryProtos.RatingHistory.newBuilder();
                    if (build(key, builder)) {
                        final RatingHistoryProtos.RatingHistory ratingHistory = builder.build();
                        ratingHistory.getRatingsList().stream().forEach(r -> {
                            if (!map.containsKey(r.getRatingType())) {
                                map.put(r.getRatingType(), new HashMap<>());
                            }
                            final HashMap<String, MutableInt> sub = map.get(r.getRatingType());
                            if (!sub.containsKey(r.getRatingValue())) {
                                sub.put(r.getRatingValue(), new MutableInt(0));
                            }
                            sub.get(r.getRatingValue()).increment();
                        });
                    }
                }

                final Map<String, List<FinderMetaItem>> result = map.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().entrySet()
                                .stream()
                                .flatMap(n -> Stream.of(new FinderMetaItem(
                                        n.getKey(), n.getKey(), n.getValue().intValue())))
                                .collect(Collectors.toList())));
                result.entrySet().forEach(e -> {
                    final String key = e.getKey().trim().toLowerCase();
                    final String sysKey = key.startsWith("rating") ? key.substring(6) : key;
                    final RatingSystem ratingSystem = this.ratingSystemProvider.getRatingSystem(sysKey);
                    Collections.sort(e.getValue(), (o1, o2) -> {
                        final Rating ratingA = ratingSystem.getRating(o1.getKey());
                        if (ratingA == null) {
                            this.logger.warn("<onUpdate> no rating found for " + o1.getKey());
                        }
                        final Rating ratingB = ratingSystem.getRating(o2.getKey());
                        if (ratingB == null) {
                            this.logger.warn("<onUpdate> no rating found for " + o2.getKey());
                        }
                        return ratingA == null
                                ? ratingB == null ? 0 : -1
                                : ratingB == null ? 1 : ratingA.compareTo(ratingB);
                    });
                });
                metaRef.set(result);
            } catch (InvalidProtocolBufferException e) {
                logger.error("<onUpdate> ", e);
            }
        });
    }

    @Override
    public Map<String, List<FinderMetaItem>> getRatingHistoryMetaData() {
        return this.metaRef.get();
    }

    public static void main(String[] args) throws Exception {
        final Path path = Paths.get("/home/zzhao/produktion/var/data/rating-history/mike-ratinghistory-test.20150414.154059.buf");
        final Path ratingPath = Paths.get("/home/zzhao/dev/istar/config/src/main/resources/istar-provider/conf/ratings.properties");

        final RatingSystemProviderImpl ratingSystemProvider = new RatingSystemProviderImpl();
        final RatingHistoryProviderImpl provider = new RatingHistoryProviderImpl();
        try {
            ratingSystemProvider.setResource(new FileSystemResource(ratingPath.toFile()));
            ratingSystemProvider.initialize();

            provider.setFile(path.toFile());
            provider.afterPropertiesSet();
            provider.setRatingSystemProvider(ratingSystemProvider);

            Map<String, List<FinderMetaItem>> listMap = provider.getRatingHistoryMetaData();
            while (listMap.isEmpty()) {
                listMap = provider.getRatingHistoryMetaData();
            }

            listMap.entrySet().forEach(e -> {
                System.out.println(e.getKey());
                e.getValue().forEach(i -> {
                    System.out.println(i.getKey() + ", " + i.getName() + ", " + i.getCount());
                });
            });
        } finally {
            provider.destroy();
        }
    }
}
