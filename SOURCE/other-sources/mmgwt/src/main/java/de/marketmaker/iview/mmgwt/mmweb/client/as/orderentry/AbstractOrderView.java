/*
 * AbstractOrderView.java
 *
 * Created on 05.11.12 08:47
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.itools.gwtutil.client.widgets.ImageSelectButton;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.mappers.DepotItemMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.MappedListBox;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.OrderAction;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ZoneDesc;

import java.util.List;
import java.util.Map;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
abstract class AbstractOrderView<P extends DisplayAbstract.PresenterAbstract> extends Composite implements DisplayAbstract<P> {
    /*
     * Do NOT CONFUSE flex table columns and view columns!
     * The view has three columns consisting of label, field and remark,
     * resulting in 9 flex cell columns.
     */
    public static final int COLSPAN_VIEW_COLUMN_2_FIELDS = 3;

    public static final String VIEW_STYLE = "as-oe"; //$NON-NLS$
    public static final String CONTENT_PANEL_STYLE = "cp"; //$NON-NLS$

    public static final String FLEX_TABLE_STYLE = "ft"; //$NON-NLS$
    public static final String SECTION_HEADLINE_CELL_STYLE = "headline"; //$NON-NLS$
    public static final String LABEL_CELL_STYLE = "labels"; //$NON-NLS$
    protected static final String FIELD_CELL_STYLE = "fields"; //$NON-NLS$
    public static final String RIGHT_COLUMNS_CELL_STYLE = "rightcols"; //$NON-NLS$

    public static final String INSTRUMENT_PANEL_STYLE = "si"; //$NON-NLS$

    public static final String SECURITY_DATA_KEY = "data"; //$NON-NLS$
    static final String REMARK_COLUMNS_STYLE="remarkcols"; //$NON-NLS$

    private P presenter;

    protected final Label priceLabel = new Label(I18n.I.price());
    protected final TextBox priceField = new TextBox();
    protected final Label priceDateLabel = new Label(I18n.I.orderEntryPriceDate());
    protected final TextBox priceDateField = new TextBox();

    private final FlowPanel commonPanel = new FlowPanel();
    private final FlowPanel customerPanel = new FlowPanel();

    protected final ListBox transactionTypeChoice = new ListBox();

    protected final ListBox investorChoice = new ListBox();
    protected final TextBox investorNumberField = new TextBox();

    protected final MappedListBox<Depot> depotChoice = new MappedListBox<Depot>();
    protected final TextBox depotBankNameField = new TextBox();
    protected final TextBox depotNumberField = new TextBox();

    protected final ListBox accountChoice = new ListBox();
    protected final TextBox accountNumberField = new TextBox();
    protected final TextBox accountBalanceField = new TextBox();

    protected final TextBox instrumentField = new TextBox();
    protected final TextBox instrumentTypeField = new TextBox();
    protected final TextBox isinField = new TextBox();
    protected final TextBox wknField = new TextBox();
    protected final ListBox exchangeChoice = new ListBox();
    protected final ListBox exchangeCurrencyChoice = new ListBox();
    protected final ImageButton selectInstrumentButton =
            GuiUtil.createImageButton("mm-icon-finder", null, null, null); //$NON-NLS$

    protected final Menu menu = new Menu();
    protected final ImageSelectButton selectInstrumentFromDepotChoice =
            new ImageSelectButton(IconImage.get("pm-investor-depot").createImage(), null, null, true).withMenu(menu); //$NON-NLS$

    protected final SelectionDelegate delegate = new SelectionDelegate();

    protected static class SelectionDelegate extends Composite implements HasSelectionHandlers<OrderSecurityInfo> {
        @Override
        public HandlerRegistration addSelectionHandler(SelectionHandler<OrderSecurityInfo> securityDataSelectionHandler) {
            return addHandler(securityDataSelectionHandler, SelectionEvent.getType());
        }
    }

    public AbstractOrderView() {
        initContent();
        initHandlers();

        final FlowPanel contentPanel = new FlowPanel();
        contentPanel.setStyleName(CONTENT_PANEL_STYLE);
        contentPanel.add(this.commonPanel);
        contentPanel.add(this.customerPanel);

        FlowPanel layout = new FlowPanel();
        layout.setStyleName(VIEW_STYLE);
        layout.add(contentPanel);

        initWidget(layout);
    }

    @Override
    public void setPresenter(P presenter) {
        this.presenter = presenter;
    }

    public P getPresenter() {
        return presenter;
    }

    private void initContent() {
        final FlexTable table = new FlexTable();
        table.setStyleName(FLEX_TABLE_STYLE);
        final FlexTable.FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
        this.commonPanel.add(table);

        int c = 0;
        final int lcol1 = c++;
        final int fcol1 = c++;
        final int lcol2 = c++;
        final int fcol2 = c++;
        final int lcol3 = c++;
        final int fcol3 = c++;
        final int colCount = c;
        int row = 0;

        addLabel(I18n.I.orderEntryTransaction(), table, cellFormatter, row, lcol1);
        this.transactionTypeChoice.setVisibleItemCount(1);
        table.setWidget(row, fcol1, this.transactionTypeChoice);
        cellFormatter.setStyleName(row, fcol1, FIELD_CELL_STYLE);

        //TODO: DO WE NEED THIS? The name of the bank is already available in the title bar.
//        table.setWidget(row, lcol2, new Label(I18n.I.orderEntryDepotBank()));
//        this.depotBankNameField.setReadOnly(true);
//        table.setWidget(row, fcol2, this.depotBankNameField);
//        cellFormatter.setColSpan(row, fcol2, COLSPAN_VIEW_COLUMN_2_FIELDS);
//        cellFormatter.setStyleName(row, lcol2, RIGHT_COLUMNS_CELL_STYLE);

        //depot and account section
        //Select the session is bound to a depot, so we do not support to change the depot of a running session
        table.setWidget(++row, lcol1, new Label(I18n.I.portfolio()));
        cellFormatter.setStyleName(row, lcol1, SECTION_HEADLINE_CELL_STYLE);
        cellFormatter.setColSpan(row, lcol1, colCount);

        addLabel(I18n.I.pmInvestor(), table, cellFormatter, ++row, lcol1);
        table.setWidget(row, fcol1, this.investorChoice);
        cellFormatter.setStyleName(row, fcol1, FIELD_CELL_STYLE);

        addLabel(I18n.I.investorNumberAbbr(), table, cellFormatter, row, lcol2);
        cellFormatter.addStyleName(row, lcol2, RIGHT_COLUMNS_CELL_STYLE);
        this.investorNumberField.setReadOnly(true);
        table.setWidget(row, fcol2, this.investorNumberField);
        cellFormatter.setColSpan(row, fcol2, COLSPAN_VIEW_COLUMN_2_FIELDS);
        cellFormatter.setStyleName(row, fcol2, FIELD_CELL_STYLE);

        this.depotChoice.setItemMapper(new DepotItemMapper());
        addLabel(I18n.I.pmDepot(), table, cellFormatter, ++row, lcol1);
        table.setWidget(row, fcol1, this.depotChoice);
        cellFormatter.setStyleName(row, fcol1, FIELD_CELL_STYLE);

        addLabel(I18n.I.orderEntryDepotNo(), table, cellFormatter, row, lcol2);
        cellFormatter.addStyleName(row, lcol2, RIGHT_COLUMNS_CELL_STYLE);
        this.depotNumberField.setReadOnly(true);
        table.setWidget(row, fcol2, this.depotNumberField);
        cellFormatter.setColSpan(row, fcol2, COLSPAN_VIEW_COLUMN_2_FIELDS);
        cellFormatter.setStyleName(row, fcol2, FIELD_CELL_STYLE);

        addLabel(I18n.I.orderEntryAccount(), table, cellFormatter, ++row, lcol1);
        table.setWidget(row, fcol1, this.accountChoice);

        addLabel(I18n.I.accountNo(), table, cellFormatter, row, lcol2);
        cellFormatter.addStyleName(row, lcol2, RIGHT_COLUMNS_CELL_STYLE);
        this.accountNumberField.setReadOnly(true);
        table.setWidget(row, fcol2, this.accountNumberField);
        cellFormatter.setColSpan(row, fcol2, COLSPAN_VIEW_COLUMN_2_FIELDS);
        cellFormatter.setStyleName(row, fcol2, FIELD_CELL_STYLE);

        addLabel(I18n.I.orderEntryAccountBalance(), table, cellFormatter, ++row, lcol2);
        cellFormatter.addStyleName(row, lcol2, RIGHT_COLUMNS_CELL_STYLE);
        this.accountBalanceField.setReadOnly(true);
        table.setWidget(row, fcol2, this.accountBalanceField);
        cellFormatter.setColSpan(row, fcol2, COLSPAN_VIEW_COLUMN_2_FIELDS);
        cellFormatter.setStyleName(row, fcol2, FIELD_CELL_STYLE);

        //Select instrument and market section
        table.setWidget(++row, lcol1, new Label(I18n.I.instrument()));
        cellFormatter.setStyleName(row, lcol1, SECTION_HEADLINE_CELL_STYLE);
        cellFormatter.setColSpan(row, lcol1, colCount);

        addLabel(I18n.I.instrument(), table, cellFormatter, ++row, lcol1);
        final Panel instrumentPanel = new HorizontalPanel();
        instrumentPanel.setStyleName(INSTRUMENT_PANEL_STYLE);

        this.instrumentField.setReadOnly(false);
        instrumentPanel.add(this.instrumentField);
        instrumentPanel.add(this.selectInstrumentButton);
        this.instrumentField.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    AbstractOrderView.this.instrumentField.setFocus(false);
                }
            }
        });

        this.selectInstrumentFromDepotChoice.setClickOpensMenu(true);
        this.selectInstrumentFromDepotChoice.addSelectionHandler(new SelectionHandler<MenuItem>() {
            @Override
            public void onSelection(SelectionEvent<MenuItem> menuItemSelectionEvent) {
                final MenuItem item = menuItemSelectionEvent.getSelectedItem();
                final OrderSecurityInfo data = (OrderSecurityInfo)item.getData(SECURITY_DATA_KEY);
                SelectionEvent.fire(AbstractOrderView.this.delegate, data);
                AbstractOrderView.this.menu.setSelectedItem(null);
            }
        });
        instrumentPanel.add(this.selectInstrumentFromDepotChoice);
        table.setWidget(row, fcol1, instrumentPanel);
        cellFormatter.setStyleName(row, fcol1, FIELD_CELL_STYLE);

        //TODO: Ob das Feature "Bestandsliste des Investors" verfügbar ist, oder nicht
        //TODO: soll zukünftig über einen FeatureDescriptor mitgeteilt werden.

        addLabel(I18n.I.type(), table, cellFormatter, row, lcol2);
        cellFormatter.addStyleName(row, lcol2, RIGHT_COLUMNS_CELL_STYLE);
        this.instrumentTypeField.setReadOnly(true);
        table.setWidget(row, fcol2, this.instrumentTypeField);
        cellFormatter.setColSpan(row, fcol2, COLSPAN_VIEW_COLUMN_2_FIELDS);
        cellFormatter.setStyleName(row, fcol2, FIELD_CELL_STYLE);

        addLabel(I18n.I.orderEntryExchange(), table, cellFormatter, ++row, lcol1);
        this.exchangeChoice.setVisibleItemCount(1);
        this.exchangeChoice.setEnabled(false);
        table.setWidget(row, fcol1, this.exchangeChoice);
        cellFormatter.setStyleName(row, fcol1, FIELD_CELL_STYLE);

        addLabel("ISIN", table, cellFormatter, row, lcol2); //$NON-NLS$
        cellFormatter.addStyleName(row, lcol2, RIGHT_COLUMNS_CELL_STYLE);
        this.isinField.setReadOnly(true);
        this.isinField.setMaxLength(12);
        this.isinField.setVisibleLength(12);
        table.setWidget(row, fcol2, this.isinField);

        addLabel("WKN", table, cellFormatter, row, lcol3); //$NON-NLS$
        this.wknField.setReadOnly(true);
        table.setWidget(row, fcol3, this.wknField);

        addLabel(I18n.I.orderEntryExchangeCurrency(), table, cellFormatter, ++row, lcol1);
        this.exchangeCurrencyChoice.setVisibleItemCount(1);
        this.exchangeCurrencyChoice.setEnabled(false);
        table.setWidget(row, fcol1, this.exchangeCurrencyChoice);
        cellFormatter.setStyleName(row, fcol1, FIELD_CELL_STYLE);

        addLabel(this.priceLabel, table, cellFormatter, row, lcol2);
        cellFormatter.addStyleName(row, lcol2, RIGHT_COLUMNS_CELL_STYLE);
        this.priceField.setReadOnly(true);
        table.setWidget(row, fcol2, this.priceField);

        addLabel(this.priceDateLabel, table, cellFormatter, row, lcol3);
        this.priceDateField.setReadOnly(true);
        table.setWidget(row, fcol3, this.priceDateField);
    }

    private void initHandlers() {
        this.accountChoice.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                AbstractOrderView.this.presenter.onAccountChanged(event);
            }
        });

        this.transactionTypeChoice.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                AbstractOrderView.this.presenter.onOrderActionChanged(event);
            }
        });

        this.selectInstrumentButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AbstractOrderView.this.presenter.onSymbolSearchButtonClicked(event);
            }
        });

        this.delegate.addSelectionHandler(new SelectionHandler<OrderSecurityInfo>() {
            @Override
            public void onSelection(SelectionEvent<OrderSecurityInfo> securityDataSelectionEvent) {
                AbstractOrderView.this.presenter.onSelectSymbolFromDepotSelected(securityDataSelectionEvent);
            }
        });

        this.instrumentField.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                AbstractOrderView.this.presenter.onSymbolSearchTextBoxValueChanged(event);
            }
        });

        this.exchangeChoice.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                AbstractOrderView.this.presenter.onExchangeChangedHandler(event);
            }
        });

        this.exchangeCurrencyChoice.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                AbstractOrderView.this.presenter.onExchangeCurrencyChangedHandler(event);
            }
        });

        this.investorChoice.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                AbstractOrderView.this.presenter.onInvestorChoiceChanged(event);
            }
        });

        this.depotChoice.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                AbstractOrderView.this.presenter.onDepotsSelectedItemChanged(event);
            }
        });
    }

    protected void addLabel(String labelText, FlexTable table, FlexTable.FlexCellFormatter cellFormatter, int row, int col) {
        addLabel(new Label(labelText), table, cellFormatter, row, col);
    }

    protected void addLabel(Label label, FlexTable table, FlexTable.FlexCellFormatter cellFormatter, int row, int col) {
        table.setWidget(row, col, label);
        cellFormatter.setStyleName(row, col, LABEL_CELL_STYLE);
    }

    protected void addWidgetToCustomerPanel(Widget widget) {
        this.customerPanel.add(widget);
    }

    protected void addWidgetToCommonPanel(Widget widget) {
        this.commonPanel.add(widget);
    }

    @Override
    public void setSelectedOrderAction(OrderAction action) {
        final OrderActionType orderActionType = action.getValue();

        for (int i = 0; i < this.transactionTypeChoice.getItemCount(); i++) {
            if (orderActionType.name().equals(this.transactionTypeChoice.getValue(i))) {
                this.transactionTypeChoice.setSelectedIndex(i);
            }
        }
    }

    @Override
    public OrderAction getSelectedOrderAction() {
        final int selectedIndex = this.transactionTypeChoice.getSelectedIndex();
        final String orderActionTypeName = this.transactionTypeChoice.getValue(selectedIndex);

        final OrderAction orderAction = new OrderAction();
        orderAction.setValue(OrderActionType.valueOf(orderActionTypeName));

        return orderAction;
    }

    @Override
    public void setOrderActions(List<OrderAction> actions) {
        this.transactionTypeChoice.clear();

        for(OrderAction action : actions) {
            final String value = action.getValue().name();
            final String label = OeRenderers.ORDER_ACTION_RENDERER.render(action);
            this.transactionTypeChoice.addItem(label, value);
        }
    }

    @Override
    public Widget getOrderView() {
        return this;
    }

    @Override
    public void setInstrumentName(String name) {
        this.instrumentField.setText(name);
    }

    @Override
    public void setInstrumentType(ShellMMType type) {
        this.instrumentTypeField.setText(PmRenderers.SHELL_MM_TYPE.render(type));
    }

    @Override
    public void setIsin(String isin) {
        this.isinField.setText(isin);
    }

    @Override
    public void setWkn(String wkn) {
        this.wknField.setText(wkn);
    }

    @Override
    public void setExchangeChoiceEnabled(boolean enable) {
        this.exchangeChoice.setEnabled(enable);
    }

    @Override
    public void setExchangeChoices(List<OrderExchangeInfo> exchangeInfoList) {
        this.exchangeChoice.clear();

        for(OrderExchangeInfo exchangeInfo : exchangeInfoList) {
            final String value;
            if(exchangeInfo.isUseExtern()) {
                value = exchangeInfo.getExternRef();
            }
            else {
                value = exchangeInfo.getID();
            }

            this.exchangeChoice.addItem(OeRenderers.ORDER_EXCHANGE_INFO_RENDERER.render(exchangeInfo), value);
        }
    }

    @Override
    public void setExchangeCurrencyChoiceEnabled(boolean enable) {
        this.exchangeCurrencyChoice.setEnabled(enable);
    }

    @Override
    public void setExchangeCurrencyChoices(List<CurrencyAnnotated> exchangeCurrencyList) {
        exchangeCurrencyChoice.clear();

        for(CurrencyAnnotated currency : exchangeCurrencyList) {
            String label = currency.getCurrency().getKuerzel();
            //Add current holdings info from PSI Transaction Data
            if(StringUtil.hasText(currency.getExtInfo())) {
                label += " " + currency.getExtInfo();
            }
            this.exchangeCurrencyChoice.addItem(label, currency.getCurrency().getId());
        }
    }

    @Override
    public int getSelectedExchangeCurrency() {
        return this.exchangeCurrencyChoice.getSelectedIndex();
    }

    @Override
    public void setSelectedExchangeCurrency(int currencyIndex) {
        this.exchangeCurrencyChoice.setSelectedIndex(currencyIndex);
    }

    @Override
    public int getSelectedExchangeChoice() {
        return this.exchangeChoice.getSelectedIndex();
    }

    @Override
    public void setSelectedExchangeChoice(int exchangeIndex) {
        this.exchangeChoice.setSelectedIndex(exchangeIndex);
    }

    @Override
    public void setDepotNo(String depotNo) {
        this.depotNumberField.setValue(depotNo);
    }

    @Override
    public void setDepotBankName(String depotBankName) {
        this.depotBankNameField.setValue(depotBankName);
    }

    @Override
    public void setAccountNo(String accountNo) {
        this.accountNumberField.setValue(accountNo);
    }

    @Override
    public void setAccountBalance(String accountBalance, String currencySymbol) {
        final String text = Renderer.PRICE23.render(accountBalance) + " " + currencySymbol;
        this.accountBalanceField.setValue(text);
    }

    @Override
    public int getAccountsSelectedItem() {
        return this.accountChoice.getSelectedIndex();
    }

    @Override
    public void setAccountsSelectedItem(int accountIndex) {
        this.accountChoice.setSelectedIndex(accountIndex);
    }

    @Override
    public void setAccounts(List<AccountData> accounts) {
        this.accountChoice.clear();
        for(AccountData account : accounts) {
            final String value = account.getId();
            final String item = account.getName() + " (" + account.getCurrency().getKuerzel() + ")"; //$NON-NLS$
            this.accountChoice.addItem(item, value);
        }
    }

    @Override
    public void setInvestorNo(String investorNo) {
        this.investorNumberField.setValue(investorNo);
    }

    @Override
    public void setSymbolsOfDepot(List<OrderSecurityInfo> securityDataList) {
        this.menu.removeAll();

        if(securityDataList == null) return;

        for(OrderSecurityInfo item : securityDataList) {
            MenuItem menuItem = new MenuItem(item.getBezeichnung() + " (" + item.getISIN() +")"); //$NON-NLS$
            menuItem.setEnabled(true);
            menuItem.addStyleName(VIEW_STYLE);
            menuItem.setData(SECURITY_DATA_KEY, item);
            this.menu.add(menuItem);
        }
    }

    @Override
    public void setPrice(String price) {
        this.priceField.setText(price);
    }

    @Override
    public void setPriceDate(String date) {
        this.priceDateField.setText(date);
    }

    @Override
    public void reset() {
        Firebug.log("reset");
        this.transactionTypeChoice.clear();

        this.investorNumberField.setText(null);
        this.depotNumberField.setText(null);
        this.depotBankNameField.setText(null);

        this.accountChoice.clear();
        this.accountNumberField.setText(null);
        this.accountBalanceField.setText(null);

        this.instrumentField.setText(null);
        this.instrumentTypeField.setText(null);
        this.menu.removeAll();
        this.isinField.setText(null);
        this.wknField.setText(null);
        this.exchangeChoice.clear();
        this.setExchangeChoiceEnabled(false);
        this.exchangeCurrencyChoice.clear();
        this.setExchangeCurrencyChoiceEnabled(false);
        this.priceField.setText(null);
        this.priceDateField.setText(null);
    }

    @Override
    public void setCurrencyLabels(String currentCurrency) {
        /* nothing to do here */
    }

    @Override
    public void setSelectSymbolFromDepotEnabled(boolean enabled) {
        this.selectInstrumentFromDepotChoice.setEnabled(enabled);
    }

    @Override
    public void setInvestors(List<ShellMMInfo> investors, Map<String, ZoneDesc> zoneDescs) {
        this.investorChoice.clear();
        if(zoneDescs == null || zoneDescs.isEmpty()) {
            for(ShellMMInfo investor : investors) {
                this.investorChoice.addItem(investor.getBezeichnung(), investor.getId());
            }

            return;
        }

        for(ShellMMInfo investor : investors) {
            final String label;
            final ZoneDesc zoneDesc = zoneDescs.get(investor.getPrimaryZoneId());
            if(zoneDesc == null) {
                label = investor.getBezeichnung();
            }
            else {
                label = investor.getBezeichnung() + I18n.I.zoneNameSuffix(zoneDesc.getName());
            }
            this.investorChoice.addItem(label, investor.getId());
        }

    }

    @Override
    public void setDepots(List<Depot> depots) {
        this.depotChoice.setItems(depots);
    }

    @Override
    public void setInvestorsSelectedIndex(int index) {
        this.investorChoice.setSelectedIndex(index);
    }

    @Override
    public String getSelectedInvestorId() {
        return this.investorChoice.getValue(this.investorChoice.getSelectedIndex());
    }

    @Override
    public void setDepotsSelectedItem(Depot item) {
        this.depotChoice.setSelectedItem(item);
    }

    @Override
    public Depot getDepotsSelectedItem() {
        return this.depotChoice.getSelectedItem();
    }

    public String getDepotsSelectedId() {
        return this.depotChoice.getValue(this.depotChoice.getSelectedIndex());
    }

    @Override
    public void setInvestorsEnabled(boolean enabled) {
        this.investorChoice.setEnabled(enabled);
    }

    @Override
    public void setDepotsEnabled(boolean enabled) {
        this.depotChoice.setEnabled(enabled);
    }
}