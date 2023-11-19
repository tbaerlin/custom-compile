package de.marketmaker.istar.analyses.analyzer.stream;

import java.util.function.Predicate;

import de.marketmaker.istar.analyses.analyzer.Analysis;

public class TimeFilter implements Predicate<Analysis> {

    private final int yyyyMmDd;

    public TimeFilter(int yyyyMmDd) {
        this.yyyyMmDd = yyyyMmDd;
    }

    @Override
    public boolean test(Analysis analysis) {
        return analysis.getStartDate() <= yyyyMmDd && analysis.getEndDate() >= yyyyMmDd;
    }

}
   