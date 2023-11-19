/*
 * FlexSnippetsView.java
 *
 * Created on 01.04.2008 10:26:11
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.ContentPanelIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.VisibilityUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NeedsScrollLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A view that uses a FlexTable to layout its sub-components
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Markus Dick
 */
public class FlexSnippetsView extends Composite implements NeedsScrollLayout {
    private static final String BASE_STYLE_NAME = "mm-gridSnippets"; // $NON-NLS$

    private static final String STYLE_INVISIBLE = "mm-invisible"; // $NON-NLS$

    private final FlexTable table = new FlexTable();

    public FlexSnippetsView(final List<Snippet> snippets) {
        //noinspection GWTStyleCheck
        this.table.setStyleName(BASE_STYLE_NAME);
        this.table.setCellSpacing(4);
        this.table.setCellPadding(0);

        int row = 0;
        int col = 0;
        boolean rowSpanWarning = false;
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        for (Snippet s : snippets) {
            final SnippetConfiguration config = s.getConfiguration();
            if (config.getBoolean("isObjectInfo", false)) { // $NON-NLS$
                continue;
            }
            final String columnWidths = config.getString("columnWidths"); // $NON-NLS$
            if (columnWidths != null) {
                setColumnWidths(this.table, columnWidths);
                continue;
            }
            final int cRow = config.getInt("row", -1); // $NON-NLS$
            final int cCol = config.getInt("col", -1); // $NON-NLS$
            if (cRow == -1) {
                if (rowSpanWarning) {
                    GWT.log("Warning: FlexSnippetsView automatic positioning may not work correctly with rowSpan > 1", null); // $NON-NLS$
                }
                if (cCol != -1) {
                    if (cCol < col) {
                        col = cCol;
                        row++;
                    }
                }
            }
            else if (cCol == -1) {
                GWT.log("Warning: FlexSnippetsView row specified, but not col", null); // $NON-NLS$
                if (rowSpanWarning) {
                    GWT.log("Warning: FlexSnippetsView automatic positioning may not work correctly with rowSpan > 1", null); // $NON-NLS$
                }
                row = cRow;
                col = 0;
            }
            else {
                row = cRow;
                col = cCol;
            }

            final Widget w;
            if (this.table.isCellPresent(row, col)) {
                w = table.getWidget(row, col);
            }
            else {
                w = null;
            }

            final MuxPanel mxc;
            if (w != null && !(w instanceof MuxPanel)) {
                mxc = new MuxPanel();
                mxc.addSnippetWidget(w);
            }
            else if (w instanceof MuxPanel) {
                mxc = (MuxPanel) w;
            }
            else {
                mxc = null;
            }

            final ContentPanelIfc panel = createPanel(s);

            final SnippetView<?> snippetView = s.getView();
            if (snippetView == null) {
                DebugUtil.logToServer("null view for " + config); // $NON-NLS$
                panel.setContentWidget(new HTML(I18n.I.error()));
            }
            else {
                if (mxc != null) {
                    mxc.addSnippetWidget(panel.asWidget());
                }
                snippetView.setContainer(panel, true);
            }

            try {
                if (mxc != null) {
                    this.table.setWidget(row, col, mxc);
                }
                else {
                    this.table.setWidget(row, col, panel);
                }
                s.onAddedToSnippetsView(this.table, row, col);
            } catch (Exception e) {
                Firebug.error("table.setWidget(" + snippetView.getClass().getName() + ")", e); // $NON-NLS$
            }
            final String style = config.getString("style", BASE_STYLE_NAME); // $NON-NLS$
            formatter.addStyleName(row, col, style);
            final int colSpan = config.getInt("colSpan", 1); // $NON-NLS$
            if (colSpan > 1) {
                formatter.setColSpan(row, col, colSpan);
            }
            final int rowSpan = config.getInt("rowSpan", 1); // $NON-NLS$
            if (rowSpan > 1) {
                formatter.setRowSpan(row, col, rowSpan);
                rowSpanWarning = true;
            }
            final String width = config.getString("width", null); // $NON-NLS$
            if (StringUtil.hasText(width)) {
                final char lastChar = width.charAt(width.length() - 1);
                formatter.getElement(row, col).getStyle().setProperty("width", Character.isDigit(lastChar) ? (width + "px") : width); // $NON-NLS$
            }
            final String maxWidth = config.getString("maxWidth", null); // $NON-NLS$
            if (StringUtil.hasText(maxWidth)) {
                final char lastChar = maxWidth.charAt(maxWidth.length() - 1);
                formatter.getElement(row, col).getStyle().setProperty("maxWidth", Character.isDigit(lastChar) ? (maxWidth + "px") : maxWidth); // $NON-NLS$
            }
            final int minHeight = config.getInt("minHeight", -1); // $NON-NLS$
            if (minHeight > 0) {
                panel.getElement().getStyle().setPropertyPx("minHeight", minHeight); // $NON-NLS$
            }
            col++; // not: col += colSpan
        }
        initWidget(this.table);
    }

    public FlexTable getTable() {
        return this.table;
    }

