/*
 * RscRecommendationRenderer.java
 *
 * Created on 26.08.2008 16:31:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ulrich Maurer
 */
public class RecommendationRenderer implements Renderer<String> {
    public static final String NULL_TEXT = "<div class=\"mm-rsc\">--</div>"; // $NON-NLS-0$

    private final Map<Object, String> recommendations;

    public static RecommendationRenderer createRscRecommendationRenderer() {
        return new RecommendationRenderer(new HashMap<Object, String>(10) {{
            put("STRONG_BUY", getHtml("analysis-strongbuy", I18n.I.buy())); // $NON-NLS$
            put("BUY", getHtml("analysis-buy", I18n.I.buy())); // $NON-NLS$
            put("HOLD", getHtml("analysis-hold", I18n.I.hold())); // $NON-NLS$
            put("SELL", getHtml("analysis-sell", I18n.I.sell())); // $NON-NLS$
            put("STRONG_SELL", getHtml("analysis-strongsell", I18n.I.sell())); // $NON-NLS$
        }});
    }

    public static RecommendationRenderer createResearchRecommendationRenderer() {
        return new RecommendationRenderer(new HashMap<Object, String>(10) {{
            put("Kaufen", getHtml("analysis-buy", I18n.I.buy())); // $NON-NLS$
            put("Halten", getHtml("analysis-hold", I18n.I.hold())); // $NON-NLS$
            put("Verkaufen", getHtml("analysis-sell", I18n.I.sell())); // $NON-NLS$
        }});
    }

    private static String getHtml(String iconClass, String text) {
        return "<div class=\"mm-rsc\">" // $NON-NLS$
                + IconImage.get(iconClass).getHTML() + "&nbsp;" + text // $NON-NLS$
                + "</div>"; // $NON-NLS$
    }

    private RecommendationRenderer(Map<Object, String> map) {
        recommendations = map;
    }

    public String render(String recommendation) {
        if (recommendation == null) {
            return NULL_TEXT;
        }
        final String result = recommendations.get(recommendation);
        return result == null ? ("<div class=\"mm-rsc\">" + recommendation + "</div>") : result; // $NON-NLS$
    }

}
