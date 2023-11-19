/*
 * SpsEditBase.java
 *
 * Created on 14.01.14
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * Author: umaurer
 */
public abstract class SpsEditBase<S extends SpsEditBase<S, W>, W extends TextBoxBase> extends SpsBoundWidget<W, SpsLeafProperty> implements HasCaption, RequiresPropertyUpdateBeforeSave, HasEditWidget {
    private int maxLength;

    protected abstract W createGwtWidget();

    public SpsEditBase() {
    }

    public void setValue(String value, boolean fireEvent) {
        Firebug.debug("<" + getClass().getSimpleName()+".setValue> value=" + value + ", fireEvent=" + fireEvent);
        getWidget().setValue(value, fireEvent);
    }

    @SuppressWarnings(value = "unchecked")
    public S withCaption(String caption) {
        setCaption(caption);
        return (S)this;
    }

    @Override
    public String getStringValue() {
        return getWidget().getValue();
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return getWidget().addKeyUpHandler(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        getWidget().fireEvent(event);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getWidget().setEnabled(enabled);
    }

    @Override
    public void setReadonly(boolean readonly) {
        super.setReadonly(readonly);
        if (getWidget() != null) {
            getWidget().setEnabled(!readonly);
        }
    }

    @Override
    public void onPropertyChange() {
        setValue(getBindFeature().getSpsProperty().getStringValue(), false);
    }

    @Override
    protected W createWidget() {
        final W textBox = createGwtWidget();
        textBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                getBindFeature().getSpsProperty().setValue(getStringValue());
            }
        });
        textBox.setStyleName(getBaseStyle());
        textBox.setEnabled(!isReadonly());

        return textBox;
    }

    /**
     * @see SpsMultilineEdit#updatePropertyBeforeSave()
     */
    @Override
    public void updatePropertyBeforeSave() {
        if (getWidget() != null) {
            final SpsLeafProperty spsProperty = getBindFeature().getSpsProperty();
            if(spsProperty != null) {
                final String value = getStringValue();
                final String propertyValue = spsProperty.getStringValue();
                //an empty string may be the same as null, so leave the property as is
                if ((value == null || value.isEmpty()) && (propertyValue == null || propertyValue.isEmpty())) {
                    return;
                }
                spsProperty.setValue(value);
            }
        }
    }

    @SuppressWarnings(value = "unchecked")
    public S withMaxLength(String maxLength) {
        if(StringUtil.hasText(maxLength)) {
            try {
                this.maxLength = Integer.parseInt(maxLength);
            }
            catch(NumberFormatException e) {
                Firebug.warn("<" + getClass().getSimpleName() + ".withMaxLength>", e);
                this.maxLength = 0;
            }
        }
        return (S)this;
    }

    protected int getMaxLength() {
        return this.maxLength;
    }
}
