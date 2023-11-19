/*
 * PresenterDisposedEvent.java
 *
 * Created on 15.03.13 15:26
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Markus Dick
 */
public class PresenterDisposedEvent extends GwtEvent<PresenterDisposedHandler> {
    public static final Type<PresenterDisposedHandler> TYPE = new Type<PresenterDisposedHandler>();

    private final HasPresenterDisposedHandlers presenter;

    PresenterDisposedEvent(HasPresenterDisposedHandlers presenter) {
        this.presenter = presenter;
    }

    public static Type<PresenterDisposedHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<PresenterDisposedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PresenterDisposedHandler handler) {
        handler.onPresenterDisposed(this);
    }

    public HasPresenterDisposedHandlers getPresenter() {
        return this.presenter;
    }

    public boolean isHasReturnParameterMap() {
        return this.presenter instanceof HasReturnParameterMap;
    }

    public HasReturnParameterMap asHasReturnParameterMap() {
        if(!isHasReturnParameterMap()) {
            return null;
        }
        return (HasReturnParameterMap)this.presenter;
    }
}
