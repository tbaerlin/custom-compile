/*
 * MultiFinderGroupsController.java
 *
 * Created on 23.07.2008 13:41:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.FinderGroupCell;
import de.marketmaker.iview.dmxml.FinderGroupItem;
import de.marketmaker.iview.dmxml.FinderGroupRow;
import de.marketmaker.iview.dmxml.FinderGroupTable;
import de.marketmaker.iview.dmxml.MinMaxAvgElement;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CertificateTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderController;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderControllerRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormKeys;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderCER;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderWNT;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRendererAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MultiFinderGroupsController extends AbstractPageController {

    private static final String ISSUER_ID = "issuer"; // $NON-NLS$

    // column configs
    public static final String LEVERAGE_TYPE = "leverageType";  // $NON-NLS$
    public static final String CERTIFICATE_TYPE = "certificateType";  // $NON-NLS$
    public static final String WARRENT_TYPE = "warrantType";  // $NON-NLS$

    public static class Cell {
        private final String colKey;
        private final String rowKey;
        private final String value;

        private Cell(String rowKey, String colKey, String value) {
            this.rowKey = rowKey;
            this.colKey = colKey;
            this.value = value;
        }

        public String getColKey() {
            return colKey;
        }

        public String getRowKey() {
            return rowKey;
        }

        public String getValue() {
            return value;
        }

        public String toString() {
            return this.rowKey + ":" + this.colKey + ":" + this.value; // $NON-NLS$
        }
    }

    private static class CellLinkListener implements LinkListener<Cell> {
        private final String type;
        private final String primaryField;
        private final String secondaryField;

        private CellLinkListener(String type, String primaryField, String secondaryField) {
            this.type = type;
            this.primaryField = primaryField;
            this.secondaryField = secondaryField;
        }

        @Override
        public void onClick(LinkContext<Cell> context, Element e) {
            final Cell c = context.data;
            final FinderController controller;
            final FinderFormConfig config;
            switch (this.secondaryField) {
                case CERTIFICATE_TYPE:
                    controller = FinderControllerRegistry.get(LiveFinderCER.CER_FINDER_ID);
                    config = new FinderFormConfig("temp", controller.getId()); // $NON-NLS$
                    config.put(LiveFinderCER.BASE_ID, "true"); // $NON-NLS$
                    config.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS$
                    config.put(FinderFormKeys.ISSUER_NAME + "-item", c.getRowKey()); // $NON-NLS$
                    config.put(FinderFormKeys.TYPE, "true"); // $NON-NLS$
                    config.put(FinderFormKeys.TYPE + "-item", c.getColKey()); // $NON-NLS$
                    config.put(FinderFormKeys.SORT, "true"); // $NON-NLS$
                    config.put(FinderFormKeys.SORT + "-item", "name"); // $NON-NLS$
                    controller.prepareFind(config);
                    PlaceUtil.goTo("M_LF_CER"); // $NON-NLS-0$
                    break;
                case LEVERAGE_TYPE:
                    controller = FinderControllerRegistry.get(LiveFinderCER.LEV_FINDER_ID);
                    config = new FinderFormConfig("temp", controller.getId()); // $NON-NLS$
                    config.put(LiveFinderCER.BASE_ID, "true"); // $NON-NLS$
                    config.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS$
                    config.put(FinderFormKeys.ISSUER_NAME + "-item", c.getRowKey()); // $NON-NLS$
                    config.put(FinderFormKeys.LEVERAGE_TYPE, "true"); // $NON-NLS$
                    config.put(FinderFormKeys.LEVERAGE_TYPE + "-item", c.getColKey()); // $NON-NLS$
                    config.put(LiveFinderCER.HIDE_NOT_ACTIVE, "true");  // $NON-NLS$
                    config.put(FinderFormKeys.SORT, "true"); // $NON-NLS$
                    config.put(FinderFormKeys.SORT + "-item", "name"); // $NON-NLS$
                    controller.prepareFind(config);
                    PlaceUtil.goTo("M_LF_LEV"); // $NON-NLS-0$
                    break;
                case WARRENT_TYPE:
                    controller = FinderControllerRegistry.get(LiveFinderWNT.WNT_FINDER_ID);
                    config = new FinderFormConfig("temp", controller.getId()); // $NON-NLS$
                    config.put(LiveFinderWNT.BASE_ID, "true"); // $NON-NLS$
                    config.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS$
                    config.put(FinderFormKeys.ISSUER_NAME + "-item", c.getRowKey()); // $NON-NLS-0$
                    config.put(FinderFormKeys.WARRANT_TYPE, "true"); // $NON-NLS-0$
                    config.put(FinderFormKeys.WARRANT_TYPE + "-item", c.getColKey()); // $NON-NLS-0$
                    controller.prepareFind(config);
                    PlaceUtil.goTo("M_LF_WNT"); // $NON-NLS-0$
                    break;
                default:
                    Firebug.warn("unexpected column type in MultiFinderGroupsController: " + secondaryField);
                    controller = FinderControllerRegistry.get("L" + type);  // $NON-NLS$
                    controller.prepareFind(this.primaryField, c.getRowKey(), this.secondaryField, c.getColKey());
                    PlaceUtil.goTo("M_LF_" + this.type); // $NON-NLS-0$
            }
        }
    }

    public static LinkListener<Cell> createLinkListener(String type, String primaryField, String secondaryField) {
        return new CellLinkListener(type, primaryField, secondaryField);
    }

    private static class CellLinkRenderer extends TableCellRendererAdapter {
        private final LinkListener<Cell> listener;

        private CellLinkRenderer(LinkListener<Cell> listener) {
            this.listener = listener;
        }

        @Override
        public void render(Object data, StringBuffer sb, Context context) {
            if (data == null) {
                return;
            }
            final Cell cell = (Cell) data;
            context.appendLink(new LinkContext<>(this.listener, cell), cell.getValue(), null, sb);
        }
    }

    /**
     * @param type in { "CER", "WNT"}
     * @param secondaryField in { "certificateType", "leverageType", "warrantType"}
     */
    public static SnippetTableWidget createTable(String type, String secondaryField, String firstColumnName, List<String> columns,
                                                 TableDataModel tdm, LinkListener<Cell> linkListener, boolean withSum) {
        final TableCellRenderer linkRenderer = new CellLinkRenderer(linkListener);

        final TableColumn[] tableColumns = new TableColumn[columns.size() + 1];
        tableColumns[0] = new TableColumn(firstColumnName, 0.3f, TableCellRenderers.STRING).withId(ISSUER_ID);

        final int lastRow = tdm.getRowCount() - 1;
        int columnId = 0;
        final float columnWidth = 0.7f / columns.size();

        for (final String column : columns) {
            ++columnId;

            final String header;
            switch (secondaryField) {
                case CERTIFICATE_TYPE:
                    header = Renderer.CERTIFICATE_CATEGORY.render(column);
                    break;
                case LEVERAGE_TYPE:
                    header = Renderer.CERT_LEVERAGE_TYPE.render(column);
                    break;
                case WARRENT_TYPE:
                    header = Renderer.WARRANT_TYPE.render(column, column);
                    break;
                default:
                    Firebug.warn("unexpected column type in MultiFinderGroupsController: " + secondaryField); // $NON-NLS$
                    header = column;
                    break;
            }

            tableColumns[columnId] = new TableColumn(header, columnWidth, linkRenderer).withId(column).alignCenter();
            if (withSum) {
                tableColumns[columnId].setRowRenderer(lastRow, TableCellRenderers.STRING_CENTER); // for sum row
            }
        }

        final DefaultTableColumnModel columnModel = new DefaultTableColumnModel(tableColumns);
        columnModel.withColumnOrder(createColumnOrder(type, secondaryField, tableColumns));
        final SnippetTableWidget stw = SnippetTableWidget.create(columnModel);
        stw.updateData(tdm);
        return stw;
    }

    private static String[] createColumnOrder(String type, String secondaryField, TableColumn[] tableColumns) {
        final ArrayList<String> result = new ArrayList<>();
        result.add(ISSUER_ID);
        for (int i = 1, tableColumnsLength = tableColumns.length; i < tableColumnsLength; i++) {
            final TableColumn tableColumn = tableColumns[i];
            switch (secondaryField) {
                case CERTIFICATE_TYPE:
                    if (CertificateTypeEnum.isCertificateAllowed(tableColumn.getId(), false)) {
                        result.add(tableColumn.getId());
                    }
                    break;
                case LEVERAGE_TYPE:
                case WARRENT_TYPE:
                default:
                    result.add(tableColumn.getId());
                    break;
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public static TableDataModel toTableDataModel(FinderGroupTable fgt) {
        return toTableDataModel(fgt, null);
    }

    public static TableDataModel toTableDataModel(FinderGroupTable fgt, Boolean preferedOnes) {
        final List<FinderGroupRow> sortedRows = getSortedRows(fgt, preferedOnes);
        final boolean withSum = preferedOnes == null || !preferedOnes;
        final int sumRows = withSum ? 2 : 0;
        final DefaultTableDataModel result = new DefaultTableDataModel(sortedRows.size() + sumRows, fgt.getColumn().size() + 1);
        final int[] sums = new int[result.getColumnCount()];
        int rowId = -1;
        for (FinderGroupRow row : sortedRows) {
            rowId++;
            result.setValueAt(rowId, 0, row.getKey());
            int columnId = 0;
            for (FinderGroupCell column : row.getColumn()) {
                columnId++; // starting with 1 since 0 is issuername
                if (column == null) {
                    continue;
                }
                for (FinderGroupItem item : column.getItem()) {
                    final MinMaxAvgElement e = (MinMaxAvgElement) item;
                    result.setValueAt(rowId, columnId, new Cell(row.getKey(), column.getKey(), e.getCount()));
                    if (e.getCount() != null) {
                        sums[columnId] += Integer.parseInt(e.getCount());
                    }
                }
            }
        }
        if (!withSum) {
            return result;
        }
        rowId++;
        // add empty line to move sum further down
        result.setValueAt(rowId, 0, ""); // $NON-NLS-0$
        rowId++;
        for (int i = 0; i < sums.length; i++) {
            result.setValueAt(rowId, i, i == 0 ? I18n.I.sum() : sums[i] + "");  // $NON-NLS-0$
        }
        return result;
    }

    // HACK so DZ and WGZ issuername appears at top
    private static List<FinderGroupRow> getSortedRows(FinderGroupTable fgt, Boolean preferedOnes) {
        final ArrayList<FinderGroupRow> result = new ArrayList<>(fgt.getRow().size());
        for (FinderGroupRow r : fgt.getRow()) {
            if (preferedOnes != null) {
                if (preferedOnes && isToBePreferred(r)) {
                    result.add(r);
                }
                if (!preferedOnes && !isToBePreferred(r)) {
                    result.add(r);
                }
            }
            else if (isToBePreferred(r)) {
                result.add(r);
            }
        }
        if (preferedOnes != null) {
            return result;
        }
        for (FinderGroupRow r : fgt.getRow()) {
            if (!isToBePreferred(r)) {
                result.add(r);
            }
        }
        return result;
    }

    private static boolean isToBePreferred(FinderGroupRow r) {
        return Customer.INSTANCE.isPreferredFinderIssuer(r.getKey());
    }

    protected final LinkListener<Cell> linkListener;
    private DmxmlContext.Block<FinderGroupTable> block;

    /**
     * @param type in {"CER", "WNT"}
     * @param primaryField Y-Axis of the matrix, always "issuername"
     * @param secondaryField  X-Axis/matrix columns value in {"certificateType", "leverageType", "warrantType"}
     */
    public MultiFinderGroupsController(String type, String primaryField, String secondaryField, String sortBy,
                                       String additionalQuery) {
        this.block = this.context.addBlock("MSC_FinderGroups"); // $NON-NLS$
        this.block.setParameter("type", type); // $NON-NLS$
        this.block.setParameter("primaryField", primaryField); // $NON-NLS$
        this.block.setParameter("secondaryField", secondaryField); // $NON-NLS$
        this.block.setParameter("disablePaging", "true"); // $NON-NLS$
        this.block.setParameter("sortBy", sortBy); // $NON-NLS$
        this.block.setParameter("ascending", "true"); // $NON-NLS$
        this.block.setParameter("providerPreference", SessionData.INSTANCE.getGuiDefValue("providerPreference")); // $NON-NLS$
        if (StringUtil.hasText(additionalQuery)) {
            this.block.setParameter("query", additionalQuery); // $NON-NLS$
        }

        this.linkListener = createLinkListener(type, primaryField, secondaryField);
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        refresh();
    }

    @Override
    protected void onResult() {
        if (!block.isResponseOk()) {
            getContentContainer().setContent(new Label(I18n.I.noData()));
            return;
        }

        final FinderGroupTable result = this.block.getResult();

        final TableDataModel headTdm = toTableDataModel(result, true);
        final SnippetTableWidget headTableWidget = createTable(
                this.block.getParameter("type"), this.block.getParameter("secondaryField"), // $NON-NLS$
                I18n.I.issuer(), result.getColumn(), headTdm, this.linkListener, false);

        final TableDataModel bodyTdm = toTableDataModel(result, false);
        final SnippetTableWidget tableWidget = createTable(
                this.block.getParameter("type"), this.block.getParameter("secondaryField"), // $NON-NLS$
                I18n.I.issuer(), result.getColumn(), bodyTdm, this.linkListener, true);

        final DockLayoutPanel panel = new DockLayoutPanel(com.google.gwt.dom.client.Style.Unit.PX);
        panel.addStyleName("mm-contentData"); // $NON-NLS-0$
        headTableWidget.addStyleName("mm-emittent-category-headtab"); // $NON-NLS-0$
        if (headTdm.getRowCount() > 0) {
            panel.addNorth(headTableWidget, calcHeight(headTdm));
        }
        final ScrollPanel nonVerticalScrollPanel = new ScrollPanel() {
            @Override
            public void setAlwaysShowScrollBars(boolean alwaysHide) {
                super.setAlwaysShowScrollBars(alwaysHide);
                getScrollableElement().getStyle().setOverflowX(alwaysHide ? Style.Overflow.HIDDEN : Style.Overflow.AUTO);
            }
        };
        nonVerticalScrollPanel.setAlwaysShowScrollBars(true);
        nonVerticalScrollPanel.setWidget(tableWidget);
        panel.add(nonVerticalScrollPanel);
        getContentContainer().setContent(panel);
    }

    private double calcHeight(TableDataModel headTdm) {
        return headTdm.getRowCount() * 17 + 30;
    }
}