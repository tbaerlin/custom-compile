package de.marketmaker.itools.gwtutil.client.vml.shape;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import de.marketmaker.itools.gwtutil.client.vml.VmlUtil;

/**
 * User: umaurer
 * Date: 15.11.13
 * Time: 11:52
 */
public abstract class Shape extends VectorObject {
    private String strokeColor;
    private double strokeOpacity;
    private double strokeWidth;
    private String fillColor;
    private double fillOpacity;

    private boolean deferredStylePending = false;

    public Shape withStyle(String strokeColor, double strokeOpacity, double strokeWidth, String fillColor, double fillOpacity) {
        this.strokeColor = strokeColor;
        this.strokeOpacity = strokeOpacity;
        this.strokeWidth = strokeWidth;
        this.fillColor = fillColor;
        this.fillOpacity = fillOpacity;
        setStyleProperties();
        return this;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
        deferredStyle();
    }

    public double getStrokeOpacity() {
        return strokeOpacity;
    }

    public void setStrokeOpacity(double strokeOpacity) {
        this.strokeOpacity = strokeOpacity;
        deferredStyle();
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
        deferredStyle();
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
        deferredStyle();
    }

    public double getFillOpacity() {
        return fillOpacity;
    }

    public void setFillOpacity(double fillOpacity) {
        this.fillOpacity = fillOpacity;
        deferredStyle();
    }

    private void deferredStyle() {
        if (this.deferredStylePending) {
            return;
        }
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                setStyleProperties();
            }
        });
        this.deferredStylePending = true;
    }

    private void setStyleProperties() {
        final Element stroke = VmlUtil.getOrCreateChildElementWithTagName(getElement(), "stroke");
        stroke.setPropertyString("color", this.strokeColor);
        stroke.setPropertyString("weight", this.strokeWidth + "px");
        stroke.setPropertyString("opacity", "" + this.strokeOpacity);
        stroke.setPropertyBoolean("on", this.strokeWidth > 0 && this.strokeColor != null);
        final Element fill = VmlUtil.getOrCreateChildElementWithTagName(getElement(), "fill");
        if (this.fillColor == null) {
            fill.setPropertyString("color", "black");
            fill.setPropertyBoolean("on", false);
        }
        else {
            fill.setPropertyString("color", this.fillColor);
            fill.setPropertyBoolean("on", true);
        }
        fill.setPropertyString("opacity", "" + this.fillOpacity);
        this.deferredStylePending = false;
    }
}