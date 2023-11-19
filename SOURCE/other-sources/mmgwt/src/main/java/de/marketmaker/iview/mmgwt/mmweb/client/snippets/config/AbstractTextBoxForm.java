/*
 * AbstractTextBoxForm.java
 *
 * Created on 19.06.2012 16:02:52
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;

import java.util.Map;

/**
 * @author Markus Dick
 */
abstract class AbstractTextBoxForm extends FormPanel {
    private final Map<String, String> params;

    private final TextField<String>textField;

    protected AbstractTextBoxForm(Map<String, String> params, final String label, int width) {
        this(params, label, width, false);
    }

    protected AbstractTextBoxForm(Map<String, String> params, final String label, int width, boolean readonly) {
        setBorders(false);
        setLabelWidth(60);

        textField = new TextField<String>();
        textField.setFieldLabel(label);
        textField.setHideLabel(false);
        textField.setSelectOnFocus(false);
        textField.setWidth(width);
        textField.setReadOnly(readonly);

        add(textField);

        this.params = params;

        textField.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent event) {
                final Object value = event.getValue();
                updateParams(value, (value == null) ? "" : value.toString());
            }
        });

        final String value = this.params.get(getValueParameterName());
        final String display = this.params.get(getDisplayParameterName());

        updateParams(value, display);
        textField.setValue(display);
    }

    protected void updateParams(final Object value, final String display) {
        if (value != null && !"".equals(value)) { // $NON-NLS-0$
            this.params.put(getDisplayParameterName(), display);
            this.params.put(getValueParameterName(), value.toString());
        }
        else {
            this.params.remove(getDisplayParameterName());
            this.params.remove(getValueParameterName());
        }
    }

    protected void updateTextField(final Object value) {
        if (value != null && !"".equals(value)) {
            textField.setValue(value.toString());
        }
        else {
            textField.setValue(null);
        }
    }

    public Map<String, String>getParams() {
        return params;
    }

    protected abstract String getDisplayParameterName();

    protected abstract String getValueParameterName();
}
