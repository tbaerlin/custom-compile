package de.marketmaker.iview.mmgwt.mmweb.client.myspace;

import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;

import java.util.Comparator;

/**
 * Author: umaurer
 * Created: 03.11.15
 */
public class SnippetConfigPositionComparator implements Comparator<SnippetConfiguration> {
    public static final SnippetConfigPositionComparator INSTANCE = new SnippetConfigPositionComparator();

    @Override
    public int compare(SnippetConfiguration sc1, SnippetConfiguration sc2) {
        int result = compareUnexact(sc1, sc2, "y", 32); // $NON-NLS$
        if (result != 0) { return result; }

        result = compareUnexact(sc1, sc2, "x", 32); // $NON-NLS$
        if (result != 0) { return result; }

        result = compareUnexact(sc1, sc2, "h", 256); // $NON-NLS$
        if (result != 0) { return result; }

        result = compareUnexact(sc1, sc2, "w", 256); // $NON-NLS$
        if (result != 0) { return result; }

        result = compareExact(sc1, sc2, "y"); // $NON-NLS$
        if (result != 0) { return result; }

        result = compareExact(sc1, sc2, "x"); // $NON-NLS$
        if (result != 0) { return result; }

        result = compareExact(sc1, sc2, "h"); // $NON-NLS$
        if (result != 0) { return result; }

        result = compareExact(sc1, sc2, "w"); // $NON-NLS$
        if (result != 0) { return result; }

        return 0;
    }

    private int compareExact(SnippetConfiguration sc1, SnippetConfiguration sc2, String paramKey) {
        int v1 = sc1.getInt(paramKey, 1000);
        int v2 = sc2.getInt(paramKey, 1000);
        return v1 - v2;
    }

    private int compareUnexact(SnippetConfiguration sc1, SnippetConfiguration sc2, String paramKey, int div) {
        int v1 = sc1.getInt(paramKey, 1000);
        int v2 = sc2.getInt(paramKey, 1000);
        return compareUnexact(v1, v2, div);
    }

    private int compareUnexact(int v1, int v2, int div) {
        int u1 = v1 / div;
        int u2 = v2 / div;
        return u1 - u2;
    }
}
