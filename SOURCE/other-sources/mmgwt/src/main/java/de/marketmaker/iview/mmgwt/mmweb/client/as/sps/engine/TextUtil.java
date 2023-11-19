package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.Dimension;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * Author: umaurer
 * Created: 23.01.14
 */
@NonNLS
public class TextUtil {
    public static final String STYLE_NO_SELECTION = "sps-no-selection"; // $NON-NLS$

    public static final SafeHtml NO_SELECTION_TEXT = StringUtil.getNoSelectionText(I18n.I.noSelectionListEntry(), STYLE_NO_SELECTION);
    public static final SafeHtml NO_SELECTION_VALUE = StringUtil.getNoSelectionText("--", STYLE_NO_SELECTION);

    public static SafeHtml toSafeHtmlOrNull(String text) {
        return text == null ? null : processLineBreaks(text, true);
    }

    public static SafeHtml toSafeHtml(String text) {
        return processLineBreaks(text, true);
    }

    public static SafeHtml toSafeHtmlNoLineBreak(String text) {
        return processLineBreaks(text, false);
    }

    private static SafeHtml processLineBreaks(String text, boolean replaceLineBreakWithBrTag) {
        if ("&nbsp;".equals(text)) {
            return SafeHtmlUtils.fromTrustedString("&nbsp;");
        }
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        boolean lastIsBackslash = false;
        for (char c : text.toCharArray()) {
            switch (c) {
                case '\n':
                    if (lastIsBackslash) {
                        sb.appendEscaped("\\");
                    }
                    lastIsBackslash = false;
                    if(replaceLineBreakWithBrTag) {
                        sb.appendHtmlConstant("<br/>");
                    }
                    else {
                        sb.append(' ');
                    }
                    break;
                case '\\':
                    if (lastIsBackslash) {
                        sb.appendEscaped("\\");
                        lastIsBackslash = false;
                    } else {
                        lastIsBackslash = true;
                    }
                    break;
                case 'n':
                    if (lastIsBackslash) {
                        if(replaceLineBreakWithBrTag) {
                            sb.appendHtmlConstant("<br/>");
                        }
                        else {
                            sb.append(' ');
                        }
                    } else {
                        sb.appendEscaped(String.valueOf(c));
                    }
                    lastIsBackslash = false;
                    break;
                default:
                    if (lastIsBackslash) {
                        sb.appendEscaped("\\");
                    }
                    lastIsBackslash = false;
                    sb.appendEscaped(String.valueOf(c));
                    break;
            }
        }
        return sb.toSafeHtml();
    }

    public static SafeHtml toSafeHtmlLines(String... lines) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        boolean first = true;
        for (String line : lines) {
            if (first) {
                first = false;
            }
            else {
                sb.appendHtmlConstant("<br/>");
            }
            sb.appendEscaped(line);
        }
        return sb.toSafeHtml();
    }

    public static boolean hasStyle(WidgetDesc wd, String... searchedStyles) {
        return CssUtil.hasStyle(wd.getStyle(), searchedStyles);
    }

    /**
     * Trim empty lines (including lines with solely whitespace) at the beginning of the given String s,
     * trim empty lines (including lines with solely whitespace) at the end of the given String s and
     * trim whitespace at the end of each line.
     */
    public static String trimMultilineEnds(String s) {
        if (s == null) {
            return null;
        }
        final char[] chars = s.toCharArray();

        // trim starting empty lines
        int start = 0;
        int i = 0;
        while (i < chars.length && chars[i] <= ' ') {
            if (chars[i] == '\n') {
                start = i + 1;
            }
            i++;
        }

        // trim all whitespace at the end
        int end = chars.length - 1;
        while (end >= start && chars[end] <= ' ') {
            end--;
        }

        // trim whitespace at the end of each line
        final StringBuilder sb = new StringBuilder(chars.length);
        int lineEnd = start;
        for (int j = start; j <= end; j++) {
            if (chars[j] <= ' ') {
                if (chars[j] == '\n') {
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    if (lineEnd > start) {
                        sb.append(chars, start, lineEnd - start);
                    }
                    start = j + 1;
                    lineEnd = start;
                }
            }
            else {
                lineEnd = j + 1;
            }
        }
        if (lineEnd > start) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(chars, start, lineEnd - start);
        }

        return sb.toString();
    }

    public static Dimension getDimensionFromSizeStyle(String style) {
        final String sizeStyle = CssUtil.getStyleValue(style, "size");
        if (sizeStyle != null) {
            if (sizeStyle.matches("[0-9]+")) {
                final int size = Integer.parseInt(sizeStyle);
                return new Dimension(size, size);
            }
            else if (sizeStyle.matches("[0-9]+x[0-9]+")) {
                final int xPos = sizeStyle.indexOf('x');
                final int width = Integer.parseInt(sizeStyle.substring(0, xPos));
                final int height  = Integer.parseInt(sizeStyle.substring(xPos + 1));
                return new Dimension(width, height);
            }
        }
        return null;
    }
}
