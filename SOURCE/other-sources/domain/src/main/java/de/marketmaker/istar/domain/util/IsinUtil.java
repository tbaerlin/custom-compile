/*
 * IsinUtil.java
 *
 * Created on 07.10.2005 13:09:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.util;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IsinUtil {
    public static final int ISIN_LENGTH = 12;

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

        final StringBuilder sb = new StringBuilder(ISIN_LENGTH * 2 - 2);
        for (int i = 0; i < ISIN_LENGTH - 1; i++) {
            final int charValue = Character.getNumericValue(isin.charAt(i));
            if (charValue < 0 || charValue > 35) {
                return false;
            }
            sb.append(charValue);
        }

        return lastChar == ('0' + computeChecksum(sb));
    }

    private static int computeChecksum(StringBuilder sb) {
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

    private static boolean isValidDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private IsinUtil() {
    }
}
