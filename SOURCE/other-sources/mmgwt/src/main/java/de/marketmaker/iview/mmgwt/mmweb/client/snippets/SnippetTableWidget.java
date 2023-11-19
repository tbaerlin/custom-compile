/*
 * SnippetTableWidget.java
 *
 * Created on 20.03.2008 13:41:39
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItemCollector;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellMetaData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RendererContext;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DOMUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.HtmlBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.HtmlBuilderImpl;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkManager;
import de.marketmaker.iview.mmgwt.mmweb.client.table.Subheading;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HTMLWithLinks;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.WidgetUtil;

import java.util.ArrayList;

/**
 * A simple table that is prepared as a whole in a StringBuffer each time the table is updated,
 * as this is way faster than rendering a gwt HTMLTable, especially as SnippetTableWidgets are
 * expected to be updated frequently.<p>
 * Calls a TableCellRenderer associated with a column (and possibly row) to render a table
 * cell's content.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnippetTableWidget extends Composite {
    private static final String STYLE_ROW = "mm-snippetTable-row"; // $NON-NLS-0$

    public static final String PUSH_ATTRIBUTE_NAME = "p"; // as short as possible // $NON-NLS-0$

    public static final String PUSH_ATTRIBUTE_VALUE = "t"; // as short as possible // $NON-NLS-0$

    public static SnippetTableWidget create(TableColumnModel columnModel) {
        return create(columnModel, "mm-snippetTable"); // $NON-NLS-0$
    }

    public static SnippetTableWidget create(TableColumnModel columnModel, String tableClass) {
        return new SnippetTableWidget(columnModel, new String[] {tableClass});
    }

    public static SnippetTableWidget create(TableColumnModel columnModel, String[] tableClasses,
                                            PriceSupport priceSupport) {
        return new SnippetTableWidget(columnModel, tableClasses, priceSupport);
    }

    protected TableColumnModel columnModel;

    private int cellpadding = 0;

    private int cellspacing = 0;

    private boolean withColumnGroups;

    private final LinkManager linkManager = new LinkManager();

    private final RendererContext context;

    /**
     * use a SimplePanel so that our html receives the onLoad/onUnload events for every update
     * and the event listener gets un-/registered correctly
     */
    private final SimplePanel content = new SimplePanel();

    private final String[] classAttributes;

    private LinkListener<String> sortLinkListener;

    private Subheading subheading;

    private String prefix = ""; // $NON-NLS-0$

    private String suffix = ""; // $NON-NLS-0$

    private static boolean appendSpaceToSortableHeaders = true;

    public static void setAppendSpaceToSortableHeaders(boolean appendSpaceToSortableHeaders) {
        SnippetTableWidget.appendSpaceToSortableHeaders = appendSpaceToSortableHeaders;
    }

    public static boolean isAppendSpaceToSortableHeaders() {
        return appendSpaceToSortableHeaders;
    }

    // if the table is used in push mode, we cache the Element objects that we need to modify
    private ArrayList<PushRenderItem> renderItems;

    public SnippetTableWidget(TableColumnModel columnModel) {
        this(columnModel, new String[] {"mm-snippetTable"}); // $NON-NLS-0$
    }

    public SnippetTableWidget(TableColumnModel columnModel, String[] classAttributes) {
        this(columnModel, classAttributes, null);
    }

    public SnippetTableWidget(TableColumnModel columnModel, String[] classAttributes,
                              PriceSupport priceSupport) {
        this.columnModel = columnModel;
        this.classAttributes = classAttributes;
        this.context = new RendererContext(this.linkManager, priceSupport);
        initWidget(this.content);
    }

    public SnippetTableWidget surroundedBy(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
        return this;
    }

    public TableColumnModel getColumnModel() {
        return this.columnModel;
    }

    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
    }

    public void setCellpadding(int cellpadding) {
        this.cellpadding = cellpadding;
    }

    public void setCellspacing(int cellspacing) {
        this.cellspacing = cellspacing;
    }

    public void setSortLinkListener(LinkListener<String> sortLinkListener) {
        this.sortLinkListener = sortLinkListener;
    }

    public void setSubheading(Subheading subheading) {
        this.subheading = subheading;
    }

    /**
     * Update the values for pushed data. Must only be called <em>after</em>
     * {@link #update(de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel, de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel)}
     * , as it modifies the DOM Elements created by that call.
     *
     * @param model contains values to be rendered
     */
    public void push(TableDataModel model) {
        // todo remove
        updatePriceGeneration();
    }

    private void updatePriceGeneration() {
        this.context.updatePriceGeneration();
    }

    /**
     * Update using a new columnModel and and a new dataModel
     *
     * @param columnModel new columnModel
     * @param dataModel   new dataModel
     */
    public void update(TableColumnModel columnModel, TableDataModel dataModel) {
        this.columnModel = columnModel;
        updateData(dataModel);
    }

    public void updateData(TableDataModel model) {
        if (model == null || model.getRowCount() == 0 && model.getColumnCount() == 0) {
            setMessage(getMessage(model), false);
            return;
        }
        else if (model.getRowCount() > 0 && model.getColumnCount() != this.columnModel.getColumnCount()) {
            throw new IllegalArgumentException(model.getColumnCount() + "!=" + this.columnModel.getColumnCount()); // $NON-NLS-0$
        }
        setContentWidget(toHtml(model));
        updatePriceGeneration();
    }

    private String getMessage(TableDataModel model) {
        return (model != null && model.getMessage() != null) ? model.getMessage() : I18n.I.noDataAvailable();
    }

    public void setMessage(String text, boolean asHtml) {
        final HTML html = new HTML();
        html.setStyleName("mm-snippetMessage"); // $NON-NLS-0$
        if (asHtml) {
            html.setHTML(text);
        }
        else {
            html.setText(text);
        }

        setContentWidget(html);
    }

    private void setContentWidget(Widget w) {
        this.renderItems = null;
        this.content.setWidget(w);
    }

    private int[] getColumnOrder(TableDataModel model) {
        final int[] result = (model != null) ? model.getColumnOrder() : null;
        if (result != null) {
            return result;
        }
        return this.columnModel.getColumnOrder();
    }

    private void appendData(TableDataModel model, int[] order, HtmlBuilder hb) {
        hb.startTag("tbody"); // $NON-NLS$
        final int rowCount = model.getRowCount();
        final int visibleColumns = computeVisibleColumnCount();
        StringBuffer sb = new StringBuffer();
        for (int row = 0; row < rowCount; row++) {
            appendSubheaderIfNeeded(model, row, hb, visibleColumns);
            appendTR(model, row, hb);
            for (int colId = 0, orderLength = order.length; colId < orderLength; colId++) {
                int col = order[colId];
                final TableColumn column = this.columnModel.getTableColumn(col);
                if (!column.isVisible()) {
                    continue;
                }
                final TableCellRenderer r = column.getRenderer(row);
                hb.startTag("td").addClass(getCellClass(r, column)); // $NON-NLS$
                final String width = column.getWidthAsString();
                if (width != null) {
                    hb.addAttribute("style", "width: " + width + "; min-width: " + width + ";"); // $NON-NLS$
                }
                if (r.isPushRenderer()) {
                    // use marker attribute to be able to find Element objects for pushed cells
                    hb.append(" p=\"t\""); // $NON-NLS$
                }
                final CellMetaData metaData = model.getMetaData(row, col);
                if (metaData != null) {
                    if (metaData.getCellClass() != null) {
                        hb.addClass(metaData.getCellClass());
                    }
                    if (metaData.getColSpan() > 1) {
                        hb.addAttribute("colSpan", metaData.getColSpan()); // $NON-NLS$
                        colId += metaData.getColSpan() - 1;
                    }
                    if (metaData.getToolTip() != null) {
                        hb.addAttribute("qtip", metaData.getToolTip()); // $NON-NLS$
                    }
                }

                sb.setLength(0);
                try {
                    r.render(model.getValueAt(row, col), sb, this.context);
                    hb.addClass(this.context.getStyle());


                    hb.setContent(sb.toString());
                }
                catch (Exception e) {
                    Firebug.warn("SnippetTableWidget (" + row + ", " + col // $NON-NLS$
                            + ") cannot render type " + getValueClass(model.getValueAt(row, col)) // $NON-NLS$
                            + " with renderer " + getClassName(r), e); // $NON-NLS$
                    hb.setContent("<div class=\"mm-renderError\">?</div>"); // $NON-NLS$
                }
            }
            hb.closeLast(); // tr
        }
        hb.closeLast(); // tbody
    }

    private String getValueClass(final Object value) {
        return value != null ? value.getClass().getName() : "null"; // $NON-NLS$
    }

    private String getClassName(TableCellRenderer r) {
        return r instanceof TableCellRenderers.DelegateRenderer
                ? ((TableCellRenderers.DelegateRenderer) r).getDelegate().getClass().getName()
                : r.getClass().getName();
    }

    private String getCellClass(TableCellRenderer r, TableColumn column) {
        final String cellClass = column.getCellClass();
        return (cellClass != null) ? cellClass : r.getContentClass();
    }

    private void appendTR(TableDataModel model, int row, HtmlBuilder hb) {
        hb.startTag("tr"); // $NON-NLS$
        final String flipId = model.getFlipId(row);
        if (flipId != null) {
            hb.addAttribute("mm:flipId", flipId); // $NON-NLS$
            hb.addAttribute("style", "display: none"); // $NON-NLS$
        }
//        hb.addClass(STYLE_ROW);
        hb.addClass(model.getRowClass(row));
    }

    private int computeVisibleColumnCount() {
        int[] order = columnModel.getColumnOrder();
        int visibleColumnCount = order.length;
        for (int col : order) {
            final TableColumn column = this.columnModel.getTableColumn(col);
            if (!column.isVisible()) {
                visibleColumnCount--;
            }
        }
        return visibleColumnCount;
    }

    private void appendSubheaderIfNeeded(TableDataModel model, int currentRow, HtmlBuilder hb, int visibleColumns) {
        if (this.subheading != null && subheading.isSubheaderRow(model, currentRow)) {
           subheading.buildHeaderRow(hb, model, visibleColumns, currentRow);
        }
    }

    private void appendHeader(TableDataModel model, int[] order, HtmlBuilder hb) {
        hb.startTag("thead"); // $NON-NLS$
        if (this.withColumnGroups) {
            appendHeaderGroups(order, hb);
        }
        hb.startTag("tr"); // $NON-NLS$
        StringBuffer sbr = new StringBuffer();
        for (final int col : order) {
            final TableColumn column = this.columnModel.getTableColumn(col);
            if (!column.isVisible()) {
                continue;
            }
            hb.startTag("th"); // $NON-NLS$
            final String width = column.getWidthAsString();
            if (width != null) {
                hb.addAttribute("style", "width: " + width + "; min-width: " + width + ";"); // $NON-NLS$
            }
            hb.addAttribute("qtip", column.getTitleToolTip()); // $NON-NLS$

            final TableColumn.Sort sort = model.getSort(column.getSortKey());
            hb.addClass(sort.getStyle());

            final String header = renderHeader(sbr, column, sort);
            hb.setContent(header); // closes th
        }
        hb.closeLast(); // tr
        hb.closeLast(); // thead
    }

    private String renderHeader(StringBuffer sbr, TableColumn column, TableColumn.Sort sort) {
        if (sort == TableColumn.Sort.NOTSORTABLE || this.sortLinkListener == null) {
            final TableCellRenderer r = column.getHeaderRenderer();
            if (r == null) {
                return column.getTitle();
            }
            r.render(column.getTitle(), sbr, this.context);
        }
        else {
            final LinkContext<String> lc = new LinkContext<>(this.sortLinkListener, column.getSortKey());
            String linkContent = column.getTitle();
            if (sort != TableColumn.Sort.SORTABLE && appendSpaceToSortableHeaders) {
                linkContent += "&nbsp;&nbsp;&nbsp;"; // $NON-NLS-0$
            }
            this.linkManager.appendLink(lc, linkContent, null, sbr);
        }
        final String result = sbr.toString();
        sbr.setLength(0);
        return result;
    }

    private void appendHeaderGroups(int[] order, HtmlBuilder hb) {
        hb.startTag("tr"); // $NON-NLS-0$
        for (int i = 0; i < order.length; i++) {
            final TableColumn column = this.columnModel.getTableColumn(order[i]);
            if (!column.isVisible()) {
                continue;
            }
            int colSpan = 1;
            while (i < order.length - 1) {
                final TableColumn nextColumn = this.columnModel.getTableColumn(order[i + 1]);
                if (!nextColumn.isInSameGroupAs(column)) {
                    break;
                }
                i++;
                if (nextColumn.isVisible()) {
                    colSpan++;
                }
            }
            final String title = column.getGroupTitle() != null ? column.getGroupTitle() : "&nbsp;"; // $NON-NLS-0$
            hb.startTag("th").addAttribute("colspan", colSpan).setContent(title); // $NON-NLS-0$ $NON-NLS-1$
        }
        hb.closeLast(); // tr
    }

    private boolean isWithColumnGroups(int[] order) {
        for (int col : order) {
            final TableColumn column = this.columnModel.getTableColumn(col);
            if (column.getGroupTitle() != null) {
                return true;
            }
        }
        return false;
    }


    private HTML
    toHtml(TableDataModel model) {
        final HTML result = new HTMLWithLinks(toHtmlStr(model), this.linkManager);
        result.setStyleName("mm-snippetTable-div"); // $NON-NLS-0$
        BrowserSpecific.INSTANCE.applyRowAction(result, STYLE_ROW);
        return result;
    }

    private String toHtmlStr(TableDataModel model) {
        this.linkManager.clear();

        final int[] order = getColumnOrder(model);
        this.withColumnGroups = isWithColumnGroups(order);

        HtmlBuilder hb = new HtmlBuilderImpl();
        hb.append(this.prefix);

        hb.startTag("table"); // $NON-NLS-0$
        for (String attr : this.classAttributes) {
            hb.addClass(attr);
        }
        hb.addAttribute("cellpadding", this.cellpadding); // $NON-NLS-0$
        hb.addAttribute("cellspacing", this.cellspacing); // $NON-NLS-0$

        if (this.columnModel.isHeaderVisible()) {
            appendHeader(model, order, hb);
        }
        appendData(model, order, hb);

        hb.closeAllTags();

        hb.append(this.suffix);
        return hb.build();
    }

    public void setVisible(String flipId, boolean visible) {
        final NodeList<Element> trElements = getTrElements();
        final int rowCount = trElements.getLength();
        for (int row = 0; row < rowCount; row++) {
            final Element trElement = trElements.getItem(row);
            if (flipId.equals(trElement.getAttribute("mm:flipId"))) { // $NON-NLS-0$
                DOMUtil.setTableRowVisible(trElement, visible);
            }
        }
    }

    public void setVisible(int[] rows, boolean visible) {
        final NodeList<Element> trElements = getTrElements();
        final int rowCount = trElements.getLength();
        for (int row : rows) {
            if (row < rowCount) {
                DOMUtil.setTableRowVisible(trElements.getItem(row), visible);
            }
        }
    }


    private NodeList<Element> getTrElements() {
        return this.content.getWidget().getElement().getElementsByTagName("tr"); // $NON-NLS-0$
    }

    Element getTdElement(int row, int column) {
        final Element trElement = getTrElement(row);
        if (trElement == null) {
            return null;
        }
        final NodeList<Element> tdElements = trElement.getElementsByTagName("td"); // $NON-NLS-0$
        if (tdElements.getLength() <= column) {
            Firebug.debug("SnippetTableWidget - column not available: " + column);
            return null;
        }
        return tdElements.getItem(column);
    }

    private Element getTrElement(int row) {
        if (this.columnModel.isHeaderVisible()) {
            row++;
        }
        final NodeList<Element> trElements = getTrElements();
        if (trElements.getLength() <= row) {
            Firebug.debug("SnippetTableWidget - row not available: " + row);
            return null;
        }
        return trElements.getItem(row);
    }

    public void setText(int row, int column, String text, boolean asHtml) {
        final Element tdElement = getTdElement(row, column);
        if (tdElement == null) {
            return;
        }
        if (asHtml) {
            tdElement.setInnerHTML(text);
        }
        else {
            tdElement.setInnerText(text);
        }
    }

    public void setLinkText(int row, int column, String text, boolean asHtml) {
        final Element tdElement = getTdElement(row, column);
        if (tdElement == null) {
            return;
        }
        if (asHtml) {
            tdElement.getFirstChildElement().setInnerHTML(text);
        }
        else {
            tdElement.getFirstChildElement().setInnerText(text);
        }
    }

    public ArrayList<PushRenderItem> getRenderItems(PushRenderItemCollector collector) {
        if (this.renderItems == null) {
            this.renderItems = collector.collect(getPushedTDs());
        }
        return this.renderItems;
    }

    public ArrayList<PushRenderItem> getRenderItems(TableDataModel model) {
        if (this.renderItems == null) {
            this.renderItems = computeRenderItems(model);
        }
        return this.renderItems;
    }

    private ArrayList<PushRenderItem> computeRenderItems(TableDataModel model) {
        final ArrayList<Element> elements = getPushedTDs();
        final ArrayList<PushRenderItem> result = new ArrayList<>();
        final int[] order = getColumnOrder(model);
        final int orderCount = order.length;

        int row = 0;
        int col = 0;
        int i = 0;
        // iterate over row/col and find the i-th td Element with the push attribute value
        // and assign it to the i-th cell that can receive pushed data:
        while (row < model.getRowCount()) {
            while (col < orderCount && !isPushed(order[col], row)) {
                col++;
            }
            if (col < orderCount) {
                try {
                    final Price price = (Price) model.getValueAt(row, order[col]);
                    if (price != null) {
                        result.add(new PushRenderItem(elements.get(i++),
                                price,
                                this.columnModel.getTableColumn(order[col]).getRenderer(row)));
                    }
                } catch(ClassCastException e) {
                    Firebug.warn("!!!!! col, row: " + col + " " + row + "\n class: " + model.getValueAt(row, col).getClass().getName());
                }
                col++;
            }
            if (col == orderCount) {
                col = 0;
                row++;
            }
        }
        return result;
    }

    private boolean isPushed(int column, int row) {
        final TableColumn tableColumn = this.columnModel.getTableColumn(column);
        return tableColumn.isVisible() && tableColumn.getRenderer(row).isPushRenderer();
    }

    private ArrayList<Element> getPushedTDs() {
        return WidgetUtil.getPushedElements(this.content.getWidget(), "td", "div"); // $NON-NLS$
    }
}
