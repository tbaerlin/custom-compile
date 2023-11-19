/*
 * IndexMembershipResponse.java
 *
 * Created on 28.03.14 14:02
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author oflege
 */
public class IndexMembershipResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 122L;

    public static final class Item implements Serializable {
        static final long serialVersionUID = 123L;

        public final long indexQid;

        public final String consituentGroup;

        private Item(Map.Entry<Long, String> e) {
            this.indexQid = e.getKey();
            this.consituentGroup = e.getValue();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder().append(indexQid).append(".qid");
            if (this.consituentGroup != null) {
                sb.append("/").append(this.consituentGroup);
            }
            return sb.toString();
        }
    }

    private List<Item> toItems(Map<Long, String> map) {
        if (map == null) {
            return null;
        }
        if (map.isEmpty()) {
            return Collections.emptyList();
        }
        if (map.size() == 1) {
            return Collections.singletonList(new Item(map.entrySet().iterator().next()));
        }
        ArrayList<Item> result = new ArrayList<>(map.size());
        for (Map.Entry<Long, String> e : map.entrySet()) {
            result.add(new Item(e));
        }
        return result;
    }

    private final List<Item> items;

    public IndexMembershipResponse(Map<Long, String> map) {
        this.items = toItems(map);
        if (this.items == null) {
            setInvalid();
        }
    }

    public IndexMembershipResponse(List<Item> allowedItems) {
        this.items = allowedItems;
    }


    public List<Item> getItems() {
        return items;
    }

    public Set<Long> getIndexQuoteIds() {
        if (this.items == null || this.items.isEmpty()) {
            return Collections.emptySet();
        }
        final HashSet<Long> result = new HashSet<>();
        for (IndexMembershipResponse.Item item : this.items) {
            result.add(item.indexQid);
        }
        return result;
    }

    public boolean hasPermissionedItems() {
        for (Item item : items) {
            if (item.consituentGroup != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", items=").append(this.items);
    }
}
