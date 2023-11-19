/*
 * SymbolSortRequest.java
 *
 * Created on 17.02.2005 13:48:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SymbolSortRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 713123L;

    public enum Type {
        LAST,
        VOLUME,
        NUM_TRADES
    }

    private final Type type;

    @Override
    public String toString() {
        return "SymbolSortRequest[" +
                "type=" + type +
                ", items=" + items +
                ']';
    }

    private List<List<VendorkeyWithDelay>> items = Collections.emptyList();

    public SymbolSortRequest(Type type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public List<List<VendorkeyWithDelay>> getItems() {
        return items;
    }

    public void setItems(List<List<VendorkeyWithDelay>> items) {
        this.items = items;
    }

    public Comparator<SnapRecord> getComparator() {
        switch (this.type) {
            case LAST:
                return SnapRecordUtils.COMPARATOR_LAST;
            case VOLUME:
                return SnapRecordUtils.COMPARATOR_VOLUME;
            case NUM_TRADES:
                return SnapRecordUtils.COMPARATOR_NUM_TRADES;
            default:
                return null;
        }
    }

}
