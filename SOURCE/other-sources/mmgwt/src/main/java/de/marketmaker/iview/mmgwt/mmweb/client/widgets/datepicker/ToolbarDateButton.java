package de.marketmaker.iview.mmgwt.mmweb.client.widgets.datepicker;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import de.marketmaker.itools.gwtutil.client.util.date.DateListener;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DatePicker;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DatePickerPopup;


/**
 * @author umaurer
 * @author Markus Dick
 */
public class ToolbarDateButton extends Button implements HasValue<MmJsDate>, HasValueChangeHandlers<MmJsDate> {
    private static final String DATA_KEY = "value"; // $NON-NLS$
    private final DatePicker datePicker;

    private DatePickerPopup datePickerPopup = null;

    public ToolbarDateButton() {
        this(null);
    }

    public ToolbarDateButton(MmJsDate date) {
        this.datePicker = new DatePicker();
        this.datePicker.setDate(date);
        this.datePicker.addListener(new DateListener() {
            public void setDate(MmJsDate date) {
                ToolbarDateButton.this.setValue(date);
            }
        });
        addStyleName("mm-tbDateButton"); // $NON-NLS$
        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showDatePickerPopup();
            }
        });
    }

    public void setDate(MmJsDate date) {
        setValue(date);
    }

    public void setText(String text) {
        if (text == null || "".equals(text)) {
            super.setData(DATA_KEY, null);
            super.setText("");
            this.datePicker.setDate(new MmJsDate());

            ValueChangeEvent.fire(this, null);
        }
        else {
            final MmJsDate date = JsDateFormatter.parseDdmmyyyy(text);
            super.setData(DATA_KEY, date);
            super.setText(text);
            this.datePicker.setDate(date);

            ValueChangeEvent.fire(this, date);
        }
    }

    public void setMinDate(MmJsDate minDate) {
        this.datePicker.setMinDate(minDate);
    }

    public void setMaxDate(MmJsDate maxDate) {
        this.datePicker.setMaxDate(maxDate);
    }

    private void showDatePickerPopup() {
        if (this.datePickerPopup == null) {
            this.datePickerPopup = new DatePickerPopup(this.datePicker, this);
        }

        this.datePickerPopup.show();
    }

    public void addListener(DateListener dateListener) {
        this.datePicker.addListener(dateListener);
    }

    public void removeListener(DateListener dateListener) {
        this.datePicker.removeListener(dateListener);
    }

    @Override
    public MmJsDate getValue() {
        return (MmJsDate) getData(DATA_KEY);
    }

    @Override
    public void setValue(MmJsDate value) {
        setValue(value, true);
    }

    @Override
    public void setValue(MmJsDate value, boolean fireEvents) {
        final MmJsDate current = getValue();
        if(current == value || current != null && current.equals(value)) {
            return;
        }

        if (value == null) {
            super.setData(DATA_KEY, null);
            super.setText("");
            this.datePicker.setDate(new MmJsDate());
        }
        else {
            super.setData(DATA_KEY, value);
            super.setText(JsDateFormatter.formatDdmmyyyy(value));
            this.datePicker.setDate(value);
        }

        if(fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<MmJsDate> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