    private void setColumnWidths(FlexTable table, String columnWidths) {
        table.getElement().getStyle().setProperty("tableLayout", "fixed"); // $NON-NLS$
        final String[] width = columnWidths.split(","); // $NON-NLS$
        final HTMLTable.ColumnFormatter columnFormatter = table.getColumnFormatter();
        for (int i = 0; i < width.length; i++) {
            final String w = width[i].trim();
            if (!("".equals(w) || "*".equals(w))) { // $NON-NLS$
                columnFormatter.setWidth(i, w);
            }
        }
    }

    void updateVisibility(SnippetView view, boolean visible) {
        final int[] rowAndCol = findRowAndColumn(view);

        if (rowAndCol != null) {
            final int row = rowAndCol[0];
            final int col = rowAndCol[1];

            if (this.table.getCellCount(row) > 1) {
                final Widget w = this.table.getWidget(row, col);
                if (w instanceof MuxPanel) {
                    ((MuxPanel) w).updateVisibility(view, visible);
                }
                else {
                    // If cell count > 1 then we set the cell (= the snippet) invisible.
                    // We set the row only invisible iff all widgets of the row are invisible,
                    // so that no padding, border, etc. of the row remains visible.
                    // This is especially necessary, e.g., for fund structure views and views that
                    // contain snippets which implement IsVisible.
                    // Refer to: MMWEB-614/R-76054
                    updateCellAndRowVisibility(view, visible, isAnyCellOfRowVisible(row), row, col);
                }
            }
            else {
                // If cell count == 1 then we set the cell (= the snippet) and the row invisible
                // so that no padding, border, etc. of the row remain visible
                // This is especially necessary, e.g., for the alert snippet in T and S views and
                // the fund structure view. Refer to: MMWEB-651
                updateCellAndRowVisibility(view, visible, visible, row, col);
            }
        }
    }

    private void updateCellAndRowVisibility(SnippetView view, boolean cellVisible,
            boolean rowVisible, int row, int col) {
        updateCellVisibility(cellVisible, row, col);
        updateRowVisibility(rowVisible, row);
        EventBusRegistry.get().fireEvent(new VisibilityUpdatedEvent<>(view.container.asWidget(), cellVisible));
    }

    private boolean isAnyCellOfRowVisible(int row) {
        final int cellCount = this.table.getCellCount(row);
        for (int col = 0; col < cellCount; col++) {
            if (isCellOfRowVisible(row, col)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCellOfRowVisible(int row, int col) {
        final String styleName = this.table.getCellFormatter().getStyleName(row, col);
        return !styleName.contains(STYLE_INVISIBLE);
    }

    private void updateRowVisibility(boolean visible, int i) {
        if (visible) {
            this.table.getRowFormatter().removeStyleName(i, STYLE_INVISIBLE);
        }
        else {
            this.table.getRowFormatter().addStyleName(i, STYLE_INVISIBLE);
        }
    }

    private void updateCellVisibility(boolean visible, int i, int n) {
        if (visible) {
            this.table.getCellFormatter().removeStyleName(i, n, STYLE_INVISIBLE);
        }
        else {
            this.table.getCellFormatter().addStyleName(i, n, STYLE_INVISIBLE);
        }
    }

    private int[] findRowAndColumn(SnippetView view) {
        final int count = this.table.getRowCount();
        for (int row = 0; row < count; row++) {
            for (int col = 0; col < this.table.getCellCount(row); col++) {
                final Widget w = this.table.getWidget(row, col);
                if (w == view.container ||
                        (w instanceof MuxPanel &&
                                ((MuxPanel) w).containsSnippetWidget(view))) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    protected ContentPanelIfc createPanel(Snippet snippet) {
        if (SessionData.isAsDesign()) {
            return new SnippetPanel();
        }
        else {
            final ContentPanel panel = new ContentPanel();
            panel.setStyleName("mm-snippet");
            return panel;
        }
    }

    @Override
    public void setSize(String width, String height) {
    }

    public static class MuxPanel extends Composite {
        final SimplePanel panel = new SimplePanel();

        final List<Widget> listWidgets = new ArrayList<>();

        public MuxPanel() {
            this.panel.setStyleName("mm-snippet");
            initWidget(this.panel);
        }

        public void addSnippetWidget(Widget widget) {
            if (this.panel.getWidget() == null) {
                this.panel.setWidget(widget);
            }
            this.listWidgets.add(widget);
        }

        public boolean containsSnippetWidget(SnippetView view) {
            for (Widget widget : this.listWidgets) {
                if (widget == view.container) {
                    return true;
                }
            }
            return false;
        }

        public void updateVisibility(SnippetView view, boolean visible) {
            final HandlerManager hm = EventBusRegistry.get();

            if (visible) {
                for (Widget widget : this.listWidgets) {
                    final boolean visibility = view.container == widget;
                    if (visibility) {
                        this.panel.setWidget(widget);
                    }
                    hm.fireEvent(new VisibilityUpdatedEvent<>(widget, visibility));
                }
            }
            else {
                boolean visibility = true;
                for (Widget widget : this.listWidgets) {
                    if (visibility) {
                        this.panel.setWidget(widget);
                    }
                    hm.fireEvent(new VisibilityUpdatedEvent<>(widget, visibility));
                    visibility = false;
                }
            }
        }
    }
}
