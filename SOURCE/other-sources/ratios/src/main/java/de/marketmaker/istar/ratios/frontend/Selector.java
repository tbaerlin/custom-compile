/*
 * Selector.java
 *
 * Created on 26.10.2005 10:44:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.Comparator;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Selector {

    public final Comparator<Selector> BY_COST = new Comparator<Selector>() {
        @Override
        public int compare(Selector o1, Selector o2) {
            return o1.getCost() - o2.getCost();
        }
    };

    public final Selector TRUE = new Selector() {
        @Override
        public boolean select(Selectable s) {
            return true;
        }

        @Override
        public int getCost() {
            return 0;
        }

        @Override
        public String toString() {
            return "Selector[*]";
        }
    };

    public final Selector FALSE = new NotSelector(TRUE);

    boolean select(Selectable s);

    /**
     * @return the cost of evaluating this selector; ideally, the least expensive expression should
     * be evaluated first, so that other, more costly selectors are not evaluated at all.
     */
    int getCost();
}
