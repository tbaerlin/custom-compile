/*
 * TableColumn.java
 *
 * Created on 20.03.2008 14:02:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TableColumn {
    public enum Sort {
        NOTSORTABLE(null), 
        SORTABLE("sortable"), // $NON-NLS-0$
        SORTED_ASC("sortable asc"), // $NON-NLS-0$
        SORTED_DESC("sortable desc"); // $NON-NLS-0$

        private final String style;

        Sort(String style) {
            this.style = style;
        }

        public String getStyle() {
            return this.style;
        }
    }

    private TableCellRenderer headerRenderer;

    private TableCellRenderer renderer;

    private TableCellRenderer[] rowRenderers;

    private String sortKey = null;

    private String groupTitle;

    private String title;

    private String titleToolTip;

    private String cellClass;

    private float width;

    private boolean fixed = false;

    private String id;

    private VisibilityCheck visibilityCheck = null;

    public TableColumn(String title, float width, TableCellRenderer renderer, String sortKey) {
        this.title = title;
        this.width = width;
        this.renderer = renderer;
        this.sortKey = sortKey;
    }

    public TableColumn(String title, float width, TableCellRenderer renderer) {
        this(title, width,  renderer, null);
    }

    public TableColumn(String title, float width, String sortKey) {
        this(title, width, TableCellRenderers.STRING_EMPTY, sortKey);
    }

    public TableColumn(String title, float width) {
        this(title, width, TableCellRenderers.STRING_EMPTY, null);
    }

    public TableColumn alignLeft() {
        return withCellClass("mm-left"); // $NON-NLS-0$
    }

    public TableColumn alignCenter() {
        return withCellClass("mm-center"); // $NON-NLS-0$
    }

    public TableColumn alignRight() {
        return withCellClass("mm-right"); // $NON-NLS-0$
    }

    public TableColumn withCellClass(String cellClass) {
        this.cellClass = cellClass;
        return this;
    }

    public TableColumn withToolTip(String toolTip) {
        this.titleToolTip = toolTip;
        return this;
    }

    public TableColumn withVisibilityCheck(VisibilityCheck vc) {
        this.visibilityCheck = vc;
        return this;
    }

    public boolean isVisible() {
        return this.visibilityCheck == null || this.visibilityCheck.isVisible(this);
    }

    public String getCellClass() {
        return cellClass;
    }

    public String getTitleToolTip() {
        return titleToolTip;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public void setRowRenderer(int row, TableCellRenderer rowRenderer) {
        if (this.rowRenderers == null) {
            this.rowRenderers = new TableCellRenderer[row + 1];
        }
        else if (row >= this.rowRenderers.length) {
            final TableCellRenderer[] tmp = new TableCellRenderer[row + 1];
            System.arraycopy(this.rowRenderers, 0, tmp, 0, this.rowRenderers.length);
            this.rowRenderers = tmp;
        }
        this.rowRenderers[row] = rowRenderer;
    }

    public TableCellRenderer getRenderer(int row) {
        if (this.rowRenderers == null) {
            return renderer;
        }
        if (row >= this.rowRenderers.length) {
            return renderer;
        }
        final TableCellRenderer rowRenderer = this.rowRenderers[row];
        return (rowRenderer != null) ? rowRenderer : renderer;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public float getWidth() {
        return width;
    }

    public boolean isInSameGroupAs(TableColumn other) {
        return this.groupTitle != null
                ? this.groupTitle.equals(other.groupTitle)
                : other.getGroupTitle() == null;
    }

    public String getWidthAsString() {
        if (this.width < 0f) {
            return null;
        }
        return this.width <= 1.0 ? (int) (this.width * 100) + "%" : (int) this.width + "px"; // $NON-NLS-0$ $NON-NLS-1$
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeaderRenderer(TableCellRenderer headerRenderer) {
        this.headerRenderer = headerRenderer;
    }

    public TableColumn withHeaderRenderer(TableCellRenderer headerRenderer) {
        this.headerRenderer = headerRenderer;
        return this;
    }

    public TableCellRenderer getHeaderRenderer() {
        return this.headerRenderer;
    }

    public void setRenderer(TableCellRenderer renderer) {
        this.renderer = renderer;
    }

    public TableColumn withRenderer(TableCellRenderer renderer) {
        this.renderer = renderer;
        return this;
    }

    /**
     * Fixed columns won't appear in column configuration dialogs, but will always be rendered.
     * It only makes sense to define all of the first n columns fixed. 
     * @return this
     */
    public TableColumn setFixed() {
        this.fixed = true;
        return this;
    }

    public boolean isFixed() {
        return this.fixed;
    }

    public TableColumn withId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }
}
