package de.marketmaker.itools.gwtutil.client.widgets.input;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * User: umaurer
 * Date: 04.06.13
 * Time: 17:27
 */
public class RadioGroup<V> implements HasValueChangeHandlers<V>, HasFocusHandlers, HasBlurHandlers {
    private final List<Radio<V>> listRadios = new ArrayList<>();
    private final HandlerManager handlerManager = new HandlerManager(this);
    private final List<FocusHandler> focusHandlers = new ArrayList<>();
    private final List<BlurHandler> blurHandlers = new ArrayList<>();
    private Radio<V> checkedRadio = null;
    private boolean oneRadioShouldBeChecked = false;

    private final ValueChangeHandler<Boolean> valueChangeHandler = new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
            if (event.getValue()) {
                //noinspection unchecked
                setChecked((Radio<V>) event.getSource());
            }
        }
    };

    public Radio<V> add(V value, boolean checked) {
        assert(!checked || checkedRadio == null);
        final Radio<V> radio = new Radio<V>(checked).withValue(value);
        if (checked) {
            this.checkedRadio = radio;
        }
        this.listRadios.add(radio);
        radio.addValueChangeHandler(this.valueChangeHandler);
        radio.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                fire(event);
            }
        });
        radio.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                fire(event);
            }
        });
        return radio;
    }

    public void ensureOneRadioChecked() {
        this.oneRadioShouldBeChecked = true;
        if (this.checkedRadio != null) {
            return;
        }
        if (this.listRadios.isEmpty()) {
            return;
        }
        setChecked(this.listRadios.get(0));
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<V> handler) {
        return this.handlerManager.addHandler(ValueChangeEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    void fireValueChangeEvent(V value) {
        ValueChangeEvent.fire(this, value);
    }

    public void setChecked(Radio<V> radio) {
        setChecked(radio, true);
    }

    public void setChecked(Radio<V> radio, boolean fireEvent) {
        if (radio == null && this.oneRadioShouldBeChecked && !this.listRadios.isEmpty()) {
            radio = this.listRadios.get(0);
        }
        if (radio == this.checkedRadio) {
            return;
        }
        if (this.checkedRadio != null) {
            this.checkedRadio.setChecked(false, false);
        }
        this.checkedRadio = radio;
        if (this.checkedRadio != null) {
            this.checkedRadio.setChecked(true, false);
        }
        if (fireEvent) {
            fireValueChangeEvent(radio == null ? null : radio.getValue());
        }
    }

    public void setEnabled(boolean enabled) {
        for (Radio<V> radio : this.listRadios) {
            radio.setEnabled(enabled);
        }
    }

    public V getValue() {
        return this.checkedRadio == null ? null : this.checkedRadio.getValue();
    }

    public void setValue(V value) {
        setValue(value, false);
    }

    public void setValue(V value, boolean fireEvent) {
        if (value == null) {
            setChecked(null, fireEvent);
            return;
        }
        for (Radio<V> radio : this.listRadios) {
            if (value.equals(radio.getValue())) {
                setChecked(radio, fireEvent);
                return;
            }
        }
        throw new IllegalArgumentException("RadioGroup: cannot set value: " + value);
    }

    public boolean focusFirst() {
        if (this.listRadios.isEmpty()) {
            return false;
        }
        listRadios.get(0).setFocus(true);
        return true;
    }

    public boolean hasFocus() {
        for (Radio<V> radio : this.listRadios) {
            if (WidgetUtil.hasFocus(radio)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public HandlerRegistration addFocusHandler(final FocusHandler handler) {
        this.focusHandlers.add(handler);
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                focusHandlers.remove(handler);
            }
        };
    }

    @Override
    public HandlerRegistration addBlurHandler(final BlurHandler handler) {
        this.blurHandlers.add(handler);
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                blurHandlers.remove(handler);
            }
        };
    }

    private void fire(FocusEvent event) {
        for (FocusHandler handler : this.focusHandlers) {
            handler.onFocus(event);
        }
    }

    private void fire(BlurEvent event) {
        for (BlurHandler handler : this.blurHandlers) {
            handler.onBlur(event);
        }
    }
}
