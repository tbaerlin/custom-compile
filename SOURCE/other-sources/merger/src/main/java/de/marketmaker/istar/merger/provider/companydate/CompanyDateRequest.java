/*
 * CompanyDateRequest.java
 *
 * Created on 19.07.2008 15:18:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.companydate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.common.util.ClassUtil;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CompanyDateRequest extends AbstractIstarRequest {
    public static final List<String> SORTFIELDS
            = Collections.unmodifiableList(Arrays.asList("date", "isin", "relevance", "name", "event", "wkn"));

    static final long serialVersionUID = 1L;

    private boolean wmAllowed;

    private boolean convensysAllowed;

    private Set<Long> iids;

    private Set<String> events;

    private Set<String> nonEvents;

    private LocalDate from;

    private LocalDate to;

    private int offset;

    private int count;

    private String sortBy;

    private boolean ascending = true;

    @Deprecated
    private List<Locale> locales;

    private Language language = Language.de;

    private String quoteNameStrategyName;

    private transient QuoteNameStrategy quoteNameStrategy;

    private Profile profile;

    private QuoteFilter baseQuoteFilter;

    public CompanyDateRequest() {
    }

    public boolean isWmAllowed() {
        return wmAllowed;
    }

    public void setWmAllowed(boolean wmAllowed) {
        this.wmAllowed = wmAllowed;
    }

    public boolean isConvensysAllowed() {
        return convensysAllowed;
    }

    public void setConvensysAllowed(boolean convensysAllowed) {
        this.convensysAllowed = convensysAllowed;
    }

    public Set<Long> getIids() {
        return iids;
    }

    public void setIids(Set<Long> iids) {
        this.iids = iids;
    }

    public Set<String> getEvents() {
        return events;
    }

    public void setEvents(Set<String> events) {
        this.events = events;
    }

    public Set<String> getNonEvents() {
        return nonEvents;
    }

    public void setNonEvents(Set<String> nonEvents) {
        this.nonEvents = nonEvents;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ListResult.Sort> getSorts() {
        return ListResult.parseSortBy(this.sortBy, this.ascending, SORTFIELDS);
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    @Deprecated
    public void setLocales(List<Locale> locales) {
        this.locales = locales;
    }

    @Deprecated
    public List<Locale> getLocales() {
        return locales;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    protected java.lang.Object readResolve() {
        if (this.language == null) {
            if (this.locales != null && !this.locales.isEmpty()) {
                this.language = Language.valueOf(this.locales.get(0));
            }
            else {
                this.language = Language.de;
            }
        }
        return this;
    }


    public String toString() {
        return "CompanyDateRequest[iids=" + iids
            + ", events=" + events
            + ", language=" + this.language
            + ", from=" + from
            + ", to=" + to
            + "]";
    }

    public String getQuoteNameStrategyName() {
        return quoteNameStrategyName;
    }

    public QuoteNameStrategy getQuoteNameStrategy() {
        if (StringUtils.isBlank(this.quoteNameStrategyName)) {
            return null;
        }
        if (this.quoteNameStrategy == null) {
            this.quoteNameStrategy = ClassUtil.getObject(this.quoteNameStrategyName);
        }
        return this.quoteNameStrategy;
    }

    public void setQuoteNameStrategyName(String quoteNameStrategyName) {
        this.quoteNameStrategyName = quoteNameStrategyName;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public void setBaseQuoteFilter(QuoteFilter baseQuoteFilter) {
        this.baseQuoteFilter = baseQuoteFilter;
    }

    public QuoteFilter getBaseQuoteFilter() {
        return baseQuoteFilter;
    }
}
