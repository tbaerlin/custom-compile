/*
 * ArraysUtil.java
 *
 * Created on 09.03.2005 13:07:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.util.Arrays;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public final class ArraysUtil {

    private ArraysUtil() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    public static int sum(int[] values) {
        return Arrays.stream(values).sum();
    }

    public static <T> T[] copyOf(T[] original) {
        if (original == null) {
            return null;
        }
        return Arrays.copyOf(original, original.length);
    }

    /**
     * Sorts the given "val" array's index array "idx" according to the natural order of elements in
     * "val" array.
     *
     * <p>
     * After the sort element order in "val" won't be changed, but its index order in "idx" is
     * changed, so that elements obtained by val[idx[i]] are in ascending order.
     *
     * <p>
     * Given <tt>idx={0, 1, 2}, val={12, 11, 13}</tt>, after sorting they would look
     * like following: <tt>idx={1, 0, 3}, val={12, 11, 13}</tt> so that if one accesses the elements
     * in val through the ordered index in idx, one will get an ordered val back, whereby the val
     * array stays unchanged.
     *
     * <p>
     * Implementation adapted from {@link java.util.Arrays#sort(long[])}.
     *
     * @param idx an index array
     * @param val a value array
     */
    public static void sort(int[] idx, long[] val) {
        sort1(idx, val, 0, val.length);
    }

    /**
     * Sorts the given "val" array, whereby the elements of the same position in the given array of
     * int arrays are kept synchronized. If one looks them as rows in a table, the sorting is on the
     * first row with values of type long, whereby the elements of other rows of the same column are
     * kept synchronized.
     *
     * <p>
     * Given <tt>val={2, 1, 3}, das={{11, 12, 13},{22, 21, 23}}</tt>, after sorting they would look
     * like following: <tt>val={1, 2, 3}, das={{12, 11, 13},{21, 22, 23}}</tt>
     *
     * <p>
     * Implementation adapted from {@link java.util.Arrays#sort(long[])}.
     *
     * @param val a value array of type long
     * @param das an array of int arrays
     */
    public static void sort(long[] val, long[]... das) {
        sort1(val, 0, val.length, das);
    }

    /**
     * Same as {@link #sort(int[], long[])}, but only for a subrange of values in those arrays
     * @param val master sort array
     * @param fromIndex inclusive
     * @param toIndex exclusive
     * @param das dependent sort array
     */
    public static void sort(long[] val, int fromIndex, int toIndex, long[]... das) {
        sort1(val, fromIndex, toIndex - fromIndex, das);
    }

    /**
     * Sorts the given "val" array, whereby the elements of the same position in the given array of
     * int arrays are kept synchronized. If one looks them as rows in a table, the sorting is on the
     * first row with values of type long, whereby the elements of other rows of the same column are
     * kept synchronized.
     *
     * <p>
     * Given <tt>val={2, 1, 3}, das={{11, 12, 13},{22, 21, 23}}</tt>, after sorting they would look
     * like following: <tt>val={1, 2, 3}, das={{12, 11, 13},{21, 22, 23}}</tt>
     *
     * <p>
     * Implementation adapted from {@link java.util.Arrays#sort(long[])}.
     *
     * @param val a value array of type long
     * @param das an array of int arrays
     */
    public static void sort(long[] val, int[]... das) {
        sort1(val, 0, val.length, das);
    }

    /**
     * Sorts the specified sub-array of longs into ascending order.
     */
    private static void sort1(long x[], int off, int len, int[]... das) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (int i = off; i < len + off; i++)
                for (int j = i; j > off && x[j - 1] > x[j]; j--)
                    swap(x, j, j - 1, das);
            return;
        }

        // Choose a partition element, v
        int m = off + (len >> 1);       // Small arrays, middle element
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {        // Big arrays, pseudomedian of 9
                int s = len / 8;
                l = med3(x, l, l + s, l + 2 * s);
                m = med3(x, m - s, m, m + s);
                n = med3(x, n - 2 * s, n - s, n);
            }
            m = med3(x, l, m, n); // Mid-size, med of 3
        }
        long v = x[m];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v)
                    swap(x, a++, b, das);
                b++;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v)
                    swap(x, c, d--, das);
                c--;
            }
            if (b > c)
                break;
            swap(x, b++, c--, das);
        }

        // Swap partition elements back to middle
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(x, off, b - s, s, das);
        s = Math.min(d - c, n - d - 1);
        vecswap(x, b, n - s, s, das);

        // Recursively sort non-partition-elements
        if ((s = b - a) > 1)
            sort1(x, off, s, das);
        if ((s = d - c) > 1)
            sort1(x, n - s, s, das);
    }
    /**
     * Sorts the specified sub-array of longs into ascending order.
     */
    private static void sort1(long x[], int off, int len, long[]... das) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (int i = off; i < len + off; i++)
                for (int j = i; j > off && x[j - 1] > x[j]; j--)
                    swap(x, j, j - 1, das);
            return;
        }

        // Choose a partition element, v
        int m = off + (len >> 1);       // Small arrays, middle element
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {        // Big arrays, pseudomedian of 9
                int s = len / 8;
                l = med3(x, l, l + s, l + 2 * s);
                m = med3(x, m - s, m, m + s);
                n = med3(x, n - 2 * s, n - s, n);
            }
            m = med3(x, l, m, n); // Mid-size, med of 3
        }
        long v = x[m];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v)
                    swap(x, a++, b, das);
                b++;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v)
                    swap(x, c, d--, das);
                c--;
            }
            if (b > c)
                break;
            swap(x, b++, c--, das);
        }

        // Swap partition elements back to middle
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(x, off, b - s, s, das);
        s = Math.min(d - c, n - d - 1);
        vecswap(x, b, n - s, s, das);

        // Recursively sort non-partition-elements
        if ((s = b - a) > 1)
            sort1(x, off, s, das);
        if ((s = d - c) > 1)
            sort1(x, n - s, s, das);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(long x[], int a, int b, int[]... das) {
        long t = x[a];
        x[a] = x[b];
        x[b] = t;
        for (int[] ia : das) {
            int tmp = ia[a];
            ia[a] = ia[b];
            ia[b] = tmp;
        }
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(long x[], int a, int b, long[]... das) {
        long t = x[a];
        x[a] = x[b];
        x[b] = t;
        for (long[] ia : das) {
            long tmp = ia[a];
            ia[a] = ia[b];
            ia[b] = tmp;
        }
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(long x[], int a, int b, int n, int[]... das) {
        for (int i = 0; i < n; i++, a++, b++)
            swap(x, a, b, das);
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(long x[], int a, int b, int n, long[]... das) {
        for (int i = 0; i < n; i++, a++, b++)
            swap(x, a, b, das);
    }

    /**
     * Returns the index of the median of the three indexed longs.
     */
    private static int med3(long x[], int a, int b, int c) {
        return (x[a] < x[b] ?
                (x[b] < x[c] ? b : x[a] < x[c] ? c : a) :
                (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }

    /**
     * Sorts the given "val" array's index array "idx" according to the natural order of elements in
     * "val" array.
     *
     * <p>
     * After the sort element order in "val" won't be changed, but its index order in "idx" is
     * changed, so that elements obtained by val[idx[i]] are in ascending order.
     *
     * <p>
     * Given <tt>idx={0, 1, 2}, val={12, 11, 13}</tt>, after sorting they would look
     * like following: <tt>idx={1, 0, 3}, val={12, 11, 13}</tt> so that if one accesses the elements
     * in val through the ordered index in idx, one will get an ordered val back, whereby the val
     * array stays unchanged.
     *
     * <p>
     * Implementation adapted from {@link java.util.Arrays#sort(int[])}.
     *
     * @param idx an index array
     * @param val a value array
     */
    public static void sort(int[] idx, int[] val) {
        sort1(idx, val, 0, val.length);
    }

    /**
     * Sorts the specified sub-array of longs into ascending order.
     */
    private static void sort1(int[] idx, long x[], int off, int len) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (int i = off; i < len + off; i++)
                for (int j = i; j > off && x[idx[j - 1]] > x[idx[j]]; j--)
                    swap(idx, j, j - 1);
            return;
        }

        // Choose a partition element, v
        int m = off + (len >> 1);       // Small arrays, middle element
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {        // Big arrays, pseudomedian of 9
                int s = len / 8;
                l = med3(idx, x, l, l + s, l + 2 * s);
                m = med3(idx, x, m - s, m, m + s);
                n = med3(idx, x, n - 2 * s, n - s, n);
            }
            m = med3(idx, x, l, m, n); // Mid-size, med of 3
        }
        long v = x[idx[m]];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && x[idx[b]] <= v) {
                if (x[idx[b]] == v)
                    swap(idx, a++, b);
                b++;
            }
            while (c >= b && x[idx[c]] >= v) {
                if (x[idx[c]] == v)
                    swap(idx, c, d--);
                c--;
            }
            if (b > c)
                break;
            swap(idx, b++, c--);
        }

        // Swap partition elements back to middle
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(idx, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(idx, b, n - s, s);

        // Recursively sort non-partition-elements
        if ((s = b - a) > 1)
            sort1(idx, x, off, s);
        if ((s = d - c) > 1)
            sort1(idx, x, n - s, s);
    }

    private static void swap(int x[], int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(int x[], int a, int b, int n) {
        for (int i = 0; i < n; i++, a++, b++)
            swap(x, a, b);
    }

    /**
     * Returns the index of the median of the three indexed longs.
     */
    private static int med3(int[] idx, long x[], int a, int b, int c) {
        return (x[idx[a]] < x[idx[b]] ?
                (x[idx[b]] < x[idx[c]] ? b : x[idx[a]] < x[idx[c]] ? c : a) :
                (x[idx[b]] > x[idx[c]] ? b : x[idx[a]] > x[idx[c]] ? c : a));
    }

    /**
     * Sorts the specified sub-array of integers into ascending order.
     */
    private static void sort1(int idx[], int x[], int off, int len) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (int i = off; i < len + off; i++)
                for (int j = i; j > off && x[idx[j - 1]] > x[idx[j]]; j--)
                    swap(idx, j, j - 1);
            return;
        }

        // Choose a partition element, v
        int m = off + (len >> 1);       // Small arrays, middle element
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {        // Big arrays, pseudomedian of 9
                int s = len / 8;
                l = med3(idx, x, l, l + s, l + 2 * s);
                m = med3(idx, x, m - s, m, m + s);
                n = med3(idx, x, n - 2 * s, n - s, n);
            }
            m = med3(idx, x, l, m, n); // Mid-size, med of 3
        }
        int v = x[idx[m]];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && x[idx[b]] <= v) {
                if (x[idx[b]] == v)
                    swap(idx, a++, b);
                b++;
            }
            while (c >= b && x[idx[c]] >= v) {
                if (x[idx[c]] == v)
                    swap(idx, c, d--);
                c--;
            }
            if (b > c)
                break;
            swap(idx, b++, c--);
        }

        // Swap partition elements back to middle
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(idx, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(idx, b, n - s, s);

        // Recursively sort non-partition-elements
        if ((s = b - a) > 1)
            sort1(idx, x, off, s);
        if ((s = d - c) > 1)
            sort1(idx, x, n - s, s);
    }

    /**
     * Returns the index of the median of the three indexed integers.
     */
    private static int med3(int[] idx, int x[], int a, int b, int c) {
        return (x[idx[a]] < x[idx[b]] ?
                (x[idx[b]] < x[idx[c]] ? b : x[idx[a]] < x[idx[c]] ? c : a) :
                (x[idx[b]] > x[idx[c]] ? b : x[idx[a]] > x[idx[c]] ? c : a));
    }

    /**
     * Searches the index of "key" inside the given array "la", whose ascending sorted order is
     * maintained in the given index array "idx".
     *
     * <p>
     * Implementation adapted from {@link java.util.Arrays#binarySearch(long[], long)}.
     *
     * @param la a value array
     * @param key a key to search
     * @param idx the ascending sorted index array of "la"
     * @return the position of "key" in "la" if "key" is found in "la", otherwise -(the suggested
     *         insertion position +1). The return value would be greater equal 0 if found. If the
     *         return value is less than 0, that means "key" cannot be found in "la".
     */
    public static int binarySearch(long[] la, long key, int[] idx) {
        return binarySearch0(la, 0, la.length, key, idx);
    }

    private static int binarySearch0(long[] a, int fromIndex, int toIndex,
            long key, int[] idx) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = a[idx[mid]];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    public static int binarySearch(int[] ia, int key, int[] idx) {
        return binarySearch0(ia, 0, ia.length, key, idx);
    }

    private static int binarySearch0(int[] a, int fromIndex, int toIndex,
            int key, int[] idx) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = a[idx[mid]];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    public static boolean contains(int[] values, int value) {
        for (int i : values) {
            if (i == value) {
                return true;
            }
        }
        return false;
    }
}
