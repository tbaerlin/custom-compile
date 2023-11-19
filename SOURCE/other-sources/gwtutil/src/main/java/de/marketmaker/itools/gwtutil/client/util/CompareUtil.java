package de.marketmaker.itools.gwtutil.client.util;

/**
 * @author Ulrich Maurer
 *         Date: 11.04.11
 */
public class CompareUtil {
    public static boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    public static int compare(String s1, String s2) {
        return s1 == null
                ? (s2 == null ? 0 : 1 )
                : (s2 == null ? -1 : s1.compareTo(s2));
    }
}
