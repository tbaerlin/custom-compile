/*
 * PriceRenderer.java
 *
 * Created on 05.06.2008 17:04:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SupplementRenderer implements Renderer<String> {

    public SupplementRenderer() {
    }

    public String render(String s) {
        if (s == null || s.length() == 0) {
            return ""; // $NON-NLS-0$
        }
        return "<span class=\"mm-supl\">" + s + "</span>"; // $NON-NLS-0$ $NON-NLS-1$
    }
}
