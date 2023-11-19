/*
 * PagedResultVisitor.java
 *
 * Created on 03.08.2006 18:35:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.marketmaker.istar.common.util.PagedResultSorter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PagedResultVisitor extends AbstractCountingResultVisitor<PagedResultVisitor> {
    PagedResultSorter<RatioData> prs;

    MetaDataCounter metaDataCounter;

    public void init(final SearchParameterParser spp) {
        super.init(spp);

        final Comparator<QuoteRatios> quoteRatiosComparator = spp.getComparator();
        this.prs =
                new PagedResultSorter<>(spp.getStartAt(),
                        spp.getNumResults(),
                        (o1, o2) -> quoteRatiosComparator.compare(o1.getSearchResult(), o2.getSearchResult()));
        this.metaDataCounter = MetaDataCounter.create(spp);
    }

    @Override
    public PagedResultVisitor merge(PagedResultVisitor v) {
        mergeCounts(v);
        this.prs.merge(v.prs);
        if (this.metaDataCounter != null) {
            this.metaDataCounter.merge(v.metaDataCounter);
        }
        return this;
    }

    @Override
    public void visit(RatioData data) {
        if (!count(data.getSearchResult())) {
            return;
        }

        if (this.prs.getLength() > 0) {
            // optimization for live-Finder: if query is just for counting metadata
            // set count (=prs.length) to 0 to improve performance
            this.prs.add(data);
        }

        if (this.metaDataCounter != null) {
            this.metaDataCounter.visit(data);
        }
    }

    public RatioSearchResponse getResponse() {
        final DefaultRatioSearchResponse result = new DefaultRatioSearchResponse();
        result.setNumTotal(this.prs.getTotalCount());
        result.setOffset(this.prs.getStart());
        result.setLength(this.prs.getLength());
        result.setElements(getResults());
        result.setResultGroupCount(getCounts());
        result.setMetadata(createMetadataResult());
        return result;
    }

    private Map<Integer, Map<String, Integer>> createMetadataResult() {
        return (this.metaDataCounter != null) ? this.metaDataCounter.getResult() : null;
    }

    private List<RatioDataResult> getResults() {
        return this.prs.getResult()
                .stream()
                .map(RatioData::createResult)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
