/*
 * FormUtil.java
 *
 * Created on 10.02.2009 13:34:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class FormValidationHandler implements FocusHandler, ChangeHandler, KeyUpHandler {

    public void onFocus(FocusEvent event) {
        validateForm();
    }

    public void onChange(ChangeEvent event) {
        validateForm();
    }

    public void onKeyUp(KeyUpEvent event) {
        validateForm();
    }

    protected abstract void validateForm();
}
