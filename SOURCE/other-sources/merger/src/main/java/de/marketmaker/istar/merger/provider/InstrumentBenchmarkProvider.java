/*
 * InstrumentBenchmarkMethod.java
 *
 * Created on 24.03.14 11:56
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.instrument.IndexCompositionProvider;
import de.marketmaker.istar.instrument.IndexMembershipRequest;
import de.marketmaker.istar.instrument.IndexPrioritySupport;

/**
 * @author oflege
 */
class InstrumentBenchmarkProvider {
    public static final long BUND_FUTURE_QID = 54394L;

    public static final long CONF_FUTURE_QID = 394865L;

    public static final long EUR_USD_QID = 7223679L;

    private final static Map<String, Long> COUNTRY_TO_INDEXQUOTEID = new HashMap<>();

    static {
        COUNTRY_TO_INDEXQUOTEID.put("de", 106547L);
        COUNTRY_TO_INDEXQUOTEID.put("us", 75123L);
    }

    private final IndexCompositionProvider indexCompositionProvider;

    InstrumentBenchmarkProvider(
            IndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    Long getBenchmarkId(Instrument instrument) {
        final InstrumentTypeEnum type = instrument.getInstrumentType();
        if (type == InstrumentTypeEnum.STK || type == InstrumentTypeEnum.GNS) {
            final Long id = getBenchmarkIdIndexBased(instrument);
            if (id != null) {
                return id;
            }
        }

        return getBenchmarkIdStatic(type, instrument.getSymbolIsin());
    }

    private Long getBenchmarkIdIndexBased(Instrument instrument) {
        final Set<Long> indexIds = getIndexQuoteIds(instrument);

        if (!indexIds.isEmpty()) {
            return getHighestPriorityIdFrom(indexIds);
        }

        final String isin = instrument.getSymbolIsin();
        if (isin == null) {
            return null;
        }

        final String countryCode = isin.substring(0, 2).toLowerCase();
        return COUNTRY_TO_INDEXQUOTEID.get(countryCode);
    }

    private Set<Long> getIndexQuoteIds(Instrument instrument) {
        return this.indexCompositionProvider
                .getIndexMembership(new IndexMembershipRequest(instrument)).getIndexQuoteIds();
    }

    private Long getBenchmarkIdStatic(final InstrumentTypeEnum type,
            final String isin) {
        if (type == InstrumentTypeEnum.BND || type == InstrumentTypeEnum.GNS) {
            if (isin != null && isin.startsWith("CH")) {
                return CONF_FUTURE_QID; // Conf Future, 969242.DTB.CON
            }
            return BUND_FUTURE_QID; // Bund-Future, 965264.DTB.CON
        }

        if (type == InstrumentTypeEnum.CUR) {
            return EUR_USD_QID; // EUR/USD, EUR.FXVWD
        }

        return null;
    }

    private Long getHighestPriorityIdFrom(Set<Long> indexIds) {
        return IndexPrioritySupport.getWithMaxWeightFrom(indexIds);
    }
}
