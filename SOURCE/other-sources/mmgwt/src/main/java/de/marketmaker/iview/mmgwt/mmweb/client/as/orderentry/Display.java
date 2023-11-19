/*
 * Display.java
 *
 * Created on 12.08.13 11:55
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Markus Dick
 */
public interface Display<P extends Display.Presenter> {
    void setPresenter(P presenter);
    P getPresenter();

    Widget getOrderView();
    void setDepotNo(String depotNo);
    void setIsin(String isin);
    void reset();

    public interface Presenter extends HasPresenterDisposedHandlers {
        void show(OrderEntryContext context);
    }
}
