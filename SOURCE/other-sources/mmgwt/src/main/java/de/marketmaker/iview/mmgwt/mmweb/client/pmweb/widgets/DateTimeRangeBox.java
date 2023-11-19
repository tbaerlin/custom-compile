/*
 * DateTimeRangeBox
 *
 * Created on 24.03.2015 10:02
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DateBox;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
* @author mdick
*/
public class DateTimeRangeBox extends Composite implements HasValue<DateTimeRange> {
    private final DateBox startDateBox = new DateBox(true);
    private final DateBox endDateBox = new DateBox(true);

    public DateTimeRangeBox() {
        final Panel panel = new HorizontalPanel();
        panel.setStyleName("mm-datetime-range");
        initWidget(panel);

        panel.add(new Label(I18n.I.from()));
        panel.add(this.startDateBox);
        panel.add(new Label(I18n.I.to()));
        panel.add(this.endDateBox);

        final ValueChangeHandler<MmJsDate> valueChangeHandler = new ValueChangeHandler<MmJsDate>() {
            @Override
            public void onValueChange(ValueChangeEvent<MmJsDate> event) {
                ValueChangeEvent.fire(DateTimeRangeBox.this, getValue());
            }
        };

        this.startDateBox.addValueChangeHandler(valueChangeHandler);
        this.endDateBox.addValueChangeHandler(valueChangeHandler);
    }


    @Override
    public DateTimeRange getValue() {
        final MmJsDate startDateValue = this.startDateBox.getDate();
        final MmJsDate endDateValue = this.endDateBox.getDate();

        if(startDateValue == null && endDateValue == null) {
            return null;
        }

        return new DateTimeRange(startDateValue, endDateValue);
    }

    @Override
    public void setValue(DateTimeRange value) {
        setValue(value, false);
    }

    @Override
    public void setValue(DateTimeRange value, boolean fireEvents) {
        final DateTimeRange oldValue = getValue();

        if (value != null) {
            this.startDateBox.setDate(value.getBegin(), false);
            this.endDateBox.setDate(value.getEnd(), false);
        }
        else {
            this.startDateBox.setDate(null, false);
            this.endDateBox.setDate(null, false);
        }

        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, oldValue, getValue());
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DateTimeRange> handler) {
        return this.addHandler(handler, ValueChangeEvent.getType());
    }
}
