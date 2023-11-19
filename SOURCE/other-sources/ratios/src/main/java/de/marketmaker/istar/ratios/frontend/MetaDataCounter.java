/*
 * MetaDataVisitor.java
 *
 * Created on 06.09.11 12:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.mutable.MutableInt;

import de.marketmaker.istar.domain.rating.Rating;
import de.marketmaker.istar.domain.rating.RatingSystem;
import de.marketmaker.istar.domainimpl.rating.RatingSystemProvider;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For a given number of string fields, counts the number of times each different value
 * is present for all {@link RatioData}s passed to {@link #visit(RatioData)}.<p>
 * <b>Important</b> Optimized for performance, see comments.
 *
 * @author oflege
 * @author zzhao
 */
public class MetaDataCounter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListSelector.class);

    private interface IMetadataField<T> {
        boolean isInstrumentField();

        RatioFieldDescription.Field getField();

        int getLocaleIndex();

        void update(T val);

        Map<String, Integer> toResult();

        void merge(IMetadataField metadataField);
    }

    private abstract static class MetadataFieldBase<T> implements IMetadataField<T> {
        protected final RatioFieldDescription.Field field;

        protected final boolean instrumentField;

        protected MetadataFieldBase(RatioFieldDescription.Field field) {
            this.field = field;
            this.instrumentField = this.field.isInstrumentField();
        }

        @Override
        public boolean isInstrumentField() {
            return this.instrumentField;
        }

        @Override
        public RatioFieldDescription.Field getField() {
            return this.field;
        }

        @Override
        public int getLocaleIndex() {
            return -1;
        }
    }

    private static class MetadataField extends MetadataFieldBase<String> {

        private final int localeIndex;

        /**
         * The final result will be <tt>TreeMap&lt;String, Integer&gt;</tt>, but while we collect the
         * data we use a <tt>HashMap</tt> (faster lookup) and a <tt>MutableInt</tt> value (faster to
         * increment than an <tt>Integer</tt> with un-/boxing)
         */
        protected final Map<String, MutableInt> counts = new HashMap<>();

        private MetadataField(RatioFieldDescription.Field field, int localeIndex) {
            super(field);
            this.localeIndex = localeIndex;
        }

        public int getLocaleIndex() {
            return this.localeIndex;
        }

        public void update(String val) {
            increment(val, 1);
        }

        private void increment(String val, int amount) {
            if (!this.counts.containsKey(val)) {
                this.counts.put(val, new MutableInt(0));
            }
            this.counts.get(val).add(amount);
        }

        @Override
        public Map<String, Integer> toResult() {
            // TODO: wouldn't we need a Collator based on the field's/spp's locale for sorting?
            final TreeMap<String, Integer> result = new TreeMap<>();
            for (Map.Entry<String, MutableInt> entry : this.counts.entrySet()) {
                result.put(entry.getKey(), entry.getValue().toInteger());
            }
            return result;
        }

        @Override
        public void merge(IMetadataField metadataField) {
            if (!(metadataField instanceof MetadataField)) {
                throw new IllegalArgumentException("cannot merge with: " + metadataField.getClass());
            }
            final MetadataField mf = (MetadataField) metadataField;
            for (Map.Entry<String, MutableInt> e : mf.counts.entrySet()) {
                increment(e.getKey(), e.getValue().intValue());
            }
        }
    }

    private static class EnumSetMetadataField extends MetadataFieldBase<BitSet> {

        protected MutableInt[] enumSetCounts = new MutableInt[RatioEnumSetFactory.SIZE_IN_BITS];

        private EnumSetMetadataField(RatioFieldDescription.Field field) {
            super(field);
        }

        @Override
        public void update(BitSet val) {
            // counts each 1 bit
            val.stream().forEach(idx -> increment(idx, 1));
        }

        private void increment(int idx, int amount) {
            if (this.enumSetCounts[idx] == null) {
                this.enumSetCounts[idx] = new MutableInt(0);
            }
            this.enumSetCounts[idx].add(amount);
        }

        @Override
        public Map<String, Integer> toResult() {
            final TreeMap<String, Integer> result = new TreeMap<>();
            for (int i = 0; i < this.enumSetCounts.length; i++) {
                MutableInt count = this.enumSetCounts[i];
                if (null != count) {
                    final String name = RatioEnumSetFactory.fromPosition(this.field.id(), i);
                    if (name == null) {
                        LOGGER.warn("<toResult> " + "no name found for fieldId '" + this.field.id() + "' "
                                + " at position '" + i +"', ignoring value in MetaDataCounter ");
                    } else {
                        result.put(name, count.intValue());
                    }
                }
            }
            return result;
        }

        @Override
        public void merge(IMetadataField metadataField) {
            if (!(metadataField instanceof EnumSetMetadataField)) {
                throw new IllegalArgumentException("cannot merge with: " + metadataField.getClass());
            }
            final EnumSetMetadataField emf = (EnumSetMetadataField) metadataField;
            for (int i = 0; i < emf.enumSetCounts.length; i++) {
                MutableInt count = emf.enumSetCounts[i];
                if (null != count) {
                    increment(i, count.intValue());
                }
            }
        }
    }

    /**
     * Creates a new {@link MetaDataCounter} iff the {@link SearchParameterParser} has any
     * metadata fieldids set, otherwise null will be returned
     *
     * @param spp specifies which metadata to capture
     * @return new {@link MetaDataCounter} or null
     */
    static MetaDataCounter create(SearchParameterParser spp) {
        return spp.isWithMetadataCounting() ? new MetaDataCounter(spp) : null;
    }

    private final List<IMetadataField> fields;

    private final Set<Object> quoteValuesAggregator = new HashSet<>();

    private final RatingSystemProvider ratingSystemProvider;

    private final boolean withDetailedSymbol;

    private MetaDataCounter(SearchParameterParser spp) {
        this.ratingSystemProvider = spp.getRatingSystemProvider();
        this.withDetailedSymbol = spp.isWithDetailedSymbol();
        final List<Integer> fieldids = spp.getMetadataFieldids();
        this.fields = new ArrayList<>(fieldids.size());
        for (final Integer fieldid : fieldids) {
            final RatioFieldDescription.Field field = RatioFieldDescription.getFieldById(fieldid);
            if (field != null) {
                final int localeIndex = RatioFieldDescription.getLocaleIndex(field, spp.getLocales());
                if (field.isEnumSet()) {
                    this.fields.add(new EnumSetMetadataField(field));
                }
                else {
                    this.fields.add(new MetadataField(field, localeIndex));
                }
            }
        }
    }

    void visit(RatioData data) {
        // using the for(int i... loop is faster than using an Iterator as no object is created
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < this.fields.size(); i++) {
            final IMetadataField mf = this.fields.get(i);

            if (mf.isInstrumentField()) {
                final Object v = getValue(mf, data.getInstrumentRatios());
                if (v != null) {
                    mf.update(v);
                }
            }
            else {
                this.quoteValuesAggregator.clear();

                final QuoteRatios[] ratios = data.getQuoteRatios();
                //noinspection ForLoopReplaceableByForEach
                for (int j = 0; j < ratios.length; j++) {
                    final QuoteRatios qr = ratios[j];
                    if (!qr.isSelected()) {
                        continue;
                    }
                    final Object v = getValue(mf, qr);
                    if (v != null && this.quoteValuesAggregator.add(v)) {
                        mf.update(v);
                    }
                }
            }
        }
    }

    private Object getValue(IMetadataField mf, Selectable selectable) {
        if (mf.getField().type() == RatioFieldDescription.Type.STRING) {
            final RatingSystem ratingSystem = getRatingSystem((MetadataField) mf);
            if (null != ratingSystem) {
                final String str = selectable.getString(mf.getField().id());
                if (null != str) {
                    final Rating rating = ratingSystem.getRating(str);
                    return this.withDetailedSymbol ? rating.getFullSymbol() : rating.getSymbol();
                }
            }
            else {
                return selectable.getString(mf.getField().id(), mf.getLocaleIndex());
            }
        }
        else if (mf.getField().type() == RatioFieldDescription.Type.NUMBER) {
            final Long l = selectable.getLong(mf.getField().id());
            return (l != null) ? Long.toString(l) : null;
        }
        else if (mf.getField().isEnumSet()) {
            final BitSet esVal = selectable.getBitSet(mf.getField().id());
            return null != esVal && !esVal.isEmpty() ? esVal : null;
        }
        return null;
    }

    private RatingSystem getRatingSystem(MetadataField mf) {
        final RatioFieldDescription.Field field = mf.getField();
        if (null == field.getRatingSystemName()) {
            return null;
        }
        return this.ratingSystemProvider.getRatingSystem(field.getRatingSystemName());
    }

    public void merge(MetaDataCounter other) {
        for (int i = 0; i < this.fields.size(); i++) {
            this.fields.get(i).merge(other.fields.get(i));
        }
    }

    public Map<Integer, Map<String, Integer>> getResult() {
        final Map<Integer, Map<String, Integer>> result
                = new HashMap<>();
        for (final IMetadataField mf : this.fields) {
            result.put(mf.getField().id(), createSortedResult(mf));
        }
        return result;
    }

    private Map<String, Integer> createSortedResult(IMetadataField mf) {
        final String ratingSystemName = mf.getField().getRatingSystemName();
        final Map<String, Integer> metadata = mf.toResult();

        if (ratingSystemName == null)
            return metadata;
        else {
            final RatingSystem ratingSystem = this.ratingSystemProvider.getRatingSystem(ratingSystemName);
            if (ratingSystem == null)
                return metadata;
            Map<String, Integer> map = new TreeMap<>((Comparator<String> & Serializable)(o1, o2) -> {
                Rating v1 = ratingSystem.getRating(o1);
                Rating v2 = ratingSystem.getRating(o2);
                return v1.compareTo(v2);
            });
            map.putAll(metadata);
            return map;
        }
    }
}
