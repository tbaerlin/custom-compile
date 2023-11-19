/*
 * PartitionResultVisitor.java
 *
 * Created on 2/6/15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.PartitionUtil;
import de.marketmaker.istar.ratios.RatioFieldDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Stefan Willenbrock
 */
public class PartitionResultVisitor implements MergeableSearchEngineVisitor<PartitionResultVisitor> {

    private final MergeableSearchEngineVisitor delegate = new PagedResultVisitor();

    private String sortField;

    private List<String> sortFieldValues = new ArrayList<>();

    public void init(final SearchParameterParser spp) {
        this.delegate.init(spp);
        this.sortField = spp.getParameterValue("sort1");
    }

    @Override
    public PartitionResultVisitor merge(PartitionResultVisitor v) {
        this.delegate.merge(v.delegate);
        return this;
    }

    @Override
    public void visit(RatioData data) {
        this.delegate.visit(data);

        final RatioFieldDescription.Field field = data.getSearchResult().getFieldByName(sortField);
        if (field == null) {
            return;
        }

        String v = data.getSearchResult().getString(field.id());
        if (v != null) {
            v = v.toUpperCase(Locale.ROOT);
        }
        this.sortFieldValues.add(v);
    }

    public RatioSearchResponse getResponse() {
        final DefaultRatioSearchResponse response = (DefaultRatioSearchResponse) this.delegate.getResponse();
        response.setPartition(PartitionUtil.partition(this.sortFieldValues));
        return response;
    }
}
