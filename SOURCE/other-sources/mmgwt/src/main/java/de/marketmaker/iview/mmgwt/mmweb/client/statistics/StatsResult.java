/*
 * PagesStats.java
 *
 * Created on 20.01.2010 17:52:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Contains statistics of pages visited by users in a certain period
 * @author oflege
 */
public class StatsResult implements Serializable {
    /**
     * How often a page has been visited in a certain period
     */
    public static class Item implements Serializable {
        private String name;
        private int num;
        private int period; // either yyyyMM, yyyyww, or yyyyddd depending on the request

        public Item() {
        }

        public Item(String name, int num, int period) {
            this.name = name;
            this.num = num;
            this.period = period;
        }

        public String getName() {
            return name;
        }

        public int getNum() {
            return num;
        }

        public int getPeriod() {
            return period;
        }
    }


    private ArrayList<Item> elements = new ArrayList<Item>();

    public void add(String name, int num, int period) {
        this.elements.add(new Item(name, num, period));
    }

    public ArrayList<Item> getItems() {
        return this.elements;
    }
}
