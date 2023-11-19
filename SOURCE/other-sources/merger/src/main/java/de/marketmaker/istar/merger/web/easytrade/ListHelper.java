/*
 * ListHelper.java
 *
 * Created on 26.07.2006 14:47:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ListHelper {
    /**
     * Given n lists of the same length, this method removes elements from all lists except those from
     * index {@code lc.getOffset()} (incl) to  {@code lc.getEndOffset()} (excl.). In other words,
     * lc defines a sublist of elements to retain in each list.
     * @param lc defines which elements to keep
     * @param lists collections of elements, assumption is that elements with the same index in
     * each lists are related.
     */
    public static void clipPage(ListCommand lc, List... lists) {
        if (lists.length == 0) {
            return;
        }
        final int from = lc.getOffset();
        final int to = Math.min(lc.getEndOffset(), lists[0].size());

        for (List list : lists) {
            if (to <= from) {
                list.clear();
            }
            else {
                clipPage(list, from, to);
            }
        }
    }

    public static void clipPage(List l, int from, int to) {
        // not just l = l.sublist(from, to) as that would keep reference to original list. Helps GC
        final List tmp = new ArrayList(to - from);
        tmp.addAll(l.subList(from, to));
        l.clear();
        l.addAll(tmp);
    }
}
