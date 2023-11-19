/*
 * SpsLabelWidget.java
 *
 * Created on 02.06.2014 13:07
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Markus Dick
 */
public class SpsLabelWidget extends SpsBoundWidget<HTML, SpsLeafProperty> implements RequiresRelease, NoValidationPopup {
    private final HTML label;
    private SafeHtml labelIcon = null;
    private SafeHtml labelText = null;
    private BindFeature<SpsLeafProperty> iconNameBindFeature;
    private Tooltip.TooltipFactory tooltipFactoryAutoCompletion;

    private boolean useLabelTextAsIconTooltip = false;
    private Tooltip.TooltipFactory tooltipFactoryIconTooltip;

    public SpsLabelWidget(String label) {
        this.labelText = TextUtil.toSafeHtml(label);
        this.label = new HTML(this.labelText);
        this.label.setStyleName("sps-label");
    }

    @Override
    protected void onWidgetConfigured() {
        super.onWidgetConfigured();
        if(!this.useLabelTextAsIconTooltip) { //auto completion tooltip and qtip cannot be used simultaneously on the same widget
            this.tooltipFactoryAutoCompletion = Tooltip.addAutoCompletion(this.label);
        }
        if (!isForceCaptionWidget()) {
            this.label.addStyleName("sps-smallLabel");
        }
    }

    public BindFeature<SpsLeafProperty> createIconNameBindFeature() {
        return this.iconNameBindFeature = new BindFeature<>(new HasBindFeature() {
            @Override
            public BindFeature getBindFeature() {
                return iconNameBindFeature;
            }

            @Override
            public void onPropertyChange() {
                setIconName(iconNameBindFeature.getSpsProperty().getStringValue());
            }
        });
    }

    public void setIconName(String iconName) {
        if (StringUtil.hasText(iconName)) {
            final AbstractImagePrototype imagePrototype = IconImage.get(iconName);
            if (imagePrototype == null) {
                Firebug.warn("SpsLabelWidget.setIconName(" + iconName + ") -> IconImage not found");
                this.labelIcon = null;
            }
            else {
                this.labelIcon = imagePrototype.getSafeHtml();
            }
        }
        else {
            this.labelIcon = null;
        }
        updateLabel();
    }

    public void setLabelText(String text) {
        this.labelText = StringUtil.hasText(text)
                ? TextUtil.toSafeHtml(text)
                : null;
        updateLabel();
    }

    @Override
    public void release() {
        if (this.iconNameBindFeature != null) {
            this.iconNameBindFeature.release();
        }
        super.release();
    }

    @Override
    public void onPropertyChange() {
        setLabelText(getBindFeature().getSpsProperty().getStringValue());
    }

    private void updateLabel() {
        if (this.labelIcon == null) {
            if (this.labelText == null) {
                this.label.setText("");
                if(tooltipFactoryAutoCompletion != null) {
                    this.tooltipFactoryAutoCompletion.remove();
                }
            }
            else {
                this.label.setHTML(this.labelText);
                if(this.tooltipFactoryAutoCompletion != null) {
                    this.tooltipFactoryAutoCompletion.set(this.labelText);
                }
            }
        }
        else if (this.labelText == null) {
            this.label.setHTML(this.labelIcon);
        }
        else if (this.useLabelTextAsIconTooltip) {  //only useful, if we have an icon and a tooltip
            this.label.setHTML(this.labelIcon);
            if(this.tooltipFactoryIconTooltip == null) {
                this.tooltipFactoryIconTooltip = Tooltip.addQtip(this.label, this.labelText);
            }
            else {
                if (!StringUtil.hasText(this.labelText)) {
                    this.tooltipFactoryIconTooltip.remove();
                } else {
                    this.tooltipFactoryIconTooltip.set(this.labelText);
                }
            }
        }
        else {
            final SafeHtmlBuilder sb = new SafeHtmlBuilder();
            sb.append(this.labelIcon);
            sb.append(this.labelText);
            this.label.setHTML(sb.toSafeHtml());
        }
    }

    @Override
    protected HTML createWidget() {
        return this.label;
    }

    public SpsLabelWidget withUseLabelTextAsIconTooltip() {
        this.useLabelTextAsIconTooltip = true;
        return this;
    }

    public BindFeature<SpsLeafProperty> getIconNameBindFeature() {
        return iconNameBindFeature;
    }
}
