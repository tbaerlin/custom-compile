package de.marketmaker.itools.gwtutil.client.util;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Author: umaurer
 * Created: 19.08.14
 */
public class StringUtility {

    public static final String NULL_FORMATTED = "<span style=\"opacity:0.2\">&lt;n/a&gt;</span>";

    public static SafeHtml toHtmlLines(String s) {
        if (s == null) {
            return SafeHtmlUtils.fromSafeConstant(NULL_FORMATTED);
        }
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        boolean first = true;
        for (String line : s.split("\\n")) { // $NON-NLS$
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

    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean hasText(String s) {
        return !isEmpty(s);
    }
}
