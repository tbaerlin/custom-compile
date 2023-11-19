/*
 * Workspace.java
 *
 * Created on 08.04.2008 10:22:40
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.MainControllerListenerAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecificIE7;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DOMUtil;

import java.util.List;

/**
 * @author Ulrich Maurer
 */
public class Workspace {
    public static final int TEASER_SIZE = 164;
    private static final Style.LayoutRegion TEASER_POSITION = Style.LayoutRegion.SOUTH;

    private final WorkspaceList list;
    private final ContentPanel contentPanel;
    private final ContentPanel accordionContainer;
    private final TeaserWidget teaserWidget;
    private int uncollapsedWidth = 110;
    private final BorderLayoutData teaserLayoutData = new BorderLayoutData(Style.LayoutRegion.SOUTH, TEASER_SIZE);

    Workspace(WorkspaceList list) {
        this.list = list;

        this.contentPanel = new ContentPanel();
        this.contentPanel.setLayout(new BorderLayout());
        this.contentPanel.layout();
        this.contentPanel.addListener(Events.Resize, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent baseEvent) {
                if (baseEvent instanceof BoxComponentEvent) {
                    BoxComponentEvent bce = (BoxComponentEvent) baseEvent;
                    teaserWidget.scaleImage(bce.getWidth());
                    layoutTeaser();
                }
            }
        });
        this.contentPanel.setHeading(I18n.I.favorites());
        this.contentPanel.getHeader().addTool(new ToolButton("x-tool-gear", new SelectionListener<IconButtonEvent>() { // $NON-NLS-0$
            @Override
            public void componentSelected(IconButtonEvent ce) {
                configureWorkspaceConstituents();
            }
        }));

        this.accordionContainer = new ContentPanel();
        this.accordionContainer.setHeaderVisible(false);
        this.accordionContainer.setLayout(new AccordionLayout());
        this.accordionContainer.setLayoutOnChange(true);

        teaserLayoutData.setRegion(TEASER_POSITION);
        teaserLayoutData.setSize(TEASER_SIZE);
        teaserLayoutData.setHidden(true);

        this.teaserWidget = new TeaserWidget(this);

        this.contentPanel.add(this.teaserWidget, this.teaserLayoutData);
        this.contentPanel.add(this.accordionContainer, new BorderLayoutData(Style.LayoutRegion.CENTER));

        //This is necessary, because otherwise clicks on internal history anchors
        //force IE7 to reload the page
        if (BrowserSpecific.INSTANCE instanceof BrowserSpecificIE7) {
            this.contentPanel.addListener(Events.Show, be -> {
                Firebug.log("<Workspace|productTeaser> launder links for IE7");
                DOMUtil.launderLinks(this.teaserWidget.getElement());
            });
        }
        init();
    }

    public ContentPanel getContentPanel() {
        return this.contentPanel;
    }

    public void setTeaser(TeaserConfigData teaserConfigData) {
        this.teaserWidget.setTeaserConfigData(teaserConfigData);
        this.teaserLayoutData.setHidden(DzBankTeaserUtil.isTeaserHiddenByAppConfig() || !teaserConfigData.isTeaserEnabled());
        setupTeaser();
    }

    public void setTeaser(String imageUrl) {
        this.teaserWidget.setImageUrl(imageUrl);
        this.teaserLayoutData.setHidden(DzBankTeaserUtil.isTeaserHiddenByAppConfig());
        setupTeaser();
    }

    public void setupTeaser() {
        this.contentPanel.layout(true);
    }

    public boolean remove(Widget widget) {
        return this.accordionContainer.remove(widget);
    }

    private void configureWorkspaceConstituents() {
        final Window window = new Window();
        window.setModal(true);
        window.setTitle(I18n.I.configureWorkspaces());

        final Button btnOk = new Button(I18n.I.ok(), IconImage.get("mm-element-ok")); // $NON-NLS$
        final Button btnCancel = new Button(I18n.I.cancel(), IconImage.get("mm-element-cancel")); // $NON-NLS$

        final WorkspaceConstituentsConfig wcc = new WorkspaceConstituentsConfig(btnOk::setEnabled);

        window.add(wcc);
        btnOk.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                wcc.saveSettings();
                window.hide();
            }
        });
        btnCancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                window.hide();
            }
        });
        final Grid grid = new Grid(1, 3);
        grid.setWidth("100%"); // $NON-NLS$
        grid.setHTML(0, 0, "&nbsp;"); // $NON-NLS$
        grid.setWidget(0, 1, btnOk);
        grid.setWidget(0, 2, btnCancel);
        grid.getCellFormatter().setWidth(0, 0, "60%"); // $NON-NLS$
        window.add(grid);

        window.show();
    }

    private void init() {
        this.contentPanel.setStyleName("mm-workspace"); // $NON-NLS-0$

        for (final AbstractWorkspaceItem item : this.list.getPanels()) {
            this.accordionContainer.add(item);
        }

        final String active = this.list.getProperty(WorkspaceList.KEY_SUFFIX_ACTIVE, "");
        final boolean collapse = this.list.getBooleanProperty(WorkspaceList.KEY_SUFFIX_COLLAPSED, false);
        Scheduler.get().scheduleDeferred(() -> {
            restoreSubpanelState(active);
            this.contentPanel.setExpanded(!collapse);
        });

        AbstractMainController.INSTANCE.addListener(new MainControllerListenerAdapter() {
            public void beforeStoreState() {
                Workspace.this.beforeStoreState();
            }
        });

        this.contentPanel.addListener(Events.BeforeCollapse, baseEvent -> this.uncollapsedWidth = this.contentPanel.getWidth());

        // fix gxt problem: When workspace is collapsed, floated and expanded, the border is gone.
        // Reason for the problem: panel.setBorders(false) in CollapsePanel.onShowPanel(ContentPanel)
        this.contentPanel.addListener(Events.Show, baseEvent -> {
            this.contentPanel.getElement().getStyle().setPropertyPx("borderWidth", 1); // $NON-NLS-0$
        });
    }

    private void restoreSubpanelState(String active) {
        final List<AbstractWorkspaceItem> panels = this.list.getPanels();
        boolean onePanelExpanded = false;
        for (AbstractWorkspaceItem panel : panels) {
            final boolean expanded = active.equals(panel.getStateKey());
            panel.setExpanded(expanded);
            onePanelExpanded = onePanelExpanded || expanded;
        }
        if (!(panels.isEmpty() || onePanelExpanded)) {
            panels.get(0).setExpanded(true);
        }
    }

    public void beforeStoreState() {
        if (this.contentPanel.isCollapsed()) {
            this.list.addProperty(WorkspaceList.KEY_SUFFIX_COLLAPSED, "true"); // $NON-NLS-0$
            this.list.addProperty(WorkspaceList.KEY_SUFFIX_WIDTH, this.uncollapsedWidth);
        } else {
            this.list.addProperty(WorkspaceList.KEY_SUFFIX_COLLAPSED, null);
            this.list.addProperty(WorkspaceList.KEY_SUFFIX_WIDTH, this.contentPanel.getWidth());
        }
        saveSubpanelState();
    }

    private void saveSubpanelState() {
        for (AbstractWorkspaceItem panel : this.list.getPanels()) {
            if (!panel.isCollapsed()) {
                this.list.addProperty(WorkspaceList.KEY_SUFFIX_ACTIVE, panel.getStateKey());
                return;
            }
        }
        this.list.addProperty(WorkspaceList.KEY_SUFFIX_ACTIVE, null);
    }

    void layoutTeaser() {
        this.teaserLayoutData.setSize(teaserWidget.getTeaserHeight());
        this.contentPanel.layout(true);
    }
}
