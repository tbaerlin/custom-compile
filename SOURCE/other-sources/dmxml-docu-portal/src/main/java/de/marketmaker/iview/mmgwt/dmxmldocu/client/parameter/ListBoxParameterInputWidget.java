/*
 * ListBoxParameterInputWidget.java
 *
 * Created on 29.03.12 16:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;

import de.marketmaker.iview.dmxmldocu.DmxmlBlockParameterDocumentation;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public abstract class ListBoxParameterInputWidget extends AbstractParameterInputWidget<ListBox> {

    public ListBoxParameterInputWidget(DmxmlBlockParameterDocumentation paramDocu) {
        super(paramDocu);
    }

    private final HashMap<String, Integer> entries = new HashMap<String, Integer>();

    @Override
    protected ListBox createInputWidget() {
        final ListBox listBox = new ListBox();
        listBox.setVisibleItemCount(1);
        final List<String> listValues = getListValues(getParameterDocu());
        int index = 0;
        for (String value : listValues) {
            listBox.addItem(value);
            entries.put(value, index++);
        }
        return listBox;
    }

    abstract List<String> getListValues(final DmxmlBlockParameterDocumentation paramDocu);

    @Override
    protected void configureInputWidget(ListBox inputWidget) {
        // somehow, ListBoxes need more width to have the same width !?
        inputWidget.setWidth("15.8em"); // $NON-NLS$
        inputWidget.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                notifyListeners();
            }
        });
    }


    @Override
    protected boolean isValid(String value) {
        return entries.containsKey(value);
    }

    @Override
    public String getParameterValue(ListBox inputWidget) {
        final int selectedIndex = inputWidget.getSelectedIndex();
        return inputWidget.getItemText(selectedIndex);
    }

    @Override
    public void setParameterValue(ListBox inputWidget, String value) {
        final Integer index = entries.get(value);
        if (index != null) {
            inputWidget.setSelectedIndex(index);
        }
    }
}
