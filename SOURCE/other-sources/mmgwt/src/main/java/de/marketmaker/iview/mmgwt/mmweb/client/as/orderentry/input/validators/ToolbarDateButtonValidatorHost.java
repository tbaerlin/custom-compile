/*
 * ToolbarDateButtonValidatorHost.java
 *
 * Created on 11.01.13 12:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.AbstractFormatterValidatorHost;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.datepicker.ToolbarDateButton;

import java.util.Arrays;

/**
 * @author Markus Dick
 */
public class ToolbarDateButtonValidatorHost extends AbstractFormatterValidatorHost<ToolbarDateButton, MmJsDate> {
    private final ToolbarDateButton valueSource;

    @SafeVarargs
    public ToolbarDateButtonValidatorHost(ToolbarDateButton valueSource, Validator<MmJsDate>... validators) {
        super(null, Arrays.asList(validators));
        this.valueSource = valueSource;

        valueSource.addValueChangeHandler(new ValueChangeHandler<MmJsDate>() {
            @Override
            public void onValueChange(ValueChangeEvent<MmJsDate> event) {
                final MmJsDate date = ToolbarDateButtonValidatorHost.this.valueSource.getValue();
                doValidate(date);
            }
        });
    }

    @Override
    protected ToolbarDateButton getValueSource() {
        return this.valueSource;
    }

    @Override
    protected boolean isValueSourceEnabled() {
        return this.valueSource.isEnabled();
    }

    @Override
    protected MmJsDate getValue() {
        return this.valueSource.getValue();
    }

    @Override
    protected void setValue(MmJsDate value) {
        /* do nothing */
    }
}
