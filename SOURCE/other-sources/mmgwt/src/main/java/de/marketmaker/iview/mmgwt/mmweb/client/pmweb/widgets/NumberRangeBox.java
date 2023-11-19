/*
 * NumberRangeBox.java
 *
 * Created on 11.08.2014 11:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.DecimalBox;

import java.math.BigDecimal;

/**
 * @author mdick
 */
public class NumberRangeBox extends Composite implements HasValue<NumberRange> {
    private final DecimalBox minBox = new DecimalBox();
    private final DecimalBox maxBox = new DecimalBox();

    public NumberRangeBox() {
        final HorizontalPanel panel = new HorizontalPanel();
        initWidget(panel);

        panel.setStyleName("mm-number-range");
        panel.add(new Label(I18n.I.from()));
        panel.add(this.minBox);
        panel.add(new Label(I18n.I.to()));
        panel.add(this.maxBox);

        final ValueChangeHandler<BigDecimal> valueChangeHandler = new ValueChangeHandler<BigDecimal>() {
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {
                ValueChangeEvent.fire(NumberRangeBox.this, getValue());
            }
        };

        this.minBox.addValueChangeHandler(valueChangeHandler);
        this.maxBox.addValueChangeHandler(valueChangeHandler);
    }

    @Override
    public NumberRange getValue() {
        return new NumberRange(this.minBox.getValue(), this.maxBox.getValue());
    }

    @Override
    public void setValue(NumberRange value) {
        setValue(value, false);
    }

    @Override
    public void setValue(NumberRange value, boolean fireEvents) {
        final boolean hasValue = value != null;
        this.minBox.setValue(hasValue && value.hasMin() ? value.getMin() : null);
        this.maxBox.setValue(hasValue && value.hasMax() ? value.getMax() : null);

        if(fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<NumberRange> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public NumberRangeBox withPercent(boolean percent) {
        this.minBox.withPercent(percent);
        this.maxBox.withPercent(percent);
        return this;
    }
}
