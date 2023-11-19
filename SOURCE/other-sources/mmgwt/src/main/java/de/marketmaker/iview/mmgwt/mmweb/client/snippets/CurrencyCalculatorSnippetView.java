/*
* CurrencyCalculatorSnippetView.java
*
* Created on 16.07.2008 13:01:43
*
* Copyright (c) vwd GmbH. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.math.BigDecimal;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;

import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.DecimalBox;
import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NumberUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.Caption;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.InfoIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.TextBox;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
public class CurrencyCalculatorSnippetView extends SnippetView<CurrencyCalculatorSnippet> {

    public static final String KEY = "key";

    private static final String MM_FORMFIELD = "mm-formfield";

    private static final String MM_LABEL = "mm-label";

    private final DecimalBox amountDecimalBox;

    private final TextBox resultTextBox;

    private final TextBox rateTextBox;

    private final TextBox factorTextBox;

    private final SnippetConfiguration config;

    private final SimplePanel layout;

    private final FlexTable formTable;

    private final Caption factorCaption;

    private final SelectButton fromSelectButton;

    private final SelectButton toSelectButton;

    public CurrencyCalculatorSnippetView(final CurrencyCalculatorSnippet snippet) {
        super(snippet);

        this.config = snippet.getConfiguration();

        setTitle(I18n.I.currencyCalculator());

        final String[][] currencies = createCurrencies();

        final String defaultCurrency = SessionData.INSTANCE.getGuiDefValue("defaultCurrency"); // $NON-NLS$

        final String defaultCurrencyKey = getDefaultCurrencyKey(currencies, defaultCurrency);
        this.fromSelectButton = createCurrencyComboBox(currencies, defaultCurrencyKey);
        this.toSelectButton = createCurrencyComboBox(currencies, defaultCurrencyKey);

        InfoIcon amountInfoIcon = new InfoIcon();
        this.amountDecimalBox = SessionData.isAsDesign()
                ? new DecimalBox(false, amountInfoIcon.getMessagePopup())
                : new DecimalBox(true);
        this.amountDecimalBox.setMandatory(true);
        this.amountDecimalBox.setMin(BigDecimal.ZERO);
        this.amountDecimalBox.addValueChangeHandler(e -> calculate());
        if (SessionData.isAsDesign()) {
            amountInfoIcon.setEditWidget(this.amountDecimalBox);
        }

        this.resultTextBox = new TextBox();
        this.resultTextBox.setReadOnly(true);

        this.rateTextBox = new TextBox();
        this.rateTextBox.setReadOnly(true);

        this.factorCaption = new Caption(I18n.I.currencyCalculatorFactor());
        this.factorTextBox = new TextBox();
        this.factorTextBox.setReadOnly(true);

        final Button btnCalc = Button.text(I18n.I.calculatorCalculate())
                .forceLegacyBorders()
                .clickHandler(e -> calculate())
                .build();

        this.formTable = new FlexTable();
        this.formTable.setStyleName("mm-currencycalculator");
        Styles.tryAddStyles(this.formTable, Styles.get().generalFormStyle()); // $NON-NLS$

        final FlexTable.FlexCellFormatter formatter = this.formTable.getFlexCellFormatter();

        this.formTable.setWidget(0, 0, new Caption(I18n.I.fromUpperCase()));
        formatter.setStyleName(0, 0, MM_FORMFIELD);
        formatter.addStyleName(0, 0, MM_LABEL);

        this.formTable.setWidget(0, 1, new Caption(I18n.I.currencyCalculatorAmount()));
        formatter.setStyleName(0, 1, MM_FORMFIELD);
        formatter.addStyleName(0, 1, MM_LABEL);

        this.formTable.setWidget(1, 0, this.fromSelectButton);
        formatter.setStyleName(1, 0, MM_FORMFIELD);

        if (SessionData.isAsDesign()) {
            final FlexTable amount = new FlexTable();
            amount.setCellPadding(0);
            amount.setCellSpacing(0);
            amount.setWidget(0, 0, amountInfoIcon);
            amount.setWidget(0, 1, this.amountDecimalBox);

            final FlexTable.FlexCellFormatter flexCellFormatter = amount.getFlexCellFormatter();
            flexCellFormatter.addStyleName(0, 0, "amountInputColumn"); // $NON-NLS$
            flexCellFormatter.addStyleName(0, 1, "infoIconColumn"); // $NON-NLS$

            this.formTable.setWidget(1, 1, amount);
        }
        else {
            this.formTable.setWidget(1, 1, this.amountDecimalBox);
        }
        formatter.setStyleName(1, 1, MM_FORMFIELD);

        this.formTable.setWidget(2, 0, new Caption(I18n.I.currencyCalculatorTo()));
        formatter.setStyleName(2, 0, MM_FORMFIELD);
        formatter.addStyleName(2, 0, MM_LABEL);

        this.formTable.setWidget(2, 1, new Caption(I18n.I.currencyCalculatorResult()));
        formatter.setStyleName(2, 1, MM_FORMFIELD);
        formatter.addStyleName(2, 1, MM_LABEL);

        this.formTable.setWidget(3, 0, this.toSelectButton);
        formatter.setStyleName(3, 0, MM_FORMFIELD);

        this.formTable.setWidget(3, 1, this.resultTextBox);
        formatter.setStyleName(3, 1, MM_FORMFIELD);
        formatter.addStyleName(3, 1, "mm-result"); // $NON-NLS$

        this.formTable.setWidget(2, 2, new Caption(I18n.I.currencyCalculatorExchangeRate()));
        formatter.setStyleName(2, 2, MM_FORMFIELD);
        formatter.addStyleName(2, 2, MM_LABEL);

        this.formTable.setWidget(3, 2, this.rateTextBox);
        formatter.setStyleName(3, 2, MM_FORMFIELD);

        this.formTable.setWidget(2, 3, this.factorCaption);
        formatter.setStyleName(2, 3, MM_FORMFIELD);
        formatter.addStyleName(2, 3, MM_LABEL);

        this.formTable.setWidget(3, 3, this.factorTextBox);
        formatter.setStyleName(3, 3, MM_FORMFIELD);

        this.formTable.setWidget(4, 1, btnCalc);
        formatter.setStyleName(4, 1, "mm-action"); // $NON-NLS$

        this.formTable.setText(5, 0, ""); // $NON-NLS-0$
        formatter.setStyleName(5, 0, "mm-valueError"); // $NON-NLS-0$
        formatter.setColSpan(5, 0, 4);

        this.formTable.getRowFormatter().setVisible(5, false);

        this.layout = new SimplePanel();
        Styles.trySetStyle(this.layout, Styles.get().generalViewStyle());
        this.layout.setWidget(this.formTable);
    }

    @NonNLS
    private String[][] createCurrencies() {
        return new String[][]{
                {"EGP", I18n.I.currencyNameEGP()},
                {"AUD", I18n.I.currencyNameAUD()},
                {"GBP", I18n.I.currencyNameGBP()},
                {"BGN", I18n.I.currencyNameBGN()},
                {"DKK", I18n.I.currencyNameDKK()},
                {"EUR", I18n.I.currencyNameEUR()},
                {"HKD", I18n.I.currencyNameHKD()},
                {"INR", I18n.I.currencyNameINR()},
                {"ISK", I18n.I.currencyNameISK()},
                {"JPY", I18n.I.currencyNameJPY()},
                {"CAD", I18n.I.currencyNameCAD()},
                {"HRK", I18n.I.currencyNameHRK()},
                {"LVL", I18n.I.currencyNameLVL()},
                {"LTL", I18n.I.currencyNameLTL()},
                {"MXN", I18n.I.currencyNameMXN()},
                {"NZD", I18n.I.currencyNameNZD()},
                {"NOK", I18n.I.currencyNameNOK()},
                {"PLN", I18n.I.currencyNamePLN()},
                {"RON", I18n.I.currencyNameRON()},
                {"RUB", I18n.I.currencyNameRUB()},
                {"SAR", I18n.I.currencyNameSAR()},
                {"SEK", I18n.I.currencyNameSEK()},
                {"CHF", I18n.I.currencyNameCHF()},
                {"SGD", I18n.I.currencyNameSGD()},
                {"SKK", I18n.I.currencyNameSKK()},
                {"SIT", I18n.I.currencyNameSIT()},
                {"ZAR", I18n.I.currencyNameZAR()},
                {"KRW", I18n.I.currencyNameKRW()},
                {"THB", I18n.I.currencyNameTHB()},
                {"CZK", I18n.I.currencyNameCZK()},
                {"TRY", I18n.I.currencyNameTRY()},
                {"HUF", I18n.I.currencyNameHUF()},
                {"USD", I18n.I.currencyNameUSD()}
        };
    }

    public SelectButton createCurrencyComboBox(String[][] currencies, String defaultCurrencyKey) {
        final Menu menu = new Menu();
        for (String[] currency : currencies) {
            menu.add(new MenuItem(currency[1]).withData(KEY, currency[0]));
        }

        final SelectButton selectButton = new SelectButton(!SessionData.isAsDesign() ? Button.getRendererType() : Button.RendererType.SPAN)
                .withMenu(menu)
                .withClickOpensMenu()
                .withSelectionHandler(e -> calculate());
        Styles.tryAddStyles(selectButton, Styles.get().comboBox(), Button.FORCED_LEGACY_BORDERS_STYLE, "currencyCombo"); // $NON-NLS$
        selectButton.setSelectedData(KEY, defaultCurrencyKey, false);
        return selectButton;
    }

    public void calculate() {
        if (setConfigParameters()) {
            this.snippet.ackParametersChanged();
        }
        setFocusAndSelectAll();
    }

    private String getDefaultCurrencyKey(String[][] currencies, String defaultKey) {
        for (String[] currency : currencies) {
            if (currency[0].equals(defaultKey)) {
                return currency[0];
            }
        }
        return "EUR";  // $NON-NLS$
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.layout);
        update("1.00", "1.00", "1"); // TODO 2.0: is this necessary // $NON-NLS$
    }

    public void update(String amount, String rate, String factor) {
        this.formTable.getRowFormatter().setVisible(5, false);
        this.amountDecimalBox.setValue(this.config.getBigDecimal("betrag", null)); // $NON-NLS$
        this.resultTextBox.setValue(Renderer.PRICE.render(amount) + " " + this.config.getString("isocodeTo")); // $NON-NLS$
        this.resultTextBox.setValid(true);
        this.rateTextBox.setValue(Renderer.PRICE.render(rate) + " " + this.config.getString("isocodeTo")); // $NON-NLS$
        this.rateTextBox.setValid(true);
        this.factorTextBox.setValue(factor);
        this.factorTextBox.setValid(true);
        setFactorVisible(Integer.parseInt(factor) > 1);
        // deferred is necessary to set the focus and select the value if the view is shown again
        // if another sub-controller was selected
        Scheduler.get().scheduleDeferred(this::setFocusAndSelectAll);
    }

    public void showError(String errorMessage) {
        this.formTable.getRowFormatter().setVisible(5, true);
        this.resultTextBox.setValue(""); // $NON-NLS-0$
        this.resultTextBox.setValid(false);
        this.rateTextBox.setValue(""); // $NON-NLS-0$
        this.rateTextBox.setValid(false);
        this.formTable.setText(5, 0, errorMessage);
        setFocusAndSelectAll();
    }

    private void setFocusAndSelectAll() {
        this.amountDecimalBox.selectAll();
        this.amountDecimalBox.setFocus(true);
    }

    private boolean setConfigParameters() {
        if (isAmountValid()) {
            this.config.put("betrag", NumberUtil.toPlainStringValue(this.amountDecimalBox.getValue())); // $NON-NLS-0$
            this.config.put("isocodeFrom", (String) this.fromSelectButton.getData(KEY)); // $NON-NLS-0$
            this.config.put("isocodeTo", (String) this.toSelectButton.getData(KEY)); // $NON-NLS-0$
            return true;
        }
        return false;
    }

    private boolean isAmountValid() {
        final BigDecimal value = this.amountDecimalBox.getValue();
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    private void setFactorVisible(boolean visible) {
        this.factorTextBox.setVisible(visible);
        this.factorCaption.setVisible(visible);
    }
}
