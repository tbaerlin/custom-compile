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
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DocQueryResponse extends AbstractIstarResponse {

    public static final DocQueryResponse EMPTY_RESPONSE =
            new DocQueryResponse(0, Collections.<Item>emptyList());

    private static final long serialVersionUID = 5116420070515524404L;

    private final List<Item> items;

    private final int totalCount;

    public DocQueryResponse(int totalCount) {
        this.totalCount = totalCount;
        this.items = new ArrayList<>();
    }

    private DocQueryResponse(int totalCount, List<Item> items) {
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

        private static final long serialVersionUID = -2456304791599480931L;

        private final String id;

        private final String wkn;

        private final String isin;

        private final String name;

        private final String user;

        private final DateTime date;

        private final String type;

        private final String localization;

        public Item(String id, String wkn, String isin, String name, String user, DateTime date,
                String type, String localization) {
            this.id = id;
            this.wkn = wkn;
            this.isin = isin;
            this.name = name;
            this.user = user;
            this.date = date;
            this.type = type;
            this.localization = localization;
        }

        public String getLocalization() {
            return localization;
        }

        public String getType() {
            return type;
        }

        public String getUser() {
            return user;
        }

        public String getId() {
            return id;
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
                    "id='" + id + '\'' +
                    ", wkn='" + wkn + '\'' +
                    ", isin='" + isin + '\'' +
                    ", name='" + name + '\'' +
                    ", date=" + date +
                    ", type=" + type +
                    ", localization=" + localization +
                    '}';
        }
    }

}
