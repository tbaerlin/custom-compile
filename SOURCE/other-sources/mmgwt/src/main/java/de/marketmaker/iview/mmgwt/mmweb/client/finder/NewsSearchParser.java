/*
 * NewsSearchParser.java
 *
 * Created on 11/26/14
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import de.marketmaker.itools.gwtutil.client.util.Firebug;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Willenbrock
 */
public class NewsSearchParser {

    private final RegExp searchPattern = RegExp.compile("\"([^'\"]+)\"|'([^'\"]+)'|([^'\"\\s]+)", "g"); // $NON-NLS-0$, $NON-NLS-1$

    private final String orPattern = "\\s[oO][rR]\\s"; // $NON-NLS-0$

    public String parse(final String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        final String[] splitText = text.split(orPattern);
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < splitText.length; i++) {
            final String split = splitText[i];
            parseSubQuery(sb, split);
            if (i < splitText.length - 1) {
                sb.append(" or "); // $NON-NLS-0$
            }
        }

        final String s = sb.toString();
        Firebug.log(getClass().getSimpleName() + " <parse> " + s); // $NON-NLS-0$
        return s;
    }

    public void parseSubQuery(final StringBuilder sb, final String text) {

        List<String> terms = query(text);
        if (terms.size() == 0) {
            return;
        }
        sb.append('(');
        for (int i = 0; i < terms.size(); i++) {
            final String t = terms.get(i);
            sb.append("text == '").append(t).append("'"); // $NON-NLS-0$ $NON-NLS-1$
            sb.append(" or symbol == '").append(t).append("'"); // $NON-NLS-0$ $NON-NLS-1$
            if (i < terms.size() - 1) {
                sb.append(") and ("); // $NON-NLS-0$
            }
        }
        sb.append(')');
    }

    private List<String> query(final String text) {
        final List<String> result = new ArrayList<>();
        MatchResult matchResult;
        while ((matchResult = searchPattern.exec(text)) != null) {
            for (int i = 1; i < matchResult.getGroupCount(); i++) {
                final String group = matchResult.getGroup(i);
                if (group == null || group.isEmpty()) {
                    continue;
                }
                result.add(group);
            }
        }
        return result;
    }
}