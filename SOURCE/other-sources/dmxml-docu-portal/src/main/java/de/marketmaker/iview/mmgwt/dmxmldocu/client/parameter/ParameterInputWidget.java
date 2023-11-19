/*
 * ParameterInputWidget.java
 *
 * Created on 28.03.12 14:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.IsWidget;

import de.marketmaker.iview.dmxmldocu.DmxmlBlockParameterDocumentation;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public interface ParameterInputWidget {
    
    public static interface ChangeListener {
        void onParameterValueChanged();
    }

    void addWidgets(FlexTable table, int row);
    
    DmxmlBlockParameterDocumentation getParameterDocu();

    void addChangeListener(ChangeListener listener);

    void removeChangeListener(ChangeListener listener);
    
    String[] getParameterValues();

    void setParameterValue(String value);
    
    boolean isEnabled();
    
    void setEnabled(boolean enabled);
    
}
