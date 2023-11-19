/*
 * StockAnalysisRequest.java
 *
 * Created on 29.01.2007 14:53:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.stockanalysis;

import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StockAnalysisRequest extends AbstractIstarRequest {
    protected static final long serialVersionUID = 1L;

    private List<Rating> ratings;
    private String source;
    private List<Long> instrumentids;
    private String sector;
    private String region;
    private String searchtext;
    private LocalDate start;
    private LocalDate end;
    private int offset;
    private int anzahl;
    private String sortBy;
    private boolean ascending;
    private boolean aggregatedResultType=false;

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public int getAnzahl() {
        return anzahl;
    }

    public void setAnzahl(int anzahl) {
        this.anzahl = anzahl;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public List<Long> getInstrumentids() {
        return instrumentids;
    }

    public void setInstrumentids(List<Long> instrumentids) {
        this.instrumentids = instrumentids;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public List<Rating> getRatings() {
        return ratings != null ? ratings : Collections.<Rating>emptyList();
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSearchtext() {
        return searchtext;
    }

    public void setSearchtext(String searchtext) {
        this.searchtext = searchtext;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setAggregatedResultType() {
        this.aggregatedResultType=true;
    }

    public boolean isAggregatedResultType() {
        return aggregatedResultType;
    }
}
