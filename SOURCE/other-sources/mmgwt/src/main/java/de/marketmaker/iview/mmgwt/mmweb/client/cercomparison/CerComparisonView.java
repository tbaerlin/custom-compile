/*
 * CerComparisonView.java
 *
 * Created on 07.09.2010 11:06:30
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison;

import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.events.UpdateViewEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.events.UpdateViewHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.model.CerTableModel;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentView;

/**
 * @author Michael Lösch
 */
class CerComparisonView implements ContentView, UpdateViewHandler {
    private final DockLayoutPanel content;

    private ScrollPanel tabPanel;

    private final AbstractSelectInstrumentWidget selectInstrumentWidget;

    private final CerTableModel tableModel;

    public CerComparisonView(CerComparisonController controller, CerTableModel tableModel) {
        this.tableModel = tableModel;
        this.content = new DockLayoutPanel(com.google.gwt.dom.client.Style.Unit.PX);
        this.content.setStyleName("mm-certcomparison");
        this.tabPanel = new ScrollPanel();
        if (SessionData.isAsDesign()) {
            this.tabPanel.setWidget(CerComparisonRenderer.getDefaultWidgetNoDnd());
        }
        else {
            this.tabPanel.setWidget(CerComparisonRenderer.getDefaultWidget());
        }
        Panel headPanel = new HorizontalPanel();
        headPanel.setStyleName("mm-certcomparison-head");
        this.content.addNorth(headPanel, 44);

        this.content.add(this.tabPanel);

        if (SessionData.isAsDesign()) {
            this.selectInstrumentWidget = new AsSelectInstrumentWidget(controller);
        }
        else {
            this.selectInstrumentWidget = new GxtDnDWidget(controller);
        }
        headPanel.add(selectInstrumentWidget);

        EventBusRegistry.get().addHandler(UpdateViewEvent.getType(), this);
    }

    public Widget getWidget() {
        return this.content;
    }

    public void onBeforeHide() {
    }

    public void onUpdateView(UpdateViewEvent event) {
        this.tabPanel.setWidget(new CerComparisonRenderer(this.tableModel, !SessionData.isAsDesign()).getWidget());
    }

    public void setQuote(QuoteWithInstrument qwi) {
        this.selectInstrumentWidget.setQuote(qwi);
    }

    public String getPrintHtml() {
        return this.tabPanel.getElement().getInnerHTML();
    }
}