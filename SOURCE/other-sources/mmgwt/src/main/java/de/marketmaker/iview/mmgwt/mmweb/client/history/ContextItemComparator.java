package de.marketmaker.iview.mmgwt.mmweb.client.history;

import de.marketmaker.itools.gwtutil.client.util.CompareUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Author: umaurer
 * Created: 11.11.14
 */
public class ContextItemComparator implements Comparator<ContextItem> {
    public static <T extends ContextItem> List<T> sort(List<T> list) {
        final ArrayList<T> sortedList = new ArrayList<T>(list);
        Collections.sort(sortedList, new ContextItemComparator());
        return sortedList;
    }

    @Override
    public int compare(ContextItem o1, ContextItem o2) {
        final String s1 = o1 == null ? null : o1.getName().toLowerCase();
        final String s2 = o2 == null ? null : o2.getName().toLowerCase();

        return CompareUtil.compare(s1, s2);
    }
}
