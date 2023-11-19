package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;

/**
 * Created on Oct 7, 2009 2:05:35 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public abstract class CalcChargeListener implements FocusHandler, ChangeHandler, KeyUpHandler {

    public void onFocus(FocusEvent event) {
        calcCharge();
    }

    public void onChange(ChangeEvent event) {
        calcCharge();
    }

    public void onKeyUp(KeyUpEvent event) {
        calcCharge();
    }

    protected abstract void calcCharge();
}
