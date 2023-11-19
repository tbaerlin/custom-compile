/*
 * Item.java
 *
 * Created on 22.08.13 08:47
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.ipomonitor;

import java.util.Comparator;

import org.joda.time.DateTime;

/**
* @author oflege
*/
class Item {

    static Comparator<Item> BY_CREATED = new Comparator<Item>() {

        @Override
        public int compare(Item o1, Item o2) {
            final int cmp = o1.created - o2.created;
            if (cmp != 0) {
                return cmp;
            }
            return o1.vwdcode.compareTo(o2.vwdcode);
        }
    };

    String vwdcode;

    String market;

    int created;

    int indexed;

    int biskeyed;

    Item(String vwdcode, String market, int created, int indexed, int biskeyed) {
        this.vwdcode = vwdcode;
        this.market = market;
        this.created = created;
        this.indexed = indexed;
        this.biskeyed = biskeyed;
    }

    boolean isComplete() {
        return isIndexed() && isWithBiskey();
    }

    boolean isWithBiskey() {
        return this.biskeyed > 0;
    }

    boolean isIndexed() {
        return this.indexed > 0;
    }

    DateTime created() {
        return new DateTime(this.created * 1000L);
    }

}
