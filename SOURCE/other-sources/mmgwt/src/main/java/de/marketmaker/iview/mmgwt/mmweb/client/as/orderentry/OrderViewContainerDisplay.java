/*
 * OrderViewContainerDisplay.java
 *
 * Created on 24.01.13 14:27
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.history.OrderEntryHistorySupport;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.List;

/**
 * @author Markus Dick
 */
interface OrderViewContainerDisplay extends ContentContainer {
    String getTitle();
    void setTitle(String title);
    void show();
    void hide();
    OrderViewContainerDisplay withToolbar();
    OrderViewContainerDisplay addOrderHistoryTool(List<OrderEntryHistorySupport.Item> historyItems);
    void setExecuteOrderButtonText(String text);
    void setExecuteOrderButtonEnabled(boolean enabled);
    void setCancelOrderButtonEnabled(boolean enabled);

    void setButtonsLocked(boolean visible);

    /**
     * @throws IllegalArgumentException if the presenter has already been set
     */
    void setPresenter(Presenter orderPresenter);

    interface Presenter {
        void dispose();
        void onExecuteOrderClicked();
        void onCancelOrderClicked();
        void onOrderEntryHistoryItemSelected(OrderEntryHistorySupport.Item item);
    }
}
