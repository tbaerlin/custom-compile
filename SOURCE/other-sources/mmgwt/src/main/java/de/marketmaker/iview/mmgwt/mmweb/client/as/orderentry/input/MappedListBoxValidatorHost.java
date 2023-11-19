/*
 * FILENAME
 *
 * Created on 16.08.13 08:32
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.formatters.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.Validator;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.MappedListBox;

import java.util.Arrays;
import java.util.List;

/**
 * @author Markus Dick
 */
public class MappedListBoxValidatorHost<T> extends AbstractFormatterValidatorHost<MappedListBox <T>, T> {
    private final MappedListBox<T> mappedListBox;

    @SafeVarargs
    public MappedListBoxValidatorHost(MappedListBox<T> mappedListBox, Validator<T>... validators) {
        super(null, Arrays.asList(validators));
        this.mappedListBox = mappedListBox;

        this.mappedListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                formatAndValidate();
            }
        });
    }

    @Override
    protected MappedListBox<T> getValueSource() {
        return this.mappedListBox;
    }

    @Override
    protected T getValue() {
        return this.mappedListBox.getSelectedItem();
    }

    @Override
    protected void setValue(T value) {
        /* do nothing */
    }

    @Override
    protected boolean isValueSourceEnabled() {
        return this.mappedListBox.isEnabled();
    }
}
