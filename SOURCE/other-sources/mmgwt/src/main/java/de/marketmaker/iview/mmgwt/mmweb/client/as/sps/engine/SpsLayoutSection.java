/*
 * SpsLayoutSection.java
 *
 * Created on 04.04.2014 07:45
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

/**
 * @author Markus Dick
 */
@NonNLS
public class SpsLayoutSection extends SpsSection implements NoValidationPopup {
    public SpsLayoutSection() {
        super();
    }

    @Override
    protected Panel addChildWidgets(Panel panel, String pClass) {
        if(!(panel instanceof LayoutPanel)) {
            throw new IllegalArgumentException("SpsLayoutSection <addChildWidgets> given panel must be a LayoutPanel");
        }
        final List<SpsWidget> spsWidgets = this.getChildrenFeature().getChildren();
        for (SpsWidget spsWidget : spsWidgets) {
            final Widget[] widgets = spsWidget.asWidgets();
            for (Widget widget : widgets) {
                panel.add(widget);
            }
        }
        return panel;
    }

    @Override
    public Widget createNorthWidget() {
        return null;
    }

    @Override
    protected HTML createCaptionWidget() {
        return null;
    }

    @Override
    protected HTML createCaptionPanel() {
        return null;
    }

    @Override
    protected SimplePanel createInfoIconPanel() {
        return null;
    }

    @Override
    protected void updateInfoIconPanel() {
        // nothing to do
    }

    @Override
    protected Panel createWidget() {
        return addChildWidgets(new LayoutPanel(), "sps-layout");
    }
}

