package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.ContentPanelIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.FlexSnippetsView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DragDropSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: umaurer
 * Created: 20.03.15
 */
public class DashboardView implements DashboardViewIfc {
    private final SimplePanel snippetsPanel = new SimplePanel();
    private final DashboardController controller;
    private final List<Widget> headerTools = new ArrayList<>();
    private final DashboardPalette palette;

    private boolean editMode = false;
    private FlexTable currentSnippetsTable = null;
    private HoverStyleSpec currentHoverStyles = null;
    private HandlerRegistration droppableHandlerRegistration = null;

    public DashboardView(DashboardController controller, DashboardConfig config) {
        this.controller = controller;
        this.palette = new DashboardPalette(controller, config);
        this.snippetsPanel.setStyleName("mm-dashboard-view");
        this.snippetsPanel.getElement().getStyle().setOverflowY(Style.Overflow.AUTO);
    }

    @Override
    public boolean isEditMode() {
        return this.editMode;
    }

    @Override
    public void show(final List<Snippet> snippets) {
        this.headerTools.clear();
        final FlexSnippetsView snippetsView = new FlexSnippetsView(snippets) {
            @Override
            protected ContentPanelIfc createPanel(final Snippet snippet) {
                return configurePanel(snippet, super.createPanel(snippet));
            }
        };
        snippetsView.addStyleName("as-dashboard");

        this.currentSnippetsTable = snippetsView.getTable();
        if (this.editMode) {
            enableDropTarget(this.currentSnippetsTable);
        }
        this.snippetsPanel.setWidget(snippetsView);
    }

    private ContentPanelIfc configurePanel(final Snippet snippet, final ContentPanelIfc panel) {
        if (snippet.isConfigurable()) {
            addHeaderTool(panel, "as-tool-settings", I18n.I.configuration(), new ContentPanelIfc.HeaderToolCommand() { // $NON-NLS$
                @Override
                public void execute(Widget headerToolWidget) {
                    snippet.configure(headerToolWidget);
                }
            });
        }
        addHeaderTool(panel, "as-tool-cellSpan", I18n.I.configureCellSpan(), new ContentPanelIfc.HeaderToolCommand() { // $NON-NLS$
            @Override
            public void execute(Widget headerToolWidget) {
                controller.configureSpans(snippet, headerToolWidget);
            }
        });
        addHeaderTool(panel, "x-tool-close", I18n.I.tooltipRemoveElement(), new ContentPanelIfc.HeaderToolCommand() { // $NON-NLS$
            @Override
            public void execute(Widget headerToolWidget) {
                controller.removeSnippet(snippet);
            }
        });

        if (this.editMode) {
            copyHeading(snippet, panel);
            enableDragging(panel, snippet);
        }
        return panel;
    }

    private void copyHeading(Snippet snippet, ContentPanelIfc panel) {
        final ContentPanelIfc previousPanel = snippet.getView().getContainer();
        if (previousPanel != null) {
            panel.setHeading(previousPanel.getHeading());
        }
    }

    private void addHeaderTool(ContentPanelIfc panel, String iconClass, String tooltip, ContentPanelIfc.HeaderToolCommand command) {
        final Widget headerTool = panel.addHeaderTool(iconClass, tooltip, command);
        headerTool.setVisible(this.editMode);
        this.headerTools.add(headerTool);
    }

    @Override
    public void enableEdit() {
        this.snippetsPanel.setStyleName("mm-dashboard-edit");
        enableDropTarget(this.currentSnippetsTable);
        for (Snippet snippet : this.controller.getSnippets()) {
            final ContentPanelIfc container = snippet.getView().getContainer();
            if (container != null) {
                enableDragging(container, snippet);
            }
        }
        for (Widget headerTool : this.headerTools) {
            headerTool.setVisible(true);
        }
        this.editMode = true;
        this.palette.show();
    }

    private void enableDropTarget(FlexTable table) {
        this.droppableHandlerRegistration = DragDropSupport.makeDroppable(table, this.controller);
    }

    private void enableDragging(ContentPanelIfc container, Snippet snippet) {
        final String rowCol = getRowCol(snippet);
        container.enableDragging("snippet:move:" + rowCol, rowCol); // $NON-NLS$
    }

    private String getRowCol(Snippet snippet) {
        return snippet.getConfiguration().getString("row") + ":" + snippet.getConfiguration().getString("col"); // $NON-NLS$
    }

    @Override
    public void disableEdit() {
        this.palette.hide();
        this.snippetsPanel.setStyleName("mm-dashboard-view");
        for (Snippet snippet : this.controller.getSnippets()) {
            final ContentPanelIfc container = snippet.getView().getContainer();
            if (container != null) {
                container.disableDragging();
            }
        }
        for (Widget headerTool : this.headerTools) {
            headerTool.setVisible(false);
        }
        disableDropTarget();
        this.editMode = false;
    }

    private void disableDropTarget() {
        if (this.droppableHandlerRegistration == null) {
            return;
        }
        this.droppableHandlerRegistration.removeHandler();
        this.droppableHandlerRegistration = null;
    }

    @Override
    public void setName(String name) {
        this.palette.setName(name);
    }

    @Override
    public String getName() {
        return this.palette.getName();
    }

    interface CellAction {
        void execute(int row, int col);
    }

    private void execute(TableCellPos[] cells, CellAction action) {
        for (TableCellPos tcp : cells) {
            if (tcp.getRow() >= this.currentSnippetsTable.getRowCount()) {
                break;
            }
            if (tcp.getTCol() >= this.currentSnippetsTable.getCellCount(tcp.getRow())) {
                continue;
            }
            action.execute(tcp.getRow(), tcp.getTCol());
        }
    }

    class HoverStyleSpec {
        final TableCellPos[] cells;
        final String styleName;

        public HoverStyleSpec(TableCellPos[] cells, String styleName) {
            this.cells = cells;
            this.styleName = styleName;
        }
    }

    @Override
    public void setHoverStyle(TableCellPos[] cells, final String styleName) {
        if (this.currentHoverStyles != null) {
            Firebug.warn("DashboardView <setHoverStyle> hover cells are not null");
            removeHoverStyle();
        }
        this.currentHoverStyles = new HoverStyleSpec(cells, styleName);
        final FlexTable.FlexCellFormatter formatter = this.currentSnippetsTable.getFlexCellFormatter();
        execute(this.currentHoverStyles.cells, new CellAction() {
            @Override
            public void execute(int row, int col) {
                formatter.addStyleName(row, col, styleName);
            }
        });
    }

    @Override
    public void removeHoverStyle() {
        if (this.currentHoverStyles == null) {
            Firebug.warn("DashboardView <removeHoverStyle> hover cells are null");
            return;
        }
        Firebug.debug("DashboardView <removeHoverStyle>");
        final FlexTable.FlexCellFormatter formatter = this.currentSnippetsTable.getFlexCellFormatter();
        execute(this.currentHoverStyles.cells, new CellAction() {
            @Override
            public void execute(int row, int col) {
                formatter.removeStyleName(row, col, currentHoverStyles.styleName);
            }
        });
        this.currentHoverStyles = null;
    }

    @Override
    public Widget asWidget() {
        return this.snippetsPanel.asWidget();
    }
}
