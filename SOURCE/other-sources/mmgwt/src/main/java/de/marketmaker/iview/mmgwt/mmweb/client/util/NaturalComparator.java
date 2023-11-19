package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.constants.NumberConstants;

import java.util.Comparator;

/**
 * @author Ulrich Maurer
 *         Date: 19.01.12
 */
public class NaturalComparator<T> implements Comparator<T> {
    private final char decimalSeparator;
    private final char groupingSeparator;
    private final String numberRegEx;
    private final boolean ascending;

    enum ChunkType {
        NUMBER, DATE, STRING
    }

    public static <T> NaturalComparator<T> createDefault() {
        final NumberConstants defaultNumberConstants = LocaleInfo.getCurrentLocale().getNumberConstants();
        return new NaturalComparator<>(defaultNumberConstants.groupingSeparator().charAt(0), defaultNumberConstants.decimalSeparator().charAt(0), true);
    }

    public static <T> NaturalComparator<T> createDescending() {
        final NumberConstants defaultNumberConstants = LocaleInfo.getCurrentLocale().getNumberConstants();
        return new NaturalComparator<>(defaultNumberConstants.groupingSeparator().charAt(0), defaultNumberConstants.decimalSeparator().charAt(0), false);
    }

    public NaturalComparator(char groupingSeparator, char decimalSeparator, boolean ascending) {
        this.groupingSeparator = groupingSeparator;
        this.decimalSeparator = decimalSeparator;
        this.ascending = ascending;
        final String gs = groupingSeparator == '.' ? ("\\.") : String.valueOf(groupingSeparator);
        final String ds = decimalSeparator == '.' ? ("\\.") : String.valueOf(decimalSeparator);
        this.numberRegEx = "([+-]?\\d{1,3}(" + gs + "\\d{3})*(" + ds + "\\d*)?)|([+-]?\\d{1,3}(\\d{3})*(" + ds + "\\d*)?)"; // $NON-NLS$^
    }

    public int compare(T t1, T t2) {
        return compare(t1 == null ? null : t1.toString(), t2 == null ? null : t2.toString(), false);
    }

    public int compareIgnoreCase(T t1, T t2) {
        return compare(t1 == null ? null : t1.toString(), t2 == null ? null : t2.toString(), true);
    }

    private int compare(String s1, String s2, boolean lowerCase) {
        if (s1 == null) {
            return s2 == null ? 0 : 1;
        }
        else if (s2 == null) {
            return -1;
        }
        final int result = compareChunks(getChunks(s1), getChunks(s2), lowerCase);
        return this.ascending ? result : -result;
    }

    private String[] getChunks(String s) {
        final String[] chunks = new String[getChunksCount(s)];
        int startPos = 0;
        int endPos = 0;
        int count = 0;
        boolean numbers = false;
        while (endPos < s.length()) {
            final boolean number = isNumber(s.charAt(endPos));
            if (endPos == 0) {
                numbers = number;
            }
            else if (number != numbers) {
                chunks[count] = s.substring(startPos, endPos);
                count++;
                startPos = endPos;
                numbers = number;
            }
            endPos++;
        }
        if (startPos != endPos) {
            chunks[count] = s.substring(startPos, endPos);
        }

        return chunks;
    }

    private int getChunksCount(String s) {
        int pos = 0;
        boolean numbers = false;
        int count = 0;
        while (pos < s.length()) {
            final boolean number = isNumber(s.charAt(pos));
            if (pos == 0 || number != numbers) {
                count++;
                numbers = number;
            }
            pos++;
        }
        return count;
    }


    private boolean isNumber(char c) {
        return c >= '0' && c <= '9' || c == groupingSeparator || c == decimalSeparator || c == '+' || c == '-';
    }

    private ChunkType getType(String chunk) {
        if (isNumber(chunk)) {
            return ChunkType.NUMBER;
        }
        else if (isDate(chunk)) {
            return ChunkType.DATE;
        }
        else {
            return ChunkType.STRING;
        }
    }

    private boolean isNumber(String s) {
        return s.matches(this.numberRegEx);
/*
        return s.matches("[+-]?\\d{1,3}(\\" + groupingSeparator + "\\d{3})*(\\" + decimalSeparator + "\\d*)?")
                || s.matches("[+-]?\\d{1,3}(\\d{3})*(\\" + decimalSeparator + "\\d*)?"); // $NON-NLS$
*/
    }

    private boolean isDate(String s) {
        return s.matches("\\d{1,2}\\.\\d{1,2}\\.\\d{4}"); // $NON-NLS$
    }

    private int compareChunks(String[] chunks1, String[] chunks2, boolean lowerCase) {
        for (int i = 0, length = chunks1.length; i < length; i++) {
            if (chunks2.length <= i) {
                return 1;
            }
            int result = compareChunk(chunks1[i], chunks2[i], lowerCase);
            if (result != 0) {
                return result;
            }
        }
        return chunks1.length < chunks2.length ? -1 : 0;
    }

    private int compareChunk(String chunk1, String chunk2, boolean lowerCase) {
        final ChunkType ct1 = getType(chunk1);
        final ChunkType ct2 = getType(chunk2);

        if (ct1 != ct2) {
            return ct1.compareTo(ct2);
        }

        switch (ct1) {
            case NUMBER:
                return compareNumbers(chunk1, chunk2);
            case DATE:
                return compareDates(chunk1, chunk2);
            default:
                return compareStrings(chunk1, chunk2, lowerCase);
        }
    }

    private int compareStrings(String s1, String s2, boolean lowerCase) {
        char v1[] = s1.toCharArray();
        char v2[] = s2.toCharArray();
        int len1 = v1.length;
        int len2 = v2.length;
        int lim = Math.min(len1, len2);

        int k = 0;
        while (k < lim) {
            char c1 = v1[k];
            char c2 = v2[k];
            if (c1 != c2) {
                c1 = removeUmlaut(c1, lowerCase);
                c2 = removeUmlaut(c2, lowerCase);
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
            k++;
        }
        return len1 - len2;
    }
    
    private char removeUmlaut(char c, boolean lowerCase) {
        switch (c) {
            case 'ä':
                return 'a';
            case 'ö':
                return 'o';
            case 'ü':
                return 'ü';
            case 'ß':
                return 's';
            case 'Ä':
                return lowerCase ? 'a' : 'A';
            case 'Ö':
                return lowerCase ? 'o' : 'O';
            case 'Ü':
                return lowerCase ? 'u' : 'U';
        }
        return lowerCase ? Character.toLowerCase(c) : c;
    }

    private int compareNumbers(String number1, String number2) {
        final double d1 = parseDouble(number1);
        final double d2 = parseDouble(number2);

        double result = d1 - d2;
        return result > 0D ? 1 : (result == 0D ? 0 : -1);
    }

    private double parseDouble(String number) {
        final char[] chars = number.toCharArray();
        int len = 0;
        for (char c : chars) {
            if (c != groupingSeparator) {
                chars[len] = c == decimalSeparator ? '.' : c;
                len++;
            }
        }
        return Double.parseDouble(new String(chars, 0, len));
    }

    private int compareDates(String d1, String d2) {
        final String[] dd1 = d1.split("\\.");
        final String[] dd2 = d2.split("\\.");
        assert (dd1.length == 3);
        assert (dd2.length == 3);
        for (int i = 2; i >= 0; i--) {
            int result = compareNumbers(dd1[i], dd2[i]);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }
}
