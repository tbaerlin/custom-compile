/*
 * HighLowPriceGraph.java
 *
 * Created on 22.07.2008 17:45:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;

/**
 * @author Ulrich Maurer
 */
public class SliderGraph extends Composite {
    private final FlexTable table;
    private final Image slider;
    private final FlexTable.FlexCellFormatter formatter;
    private final Panel graph;
    private final Image image;

    @SuppressWarnings("FieldCanBeLocal")
    private final int rowGraph;
    private final int rowValues;
    private final int rowExplainText;

    public SliderGraph(String graphUrl, String blackPixelUrl) {
        this(graphUrl, blackPixelUrl, false);
    }

    public SliderGraph(String graphUrl, String blackPixelUrl, boolean explainTextOnTop) {
        if (explainTextOnTop) {
            this.rowExplainText = 0;
            this.rowGraph = 1;
            this.rowValues = 2;
        }
        else {
            this.rowGraph = 0;
            this.rowValues = 1;
            this.rowExplainText = 2;
        }
        this.slider = new Image(blackPixelUrl);
        this.slider.setStyleName("mm-slider");
        this.slider.setVisible(false);

        final Panel sliderbox = new FlowPanel();
        sliderbox.setStyleName("mm-sliderbox");
        this.image = new Image();
        this.image.setStyleName("mm-sliderback");
        setGraphUrl(graphUrl);
        sliderbox.add(this.slider);

        final String style = "mm-sliderGraph";
        this.graph = new FlowPanel();
        this.graph.setStyleName(style);
        this.graph.add(this.image);
        this.graph.add(sliderbox);

        this.table = new FlexTable();
        this.table.setStyleName(style);
        this.table.setWidget(this.rowGraph, 0, this.graph);

        this.formatter = this.table.getFlexCellFormatter();
        this.formatter.setColSpan(this.rowGraph, 0, 2);
        this.formatter.setColSpan(this.rowExplainText, 0, 2);
        setLowHighStyles("mm-low", "mm-high");

        initWidget(table);
    }

    public void setStyle(String style) {
        this.table.setStyleName("mm-sliderGraph " + style);
        this.graph.setStyleName("mm-sliderGraph " + style);
    }

    public void setLowHighStyles(String low, String high) {
        this.formatter.setStyleName(this.rowValues, 0, low);
        this.formatter.setStyleName(this.rowValues, 1, high);
    }

    public void setExplainStyle(String explain) {
        this.formatter.setStyleName(this.rowExplainText, 0, explain);
    }

    public void setLowHighTexts(String low, String high) {
        setLowHighTexts(low, high, false);
    }

    public void setGraphUrl(String url) {
        this.image.setUrl(url);
    }

    public void setLowHighTexts(String low, String high, boolean asHtml) {
        if (asHtml) {
            this.table.setHTML(this.rowValues, 0, low);
            this.table.setHTML(this.rowValues, 1, high);
        }
        else {
            this.table.setText(this.rowValues, 0, low);
            this.table.setText(this.rowValues, 1, high);
        }
    }

    public void setExplainText(String explain) {
        setExplainText(explain, false);
    }

    public void setExplainText(String explain, boolean asHtml) {
        if (asHtml) {
            this.table.setHTML(this.rowExplainText, 0, explain);
        }
        else {
            this.table.setText(this.rowExplainText, 0, explain);
        }
    }

    public void setLowHighTooltips(String low, String high) {
        this.formatter.getElement(this.rowValues, 0).setAttribute("qtip", low);
        this.formatter.getElement(this.rowValues, 1).setAttribute("qtip", high);
    }

    public void setExplainTooltip(String explain) {
        this.formatter.getElement(this.rowExplainText, 0).setAttribute("qtip", explain);
    }


    public void setSliderVisible(boolean visible) {
        this.slider.setVisible(visible);
    }

    /**
     * Set the slider value. The value must between 0f and 100f.
     *
     * @param value The slider value.
     */
    public void setValue(float value) {
        final float v = Math.min(100f, Math.max(0f, value));
        DOM.setStyleAttribute(this.slider.getElement(), "left", v + "%");
    }
}