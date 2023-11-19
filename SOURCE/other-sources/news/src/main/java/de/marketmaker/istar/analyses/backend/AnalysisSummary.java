/*
 * AnalysisSummary.java
 *
 * Created on 21.03.12 09:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.Period;

import de.marketmaker.istar.common.util.PeriodEditor;
import de.marketmaker.istar.domain.data.StockAnalysis;
import de.marketmaker.istar.domain.data.StockAnalysis.Recommendation;

/**
 *  the relevant analysis information (region, branch, rating ...)
 *
 * @author oflege
 */
class AnalysisSummary {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);

    static {
        GERMAN_COLLATOR.setStrength(Collator.PRIMARY); // ignore case
    }


    static final Comparator<AnalysisSummary> BY_DATE = (o1, o2) -> Long.compare(o1.getDate(), o2.getDate());

    static final Comparator<AnalysisSummary> BY_BRANCH = (o1, o2) -> compare(o1.getBranch(), o2.getBranch());

    static final Comparator<AnalysisSummary> BY_REGION = (o1, o2) -> compare(o1.getRegion(), o2.getRegion());

    static final Comparator<AnalysisSummary> BY_SOURCE = (o1, o2) -> compare(o1.getSource(), o2.getSource());

    static final Comparator<AnalysisSummary> BY_RATING = (o1, o2) -> {
        Recommendation r1 = o1.getRecommendation();
        Recommendation r2 = o2.getRecommendation();
        if (r1 == Recommendation.NONE) {
            return (r2 == Recommendation.NONE) ? 0 : 1;
        }
        if (r2 == Recommendation.NONE) {
            return -1;
        }
        // buy < sell in enum, but in asc order buy should be > sell, so switch args
        return r2.compareTo(r1);
    };

    private static int compare(String s1, String s2) {
        if (s1 == null) {
            return (s2 == null) ? 0 : 1;
        }
        if (s2 == null) {
            return -1;
        }
        return GERMAN_COLLATOR.compare(s1, s2);
    }

    private final long id;

    private final long instrumentid;

    private final long date;

    private final long endDate;

    private final double target;

    private final double minTarget;

    private final String source;

    private final Recommendation recommendation;

    private final String currency;

    private final String region;

    private final String branch;

    // the summary that is next when ordered by date
    private AnalysisSummary previous;

    public AnalysisSummary(Protos.Analysis record, String mappedSourceName) {
        this.id = record.getId();
        this.instrumentid = record.getIidCount() > 0 ? record.getIid(0) : 0L;
        this.date = record.getAgencyDate();
        this.endDate = getEndDate(record);
        this.target = record.hasTarget() ? Double.parseDouble(record.getTarget()) : Double.NaN;
        this.minTarget = record.hasMinTarget() ? Double.parseDouble(record.getMinTarget()) : this.target;
        this.source = mappedSourceName;
        this.recommendation = record.hasRating()
                ? StockAnalysis.Recommendation.values()[record.getRating().getNumber()]
                : StockAnalysis.Recommendation.NONE;
        this.currency = record.hasCurrency() ? record.getCurrency().intern() : null;
        this.region = toSortableString(record.getCountryList());
        this.branch = toSortableString(record.getBranchList());
    }

    private long getEndDate(Protos.Analysis record) {
        long result = Long.MAX_VALUE;
        if (record.hasTimeframe()) {
            try {
                Period p = PeriodEditor.fromText("P" + record.getTimeframe().toUpperCase());
                result = new DateTime(this.date).plus(p).getMillis();
            } catch (IllegalArgumentException ex) {
                logger.warn("<getEndDate> error parsing timeframe, value was '" + record.getTimeframe()
                        + "', setting endDate to max, the recordId was " + record.getId()
                        + " Long.MAX_VALUE will be used as end date");
            }
        }
        return result;
    }

    private String toSortableString(List<String> items) {
        if (items.isEmpty() || items.size() > 1) {
            return null;
        }
        return items.get(0);
    }

    /**
     * Add other to the chain of summaries starting at this and following the {@link #previous} field,
     * so that after adding other the chain is still ordered by {@link #date} from most recent to
     * oldest.
     * @param other to be added
     * @return this iff this.date is more recent than other.date, otherwise other
     */
    AnalysisSummary add(AnalysisSummary other) {
        if (isMoreRecentThan(other)) {
            this.previous = (this.previous == null) ? other : this.previous.add(other);
            return this;
        }
        else  { // this.date is smaller than other.date, so other is more recent
            other.previous = this;
            return other;
        }
    }

    private boolean isMoreRecentThan(AnalysisSummary other) {
        // ids will never be the same, so comparing ids ensures total order
        return this.date > other.date || (this.date == other.date && this.id > other.id);
    }

    AnalysisSummary remove(AnalysisSummary summary) {
        if (this == summary) {
            return this.previous;
        }
        if (this.previous != null) {
            this.previous = this.previous.remove(summary);
        }
        return this;
    }

    public long getId() {
        return id;
    }

    public long getInstrumentid() {
        return instrumentid;
    }

    public long getDate() {
        return date;
    }

    public long getEndDate() {
        return endDate;
    }

    public double getTarget() {
        return target;
    }

    public double getMinTarget() {
        return minTarget;
    }

    public String getSource() {
        return source;
    }

    public AnalysisSummary getPrevious() {
        return previous;
    }

    public void setPrevious(AnalysisSummary previous) {
        this.previous = previous;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }

    public String getCurrency() {
        return currency;
    }

    public String getRegion() {
        return this.region;
    }

    public String getBranch() {
        return branch;
    }

    public boolean hasRecommendation() {
        return this.recommendation != null && this.recommendation != Recommendation.NONE;
    }
}
