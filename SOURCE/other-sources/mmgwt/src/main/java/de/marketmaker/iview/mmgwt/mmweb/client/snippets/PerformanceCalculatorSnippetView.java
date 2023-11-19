/*
 * PerformanceCalculatorSnippetView.java
 *
 * Created on 4/8/14 9:22 AM
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.dmxml.MSCPerformanceCalculatorElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Stefan Willenbrock
 */
public class PerformanceCalculatorSnippetView extends SnippetView<AbstractPerformanceCalculator> {
    private static final String PANEL_CSS = "padding: 5px;"; // $NON-NLS$
    private static final String ROW_CSS = "width: 42em; margin: 0em 0em 0.25em 0em;"; // $NON-NLS$
    private static final String INPUT_COL1_CSS = "width: 14em;"; // $NON-NLS$
    private static final String INPUT_COL2_CSS = "width: 18em;"; // $NON-NLS$
    private static final String INPUT_COL3_CSS = "margin: 0em 0em 0em 1em;"; // $NON-NLS$
    private static final String CALC_BUTTON_CSS = "margin: 1em 0em 1em 0em;"; // $NON-NLS$
    private static final String FULL_WIDTH = "100%"; // $NON-NLS$
    private static final String RESULT_DIV_ID = "pfc-result-div"; // $NON-NLS$

    private final InlineLabel inlineCurrencyA = new InlineLabel(I18n.I.currency());
    private final InlineLabel inlineCurrencyB = new InlineLabel(I18n.I.currency());
    private final InlineLabel inlineCurrencyC = new InlineLabel(I18n.I.currency());

    private HasText instrumentName;
    private Button calcButton;
    private SnippetTableWidget resultTable;
    private HTMLPanel htmlPanel;
    private HTMLPanel resultWidget;

    private SnippetConfigurationView snippetConfigurationView;

    @SuppressWarnings({"GWTStyleCheck"})
    public PerformanceCalculatorSnippetView(final AbstractPerformanceCalculator snippet, boolean hasSymbolWidget) {
        super(snippet);

        setTitle(I18n.I.performanceCalculator());

        for (TextBox textBox : this.snippet.getInput().getInputBoxes()) {
            textBox.setWidth(FULL_WIDTH);
            textBox.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
        }

        createInputForm(hasSymbolWidget);
        createResultWidget();
    }

    private Widget createInstrumentField() {
        //Legacy DND rubbish
        if (!SessionData.isAsDesign()) {
            return createLegacyGxtInstrumentNameDndTarget();
        }

        // ICE/AS Design default
        final Label instrumentName = new Label();
        instrumentName.setStylePrimaryName("labelAsReadOnlyField");
        this.instrumentName = instrumentName;
        return instrumentName;
    }

    private Widget createLegacyGxtInstrumentNameDndTarget() {
        final Text gxtInstrumentName = new Text();
        gxtInstrumentName.addStyleName("mm-finder-instrumentName");
        gxtInstrumentName.setWidth(FULL_WIDTH);

        final DropTarget dt = new DropTarget(gxtInstrumentName) {
            @Override
            protected void onDragDrop(DNDEvent dndEvent) {
                super.onDragDrop(dndEvent);
                final QuoteWithInstrument quote = dndEvent.getData();
                snippet.onSymbolEvent(quote.getName(), quote.getQuoteData().getQid());
            }
        };
        dt.setGroup("ins"); // $NON-NLS$
        dt.setOverStyle("drag-ok"); // $NON-NLS$

        this.instrumentName = new HasText() {
            @Override
            public String getText() {
                return gxtInstrumentName.getText();
            }

            @Override
            public void setText(String s) {
                gxtInstrumentName.setText(s);
            }
        };
        return gxtInstrumentName;
    }

