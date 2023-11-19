/*
 * PerformanceCalculatorInput.java
 *
 * Created on 4/25/14 9:28 AM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.TextBox;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Map;

/**
 * @author Stefan Willenbrock
 */
@NonNLS
public class PerformanceCalculatorInput {

    private TextBox investment = new TextBox();

    private final TextBox agio = new TextBox();

    private final TextBox orderCharge = new TextBox();

    private final TextBox comission = new TextBox();

    private final TextBox depositCharge = new TextBox();

    private final TextBox miscCosts = new TextBox();

    private final TextBox[] inputBoxes = new TextBox[]{this.investment, this.agio, this.orderCharge, this.comission, this.depositCharge, this.miscCosts};

    private final TextBox exchangeInput = new TextBox();

    public TextBox getInvestment() {
        return investment;
    }

    public TextBox getAgio() {
        return agio;
    }

    public TextBox getOrderCharge() {
        return orderCharge;
    }

    public TextBox getComission() {
        return comission;
    }

    public TextBox getDepositCharge() {
        return depositCharge;
    }

    public TextBox getMiscCosts() {
        return miscCosts;
    }

    public TextBox[] getInputBoxes() {
        return inputBoxes;
    }

    public TextBox getExchangeInput() {
        return exchangeInput;
    }

    PerformanceCalculatorInput(AbstractPerformanceCalculator snippet) {
        addKeyDownHandler(snippet);
        setFormParameters(snippet.getInputConfiguration());
    }

    void markInput() {
        if (areAllEmpty()) {
            for (TextBox textBox : inputBoxes) {
                textBox.removeStyleName("x-form-invalid");
            }
        }
        else {
            if (isInputPositive(this.investment)) {
                this.investment.removeStyleName("x-form-invalid");
            }
            else {
                this.investment.addStyleName("x-form-invalid");
            }
            markOptionalParams();
        }
    }

    void markOptionalParams() {
        for (TextBox textBox : inputBoxes) {
            if (this.investment.equals(textBox)) {
                continue;
            }
            if (isInputValid(textBox)) {
                textBox.removeStyleName("x-form-invalid");
            }
            else {
                textBox.addStyleName("x-form-invalid");
            }
        }
    }

    boolean areAllEmpty() {
        for (TextBox textBox : inputBoxes) {
            if (!isInputEmpty(textBox)) {
                return false;
            }
        }
        return true;
    }

    boolean areAllValid() {
        for (TextBox textBox : inputBoxes) {
            if (!isInputValid(textBox)) {
                return false;
            }
        }
        return true;
    }

    boolean isInvestmentPositive() {
        return isInputPositive(this.investment);
    }

    boolean isInputValid(TextBox widget) {
        if (isInputEmpty(widget)) {
            return true;
        }
        return isInputPositive(widget);
    }

    boolean isInputPositive(TextBox widget) {
        try {
            return parseInput(widget.getValue()) >= 0;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    boolean isInputEmpty(TextBox widget) {
        return ("").equals(widget.getValue());
    }

    void enable(boolean enabled) {
        for (TextBox textBox : inputBoxes) {
            textBox.setEnabled(enabled);
        }
        this.exchangeInput.setEnabled(enabled);
        if (!enabled) {
            this.exchangeInput.setValue("");
        }
    }

    void setFormParameters(Map<String, String> config) {
        this.investment.setText(config.get("investment"));
        this.agio.setText(config.get("agio"));
        this.orderCharge.setText(config.get("orderCharge"));
        this.comission.setText(config.get("comission"));
        this.depositCharge.setText(config.get("depositCharge"));
        this.miscCosts.setText(config.get("miscCosts"));
        formatAllInput();
    }

    private void addKeyDownHandler(final AbstractPerformanceCalculator snippet) {
        final KeyDownHandler handler = new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
                    snippet.onCalculateEvent();
                }
            }
        };
        for (TextBox textBox : this.inputBoxes) {
            textBox.addKeyDownHandler(handler);
        }
    }

    void formatAllInput() {
        for (TextBox tb : this.inputBoxes) {
            if (StringUtil.hasText(tb.getValue())) {
                try {
                    tb.setValue(formatInput(parseInput(tb.getValue())));
                } catch (Exception e) {
                    Firebug.error("failed to format input \"" + tb.getValue() + "\" for input field \"" + tb.getName() + "\"", e);
                }
            }
        }

    }

    private double parseInput(String value) {
        return NumberFormat.getDecimalFormat().parse(value);
    }

    private String formatInput(double value) {
        return NumberFormat.getFormat("#,##0.00#").format(value);
    }
}
