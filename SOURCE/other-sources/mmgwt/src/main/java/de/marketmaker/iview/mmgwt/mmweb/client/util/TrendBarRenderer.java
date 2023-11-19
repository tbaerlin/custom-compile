/*
* TrendBarRenderer.java
*
* Created on 27.08.2008 11:29:33
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CurrentTrendBar;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Michael Lösch
 */
@NonNLS
public class TrendBarRenderer implements Renderer<CurrentTrendBar> {

    public String render(CurrentTrendBar trendBar) {
        final StringBuilder sb = new StringBuilder();

        sb.append("<center>");
        if (trendBar == null || !trendBar.isValid()) {
            sb.append("--");
        }
        else {
            sb.append("<table class=\"mm-trendbar\" cellpadding=\"0\" cellspacing=\"0\">")
                    .append("<tr>").append("<td width=\"").append(trendBar.getNegativeSideWidth()).append("\">");
            if ("0%".equals(trendBar.getNegativeWidth())) {
                sb.append("&nbsp;");
            }
            else {
                appendDiv(sb, trendBar.getNegativeWidth(), "mm-trendbar-negative", "negative-pixel");
            }
            sb.append("</td>")
                    .append("<td><div class=\"mm-trendbar-zero\"><img src=\"")
                    .append(IconImage.getUrl("trendbar-zero-pixel"))
                    .append("\" ")
                    .append("class=\"mm-trendbar-zero\"/></div>")
                    .append("</td>")
                    .append("<td width=\"").append(trendBar.getPositiveSideWidth()).append("\">");
            if ("0%".equals(trendBar.getPositiveWidth())) {
                sb.append("&nbsp;");
            }
            else {
                appendDiv(sb, trendBar.getPositiveWidth(), "mm-trendbar-positive", "positive-pixel");
            }
            sb.append("</td></tr></table>");
        }
        sb.append("</center>");
        return sb.toString();
    }

    private void appendDiv(StringBuilder sb, String width, String styleName, String pixelIconClass) {
        final String pixelIconUrl = IconImage.getUrl(pixelIconClass);
        sb.append("<div style=\"width:")
                .append(width)
                .append("\" class=\"")
                .append(styleName)
                .append("\"><img src=\"")
                .append(pixelIconUrl)
                .append("\" class=\"")
                .append(styleName)
                .append("\"/></div>");
    }
}
