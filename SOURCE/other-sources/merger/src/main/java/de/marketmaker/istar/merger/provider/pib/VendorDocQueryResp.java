/*
 * DocQueryRespons.java
 *
 * Created on 12.07.11 11:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author zzhao
 */
public class VendorDocQueryResp extends AbstractIstarResponse {

    public static final VendorDocQueryResp EMPTY_RESPONSE =
            new VendorDocQueryResp(0, Collections.<Item>emptyList());

    private static final long serialVersionUID = 2520302973539304384L;

    private final List<Item> items;

    private final int totalCount;

    public VendorDocQueryResp(int totalCount) {
        this.totalCount = totalCount;
        this.items = new ArrayList<>();
    }

    private VendorDocQueryResp(int totalCount, List<Item> items) {
        this.totalCount = totalCount;
        this.items = items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void add(Item item) {
        this.items.add(item);
    }

    public List<Item> getItems() {
        return items;
    }

    public int getCount() {
        return null == this.items ? 0 : this.items.size();
    }

    public static class Item implements Serializable {

        private static final long serialVersionUID = -4838086891453233785L;

        private final int docId;

        private final String isin;

        private final String wkn;

        private final String name;

        private final DateTime date;

        public Item(int docId, String wkn, String isin, String name, DateTime date) {
            this.docId = docId;
            this.wkn = wkn;
            this.isin = isin;
            this.name = name;
            this.date = date;
        }

        public int getDocId() {
            return docId;
        }

        public String getWkn() {
            return wkn;
        }

        public String getIsin() {
            return isin;
        }

        public String getName() {
            return name;
        }

        public DateTime getDate() {
            return date;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "docId='" + docId + '\'' +
                    ", wkn='" + wkn + '\'' +
                    ", isin='" + isin + '\'' +
                    ", name='" + name + '\'' +
                    ", date=" + date +
                    '}';
        }
    }

}
