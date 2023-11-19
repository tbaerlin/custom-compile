/*
 * TextFieldInputWidget.java
 *
 * Created on 28.03.12 16:48
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.TextBox;
import de.marketmaker.iview.dmxmldocu.DmxmlBlockParameterDocumentation;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class TextBoxParameterInputWidget extends AbstractParameterInputWidget<TextBox> {

    public TextBoxParameterInputWidget(DmxmlBlockParameterDocumentation paramDocu) {
        super(paramDocu);
    }

    /**
     * Special constructor for uiBinder; this one directly calls initComponents.
     * Do NOT use this for manually created components
     * @param paramName
     * @param description
     * @param required
     * @param multiValued
     * @param sampleValue
     */
    @UiConstructor
    public TextBoxParameterInputWidget(String paramName, String description, boolean required,
            boolean multiValued, String sampleValue) {
        super(createParamDocu(paramName, description, required, multiValued, sampleValue));
        initComponents();
    }

    private static DmxmlBlockParameterDocumentation createParamDocu(String name, String description,
            boolean required, boolean multiValued, String sampleValue) {
        final DmxmlBlockParameterDocumentation result = new DmxmlBlockParameterDocumentation();
        result.setName(name);
        result.setDescription(description);
        result.setRequired(required);
        result.setMultiValued(multiValued);
        result.setSampleValue(sampleValue);
        return result;
    }

    @Override
    protected void configureInputWidget(TextBox inputWidget) {
        inputWidget.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                notifyListeners();
            }
        });
    }

    @Override
    protected TextBox createInputWidget() {
        return new TextBox();
    }

    @Override
    protected String getParameterValue(TextBox inputWidget) {
        return inputWidget.getValue();
    }

    @Override
    protected void setParameterValue(TextBox inputWidget, String value) {
        inputWidget.setValue(value);
    }
}
