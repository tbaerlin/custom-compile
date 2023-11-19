/*
 * BestToolVisitor.java
 *
 * Created on 03.08.2006 18:35:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.PagedResultSorter;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.ratios.RatioFieldDescription;

import de.marketmaker.istar.ratios.frontend.BestToolRatioSearchResponse.BestToolElement;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BestToolVisitor implements MergeableSearchEngineVisitor<BestToolVisitor> {
    private interface IntervalOperator {
        Object handle(Long value);
    }

    private static class DecimalIntervalOperator implements IntervalOperator {
        private final long interval;

        public DecimalIntervalOperator(String command, boolean isPercent) {
            final long tmp = PriceCoder.encode(command.substring(PREFIX_INTERVAL.length()));
            this.interval = PriceCoder.toDefaultEncoding(tmp) / (isPercent ? 100 : 1);
        }

        public Long handle(Long value) {
            if (value == null) {
                return 0L;
            }
            return value - (value % this.interval) + (value > 0 ? this.interval : 0);
        }
    }

    private static class DateIntervalOperator implements IntervalOperator {
        private static class Item {
            private final String key;

            private final int lower;

            private final int upper;

            private final boolean endless;

            private Item(String key) {
                this.key = key;
                this.endless = true;
                this.lower = Integer.MIN_VALUE;
                this.upper = Integer.MAX_VALUE;
            }

            private Item(String key, int lower, int upper) {
                this.key = key;
                this.lower = lower;
                this.upper = upper;
                this.endless = false;
            }

            public String getKey() {
                return key;
            }

            public int getLower() {
                return lower;
            }

            public int getUpper() {
                return upper;
            }

            public boolean isEndless() {
                return endless;
            }

            @Override
            public String toString() {
                return "Item{" +
                        "key='" + key + '\'' +
                        ", lower=" + lower +
                        ", upper=" + upper +
                        ", endless=" + endless +
                        '}';
            }
        }

        private final List<Item> items = new ArrayList<>();

        private final RatioFieldDescription.Field field;

        public DateIntervalOperator(String command, RatioFieldDescription.Field field) {
            this.field = field;

            final String def = command.substring(PREFIX_INTERVAL.length());
            final String[] intervals = def.split(Pattern.quote(";"));

            for (final String interval : intervals) {
                final boolean endless = SearchParameterParser.NULL_VALUE.equals(interval);

                if (endless) {
                    this.items.add(new Item(interval));
                    continue;
                }

                final String[] tokens = interval.split(Pattern.quote("_"));
                if (tokens.length != 2) {
                    continue;
                }

                final int start = SearchParameterParser.getDateRangeParameter(tokens[0], Integer.MIN_VALUE);
                final int end = SearchParameterParser.getDateRangeParameter(tokens[1], Integer.MAX_VALUE);

                this.items.add(new Item(interval, start, end));
            }
        }

        public String handle(Long date) {
            for (final Item item : this.items) {
                if (date == null) {
                    if (item.isEndless()) {
                        return SearchParameterParser.NULL_VALUE;
                    }

                    if (this.field.isNullAsMin() != null && this.field.isNullAsMin()
                            && (item.getLower() == Integer.MIN_VALUE || !this.field.isNullAsMin() && item.getUpper() == Integer.MAX_VALUE)) {
                        return item.getKey();
                    }

                    continue;
                }

                if (date >= item.getLower() && date < item.getUpper()) {
                    return item.getKey();
                }
            }
            return null;
        }
    }

    private static final String PREFIX_INTERVAL = "interval:";

    public static final String KEY_GROUP_BY = "bt:groupBy";

    public static final String KEY_SOURCE = "bt:source";

    public static final String KEY_OPERATOR = "bt:operator";

    private RatioFieldDescription.Field[] groupByField;

    private RatioFieldDescription.Field[] sourceFields;

    private IntervalOperator[] operators;

    private int[] localeIndexes;

    private boolean[] sourceIsInstrumentField;

    private boolean[] groupByIsInstrumentField;

    private final Map<String, Map<Object, PagedResultSorter<BestToolElement>>> result
            = new HashMap<>();

    private int numResults;

    public void init(SearchParameterParser spp) {
        this.numResults = Integer.parseInt(spp.getParameterValue(SearchParameterParser.PARAM_NUMRESULTS));

        final String source0 = spp.getParameterValue(KEY_SOURCE);
        final String[] sources0 = source0.split(",");
        this.sourceFields = new RatioFieldDescription.Field[sources0.length];
        this.sourceIsInstrumentField = new boolean[sources0.length];
        for (int i = 0; i < sources0.length; i++) {
            this.sourceFields[i] = RatioFieldDescription.getFieldByName(sources0[i]);
            this.sourceIsInstrumentField[i] = this.sourceFields[i].isInstrumentField();
        }

        final String source1 = spp.getParameterValue(KEY_GROUP_BY);
        final String[] sources1 = source1.split(",");
        this.groupByField = new RatioFieldDescription.Field[sources1.length];
        this.groupByIsInstrumentField = new boolean[sources1.length];
        this.localeIndexes = new int[sources1.length];
        for (int i = 0; i < sources1.length; i++) {
            this.groupByField[i] = RatioFieldDescription.getFieldByName(sources1[i]);
            this.groupByIsInstrumentField[i] = this.groupByField[i].isInstrumentField();
            this.localeIndexes[i] = RatioFieldDescription.getLocaleIndex(this.groupByField[i], spp.getLocales());
        }


        final String operator = spp.getParameterValue(KEY_OPERATOR);
        if (operator == null) {
            this.operators = new IntervalOperator[this.groupByField.length];
        }
        else {
            final String[] strings = operator.split(",");
            this.operators = new IntervalOperator[strings.length];
            for (int i = 0; i < strings.length; i++) {
                if (StringUtils.hasText(strings[i]) && strings[i].startsWith(PREFIX_INTERVAL)) {
                    if (isLongBased(this.groupByField[i])) {
                        this.operators[i] = new DecimalIntervalOperator(strings[i],
                                this.groupByField[i].isPercent());
                    }
                    else if (isDateBased(this.groupByField[i])) {
                        this.operators[i] = new DateIntervalOperator(strings[i], this.groupByField[i]);
                    }
                }
            }
        }
    }

    @Override
    public void visit(RatioData data) {
        final QuoteRatios qr = data.getSearchResult();
        final String groupBy0 = getGroupBy0(qr);
        if (groupBy0 == null) {
            return;
        }

        final Object groupBy1 = getGroupBy1(qr);
        if (groupBy1 == null) {
            return;
        }

        final Selectable s = getSelectable(qr, this.sourceIsInstrumentField[0]);
        final Long source = s.getLong(this.sourceFields[0].id());
        if (isUndefined(source)) {
            return;
        }

        add(qr.getId(), groupBy0, groupBy1, source);
    }

    public void add(long qid, String groupBy0, Object groupBy1, long source) {
        final PagedResultSorter<BestToolElement> sorter = getSorter(groupBy0, groupBy1);
        final BestToolElement last = sorter.getLast();
        if (last == null || last.getSourceValue() < source) {
            sorter.add(new BestToolElement(qid, source));
        }
    }

    private PagedResultSorter<BestToolElement> getSorter(
            String groupBy0, Object groupBy1) {
        Map<Object, PagedResultSorter<BestToolElement>> firstMap = this.result.get(groupBy0);

        if (firstMap == null) {
            firstMap = new HashMap<>();
            this.result.put(groupBy0, firstMap);
        }

        PagedResultSorter<BestToolElement> result = firstMap.get(groupBy1);
        if (result == null) {
            result = new PagedResultSorter<>(0, this.numResults, null);
            firstMap.put(groupBy1, result);
        }
        return result;
    }


    @Override
    public BestToolVisitor merge(BestToolVisitor v) {
        for (Map.Entry<String, Map<Object, PagedResultSorter<BestToolElement>>> e : v.result.entrySet()) {
            Map<Object, PagedResultSorter<BestToolElement>> my = this.result.get(e.getKey());
            if (my != null) {
                merge(my, e.getValue());
            }
            else {
                this.result.put(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    private void merge(Map<Object, PagedResultSorter<BestToolElement>> my,
            Map<Object, PagedResultSorter<BestToolElement>> other) {
        for (Map.Entry<Object, PagedResultSorter<BestToolElement>> e : other.entrySet()) {
            PagedResultSorter<BestToolElement> existing = my.get(e.getKey());
            if (existing != null) {
                existing.merge(e.getValue());
            }
            else {
                my.put(e.getKey(), e.getValue());
            }
        }
    }

    private String getGroupBy0(QuoteRatios result) {
        final Selectable srGroupBy0 = getSelectable(result, this.groupByIsInstrumentField[0]);
        return MinMaxAvgVisitor.getString(srGroupBy0, this.groupByField[0], this.localeIndexes[0]);
    }

    private Object getGroupBy1(QuoteRatios result) {
        if (this.groupByField.length > 1) {
            final Selectable srGroupBy1 = getSelectable(result, this.groupByIsInstrumentField[1]);
            if (isLongBased(this.groupByField[1])) {
                final Long groupBy1Raw = srGroupBy1.getLong(this.groupByField[1].id());
                return getGroupBy1ForLong(groupBy1Raw);
            }
            else if (isDateBased(this.groupByField[1])) {
                final Integer groupBy1Raw = srGroupBy1.getInt(this.groupByField[1].id());
                return getGroupBy1ForDate(groupBy1Raw);
            }
            return null;
        }
        return 0L;
    }

    private boolean isDateBased(final RatioFieldDescription.Field field) {
        return field.type() == RatioFieldDescription.Type.DATE;
    }

    private boolean isLongBased(final RatioFieldDescription.Field field) {
        return field.type() == RatioFieldDescription.Type.DECIMAL
                || field.type() == RatioFieldDescription.Type.NUMBER;
    }

    private boolean isUndefined(Long value) {
        return value == null || value == Long.MAX_VALUE || value == Long.MIN_VALUE;
    }

    private boolean isUndefined(Integer value) {
        return value == null || value == Integer.MAX_VALUE || value == Integer.MIN_VALUE;
    }

    private Selectable getSelectable(QuoteRatios qr, final boolean isInstrumentField) {
        return isInstrumentField ? qr.getInstrumentRatios() : qr;
    }

    private long getGroupBy1ForLong(Long rawValue) {
        if (isUndefined(rawValue)) {
            return 0L;
        }
        if (this.operators[1] == null) {
            return rawValue;
        }
        return (Long) this.operators[1].handle(rawValue);
    }

    private String getGroupBy1ForDate(Integer rawValue) {
        if (this.operators[1] == null) {
            return null;
        }
        return (String) this.operators[1].handle(rawValue == null ? null : rawValue.longValue());
    }

    public BestToolRatioSearchResponse getResponse() {
        final Map<String, Map<Object, List<BestToolElement>>> map
                = new HashMap<>();
        for (String key : result.keySet()) {
            map.put(key, getResult(key));
        }
        return new BestToolRatioSearchResponse(map);
    }

    private HashMap<Object, List<BestToolElement>> getResult(String key) {
        HashMap<Object, List<BestToolElement>> result = new HashMap<>();
        for (Map.Entry<Object, PagedResultSorter<BestToolElement>> entry : this.result.get(key).entrySet()) {
            result.put(entry.getKey(), entry.getValue().getResult());
        }
        return result;
    }
}