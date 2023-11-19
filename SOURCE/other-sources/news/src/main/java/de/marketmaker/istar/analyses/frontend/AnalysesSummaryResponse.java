/*
 * AnalysesMetaResponse.java
 *
 * Created on 21.03.12 08:29
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.frontend;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.NullStockAnalysisSummary;
import de.marketmaker.istar.domain.data.StockAnalysisAims;
import de.marketmaker.istar.domain.data.StockAnalysisSummary;
import de.marketmaker.istar.domainimpl.data.StockAnalysisAimsImpl;

/**
 * @author oflege
 */
public class AnalysesSummaryResponse extends AbstractIstarResponse {
    protected static final long serialVersionUID = 1L;

    private static class Item implements Serializable {
        protected static final long serialVersionUID = 1L;

        private StockAnalysisSummary summary;

        private StockAnalysisAims aims;

        private String sector;
    }

    private final Map<Long, Item> items = new HashMap<>();
    
    public AnalysesSummaryResponse() {
    }
    
    public Collection<Long> getInstrumentIds() {
        return Collections.unmodifiableSet(this.items.keySet());
    }
    
    public void add(long id, StockAnalysisSummary summary, StockAnalysisAims aims, String sector) {
        Item item = getOrCreate(id);
        item.summary = summary;
        item.aims = aims;
        item.sector = sector;
    }

    private Item getOrCreate(long id) {
        final Item existing = this.items.get(id);
        if (existing != null) {
            return existing;
        }
        final Item result = new Item();
        this.items.put(id, result);
        return result;
    }

    public StockAnalysisSummary getSummary(long id) {
        Item item = this.items.get(id);
        return (item != null && item.summary != null) ? item.summary : NullStockAnalysisSummary.INSTANCE;
    }

    public StockAnalysisAims getAims(long id) {
        Item item = this.items.get(id);
        return (item != null && item.aims != null) ? item.aims : StockAnalysisAimsImpl.NULL;
    }

    public String getSector(long id) {
        Item item = this.items.get(id);
        return (item != null) ? item.sector : null;
    }
}
