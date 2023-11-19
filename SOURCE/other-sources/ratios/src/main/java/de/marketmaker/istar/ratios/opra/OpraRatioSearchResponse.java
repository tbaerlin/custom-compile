/*
 * RatioSearchResponse.java
 *
 * Created on 27.10.2005 17:21:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.opra;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

import java.util.Collections;
import java.util.List;

/**
 * An OPRA ratio search response which encapsulates the search result and provides paging information.
 * <p>
 * The search result could be obtained by {@link #getItems()}.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OpraRatioSearchResponse extends AbstractIstarResponse {

    static final long serialVersionUID = 1L;

    private List<OpraItem> items;

    private int numTotal;

    private int offset;

    private int length;

    public List<OpraItem> getItems() {
        return (this.items != null) ? this.items : Collections.emptyList();
    }

    public void setItems(List<OpraItem> items) {
        this.items = items;
    }

    public int getNumTotal() {
        return numTotal;
    }

    public void setNumTotal(int numTotal) {
        this.numTotal = numTotal;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}