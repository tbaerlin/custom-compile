/*
 * OrderDetailsDisplay.java
 *
 * Created on 13.11.13 11:17
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

/**
 * @author Markus Dick
 */
public interface OrderDetailsDisplay<P extends OrderDetailsDisplay.Presenter> extends OrderConfirmationDisplay<P> {
    void setReloadActive(boolean active);
    void setChangeOrderButtonVisible(boolean visible);

    public interface Presenter extends OrderConfirmationDisplay.Presenter {
        void onReloadClicked();
        void onChangeOrderClicked();
    }
}
