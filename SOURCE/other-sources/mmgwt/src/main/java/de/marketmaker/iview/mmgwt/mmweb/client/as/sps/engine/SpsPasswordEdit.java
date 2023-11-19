/*
 * SpsPasswordEdit.java
 *
 * Created on 2015-01-22 09:48
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.PasswordTextBox;

/**
 * @author mdick
 */
public class SpsPasswordEdit extends SpsEditBase<SpsPasswordEdit, PasswordTextBox> {
    private boolean propertyUpdateOnKeyUp = false;

    public SpsPasswordEdit() {

    }

    //special feature for hand crafted SPS views
    //for PM provided SPS views not necessary
    public SpsPasswordEdit withPropertyUpdateOnKeyUp() {
        this.propertyUpdateOnKeyUp = true;
        return this;
    }

    @Override
    protected void onWidgetConfigured() {
        super.onWidgetConfigured();
        if(this.propertyUpdateOnKeyUp) {
            addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent keyUpEvent) {
                    updatePropertyOnKeyUp();
                }
            });
        }
    }

    private void updatePropertyOnKeyUp() {
        if(!getWidget().getText().equals(getBindFeature().getSpsProperty().getStringValue())) {
            getBindFeature().getSpsProperty().setValue(getStringValue());
        }
    }

    @Override
    protected PasswordTextBox createGwtWidget() {
        final PasswordTextBox passwordTextBox = new PasswordTextBox();
        if(getMaxLength() > 0) {
            passwordTextBox.setMaxLength(getMaxLength());
        }
        return passwordTextBox;
    }

    /**
     * Overwritten to avoid logging of entered value, which is done by sps-edit-base
     */
    @Override
    public void setValue(String value, boolean fireEvent) {
        getWidget().setValue(value, fireEvent);
    }
}
