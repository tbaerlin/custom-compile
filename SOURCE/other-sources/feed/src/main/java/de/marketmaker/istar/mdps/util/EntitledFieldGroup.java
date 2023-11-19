/*
 * EntitledFieldGroup.java
 *
 * Created on 04.10.13 12:17
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.util;

import java.util.BitSet;

import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.domain.data.SnapRecord.CP_1252;

/**
* @author oflege
*/
public class EntitledFieldGroup implements Comparable<EntitledFieldGroup> {

    public static final EntitledFieldGroup[] NULL = new EntitledFieldGroup[0];

    public static final EntitledFieldGroup ALL
            = new EntitledFieldGroup(0, CompactBitSet.fromTo(1, VwdFieldOrder.MAX_ORDER));

    private final byte[] selectorStr;

    private final CompactBitSet fieldOrders;

    final int selector;

    EntitledFieldGroup(int selector, CompactBitSet fieldOrders) {
        this.selector = selector;
        this.selectorStr = Integer.toString(selector).getBytes(CP_1252);
        this.fieldOrders = fieldOrders;
    }

    public byte[] getSelectorStr() {
        return selectorStr;
    }

    public CompactBitSet getFieldOrders() {
        return fieldOrders;
    }

    @Override
    public String toString() {
        return new String(selectorStr, CP_1252) + ":" + this.fieldOrders;
    }

    @Override
    public int compareTo(EntitledFieldGroup o) {
        final int cmp = this.fieldOrders.compareTo(o.fieldOrders);
        return (cmp != 0) ? cmp : Integer.compare(this.selector, o.selector);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntitledFieldGroup that = (EntitledFieldGroup) o;
        return (this.selector == that.selector) && fieldOrders.equals(that.fieldOrders);
    }

    @Override
    public int hashCode() {
        return 31 * this.selector + fieldOrders.hashCode();
    }
}
