package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.NameValuePair;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;

import java.util.HashMap;

/**
 * @author Ulrich Maurer
 *         Date: 13.04.11
 */
public class PmReportSnippetConfigForm extends FormPanel {
    private final HashMap<String, String> params;

    public PmReportSnippetConfigForm(SnippetConfigurationView scv) {
        setHeaderVisible(false);
        setBorders(false);
        setLabelWidth(120);
        setFieldWidth(200);

        this.params = scv.getParams();

        add(createTextField("reportId", "varuebersicht")); // $NON-NLS$
        add(createComboBox("inputObjectType", "INVESTORGROUP_BY_ID")); // $NON-NLS$
        add(createTextField("inputObject", "1000")); // $NON-NLS$
    }

    private TextField<String> createTextField(final String paramName, final String defaultValue) {
        final TextField<String> textField = new TextField<String>();
        textField.setFieldLabel(paramName);
        final String value = params.get(paramName);
        textField.setValue(value == null ? defaultValue : value);
        textField.addListener(Events.Change, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                params.put(paramName, textField.getValue());
            }
        });
        return textField;
    }

    private ComboBox<NameValuePair> createComboBox(final String paramName, final String defaultValue) {
        final ComboBox<NameValuePair> cb = new ComboBox<NameValuePair>();
        final ListStore<NameValuePair> store = initStore();
        cb.setForceSelection(true);
        cb.setMinChars(1);
        cb.setFieldLabel(paramName);
        cb.setStore(store);
        cb.setEditable(false);
        cb.setTriggerAction(ComboBox.TriggerAction.ALL);
        cb.setTypeAhead(true);
        cb.setSelectOnFocus(false);
        cb.setDisplayField(NameValuePair.DISPLAY_FIELD);
        cb.setValueField(NameValuePair.VALUE_FIELD);

        String value = params.get(paramName);
        if (value == null) {
            value = defaultValue;
        }
        for (int i = 0, n = store.getCount(); i < n; i++) {
            final NameValuePair pair = store.getAt(i);
            if (value.equals(pair.getValue())) {
                cb.setValue(pair);
                break;
            }
        }

        cb.addListener(Events.Select, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent event) {
                params.put(paramName, cb.getValue().getValue());
            }
        });

        return cb;
    }

    private ListStore<NameValuePair> initStore() {
        return NameValuePair.createStore(new String[][]{
                {"ACCOUNT_BY_ID", I18n.I.pmAccount()}, // $NON-NLS$
                {"INVESTOR_BY_ID",I18n.I.pmInvestor()}, // $NON-NLS$
                {"INVESTORGROUP_BY_ID",I18n.I.pmGroup()}, // $NON-NLS$
                {"SECURITIES_ACCOUNT_BY_ID",I18n.I.pmDepot()}, // $NON-NLS$
                {"PORTFOLIO_BY_ID",I18n.I.pmPortfolio()} // $NON-NLS$
        });
    }
}