    @NonNLS
    private Widget createInputForm(boolean hasSymbolWidget) {
        final String symbolInputId = "pfc-sym-input";
        final String symbolButtonId = "pfc-sym-button";
        final String exchangeInputId = "pfc-exc-input";
        final String calcButtonId = "pfc-calc-button";

        String html = "<div style='" + PANEL_CSS + "'><div style='margin: 1em 0em;'>" + I18n.I.performanceCalculatorTitle() + "</div>";

        if (hasSymbolWidget) {
            html += "<div style='" + ROW_CSS + "'>" +
                    "<table cellspacing='0' cellpadding='0'><tr>" +
                    "<td><div style='" + INPUT_COL1_CSS + "'>" + I18n.I.instrument() + "</div></td>" +
                    "<td><div id='" + symbolInputId + "' style='" + INPUT_COL2_CSS + "'></div></td>" +
                    "<td><div id='" + symbolButtonId + "' style='" + INPUT_COL3_CSS + "'></div></td>" +
                    "</tr></table>" +
                    "</div><div style='" + ROW_CSS + "'>" +
                    "<table cellspacing='0' cellpadding='0'><tr>" +
                    "<td><div style='" + INPUT_COL1_CSS + "'>" + I18n.I.exchange() + "</div></td>" +
                    "<td><div id='" + exchangeInputId + "' style='" + INPUT_COL2_CSS + "'></div></td>" +
                    "</tr></table>" +
                    "</div>";
        }

        html += "<div style='" + ROW_CSS + "'>" +
                "<table cellspacing='0' cellpadding='0'><tr>" +
                "<td><div style='" + INPUT_COL1_CSS + "'>" + I18n.I.initialInvestmentSum() + "</div></td>" +
                "<td><div id='pfcInvest' style='" + INPUT_COL2_CSS + "'></div></td>" +
                "<td><div id='pfcInvestUnit' style='" + INPUT_COL3_CSS + "'></div></td>" +
                "</tr></table>" +
                "</div><div style='" + ROW_CSS + "'>" +
                "<table cellspacing='0' cellpadding='0'><tr>" +
                "<td><div style='" + INPUT_COL1_CSS + "'>" + I18n.I.issueSurcharge() + "</div></td>" +
                "<td><div id='pfcAgio' style='" + INPUT_COL2_CSS + "'></div></td>" +
                "<td><div style='" + INPUT_COL3_CSS + "'>%</div></td>" +
                "</tr></table>" +
                "</div><div style='" + ROW_CSS + "'>" +
                "<table cellspacing='0' cellpadding='0'><tr>" +
                "<td><div style='" + INPUT_COL1_CSS + "'>" + I18n.I.orderCharge() + "</div></td>" +
                "<td><div id='pfcOrderCharge' style='" + INPUT_COL2_CSS + "'></div></td>" +
                "<td><div style='" + INPUT_COL3_CSS + "'>%</div></td>" +
                "</tr></table>" +
                "</div><div style='" + ROW_CSS + "'>" +
                "<table cellspacing='0' cellpadding='0'><tr>" +
                "<td><div style='" + INPUT_COL1_CSS + "'>" + I18n.I.exchangeCommission() + "</div></td>" +
                "<td><div id='pfcComission' style='" + INPUT_COL2_CSS + "'></div></td>" +
                "<td><div style='" + INPUT_COL3_CSS + "'>%</div></td>" +
                "</tr></table>" +
                "</div><div style='" + ROW_CSS + "'>" +
                "<table cellspacing='0' cellpadding='0'><tr>" +
                "<td><div style='" + INPUT_COL1_CSS + "'>" + I18n.I.depotAccountFee() + "</div></td>" +
                "<td><div id='pfcDepositCharge' style='" + INPUT_COL2_CSS + "'></div></td>" +
                "<td><div id='pfcDepositChargeUnit' style='" + INPUT_COL3_CSS + "'></div></td>" +
                "</tr></table>" +
                "</div><div style='" + ROW_CSS + "'>" +
                "<table cellspacing='0' cellpadding='0'><tr>" +
                "<td><div style='" + INPUT_COL1_CSS + "'>" + I18n.I.misc() + "</div></td>" +
                "<td><div id='pfcMiscCosts' style='" + INPUT_COL2_CSS + "'></div></td>" +
                "<td><div id='pfcMiscCostsUnit' style='" + INPUT_COL3_CSS + "'></div></td>" +
                "</tr></table>" +
                "</div><div style='" + ROW_CSS + "'>" +
                "<table cellspacing='0' cellpadding='0'><tr>" +
                "<td><div style='" + INPUT_COL1_CSS + "'></div></td>" +
                "<td><div id='" + calcButtonId + "' style='" + CALC_BUTTON_CSS + "'></div></td>" +
                "</tr></table>" +
                "</div><div id='" + RESULT_DIV_ID + "' style='" + ROW_CSS + "'>" +
                "</div>" +
                "</div>";
        this.htmlPanel = new HTMLPanel(html);

        final PerformanceCalculatorInput input = this.snippet.getInput();
        if (hasSymbolWidget) {
            this.htmlPanel.add(createInstrumentField(), symbolInputId);
            this.htmlPanel.add(Button.text(I18n.I.selection())
                    .forceLegacyBorders()
                    .clickHandler(e -> showDialog())
                    .build(), symbolButtonId);

            final TextBox exchangeInput = input.getExchangeInput();
            exchangeInput.setReadOnly(true);
            exchangeInput.setWidth(FULL_WIDTH);
            exchangeInput.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
            this.htmlPanel.add(exchangeInput, exchangeInputId);
        }

        this.htmlPanel.add(input.getInvestment(), "pfcInvest");
        this.htmlPanel.add(input.getAgio(), "pfcAgio");
        this.htmlPanel.add(input.getOrderCharge(), "pfcOrderCharge");
        this.htmlPanel.add(input.getComission(), "pfcComission");
        this.htmlPanel.add(input.getDepositCharge(), "pfcDepositCharge");
        this.htmlPanel.add(input.getMiscCosts(), "pfcMiscCosts");

        this.htmlPanel.add(this.inlineCurrencyA, "pfcInvestUnit");
        this.htmlPanel.add(this.inlineCurrencyB, "pfcDepositChargeUnit");
        this.htmlPanel.add(this.inlineCurrencyC, "pfcMiscCostsUnit");

        this.calcButton = Button.text(I18n.I.calculatorCalculate())
                .forceLegacyBorders()
                .clickHandler(e -> this.snippet.onCalculateEvent())
                .build();
        this.calcButton.setEnabled(false);
        this.htmlPanel.add(this.calcButton, calcButtonId);

        return this.htmlPanel;
    }

