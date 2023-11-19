/*
 * SortUtil.java
 *
 * Created on 13.07.2006 13:36:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Given a base list and a number of sibling lists, an instance of this class can
 * be used to sort the base list according to some Comparator and then sort the
 * sibling lists in such a way that elements at the same positions as in the unsorted
 * lists will again be in the same positions in the sorted lists.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MultiListSorter {

    private final Comparator comparator;

    public MultiListSorter(final Comparator comparator, boolean inverse) {
        this.comparator = inverse ? Collections.reverseOrder(comparator) : comparator;
    }

    private static class PositionWrapper {
        private final Object value;
        private final int unsortedPosition;

        PositionWrapper(Object value, int unsortedPosition) {
            this.value = value;
            this.unsortedPosition = unsortedPosition;
        }
    }


    public void sort(List... lists) {
        if (lists.length == 0 || lists[0].isEmpty()) {
            return;
        }
        if (lists.length == 1) {
            Collections.sort(lists[0], this.comparator);
            return;
        }

        final List<PositionWrapper> wrappedBase = createWrapper(lists[0]);

        wrappedBase.sort(new Comparator<PositionWrapper>() {
            public int compare(PositionWrapper o1, PositionWrapper o2) {
                return MultiListSorter.this.comparator.compare(o1.value, o2.value);
            }
        });

        for (int i = 0; i < lists.length; i++) {
            final List target = lists[i];
            final List tmp = new ArrayList(target.size());
            tmp.addAll(target);
            int n = 0;
            for (int j = 0; j < wrappedBase.size(); j++) {
                final PositionWrapper pw = wrappedBase.get(j);
                target.set(n++, tmp.get(pw.unsortedPosition));
            }
        }
    }

    private List<PositionWrapper> createWrapper(List base) {
        final List<PositionWrapper> tmp = new ArrayList<>(base.size());
        int n = 0;
        for (Object t : base) {
            tmp.add(new PositionWrapper(t, n++));
        }
        return tmp;
    }
}
