/*
 * ReportConstraint.java
 *
 * Created on 23.05.12 16:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.report;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.data.DownloadableItem;

/**
 * @author zzhao
 */
public class ReportConstraint {

    private final String language;

    private final String country;

    private final String filterStrategy;

    private final boolean oneReportEachType;

    private ReportSelectionStrategy selectionStrategy;

    private final DateTime date;

    private boolean newlyFiltered = false;

    private final String type;

    public ReportConstraint(String language, String country, String filterStrategy,
            boolean oneReportEachType) {
        this(language, country, filterStrategy, oneReportEachType, null, null);
    }

    public ReportConstraint(String language, String country, String filterStrategy,
            boolean oneReportEachType, DateTime date, String type) {
        this.language = language;
        this.country = country;
        this.filterStrategy = filterStrategy;
        this.oneReportEachType = oneReportEachType;
        this.date = date;
        this.type = type;
    }

    public boolean isOneReportEachType() {
        return oneReportEachType;
    }

    public ReportSelectionStrategy getSelectionStrategy() {
        return selectionStrategy;
    }

    public void setSelectionStrategy(ReportSelectionStrategy selectionStrategy) {
        this.selectionStrategy = selectionStrategy;
    }

    public boolean isNewlyFiltered() {
        return newlyFiltered;
    }

    public void setNewlyFiltered(boolean newlyFiltered) {
        this.newlyFiltered = newlyFiltered;
    }

    public boolean passLanguageCheck(DownloadableItem item) {
        return !isDefinedAndNotLess(this.language, item.getLanguage());
    }

    public boolean passCountryCheck(DownloadableItem item) {
        return !isDefinedAndNotLess(this.country, item.getCountry());
    }

    public boolean passDateCheck(DownloadableItem item) {
        return isDefinedOrEarlier(this.date, item.getDate());
    }

    public boolean passTypeCheck(DownloadableItem item) {
        return this.type == null || this.type.equals(item.getType().name());
    }

    public boolean passFilterStrategyCheck(DownloadableItem item) {
        return isWithinFilterConstraints(item);
    }

    public boolean passAllChecks(DownloadableItem item) {
        return passLanguageCheck(item) && passCountryCheck(item) && passFilterStrategyCheck(item) && passDateCheck(item) && passTypeCheck(item);
    }

    private boolean isWithinFilterConstraints(DownloadableItem item) {
        return null == this.filterStrategy
                || ("DZBANK-PIB".equals(this.filterStrategy) && "Produktinfo".equals(item.getDescription()))
                || ("UNION-PIF".equals(this.filterStrategy) && "Union PIF".equals(item.getDescription()));
    }

    private boolean isDefinedAndNotLess(String expected, String actual) {
        return (expected != null) && !expected.equalsIgnoreCase(actual);
    }

    private boolean isDefinedOrEarlier(DateTime expected, DateTime actual) {
        return (expected == null) || expected.compareTo(actual) < 1;
    }
}
