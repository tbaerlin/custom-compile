/*
 * SymbolSortResponse.java
 *
 * Created on 17.02.2005 13:48:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.SnapRecord;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SymbolSortResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 7L;

    /**
     * @deprecated use items instead
     */
    private List<String> vendorkeys = Collections.emptyList();

    /**
     * the minimum vendorkeys/snapRecords wrt. the corresponding lists in the request.
     */
    private final List<Item> items;

    /**
     * Builds an Item that is the minimum of all vendorkey/snapRecord tuples
     * submitted to {@link #update(String, de.marketmaker.istar.domain.data.SnapRecord)},
     * based on a given SnapRecord-Comparator
     */
    public static class Builder {
        private final Comparator<SnapRecord> cmp;

        private String vendorkey;

        private SnapRecord snap;

        public Builder(Comparator<SnapRecord> cmp) {
            this.cmp = cmp;
        }

        public Builder update(String vendorkey, SnapRecord snap) {
            if (this.vendorkey == null) {
                this.vendorkey = vendorkey;
                this.snap = snap;
            }
            else if (this.cmp.compare(this.snap, snap) > 0) {
                this.vendorkey = vendorkey;
                this.snap = snap;
            }
            return this;
        }

        public Item build() {
            return (this.vendorkey == null) ? null : new Item(this.vendorkey, this.snap);
        }
    }

    public static class Item implements Serializable {
        static final long serialVersionUID = 7L;

        private final String vendorkey;

        private final SnapRecord snapRecord;

        private Item(String vendorkey, SnapRecord snapRecord) {
            this.vendorkey = vendorkey;
            this.snapRecord = snapRecord;
        }

        public String getVendorkey() {
            return vendorkey;
        }

        public SnapRecord getSnapRecord() {
            return snapRecord;
        }
    }

    public SymbolSortResponse(List<Item> result) {
        this.items = new ArrayList<>(result);
    }

    public SymbolSortResponse() {
        this.items = null;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<String> getVendorkeys() {
        if (this.vendorkeys != null) {
            return this.vendorkeys;
        }

        final ArrayList<String> result = new ArrayList<>(this.items.size());
        for (Item item : items) {
            result.add(item.getVendorkey());
        }
        return result;
    }

    /**
     * @deprecated use {@link SymbolSortResponse#SymbolSortResponse(java.util.List)}
     */
    public void setVendorkeys(List<String> vendorkeys) {
        this.vendorkeys = vendorkeys;
    }

    /**
     * all items in this object that are larger than the corresponding items in tmp wrt. cmp
     * will be replaced by those items, so that after merge this object's items will contain
     * the combined minimums of all items.
     * @param tmp to be merged
     * @param cmp used to compare items
     */
    public void merge(SymbolSortResponse tmp, Comparator<SnapRecord> cmp) {
        if (this.items.size() != tmp.items.size()) {
            throw new IllegalStateException(this.items.size() + "!=" + tmp.items.size());
        }
        for (int i = 0; i < items.size(); i++) {
            this.items.set(i, min(this.items.get(i), tmp.items.get(i), cmp));
        }
    }

    private Item min(Item i1, Item i2, Comparator<SnapRecord> cmp) {
        if (i1 == null) {
            return i2;
        }
        if (i2 == null) {
            return i1;
        }
        return cmp.compare(i1.getSnapRecord(), i2.getSnapRecord()) <= 0 ? i1 : i2;
    }
}
