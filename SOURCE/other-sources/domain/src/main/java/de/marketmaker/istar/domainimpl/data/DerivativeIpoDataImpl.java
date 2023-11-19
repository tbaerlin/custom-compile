/*
 * DerivativeIpoDataImpl.java
 *
 * Created on 20.11.2008 09:58:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.DerivativeIpoData;
import de.marketmaker.istar.domain.data.DownloadableItem;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DerivativeIpoDataImpl implements DerivativeIpoData, Serializable {
    protected static final long serialVersionUID = 1L;

    private final String wkn;
    private final String name;
    private final LocalDate subscriptionStart;
    private final LocalDate subscriptionEnd;
    private final LocalDate valutaDate;
    private final LocalDate expirationDate;
    private final int sort;
    private final List<DownloadableItem> reports = new ArrayList<>();

    private final boolean dzPib;

    public DerivativeIpoDataImpl(String wkn, String name, LocalDate subscriptionStart, LocalDate subscriptionEnd, LocalDate valutaDate, LocalDate expirationDate, int sort, boolean dzPib) {
        this.wkn = wkn;
        this.name = name;
        this.subscriptionStart = subscriptionStart;
        this.subscriptionEnd = subscriptionEnd;
        this.valutaDate = valutaDate;
        this.expirationDate = expirationDate;
        this.sort = sort;
        this.dzPib=dzPib;
    }

    public String getWkn() {
        return wkn;
    }

    public String getName() {
        return name;
    }

    public LocalDate getSubscriptionStart() {
        return subscriptionStart;
    }

    public LocalDate getSubscriptionEnd() {
        return subscriptionEnd;
    }

    public LocalDate getValutaDate() {
        return valutaDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public int getSort() {
        return sort;
    }

    @Override
    public boolean isDzPib() {
        return this.dzPib;
    }

    public List<DownloadableItem> getReports() {
        return reports;
    }

    public DownloadableItem getPibReport() {
        for (final DownloadableItem report : this.reports) {
            if (isPibReport(report)) {
                return report;
            }
        }
        return null;
    }

    private boolean isPibReport(DownloadableItem report) {
        return "Produktinfo".equals(report.getDescription()) // used by dz
                || "Produktinformation".equals(report.getDescription()); // used by wgz
    }

    public void addReport(DownloadableItem report) {
        this.reports.add(report);
    }

    public void addReports(List<DownloadableItem> reports) {
        this.reports.addAll(reports);
    }

    @Override
    public String toString() {
        return "DerivativeIpoDataImpl{" +
                "wkn=" + wkn +
                ", name=" + name +
                ", subscriptionStart=" + subscriptionStart +
                ", subscriptionEnd=" + subscriptionEnd +
                ", valutaDate=" + valutaDate +
                ", expirationDate=" + expirationDate +
                ", sort=" + sort +
                ", reports=" + reports +
                '}';
    }
}
