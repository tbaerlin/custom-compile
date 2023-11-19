/*
 * Line.java
 *
 * Created on 21.07.2010 11:00:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.i18n;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * @author zzhao
 */
public class Line {
    private final String theLine;

    private final List<LiteralStringMatch> matches;

    public Line(String theLine) {
        this.theLine = theLine;
        this.matches = new ArrayList<>(2);
    }

    public String getTheLine() {
        return theLine;
    }

    public List<LiteralStringMatch> getMatches() {
        return matches;
    }

    public Line addLiteralStringMatch(LiteralStringMatch match) {
        this.matches.add(match);
        return this;
    }

    public String getAdjustedLine() {
        if (this.matches.isEmpty()) {
            return this.theLine;
        }
        else {
            StringBuilder sb = new StringBuilder(this.theLine.length());
            StringBuilder comments = new StringBuilder(10).append(" //");
            int startIndex = 0;
            for (int i = 0; i < this.matches.size(); i++) {
                LiteralStringMatch match = this.matches.get(i);
                if (!StringUtility.hasText(match.getPropertyKey())) {
                    sb.append(this.theLine.substring(startIndex, match.getEndIndex()));
                    if (match.isMessage()) {
                        if (comments.indexOf("TODO:") == -1) {
                            comments.append(" TODO:");
                        }
                    }
                    else {
                        comments.append(" $NON-NLS-").append(i).append("$");
                    }
                }
                else {
                    sb.append(this.theLine.substring(startIndex, match.getStartIndex()));
                    sb.append("I18nMessages.INSTANCE.").append(match.getPropertyKey()).append("()");
                    if (match.isMessage()) {
                        if (comments.indexOf("TODO:") == -1) {
                            comments.append(" TODO:");
                        }
                    }
                    comments.append(" $NLS-").append(i).append("$");
                }
                startIndex = match.getEndIndex();
            }
            if (startIndex < this.theLine.length()) {
                sb.append(this.theLine.substring(startIndex));
            }
            if (comments.length() > " //".length()) {
                sb.append(comments.toString());
            }

            return sb.toString();
        }
    }

    public boolean isNonNLSLine() {
        if (this.matches.isEmpty()) {
            return false;
        }
        for (LiteralStringMatch match : this.matches) {
            if (!match.isMessage() && !StringUtility.hasText(match.getPropertyKey())) {
                return true;
            }
        }

        return false;
    }

    public String getNonNLSHtmlLine() {
        StringBuilder sb = new StringBuilder(this.theLine.length());
        int startIdx = 0;
        for (LiteralStringMatch match : this.matches) {
            if (!match.isMessage() && !StringUtility.hasText(match.getPropertyKey())) {
                sb.append(StringEscapeUtils.escapeHtml(this.theLine.substring(startIdx, match.getStartIndex())));
                sb.append("<span class=\"mystring\">");
                sb.append(StringEscapeUtils.escapeHtml("\"" + match.getTheMatch())).append("\"");
                sb.append("</span>");
                startIdx = match.getEndIndex();
            }
        }
        if (startIdx < this.theLine.length()) {
            sb.append(StringEscapeUtils.escapeHtml(this.theLine.substring(startIdx)));
        }

        return sb.toString();
    }
}