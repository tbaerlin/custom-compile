/*
 * AbstractComboBoxForm.java
 *
 * Created on 05.05.2008 14:15:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import java.util.Map;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;

import de.marketmaker.iview.mmgwt.mmweb.client.data.NameValuePair;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
abstract class AbstractComboBoxForm extends FormPanel implements ContributesToTitleSuffix {
    protected final Map<String, String> params;
    protected final ComboBox<NameValuePair> cb;

    protected AbstractComboBoxForm(Map<String, String> params, final String label, int width) {
        setBorders(false);
        setLabelWidth(60);
        setFieldWidth(width);

        cb = new ComboBox<>();
        cb.setForceSelection(true);
        cb.setMinChars(1);
        cb.setFieldLabel(label);
        cb.setEditable(false);
        cb.setTriggerAction(ComboBox.TriggerAction.ALL);
        cb.setTypeAhead(true);
        cb.setSelectOnFocus(false);
        cb.setDisplayField(NameValuePair.DISPLAY_FIELD);
        cb.setValueField(NameValuePair.VALUE_FIELD);
        add(cb);

        this.params = params;

        initComboBox();
    }

    /**
     * the initialization of the combobox can be overridden in subclasses
     * e.g. if we need to fetch data from the backend
     */
    protected void initComboBox() {
        initComboBox(initStore());
    }

    protected void initComboBox(ListStore<NameValuePair> store) {
        cb.setStore(store);
        cb.addListener(Events.Select, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent event) {
                updateParams(cb.getValue());
            }
        });

        final String current = this.params.get(getValueParameterName());
        for (int i = 0, n = store.getCount(); i < n; i++) {
            final NameValuePair pair = store.getAt(i);
            final String value = pair.getValue();
            if (StringUtil.equals(value, current)) {
                cb.setValue(pair);
                return;
            }
        }

        final NameValuePair pair = store.getAt(0);
        if (pair != null) {
            updateParams(pair);
            cb.setValue(pair);
        }
    }

    private void updateParams(NameValuePair pair) {
        final String value = pair.getValue();
        if (StringUtil.hasText(value)) {
            this.params.put(getValueParameterName(), pair.getValue());
            this.params.put(getDisplayParameterName(), pair.getName());
            this.params.put(getTitleSuffixContributionParameterName(), pair.getName());
        }
        else {
            this.params.remove(getValueParameterName());
            this.params.remove(getDisplayParameterName());
            this.params.remove(getTitleSuffixContributionParameterName());
        }
    }

    @Override
    public String getTitleSuffixContribution() {
        return this.params.get(getTitleSuffixContributionParameterName());
    }

    public abstract String getTitleSuffixContributionParameterName();

    protected abstract ListStore<NameValuePair> initStore();

    protected abstract String getDisplayParameterName();

    protected abstract String getValueParameterName();
}