    @NonNLS
    private Widget createResultWidget() {
        final String resultWidgetId = "pfc-res-wgt";

        final String html = "<div id='" + resultWidgetId + "' class='mm-borderedContent'></div>" +
                "<div style='margin: 1em 0em 0em 0em;'>" + I18n.I.performanceCalculatorHint() + "</div>" +
                "<div style='margin: 1em 0em 0em 0em;'>" + I18n.I.performanceCalculatorOneTimeCosts() + "</div>";
        this.resultWidget = new HTMLPanel(html);

        final DefaultTableColumnModel columnModel = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.periods(), 0.3325f),
                new TableColumn(I18n.I.netGrowthDevelopment(), 0.3325f, TableCellRenderers.DEFAULT_RIGHT),
                new TableColumn(I18n.I.grossGrowthDevelopment(), 0.3325f, TableCellRenderers.DEFAULT_RIGHT),
        });
        this.resultTable = new SnippetTableWidget(columnModel);
        this.resultWidget.add(this.resultTable, resultWidgetId);

        return this.resultWidget;
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.htmlPanel);
        update(null);
    }

    protected void update(List<MSCPerformanceCalculatorElement> data) {
        if (data == null) {
            this.resultTable.setMessage("&nbsp;", true); // $NON-NLS$
            return;
        }
        final DefaultTableDataModel tableDataModel = new DefaultTableDataModel(data.size(), 3);
        final List<MSCPerformanceCalculatorElement> reversedData = new ArrayList<>();
        reversedData.addAll(data);
        Collections.reverse(reversedData);
        for (int i = 0; i < reversedData.size(); i++) {
            tableDataModel.setValueAt(i, 0, reversedData.get(i).getStart() + " - " + reversedData.get(i).getEnd() + (i == 0 ? "*" : "")); // $NON-NLS$
            tableDataModel.setValueAt(i, 1, Renderer.PERCENT.render(reversedData.get(i).getPerformanceNetto()));
            tableDataModel.setValueAt(i, 2, Renderer.PERCENT.render(reversedData.get(i).getPerformanceBrutto()));
        }
        this.resultTable.updateData(tableDataModel);
    }

    protected void updateCurrencyDiv(String currency) {
        final String c = currency == null ? I18n.I.currency() : currency;
        this.inlineCurrencyA.setText(c);
        this.inlineCurrencyB.setText(c);
        this.inlineCurrencyC.setText(c);
    }

    protected void updateSymbolText(String symbol) {
        this.instrumentName.setText(symbol);
    }

    protected void enable(boolean hasSymbol) {
        this.calcButton.setEnabled(hasSymbol);
        if (hasSymbol) {
            this.htmlPanel.add(this.resultWidget, RESULT_DIV_ID);
        } else {
            this.htmlPanel.remove(this.resultWidget);
        }
    }

    protected void showError(String message) {
        this.resultTable.setMessage(message, false);
    }

    protected void showDialog() {
        if(this.snippetConfigurationView == null) {
            final HashMap<String, String> symbolParams = new HashMap<>();
            this.snippetConfigurationView = new SnippetConfigurationView(new ConfigurableSnippet() {
                @Override
                public HashMap<String, String> getCopyOfParameters() {
                    return new HashMap<>(symbolParams);
                }

                @Override
                public void setParameters(HashMap<String, String> params) {
                    symbolParams.putAll(params);
                    snippet.onSymbolEvent(symbolParams.get("title"), symbolParams.get("symbol")); // $NON-NLS$
                }
            }, SnippetConfigurationView.SymbolParameterType.IID);
            this.snippetConfigurationView.addSelectSymbol(null);
        }

        this.snippetConfigurationView.show();
    }
}
