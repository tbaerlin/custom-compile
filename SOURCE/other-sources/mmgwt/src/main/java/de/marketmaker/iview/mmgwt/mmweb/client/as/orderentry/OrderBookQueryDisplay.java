/*
 * OrderBookQueryDisplay.java
 *
 * Created on 14.10.13 17:31
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.user.client.ui.IsWidget;
import de.marketmaker.iview.pmxml.TextWithKey;

import java.util.List;

/**
 * @author Markus Dick
 */
public interface OrderBookQueryDisplay extends IsWidget {
    void setPresenter(Presenter presenter);

    void setWkn(String wkn);
    String getWkn();

    void setOrderExecutionStates(List<TextWithKey> orderExecutionStates);
    void setSelectedOrderExecutionState(TextWithKey selectedOrderExecutionState);
    TextWithKey getSelectedOrderExecutionState();

    void setOrderNumber(String orderNumber);
    String getOrderNumber();

    interface Presenter {
        void onOkClicked();
        void onCancelClicked();
    }
}
