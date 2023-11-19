/*
 * StringUtil.java
 *
 * Created on 02.04.2008 17:50:33
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StringUtil {
    public static final char TOKEN_DIVIDER = '/';
    public static final char TOKEN_NAME_VALUE_PAIR_DIVIDER = '=';

    public static ArrayList<String> split(String s, char c) {
        final ArrayList<String> result = new ArrayList<>();
        if (s == null) {
            return result;
        }
        StringBuilder sb = new StringBuilder(100);
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\\') {
                    sb.setCharAt(sb.length() - 1, c);                    
                }
                else {
                    result.add(sb.toString());
                    sb.setLength(0);
                }
            }
            else {
                sb.append(s.charAt(i));
            }
        }
        if (sb.length() > 0) {
            result.add(sb.toString());
        }
        return result;
    }

    public static String[] splitToken(String hkey) {
        final ArrayList<String> list = split(hkey, TOKEN_DIVIDER);
        return list.toArray(new String[list.size()]);
    }


    public static String join(char divider, String... token) {
        return join(divider, Arrays.asList(token));
    }

    public static String join(char divider, List<String> token) {
        return join(String.valueOf(divider), token);
    }

    public static String join(String divider, Collection<String> token) {
        final StringBuilder sb = new StringBuilder();
        for (String t : token) {
            appendToken(sb, divider, t);
        }
        return sb.toString();
    }

    public static void appendToken(StringBuilder sb, String token) {
        appendToken(sb, String.valueOf(TOKEN_DIVIDER), token);
    }

    public static void appendToken(StringBuilder sb, String divider, String token) {
        if (sb.length() > 0) {
            sb.append(divider);
        }
        sb.append(token.replace(divider, "\\" + divider));
    }


    public static String joinTokens(String... token) {
        return join(TOKEN_DIVIDER, token);
    }

    public static String joinToken(String key, String value) {
        return join(TOKEN_NAME_VALUE_PAIR_DIVIDER, key, value);
    }

    public static String[] concat(String[] values, String value) {
        if (value == null) {
            return values;
        }
        return concat(values, new String[]{value});
    }

    public static String[] concat(String[] first, String[] second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        final String[] result = new String[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


    public static String replace(String text, String findText, String replaceBy) {
        return text.replace(findText, replaceBy);
    }

    public static boolean hasText(String s) {
        return s != null && s.trim().length() > 0;
    }

    public static boolean hasText(SafeHtml safeHtml) {
        return safeHtml != null && safeHtml.asString().length() > 0;
    }

    public static boolean hasText(String... s) {
        for (String value : s) {
            if (hasText(value)) {
                return true;
            }
        }
        return false;
    }

    public static String sOrDash(String... s) {
        if (s.length == 1) {
            return s[0] == null ? "--" : s[0];
        }
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String a : s) {
            if (a == null) {
                continue;
            }
            if (first) {
                first = false;
            }
            else {
                sb.append(' ');
            }
            sb.append(a);
        }
        if (first) {
            return "--";
        }
        return sb.toString();
    }

    public static boolean equals(String s, String t) {
        return (s == null && t == null) || (s != null && s.equals(t));
    }


    public static void reduceCurrencyNameLength(InstrumentData id) {
        id.setName(getReducedCurrencyName(id));
    }

    public static String getReducedCurrencyName(InstrumentData instrumentData) {
        final String name = instrumentData.getName();
        if (InstrumentTypeEnum.valueOf(instrumentData.getType()) == InstrumentTypeEnum.CUR) {
            if (name.matches("[A-Z]{3} .* \\([A-Z]{3}\\)")) { // $NON-NLS$
                return name.replaceAll("^([A-Z]{3}) .* \\(([A-Z]{3})\\)$", "$1 / $2"); // $NON-NLS$
            }
            else if (name.matches("[A-Z]{3}/[A-Z]{3},? .*")) { // $NON-NLS$
                return name.substring(0, 7);
            }
        }
        return name;
    }

    public static void appendAttribute(StringBuffer sb, String name, String value) {
        sb.append(" ").append(name).append("=\"").append(value).append("\""); // $NON-NLS$
    }

    public static String wrapLongLine(String s, int lineLength, String divider) {
        if (s.length() <= lineLength) {
            return s;
        }
        final int i = findSpace(s, lineLength);
        if (i != -1) {
            return s.substring(0, i) + divider + wrapLongLine(s.substring(i + 1), lineLength, divider);
        }
        return s;
    }

    private static int findSpace(String s, int from) {
        for (int i = from; i > 0; i--) {
            if (s.charAt(i) == ' ') {
                return i;
            }
        }
        return s.indexOf(' ', from);
    }

    public static String htmlBold(String s) {
        return "<b>" + s + "</b>"; // $NON-NLS$
    }

    public static SafeHtml asHeader(SafeHtml safeHtml1, SafeHtml safeHtml2) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.append(safeHtml1);
        appendSeparator(sb);
        sb.append(safeHtml2);
        return sb.toSafeHtml();
    }

    public static SafeHtml asHeader(SafeHtml safeHtml, String text) {
        if(text == null) {
            return safeHtml;
        }
        final SafeHtmlBuilder sb = new SafeHtmlBuilder().append(safeHtml);
        appendSeparator(sb);
        return sb.appendEscaped(text).toSafeHtml();
    }

    public static SafeHtml asHeader(String... texts) {
        if (texts == null) {
            return null;
        }
        if (texts.length == 0) {
            return null;
        }
        if (texts.length == 1 && texts[0] == null) {
            return null;
        }
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        boolean first = true;
        for (String text : texts) {
            if (first) {
                first = false;
            }
            else {
                appendSeparator(sb);
            }
            sb.appendEscaped(text);
        }
        return sb.toSafeHtml();
    }

    public static int appendHeader(SafeHtmlBuilder sb, String text, int count) {
        if (count > 0) {
            appendSeparator(sb);
        }
        sb.appendEscaped(text);
        return count + 1;
    }

    private static void appendSeparator(SafeHtmlBuilder sb) {
        if (SessionData.isAsDesign()) {
            if (PhoneGapUtil.isPhoneGap()) {
                sb.appendHtmlConstant("<span class=\"header-separator\">&#10093;</span>");
            } else {
                sb.appendHtmlConstant("<span class=\"header-separator\">&#9002;</span>");
            }
        }
        else {
            sb.appendHtmlConstant(" <span class=\"header-separator\">::</span> ");
        }
    }

    public static boolean startsWith(String stringValue, char c) {
        return !stringValue.isEmpty() && stringValue.charAt(0) == c;
    }

    public static SafeHtml getNoSelectionText(String text, final String style) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<span class=\"" + style + "\">").appendEscaped(text).appendHtmlConstant("</span>");
        return sb.toSafeHtml();
    }

    public static String format(String inStr, String... values){
        for (String value:values) {
            if (value == null) {
                value = ""; // $NON-NLS$
            }
            inStr = inStr.replaceFirst("%s", value); // $NON-NLS$
        }
        return inStr;
    }
}
