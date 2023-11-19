/*
 * DashboardSnippetErrorUtil.java
 *
 * Created on 18.09.2015 11:05
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
public final class DashboardSnippetErrorUtil {
    @NonNLS
    public static SafeHtml getErrorHtml(String shortMessage, String iconTooltip) {
        return SafeHtmlUtils.fromTrustedString("<div class=\"sps-taskView\">" +
                "<div class=\"sps-section\" style=\"padding-top: 20px; text-align: center;\">" +
                "<div class=\"sps-section\">" +
                getIconChild(iconTooltip) +
                "<div class=\"sps-child\"><div class=\"sps-label sps-smallLabel\">&nbsp;</div></div>" +
                getTextChild(shortMessage) +
                "<div class=\"sps-child\"><div class=\"sps-label sps-smallLabel\">&nbsp;</div></div>" +
                "</div>" +
                "</div>" +
                "</div>");
    }

    @NonNLS
    private static String getIconChild(String qtip) {
        return "<div class=\"sps-child\"><div class=\"sps-label sps-smallLabel\">" +
                "<span qtip=\"" + SafeHtmlUtils.htmlEscape(qtip) + "\">" +
                IconImage.get("PmIcon:CheckUndetermined").getHTML() +
                "</span></div></div>";
    }

    @NonNLS
    private static String getTextChild(String message) {
        return "<div class=\"sps-child\"><div class=\"sps-label sps-smallLabel\">" +
                SafeHtmlUtils.htmlEscape(message) +
                "</div></div>";
    }
}
