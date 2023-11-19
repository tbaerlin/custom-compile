package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.regexp.shared.RegExp;

/**
 * This was a copy of de.marketmaker.istar.domain.util.SymbolUtil but changes slightly over time.
 * @author Ulrich Maurer
 *         Date: 28.03.11
 */
public class SymbolUtil {
    public static final int ISIN_LENGTH = 12;

    public static final int WKN_LENGTH = 6;

    //changed pattern cf.: MMWEB-761/R-82377
    private static final String WKN_REGEX_PATTERN = "[A-Z0-9]{6}|[a-z0-9]{6}";  // $NON-NLS$

    public static boolean isWkn(final String wkn) {
        return !(wkn == null || wkn.length() != WKN_LENGTH) && RegExp.compile(WKN_REGEX_PATTERN).test(wkn);
    }

    /**
     * Checks whether the given isin is a valid ISIN, this includes checking the check-sum.
     * @param isin to be tested
     * @return true iff isin is an ISIN
     */
    public static boolean isIsin(String isin) {
        if (isin == null || isin.length() != ISIN_LENGTH) {
            return false;
        }

        final char lastChar = isin.charAt(ISIN_LENGTH - 1);
        if (!isValidDigit(lastChar)) {
            return false;
        }

        final StringBuilder sb = new StringBuilder(11);
        for (int i = 0, n = ISIN_LENGTH - 1; i < n; i++) {
            final char c = Character.toUpperCase(isin.charAt(i));
            if (!isValidCharacter(c) && (i > 1 && !isValidDigit(c))) {
                return false;
            }
            sb.append(c);
        }

        return lastChar == ('0' + getChecksum(sb.toString()));
    }

    /**
     * Returns the isin checksum between 0 and 9 for the given isin without checksum.
     * @return checksum
     */
    private static int getChecksum(String isinWithoutChecksum) {
        final StringBuilder sb = new StringBuilder(15);

        for (int i = 0; i < isinWithoutChecksum.length(); i++) {
            final char c = isinWithoutChecksum.charAt(i);
            if (isValidDigit(c)) {
                sb.append(c);
            }
            else {
                sb.append((c - 55) / 10);
                sb.append((c - 55) % 10);
            }
        }

        int qs = 0; // Summe der Quersummen der Produkte von weight und Zahlwert an Position i
        int weight = 2; // alternates between 2 and 1
        for (int i = sb.length() - 1; i >= 0; i--) {
            final int j = (sb.charAt(i) - '0') * weight; // j <= 0 < 19
            qs += (j < 10) ? j : (1 + (j % 10));
            weight = 3 - weight;
        }

        final int preresult = qs % 10;
        return (preresult == 0) ? 0 : (10 - preresult);
    }

    private static boolean isValidCharacter(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private static boolean isValidDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private SymbolUtil() {
    }
}
