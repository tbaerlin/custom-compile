/*
* TrendBarRenderer.java
*
* Created on 27.08.2008 11:29:33
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author Michael Lösch
 */
public class TooltipRenderer implements Renderer<String[]> {

    public String render(String[] strings) {
        StringBuffer sb = new StringBuffer();
        StringBuffer tooltip = new StringBuffer();
        String value;
        if (strings.length > 0) {
            sb.append("<div"); // $NON-NLS-0$
            value = strings[0];
            if (strings.length > 1) {
                for (int i = 1; i < strings.length; i++) {
                    tooltip.append(strings[i]).append("\t"); // $NON-NLS-0$
                }
                sb.append(" title=\"").append(tooltip).append("\""); // $NON-NLS-0$ $NON-NLS-1$
            }
            sb.append(">").append(value).append("</div>"); // $NON-NLS-0$ $NON-NLS-1$
        }
        return sb.toString();
    }
}
