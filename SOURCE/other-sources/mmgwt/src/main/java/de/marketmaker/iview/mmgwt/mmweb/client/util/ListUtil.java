/*
 * ListUtil.java
 *
 * Created on 29.08.2008 14:27:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Ulrich Maurer
 */
public class ListUtil {
    /**
     * Split a given list in <code>nrOfSublists</code> sublists.
     *
     * @param list The List that should be splitted.
     * @param nrOfSublists The number of sublists.
     * @return A List, containing <code>nrOfSublists</code> sublists.
     */
    public static <T> List<List<T>> splitByNumber(List<T> list, int nrOfSublists) {
        final List<List<T>> lists = new ArrayList<List<T>>(nrOfSublists);
        final int listSize = list.size();
        final int subListSize = listSize / nrOfSublists;
        final int oneMore = listSize % nrOfSublists;
        int startIndex = 0;
        for (int i = 0; i < nrOfSublists; i++) {
            int endIndex = startIndex + subListSize;
            if (i < oneMore) {
                endIndex++;
            }
            if (endIndex >= listSize) {
                endIndex = listSize;
            }
            if (startIndex == endIndex) {
                List<T> emptyList = Collections.emptyList();
                lists.add(emptyList);
            }
            else {
                lists.add(subList(list, startIndex, endIndex));
            }
            startIndex = endIndex;
        }
        return lists;
    }


    public static <T> List<T> subList(List<T> list, int fromIndex, int toIndex) {
        assert fromIndex < toIndex;
        assert fromIndex >= 0;
        assert toIndex <= list.size();
        final List<T> listResult = new ArrayList<T>(toIndex - fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            listResult.add(list.get(i));
        }
        return listResult;
    }

    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public static <E> List<E> add(List<E> list, E e) {
        if (list == null) {
            list = new ArrayList<E>();
        }
        list.add(e);
        return list;
    }

}
