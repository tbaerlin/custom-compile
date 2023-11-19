/*
 * HighLowPriceGraph.java
 *
 * Created on 22.07.2008 17:45:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import de.marketmaker.itools.gwtutil.client.widgets.SliderGraph;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Ulrich Maurer
 */
public class HighLowPriceGraph extends SliderGraph {
    public HighLowPriceGraph(String graphImageClass) {
        this(graphImageClass, false);
    }

    public HighLowPriceGraph(String graphImageClass, boolean explainTextOnTop) {
        super(IconImage.getUrl(graphImageClass), IconImage.getUrl("slider-slider"), explainTextOnTop); // $NON-NLS$
        setStyle("mm-hlpGraph"); // $NON-NLS$

        setLowHighTexts("low", "high"); // $NON-NLS$

        setExplainText(I18n.I.lowHigh52Weeks()); 
        setExplainStyle("mm-explain"); // $NON-NLS$

        setLowHighTooltips(I18n.I.low52Weeks(), I18n.I.high52Weeks()); 
    }

    public void setData(final String low, final String high, final String price) {
        setLowHighTexts(Renderer.PRICE.render(low), Renderer.PRICE.render(high));
        if (low == null || high == null || price == null) {
            setSliderVisible(false);
            return;
        }
        final float fLow = Float.parseFloat(low);
        final float fHigh = Float.parseFloat(high);
        final float fPrice = Float.parseFloat(price);
        setValue((fPrice - fLow) * 100f / (fHigh - fLow));
        setSliderVisible(true);
    }
}
