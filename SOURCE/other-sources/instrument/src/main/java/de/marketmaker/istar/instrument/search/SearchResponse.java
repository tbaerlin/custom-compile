/*
 * SearchResponse.java
 *
 * Created on 22.12.2004 14:05:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.CacheableInstrumentResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SearchResponse extends AbstractIstarResponse implements CacheableInstrumentResponse {
    static final long serialVersionUID = 123545L;

    public static SearchResponse getInvalid() {
        final SearchResponse result = new SearchResponse();
        result.setInvalid();
        return result;
    }

    private long instrumentUpdateTimestamp;

    private List<Instrument> instruments = Collections.emptyList();
    private List<Quote> quotes = Collections.emptyList();

    private Set<Long> instrumentids = Collections.emptySet();
    private Set<Long> quoteids = Collections.emptySet();

    private int truncatedResultSize;
    private String querystring;
    private int numTotalHits;
    private Map<InstrumentTypeEnum,Integer> typeCounts = Collections.emptyMap();
    private int remainingTypesCount;
    private int validObjectCount;
    private int instrumentCount;

    private Map<String, List<String>> termSubstitutions = Collections.emptyMap();
    private Map<Long, Instrument> underlyings=Collections.emptyMap();

    public SearchResponse() {
    }

    public SearchResponse(long instrumentUpdateTimestamp) {
        this.instrumentUpdateTimestamp = instrumentUpdateTimestamp;
    }

    @Override
    public long getInstrumentUpdateTimestamp() {
        return this.instrumentUpdateTimestamp;
    }

    @Override
    public List<Instrument> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<Instrument> instruments) {
        this.instruments = instruments;
    }

    public void setInstrumentidHits(Set<Long> instrumentids) {
        this.instrumentids = instrumentids;
    }

    public void setQuoteidHits(Set<Long> quoteids) {
        this.quoteids = quoteids;
    }

    public Set<Long> getInstrumentids() {
        return instrumentids;
    }

    public Set<Long> getQuoteids() {
        return quoteids;
    }

    public void setTruncatedResultSize(int size) {
        this.truncatedResultSize = size;
    }

    public int getTruncatedResultSize() {
        return truncatedResultSize;
    }

    public boolean isTruncatedResultSize() {
        return this.truncatedResultSize != Integer.MIN_VALUE;
    }

    public void setQuerystring(String querystring) {
        this.querystring = querystring;
    }

    public String getQuerystring() {
        return querystring;
    }

    public void setNumTotalHits(int numTotalHits) {
        this.numTotalHits = numTotalHits;
    }

    public int getNumTotalHits() {
        return numTotalHits;
    }

    public void setTermSubstitutions(Map<String, List<String>> termSubstitutions) {
        this.termSubstitutions = termSubstitutions;
    }

    public Map<String, List<String>> getTermSubstitutions() {
        return termSubstitutions;
    }

    public void setUnderlyings(Map<Long, Instrument> underlyings) {
        this.underlyings=underlyings;
    }

    @Override
    public Map<Long, Instrument> getUnderlyings() {
        return underlyings;
    }

    public List<Quote> getQuotes() {
        return quotes;
    }

    public void setQuotes(List<Quote> quotes) {
        this.quotes = quotes;
    }

    public Map<InstrumentTypeEnum, Integer> getTypeCounts() {
        return typeCounts;
    }

    public void setTypeCounts(Map<InstrumentTypeEnum, Integer> typeCounts) {
        this.typeCounts = typeCounts;
    }

    public int getRemainingTypesCount() {
        return remainingTypesCount;
    }

    public void setRemainingTypesCount(int remainingTypesCount) {
        this.remainingTypesCount = remainingTypesCount;
    }

    public int getTotalTypesCount() {
        int result = this.remainingTypesCount;
        for (Integer anInt : this.typeCounts.values()) {
            result += anInt;
        }
        return result;
    }

    public void setValidObjectCount(int validObjectCount) {
        this.validObjectCount=validObjectCount;
    }

    public int getValidObjectCount() {
        return validObjectCount;
    }

    /**
     * Total number of (filtered) Instruments matched by the search
     * @return #instruments
     */
    public int getInstrumentCount() {
        return this.instrumentCount;
    }

    public void setInstrumentCount(int instrumentCount) {
        this.instrumentCount = instrumentCount;
    }
}