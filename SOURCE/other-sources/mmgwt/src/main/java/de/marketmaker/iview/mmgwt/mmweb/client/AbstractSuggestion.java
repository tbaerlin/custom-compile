/*
 * AbstractSuggestion.java
 *
 * Created on 04.06.13 13:15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.SuggestOracle;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DOMUtil;

/**
 * @author Markus Dick
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractSuggestion implements SuggestOracle.Suggestion {
    private String display;
    private String replacement = null;

    public abstract void goTo();

    public String getDisplayString() {
        return this.display;
    }

    public String getReplacementString() {
        return this.replacement;
    }

    protected String getStringWithMatch(String query, String value) {
        if (value == null) {
            return "";
        }

        final String escaped = SafeHtmlUtils.htmlEscape(value);
        final String lvalue = escaped.toLowerCase();

        int p = lvalue.indexOf(query);
        while (p > 0 && lvalue.charAt(p - 1) != ' ') {
            p = lvalue.indexOf(query, p + 1);
        }

        if (p == -1) {
            return escaped;
        }

        this.replacement = value;

        final StringBuilder sb = new StringBuilder();
        if (p > 0) {
            sb.append(escaped.substring(0, p));
        }
        sb.append("<b>").append(escaped.substring(p, p + query.length())).append("</b>"); // $NON-NLS-0$ $NON-NLS-1$
        if (p + query.length() < escaped.length()) {
            sb.append(escaped.substring(p + query.length()));
        }
        return sb.toString();
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public String getDisplay() {
        return display;
    }

    public String getReplacement() {
        return replacement;
    }
}
