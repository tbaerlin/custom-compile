/*
 * PmMainView.java
 *
 * Created on 02.09.2009 16:48:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PmMainView extends SimpleMainView {

    private static final String TOKEN = "token"; // $NON-NLS$

    private static final String PORTRAIT_PREFIX = "P_"; // $NON-NLS$

    private final Map<String, TabItem> mapTabs = new HashMap<String, TabItem>();

    private TabItem tabItemInfo;

    private TabPanel tabPanel;

    public PmMainView(PmMainController mc) {
        super(mc);
    }

    @Override
    protected void initNorthPanel() {
        setContentHeaderVisible(false);

        this.tabPanel = new TabPanel();
        this.tabPanel.setStyleName("mm-pm-tab"); // $NON-NLS$
        String historyToken = this.controller.placeManager.getCurrentPlace();
        if (!StringUtil.hasText(historyToken)) {
            historyToken = controller.getStartPage();
        }

        boolean tabSelected = false;
        for (Map.Entry<String, String> e : getController().getPages()) {
            tabSelected |= addTabItem(e.getKey(), e.getValue(), historyToken);
        }

        this.tabItemInfo = new TabItem(I18n.I.portrait());
        this.tabItemInfo.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                final HistoryToken historyToken = tabItemInfo.getData(TOKEN);
                if (historyToken != null) {
                    historyToken.fire();
                }
            }
        });

        if (!tabSelected) {
            this.tabPanel.add(this.tabItemInfo);
            this.tabPanel.setSelection(this.tabItemInfo);
        }

        final BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.NORTH);
        data.setMargins(new Margins(5, 0, -8, 3));
        data.setSize(27f);
        this.layoutContainer.add(this.tabPanel, data);
    }

    private PmMainController getController() {
        return (PmMainController) this.controller;
    }

    @Override
    protected void onPanelsInitialized() {
        this.centerPanel.addStyleName("mm-content-pm"); // $NON-NLS$
    }

    private boolean addTabItem(final String key, String name, final String historyKey) {
        final TabItem tabItem = new TabItem(name);
        tabItem.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                PlaceUtil.goTo(key);
            }
        });
        this.mapTabs.put(key, tabItem);
        this.tabPanel.add(tabItem);
        if (key.equals(historyKey)) {
            this.tabPanel.setSelection(tabItem);
            return true;
        }
        return false;
    }

    public void setActiveTabItem(HistoryToken historyToken) {
        final String controllerId = historyToken.getControllerId();
        final TabItem tabItem = this.mapTabs.get(controllerId);
        if (tabItem != null) {
            this.tabPanel.setSelection(tabItem);
        }
        else if (controllerId.startsWith(PORTRAIT_PREFIX)) {
            if (!this.tabItemInfo.isAttached()) {
                this.tabPanel.add(this.tabItemInfo);
            }
            this.tabPanel.setSelection(this.tabItemInfo);
            this.tabItemInfo.setData(TOKEN, historyToken);
        }
    }

    @Override
    public void setContentHeader(SafeHtml safeHtml) {
        if (this.tabPanel.getSelectedItem() == this.tabItemInfo) {
            this.tabItemInfo.setText(safeHtml.asString());
        }
    }
}
