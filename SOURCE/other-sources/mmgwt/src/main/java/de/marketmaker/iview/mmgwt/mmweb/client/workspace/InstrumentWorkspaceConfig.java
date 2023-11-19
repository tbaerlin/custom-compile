/*
 * InstrumentWorkspaceConfig.java
 *
 * Created on 05.08.2008 14:11:01
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import java.io.Serializable;
import java.util.ArrayList;

import de.marketmaker.iview.mmgwt.mmweb.client.data.WorkspaceConfig;

/**
 * @author Oliver Flege
* @author Thomas Kiesgen
*/
public class InstrumentWorkspaceConfig extends WorkspaceConfig {
    protected static final long serialVersionUID = -6056968756987869860L;
    
    private ArrayList<Item> items = new ArrayList<>();

    public static class Item implements Comparable<Item>, Serializable {
        protected static final long serialVersionUID = 1L;

        private String qid;
        private String iid;
        private String name;
        private String alias;
        private String type;

        private String marketName;
        private String marketCode;

        private int order;

        public Item() {
            // needed for GWT serialization
        }

        Item(String iid, String qid, String name, String type, String marketName, String marketCode) {
            this.iid = iid;
            this.qid = qid;
            this.name = name;
            this.type = type;
            this.marketName = marketName;
            this.marketCode = marketCode;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return this.qid != null ? this.qid : this.iid;
        }

        public String getIid() {
            return this.iid;
        }

        public String getQid() {
            return this.qid;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getMarketCode() {
            return marketCode;
        }

        public String getMarketName() {
            return marketName;
        }

        public int compareTo(Item o) {
            final int n = this.name.compareTo(o.name);
            return n != 0 ? n : getId().compareTo(o.getId());
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        @Override
        public String toString() {
            return getId();
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public int getOrder() {
            return order;
        }
    }

    public InstrumentWorkspaceConfig() {
    }

    public ArrayList<Item> getItems() {
        return this.items;
    }

    public void addItem(Item item) {
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "instruments" + this.items.toString(); // $NON-NLS-0$
    }
}
