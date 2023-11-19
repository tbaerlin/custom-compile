/*
 * LargeNumberRenderer.java
 *
 * Created on 05.06.2008 17:04:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.StringBasedNumberFormat.ROUND_0;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.StringBasedNumberFormat.ROUND_0_1;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.StringBasedNumberFormat.ROUND_0_2;

/**
 * Renders numbers < 10 with 2 decimal digits, those < 100 with 1, and those >= 100
 * are rendered without decimal digits.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LargeNumberRenderer implements Renderer<String> {
    private final String defaultValue;

    private final LargeNumberLabels labels;

    public LargeNumberRenderer() {
        this("--", Renderer.LARGE_NUMBER_LABELS); // $NON-NLS-0$
    }

    public LargeNumberRenderer(String defaultValue, LargeNumberLabels labels) {
        this.labels = labels;
        this.defaultValue = defaultValue;
    }

    public String renderLong(Long l) {
        if (l == null) {
            return this.defaultValue;
        }
        return render(l.toString());
    }

    public String render(String s) {
        if (s == null) {
            return this.defaultValue;
        }
        if ("0".equals(s)) { // $NON-NLS-0$
            return "0"; // $NON-NLS-0$
        }

        final int dotPos = s.indexOf('.');
        final int neg = s.charAt(0) == '-' ? 1: 0;

        final int lg10 = (dotPos == -1) ? (s.length() - neg) : (dotPos - neg);

        final int shiftRight;
        final String suffix;
        final StringBasedNumberFormat formatter;
        final String nbsp = "&nbsp;"; // $NON-NLS$

        if (lg10 > 9) {
            if (lg10 > 12) {
                if (lg10 > 15) {
                    shiftRight = -15;
                    formatter = lg10 > 17 ? ROUND_0 : (lg10 > 16 ? ROUND_0_1 : ROUND_0_2);
                    suffix = nbsp + labels.quadrillionAbbr();
                }
                else {
                    shiftRight = -12;
                    formatter = lg10 > 14 ? ROUND_0 : (lg10 > 13 ? ROUND_0_1 : ROUND_0_2);
                    suffix = nbsp + labels.trillionAbbr();
                }
            }
            else {
                shiftRight = -9;
                formatter = lg10 > 11 ? ROUND_0 : (lg10 > 10 ? ROUND_0_1 : ROUND_0_2);
                suffix = nbsp + labels.billionAbbr();
            }
        }
        else {
            if (lg10 > 6) {
                shiftRight = -6;
                formatter = lg10 > 8 ? ROUND_0 : (lg10 > 7 ? ROUND_0_1 : ROUND_0_2);
                suffix = nbsp + labels.millionAbbr();
            }
            else {
                shiftRight = 0;
                formatter = lg10 > 3 ? ROUND_0 : (lg10 > 2 ? ROUND_0_1 : ROUND_0_2);
                suffix = ""; // $NON-NLS-0$
            }
        }

        return formatter.format(s, shiftRight, null) + suffix;
    }

    /**
     * Holds the values for the i18n labels.
     * This is only necessary so that we do not need to directly include GWT's i18n,
     * which forces us to use GWT test cases that require a running Jetty Web Container during builds...
     *
     * @author Markus Dick
     */
    public static class LargeNumberLabels {
        private final String millionAbbr;
        private final String billionAbbr;
        private final String trillionAbbr;
        private final String quadrillionAbbr;

        LargeNumberLabels(String mAbbr, String bnAbbr, String trAbbr, String quadAbbr) {
            millionAbbr = mAbbr;
            billionAbbr = bnAbbr;
            trillionAbbr = trAbbr;
            quadrillionAbbr = quadAbbr;
        }


        public String millionAbbr() {
            return millionAbbr;
        }

        public String billionAbbr() {
            return billionAbbr;
        }

        public String trillionAbbr() {
            return trillionAbbr;
        }

        public String quadrillionAbbr() {
            return quadrillionAbbr;
        }
    }
}
