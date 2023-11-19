/*
 * EditOrderView.java
 *
 * Created on 01.10.2008 10:38:01
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBoxBase;

import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DateBox;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCPriceDatas;
import de.marketmaker.iview.dmxml.MSCPriceDatasElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QwiPosition;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceDataType;
import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.FormValidationHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NumberUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PriceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.Caption;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ReadonlyField;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.TextBox;

/**
 * @author Michael Lösch
 */
class EditOrderView {

    private static final String UNIT = "UNIT"; // $NON-NLS$

    public static final String ICE_W90 = "w90"; // $NON-NLS$

    private final SelectButton marketButton;

    private final ReadonlyField marketField;

    private final DialogIfc dialog;

    private String currency;

    private String exchangeRate = "1"; // $NON-NLS$

    private final FormValidationHandler formValidHandler;

    private final HTML changeAbsoluteHtml;

    private final HTML changePercentHtml;

    private final DateBox dateDateBox;

    private final Label errorLabel;

    private final Label infoLabel;

    private final ReadonlyField instrumentField;

    private final ReadonlyField isinField;

    private final ReadonlyField lastPriceField;

    private final Caption amountCaption;

    private final Caption priceCaption;

    private final Caption chargeCaption;

    private final ReadonlyField typeField;

    private final ReadonlyField wknField;

    private final ReadonlyField yesterdayPriceField;

    private final Menu marketMenu;

    private String amountCaptionText;

    private String priceCaptionText;

    private String quoteId;

    // count or amount [currency]
    private final TextBox amountTextBox;

    private final TextBox priceTextBox;

    private final TextBox chargeTextBox;

    private boolean valid = true;

    private EditOrderController controller;

    static void show(EditOrderController controller, MSCPriceDatas datas, Date date, String price,
            String amount, String charge) {
        final EditOrderView view = new EditOrderView(controller);
        view.updateView(datas, date, price, amount, charge);
        view.validateForm();
    }

    static void show(EditOrderController controller, QwiPosition position, Date date, String price,
            String amount, String charge) {
        final EditOrderView view = new EditOrderView(controller);
        view.updateView(position, date, price, amount, charge);
        view.validateForm();
    }

