/*
 * SimpleComparatorChain.java
 *
 * Created on 26.10.2005 12:04:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A chain of comparators used to compare objects.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SimpleComparatorChain<K> implements Comparator<K>, Serializable {
    protected static final long serialVersionUID = 1L;

    private final List<Comparator<K>> comparators = new ArrayList<>();

    public void add(Comparator<K> c) {
        this.comparators.add(c);
    }

    public List<Comparator<K>> getComparators() {
        return Collections.unmodifiableList(this.comparators);
    }

    @Override
    public int compare(K o1, K o2) {
        for (final Comparator<K> comparator : this.comparators) {
            final int result = comparator.compare(o1, o2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    public String toString() {
        return "SimpleComparatorChain[" + this.comparators + "]";
    }

}
