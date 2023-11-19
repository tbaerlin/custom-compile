/*
 * NewsUtil.java
 *
 * Created on 14.01.2009 10:14:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.Arrays;
import java.util.HashSet;

import de.marketmaker.itools.gwtutil.client.util.date.GwtDateParser;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.NWSNews;
import de.marketmaker.iview.dmxml.NWSSearchElement;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsUtil {
    private static final HashSet<String> AGENCY_PREFIXES = new HashSet<>(Arrays.asList(
            "BUSINESS WIRE", "ddp direct", "DGAP-Adhoc", "DGAP-News", "DGAP-PVR", "DJ", "Original-Research" // $NON-NLS$
    ));

    public static String headlineWithoutAgency(NWSSearchElement element) {
        return headlineWithoutAgency(element.getHeadline(), element.getSource());
    }

    public static String headlineWithoutAgency(NWSNews news) {
        return headlineWithoutAgency(news.getHeadline(), news.getSource());
    }

    public static String headlineWithoutAgency(String hl, String source) {
        if (hl == null) {
            return null;
        }
        final int p = hl.indexOf(": "); // $NON-NLS-0$
        if (p <= 0) {
            return hl;
        }
        final String prefix = hl.substring(0, p);
        return (source.equals(prefix) || AGENCY_PREFIXES.contains(prefix)) ? hl.substring(p + 2) : hl;
    }

    public static String headlineWithDate(NWSSearchElement element) {
        StringBuilder result = new StringBuilder();
        final MmJsDate newsDate = GwtDateParser.getMmJsDate(element.getDate());
        if (newsDate.getMidnight().getDate() == new MmJsDate().getMidnight().getDate()) {
            result.append(JsDateFormatter.formatHhmm(newsDate));
        }
        else {
            result.append(JsDateFormatter.formatDdmm(newsDate));
        }
        result.append(" ").append(headlineWithoutAgency(element)); // $NON-NLS-0$
        return result.toString();
    }

    public static String getTextAsHTML(String text) {
        final int pos = getCharPosToEncode(text, 0);
        if (pos != -1) {
            return getTextAsHTML(text, pos);
        }
        return text;
    }

    private static int getCharPosToEncode(String text, int n) {
        for (int i = n; i < text.length(); i++) {
            if (text.charAt(i) == '<' || text.charAt(i) > 255) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Turns text into something that can be used as content for an HTML element
     *
     * @param text to be converted
     * @param n    position of first char to be encoded
     * @return html compatible version of text
     */
    private static String getTextAsHTML(String text, int n) {
        final StringBuilder sb = new StringBuilder();

        int from = 0;
        int pos = n;
        do {
            sb.append(text.substring(from, pos));
            // IE requires html escape sequences for chars with value > 255
            if (text.charAt(pos) > 255) {
                sb.append("&#x").append(Integer.toHexString(text.charAt(pos))).append(";"); // $NON-NLS$
                from = pos + 1;
            }
            else { // '<'
                int to = -1;
                if (text.startsWith("<img ", pos)) { // $NON-NLS-0$
                    to = text.indexOf(">", pos) + 1; // $NON-NLS-0$
                    if (to != -1) {
                        sb.append("<br>").append(text.substring(pos, to)).append("<br>"); // $NON-NLS$
                    }
                }
                else if (text.startsWith("<a ", pos)) { // $NON-NLS-0$
                    to = text.indexOf("</a>", pos) + 4; // $NON-NLS-0$
                    if (to != -1) {
                        sb.append("<a target=\"_blank\"").append(text.substring(pos + 2, to)); // $NON-NLS$
                    }
                }
                else if (text.startsWith("<p>", pos) || text.startsWith("<P>", pos)) { // $NON-NLS$
                    sb.append("<p>"); // $NON-NLS-0$
                    to = pos + 3;
                }
                else if (text.startsWith("</p>", pos) || text.startsWith("</P>", pos)) { // $NON-NLS$
                    sb.append("</p>"); // $NON-NLS-0$
                    to = pos + 4;
                }
                else if (text.startsWith("<br>", pos)) { // $NON-NLS-0$
                    sb.append("<br>"); // $NON-NLS-0$
                    to = pos + 4;
                }

                if (to == -1) {
                    // turn every '<' that does not start a tag we care about into &lt;
                    sb.append("&lt;"); // $NON-NLS-0$
                    from = pos + 1;
                }
                else {
                    from = to;
                }
            }
            pos = getCharPosToEncode(text, from);
        } while (pos != -1);

        if (from < text.length()) {
            sb.append(text.substring(from));
        }

        return sb.toString();
    }

}