    private EditOrderView(final EditOrderController controller) {
        this.controller = controller;

        final Caption portfolioCaption = new Caption(amendLabel(I18n.I.portfolio()));
        final Caption dateCaption = new Caption(amendLabel(I18n.I.date()));
        final Caption instrumentCaption = new Caption(amendLabel(I18n.I.instrument()));
        final Caption isinCaption = new Caption(amendLabel("ISIN"));  // $NON-NLS$
        final Caption wknCaption = new Caption(amendLabel("WKN"));  // $NON-NLS$
        final Caption marketCaption = new Caption(amendLabel(I18n.I.marketName()));
        final Caption typeCaption = new Caption(amendLabel(I18n.I.type()));
        final Caption lastPriceCaption = new Caption(amendLabel(I18n.I.last()));
        final Caption yesterdayPriceCaption = new Caption(amendLabel(I18n.I.previousDay()));
        final Caption changeAbsoluteCaption = new Caption(amendLabel("+/-"));  // $NON-NLS$
        final Caption changePercentCaption = new Caption(amendLabel("+/- %"));  // $NON-NLS$
        this.priceCaption = new Caption();
        this.amountCaption = new Caption();
        this.chargeCaption = new Caption();

        final ReadonlyField portfolioField = new ReadonlyField(this.controller.getElement().getName());
        this.dateDateBox = new DateBox();
        this.instrumentField = new ReadonlyField();
        this.isinField = new ReadonlyField();
        this.wknField = new ReadonlyField();
        this.marketMenu = new Menu();
        this.marketButton = new SelectButton(SessionData.isAsDesign() ? Button.RendererType.SPAN : Button.RendererType.TABLE).withMenu(this.marketMenu).withClickOpensMenu();
        Styles.tryAddStyles(this.marketButton, Styles.get().comboBox(), Styles.get().comboBoxWidth180());
        this.marketField = new ReadonlyField();
        this.typeField = new ReadonlyField();
        this.yesterdayPriceField = new ReadonlyField();
        this.changeAbsoluteHtml = Styles.tryAddStyles(new HTML(), Styles.get().labelReadOnlyField());
        this.changePercentHtml = Styles.tryAddStyles(new HTML(), Styles.get().labelReadOnlyField());
        this.lastPriceField = new ReadonlyField();
        this.priceTextBox = new TextBox();
        this.amountTextBox = new TextBox();
        this.chargeTextBox = new TextBox();
        if (SessionData.isAsDesign()) {
            this.priceTextBox.addStyleName(ICE_W90);
            this.amountTextBox.addStyleName(ICE_W90);
            this.chargeTextBox.addStyleName(ICE_W90);
        }

        this.formValidHandler = new FormValidationHandler() {
            protected void validateForm() {
                EditOrderView.this.validateForm();
            }
        };

        final CalcChargeListener calcChargeListener = new CalcChargeListener() {
            protected void calcCharge() {
                if (valid) {
                    final String charge = controller.calcCharge(priceTextBox.getText().trim(),
                            amountTextBox.getText().trim(),
                            exchangeRate);
                    chargeTextBox.setText(Renderer.PRICE2.render(charge));
                }
            }
        };

        setPriceLabel(computePriceLabel());

        applyValidation(this.priceTextBox, this.formValidHandler);
        applyValidation(this.amountTextBox, this.formValidHandler);
        applyValidation(this.chargeTextBox, this.formValidHandler);
        applyCalcCharge(this.priceTextBox, calcChargeListener);
        applyCalcCharge(this.amountTextBox, calcChargeListener);

        this.errorLabel = new Label();
        this.infoLabel = new Label(amendLabel(I18n.I.bidAskMidRate()));
        this.infoLabel.setVisible(false);
        this.infoLabel.setWidth("200px"); // $NON-NLS$

        this.priceTextBox.addChangeHandler(event -> this.infoLabel.setVisible(false));

        final KeyUpHandler upHandler = event -> {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                closeIfValid();
            }
        };

        this.priceTextBox.addKeyUpHandler(upHandler);
        this.amountTextBox.addKeyUpHandler(upHandler);

        final FlexTable g = Styles.tryAddStyles(new FlexTable(), Styles.get().generalFormStyle());
        g.setCellSpacing(SessionData.isAsDesign() ? 0 : 7);
        g.setCellPadding(0);

        int row = 0;

        g.setWidget(row, 0, portfolioCaption);
        g.setWidget(row++, 1, portfolioField);
        g.setWidget(row, 0, dateCaption);
        g.setWidget(row++, 1, this.dateDateBox);
        g.setWidget(row, 0, instrumentCaption);
        g.setWidget(row++, 1, this.instrumentField);
        if (SessionData.INSTANCE.isShowIsin()) {
            g.setWidget(row, 0, isinCaption);
            g.setWidget(row++, 1, this.isinField);
        }
        if (SessionData.INSTANCE.isShowWkn()) {
            g.setWidget(row, 0, wknCaption);
            g.setWidget(row++, 1, this.wknField);
        }
        g.setWidget(row, 0, marketCaption);
        if (this.controller.getOperation() == EditOrderController.Operation.EDIT) {
            g.setWidget(row++, 1, this.marketField);
        }
        else {
            g.setWidget(row++, 1, this.marketButton);
        }
        g.setWidget(row, 0, typeCaption);
        g.setWidget(row++, 1, this.typeField);
        if (this.controller.getOperation() != EditOrderController.Operation.EDIT) {
            g.setWidget(row, 0, lastPriceCaption);
            g.setWidget(row++, 1, this.lastPriceField);
            g.setWidget(row, 0, yesterdayPriceCaption);
            g.setWidget(row++, 1, this.yesterdayPriceField);
            g.setWidget(row, 0, changeAbsoluteCaption);
            g.setWidget(row++, 1, this.changeAbsoluteHtml);
            g.setWidget(row, 0, changePercentCaption);
            g.setWidget(row++, 1, this.changePercentHtml);
        }
        g.setWidget(row, 0, this.priceCaption);
        g.setWidget(row++, 1, this.priceTextBox);
        g.setWidget(row, 0, this.amountCaption);
        g.setWidget(row++, 1, this.amountTextBox);
        g.setWidget(row, 0, this.chargeCaption);
        g.setWidget(row++, 1, this.chargeTextBox);
        g.setWidget(row, 0, this.errorLabel);
        g.getFlexCellFormatter().setColSpan(row++, 0, 2);
        g.setWidget(row, 0, this.infoLabel);
        g.getFlexCellFormatter().setColSpan(row, 0, 2);

