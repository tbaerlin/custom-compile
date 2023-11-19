/*
 * InstrumentAnalyses.java
 *
 * Created on 21.03.12 09:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.mutable.MutableInt;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.data.StockAnalysis;
import de.marketmaker.istar.domain.data.StockAnalysisAims;
import de.marketmaker.istar.domain.data.StockAnalysisSummary;
import de.marketmaker.istar.domainimpl.data.StockAnalysisAimsImpl;
import de.marketmaker.istar.domainimpl.data.StockAnalysisSummaryImpl;

/**
 * @author oflege
 */
class InstrumentAnalyses {
    /**
     * most recent analysis for this instrument from all issuers
     */
    private final List<AnalysisSummary> summaries = new ArrayList<>(4);

    InstrumentAnalyses(AnalysisSummary summary) {
        this.summaries.add(summary);
    }

    boolean isEmpty() {
        return this.summaries.isEmpty();
    }

    Set<String> getSources() {
        final Set<String> result = new TreeSet<>();
        for (AnalysisSummary summary : summaries) {
            result.add(summary.getSource());
        }
        return result;
    }

    private int findIndexForSource(String source) {
        for (int i = 0; i < this.summaries.size(); i++) {
            final AnalysisSummary as = this.summaries.get(i);
            if (as.getSource().equals(source)) {
                return i;
            }
        }
        return -1;
    }

    void add(AnalysisSummary summary) {
        final int i = findIndexForSource(summary.getSource());
        if (i < 0) {
            this.summaries.add(summary);
        }
        else {
            final AnalysisSummary as = this.summaries.get(i);
            this.summaries.set(i, as.add(summary));
        }
    }

    AnalysisSummary getForSource(String source) {
        final int i = findIndexForSource(source);
        return (i >= 0) ? this.summaries.get(i) : null;
    }

    AnalysisSummary getLatest() {
        final long endOfToday = getEndOfTodayMillis();
        AnalysisSummary result = null;
        for (AnalysisSummary summary : summaries) {
            if (summary.getEndDate() < endOfToday) {
                continue;
            }
            if (result == null || result.getDate() < summary.getDate()) {
                result = summary;
            }
        }
        return result;
    }

    public StockAnalysisAims getAims(Set<Long> ids) {
        long endOfToday = getEndOfTodayMillis();
        double minAim = Double.POSITIVE_INFINITY;
        double maxAim = Double.NEGATIVE_INFINITY;
        String currency = null;
        for (AnalysisSummary summary : this.summaries) {
            final AnalysisSummary selected = getFirst(summary, ids);
            if (selected == null || selected.getEndDate() < endOfToday) {
                continue;
            }
            if (currency == null) {
                currency = selected.getCurrency();
            }
            else if (!currency.equals(selected.getCurrency())) {
                // TODO: what if multiple currencies are involved?!
                continue;
            }
            if (!Double.isNaN(selected.getTarget())) {
                maxAim = Math.max(maxAim, selected.getTarget());
                minAim = Math.min(minAim, selected.getMinTarget());
            }
        }
        if (Double.isInfinite(minAim) && Double.isInfinite(maxAim)) {
            return null;
        }
        return new StockAnalysisAimsImpl(currency,
                Double.isInfinite(minAim) ? BigDecimal.ZERO : BigDecimal.valueOf(minAim),
                Double.isInfinite(maxAim) ? BigDecimal.ZERO : BigDecimal.valueOf(maxAim)
        );
    }

    private AnalysisSummary getFirst(AnalysisSummary summary, Set<Long> ids) {
        AnalysisSummary selected = summary;
        while (selected != null && !ids.contains(selected.getId())) {
            selected = selected.getPrevious();
        }
        return selected;
    }

    public StockAnalysisSummary getSummary(Set<Long> ids) {
        final Map<StockAnalysis.Recommendation, MutableInt> counts
                = new EnumMap<>(StockAnalysis.Recommendation.class);
        for (StockAnalysis.Recommendation r : StockAnalysis.Recommendation.values()) {
            counts.put(r, new MutableInt());
        }

        final long endOfToday = getEndOfTodayMillis();
        long mostRecent = Long.MIN_VALUE;
        for (AnalysisSummary summary : this.summaries) {
            final AnalysisSummary selected = getFirst(summary, ids);
            if (selected == null || selected.getEndDate() < endOfToday) {
                continue;
            }
            counts.get(selected.getRecommendation()).add(1);
            mostRecent = Math.max(mostRecent, selected.getDate());
        }
        if (mostRecent == Long.MIN_VALUE) {
            return null;
        }

        return new StockAnalysisSummaryImpl(new DateTime(mostRecent),
                counts.get(StockAnalysis.Recommendation.BUY).intValue(),
                counts.get(StockAnalysis.Recommendation.STRONG_BUY).intValue(),
                counts.get(StockAnalysis.Recommendation.HOLD).intValue(),
                counts.get(StockAnalysis.Recommendation.SELL).intValue(),
                counts.get(StockAnalysis.Recommendation.STRONG_SELL).intValue()
        );
    }

    private long getEndOfTodayMillis() {
        return new LocalDate().plusDays(1).toDateTimeAtStartOfDay().getMillis();
    }

    /**
     * Removes those analysis-ids from the given set for which the id of a more recent analysis
     * can also be found in the set.
     * @param ids analysis-ids
     */
    void retainMostRecent(Set<Long> ids) {
        for (AnalysisSummary summary : this.summaries) {
            AnalysisSummary current = summary;
            boolean found = false;
            do {
                if (found) {
                    ids.remove(current.getId());
                }
                found |= ids.contains(current.getId());
                current = current.getPrevious();
            } while (current != null);
        }
    }

    int size() {
        int n = 0;
        for (AnalysisSummary s : summaries) {
            AnalysisSummary c = s;
            while (c != null) {
                n++;
                c = c.getPrevious();
            }
        }
        return n;
    }

    public void addDocumentIds(HashSet<Long> result) {
        for (AnalysisSummary summary : this.summaries) {
            AnalysisSummary current = summary;
            while (current != null) {
                result.add(current.getId());
                current = current.getPrevious();
            }
        }
    }

    public void remove(AnalysisSummary summary) {
        final int i = findIndexForSource(summary.getSource());
        if (i < 0) {
            return;
        }
        final AnalysisSummary as = this.summaries.get(i);
        final AnalysisSummary withSummaryRemoved = as.remove(summary);
        if (withSummaryRemoved == null) {
            this.summaries.remove(i);
        }
        else {
            this.summaries.set(i, withSummaryRemoved);
        }
    }

    public String getSector() {
        final AnalysisSummary latest = getLatest();
        return (latest != null) ? latest.getBranch() : null;
    }
}
