/*
 * PerformanceCalculatorSnippet.java
 *
 * Created on 4/8/14 9:03 AM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.TextBox;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCPerformanceCalculator;
import de.marketmaker.iview.dmxml.MSCPerformanceCalculatorElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.List;
import java.util.Map;

/**
 * @author Stefan Willenbrock
 */
public abstract class AbstractPerformanceCalculator extends AbstractSnippet<AbstractPerformanceCalculator, PerformanceCalculatorSnippetView> {

    final String symbolKey = "symbol"; // $NON-NLS-0$

    final String[] parameters = {"investment", "agio", // $NON-NLS$
            "orderCharge", "comission", "depositCharge", "miscCosts"}; // $NON-NLS$

    private final Map<String, String> inputConfiguration;

    private final PerformanceCalculatorInput input;

    protected AbstractPerformanceCalculator(DmxmlContext context, SnippetConfiguration configuration,
                                            Map<String, String> inputConfiguration) {
        super(context, configuration);
        this.inputConfiguration = inputConfiguration;
        initialize();
        this.input = new PerformanceCalculatorInput(this);
    }

    private DmxmlContext.Block<MSCPerformanceCalculator> block;

    public PerformanceCalculatorInput getInput() {
        return input;
    }

    @Override
    public void destroy() {
        destroyBlock(this.block);
    }

    @Override
    public void updateView() {
        String marketName = null, currency = null;
        List<MSCPerformanceCalculatorElement> data = null;

        if (this.block == null) {
            Firebug.log("<updateView> no block instance"); // $NON-NLS-0$
        } else if (!this.block.isResponseOk()) {
            if (("performances.missing").equals(this.block.getError().getCode())) { // $NON-NLS-0$
                getView().showError(I18n.I.noPerformancesAvailable());
            } else {
                getView().showError(this.block.getError().getCode());
            }
        } else {
            final MSCPerformanceCalculator performanceCalculator = this.block.getResult();
            marketName = performanceCalculator.getQuotedata().getMarketName();
            currency = performanceCalculator.getQuotedata().getCurrencyIso();
            data = performanceCalculator.getData();
        }

        this.input.getExchangeInput().setText(marketName);
        getView().updateCurrencyDiv(currency);
        getView().update(data);
    }

    protected Map<String, String> getInputConfiguration() {
        return this.inputConfiguration;
    };

    protected void enableBlock() {
        if (this.block == null) {
            createBlock();
        }
    }

    protected void createBlock() {
        this.block = createBlock("MSC_PerformanceCalculator"); // $NON-NLS-0$
    }

    @Override
    protected void onParametersChanged() {
        this.block.setParameter(symbolKey, getInputConfiguration().get(symbolKey));
        for (String p : this.parameters) {
            this.block.setParameter(p, getInputConfiguration().get(p));
        }
    }

    protected void onCalculateEvent() {
        this.input.markInput();
        if (setConfigParams()) {
            this.input.formatAllInput();
            ackParametersChanged();
        } else {
            clearConfig();
            ackParametersChanged();
        }
    }

    protected boolean setConfigParams() {
        if (input.areAllValid() && input.isInvestmentPositive()) {
            setConfigParam("investment", input.getInvestment()); // $NON-NLS-0$
            setConfigParam("agio", input.getAgio()); // $NON-NLS-0$
            setConfigParam("orderCharge", input.getOrderCharge()); // $NON-NLS-0$
            setConfigParam("comission", input.getComission()); // $NON-NLS-0$
            setConfigParam("depositCharge", input.getDepositCharge()); // $NON-NLS-0$
            setConfigParam("miscCosts", input.getMiscCosts()); // $NON-NLS-0$
            return true;
        } else {
            return false;
        }
    }

    protected void setConfigParam(String parameter, TextBox widget) {
        if (widget.getValue() == null) {
            getInputConfiguration().remove(parameter);
        } else {
            try {
                final double v = NumberFormat.getDecimalFormat().parse(widget.getValue());
                getInputConfiguration().put(parameter, String.valueOf(v));
            } catch (NumberFormatException e) {
                e.printStackTrace(); // prevented by input.areAllValid()
            }
        }
    }

    protected void clearConfig() {
        for (String p : this.parameters) {
            getInputConfiguration().remove(p);
        }
    }

    protected void onSymbolEvent(String title, String qid) {
        if (qid == null) {
            getInputConfiguration().put(symbolKey, ""); // $NON-NLS-0$
            getView().updateCurrencyDiv(I18n.I.currency());
            getView().updateSymbolText(""); // $NON-NLS-0$
            setSymbolState(false);
            return;
        }
        getInputConfiguration().put(symbolKey, qid);
        getView().updateSymbolText(title);
        setSymbolState(true);

        enableBlock();
        if (setConfigParams()) {
            ackParametersChanged();
        }
    }

    protected void setSymbolState(boolean hasSymbol) {
        this.input.enable(hasSymbol);
        getView().enable(hasSymbol);
    }

    protected void initialize() {
        getInputConfiguration().put("investment", "1000"); // $NON-NLS$
    }
}
