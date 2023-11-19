/*
 * SearchRequest.java
 *
 * Created on 22.12.2004 14:05:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.util.EnumSet;
import java.util.List;

import de.marketmaker.istar.common.request.IstarRequest;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface SearchRequest extends IstarRequest {
    static final long serialVersionUID = 3622659722311525881L;
    
    String getSearchExpression();

    String getSearchConstraints();

    SearchRequestResultType getResultType();

    int getMaxNumResults();

    List<String> getDefaultFields();

    boolean isCountInstrumentResults();

    EnumSet<InstrumentTypeEnum> getCountTypes();

    EnumSet<InstrumentTypeEnum> getFilterTypes();

    Profile getProfile();

    List<String> getAbos();

    int getPagingOffset();

    int getPagingCount();

    boolean isUsePaging();

    String[] getSortFields();

    EnumSet<InstrumentSearcherImpl.SIMPLESEARCH_STEPS> getSearchSteps();

    boolean isFilterBlacklistMarkets();

    boolean isFilterOpraMarkets();
}
