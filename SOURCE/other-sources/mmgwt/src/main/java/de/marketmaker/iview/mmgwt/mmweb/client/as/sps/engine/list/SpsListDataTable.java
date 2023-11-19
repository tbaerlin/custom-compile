package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.itools.gwtutil.client.widgets.TableUtil;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.chart.SelectableChart;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.event.SpsHoverEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DataItemFormatter;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmPlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.MainInput;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ListWidgetDescColumn;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.TiType;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * @see de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.SpsListDataTableTestMock
 * Author: umaurer
 * Created: 24.07.14
 */
public class SpsListDataTable<P extends SpsProperty> extends SpsBoundWidget<FlexTable, P> implements NoValidationPopup {
    private final ListWidgetDescColumn[] columns;
    private final String[] columnHeaders;
    private final List<String> columnFieldNames;
    private final DataItemFormatter[] columnFormatters;
    private final String[] cellStyles;
    private final SpsListBindFeature spsListBindFeature;
    private final SpsListHoverHandler hoverHandler;
    private HandlerRegistration hoverHandlerRegistration;

    private boolean renderShellMMInfoLinks;
    private String historyContextName;
    private MainInput mainInput;

    public SpsListDataTable(Context context, BindToken parentToken, BindToken itemsBindToken, List<ListWidgetDescColumn> columns) {
        this.spsListBindFeature = new SpsListBindFeature(context, parentToken, itemsBindToken) {
            @Override
            public void onChange() {
                onItemsChange();
            }
        };
        int columnCount = columns.size();
        this.columns = new ListWidgetDescColumn[columnCount];
        this.columnHeaders = new String[columnCount];
        this.columnFormatters = new DataItemFormatter[columnCount];
        this.cellStyles = new String[columnCount];
        this.columnFieldNames = new ArrayList<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
            this.columns[i] = columns.get(i);
            this.columnHeaders[i] = columns.get(i).getColumnName();
            final String fieldName = this.columns[i].getFieldName();
            this.columnFieldNames.add(fieldName);
            final ParsedTypeInfo pti = SpsUtil.getListCellDecl(context, itemsBindToken, fieldName).getDescription();
            this.columnFormatters[i] = new DataItemFormatter(pti, this.columns[i].getColumnStyle()).withTrailingZeros(true);
            this.cellStyles[i] = getCellStyles(this.columns[i].getColumnStyle(), pti.getTypeId());
        }
        this.hoverHandler = new SpsListHoverHandler(this, itemsBindToken, this.columnFieldNames);
        this.hoverHandlerRegistration = EventBusRegistry.get().addHandler(SpsHoverEvent.getType(), this.hoverHandler);
    }

    private String getCellStyles(String styles, TiType tiType) {
        if (CssUtil.hasStyle(styles, "mm-left", "mm-center", "mm-right")) { // $NON-NLS$
            return styles;
        }
        switch (tiType) {
            case TI_NUMBER:
                return styles == null ? "mm-right" : (styles + " mm-right"); // $NON-NLS$
            case TI_BOOLEAN:
                return styles == null ? "mm-center" : (styles + " mm-center"); // $NON-NLS$
        }
        return styles;
    }

    @Override
    public void release() {
        super.release();
        if (this.hoverHandlerRegistration != null) {
            this.hoverHandlerRegistration.removeHandler();
            this.hoverHandlerRegistration = null;
        }
    }

    private void onItemsChange() {
        boolean warnedEntryColorAndMiniBar = false;
        final FlexTable table = getWidget();
        table.removeAllRows();
        final HTMLTable.ColumnFormatter columnFormatter = table.getColumnFormatter();
        for (int col = 0; col < this.cellStyles.length; col++) {
            final Integer width = CssUtil.getStyleValueInt(this.cellStyles[col], "width"); // $NON-NLS$
            if (width != null) {
                columnFormatter.getElement(col).getStyle().setWidth(width, PX);
            }
        }
        TableUtil.setTableHeaders(table, this.columnHeaders, this.cellStyles);
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        final boolean entryColor = hasStyle("entryColor"); // $NON-NLS$
        final SpsListProperty itemListProperty = this.spsListBindFeature.getSpsProperty();
        for (SpsProperty spsProperty : itemListProperty.getChildren()) {
            if (!(spsProperty instanceof SpsGroupProperty)) {
                throw new IllegalStateException("not group property: " + spsProperty.getBindToken()); // $NON-NLS$
            }
            final SpsGroupProperty gp = (SpsGroupProperty) spsProperty;
            final int row = table.getRowCount();
            for (int col = 0; col < this.columnHeaders.length; col++) {
                final SpsLeafProperty lp = (SpsLeafProperty) gp.get(this.columns[col].getFieldName());
                final MM dataItem = lp.getDataItem();

                final String columnText = this.columnFormatters[col].format(dataItem);
                if (entryColor && col == 0) {
                    final SafeHtmlBuilder sb = new SafeHtmlBuilder();
                    sb.appendHtmlConstant("<div class=\"color color-" + (row % 12) + "\">&nbsp;</div>");
                    if (columnText == null) {
                        sb.appendHtmlConstant(StringUtility.NULL_FORMATTED);
                    } else if (columnText.startsWith("<") && columnText.endsWith(">")) {
                        if (!warnedEntryColorAndMiniBar && CssUtil.hasStyle(this.columns[col].getColumnStyle(), "miniBar")) {   // $NON-NLS$
                            DebugUtil.showDeveloperNotification("WARNING - Column style 'miniBar' should not be applied for the first column of a SpsListDataTable in style 'entryColor'.");
                            warnedEntryColorAndMiniBar = true;
                        }
                        sb.appendHtmlConstant(columnText);
                    } else {
                        sb.appendEscaped(columnText);
                    }
                    if (this.renderShellMMInfoLinks && lp.isShellMMInfo() && dataItem instanceof ShellMMInfo && canGoTo((ShellMMInfo) dataItem)) {
                        table.setWidget(row, col, createLink(lp, (ShellMMInfo) dataItem, sb.toSafeHtml()));
                    } else {
                        table.setHTML(row, col, sb.toSafeHtml());
                    }
                } else {
                    if (this.renderShellMMInfoLinks && lp.isShellMMInfo() && dataItem instanceof ShellMMInfo && canGoTo((ShellMMInfo) dataItem)) {
                        final SafeHtml linkHtml;
                        if (columnText == null) {
                            linkHtml = SafeHtmlUtils.EMPTY_SAFE_HTML;
                        } else if (columnText.startsWith("<") && columnText.endsWith(">")) {
                            linkHtml = SafeHtmlUtils.fromTrustedString(columnText);
                        } else {
                            linkHtml = SafeHtmlUtils.fromString(columnText);
                        }
                        table.setWidget(row, col, createLink(lp, (ShellMMInfo) dataItem, linkHtml));
                    } else if (columnText != null && columnText.startsWith("<") && columnText.endsWith(">")) {
                        table.setHTML(row, col, columnText);
                    } else {
                        final Integer width = CssUtil.getStyleValueInt(this.cellStyles[col], "width"); // $NON-NLS$
                        if (width != null) {
                            final Label label = new Label(columnText);
                            label.setStyleName("overflowCompletion");
                            label.getElement().getStyle().setWidth(width, PX);
                            Tooltip.addAutoCompletion(label);
                            table.setWidget(row, col, label);
                        }
                        else {
                            table.setText(row, col, columnText);
                        }
                    }
                }
                if (this.cellStyles[col] != null) {
                    formatter.setStyleName(row, col, this.cellStyles[col]);
                }
            }
        }
    }

    private boolean canGoTo(ShellMMInfo value) {
        if(this.mainInput == null) {
            return PmPlaceUtil.canGoTo(value);
        }
        final ShellMMInfo shellMMInfo = this.mainInput.asShellMMInfo();
        return shellMMInfo != null
                && !(MmTalkHelper.isSameShellMMType(shellMMInfo, value) && MmTalkHelper.equals(TiType.TI_SHELL_MM, shellMMInfo, value))
                && PmPlaceUtil.canGoTo(value);
    }

    private HTML createLink(final SpsLeafProperty lp, final ShellMMInfo dataItem, SafeHtml columnText) {
        final HTML html = new HTML(columnText);
        html.addStyleName("mm-link");
        html.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (StringUtil.hasText(historyContextName)) {
                    PmPlaceUtil.goTo(dataItem, SpsUtil.extractDistinctShellMMInfoHistoryContext(historyContextName, lp));
                } else {
                    PmPlaceUtil.goTo(dataItem);
                }
            }
        });
        return html;
    }

    class SelectableTable implements SelectableChart {
        private final HTMLTable.RowFormatter rowFormatter;
        private final FlexTable.FlexCellFormatter cellFormatter;

        SelectableTable(FlexTable table) {
            this.rowFormatter = table.getRowFormatter();
            this.cellFormatter = table.getFlexCellFormatter();
        }

        @Override
        public void setSelectedValue(Index index) {
            if (index.isSelected()) {
                this.rowFormatter.addStyleName(index.getEntryIndex(), "hover");
                if (index.getValueIndex() != -1) {
                    this.cellFormatter.addStyleName(index.getEntryIndex(), index.getValueIndex(), "hover");
                }
            } else {
                this.rowFormatter.removeStyleName(index.getEntryIndex(), "hover");
                if (index.getValueIndex() != -1) {
                    this.cellFormatter.removeStyleName(index.getEntryIndex(), index.getValueIndex(), "hover");
                }
            }
        }
    }

    @Override
    public void onPropertyChange() {
        // ignore
    }

    class TableHoverHandler implements MouseOverHandler, MouseMoveHandler, MouseOutHandler {
        @SuppressWarnings("unused")
        private final Node tableNode;
        private final FlexTable table;
        private final SelectableTable selectable;
        private Element lastTd = null;

        public TableHoverHandler(FlexTable table, SelectableTable selectable) {
            this.table = table;
            this.selectable = selectable;
            this.tableNode = table.getElement();
        }

        @Override
        public void onMouseOver(MouseOverEvent event) {
            maybeSendHoverEvent(TableUtil.getCellElement(this.table, event));
        }

        @Override
        public void onMouseMove(MouseMoveEvent event) {
            maybeSendHoverEvent(TableUtil.getCellElement(this.table, event));
        }

        private void maybeSendHoverEvent(Element td) {
            if (this.lastTd == td) {
                return;
            }
            if (this.lastTd != null) {
                final TableUtil.CellIndex cellIndex = TableUtil.getCellIndex(getWidget(), this.lastTd);
                if (cellIndex != null) {
//                    EventBusRegistry.get().fireEvent(new SpsHoverEvent(SpsListDataTable.this, getBindKeys(cellIndex), false));
                    this.selectable.setSelectedValue(new SelectableChart.Index(cellIndex.getRow(), cellIndex.getColumn(), false));
                }
            }
            this.lastTd = td;
            if (this.lastTd != null) {
                final TableUtil.CellIndex cellIndex = TableUtil.getCellIndex(getWidget(), this.lastTd);
                if (cellIndex != null) {
//                EventBusRegistry.get().fireEvent(new SpsHoverEvent(SpsListDataTable.this, getBindKeys(cellIndex), true));
                    this.selectable.setSelectedValue(new SelectableChart.Index(cellIndex.getRow(), cellIndex.getColumn(), true));
                }
            }
        }

        @Override
        public void onMouseOut(MouseOutEvent event) {
            maybeSendHoverEvent(null);
        }
    }

    private String getBindKeys(TableUtil.CellIndex index) {
        final SpsGroupProperty spsPropertyEntry = (SpsGroupProperty) this.spsListBindFeature.getSpsProperty().get(index.getRow());
        final SpsProperty spsPropertyValue = spsPropertyEntry.get(this.columnFieldNames.get(index.getColumn()));
        return spsPropertyValue.getBindToken().toString();
    }

    @Override
    protected FlexTable createWidget() {
        final FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("mm-snippetTable sps-dataTable");
        final SelectableTable selectable = new SelectableTable(table);
        this.hoverHandler.setSelectable(selectable);

        final TableHoverHandler tableHoverHandler = new TableHoverHandler(table, selectable);
        table.addDomHandler(tableHoverHandler, MouseOverEvent.getType());
        table.addDomHandler(tableHoverHandler, MouseMoveEvent.getType());
        table.addDomHandler(tableHoverHandler, MouseOutEvent.getType());
        return table;
    }

    public SpsListDataTable withShellMMInfoLink(String historyContextName) {
        this.renderShellMMInfoLinks = true;
        this.historyContextName = historyContextName;
        return this;
    }

    public SpsListDataTable withMainInput(MainInput mainInput) {
        this.mainInput = mainInput;
        return this;
    }
}