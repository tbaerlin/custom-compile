/*
 * HasStringValueValidatorHost.java
 *
 * Created on 16.01.13 12:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.formatters.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.Validator;

import java.util.Arrays;

/**
 * @author Markus Dick
 */
public class HasStringValueHost extends AbstractFormatterValidatorHost<HasValue<String>, String> {
    private final HasValue<String> valueSource;
    private final HasEnabled valueSourceHasEnabled;

    @SafeVarargs
    public HasStringValueHost(HasValue<String> valueSource, Formatter<String> formatter, Validator<String>... validators) {
        super(formatter, Arrays.asList(validators));

        this.valueSource = valueSource;

        if(valueSource instanceof HasEnabled) {
            this.valueSourceHasEnabled = (HasEnabled)valueSource;
        }
        else {
            this.valueSourceHasEnabled = null;
        }

        valueSource.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                formatAndValidate();
            }
        });

        if(valueSource instanceof HasBlurHandlers) {
            final HasBlurHandlers blurSource = (HasBlurHandlers)valueSource;

            blurSource.addBlurHandler(new BlurHandler() {
                @Override
                public void onBlur(BlurEvent event) {
                    formatAndValidate();
                }
            });
        }

        if(valueSource instanceof HasKeyUpHandlers) {
            final HasKeyUpHandlers keyUpSource = (HasKeyUpHandlers)valueSource;

            keyUpSource.addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    validate();
                }
            });
        }
    }

    @Override
    protected HasValue<String> getValueSource() {
        return this.valueSource;
    }

    @Override
    protected boolean isValueSourceEnabled() {
        return this.valueSourceHasEnabled == null || this.valueSourceHasEnabled.isEnabled();
    }

    @Override
    protected String getValue() {
        return this.valueSource.getValue();
    }

    @Override
    protected void setValue(String value) {
        this.valueSource.setValue(value, false);
    }
}
