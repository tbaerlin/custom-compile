/*
 * OrderBookDisplay.java
 *
 * Created on 05.09.13 16:04
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.IsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.IsPageControllerIsWidget;
import de.marketmaker.iview.pmxml.OrderbookDataType;

import java.util.List;

/**
 * @author Markus Dick
 */
public interface OrderBookDisplay<T extends OrderbookDataType> extends IsWidget {
    void setPresenter(Presenter<T> presenter);
    void setEntries(List<T> entries);
    String getPrintHtml();

    public interface Presenter<T> extends IsPageControllerIsWidget {
        void onShowQueryCriteriaClicked(ClickEvent event);
        void onCancelOrderClicked(ClickEvent event, T data);
        void onShowOrderClicked(ClickEvent event, T data);
        void onChangeOrderClicked(ClickEvent event, T data);

        boolean isShowOrderSupported();
        boolean isChangeOrderSupported();
        boolean isCancelOrderSupported();
    }
}
