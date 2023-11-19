/*
 * OrderBookViewBHLKGS.java
 *
 * Created on 05.09.13 16:12
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PriceWithCurrency;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.table.OrderBookItemPinRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.table.OrderBookMenu;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SortedProxyTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModelBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.pmxml.OrderbookDataTypeBHL;

import java.util.Comparator;
import java.util.List;

/**
 * @author Markus Dick
 */
class OrderBookViewBHLKGS extends Composite implements OrderBookDisplay<OrderbookDataTypeBHL> {
    public static final String ORDER_COLUMN = "o"; //$NON-NLS$
    public static final String TRANSACTION_COLUMN = "t"; //$NON-NLS$
    public static final String WKN_COLUMN = "w"; //$NON-NLS$
    public static final String INSTRUMENT_NAME_COLUMN = "in"; //$NON-NLS$
    public static final String SETTLEMENT_CURRENCY_COLUMN = "sc"; //$NON-NLS$
    public static final String AMOUNT_NOMINAL_COLUMN = "an"; //$NON-NLS$
    public static final String LIMIT_OR_STOP_LIMIT_COLUMN = "losl"; //$NON-NLS$
    public static final String LIMIT_CLAUSE_COLUMN = "lc"; //$NON-NLS$
    public static final String VALIDITY_COLUMN = "v"; //$NON-NLS$
    public static final String STATE_COLUMN = "s"; //$NON-NLS$
    public static final String EXCHANGE_COLUMN = "e"; //$NON-NLS$
    public static final String AMOUNT_NOMINAL_OPEN_COLUMN = "ano"; //$NON-NLS$
    public static final String ENTRY_DATE_COLUMN = "ed"; //$NON-NLS$

    private Presenter<OrderbookDataTypeBHL> presenter;

    private final SortedProxyTableDataModel sortedTableDataModel;
    private final SnippetTableWidget snippetTableWidget;
    private final OrderBookMenu<OrderbookDataTypeBHL> menu;

