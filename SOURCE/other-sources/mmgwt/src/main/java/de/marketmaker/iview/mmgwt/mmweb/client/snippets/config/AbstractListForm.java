/*
 * AbstractListForm.java
 *
 * Created on 05.05.2008 14:15:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;

import java.util.Map;

/**
 * @author Ulrich Maurer
 */
abstract class AbstractListForm extends ContentPanel {
    private final Map<String, String> params;

    protected AbstractListForm(Map<String, String> params, final String fieldName) {
        setBorders(false);
        setLayout(new FitLayout());

        this.params = params;
        final String current = this.params.get(fieldName);

        final ListBox listBox = new ListBox();
        listBox.setVisibleItemCount(10);
        final String[][] values = initValues();
        for (String[] value : values) {
            listBox.addItem(value[1], value[0]);
            if (current != null && current.equals(value[0])) {
                listBox.setSelectedIndex(listBox.getItemCount() - 1);
            }
        }
        if (listBox.getSelectedIndex() < 0) {
            listBox.setSelectedIndex(0);
        }
        listBox.addChangeHandler(new ChangeHandler(){
            public void onChange(ChangeEvent event) {
                AbstractListForm.this.params.put(fieldName, listBox.getValue(listBox.getSelectedIndex()));
                AbstractListForm.this.params.put("titleSuffix", listBox.getItemText(listBox.getSelectedIndex())); // $NON-NLS-0$
            }
        });

        add(listBox);
    }

    protected abstract String[][] initValues();
}
