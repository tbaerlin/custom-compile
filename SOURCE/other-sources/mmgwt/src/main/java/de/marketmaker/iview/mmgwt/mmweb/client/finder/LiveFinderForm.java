/*
 * LiveFinderForm.java
 *
 * Created on 10.06.2008 13:31:45
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.Map;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.FlexTable;

import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.LiveFinderAbstractConfigurator;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.LiveFinderSectionConfigurator;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.StyledBorderLayout;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Michael Lösch
 */
class LiveFinderForm extends AbstractFinderForm {
    private LayoutContainer finderForm;

    private LiveFinderParamPanel liveFinderParamPanel;

    private Map<String, FinderMetaList> metaData;

    private ToolButton toolBtn;

    private int verticalScrollPosition;

    private LiveFinderAbstractConfigurator configurator;

    LiveFinderForm(FinderController fc) {
        super(fc);
    }

    @Override
    void showSettings() {
        showInternal();
    }

    @Override
    void showResult() {
        showInternal();
    }

    private void showInternal() {
        this.cardLayout.setActiveItem(this.finderForm);
        this.formShowing = false;
    }

    @Override
    void addResultPanel(AbstractFinderView view) {
        final FloatingToolbar t = new FloatingToolbar();
        setTopComponent(t);
        view.addViewSelectionTo(t);
        this.formPanel.add(view);
    }

    @Override
    protected void initSubclass() {
        this.finderForm = new LayoutContainer();
        this.finderForm.setBorders(false);
        this.finderForm.addStyleName("mm-contentData");
        this.finderForm.addStyleName("mm-finder-wrapper");
        this.finderForm.addStyleName("mm-liveFinder-wrapper");
        this.finderForm.setLayout(new StyledBorderLayout());

        final BorderLayoutData ffData = new BorderLayoutData(Style.LayoutRegion.CENTER);
        ffData.setCollapsible(true);
        ffData.setFloatable(true);
        ffData.setSplit(false);

        this.formPanel.setLayout(new FitLayout());
        this.finderForm.add(this.formPanel, ffData);
        add(this.finderForm);

        final BorderLayoutData lvData = createLiveFinderParamPanelLayoutData(300);
        this.liveFinderParamPanel = new LiveFinderParamPanel();
        this.liveFinderParamPanel.addStyleName("mm-liveFinderParamPanel");
        this.finderForm.add(this.liveFinderParamPanel, lvData);

        if (!SessionData.INSTANCE.isAnonymous()) {
            addManagementPanel(this.finderForm);
        }

    }

    private BorderLayoutData createLiveFinderParamPanelLayoutData(int size) {
        final BorderLayoutData lvData = new BorderLayoutData(Style.LayoutRegion.WEST, size);
        lvData.setCollapsible(true);
        lvData.setFloatable(true);
        lvData.setSplit(true);
        return lvData;
    }

    public void setLiveFinderParamPanelWidth(int size) {
        this.finderForm.setLayoutData(this.liveFinderParamPanel, createLiveFinderParamPanelLayoutData(size));
    }

    @Override
    protected void addToFormPanel(FlexTable flexTable, Map<String, FinderMetaList> map) {
        this.liveFinderParamPanel.setWidget(flexTable, getConfigToolBtn());
    }

    private ToolButton getConfigToolBtn() {
        if (this.toolBtn == null) {
            this.toolBtn = new ToolButton("x-tool-gear", new SelectionListener<IconButtonEvent>() { // $NON-NLS$

                @Override
                public void componentSelected(IconButtonEvent ce) {
                    getConfigurator().show();
                }
            });

            this.toolBtn.setToolTip(I18n.I.searchParamsConfig());
        }
        return this.toolBtn;
    }

    @Override
    void initialize(Map<String, FinderMetaList> map) {
        super.initialize(map);
        this.metaData = map;
    }

    @Override
    protected void showConfigurator() {
        getConfigurator().show();
    }

    void updateMetadata(Map<String, FinderMetaList> map, boolean force) {
        for (FinderSection section : this.sections) {
            section.updateMetadata(map, force);
        }
    }

    private void configChanged(boolean withSearchCall) {
        final FinderFormConfig tmpConf = getConfig("tmpConf"); // $NON-NLS$
        for (FinderSection section : this.sections) {
            section.reloadElements();
        }
        initialize(this.metaData);
        for (FinderSection section : this.sections) {
            section.reset();
        }
        onRender();
        apply(tmpConf);
        if (withSearchCall) {
            this.fc.search();
        }
    }

    public void onConfigChange(final boolean withSearchCall) {
        this.verticalScrollPosition = this.liveFinderParamPanel.getVerticalScrollPosition();
        Scheduler.get().scheduleDeferred(() -> {
            configChanged(withSearchCall);
            this.liveFinderParamPanel.setVerticalScrollPosition(this.verticalScrollPosition);
        });
    }

    @Override
    protected void addManagementPanel(LayoutContainer container) {
        final BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.EAST, 240);
        data.setCollapsible(true);
        data.setFloatable(true);
        data.setSplit(false);
        final ContentPanel managementPanel = createManagementPanel(true);
        managementPanel.addStyleName("mm-contentData-rightPanel");
        container.add(managementPanel, data);
        //noinspection Convert2MethodRef
        Scheduler.get().scheduleDeferred(() -> managementPanel.collapse());
    }

    public void addMissingElements(FinderFormConfig config) {
        for (FinderSection section : this.sections) {
            section.addMissingElements(config);
        }
    }

    public String getContentPanelHtml() {
        return this.formPanel.getElement().getInnerHTML();
    }

    @Override
    void setControlsEnabled(boolean value) {
        this.liveFinderParamPanel.setEnabled(value);
    }

    @Override
    protected void loadSelectedSearch() {
        final int n = getSavedSeachIdx();
        if (n >= 0) {
            final FinderFormConfig config = SessionData.INSTANCE.getUser().getAppConfig().getSavedSearch(this.fc.getId(), n);
            addMissingElements(config);
            Scheduler.get().scheduleDeferred(() -> {
                apply(config);
                fc.onSearchLoaded();
            });
        }
    }

    private LiveFinderAbstractConfigurator getConfigurator() {
        if (this.configurator == null) {
            this.configurator = new LiveFinderSectionConfigurator(I18n.I.searchParamsConfig(), sections, () -> configChanged(true));
        }

        return this.configurator;
    }
}