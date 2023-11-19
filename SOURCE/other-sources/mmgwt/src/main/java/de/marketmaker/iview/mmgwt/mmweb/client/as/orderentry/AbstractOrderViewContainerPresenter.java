/*
 * AbstractOrderViewContainerPresenter.java
 *
 * Created on 28.02.13 13:16
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.history.OrderEntryHistorySupport;

/**
 * @author Markus Dick
 */
public abstract class AbstractOrderViewContainerPresenter
        implements OrderViewContainerDisplay.Presenter, HasPresenterDisposedHandlers {

    private final OrderViewContainerDisplay orderViewContainerDisplay;
    private final HandlerManager handlerManager;

    public AbstractOrderViewContainerPresenter(OrderViewContainerDisplay orderViewContainerDisplay) {
        this.orderViewContainerDisplay = orderViewContainerDisplay;
        this.handlerManager = new HandlerManager(this);
    }

    public final void dispose() {
        getOrderViewContainerDisplay().hide();
        firePresenterDisposedEvent();
    }

    protected void firePresenterDisposedEvent() {
        this.handlerManager.fireEvent(new PresenterDisposedEvent(this));
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    @Override
    public HandlerRegistration addPresenterDisposedHandler(PresenterDisposedHandler handler) {
        return this.handlerManager.addHandler(PresenterDisposedEvent.TYPE, handler);
    }

    @Override
    public void onCancelOrderClicked() {
        dispose();
    }

    @Override
    public void onOrderEntryHistoryItemSelected(OrderEntryHistorySupport.Item item) {
        /* provides an empty default impl. */
    }

    protected OrderViewContainerDisplay getOrderViewContainerDisplay() {
        return this.orderViewContainerDisplay;
    }
}
