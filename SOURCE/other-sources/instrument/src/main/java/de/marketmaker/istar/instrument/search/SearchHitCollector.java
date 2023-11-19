/*
 * SearchHitCollector.java
 *
 * Created on 18.11.2007 19:01:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.instrument.IndexConstants;

/**
 * Collects search hits, counts hits by instrument type, and applies the filter that specifies
 * which instrument types are to be contained in the search result. An object of this type
 * will be created for each search.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class SearchHitCollector extends Collector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchHitCollector.class);

    private static FieldCache.ByteParser INSTRUMENT_TYPE_PARSER = new FieldCache.ByteParser() {
        public byte parseByte(String s) {
            return (byte) InstrumentTypeEnum.valueOf(s.toUpperCase()).ordinal();
        }
    };

    private static final boolean[] FILTER_ALL = new boolean[InstrumentTypeEnum.values().length];

    static {
        Arrays.fill(FILTER_ALL, true);
    }

    /**
     * array with indexReader.maxDoc() elements; contains the ordinal of the InstrumentTypeEnum
     * of the quote with the corresponding document number
     */
    private byte[] types;

    /**
     * array with indexReader.maxDoc() elements; contains an id for the instrument
     * of the quote with the corresponding document number; THIS IS NOT THE iid, we use consecutive
     * numbers from 0 to n to be able to store them in a BitSet as compact as possible
     */
    private int[] ids;

    private final int typeCounts[] = new int[InstrumentTypeEnum.values().length];

    /**
     * Delegate that collects the actual results, sorts them, etc.
     */
    private final Collector delegate;

    private final boolean[] filters;

    private final boolean[] countMask;

    private final boolean collectInstruments;

    private final BitSet instrumentsCollected = new BitSet(1 << 16);

    private int instrumentCount = 0;

    SearchHitCollector(SearchRequest sr, Collector delegate) {
        this.delegate = delegate;
        this.filters = initFilterTypes(sr);
        this.countMask = initCountMask(sr);
        this.collectInstruments = sr.isCountInstrumentResults();
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
        this.delegate.setScorer(scorer);
    }

    @Override
    public void setNextReader(IndexReader indexReader, int i) throws IOException {
        this.types = FieldCache.DEFAULT.getBytes(indexReader, IndexConstants.FIELDNAME_TYPE,
                INSTRUMENT_TYPE_PARSER);
        this.ids = FieldCache.DEFAULT.getInts(indexReader, IndexConstants.FIELDNAME_NUM_INDEXED);

        this.delegate.setNextReader(indexReader, i);
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return this.delegate.acceptsDocsOutOfOrder();
    }

    @Override
    public void collect(int i) throws IOException {
        final boolean alreadyCollected = this.instrumentsCollected.get(this.ids[i]);
        if (alreadyCollected) {
            if (this.collectInstruments) {
                return;
            }
        }
        else {
            this.instrumentsCollected.set(this.ids[i]);
        }

        this.typeCounts[this.types[i]]++;
        if (this.filters[this.types[i]]) {
            this.delegate.collect(i);
            if (!alreadyCollected) {
                this.instrumentCount++;
            }
        }
    }

    private static boolean[] initCountMask(SearchRequest sr) {
        final boolean result[] = new boolean[InstrumentTypeEnum.values().length];
        if (sr.getCountTypes() != null) {
            for (InstrumentTypeEnum type : sr.getCountTypes()) {
                result[type.ordinal()] = true;
            }
        }
        return result;
    }

    private static boolean[] initFilterTypes(SearchRequest sr) {
        if (sr.getFilterTypes() == null) {
            return FILTER_ALL;
        }

        final boolean result[] = new boolean[InstrumentTypeEnum.values().length];
        // a jit-compiler bug in jdk1.7.0_05/6 caused the previous version of this method
        // to create a result array that contained true values at random positions - only for
        // this method and only after some time/re-compilations. So add this extra check:
        if (isAnyTrueIn(result)) {
            LOGGER.warn("<initFilterTypes> init error: " + Arrays.toString(result));
            Arrays.fill(result, false);
        }

        for (InstrumentTypeEnum typeEnum : sr.getFilterTypes()) {
            result[typeEnum.ordinal()] = true;
        }
        return result;
    }

    private static boolean isAnyTrueIn(boolean[] values) {
        for (boolean b : values) {
            if (b) {
                return true;
            }
        }
        return false;
    }

    public int[] getTypeCounts() {
        return this.typeCounts;
    }

    public int getTotalCount() {
        int n = 0;
        for (int typeCount : this.typeCounts) {
            n += typeCount;
        }
        return n;
    }

    public int getValidCount() {
        int n = 0;
        for (int i = 0; i < this.typeCounts.length; i++) {
            if (this.filters[i]) {
                n += typeCounts[i];
            }
        }
        return n;
    }

    public int getRemainingCount() {
        int n = 0;
        for (int i = 0; i < this.typeCounts.length; i++) {
            if (!this.countMask[i]) {
                n += typeCounts[i];
            }
        }
        return n;
    }

    /**
     * Total number of (filtered) Instruments matched by the search
     * @return instrument count
     */
    public int getInstrumentCount() {
        return this.instrumentCount;
    }

    public Map<InstrumentTypeEnum, Integer> getCountByType() {
        final Map<InstrumentTypeEnum, Integer> result =
                new EnumMap<>(InstrumentTypeEnum.class);
        for (int i = 0; i < this.typeCounts.length; i++) {
            if (this.countMask[i]) {
                result.put(InstrumentTypeEnum.values()[i], this.typeCounts[i]);
            }
        }
        return result;
    }
}