        final FlowPanel view = Styles.tryAddStyles(new FlowPanel(), Styles.get().generalViewStyle());
        view.add(g);

        this.dialog = Dialog.getImpl().createDialog();
        this.dialog.withTitle(controller.getOperation().getTitle())
                .withWidget(view)
                .withDefaultButton(this.controller.getOperation().getAction(), this::closeIfValid)
                .withButton(I18n.I.cancel())
                .show();
    }

    private String amendLabel(String label) {
        if (SessionData.isAsDesign()) {
            return label;
        }
        return label + ":"; // $NON-NLS$
    }

    private void updateView(MSCPriceDatas datas, Date date, String price, String amount,
            String charge) {
        this.dateDateBox.setDate(new MmJsDate(date));

        final InstrumentData instrumentData = datas.getInstrumentdata();
        this.instrumentField.setText(instrumentData.getName());
        if (SessionData.INSTANCE.isShowIsin()) {
            this.isinField.setText(instrumentData.getIsin());
        }
        if (SessionData.INSTANCE.isShowWkn()) {
            this.wknField.setText(instrumentData.getWkn());
        }
        this.typeField.setText(InstrumentTypeEnum.valueOf(instrumentData.getType()).getDescription());

        createMenu(datas);

        final MSCPriceDatasElement selectedElement = PriceUtil.getSelectedElement(datas);
        if (selectedElement != null) {
            updateSelection(selectedElement, true);
        }

        if (price != null) {
            this.priceTextBox.setText(Renderer.PRICE2.render(price));
        }
        if (amount != null) {
            this.amountTextBox.setText(Renderer.PRICE_0MAX5.render(amount));
        }
        if (charge != null) {
            this.chargeTextBox.setText(Renderer.PRICE2.render(charge));
        }
    }

    private void updateView(QwiPosition position, Date date, String price, String amount,
            String charge) {
        this.dateDateBox.setDate(new MmJsDate(date));

        final InstrumentData instrumentData = position.getInstrumentData();
        this.instrumentField.setText(instrumentData.getName());
        if (SessionData.INSTANCE.isShowIsin()) {
            this.isinField.setText(instrumentData.getIsin());
        }
        if (SessionData.INSTANCE.isShowWkn()) {
            this.wknField.setText(instrumentData.getWkn());
        }
        this.typeField.setText(InstrumentTypeEnum.valueOf(instrumentData.getType()).getDescription());

        setAmountLabel(I18n.I.perShare()); // Stück
        this.chargeCaption.setText(amendLabel(I18n.I.feeIn(position.getQuoteData().getCurrencyIso())));

        this.exchangeRate = "1"; // $NON-NLS$

        this.quoteId = position.getQuoteData().getQid();

        if (price != null) {
            this.priceTextBox.setText(Renderer.PRICE2.render(price));
        }
        if (amount != null) {
            this.amountTextBox.setText(Renderer.PRICE_0MAX5.render(amount));
        }
        if (charge != null) {
            this.chargeTextBox.setText(Renderer.PRICE2.render(charge));
        }
    }

    private void applyValidation(TextBoxBase textBox, FormValidationHandler handler) {
        textBox.addFocusHandler(handler);
        textBox.addChangeHandler(handler);
        textBox.addKeyUpHandler(handler);
    }

    private void applyCalcCharge(TextBoxBase textBox, CalcChargeListener listener) {
        textBox.addFocusHandler(listener);
        textBox.addChangeHandler(listener);
        textBox.addKeyUpHandler(listener);
    }

    private void closeIfValid() {
        if (this.valid) {
            this.controller.onEditDone(this.quoteId,
                    this.amountTextBox.getText().trim(),
                    this.priceTextBox.getText().trim(),
                    this.currency,
                    this.chargeTextBox.getText().trim(), this.dateDateBox.getDate().getJavaDate());
        }
        else {
            this.dialog.keepOpen();
        }
    }

    private void createMenu(final MSCPriceDatas datas) {
        this.marketMenu.removeAll();
        final List<MSCPriceDatasElement> elements = datas.getElement();
        for (final MSCPriceDatasElement element : elements) {
            this.marketMenu.add(new MenuItem(element.getQuotedata().getMarketName(), event -> updateSelection(element, true)));
        }
    }

    private String computePriceLabel() {
        switch (this.controller.getOperation()) {
            case SELL:
                return I18n.I.sellPrice();
            case BUY:
                return I18n.I.buyPrice();
            default:
                return I18n.I.price();
        }
    }

    private boolean isQuotedPerUnit(MSCPriceDatasElement selectedElement) {
        return selectedElement.getQuotedata().getQuotedPer().equals(UNIT);
    }

    private void setAmountLabel(String metaAmount) {
        this.amountCaptionText = metaAmount;
        this.amountCaption.setText(amendLabel(this.amountCaptionText));
    }

    private void setPriceLabel(String metaPrice) {
        this.priceCaptionText = metaPrice;
        this.priceCaption.setText(this.priceCaptionText);
    }

    private void updateSelection(final MSCPriceDatasElement selected, boolean checkForCrossRate) {
        this.currency = selected.getQuotedata().getCurrencyIso();
        if (!updateExchangeRate(selected, checkForCrossRate)) {
            return;
        }

        this.marketButton.setText(selected.getQuotedata().getMarketName());
        this.marketField.setText(selected.getQuotedata().getMarketName());

        final Price p = Price.create(selected);
        this.yesterdayPriceField.setText(renderPriceWithCurrency(p.getPreviousPrice().getPrice(), this.currency));
        this.changeAbsoluteHtml.setHTML(Renderer.CHANGE_PRICE.render(p.getChangeNet()));
        this.changePercentHtml.setHTML(Renderer.CHANGE_PERCENT.render(p.getChangePercent()));
        updatePriceForms(p, selected);
        this.quoteId = selected.getQuotedata().getQid();
        // quoted per percent -> price is in percent
        if (isQuotedPerUnit(selected)) {
            setPriceLabel(computePriceLabel() + " (" + this.currency + ")");
            setAmountLabel(I18n.I.perShare());   // Stück
        }
        else {  // quoted per percent -> amount is euros
            setPriceLabel(computePriceLabel());
            setAmountLabel(I18n.I.perValue() + " (" + this.currency + ")");  // Nennwert
        }
        this.chargeCaption.setText(amendLabel(I18n.I.feeIn(this.currency)));
        this.formValidHandler.onChange(null);
    }

    private boolean updateExchangeRate(final MSCPriceDatasElement selected,
            boolean checkForCrossRate) {
        boolean foreignAndValidCurrency = this.currency != null && !this.currency.equals(this.controller.getElement().getPortfolioCurrency());
        if (foreignAndValidCurrency) {
            if (checkForCrossRate) {
                this.controller.requestCrossrate(this.currency, new ResponseTypeCallback() {
                    protected void onResult() {
                        exchangeRate = String.valueOf(controller.getCrossRate());
                        updateSelection(selected, false);
                    }
                });
                return false;
            }
        }
        else {
            this.exchangeRate = "1"; // $NON-NLS$
        }
        return true;
    }

    private void updatePriceForms(final Price p, final MSCPriceDatasElement selected) {
        final String orderPrice;
        if (isBidAskMidEnabled(p, selected)) {
            orderPrice = this.controller.calcBidAskMidRate(p, this.exchangeRate);
            this.infoLabel.setVisible(true);
            this.lastPriceField.setText(renderPriceWithCurrency(null, this.currency));
        }
        else {
            orderPrice = getPriceValue(p, selected);
            this.lastPriceField.setText(renderPriceWithCurrency(orderPrice, this.currency));
        }

        if (!isEdit()) {
            this.priceTextBox.setText(Renderer.PRICE2.render(orderPrice));
        }
    }

    private boolean isBidAskMidEnabled(final Price p, final MSCPriceDatasElement element) {
        final String currencyIso = element.getQuotedata().getCurrencyIso();
        final String price = p.getLastPrice().getPrice();
        final String ask = p.getAsk();
        final String bid = p.getBid();
        return currencyIso != null && price == null && ask != null && bid != null;
    }

    private String getPriceValue(Price p, MSCPriceDatasElement selected) {
        final String thePrice;
        if (PriceDataType.FUND_OTC == p.getType()) {
            if (this.controller.getOperation() == EditOrderController.Operation.BUY &&
                    StringUtil.hasText(selected.getFundpricedata().getIssueprice())) {
                thePrice = selected.getFundpricedata().getIssueprice();
            }
            else if (this.controller.getOperation() == EditOrderController.Operation.SELL &&
                    StringUtil.hasText(selected.getFundpricedata().getRepurchasingprice())) {
                thePrice = selected.getFundpricedata().getRepurchasingprice();
            }
            else {
                thePrice = p.getLastPrice().getPrice();
            }
        }
        else {
            thePrice = p.getLastPrice().getPrice();
        }
        return thePrice;
    }

    private String renderPriceWithCurrency(String price, String currency) {
        final String renderedPrice = Renderer.PRICE2.render(price);
        if (currency != null) {
            return renderedPrice + " " + currency;
        }
        return renderedPrice;
    }

    private boolean isEdit() {
        return this.controller.getOperation() == EditOrderController.Operation.EDIT;
    }

    private void validateForm() {
        resetValidation();

        if (!isValidPrice(this.priceTextBox.getText().trim(), false, false)) {
            onInvalidField(this.priceTextBox, I18n.I.invalidInputForField(this.priceCaptionText));
        }
        if (!isValidPrice(this.amountTextBox.getText().trim(), false, false)) {
            onInvalidField(this.amountTextBox, I18n.I.invalidInputForField(this.amountCaptionText));
        }
        if (!isValidPrice(this.chargeTextBox.getText().trim(), true, true)) {
            onInvalidField(this.chargeTextBox, I18n.I.invalidInputForField(this.chargeCaption.getText()));
        }
    }

    private boolean isValidPrice(String s, boolean allowZero, boolean allowEmpty) {
        if ("".equals(s)) {
            return allowEmpty;
        }
        try {
            final double value = NumberUtil.toDouble(s);
            return value > 0 || (allowZero && value >= 0);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void onInvalidField(TextBoxBase textBox, String text) {
        Styles.tryAddStyles(textBox, Styles.get().textBoxInvalid());
        if (this.valid) {
            this.errorLabel.setText(text);
        }
        this.valid = false;
    }

    private void resetValidation() {
        this.valid = true;
        this.errorLabel.setText("");
        final String invalidStyle = Styles.get().textBoxInvalid();
        Styles.tryRemoveStyles(amountTextBox, invalidStyle);
        Styles.tryRemoveStyles(priceTextBox, invalidStyle);
        Styles.tryRemoveStyles(chargeTextBox, invalidStyle);
    }
}
