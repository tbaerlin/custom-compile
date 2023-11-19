/*
 * XmlUtil.java
 *
 * Created on 13.05.2005 09:25:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import com.sun.org.apache.xml.internal.utils.XMLChar;

/**
 * Provides static helper methods to deal with xml
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class XmlUtil {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String CDATA_START = "<![CDATA[";

    private static final String CDATA_END = "]]>";

    public static class Invoker {
        private Invoker() {
        }

        public String encode(String str) {
            return XmlUtil.encode(str);
        }
    }

    public static final XmlUtil.Invoker ENCODER = new XmlUtil.Invoker();

    private XmlUtil() {
    }

    public static String encode(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder sb = null;

        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);

            String s;

            switch (c) {
                case '\n':
                    s = LINE_SEPARATOR;
                    break;
                case '&':
                    s = "&amp;";
                    break;
                case '<':
                    s = "&lt;";
                    break;
                case '>':
                    s = "&gt;";
                    break;
                case '"':
                    s = "&quot;";
                    break;
                default:
                    if (!XMLChar.isValid(c)) {
                        s = "";
                        break;
                    }
                    if (sb != null) {
                        sb.append(c);
                    }
                    continue;
            }

            if (sb == null) {
                sb = new StringBuilder(Math.max(16, str.length() + 4));
                if (i > 0) {
                    sb.append(str.substring(0, i));
                }
            }
            sb.append(s);
        }

        return (sb != null) ? sb.toString() : str;
    }

    public static String encodeCData(String str) {
        if (str == null) {
            return "";
        }

        final StringBuilder sb =
                new StringBuilder(CDATA_START.length() + str.length() + CDATA_END.length());
        sb.append(CDATA_START);

        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            if (XMLChar.isValid(c)) {
                if (c == '>' && i > 1 && str.charAt(i - 2) == ']' && str.charAt(i - 1) == ']') {
                    // ]]> must not appear in the CDATA section. The recommended way to deal
                    // with that is to close the first CDATA after the ]] and put
                    // the < at the start of a new CDATA section:
                    sb.append(CDATA_END).append(CDATA_START);
                }
                sb.append(c);
            }
        }

        sb.append(CDATA_END);

        return sb.toString();
    }


}