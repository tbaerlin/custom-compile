/*
 * FndSectorMMA.java
 *
 * Created on 13.12.2007 16:23:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.util;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.web.easytrade.block.AbstractFindersuchergebnis;
import de.marketmaker.istar.ratios.frontend.MinMaxAvgRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.PreferIssuerFundQuoteStrategy;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.MinMaxAvgVisitor;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * Method object superclass, instances compute minMaxAvg for all funds in a given fund's sector
 * for a confgurable number of fields.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class FndSectorMMA {
    protected final RatiosProvider ratiosProvider;

    protected final RatioDataRecord fundRatios;

    protected Map<Integer, Map<String, Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg>>> mmaResult;

    protected final Map<String, Object> result = new HashMap<>();

    public FndSectorMMA(RatiosProvider ratiosProvider, RatioDataRecord fundRatios) {
        this.fundRatios = fundRatios;
        this.ratiosProvider = ratiosProvider;
    }

    protected MinMaxAvgRatioSearchResponse.MinMaxAvg getMma(RatioFieldDescription.Field field) {
        if (!this.mmaResult.containsKey(field.id())) {
            return null;
        }

        final Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg> map
                = this.mmaResult.get(field.id()).get(getInvestmentFocus());
        if (map == null) {
            return null;
        }
        return map.get("default");
    }

    private String getInvestmentFocus() {
        return this.fundRatios.getInvestmentFocus();
    }

    public Map<String, Object> compute() {
        if (!StringUtils.hasText(getInvestmentFocus())) {
            return this.result;
        }

        final RatioSearchRequest mmaRequest = createRequest();

        final MinMaxAvgRatioSearchResponse mmaResponse
                = (MinMaxAvgRatioSearchResponse) this.ratiosProvider.search(mmaRequest);
        if (!mmaResponse.isValid()) {
            return this.result;
        }

        this.mmaResult = mmaResponse.getResult();
        processResponse();

        return this.result;
    }

    private RatioSearchRequest createRequest() {
        final RatioSearchRequest result = new RatioSearchRequest(getProfile(), getLocales());
        result.setType(InstrumentTypeEnum.FND);
        result.setDataRecordStrategyClass(PreferIssuerFundQuoteStrategy.class);
        result.setVisitorClass(MinMaxAvgVisitor.class);
        result.setParameters(createParameters());
        return result;
    }

    private Map<String, String> createParameters() {
        final Map<String, String> result = new HashMap<>();
        addGroupBy(result);
        initMMAParameters(result);
        return result;
    }

    private void addGroupBy(Map<String, String> result) {
        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = AbstractFindersuchergebnis.getFields(InstrumentTypeEnum.FND);
        final RatioFieldDescription.Field field = fields.get(RatioDataRecord.Field.investmentFocus);
        result.put(field.name(), getInvestmentFocus());
        result.put(MinMaxAvgVisitor.KEY_GROUP_BY, field.name());
    }

    private List<Locale> getLocales() {
        return getContext().getLocales();
    }

    private Profile getProfile() {
        return getContext().getProfile();
    }

    private RequestContext getContext() {
        return RequestContextHolder.getRequestContext();
    }

    protected abstract void processResponse();

    protected abstract void initMMAParameters(Map<String, String> mmaParameters);
}
