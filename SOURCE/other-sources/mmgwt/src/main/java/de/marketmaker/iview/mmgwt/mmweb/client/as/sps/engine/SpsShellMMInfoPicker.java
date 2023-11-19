/*
 * SpsShellMMInfoPicker.java
 *
 * Created on 16.04.2014 11:30
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.ShellMMInfoPicker;

import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.Collection;

/**
 * @author Markus Dick
 */
public class SpsShellMMInfoPicker extends SpsBoundWidget<ShellMMInfoPicker, SpsLeafProperty> implements ValueChangeHandler<ShellMMInfo>, HasEditWidget {
    private final ShellMMInfoPicker picker = new ShellMMInfoPicker();

    public SpsShellMMInfoPicker() {
        this.picker.setStyleName("sps-edit-shellMMInfo");
        this.picker.addValueChangeHandler(this);
    }

    public SpsShellMMInfoPicker withShellMMInfoLink(final String historyContextName) {
        this.picker.withHistoryContextSupplier(() -> SpsUtil.extractShellMMInfoHistoryContext(historyContextName, getBindFeature().getSpsProperty()));
        return this;
    }

    public void setShellMMTypes(Collection<ShellMMType> shellMMTypes) {
        this.picker.setShellMMTypes(shellMMTypes);
    }

    public void setSelectSymbolFormStyle(ShellMMInfoPicker.SelectSymbolFormStyle selectSymbolFormStyle) {
        this.picker.setSelectSymbolFormStyle(selectSymbolFormStyle);
    }

    @Override
    public void onValueChange(ValueChangeEvent<ShellMMInfo> event) {
        updateProperty(event.getValue());
        /* //log all securities of this list context. Uncomment for debugging. Please, do not delete.
        SpsUtil.consumeSameDescendantsOfNearestList(getBindFeature().getSpsProperty(), new Consumer<SpsProperty>() {
            @Override
            public void accept(SpsProperty property) {
                if(property instanceof SpsLeafProperty) {
                    Firebug.debug(((SpsLeafProperty) property).getStringValue());
                }
            }
        });*/
    }

    private void updateProperty(ShellMMInfo value) {
        getBindFeature().getSpsProperty().setValue(value, true, true);
    }

    @Override
    public void onPropertyChange() {
        final SpsLeafProperty spsProperty = getBindFeature().getSpsProperty();
        if(spsProperty.isShellMMInfo()) {
            this.picker.setValue(spsProperty.getShellMMInfo());
        }
    }

    @Override
    protected ShellMMInfoPicker createWidget() {
        return this.picker;
    }

    @Override
    public String getStringValue() {
        return this.picker.getStringValue();
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return this.picker.addKeyUpHandler(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        // ignore
    }
}
