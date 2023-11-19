/*
 * AbstractFinderView.java
 *
 * Created on 17.06.2008 13:16:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PercentRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSelectionViewButtons;

import static com.google.gwt.dom.client.Style.Unit;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
abstract class AbstractFinderView<V extends AbstractFinder>
        extends WidgetComponent implements LinkListener<String>, ProvidesResize {


    private final TableColumnModel[] columnModels;

    protected final V controller;

    private SnippetTableWidget currentWidget;

    protected Grid g;

    private final SnippetTableWidget[] stws;

    protected static final TableCellRenderer PCT_NO_SUFFIX
            = new TableCellRenderers.DelegateRenderer<>(new PercentRenderer("--", false), "mm-right"); // $NON-NLS-0$ $NON-NLS-1$

    private ViewSelectionViewButtons viewSelectionView;

    protected final FloatingToolbar bottomToolbar;

    protected AbstractFinderView(V controller) {
        super(new DockLayoutPanel(Unit.PX));
        final DockLayoutPanel layoutPanel = (DockLayoutPanel) this.getWidget();

        this.controller = controller;
        addStyleName("mm-finderResult-panel"); // $NON-NLS-0$

        final PagingWidgets pagingWidgets = new PagingWidgets(getPagingWidgetsConfig());
        this.bottomToolbar = pagingWidgets.getToolbar();
        layoutPanel.addSouth(inPanel(this.bottomToolbar, "x-panel-bbar", null), 27); // $NON-NLS$
        this.controller.getPagingFeature().setPagingWidgets(pagingWidgets);

        createDataGrid();

        layoutPanel.add(inPanel(this.g, "x-panel-body", Style.Overflow.AUTO)); // $NON-NLS$

        final int n = getViewCount();
        this.stws = new SnippetTableWidget[n];
        this.columnModels = new TableColumnModel[n];

        initColumnModels(this.columnModels);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        BrowserSpecific.INSTANCE.forceLayout((DockLayoutPanel) this.getWidget());
    }

    private Widget inPanel(Widget widget, String panelStyle, Style.Overflow overflow) {
        final SimplePanel panel = new SimplePanel(widget);
        panel.addStyleName(panelStyle);
        if (overflow != null) {
            panel.getElement().getStyle().setOverflow(overflow);
        }
        return panel;
    }

    protected PagingWidgets.Config getPagingWidgetsConfig() {
        return new PagingWidgets.Config().withPageSize(FeatureFlags.Feature.VWD_RELEASE_2015.isEnabled());
    }

    private void createDataGrid() {
        final int rowCount = getNumRowsInDataGrid();
        this.g = new Grid(rowCount, 1);
        this.g.setCellPadding(0);
        this.g.setCellSpacing(0);
        final String styleName = "mm-finderResult"; // $NON-NLS-0$
        this.g.setStyleName(styleName);
        final HTMLTable.CellFormatter formatter = this.g.getCellFormatter();
        formatter.setStyleName(0, 0, styleName + " query"); // $NON-NLS-0$
        formatter.setStyleName(1, 0, styleName + " table"); // $NON-NLS-0$
        if (rowCount >= 3) {
            formatter.setStyleName(2, 0, styleName + " detail"); // $NON-NLS-0$
        }
    }

    protected int getNumRowsInDataGrid() {
        return 2;
    }

    protected int getSelectedView() {
        return this.controller.getViewSelectionModel().getSelectedView() - this.controller.getSettingsViewCount();
    }

    protected int getViewCount() {
        return this.controller.getViewSelectionModel().getViewCount() - this.controller.getSettingsViewCount();
    }

    protected void updateViewButtons() {
        this.viewSelectionView.updateButtons();
    }

    protected void addViewSelectionTo(final FloatingToolbar toolbar) {
        final IndexedViewSelectionModel viewSelectionModel = this.controller.getViewSelectionModel();
        viewSelectionView = new ViewSelectionViewButtons(viewSelectionModel, toolbar) {
            protected void addButtons() {
                final int settingsViewCount = controller.getSettingsViewCount();
                if (this.buttons.length <= settingsViewCount + 1) {
                    super.addButtons();
                    return;
                }
                for (int i = 0; i < settingsViewCount; i++) {
                    if (i > 0) {
                        addSeparator();
                    }
                    add(this.buttons[i]); // Settings
                }
                addSpacer();
                addSpacer();

                final Label ttiResult = new Label(I18n.I.result());
                ttiResult.addStyleName("mm-toolbar-text"); // $NON-NLS-0$
                add(ttiResult);
                addSpacer();

                for (int i = settingsViewCount; i < this.buttons.length; i++) {
                    if (i > settingsViewCount) {
                        addSeparator();
                    }
                    add(this.buttons[i]);
                }

// TODO: use this code, if PagingWidgets should be located in TopToolbar (remove PagingWidgets in constructor)
//                final PagingWidgets pagingWidgets = new PagingWidgets(getPagingWidgetsConfig().withToolbar(toolbar));
//                controller.getPagingFeature().setPagingWidgets(pagingWidgets);
            }
        };
    }

    protected void unselectResult() {
        this.controller.getViewSelectionModel().setUnselected(true);
    }

    public void onClick(LinkContext<String> context, Element e) {
        this.controller.toggleSort(context);
    }

    protected abstract void initColumnModels(TableColumnModel[] columnModels);

    void show(TableDataModel dtm, Widget explanation) {
        final int n = getSelectedView();

        if (this.stws[n] == null) {
            this.stws[n] = SnippetTableWidget.create(this.columnModels[n],
                    new String[]{"mm-snippetTable", "mm-noWrapSnippetTable"}, null); // $NON-NLS-0$ $NON-NLS-1$
            this.stws[n].setSortLinkListener(this);
        }
        this.stws[n].updateData(dtm);
        this.g.setWidget(0, 0, explanation);

        if (this.currentWidget != this.stws[n]) {
            this.g.setWidget(1, 0, this.stws[n]);
            this.currentWidget = this.stws[n];
        }
        showView(n);
    }

    protected void showView(int n) {
        // subclasses can override this to show additional content
    }
}