    public OrderBookViewBHLKGS() {
        final DockLayoutPanel layout = new DockLayoutPanel(Style.Unit.PX);

        layout.setStyleName("mm-contentData");
        initWidget(layout);

        final FloatingToolbar toolbar = new FloatingToolbar(FloatingToolbar.ToolbarHeight.FOR_ICON_SIZE_S);
        layout.addNorth(toolbar, toolbar.getToolbarHeightPixel());

        toolbar.add(Button.icon("as-tool-settings") // $NON-NLS$
                .text(I18n.I.orderEntryBHLKGSOrderBookQueryCriteria())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        presenter.onShowQueryCriteriaClicked(event);
                    }
                }).build());

        this.menu = new OrderBookMenu<>(new OrderBookMenu.Callback<OrderbookDataTypeBHL>() {
            @Override
            public void onDeleteOrderClicked(ClickEvent event, OrderbookDataTypeBHL data) {
                OrderBookViewBHLKGS.this.presenter.onCancelOrderClicked(event, data);
            }

            @Override
            public void onShowOrderClicked(ClickEvent event, OrderbookDataTypeBHL data) {
                OrderBookViewBHLKGS.this.presenter.onShowOrderClicked(event, data);
            }

            @Override
            public void onChangeOrderClicked(ClickEvent event, OrderbookDataTypeBHL data) {
                OrderBookViewBHLKGS.this.presenter.onShowOrderClicked(event, data);
            }
        });

        final LinkListener<OrderbookDataTypeBHL> pinLinkListener = new LinkListener<OrderbookDataTypeBHL>() {
            public void onClick(LinkContext<OrderbookDataTypeBHL> context, Element e) {
                onPinClick(context, e);
            }
        };

        final LinkListener<OrderbookDataTypeBHL> detailsLinkListener = new LinkListener<OrderbookDataTypeBHL>() {
            public void onClick(LinkContext<OrderbookDataTypeBHL> context, Element e) {
                onDetailsClick(context, e);
            }
        };

        final OrderBookItemPinRenderer<OrderbookDataTypeBHL> pinRenderer
                = new OrderBookItemPinRenderer<>(pinLinkListener, detailsLinkListener);

        final TableColumnModelBuilder tb = new TableColumnModelBuilder();
        tb.addColumns(
                new TableColumn(I18n.I.orderEntryOrderNumber(), 90f, ORDER_COLUMN).withRenderer(pinRenderer).withVisibilityCheck(SimpleVisibilityCheck.valueOf(true)),
                new TableColumn(I18n.I.orderEntryBHLKGSContractDate(), 120f, ENTRY_DATE_COLUMN).withRenderer(OeRenderers.PM_DATE_TIME_STRING_TABLE_CELL_RENDERER),
                new TableColumn(I18n.I.orderEntryValidity(), 65f, VALIDITY_COLUMN).withRenderer(OeRenderers.PM_DATE_STRING_TABLE_CELL_RENDERER),
                new TableColumn(I18n.I.orderEntryState(), 80f, STATE_COLUMN),
                new TableColumn(I18n.I.orderEntryTransaction(), 80f , TRANSACTION_COLUMN),
                new TableColumn("WKN", 60f, WKN_COLUMN), //$NON-NLS$
                new TableColumn(I18n.I.orderEntryBHLKGSInstrumentName(), 150f, INSTRUMENT_NAME_COLUMN),
                new TableColumn(I18n.I.orderEntryBHLKGSSettlementCurrencyAbbr(), 70f, SETTLEMENT_CURRENCY_COLUMN),
                new TableColumn(I18n.I.orderEntryAmountNominal(), 100f, AMOUNT_NOMINAL_COLUMN).withRenderer(OeRenderers.PRICE_NOT_EMPTY_OR_ZERO_TABLE_CELL_RENDERER),
                new TableColumn(I18n.I.orderEntryAmountNominalOpen(), 100f, AMOUNT_NOMINAL_OPEN_COLUMN).withRenderer(OeRenderers.PRICE_NOT_EMPTY_OR_ZERO_TABLE_CELL_RENDERER),
                new TableColumn(I18n.I.orderEntryLimitOrStopLimit(), 100f, LIMIT_OR_STOP_LIMIT_COLUMN).withRenderer(OeRenderers.PRICE_WITH_CURRENCY_TABLE_CELL_RENDERER),
                new TableColumn(I18n.I.orderEntryLimitClause(), 80f, LIMIT_CLAUSE_COLUMN),
                new TableColumn(I18n.I.orderEntryExchange(), 150f, EXCHANGE_COLUMN),
                new TableColumn(I18n.I.orderEntryNameOfCustomer(), 180f)
        );
        final TableColumnModel tableColumnModel = tb.asTableColumnModel();

        this.sortedTableDataModel = SortedProxyTableDataModel.create(tableColumnModel,
            new Command() {
                @Override
                public void execute() {
                    OrderBookViewBHLKGS.this.snippetTableWidget.updateData(OrderBookViewBHLKGS.this.sortedTableDataModel);
                }
            },

            new SortedProxyTableDataModel.ValueMapping() {
                @Override
                public String toString(Object value, String columnKey) {
                    if(value == null ) return "";

                    if(value instanceof OrderbookDataTypeBHL) {
                        return ((OrderbookDataTypeBHL) value).getOrderNumber();
                    }

                    if(value instanceof PriceWithCurrency) {
                        return ((PriceWithCurrency) value).getPrice();
                    }

                    if(STATE_COLUMN.equals(columnKey) || INSTRUMENT_NAME_COLUMN.equals(columnKey)) {
                        return value.toString().toUpperCase();
                    }
                    return value.toString();
                }
            },
        new MyComparator<SortedProxyTableDataModel.SortMapEntry>(true), new MyComparator<SortedProxyTableDataModel.SortMapEntry>(false));
        this.sortedTableDataModel.setDelegate(DefaultTableDataModel.NULL);

        this.snippetTableWidget = new SnippetTableWidget(tb.asTableColumnModel());
        this.snippetTableWidget.setSortLinkListener(this.sortedTableDataModel);
        final ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.add(this.snippetTableWidget);
        layout.add(scrollPanel);
        this.snippetTableWidget.updateData(this.sortedTableDataModel);
    }

    private void onPinClick(LinkContext<OrderbookDataTypeBHL> context, Element e) {
        final OrderbookDataTypeBHL data = context.getData();
        this.menu.setShowOrderMenuItemVisible(this.presenter.isShowOrderSupported());
        this.menu.setChangeOrderMenuItemVisible(data.isIsChangeAllowed() && this.presenter.isChangeOrderSupported());
        this.menu.setDeleteOrderMenuItemVisible(data.isIsDeleteAllowed() && this.presenter.isCancelOrderSupported());
        this.menu.show(context.data, e);
    }

    private void onDetailsClick(LinkContext<OrderbookDataTypeBHL> context, Element e) {
        OrderBookViewBHLKGS.this.presenter.onShowOrderClicked(null, context.getData());
    }

    @Override
    public void setEntries(List<OrderbookDataTypeBHL> entries) {
        final TableDataModel tableDataModel = createDataModel(entries, new AbstractRowMapper<OrderbookDataTypeBHL>() {
            @Override
            public Object[] mapRow(OrderbookDataTypeBHL ob) {
                return new Object[]{
                        ob,
                        ob.getOrderDate(),
                        ob.getExpirationDate(),
                        ob.getOrderStatus(),
                        ob.getTransaktionType(),
                        ob.getWKN(),
                        ob.getSecurityName(),
                        ob.getOrderCurrency(),
                        ob.getQuantity(),
                        ob.getUnfilledQuantity(),
                        StringUtil.hasText(ob.getLimit()) ? new PriceWithCurrency(ob.getLimit(), ob.getLimitCurrency()) : null,
                        ob.getLimitOption(),
                        ob.getExchangeName(),
                        ob.getOwnerName()
                };
            }
        });

        this.sortedTableDataModel.setDelegate(tableDataModel);
        this.snippetTableWidget.updateData(this.sortedTableDataModel);
    }

    private TableDataModel createDataModel(List<OrderbookDataTypeBHL> entries, RowMapper<OrderbookDataTypeBHL> mapper) {
        if (entries.isEmpty()) {
            return DefaultTableDataModel.NULL;
        }
        return DefaultTableDataModel.create(entries, mapper);
    }

    @Override
    public void setPresenter(Presenter<OrderbookDataTypeBHL> presenter) {
        this.presenter = presenter;
    }

    @Override
    public String getPrintHtml() {
        return this.snippetTableWidget.getElement().getParentElement().getInnerHTML();
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    private static class MyComparator<T> implements Comparator<T> {
        private static final String NUMBER_REG_EX = "^([+-]?\\d{1,3}(\\d{3})*(\\.\\d*)?)$"; //$NON-NLS$
        private final boolean ascending;

        private MyComparator(boolean ascending) {
            this.ascending = ascending;
        }

        @Override
        public int compare(T t1, T t2) {
            final String s1 = t1 == null ? null : t1.toString();
            final String s2 = t2 == null ? null : t2.toString();
            if (s1 == null) {
                return s2 == null ? 0 : 1;
            }
            else if (s2 == null) {
                return -1;
            }

            final int result = doCompare(s1, s2);

            return this.ascending ? result : -result;
        }

        private int doCompare(String s1, String s2) {
            if(isNumber(s1) && isNumber(s2)) {
                try {
                    final Double d1 = Double.parseDouble(s1);
                    final Double d2 = Double.parseDouble(s2);
                    return d1.compareTo(d2);
                }
                catch(NumberFormatException e) {
                    Firebug.warn("<MyComparator.compare> Failed to parse double", e);
                }
            }
            return s1.compareTo(s2);
        }

        private boolean isNumber(String s) {
            return s.matches(NUMBER_REG_EX);
        }
    }
}
