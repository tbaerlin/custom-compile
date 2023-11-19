package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;

/**
 * Created on 07.10.11 09:38
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public abstract class SearchHandler {
    private final ClickHandler clickHandler;
    private final KeyUpHandler keyUpHandler;
    private final ChangeHandler changeHandler;
    private final ValueChangeHandler<String> strValueChangHandler;
    private final ValueChangeHandler<MmJsDate> dateValueChangHandler;


    protected SearchHandler() {
        this.clickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onSearch();
            }
        };
        this.keyUpHandler = new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    onSearch();
                }
            }
        };
        this.changeHandler = new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onSearch();
            }
        };
        this.strValueChangHandler = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> stringValueChangeEvent) {
                onSearch();
            }
        };
        this.dateValueChangHandler = new ValueChangeHandler<MmJsDate>() {
            @Override
            public void onValueChange(ValueChangeEvent<MmJsDate> mmJsDateValueChangeEvent) {
                onSearch();
            }
        };

    }

    public ChangeHandler getChangeHandler() {
        return changeHandler;
    }

    public ClickHandler getClickHandler() {
        return clickHandler;
    }

    public KeyUpHandler getKeyUpHandler() {
        return keyUpHandler;
    }

    public ValueChangeHandler<String> getStrValueChangHandler() {
        return strValueChangHandler;
    }

    public ValueChangeHandler<MmJsDate> getDateValueChangHandler() {
        return dateValueChangHandler;
    }

    public abstract void onSearch();

}
