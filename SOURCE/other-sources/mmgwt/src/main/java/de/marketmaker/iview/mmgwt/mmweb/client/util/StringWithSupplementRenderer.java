/*
 * PriceWithSupplementRenderer.java
 *
 * Created on 05.09.2008 16:49:01
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.Map;

/**
 * @author Ulrich Maurer
 */
public class StringWithSupplementRenderer implements Renderer<Map.Entry<String, String>> {
    private final Renderer<String> stringRenderer;
    private final Renderer<String> supplementRenderer;

    public StringWithSupplementRenderer() {
        this(new StringRenderer(""), new StringRenderer(""));
    }

    public StringWithSupplementRenderer(Renderer<String> stringRenderer, Renderer<String> supplementRenderer) {
        this.stringRenderer = stringRenderer;
        this.supplementRenderer = supplementRenderer;
    }

    public String render(Map.Entry<String, String> stringWithSupplement) {
        if (stringWithSupplement == null) {
            return ""; // $NON-NLS-0$
        }
        final String string = this.stringRenderer.render(stringWithSupplement.getKey());
        final String supplement = this.supplementRenderer.render(stringWithSupplement.getValue());

        final StringBuilder sb = new StringBuilder();
        if (StringUtil.hasText(string)) {
            sb.append(string);
            if (StringUtil.hasText(supplement)) {
                sb.append(" (").append(supplement).append(")");
            }
        }
        else if (StringUtil.hasText(supplement)) {
            sb.append(supplement);
        }
        return sb.toString();
    }
}
