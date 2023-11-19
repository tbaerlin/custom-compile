/*
 * AbstractCountingResultVisitor.java
 *
 * Created on 29.09.2006 10:17:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableInt;

import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractCountingResultVisitor<T extends AbstractCountingResultVisitor>
        implements MergeableSearchEngineVisitor<T> {
    private RatioFieldDescription.Field fieldForResultCount;

    private Map<String, MutableInt> counts;

    private String filterForResultCount;

    // -1 means non-locale field

    private int localeIndex = -1;

    private boolean instrumentField;

    public void init(SearchParameterParser spp) {
        if (spp.getFieldidForResultCount() > 0) {
            this.fieldForResultCount = RatioFieldDescription.getFieldById(spp.getFieldidForResultCount());

            if (this.fieldForResultCount.getLocales() != null) {
                this.localeIndex = RatioFieldDescription.getLocaleIndex(this.fieldForResultCount, spp.getLocales());
            }

            this.instrumentField = this.fieldForResultCount.isInstrumentField();
            this.counts = new HashMap<>();
            this.filterForResultCount = spp.getFilterForResultCount();
        }
    }

    protected boolean count(QuoteRatios rdr) {
        if (this.fieldForResultCount == null) {
            return true;
        }

        final Selectable s = this.instrumentField ? rdr.getInstrumentRatios() : rdr;
        final String value = s.getString(this.fieldForResultCount.id(), this.localeIndex);
        if (value == null) {
            return false;
        }

        this.counts.computeIfAbsent(value, (v) -> new MutableInt()).increment();

        return this.filterForResultCount == null || this.filterForResultCount.equals(value);
    }

    protected void mergeCounts(AbstractCountingResultVisitor<T> v) {
        if (this.counts == null) {
            return;
        }
        for (Map.Entry<String, MutableInt> e : v.counts.entrySet()) {
            MutableInt m = this.counts.get(e.getKey());
            if (m != null) {
                m.add(e.getValue().intValue());
            }
            else {
                this.counts.put(e.getKey(), e.getValue());
            }
        }
    }

    protected Map<Object, Integer> getCounts() {
        if (this.counts == null) {
            return null;
        }

        return this.counts.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toInteger()));
    }
}
