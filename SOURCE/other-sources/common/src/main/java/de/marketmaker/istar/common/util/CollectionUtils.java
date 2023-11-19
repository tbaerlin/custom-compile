/*
 * CollectionUtils.java
 *
 * Created on 07.09.2006 09:28:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CollectionUtils {
    private CollectionUtils() {
    }

    /**
     * Applies the given comparators in order and returns the first result that is not 0; if
     * all comparators return 0, the result will be 0 as well.
     * @param comparators used for comparison
     * @return comparator chain
     */
    public static <V> Comparator<V> chain(final Comparator<V>... comparators) {
        return (o1, o2) -> {
            for (Comparator<V> comparator : comparators) {
                final int cmp = comparator.compare(o1, o2);
                if (cmp != 0) {
                    return cmp;
                }
            }
            return 0;
        };
    }

    /**
     * Inverts the given map (entry keys become values and vice versa)
     * and returns the result in a new {@link HashMap}
     * @param map to be inverted
     * @return inverted map
     */
    public static <V, K> Map<V, K> invert(Map<K, V> map) {
        return invert(map, new HashMap<>(map.size()));
    }

    /**
     * Inverts the given map and add the resulting tuples to result. In contrast to
     * {@link #invert(java.util.Map)}, this version allows to control the type of the
     * returned map
     * @param map input map
     * @param result output map
     * @return result
     */
    public static <V, K> Map<V, K> invert(Map<K, V> map, Map<V, K> result) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            result.put(entry.getValue(), entry.getKey());
        }
        return result;
    }

    public static int removeNulls(Collection c) {
        int n = 0;
        for (final Iterator it = c.iterator(); it.hasNext();) {
            if (it.next() == null) {
                it.remove();
                n++;
            }
        }
        return n;
    }

    public static int removeNulls(Collection... collections) {
        if (collections.length == 0 || collections[0].isEmpty()) {
            return 0;
        }
        if (collections.length == 1) {
            return removeNulls(collections[0]);
        }

        final int size = collections[0].size();
        for (final Collection c : collections) {
            if (c.size() != size) {
                throw new IllegalStateException("size does not match: " + c.size() + "!=" + size);
            }
        }

        final Iterator[] its = new Iterator[collections.length];
        for (int i = 0; i < collections.length; i++) {
            its[i] = collections[i].iterator();
        }

        int n = 0;
        for (final Iterator itPrimary = its[0]; itPrimary.hasNext();) {
            for (int i = 1; i < its.length; i++) {
                its[i].next();
            }
            if (itPrimary.next() == null) {
                for (final Iterator it : its) {
                    it.remove();
                }
                n++;
            }
        }
        return n;
    }

    /**
     * Adapted from Collections.nCopies, but use ArrayList to support remove method.
     */
    public static <T> List<T> nCopiesRemovable(int n, T o) {
        if (n < 0) {
            throw new IllegalArgumentException("List length = " + n);
        }
        final ArrayList<T> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(o);
        }
        return list;
    }
}
