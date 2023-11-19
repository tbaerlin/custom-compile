/*
 * PagesWorkspaceConfig.java
 *
 * Created on 05.08.2008 14:11:01
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import java.io.Serializable;
import java.util.ArrayList;

import de.marketmaker.iview.mmgwt.mmweb.client.data.WorkspaceConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.PageType;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PagesWorkspaceConfig extends WorkspaceConfig {
    protected static final long serialVersionUID = -6795138825016690431L;
    
    private ArrayList<Item> items = new ArrayList<>();

    public static class Item implements Comparable<Item>, Serializable {
        protected static final long serialVersionUID = 1L;

        private String key;

        private String alias = null;

        private PageType type;

        private int order;

        public Item() {
            // needed for GWT serialization
        }

        Item(String key, PageType type) {
            this.key = key;
            this.type = type;
        }

        public PageType getType() {
            return type;
        }

        public String getKey() {
            return key;
        }

        public int compareTo(Item o) {
            if (this.type != o.type) {
                return this.type.compareTo(o.type);
            }
            return this.key.compareTo(o.key);
        }

        @Override
        public String toString() {
            return this.type + ":" + this.key; // $NON-NLS-0$
        }

        public String getAlias() {
            return this.alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public int getOrder() {
            return this.order;
        }
    }

    public PagesWorkspaceConfig() {
    }

    public ArrayList<Item> getItems() {
        return this.items;
    }

    public void addItem(Item item) {
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "pages" + this.items.toString(); // $NON-NLS-0$
    }
}
