/*
 * ExtendBarRenderer.java
 *
 * Created on 05.09.2008 12:45:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ExtendBarData;

/**
 * @author Ulrich Maurer
 */
public class ExtendBarRenderer implements Renderer<ExtendBarData> {
    public static final String STYLE_RIGHT = "mm-extendBar right"; // $NON-NLS$
    public static final String STYLE_LEFT = "mm-extendBar left"; // $NON-NLS$
    private final String styleName;

    public ExtendBarRenderer(String styleName) {
        this.styleName = styleName;
    }

    public String render(ExtendBarData extendBarData) {
        if (extendBarData == null) {
            return "";
        }                              

        return "<div class=\"" + this.styleName + "\"><img src=\"" + IconImage.getUrl("neutral-pixel") + "\" class=\"" // $NON-NLS$
                + this.styleName + "\" style=\"width: " + extendBarData.getPercentValue() + "%\"></img></div>"; // $NON-NLS$
    }
}
