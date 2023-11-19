/*
 * FndSectorMMA.java
 *
 * Created on 13.12.2007 16:23:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.PagedResultVisitor;
import de.marketmaker.istar.ratios.frontend.PreferIssuerFundQuoteStrategy;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;

/**
 * Method object that performs an arbitrary search on elements within a fund's sector.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndSectorSearch {
    protected final RatiosProvider ratiosProvider;

    protected final RatioDataRecord fundRatios;

    protected final Map<String, Object> result = new HashMap<>();

    private String sortField = "bviperformance1y";

    private boolean sortDescending = false;

    private int numResults = 1;

    public static final String KEY_ELEMENTS = "elements";

    public FndSectorSearch(RatiosProvider ratiosProvider, RatioDataRecord fundRatios) {
        this.fundRatios = fundRatios;
        this.ratiosProvider = ratiosProvider;
    }

    public Map<String, Object> compute() {
        if (!StringUtils.hasText(this.fundRatios.getSectorFww())) {
            return this.result;
        }

        final RatioSearchRequest request = new RatioSearchRequest(RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales());
        request.setType(InstrumentTypeEnum.FND);
        request.setDataRecordStrategyClass(PreferIssuerFundQuoteStrategy.class);
        request.setVisitorClass(PagedResultVisitor.class);

        final Map<String, String> params = new HashMap<>();
        request.setParameters(params);
        params.put("fwwsector", fundRatios.getSectorFww());
        params.put("i", "0");
        params.put("n", Integer.toString(this.numResults));
        params.put("sort1", this.sortField);
        params.put("sort1:D", Boolean.valueOf(this.sortDescending).toString());

        final DefaultRatioSearchResponse response
                = (DefaultRatioSearchResponse) this.ratiosProvider.search(request);
        if (!response.isValid()) {
            return this.result;
        }

        this.result.put(KEY_ELEMENTS, response.getElements());

        return this.result;
    }

    public FndSectorSearch withSortField(String s) {
        this.sortField = s;
        return this;
    }

    public FndSectorSearch withSortDescending(boolean b) {
        this.sortDescending = b;
        return this;
    }

    public FndSectorSearch withNumResults(int n) {
        this.numResults = n;
        return this;
    }
}