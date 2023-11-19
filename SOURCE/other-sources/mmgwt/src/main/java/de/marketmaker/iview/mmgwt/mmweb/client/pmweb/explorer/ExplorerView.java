package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.explorer;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.NavTree;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.NavTreeItemIdentifier;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history.PmItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;

import java.util.List;

/**
 * Created on 15.04.13 08:26
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class ExplorerView {
    private final DockLayoutPanel panel;
    private final ExplorerController controller;
    private final SimpleLayoutPanel contentContainer;
    private final SnippetTableWidget folderTable;
    private final FloatingPanel navTreeFloatingPanel;
    private NavTree<Model.Item> navTree;
    private final Widget layoutView;

    public ExplorerView(ExplorerController controller, Widget layoutView) {
        this.layoutView = layoutView;
        this.controller = controller;

        this.navTreeFloatingPanel = new FloatingPanel(FloatingPanel.Orientation.VERTICAL);
        this.contentContainer = new SimpleLayoutPanel();
        this.contentContainer.setStyleName("mm-contentData");

        final DockLayoutPanel leftPanel = new DockLayoutPanel(Style.Unit.PX);
        leftPanel.setStyleName("as-explorer-leftPanel");
        final FlowPanel leftHeader = new FlowPanel();
        leftHeader.setStyleName("as-explorer-collapse-panel");
        final Label labelCollapse = new HTML("<img src=\"clear.cache.gif\"/>"); // $NON-NLS$
        final Label labelExpand = new HTML("<img src=\"clear.cache.gif\"/>"); // $NON-NLS$
        labelCollapse.setStyleName("mm-link as-explorer-collapse expanded");
        labelExpand.setStyleName("mm-link as-explorer-collapse collapsed");
        labelCollapse.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                panel.setWidgetHidden(leftPanel, true);
                panel.setWidgetHidden(labelExpand, false);
            }
        });
        labelExpand.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                panel.setWidgetHidden(leftPanel, false);
                panel.setWidgetHidden(labelExpand, true);
                scheduleNavTreeResize();
            }
        });
        leftHeader.add(labelCollapse);
        leftPanel.addNorth(leftHeader, 33);
        leftPanel.add(this.navTreeFloatingPanel);
        this.panel = new DockLayoutPanel(Style.Unit.PX);
        this.panel.setStyleName("as-explorerView");
        this.panel.addWest(leftPanel, 200);
        this.panel.addWest(labelExpand, 19);
        this.panel.setWidgetHidden(labelExpand, true);
        this.panel.add(this.contentContainer);

        this.folderTable = SnippetTableWidget.create(createFolderColumnModel());
    }

    public DockLayoutPanel getPanel() {
        return this.panel;
    }

    private void scheduleNavTreeResize() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                navTreeFloatingPanel.onResize();
            }
        });
    }

    private TableColumnModel createFolderColumnModel() {
        return new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.name(), -1f, new FolderItemRenderer("n/a")) // $NON-NLS$
                , new TableColumn(I18n.I.zone(), -1f, TableCellRenderers.STRING)
        });
    }

    public void showFolderItems(final List<FolderItem> folderItems) {
        final DefaultTableDataModel tdm = DefaultTableDataModel.create(folderItems, new AbstractRowMapper<FolderItem>() {
            @Override
            public Object[] mapRow(FolderItem folderItem) {
                return new Object[]{
                        folderItem.withHistoryContext(PmItemListContext.createForFolder(
                                controller.getCurrentType().getDisplayText(), "pm-folder", folderItem, folderItems  // $NON-NLS$
                        )),
                        folderItem.getZone()
                };
            }
        });
        this.folderTable.updateData(tdm);
        this.contentContainer.setWidget(new ScrollPanel(this.folderTable));
    }

    public void updateExplorerTree(Model model) {
        this.navTree = new NavTree<>(model.getRootItem(), 1, this.controller);
        this.navTree.addSelectionHandler(new SelectionHandler<Model.Item>() {
            @Override
            public void onSelection(SelectionEvent<Model.Item> event) {
                NavTree.select(navTree, event.getSelectedItem());
                controller.selectionChanged(event.getSelectedItem());
            }
        });
        this.navTree.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                navTreeFloatingPanel.onResize();
            }
        });
        select(model.getSelectedIds());
        this.navTreeFloatingPanel.setWidget(this.navTree);
    }

    public void showLayout() {
        this.contentContainer.setWidget(this.layoutView);
    }

    public void select(String[] items) {
        if (this.navTree == null) {
            return;
        }
        this.navTree.setSelectedByIds(items, new NavTreeItemIdentifier<Model.Item>() {
            @Override
            public boolean hasId(Model.Item item, String id) {
                return item.getId().equals(id);
            }
        }, false);
    }

    public void showNoData(ExplorerController.Type type) {
        this.navTreeFloatingPanel.setWidget(new Label("..."));
        this.contentContainer.setWidget(new HTML("<center>" + type.getDisplayText() + ": " + I18n.I.noDataAvailable() + "</center>")); // $NON-NLS$
    }

    public void showNoLayout(ExplorerController.Type type) {
        this.contentContainer.setWidget(new HTML("<center>" + (type != null ? type.getDisplayText() + ": " : "") + I18n.I.pmAnalysisNotAvailable() + "</center>")); // $NON-NLS$
    }

    public int getOffsetHeight() {
        return this.contentContainer.getOffsetHeight();
    }

    public int getOffsetWidth() {
        return this.contentContainer.getOffsetWidth();
    }
}