/*
 * PagedResultSorter.java
 *
 * Created on 16.11.2005 12:56:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.min;

/**
 * Provides an efficient way of sorting an arbitrary set of items and providing a subset
 * of those sorted items as result.
 * If the subset is rather small compared to the total set, this implementation will invoke far
 * less compare methods than a {@link java.util.Collections#sort(java.util.List)}.
 * In any case, it will not perform more comparisons.
 * <p>
 * <b>Usage:</b><br>
 * <tt>
 * PagedResultSorter&lt;String&gt; prs<br>
 * = new PagedResultSorter&lt;String&gt;(0, 20, null);<br>
 * prs.add("foo");<br>
 * prs.add("bar");<br>
 * String[] result = prs.getResult();<br>
 * </tt>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PagedResultSorter<V> {
    private static final int MAX_SIZE = 1 << 20;

    private final int start;

    private final Comparator<? super V> sorter;

    private V[] values;

    /**
     * Current number of elements in this sorter.
     */
    private int count;

    /**
     * a total number of elements that we tried to add to this sorter. Some of those elements
     * could be discarded because of sorting. This field is always >= {@link count}
     */
    private int totalCount;

    private boolean sorted;

    private final int length;

    /**
     * We never store more elements than {@link limit}: for example, if our window starts at 50
     * and has length of 20, there is no need to store more than 70 elements.
     */
    private final int limit;

    /**
     * @param start subset to be returned starts at this position
     * @param length max. length of subset to be returned
     * @param sorter used to compare the elements in the result set, can be null IFF element class implements
     * {@link Comparable}
     */
    public PagedResultSorter(int start, int length, Comparator<? super V> sorter) {
        this(start, length, start + length, sorter);
    }

    /**
     * @param start subset to be returned starts at this position
     * @param length max. length of subset to be returned
     * @param maxAddedCount number of times {@link #add(Object)} will be called at most; useful if
     * start + length is larger than maxAddedCount, as the pre-allocated value array will be created
     * using the minimum of those two numbers as its size.
     * @param sorter used to compare the elements in the result set,
     * can be null IFF element class implements {@link Comparable}
     */
    public PagedResultSorter(int start, int length, int maxAddedCount,
            Comparator<? super V> sorter) {
        this.start = start;
        this.length = length;
        this.limit = start + length;
        this.sorter = sorter;
        this.values = (V[]) new Object[min(MAX_SIZE, min(this.limit, maxAddedCount))];
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return this.length;
    }

    private void ensureValuesLength(int x) {
        if (this.values.length < x) {
            this.values = Arrays.copyOf(this.values, x);
        }
    }

    public void merge(PagedResultSorter<V> other) {
        ensureValuesLength(min(this.limit, this.count + other.count));

        if ((this.count + other.count) <= this.limit) {
            System.arraycopy(other.values, 0, this.values, this.count, other.count);
            this.count += other.count;
            this.totalCount += other.totalCount;
            return;
        }

        other.sort();
        int j = 0;
        while (!this.sorted && j < other.count) {
            add(other.values[j++]);
        }
        this.totalCount += (other.totalCount - j);

        // since this and other are sorted, we can do a merge sort and therefore avoid
        // the System.arraycopy in add
        final V[] copy = Arrays.copyOf(this.values, this.count);
        int i = 0;
        int n = 0;
        while (n < this.limit && i < this.count && j < other.count) {
            final int cmp = compare(copy[i], other.values[j]);
            if (cmp <= 0) {
                this.values[n++] = copy[i++];
            }
            else {
                this.values[n++] = other.values[j++];
            }
        }
        while (n < this.limit && i < this.count) {
            this.values[n++] = copy[i++];
        }
        while (n < this.limit && j < other.count) {
            this.values[n++] = other.values[j++];
        }
        this.count = n;
    }

    /**
     * Convenience method, adds all elements of the given collection
     */
    public void addAll(Collection<V> values) {
        values.forEach(this::add);
    }

    /**
     * If limit is not yet reached, we just append new element to the end.
     * Otherwise, we either discard the new element since it's bigger than
     * our last element or insert it in its correct position and get rid of
     * the current last element.
     */
    public void add(V value) {
        this.totalCount++;

        if (this.count < this.limit) {
            this.values[this.count++] = value;
            if (this.count == this.values.length) {
                ensureValuesLength(Math.min(this.limit, this.values.length * 2));
            }
            if (this.count == this.limit) {
                sort();
            }
            return;
        }

        sort();

        if (compare(getLast(), value) <= 0) {
            // value not included in result
            return;
        }

        int pos = binarySearch(value);
        if (pos >= 0) {
            // found a matching element in our values; to preserve the input order, we
            // have to insert after the last matching value
            ++pos;
            while (compare(this.values[pos], value) == 0) {
                ++pos;
            }
        }
        else {
            pos = -pos - 1;
        }

        System.arraycopy(this.values, pos, this.values, pos + 1, this.count - pos - 1);
        this.values[pos] = value;
    }

    /**
     * Returns the last value maintained by this sorter. Returns null if less than
     * <tt>start + length</tt> elements have been added; otherwise, the element will be
     * the <tt>(start + length)</tt>-th of all added elements in sorted order.
     * @return last element, may be null
     */
    public V getLast() {
        return (values.length < this.limit) ? null : this.values[this.limit - 1];
    }

    private int binarySearch(V value) {
        if (this.sorter != null) {
            return Arrays.binarySearch(this.values, value, this.sorter);
        }
        else {
            return Arrays.binarySearch(this.values, value);
        }
    }

    private int compare(V o1, V o2) {
        if (this.sorter != null) {
            return this.sorter.compare(o1, o2);
        }
        else {
            return ((Comparable) o1).compareTo(o2);
        }
    }

    private void sort() {
        if (this.sorted) {
            return;
        }
        if (this.sorter != null) {
            Arrays.sort(this.values, 0, this.count, this.sorter);
        }
        else {
            Arrays.sort(this.values, 0, this.count);
        }
        this.sorted = true;
    }

    public int size() {
        return Math.max(0, this.count - this.start);
    }

    public List<V> getResult() {
        if (this.count == 0 || this.count <= this.start) {
            return Collections.emptyList();
        }
        sort();
        final V[] result = (V[]) new Object[this.count - this.start];
        System.arraycopy(this.values, this.start, result, 0, result.length);
        return Arrays.asList(result);
    }

    public int getTotalCount() {
        return this.totalCount;
    }
}
