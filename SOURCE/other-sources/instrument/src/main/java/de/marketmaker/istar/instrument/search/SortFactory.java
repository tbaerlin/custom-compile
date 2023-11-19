/*
 * SearchSorters.java
 *
 * Created on 20.08.2009 11:12:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import de.marketmaker.istar.instrument.export.ByVolumeQuoteSorter;
import de.marketmaker.istar.instrument.export.DefaultInstrumentSorter;
import de.marketmaker.istar.instrument.export.DutchQuoteSorter;
import de.marketmaker.istar.instrument.export.FrenchQuoteSorter;
import de.marketmaker.istar.instrument.export.GermanByVolumeQuoteSorter;
import de.marketmaker.istar.instrument.export.ItalianQuoteSorter;
import de.marketmaker.istar.instrument.export.MarketManagerQuoteSorter;
import de.marketmaker.istar.instrument.export.NumIndexedSorter;
import de.marketmaker.istar.instrument.export.SwissQuoteSorter;
import de.marketmaker.istar.instrument.export.BelgianQuoteSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SortFactory {
    public static final SortFactory INSTANCE = new SortFactory();

    private final static String[] DEFAULT_SORTFIELDS = new String[]{
            DefaultInstrumentSorter.SORT_FIELD.getField(),
            NumIndexedSorter.SORT_FIELD.getField(),
            MarketManagerQuoteSorter.SORT_FIELD.getField()
    };

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, SortField> instrumentSorters
            = new HashMap<>();

    private final Map<String, SortField> quoteSorters
            = new HashMap<>();

    private SortFactory() {
        addInstrumentSorter(DefaultInstrumentSorter.SORT_FIELD);
        addInstrumentSorter(NumIndexedSorter.SORT_FIELD);

        addQuoteSorter(MarketManagerQuoteSorter.SORT_FIELD);
        addQuoteSorter(ByVolumeQuoteSorter.SORT_FIELD);
        addQuoteSorter(GermanByVolumeQuoteSorter.SORT_FIELD);
        addQuoteSorter(SwissQuoteSorter.SORT_FIELD);
        addQuoteSorter(ItalianQuoteSorter.SORT_FIELD);
        addQuoteSorter(DutchQuoteSorter.SORT_FIELD);
        addQuoteSorter(BelgianQuoteSorter.SORT_FIELD);
        addQuoteSorter(FrenchQuoteSorter.SORT_FIELD);
    }

    public Sort getSort(SearchRequest sr) {
        return getSort(sr, getSortFields(sr));
    }

    private Sort getSort(SearchRequest sr, String[] fields) {
        final SortField[] sortFields = new SortField[fields.length];
        int n = 0;
        for (String field : fields) {
            final SortField sf = sr.isCountInstrumentResults()
                    ? getInstrumentSortField(field)
                    : getSortField(field);
            if (sf != null) {
                sortFields[n++] = sf;
            }
        }

        if (n == 0) {
            this.logger.warn("<getSort> no valid sort fields in " + sr + ", using default");
            return getSort(sr, DEFAULT_SORTFIELDS);
        }

        if (!sr.isCountInstrumentResults()
            && !this.quoteSorters.containsKey(sortFields[n - 1].getField())) {
            this.logger.warn("<getSort> search for quotes but no QuoteSorter may result in " +
                    "arbitrary order for " + sr);
        }

        return new Sort(Arrays.copyOf(sortFields, n));
    }

    private void addInstrumentSorter(final SortField sf) {
        this.instrumentSorters.put(sf.getField(), sf);
    }

    private void addQuoteSorter(final SortField sf) {
        this.quoteSorters.put(sf.getField(), sf);
    }

    private SortField getInstrumentSortField(String name) {
        return this.instrumentSorters.get(name);
    }

    private SortField getSortField(String name) {
        final SortField result = this.instrumentSorters.get(name);
        return (result != null) ? result : this.quoteSorters.get(name);
    }
    
    private String[] getSortFields(SearchRequest sr) {
        final String[] fields = sr.getSortFields();                        
        return (fields != null) ? fields : DEFAULT_SORTFIELDS;
    }
}
