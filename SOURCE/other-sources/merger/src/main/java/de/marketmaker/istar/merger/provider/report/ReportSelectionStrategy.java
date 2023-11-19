/*
 * ReportSelectionStrategy.java
 *
 * Created on 23.05.12 16:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.report;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.domain.data.DownloadableItem;

import static de.marketmaker.istar.domain.data.DownloadableItem.Source;

/**
 * @author zzhao
 */
public interface ReportSelectionStrategy {

    Map<String, Map<Source, List<DownloadableItem>>> select(
            Map<String, Map<Source, List<DownloadableItem>>> reports);

    /**
     * @see #select(Map)
     */
    class IncludeOne implements ReportSelectionStrategy {

        private final List<Source> sources;

        public IncludeOne(List<Source> sources) {
            this.sources = sources;
        }

        /**
         * Includes for every report type reports for one of the specified ordered sources.
         * If multiple sources are available, the order of the {@link #sources} prioritizes.
         *
         * @return Report items mapped by one source and grouped by report type.
         */
        @Override
        public Map<String, Map<Source, List<DownloadableItem>>> select(
                Map<String, Map<Source, List<DownloadableItem>>> reports) {
            final Map<String, Map<Source, List<DownloadableItem>>> map =
                    new HashMap<>();
            for (Map.Entry<String, Map<Source, List<DownloadableItem>>> entry : reports.entrySet()) {
                for (Source src : this.sources) {
                    if (entry.getValue().containsKey(src)) {
                        final EnumMap<Source, List<DownloadableItem>> items =
                                new EnumMap<>(Source.class);
                        items.put(src, entry.getValue().get(src));
                        map.put(entry.getKey(), items);
                        break; // continue with next report type
                    }
                }
            }
            return map;
        }
    }

    /**
     * @see #select(Map)
     */
    class IncludeThis implements ReportSelectionStrategy {

        private final Source source;

        public IncludeThis(Source source) {
            this.source = source;
        }

        /**
         * Includes for every report type reports for the specified {@link #source}.
         *
         * @return Report items mapped by one source and grouped by report type.
         */
        @Override
        public Map<String, Map<Source, List<DownloadableItem>>> select(
                Map<String, Map<Source, List<DownloadableItem>>> reports) {
            final Map<String, Map<Source, List<DownloadableItem>>> map =
                    new HashMap<>();
            for (Map.Entry<String, Map<Source, List<DownloadableItem>>> entry : reports.entrySet()) {
                if (entry.getValue().containsKey(this.source)) {
                    final EnumMap<Source, List<DownloadableItem>> items =
                            new EnumMap<>(Source.class);
                    items.put(this.source, entry.getValue().get(this.source));
                    map.put(entry.getKey(), items);
                }
            }
            return map;
        }
    }

    /**
     * @see #select(Map)
     */
    class IncludeAll implements ReportSelectionStrategy {

        private final EnumSet<Source> sources;

        public IncludeAll(EnumSet<Source> sources) {
            this.sources = sources;
        }

        @Override
        public Map<String, Map<Source, List<DownloadableItem>>> select(
                Map<String, Map<Source, List<DownloadableItem>>> reports) {
            final Map<String, Map<Source, List<DownloadableItem>>> map =
                    new HashMap<>();
            for (Map.Entry<String, Map<Source, List<DownloadableItem>>> entry : reports.entrySet()) {
                for (Source src : this.sources) {
                    if (entry.getValue().containsKey(src)) {
                        if (!map.containsKey(entry.getKey())) {
                            map.put(entry.getKey(),
                                    new EnumMap<>(Source.class));
                        }

                        final Map<Source, List<DownloadableItem>> items = map.get(entry.getKey());
                        items.put(src, entry.getValue().get(src));
                    }
                }
            }
            return map;
        }
    }
}
